import React, { useEffect, useMemo, useRef, useState } from "react";
import styled from "styled-components";
import Modal from "../common/manager/Modal.jsx";
import OrderCard from "./OrderCard.jsx";

import {
  getTableOrders,
  setOrderStatus,
  updateOrderItemFinished,
} from "../../api/manager/orderApi.js";

export default function OrderHistoryModal({
                                            open,
                                            boothId,
                                            tableId,
                                            tableNumber,
                                            onClose,
                                          }) {
  const [loading, setLoading] = useState(false);
  const [orders, setOrders] = useState([]);
  const [error, setError] = useState(null);

  const listRef = useRef(null);
  const colRefs = useRef([]);

  const reload = async () => {
    const data = await getTableOrders(boothId, tableId);
    setOrders(Array.isArray(data) ? data : []);
  };

  useEffect(() => {
    if (!open || !boothId || !tableId) return;
    setLoading(true);
    setError(null);
    (async () => {
      try {
        await reload();
      } catch (e) {
        setError("주문 이력을 불러오지 못했습니다.");
      } finally {
        setLoading(false);
      }
    })();
  }, [open, boothId, tableId]);

  // 최신순 정렬
  const sorted = useMemo(
      () =>
          [...orders].sort(
              (a, b) =>
                  new Date(b?.customerOrder?.created_at).getTime() -
                  new Date(a?.customerOrder?.created_at).getTime()
          ),
      [orders]
  );

  const fmtHM = (iso) => {
    if (!iso) return "";
    const d = new Date(iso);
    return `${String(d.getHours()).padStart(2, "0")}:${String(
        d.getMinutes()
    ).padStart(2, "0")}`;
  };
  const fmtYMD = (iso) => {
    if (!iso) return "";
    const d = new Date(iso);
    return `${d.getFullYear()}.${String(d.getMonth() + 1).padStart(
        2,
        "0"
    )}.${String(d.getDate()).padStart(2, "0")}`;
  };

  // 공통 상태 변경
  const handleSetStatus = async (orderId, next) => {
    try {
      await setOrderStatus(orderId, next);
      await reload();
    } catch (e) {
      alert(`주문 상태 변경 실패: ${next}`);
    }
  };

  // ✅ 개별 아이템 토글
  const handleToggleItem = async (orderId, item) => {
    if (!orderId || !item?.id) return;
    try {
      await updateOrderItemFinished(orderId, item.id, !item.is_finished);
      await reload();
    } catch (e) {
      alert("항목 상태 변경 실패");
    }
  };

  // API 응답 → OrderCard props 매핑
  const toCardProps = (o) => {
    const co = o?.customerOrder || {};
    const status = (co.status || "").toUpperCase();
    const amount = o?.paymentInfo?.amount ?? co.total_amount ?? 0;
    const id = co.order_id;

    return {
      tableNo: tableNumber,
      timeText: `${fmtYMD(co.created_at)} ${fmtHM(co.created_at)}`,
      orderId: id,
      active: true,
      orderStatus: status,
      items: (o?.orderItems || []).map((it) => ({
        id: it.order_item_id ?? it.id,
        name: it.name,
        qty: it.quantity ?? 0,
        is_finished:
            typeof it.is_finished === "boolean" ? it.is_finished : false,
      })),
      customerName: o?.paymentInfo?.payer_name || "-",
      addAmount: "-",
      totalAmount: amount,
      onApprove: () => handleSetStatus(id, "APPROVED"),
      onReject: () => handleSetStatus(id, "REJECTED"),
      onClear: () => handleSetStatus(id, "FINISHED"),
      onReceiptClick: () => {},
      // ✅ 전달
      onToggleItem: (item) => handleToggleItem(id, item),
    };
  };

  return (
      <Modal open={open} title={`테이블 ${tableNumber}`} onClose={onClose}>
        {loading && <Empty>불러오는 중…</Empty>}
        {error && <Empty>{error}</Empty>}
        {!loading && !error && sorted.length === 0 && (
            <Empty>이 테이블의 주문 이력이 없습니다.</Empty>
        )}
        {!loading && !error && sorted.length > 0 && (
            <List ref={listRef}>
              {sorted.map((o, i) => (
                  <CardWrap
                      key={o?.customerOrder?.order_id ?? `${i}`}
                      ref={(el) => (colRefs.current[i] = el)}
                  >
                    <OrderCard {...toCardProps(o)} />
                  </CardWrap>
              ))}
            </List>
        )}
      </Modal>
  );
}

/* ===== styles ===== */
const Empty = styled.div`
  padding: 40px 8px;
  color: #888;
  text-align: center;
  font-weight: 600;
`;

const List = styled.div`
  display: grid;
  grid-auto-flow: column;
  grid-auto-columns: 260px; /* ✅ 카드 폭 축소 */
  gap: 12px;                /* ✅ 카드 간격 축소 */
  overflow-x: auto;
  padding: 8px 6px 6px 6px;
`;

const CardWrap = styled.div``;
