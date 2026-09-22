# Order MSA + DDD Assignment

주문 도메인을 기준으로 서비스 경계를 분리하고, 각 서비스를 DDD 계층 구조로 구현한 Spring Boot 3.3.5 / Java 17 멀티모듈 프로젝트입니다.

## 1. Bounded Context

```text
                         ┌──────────────────┐
                         │   Order Context  │
                         │ 주문 생성/조회/취소 │
                         └───────┬──────────┘
                                 │
                    ┌────────────┴────────────┐
                    │                         │
             HTTP/REST                    HTTP/REST
                    │                         │
          ┌─────────▼────────┐      ┌────────▼─────────┐
          │ Product Context  │      │ Payment Context  │
          │ 상품/재고 책임      │      │ 결제 책임          │
          └──────────────────┘      └──────────────────┘
```

각 마이크로서비스는 하나의 Bounded Context입니다.

### Order Context
- 주문 생성
- 주문 조회
- 주문 취소
- 주문 상태 관리
- 주문 항목 관리
- 주문 금액 계산
- 상품/결제 컨텍스트 호출을 통한 필요한 정보 확인

**소유 데이터**
- orders
- order_items

### Product Context
- 상품 조회
- 상품 생성/수정
- 재고 확인
- 재고 차감
- 재고 복구

**소유 데이터**
- products

### Payment Context
- 결제 승인
- 결제 조회
- 결제 취소

**소유 데이터**
- payments

## 2. DDD + 헥사고날(Ports & Adapters) 구조

각 서비스는 동일한 구조를 가집니다. 헥사고날은 **in(들어오는 쪽)과 out(나가는 쪽)** 을 분리하는 것이 핵심이라, 포트와 어댑터 모두 `in` / `out`으로 나눕니다.

```text
domain           Aggregate, Entity, Value Object, Domain Event, Domain Service, Domain Exception
application
  ├─ port.in     Inbound Port  : 유스케이스 인터페이스 (웹/메시징이 호출)
  ├─ port.out    Outbound Port : Repository, 이벤트 발행, 외부 서비스 조회 인터페이스
  ├─ service     유스케이스 구현 (port.in 구현, port.out 사용)
  └─ dto         Command / Response
adapter
  ├─ in.web        REST Controller, Request DTO, Exception Handler   -> port.in 호출
  ├─ in.messaging  Kafka Listener, 수신 메시지                        -> port.in 호출
  ├─ out.persistence  JPA Adapter                                    <- port.out 구현
  ├─ out.messaging    Kafka Publisher, 발행 메시지                    <- port.out 구현
  └─ out.client       REST Client Adapter                            <- port.out 구현
```

의존 방향은 항상 `adapter -> application -> domain` 입니다.
`domain`과 `application`은 `adapter`(JPA/Kafka/REST/Spring MVC)를 알지 못하고, 어댑터가 포트를 구현/호출합니다.
다른 Bounded Context는 도메인 객체가 아니라 Outbound Port로만 의존합니다.

예시 (Order Service):

```text
com.example.order
├── domain
│   ├── model.Order (Aggregate Root) / OrderLine / Money / ProductSnapshot
│   ├── event.OrderPlaced / OrderCancelled
│   ├── service.PlaceOrderService        (순수 도메인 규칙, 저장/발행은 하지 않음)
│   └── exception.*
├── application
│   ├── port.in.OrderUseCase
│   ├── port.out.OrderRepositoryPort / OrderEventPublisherPort / ProductCatalogPort
│   ├── service.OrderApplicationService
│   └── dto.CreateOrderCommand / OrderResponse
├── adapter
│   ├── in.web.OrderWebAdapter (+ dto, exception)
│   ├── in.messaging.OrderKafkaListener (+ Payment*/Stock* 메시지)
│   ├── out.persistence.OrderPersistenceAdapter / OrderJpaRepository
│   ├── out.messaging.KafkaOrderEventPublisher (+ OrderPlaced/Cancelled 메시지)
│   └── out.client.ProductCatalogAdapter
└── config   KafkaTopicConfig / RestClientConfig / DomainConfig
```

Product / Payment Service도 같은 규칙입니다.

| 서비스 | in.web | in.messaging | out.persistence | out.messaging |
|--------|--------|--------------|-----------------|---------------|
| order | OrderWebAdapter | OrderKafkaListener | OrderPersistenceAdapter | KafkaOrderEventPublisher |
| product | ProductWebAdapter | ProductKafkaListener | ProductPersistenceAdapter | KafkaStockEventPublisher |
| payment | PaymentWebAdapter | PaymentKafkaListener | PaymentPersistenceAdapter | KafkaPaymentEventPublisher |

