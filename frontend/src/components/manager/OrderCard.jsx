import React, { useState, useEffect } from "react";
import styled from "styled-components";
import { updateOrderItemFinished } from "../../api/manager/orderApi.js";

export default function OrderCard({
                                      tableNo,
                                      timeText = "",
                                      active = false,
                                      orderStatus = null, // 'PENDING' | 'APPROVED' | 'REJECTED' | 'FINISHED'
                                      items = [], // [{ id, name, qty, is_finished }]
                                      customerName = "",
                                      addAmount = 0,
                                      totalAmount = 0,
                                      onApprove,
                                      onReject,
                                      onClear,
                                      onReceiptClick,
                                      isHistory = true,
                                      orderId,
                                  }) {
    const isPending = active && orderStatus === "PENDING";
    const isApproved = active && orderStatus === "APPROVED";
    const isRejected = active && orderStatus === "REJECTED";
    const isFinished = active && orderStatus === "FINISHED";

    const [checks, setChecks] = useState({});

    useEffect(() => {
        const init = {};
        items.forEach((it, idx) => {
            // 주문이 FINISHED면 강제 true, 아니면 서버 값 반영
            init[idx] = orderStatus === "FINISHED" ? true : !!it.is_finished;
        });
        setChecks(init);
    }, [items, orderStatus]);

    const toggleCheck = async (idx) => {
        if (orderStatus === "FINISHED") return;

        const it = items[idx];
        if (!orderId || !it?.id) return;

        const newValue = !checks[idx];
        try {
            // ✅ 서버 API 호출
            await updateOrderItemFinished(orderId, it.id, newValue);

            // ✅ 성공하면 state 업데이트
            setChecks((m) => ({ ...m, [idx]: newValue }));
        } catch (e) {
            console.error(e);
            alert("항목 상태 변경에 실패했습니다.");
        }
    };

    return (
        <Card>
            <CardHead>
                <Title>
                    테이블 {tableNo} {timeText && <Time>{timeText}</Time>}
                </Title>
                {!isHistory && (
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
                            <ItemRow
                                key={`${it.id ?? it.name}-${i}`}
                                onClick={() => toggleCheck(i)}
                            >
                                <ItemName>{it.name}</ItemName>
                                <ItemQty>{it.qty}</ItemQty>
                                <ItemCheck $on={!!checks[i]}>
                                    {checks[i] ? "✓" : "□"}
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
                            <MetaKey>주문 금액</MetaKey>
                            <MetaVal>
                                {totalAmount.toLocaleString("ko-KR")}
                                <Won>원</Won>
                            </MetaVal>
                        </MetaRow>
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
    width: 260px;        /* ✅ 카드 폭 축소 */
    min-height: 360px;   /* ✅ 최소 높이 축소 */
    background: #fff;
    border: 1px solid #eee;
    border-radius: 16px;
    padding: 12px;
    display: flex;
    flex-direction: column;
    gap: 12px;
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
    gap: 10px;
    padding-top: 6px;
    max-height: 200px;   /* ✅ 스크롤 가능하도록 높이 제한 */
    overflow-y: auto;    /* ✅ 아이템 많으면 스크롤 */
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
    color: ${({ $on }) => ($on ? "#f05454" : "#bbb")};
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
