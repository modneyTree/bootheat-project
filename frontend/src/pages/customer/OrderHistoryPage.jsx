// src/pages/customer/OrderHistoryPage.jsx
import React, { useEffect, useRef, useState } from "react";
import styled from "styled-components";
import { useNavigate, useParams } from "react-router-dom";
import Header from "../../components/common/Header.jsx";
import { paths } from "../../routes/paths.js";
import customerApi, { listOrdersByLatestVisit } from "../../api/customerApi.js";

const STATUS_MAP = {
  PENDING:  { label: "확인 대기 중",      color: "#F59E0B" },
  APPROVED: { label: "승인 완료 (요리중)", color: "#3B82F6" },
  REJECTED: { label: "취소",              color: "#EF4444" },
  FINISHED: { label: "처리 완료",         color: "#10B981" },
};

function adaptOrder(o) {
  return {
    orderId: o.orderId,
    customerOrder: {
      status: o.status,
      created_at: o.createdAt,
    },
    orderItems: Array.isArray(o.items) ? o.items : [],
    paymentInfo: o.payment || {},
    orderCode: o.orderCode,
    visitId: o.visitId,
    totalAmount: o.totalAmount,
  };
}

export default function OrderHistoryPage() {
  // ⚠️ URL에서 받는 건 tableNumber
  const { boothId, tableId: tableNumberParam } = useParams();
  const navigate = useNavigate();

  const [resolvedTableId, setResolvedTableId] = useState(null);
  const [orders, setOrders]   = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError]     = useState(null);

  const didNavigateRef = useRef(false);

  const formatDate = (iso) => {
    const d = new Date(iso);
    const yyyy = d.getFullYear();
    const mm = `${d.getMonth() + 1}`.padStart(2, "0");
    const dd = `${d.getDate()}`.padStart(2, "0");
    const hh = `${d.getHours()}`.padStart(2, "0");
    const min = `${d.getMinutes()}`.padStart(2, "0");
    return `${yyyy}.${mm}.${dd} ${hh}:${min}`;
  };

  const goMenu = () => navigate(paths.menu(boothId, tableNumberParam));

  // ✅ tableNumber → tableId 변환
  useEffect(() => {
    let canceled = false;
    async function resolveTableId() {
      try {
        const { data } = await customerApi.client.get(`/booths/${boothId}/tables`);
        const rows = Array.isArray(data) ? data : [];

        const row = rows.find(t => t.tableNumber === Number(tableNumberParam));
        if (!canceled && row) {
          setResolvedTableId(row.tableId);
        }
      } catch (e) {
        console.error("테이블 변환 실패", e);
      }
    }
    resolveTableId();
    return () => { canceled = true; };
  }, [boothId, tableNumberParam]);

  // ✅ 최초 주문 목록 로딩
  useEffect(() => {
    if (!resolvedTableId) return;
    let canceled = false;

    async function fetchOrders() {
      setLoading(true);
      setError(null);
      try {
        const raw = await listOrdersByLatestVisit(Number(boothId), Number(resolvedTableId), 20);
        const adapted = (Array.isArray(raw) ? raw : []).map(adaptOrder);
        adapted.sort((a, b) => new Date(b.customerOrder.created_at) - new Date(a.customerOrder.created_at));
        if (!canceled) setOrders(adapted);
      } catch (e) {
        if (!canceled) {
          setOrders([]);
          setError("주문 내역을 불러오는 중 오류가 발생했습니다.");
          console.error(e);
        }
      } finally {
        if (!canceled) setLoading(false);
      }
    }
    fetchOrders();
    return () => { canceled = true; };
  }, [boothId, resolvedTableId]);

  // ✅ 폴링: 최신 주문 상태 감시
  useEffect(() => {
    if (!resolvedTableId) return;
    let canceled = false;

    const interval = setInterval(async () => {
      try {
        const raw = await listOrdersByLatestVisit(Number(boothId), Number(resolvedTableId), 5);
        if (!Array.isArray(raw) || raw.length === 0) return;

        const sorted = [...raw].sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt));
        const latest = sorted[0];

        if (latest?.status === "APPROVED" && !canceled && !didNavigateRef.current) {
          didNavigateRef.current = true;
          clearInterval(interval);
          navigate(paths.orderComplete(boothId, tableNumberParam)); // ✅ 이동은 여전히 tableNumber 사용
        }
      } catch (e) {
        console.warn("주문 상태 확인 실패", e);
      }
    }, 3000);

    return () => {
      canceled = true;
      clearInterval(interval);
    };
  }, [boothId, resolvedTableId, navigate, tableNumberParam]);

  return (
      <Page>
        <Header
            title={`${tableNumberParam}번 주문 내역`}
            leftIcon={<span style={{ fontSize: 22 }}>×</span>}
            onLeft={goMenu}
            rightIcon={<span />}
        />

        {loading ? (
            <List>
              <Card><Skeleton>주문 내역을 불러오는 중…</Skeleton></Card>
              <Card><Skeleton>주문 내역을 불러오는 중…</Skeleton></Card>
            </List>
        ) : error ? (
            <List>
              <Card><ErrorText>{error}</ErrorText></Card>
            </List>
        ) : orders.length === 0 ? (
            <EmptyBox>
              <div>주문 내역이 없습니다.</div>
              <SmallBtn onClick={goMenu}>메뉴로 가기</SmallBtn>
            </EmptyBox>
        ) : (
            <List>
              {orders.map((o) => {
                const stat = STATUS_MAP[o.customerOrder.status] || STATUS_MAP.PENDING;
                const itemCount = Array.isArray(o.orderItems) ? o.orderItems.length : 0;
                const amount = o.paymentInfo?.amount || 0;

                return (
                    <Card key={o.orderId}>
                      <TopRow>
                        <OrderTitle>{formatDate(o.customerOrder.created_at)} 주문</OrderTitle>
                        <Status>
                          <Dot style={{ background: stat.color }} />
                          <StatusText style={{ color: stat.color }}>{stat.label}</StatusText>
                        </Status>
                      </TopRow>

                      <Sub>{o.orderCode ? o.orderCode : `ODR${o.orderId}`}</Sub>

                      <MetaRow>
                        <MetaCol>
                          <MetaLabel>총 금액</MetaLabel>
                          <MetaStrong>{amount.toLocaleString()}원</MetaStrong>
                        </MetaCol>
                        <MetaColRight>
                          <MetaLabel>수량</MetaLabel>
                          <MetaStrong>{itemCount}</MetaStrong>
                        </MetaColRight>
                      </MetaRow>
                    </Card>
                );
              })}
            </List>
        )}
      </Page>
  );
}