Product/Payment의 메시징 유스케이스(`OrderEventUseCase` / `OrderEventService`)는 트랜잭션 밖에서 처리 결과 이벤트(성공/실패)를 Outbound Port로 발행합니다.

### 핵심 도메인 모델

- `Order`: 주문 Aggregate Root. `create()` + `addLine()` / `markPaid()` / `cancel()`로 일관성을 유지합니다.
- `OrderLine`: Aggregate 내부 라인. 루트를 통해서만 생성됩니다.
- `Money`: 금액 VO. 음수/통화 규칙을 생성 시점에 강제합니다.
- `ProductSnapshot`: 상품 스냅샷 VO. 다른 컨텍스트의 상품을 주문 시점에만 보관합니다.
- `PlaceOrderService`: 도메인 서비스. Aggregate 조립 규칙만 담당하며, 저장과 `OrderPlaced` 발행은 Application Service가 Outbound Port로 수행합니다.
- `Product` + `Stock` + `Money`: 상품 Aggregate Root와 재고/가격 VO.
- `Payment` + `Money`: 결제 Aggregate Root. 승인/취소 상태 전이를 캡슐화합니다.

## 3. Boundary 원칙

각 서비스는 자신의 DB만 직접 접근합니다.

- Order Service -> Product DB 직접 접근 금지
- Order Service -> Payment DB 직접 접근 금지
- Product Service -> Order DB 직접 접근 금지
- Payment Service -> Order DB 직접 접근 금지

서비스 간 데이터가 필요하면 API를 사용합니다.

특히 `Order`가 `Product` 객체 전체를 소유하지 않고 `productId`, `productName`, `unitPrice`처럼 주문에 필요한 값만 주문 시점에 스냅샷으로 보관합니다.

따라서 상품명이 변경되더라도 과거 주문의 상품명/가격이 변하지 않습니다.

## 4. Event-driven + Saga (Choreography)

서비스 간 협력은 Kafka 이벤트로만 이루어지며, **중앙 조율자(Orchestrator) 없이** 각 서비스가 이벤트를 구독해 자기 로컬 트랜잭션을 수행하고 결과 이벤트를 발행합니다.
Kafka는 헥사고날의 in/out 어댑터로 붙습니다: 구독은 `adapter.in.messaging`, 발행은 `adapter.out.messaging`이 담당하고, 서비스 내부는 `port.in` / `port.out`만 압니다.

```text
Order    POST /api/orders  → Order(CREATED) 저장 ─▶ order.placed
Product  order.placed      → 재고 차감 + StockReservation 기록 ─▶ stock.deducted  (실패: stock.deduct.failed)
Payment  stock.deducted    → 결제 승인 ─▶ payment.approved                         (실패: payment.failed)
Order    payment.approved  → Order.markPaid() → PAID
```

주문 생성 API는 `CREATED`로 바로 응답하고, 최종 상태는 이벤트를 통해 수렴합니다(Eventual Consistency).

| 서비스 | 구독(in.messaging) | 발행(out.messaging) |
|--------|-------------------|---------------------|
| order | payment.approved, payment.failed, stock.deduct.failed | order.placed, order.cancelled |
| product | order.placed, order.cancelled | stock.deducted, stock.deduct.failed |
| payment | stock.deducted, order.cancelled | payment.approved, payment.failed |

### 보상(Compensation)

| 상황 | 반응 |
|------|------|
| `stock.deduct.failed` | Order가 주문을 `CANCELLED`로 변경 (되돌릴 재고 없음) |
| `payment.failed` | Order가 주문을 `CANCELLED`로 변경하고 `order.cancelled` 발행 → Product가 재고 복구 |
| 사용자 주문 취소 | Order가 `order.cancelled` 발행 → Product 재고 복구, 결제됐다면(`wasPaid`) Payment가 결제 취소 |
| 취소된 주문에 `payment.approved`가 뒤늦게 도착 | Order가 `order.cancelled(wasPaid=true)`를 다시 발행 → Payment가 결제 취소 |

### 멱등성 / 순서 뒤바뀜 대응

토픽이 달라 `order.placed`와 `order.cancelled`의 도착 순서는 보장되지 않고, Kafka는 중복 전달이 가능합니다.

