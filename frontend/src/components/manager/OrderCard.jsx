// src/components/manager/OrderCard.jsx
import React, { useState, useEffect } from "react";
import styled from "styled-components";

export default function OrderCard({
  tableNo,
  timeText = "",
  active = false, //
  orderStatus = null, // 'PENDING' | 'APPROVED' | 'REJECTED' | 'FINISHED' | null
  items = [], // [{ name, qty }]
  customerName = "",
  addAmount = 0,
  totalAmount = 0,
  onApprove,
  onReject,
  onClear,
  onReceiptClick,
  isHistory = true,
}) {
  const isPending = active && orderStatus === "PENDING";
  const isApproved = active && orderStatus === "APPROVED";
  const isRejected = active && orderStatus === "REJECTED";
  const isFinished = active && orderStatus === "FINISHED";

  const [checks, setChecks] = useState({});

  useEffect(() => {
    console.log(items);
    const init = {};
    items.forEach((it, idx) => {
      init[idx] = orderStatus === "FINISHED" ? true : false; // FINISHED면 전부 체크
    });
    setChecks(init);
  }, [items, orderStatus]);

  const toggleCheck = (idx) => {
    // FINISHED 상태면 체크 변경 불가
    if (orderStatus === "FINISHED") return;
    setChecks((m) => ({ ...m, [idx]: !m[idx] }));
  };

  return (
    <Card>
      <CardHead>
        <Title>
          테이블 {tableNo} {timeText && <Time>{timeText}</Time>}
        </Title>
        {isHistory ? (
          <></>
        ) : (
          <ReceiptIcon
            role="button"
            onClick={onReceiptClick}
            title="영수증 보기"
          >
            🧾
          </ReceiptIcon>
        )}
      </CardHead>

      {!active ? (
        <EmptyWrap>
          <EmptyText>현재 테이블이 비어있습니다.</EmptyText>
        </EmptyWrap>
      ) : (
        <>
          <ItemList>
            {items.map((it, i) => (
              <ItemRow key={`${it.name}-${i}`} onClick={() => toggleCheck(i)}>
                <ItemName>{it.name}</ItemName>
                <ItemQty>{it.qty}</ItemQty>
                <ItemCheck $on={!!checks[i]}>
                  {checks[i] ? "✓" : "□"} {/* 체크 전에는 빈 사각형 표시 */}
                </ItemCheck>
              </ItemRow>
            ))}
          </ItemList>

          <Meta>
            <MetaRow>
              <MetaKey>주문자</MetaKey>
              <MetaVal>{customerName || "-"}</MetaVal>
            </MetaRow>
            <MetaRow>
              <MetaKey>추가 주문 금액</MetaKey>
              <MetaVal>
                {addAmount.toLocaleString("ko-KR")}
                <Won>원</Won>
              </MetaVal>
            </MetaRow>
            {/*<MetaRow>
              <MetaKey>총 주문 금액</MetaKey>
              <MetaVal>
                {totalAmount.toLocaleString("ko-KR")}
                <Won>원</Won>
              </MetaVal>
            </MetaRow>*/}
          </Meta>

          <Actions>
            {isPending && (
              <>
                <PrimaryBtn onClick={onApprove}>주문 수락</PrimaryBtn>
                <GhostBtn onClick={onReject}>거절</GhostBtn>
              </>
            )}
            {isApproved && <GhostBtn onClick={onClear}>비우기</GhostBtn>}
            {isRejected && <GhostBtn disabled>거절됨</GhostBtn>}
            {isFinished && <GhostBtn disabled>완료</GhostBtn>}
          </Actions>
        </>
      )}
    </Card>
  );
}

/* ===== styles ===== */
const Card = styled.article`
  width: 320px;
  min-height: 520px;
  background: #fff;
  border: 1px solid #eee;
  border-radius: 20px;
  padding: 20px 18px;
  display: flex;
  flex-direction: column;
  gap: 16px;
`;
const CardHead = styled.div`
  display: flex;
  align-items: center;
  justify-content: space-between;
`;
const Title = styled.h3`
  margin: 0;
  font-size: 22px;
  font-weight: 800;
  color: #111320;
  display: flex;
  align-items: center;
  gap: 10px;
`;
const Time = styled.span`
  font-size: 14px;
  font-weight: 700;
  color: #8a8a8a;
`;
const ReceiptIcon = styled.span`
  font-size: 22px;
  color: #1d2230;
  cursor: pointer;
`;
const EmptyWrap = styled.div`
  flex: 1;
  display: grid;
  place-items: center;
`;
const EmptyText = styled.p`
  margin: 0;
  color: #8a8a8a;
  font-weight: 600;
`;
const ItemList = styled.div`
  display: grid;
  gap: 14px;
  padding-top: 8px;
`;
const ItemRow = styled.div`
  display: grid;
  grid-template-columns: 1fr auto 24px;
  align-items: center;
  column-gap: 12px;
  cursor: pointer;
`;
const ItemName = styled.div`
  font-size: 18px;
  font-weight: 700;
  color: #222;
`;
const ItemQty = styled.div`
  font-size: 18px;
  font-weight: 700;
  color: #222;
`;
const ItemCheck = styled.div`
  justify-self: end;
  font-size: 20px;
  font-weight: 900;
  color: ${({ $on }) => ($on ? "#f05454" : "#bbb")}; /* 체크 전에는 회색 */
`;
const Meta = styled.div`
  margin-top: auto;
  display: grid;
  gap: 10px;
  padding-top: 6px;
`;
const MetaRow = styled.div`
  display: grid;
  grid-template-columns: 1fr auto;
  align-items: center;
`;
const MetaKey = styled.span`
  color: #444;
  font-weight: 700;
`;
const MetaVal = styled.span`
  font-weight: 800;
  font-size: 18px;
`;
const Won = styled.span`
  margin-left: 2px;
  font-size: 16px;
  font-weight: 700;
`;
const Actions = styled.div`
  display: grid;
  grid-auto-flow: column;
  gap: 12px;
  margin-top: 8px;
`;
const PrimaryBtn = styled.button`
  height: 52px;
  border: none;
  border-radius: 12px;
  background: #e96848;
  color: #fff;
  font-weight: 900;
  cursor: pointer;
  &:hover {
    filter: brightness(0.97);
  }
`;
const GhostBtn = styled.button`
  height: 52px;
  border: 1px solid #ddd;
  background: #e9e9e9;
  color: #222;
  font-weight: 900;
  border-radius: 12px;
  cursor: pointer;
  &:hover {
    filter: brightness(0.98);
  }
`;
