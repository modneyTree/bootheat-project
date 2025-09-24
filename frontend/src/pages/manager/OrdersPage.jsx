// src/pages/manager/Manager/OrderPage.jsx
import React, { useEffect, useMemo, useState, useCallback, useRef } from "react";
import styled from "styled-components";
import { useParams } from "react-router-dom";
import {
  getTablesByBooth,
  getLatestVisitOrderIds,
  getOrderDetail,
  approveOrder,
  rejectOrder,
  closeVisit,
  createTable,
  setOrderStatus,
  updateOrderItemFinished,
  updateAllOrderItemsFinished,
} from "../../api/manager/orderApi.js";
import AppLayout from "../../components/common/manager/AppLayout.jsx";
import OrderCard from "../../components/manager/OrderCard.jsx";
import OrderHistoryModal from "../../components/manager/OrderHistoryModal.jsx";

/* ========== utils ========== */
function formatTime(ts) {
  if (!ts) return "";
  const d = new Date(ts);
  const hh = String(d.getHours()).padStart(2, "0");
  const mm = String(d.getMinutes()).padStart(2, "0");
  return `${hh}:${mm}`;
}
function getCO(o) {
  return o?.customerOrder || {};
}
function getPI(o) {
  return o?.paymentInfo || {};
}
function getCreatedAt(o) {
  return getCO(o).created_at ?? null;
}
function getStatus(o) {
  return (getCO(o).status ?? "PENDING").toUpperCase();
}
function getAmount(o) {
  return getPI(o).amount ?? getCO(o).total_amount ?? 0;
}
function getPayerName(o) {
  return getPI(o).payer_name ?? "";
}
function getItemsRaw(o) {
  return Array.isArray(o?.orderItems) ? o.orderItems : [];
}
function getOrderId(o) {
  return getCO(o).order_id ?? o?.orderId ?? o?.id ?? null;
}
const ts = (v) => (v ? new Date(v).getTime() : -Infinity);

/** 최신 주문 1건을 OrderCard props로 변환 */
function toLatestCardProps(table, latestOrder) {
  if (!table?.active || !latestOrder) {
    return {
      tableNo: table?.tableNumber ?? "-",
      timeText: "",
      active: false,
      orderStatus: null,
      items: [],
      customerName: "",
      addAmount: 0,
      totalAmount: 0,
      orderId: null,
    };
  }

  const status = getStatus(latestOrder);
  const timeText = formatTime(getCreatedAt(latestOrder));
  const amount = getAmount(latestOrder);
  const orderId = getOrderId(latestOrder);
  const payer = getPayerName(latestOrder) || "-";

  // ✅ order_item_id → id로 매핑, is_finished 그대로 사용
  const items = getItemsRaw(latestOrder).map((it) => ({
    id: it.order_item_id ?? it.id,
    name: it.name,
    qty: it.qty ?? it.quantity ?? 0,
    is_finished: typeof it.is_finished === "boolean" ? it.is_finished : false,
  }));

  return {
    tableNo: table.tableNumber,
    timeText,
    active: true,
    orderStatus: status,
    items,
    customerName: payer,
    addAmount: amount,
    totalAmount: amount,
    orderId,
  };
}

function pickLatestPending(orders = []) {
  return [...orders]
      .filter((o) => getStatus(o) === "PENDING")
      .sort((a, b) => ts(getCreatedAt(b)) - ts(getCreatedAt(a)))[0];
}