- Product: `stock_reservations`(PK = orderId)에 주문 단위 차감 기록을 남깁니다. 같은 주문은 재고를 한 번만 차감/복구하고, 차감 전에 취소가 먼저 오면 `RELEASED` 표식만 남겨 이후 차감을 막습니다.
- Payment: `orderId` 기준으로 이미 결제가 있으면 새로 승인하지 않고, 이미 취소된 결제의 재취소는 무시합니다.
- Order: 상태 기반으로 처리하므로 이미 `PAID`/`CANCELLED`인 주문에 온 중복 이벤트는 무시합니다.
- 재고 변경은 `SELECT ... FOR UPDATE`(비관적 락)로 직렬화해 동시 차감 시 갱신 유실을 막습니다.
- 모든 이벤트에는 `eventId`(UUID)가 있고 수신 로그에 함께 남습니다.

### Kafka 토픽

- `order.placed` / `order.cancelled`
- `stock.deducted` / `stock.deduct.failed`
- `payment.approved` / `payment.failed`

### 알려진 한계

- DB 저장과 Kafka 발행이 하나의 트랜잭션이 아닙니다(커밋 후 발행). 운영에서는 Transactional Outbox와 재시도/DLQ를 함께 적용해야 합니다.
- 상품 조회(`GET /api/products/{id}`)는 동기 REST입니다.

## 5. 실행

각 서비스는 자신의 PostgreSQL만 사용합니다.

| 서비스 | DB 컨테이너 | 로컬 포트 | Database |
|--------|-------------|-----------|----------|
| Order Service | order-db | 5432 | orderdb |
| Product Service | product-db | 5433 | productdb |
| Payment Service | payment-db | 5434 | paymentdb |

공통 계정: `postgres` / `postgres`

### 1) PostgreSQL + Kafka 실행 (로컬 bootRun용)

```bash
docker compose up -d order-db product-db payment-db kafka
```

Kafka는 `localhost:9092`로 연결됩니다.

그다음 서비스를 독립 실행합니다.

```bash
./gradlew :product-service:bootRun
./gradlew :payment-service:bootRun
./gradlew :order-service:bootRun
```

Windows CMD에서는:

```cmd
gradlew.bat :product-service:bootRun
gradlew.bat :payment-service:bootRun
gradlew.bat :order-service:bootRun
```

기본 포트:
- Product Service: 8081
- Payment Service: 8082
- Order Service: 8080

### 2) 전체 Docker 실행

```bash
docker compose up --build
```

앱 컨테이너는 서비스별 PostgreSQL이 healthy 상태가 된 뒤에 기동합니다.

도메인 단위 테스트:

```bash
./gradlew test
```

## 6. 테스트 예시

상품 생성:

```http
POST http://localhost:8081/api/products
Content-Type: application/json

{
  "name": "MacBook Pro",
  "price": 2500000,
  "stock": 10
}
```

주문 생성:

```http
POST http://localhost:8080/api/orders
Content-Type: application/json

{
  "productId": 1,
  "quantity": 2
}
```

주문 조회:

```http
GET http://localhost:8080/api/orders/1
```

주문 취소:

```http
POST http://localhost:8080/api/orders/1/cancel
```

## 7. 왜 이 경계인가?

`Order`, `Product`, `Payment`는 변경 이유와 데이터 소유권이 서로 다릅니다.

- 상품 가격/재고 정책 변경 -> Product
- 주문 상태/주문 정책 변경 -> Order
- 결제 승인/취소/결제수단 변경 -> Payment

따라서 하나의 거대한 주문 서비스 안에 모두 넣기보다 독립적인 Bounded Context로 분리하는 것이 적절합니다.

도메인 로직은 Controller가 아니라 Aggregate에 있습니다. 다른 서비스 호출은 adapter.out이 Outbound Port를 구현하므로, 도메인은 REST/JPA를 알지 않습니다.

## 8. 추가 발전 방향

운영 수준으로 발전시킨다면 다음을 적용할 수 있습니다.

1. REST 동기 호출 의존성 감소 (상품 조회만 REST, 재고/결제는 Kafka)
2. Transactional Outbox, 재시도/DLQ 적용으로 이벤트 유실 방지

## 9. 제출 시 설명할 핵심

> "서비스를 기술 기준으로 나눈 것이 아니라 도메인의 책임과 데이터 소유권을 기준으로 Bounded Context를 나눴습니다. Order는 주문의 생명주기를 책임지고 Product는 상품과 재고를, Payment는 결제 생명주기를 책임집니다. 각 서비스는 자신의 데이터를 소유하며 다른 서비스의 DB에 직접 접근하지 않습니다. 비즈니스 규칙은 Aggregate에 두고, 외부 시스템 의존은 in/out Port와 Adapter로 격리했습니다."
