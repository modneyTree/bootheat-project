# BoothEat (부스잇)
### QR 코드 기반 축제 결제 프로토타입 (Backend 중심)

BoothEat은 대학 축제 및 동아리 행사에서 발생하는  
**결제 대기 시간과 주문 오류 문제를 해결하기 위해**  
테이블별 QR 코드로 주문을 받고, 매니저가 입금 내역을 확인해  
**승인/거절**하는 모바일 키오스크형 결제 시스템입니다.

본 프로젝트는 **3일 프로토타입 개발** 이후  
👉 **실제 동아리 행사에서 운영된 실사용 서비스**입니다.

---

## Summary
- 역할: **백엔드 개발 (설계 · 구현 · 배포)**
- 협업: 기획자 1명 / 프론트엔드 1명 / 백엔드 1명
- 기술 스택:
  - Backend: **Spring Boot, Spring Data JPA**
  - DB: **H2 (초기) → MySQL 전환 고려**
  - Frontend: React
  - Infra: **AWS Lightsail**
- 핵심 기능:
  - 주문 상태 관리 (**PENDING / APPROVED / REJECTED / FINISHED**)
  - 메뉴 품절 토글 (`available`)
  - 매출 · 메뉴 랭킹 집계 (GROUP BY)
  - **할인코드 적용 로직**
- 배포: **AWS Lightsail**
- 도메인: https://modney.shop

---

## 🚀 Live Demo (배포)

- **Manager 화면**
  - https://modney.shop/manager/booths/1/menus
  - 주문 승인/거절, 메뉴 품절 토글, 매출·랭킹 확인

- **Customer 화면 (Mobile Web)**
  - https://modney.shop/booths/1/tables/1/menu
  - QR 기반 메뉴 조회 → 장바구니 → 주문 생성(PENDING)

> ⚠️ 축제 환경을 가정한 모바일 UX로, **고객 화면은 모바일 접속을 권장**합니다.

---

## 📄 Documentation
- [Bootheat 프로젝트 안내서 (PDF)](./docs/bootheat-guide.pdf)
  > 창업 해커톤 발표를 위해 **기획자가 작성한 서비스 가이드 문서**입니다.  
  > 서비스 컨셉, 기능 정의, 화면 흐름을 이해하기 위한 참고 자료이며,  
  > **백엔드 설계·구현·배포는 본인이 담당**했습니다.

---

## Real-world Usage (실제 운영)

- **운영 기간**: 2025년 9월, 3일간
- **운영 대상**:
  - SSCC
  - 로타렉트
- **운영 방식**:
  - 동아리 행사 부스에서 테이블 QR 기반 주문
  - 매니저가 실시간으로 입금 내역 확인 후 승인
- **의의**:
  - 단순 데모가 아닌 **실제 사용자 대상 서비스**
  - 운영 중 발생 가능한 흐름(대기 주문, 승인 지연, 품절 등)을 고려한 백엔드 설계

---

## Why / How

### Why
- 행사 현장에서는 현금·계좌이체 기반 결제로 인해  
  **대기 시간이 길고 주문/결제 오류가 빈번**

### How
- 테이블별 QR 코드(URL)로 메뉴 접근
- 주문 시 결제 정보(입금자명/금액)를 함께 수집
- 서버에서 주문 상태를 기준으로 단일 흐름 관리
- 매니저 승인 기반으로 실제 운영 환경 반영
- 승인된 주문 기준으로 **실시간 매출·랭킹 집계**

---

## Flow

### Customer
1. QR 코드 스캔
2. 메뉴 조회
3. 장바구니
4. 입금자명·금액 입력
5. 할인코드 적용(선택)
6. 주문 생성 (**PENDING**)

### Manager
1. 로그인
2. 대기 주문(PENDING) 확인
3. 입금 내역 대조
4. 승인(**APPROVED**) / 거절(**REJECTED**)
5. 전체 제공 완료 처리(**FINISHED**)

---

## Core Logic

- **주문 상태(State) 기반 처리**
  - `PENDING → APPROVED → FINISHED`
  - `PENDING → REJECTED`
- **할인코드 로직**
  - 코드 유효성 검증
  - 할인 금액/비율 적용 후 최종 결제 금액 계산
- **메뉴 품절 처리**
  - `menu_item.available` 기반 주문 차단
- **통계/랭킹**
  - 승인된 주문(`APPROVED`) 기준 일자별 `GROUP BY` 집계
- (검토) 중복 주문 방지: `X-Idempotency-Key`

---

## API Overview (요약)

### Public API
- `GET /api/booths/{boothId}/tables/{tableNo}`
- `POST /api/orders`
- `GET /api/orders/{orderId}` *(선택)*

### Manager API
- `POST /api/auth/login`
- `GET /api/manager/orders?status=PENDING&boothId=...`
- `POST /api/manager/orders/{orderId}/approve`
- `POST /api/manager/orders/{orderId}/reject`
- `POST /api/manager/menus/{menuItemId}/toggle-available`
- `GET /api/manager/booths/{boothId}/stats/date/{yyyy-MM-dd}`

---

## Deployment & Ops (AWS Lightsail)

- AWS Lightsail 인스턴스 생성 및 배포
- 도메인 연결 (modney.shop)
- 서비스 운영 중:
  - 서버 재시작 대응
  - API 정상 동작 확인
  - 실제 트래픽 환경 검증

---

## Retrospective

프로토타입임에도 실제 사용자와 운영 환경을 경험하며  
**상태 관리, 데이터 무결성, 운영 안정성**의 중요성을 체감했습니다.  
특히 배포 이후의 운영과 대응까지 경험하며  
백엔드 개발자가 **구현뿐 아니라 서비스 운영의 책임을 진다는 점**을 배웠습니다.
