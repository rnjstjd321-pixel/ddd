# Order Service - Ports & Adapters (in / out)

```text
adapter.in  ──▶  application.port.in  ──▶  application.service  ──▶  application.port.out  ◀──  adapter.out
(web, messaging)   (OrderUseCase)          (OrderApplicationService)   (Repository/Event/Catalog)  (JPA, Kafka, REST)
                                                     │
                                                   domain
```

- Inbound ports (`application.port.in`)
  - `OrderUseCase` - `adapter.in.web.OrderWebAdapter`, `adapter.in.messaging.OrderKafkaListener`가 호출한다.
- Outbound ports (`application.port.out`)
  - `OrderRepositoryPort` - `adapter.out.persistence.OrderPersistenceAdapter` (JPA)
  - `OrderEventPublisherPort` - `adapter.out.messaging.KafkaOrderEventPublisher` (Kafka, 커밋 이후 발행)
  - `ProductCatalogPort` - `adapter.out.client.ProductCatalogAdapter` (REST)

`domain`과 `application`은 어댑터(Spring MVC/JPA/Kafka/REST)에 의존하지 않는다.