/* ===== styled ===== */
const Page = styled.div`max-width: 560px; margin: 0 auto;`;
const List = styled.div`padding: 12px 16px 24px; display: grid; gap: 16px;`;
const Card = styled.div`border: 2px dashed #d9d9d9; border-radius: 16px; padding: 16px; background: #fff;`;
const TopRow = styled.div`display: flex; align-items: center; gap: 8px;`;
const OrderTitle = styled.div`flex: 1; font-weight: 800; font-size: 18px;`;
const Status = styled.div`display: inline-flex; align-items: center; gap: 6px;`;
const Dot = styled.span`width: 10px; height: 10px; border-radius: 999px; display: inline-block;`;
const StatusText = styled.span`font-weight: 700; font-size: 14px;`;
const Sub = styled.div`margin-top: 4px; color: #9aa0a6; font-size: 14px;`;
const MetaRow = styled.div`margin-top: 18px; display: grid; grid-template-columns: 1fr 1fr;`;
const MetaCol = styled.div``;
const MetaColRight = styled(MetaCol)`text-align: right;`;
const MetaLabel = styled.div`color: #a7a7a7; font-size: 14px;`;
const MetaStrong = styled.div`margin-top: 6px; font-weight: 900; font-size: 18px;`;
const Skeleton = styled.div`color: #9aa0a6; font-size: 14px;`;
const ErrorText = styled.div`color: #ef4444; font-size: 14px;`;
const EmptyBox = styled.div`padding: 40px 16px; text-align: center; color: #6b7280; display: grid; gap: 12px;`;
const SmallBtn = styled.button`
  display: inline-block;
  margin: 0 auto;
  padding: 8px 12px;
  border-radius: 10px;
  border: 1px solid #ddd;
  background: #fff;
  cursor: pointer;
`;
