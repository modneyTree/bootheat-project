// src/api/manager/orderApi.js
import axios from "axios";
import { BASE_URL, API_MANAGER_ORDERS } from "../api.js";

/** axios 인스턴스 (쿠키 포함) */
const client = axios.create({
  baseURL: BASE_URL,
  withCredentials: true,
  headers: { "Content-Type": "application/json" },
});

/** 공통 에러 핸들러 */
function handleApiError(err) {
  const status = err?.response?.status;
  const msg =
      err?.response?.data?.message ||
      err?.message ||
      "Unknown error while calling order API";
  const wrapped = new Error(`[${status || "ERR"}] ${msg}`);
  wrapped.status = status;
  wrapped.cause = err;
  throw wrapped;
}

/** 상태값 검증 */
const VALID_STATUSES = ["PENDING", "APPROVED", "REJECTED", "FINISHED"];
function assertValidStatus(status) {
  if (!VALID_STATUSES.includes(status)) {
    throw new Error(
        `Invalid status "${status}". Use one of: ${VALID_STATUSES.join(", ")}`
    );
  }
}

/** 1) 주문 상태 변경 (통합 엔드포인트) */
export async function setOrderStatus(orderId, status) {
  try {
    assertValidStatus(status);
    const url = API_MANAGER_ORDERS.SET_STATUS(orderId, status);
    const body = { order_id: Number(orderId), status };
    const res = await client.post(url, body);
    return res.data ?? true;
  } catch (err) {
    handleApiError(err);
  }
}

/** 편의 함수들 */
export async function approveOrder(orderId) {
  try {
    return await setOrderStatus(orderId, "APPROVED");
  } catch (err) {
    handleApiError(err);
  }
}
export async function rejectOrder(orderId) {
  try {
    return await setOrderStatus(orderId, "REJECTED");
  } catch (err) {
    handleApiError(err);
  }
}
export async function pendingOrder(orderId) {
  try {
    return await setOrderStatus(orderId, "PENDING");
  } catch (err) {
    handleApiError(err);
  }
}
export async function finishOrder(orderId) {
  try {
    return await setOrderStatus(orderId, "FINISHED");
  } catch (err) {
    handleApiError(err);
  }
}

/** 2) 테이블 비우기(현재 visit 종료, active=false) */
export async function closeVisit(tableId) {
  try {
    const url = API_MANAGER_ORDERS.CLOSE_VISIT(tableId);
    const res = await client.post(url, { tableId: Number(tableId) });
    return res.data ?? true;
  } catch (err) {
    handleApiError(err);
  }
}

/** 3) 부스의 테이블 목록 조회 */
export async function getTablesByBooth(boothId) {
  try {
    const url = API_MANAGER_ORDERS.GET_TABLES_BY_BOOTH(boothId);
    const res = await client.get(url);
    // 기대 응답: [{ tableId, tableNumber, active }, ...]
    return res.data;
  } catch (err) {
    handleApiError(err);
  }
}

/** 4) 해당 Table의 최신 visit의 주문 ID 배열 */
export async function getLatestVisitOrderIds(tableId) {
  try {
    const url = API_MANAGER_ORDERS.GET_LATEST_VISIT_ORDER_IDS(tableId);
    const res = await client.get(url);
    // 백엔드 구현에 따라 배열 또는 { orderIds: [] } 둘 다 수용
    if (Array.isArray(res.data)) return res.data;
    return res.data?.orderIds ?? [];
  } catch (err) {
    handleApiError(err);
  }
}

/** 5) 주문 단건 상세 조회 */
export async function getOrderDetail(orderId) {
  try {
    const url = API_MANAGER_ORDERS.GET_ORDER_DETAIL(orderId);
    const res = await client.get(url);
    return res.data;
  } catch (err) {
    handleApiError(err);
  }
}

/** 6) 특정 부스-테이블의 모든 주문 이력 */
export async function getTableOrders(boothId, tableId) {
  try {
    const url = API_MANAGER_ORDERS.GET_TABLE_ORDERS(boothId, tableId);
    const res = await client.get(url);
    // 기대 응답: [{ customerOrder, orderItems, paymentInfo }, ...]
    return res.data ?? [];
  } catch (err) {
    handleApiError(err);
  }
}

/** 7) 테이블 생성 (좌석/테이블 추가) */
export async function createTable(boothId) {
  try {
    const url = API_MANAGER_ORDERS.CREATE_TABLE(boothId);
    const res = await client.post(url); // body 없음
    return res.data ?? true;
  } catch (err) {
    handleApiError(err);
  }
}

/** 8) (신규) 특정 라인아이템 완료/취소
 *  PATCH /api/manager/orders/{orderId}/items/{itemId}/finished?finished=true|false
 */
export async function updateOrderItemFinished(orderId, itemId, finished) {
  try {
    const url = API_MANAGER_ORDERS.UPDATE_ITEM_FINISHED(orderId, itemId, finished);
    // 쿼리스트링 기반이라 body 없음. (서버가 body 받도록 했으면 {} 대신 { isFinished: finished } 사용)
    const res = await client.patch(url, null, { headers: { Accept: "application/json" } });
    return res.data ?? true;
  } catch (err) {
    handleApiError(err);
  }
}

/** 9) (신규) 주문의 모든 라인아이템 일괄 완료/취소
 *  PATCH /api/manager/orders/{orderId}/items/finished?finished=true|false
 */
export async function updateAllOrderItemsFinished(orderId, finished) {
  try {
    const url = API_MANAGER_ORDERS.UPDATE_ALL_ITEMS_FINISHED(orderId, finished);
    const res = await client.patch(url, null, { headers: { Accept: "application/json" } });
    return res.data ?? true;
  } catch (err) {
    handleApiError(err);
  }
}

/** 묶어서 export (편의) */
const orderApi = {
  setOrderStatus,
  approveOrder,
  rejectOrder,
  pendingOrder,
  finishOrder,
  closeVisit,
  getTablesByBooth,
  getLatestVisitOrderIds,
  getOrderDetail,
  getTableOrders,
  createTable,
  updateOrderItemFinished,
  updateAllOrderItemsFinished,
};

export default orderApi;
