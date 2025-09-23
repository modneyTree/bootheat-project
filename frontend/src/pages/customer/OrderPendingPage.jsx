// src/pages/OrderPendingPage.jsx
import React, { useEffect, useState } from "react";
import styled, { keyframes } from "styled-components"; // ✅ keyframes 필요
import { useParams, useNavigate } from "react-router-dom";
import { useDispatch, useSelector, shallowEqual } from "react-redux";
import { clear } from "../../store/cartSlice.js";
import Header from "../../components/common/Header.jsx";
import { paths } from "../../routes/paths.js";
import { showSuccessToast } from "../../utils/toast.js";
import { getOrderDetail } from "../../api/customerApi.js";
import { selectOrderIdsByTable } from "../../store/orderIdsSlice.js";

export default function OrderPendingPage() {
  const { boothId, tableId } = useParams();
  const navigate = useNavigate();
  const dispatch = useDispatch();

  const reduxOrderIds = useSelector(
      selectOrderIdsByTable(Number(tableId)),
      shallowEqual
  );

  const [currentOrderId, setCurrentOrderId] = useState(null);

  useEffect(() => {
    dispatch(clear());

    let orderId = null;
    if (reduxOrderIds && reduxOrderIds.length > 0) {
      orderId = reduxOrderIds[reduxOrderIds.length - 1];
      localStorage.setItem("lastOrderNumber", String(orderId));
    } else {
      const stored = localStorage.getItem("lastOrderNumber");
      if (stored) orderId = Number(stored);
    }
    if (orderId) setCurrentOrderId(orderId);
  }, [reduxOrderIds, dispatch]);

  useEffect(() => {
    if (!currentOrderId) return;
    let interval;

    async function pollOrderStatus() {
      try {
        const data = await getOrderDetail(currentOrderId);
        const status = data?.customerOrder?.status;
        if (status === "APPROVED") {
          showSuccessToast("결제 확인이 완료되었습니다.");
          navigate(paths.complete(boothId, tableId, currentOrderId));
        }
      } catch (e) {
        console.error("주문 상세 조회 실패", e);
      }
    }

    pollOrderStatus();
    interval = setInterval(pollOrderStatus, 3000);
    return () => clearInterval(interval);
  }, [currentOrderId, boothId, tableId, navigate]);

  const goHome = () => navigate(paths.menu(boothId, tableId));

  return (
      <Page>
        <Header
            title="결제 확인"
            leftIcon={<span style={{ fontSize: 22 }}>×</span>}
            onLeft={goHome}
            rightIcon={<span />}
        />

        <Content>
          <MainText>주문 확인 중입니다…</MainText>
          {/* ✅ 원형 로딩 스피너 추가 */}
          <Spinner />
          <SubText>잠시만 기다려주세요.</SubText>
          <SubText>
            2분 동안 주문 확인이 없을 경우{"\n"}직원에게 문의해주세요 :)
          </SubText>
        </Content>

        <BottomBar>
          <OrderButton onClick={goHome}>확인</OrderButton>
        </BottomBar>
      </Page>
  );
}

/* ===== styled ===== */
const Page = styled.div`
  max-width: 560px;
  margin: 0 auto;
  padding-bottom: 120px;
`;

const Content = styled.div`
  padding: 24px 16px 0 16px;
  display: grid;
  place-items: center;
  row-gap: 24px;
  text-align: center;
  min-height: 60vh;
`;

const MainText = styled.h1`
  margin: 80px 0 0;
  font-size: 28px;
  font-weight: 900;
  letter-spacing: -0.3px;
`;

const SubText = styled.p`
  margin: 0;
  white-space: pre-line;
  color: #666;
  line-height: 1.6;
  font-size: 16px;
`;

const BottomBar = styled.div`
  position: fixed;
  left: 50%;
  bottom: 16px;
  transform: translateX(-50%);
  width: min(520px, 92vw);
  padding: 0 8px;
  z-index: 20;
`;

const OrderButton = styled.button`
  width: 100%;
  height: 56px;
  background: #ef6a3b;
  color: #fff;
  border: 0;
  border-radius: 16px;
  font-size: 18px;
  font-weight: 800;
  cursor: pointer;
  box-shadow: 0 6px 16px rgba(239, 106, 59, 0.25);
`;

/* ===== Spinner 스타일 ===== */
const spin = keyframes`
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
`;

const Spinner = styled.div`
  border: 6px solid #eee;
  border-top: 6px solid #ef6a3b; /* 주황색 포인트 */
  border-radius: 50%;
  width: 48px;
  height: 48px;
  animation: ${spin} 1s linear infinite;
`;
