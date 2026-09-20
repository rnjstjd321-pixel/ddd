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

## 2. DDD 계층

각 서비스는 동일한 4계층을 가집니다.

```text
presentation     Controller, Request DTO, Exception Handler
application      Application Service, Command, Port
domain           Aggregate, Entity, Value Object, Domain Exception, Repository 인터페이스
infrastructure   JPA Adapter, REST Adapter
```

도메인 규칙은 Aggregate 안에 두고, 애플리케이션 서비스는 유스케이스를 조율만 합니다.
다른 Bounded Context는 도메인 객체가 아니라 Port로만 의존하고, infrastructure Adapter가 그 Port를 구현합니다.

예시 (Order Service):

```text
com.example.order
├── presentation
│   └── OrderController
├── application
│   ├── service.OrderApplicationService
│   └── port.ProductCatalogPort
├── domain
│   ├── model.Order (Aggregate Root)
│   ├── model.OrderLine / Money / ProductSnapshot (VO)
│   ├── event.OrderPlaced / OrderCancelled
│   ├── service.PlaceOrderService
│   └── repository.OrderRepository
└── infrastructure
    ├── persistence.OrderRepositoryAdapter
    ├── client.ProductCatalogAdapter
    └── event.KafkaDomainEventPublisher / messaging listeners
```

### 핵심 도메인 모델

- `Order`: 주문 Aggregate Root. `create()` + `addLine()` / `markPaid()` / `cancel()`로 일관성을 유지합니다.
- `OrderLine`: Aggregate 내부 라인. 루트를 통해서만 생성됩니다.
- `Money`: 금액 VO. 음수/통화 규칙을 생성 시점에 강제합니다.
- `ProductSnapshot`: 상품 스냅샷 VO. 다른 컨텍스트의 상품을 주문 시점에만 보관합니다.
- `PlaceOrderService`: 도메인 서비스. Aggregate를 조립하고 `OrderPlaced` 이벤트를 발행합니다.
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

## 4. 주문 생성 흐름

```text
Client
  |
  v
Order Application Service
  |
  | 1. 상품 조회 (동기 REST)
  v
Product Context
  |
  | 2. Order 저장(CREATED) + order.placed 발행
  v
Kafka
  |
  | 3. 재고 차감 → stock.deducted
  v
Product Context
  |
  | 4. 결제 승인 → payment.approved
  v
Payment Context
  |
  | 5. Order.markPaid()
  v
Order -> PAID
```

주문 생성 API는 `CREATED` 상태로 바로 응답하고, 결제 완료는 Kafka를 통해 비동기로 `PAID`로 반영됩니다.
조회(`GET /api/orders/{id}`)로 최종 상태를 확인하면 됩니다.

Kafka 토픽:
- `order.placed` / `order.cancelled`
- `stock.deducted` / `stock.deduct.failed`
- `payment.approved` / `payment.failed`

실제 운영 환경에서는 Outbox Pattern과 멱등 소비를 함께 적용하는 것이 좋습니다.

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

도메인 로직은 Controller가 아니라 Aggregate에 있습니다. 다른 서비스 호출은 infrastructure Adapter가 Port를 구현하므로, 도메인은 REST/JPA를 알지 않습니다.

## 8. 추가 발전 방향

운영 수준으로 발전시킨다면 다음을 적용할 수 있습니다.

1. REST 동기 호출 의존성 감소 (상품 조회만 REST, 재고/결제는 Kafka)
2. Outbox Pattern
3. Saga Pattern 및 보상 트랜잭션 고도화
4. Redis 기반 상품/재고 조회 캐시
5. Spring Cloud Gateway
6. Kubernetes Deployment/Service
7. OpenTelemetry + Prometheus/Grafana
8. API timeout/retry/circuit breaker

## 9. 제출 시 설명할 핵심

> "서비스를 기술 기준으로 나눈 것이 아니라 도메인의 책임과 데이터 소유권을 기준으로 Bounded Context를 나눴습니다. Order는 주문의 생명주기를 책임지고 Product는 상품과 재고를, Payment는 결제 생명주기를 책임집니다. 각 서비스는 자신의 데이터를 소유하며 다른 서비스의 DB에 직접 접근하지 않습니다. 비즈니스 규칙은 Aggregate에 두고, 외부 시스템 의존은 Port/Adapter로 격리했습니다."