/* ========== component ========== */
export default function ManagerOrderPage() {
  const { boothId } = useParams();
  const boothNum = Number(boothId);

  const [loading, setLoading] = useState(true);
  const [tables, setTables] = useState([]);
  const [ordersByTable, setOrdersByTable] = useState({});

  const [historyOpen, setHistoryOpen] = useState(false);
  const [historyTable, setHistoryTable] = useState(null);

  const loadingRef = useRef(false);

  const load = useCallback(async () => {
    if (!boothNum) return;
    if (loadingRef.current) return; // 중복 요청 방지
    loadingRef.current = true;

    try {
      setLoading(true);
      const tableList = await getTablesByBooth(boothNum);
      const safeTables = Array.isArray(tableList) ? tableList : [];
      setTables(safeTables);

      const idsByTable = await Promise.all(
          safeTables.map(async (t) => {
            try {
              const ids = await getLatestVisitOrderIds(t.tableId);
              return {
                tableId: t.tableId,
                ids: Array.isArray(ids) ? ids : ids?.orderIds ?? [],
              };
            } catch {
              return { tableId: t.tableId, ids: [] };
            }
          })
      );

      const detailPairs = await Promise.all(
          idsByTable.map(async ({ tableId, ids }) => {
            if (!ids.length) return { tableId, details: [] };
            const details = await Promise.all(
                ids.map(async (oid) => {
                  try {
                    return await getOrderDetail(oid);
                  } catch {
                    return null;
                  }
                })
            );
            return { tableId, details: details.filter(Boolean) };
          })
      );

      const map = {};
      detailPairs.forEach(({ tableId, details }) => {
        map[tableId] = details;
      });
      setOrdersByTable(map);
    } finally {
      setLoading(false);
      loadingRef.current = false;
    }
  }, [boothNum]);

  // ✅ 초기 로드 + 10초마다 자동 업데이트 (탭 비활성화 시 정지)
  useEffect(() => {
    load(); // 첫 실행

    const id = setInterval(() => {
      if (document.visibilityState === "visible") {
        load();
      }
    }, 10000);

    return () => clearInterval(id);
  }, [load]);

  const handleApprove = async (tableId) => {
    const list = ordersByTable[tableId] || [];
    const target = pickLatestPending(list);
    const id = getOrderId(target);
    if (!id) return;
    try {
      await approveOrder(id);
      load();
    } catch (e) {
      console.error(e);
      alert("주문 수락에 실패했습니다.");
    }
  };

  const handleReject = async (tableId) => {
    const list = ordersByTable[tableId] || [];
    const target = pickLatestPending(list);
    const id = getOrderId(target);
    if (!id) return;
    try {
      await rejectOrder(id);
      load();
    } catch (e) {
      console.error(e);
      alert("주문 거절에 실패했습니다.");
    }
  };

  const handleToggleItem = async (orderId, item) => {
    if (!orderId || !item?.id) return;
    try {
      await updateOrderItemFinished(orderId, item.id, !item.is_finished);
      load();
    } catch (e) {
      console.error(e);
      alert("항목 상태 변경에 실패했습니다.");
    }
  };

  const handleClear = async (tableId, orderIds = []) => {
    const ok = window.confirm("정말로 비우시겠습니까?");
    if (!ok) return;

    try {
      await Promise.allSettled(
          orderIds.map((oid) => updateAllOrderItemsFinished(oid, true))
      );
      await Promise.allSettled(
          orderIds.map((oid) => setOrderStatus(oid, "FINISHED"))
      );
      await closeVisit(tableId);
    } catch (e) {
      console.error("비우기 처리 중 오류", e);
      alert("일부 주문 비우기 처리에 실패했습니다. 새로고침 후 상태를 확인하세요.");
    }
    load();
  };

  const handleReceiptClick = (tableId) => {
    const t = tables.find((x) => x.tableId === tableId);
    if (!t) return;
    setHistoryTable({ tableId: t.tableId, tableNumber: t.tableNumber });
    setHistoryOpen(true);
  };

  const handleCreateTable = async () => {
    await createTable(boothNum);
    load();
  };

  const cards = useMemo(() => {
    return (tables || []).map((t) => {
      const details = ordersByTable[t.tableId] || [];
      const latest = details.reduce(
          (acc, o) => (ts(getCreatedAt(o)) > ts(getCreatedAt(acc)) ? o : acc),
          null
      );

      const cardProps = toLatestCardProps(t, latest);
      const orderIds = details.map((o) => getOrderId(o)).filter(Boolean);

      return {
        table: t,
        cardProps,
        orderIds,
        orders: details,
      };
    });
  }, [tables, ordersByTable]);

  return (
      <AppLayout title="주문 관리">
        <TopBar>
          <Left>
            <H1>부스 #{boothId} 주문 현황</H1>
            {!loading && <CountText>총 {tables.length}개 테이블</CountText>}
          </Left>
          <Right>
            <CreateBtn onClick={handleCreateTable}>테이블 새로 생성</CreateBtn>
            <RefreshBtn onClick={load}>새로고침</RefreshBtn>
          </Right>
        </TopBar>

        {loading ? (
            <LoaderWrap>불러오는 중...</LoaderWrap>
        ) : (
            <Grid>
              {cards.map(({ table, cardProps, orderIds }) => (
                  <OrderCard
                      key={table.tableId}
                      {...cardProps}
                      onApprove={() => handleApprove(table.tableId)}
                      onReject={() => handleReject(table.tableId)}
                      onClear={() => handleClear(table.tableId, orderIds)}
                      onReceiptClick={() => handleReceiptClick(table.tableId)}
                      isHistory={false}
                      onToggleItem={(item) =>
                          handleToggleItem(cardProps.orderId, item)
                      }
                  />
              ))}
            </Grid>
        )}

        <OrderHistoryModal
            open={historyOpen}
            boothId={boothNum}
            tableId={historyTable?.tableId}
            tableNumber={historyTable?.tableNumber}
            onClose={() => setHistoryOpen(false)}
        />
      </AppLayout>
  );
}

/* ========== styled ========== */
const TopBar = styled.div`
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  margin-bottom: 20px;
`;
const Left = styled.div``;
const Right = styled.div`
  display: flex;
  gap: 10px;
`;
const H1 = styled.h2`
  font-size: 20px;
  margin: 0;
`;
const CountText = styled.p`
  margin: 4px 0 0 0;
  color: #888;
  font-size: 13px;
`;
const Grid = styled.div`
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(240px, 1fr)); /* ✅ 카드 축소 대응 */
  gap: 12px; /* ✅ 카드 간 간격 줄임 */
`;
const LoaderWrap = styled.div`
  padding: 60px 0;
  text-align: center;
  color: #666;
`;
const ButtonBase = styled.button`
  border: 0;
  border-radius: 10px;
  padding: 10px 14px;
  font-weight: 600;
  cursor: pointer;
`;
const CreateBtn = styled(ButtonBase)`
  background: #111;
  color: #fff;
`;
const RefreshBtn = styled(ButtonBase)`
  background: #f1f3f5;
  color: #111;
`;
