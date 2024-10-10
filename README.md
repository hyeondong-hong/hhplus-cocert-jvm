## 선택 시나리오: 콘서트 예약 서비스

### 주요 문서
* [프로젝트 마일스톤](./docs/milestone.md)
* [Sequence Diagram](./docs/sequence-diagram.md)
* [Flowchart](./docs/flowchart.md)
* [ERD](./docs/erd.md)

### 패키지 구조 - 클린아키텍처
```
io/hhplus/concert
├── controller
├── domain
├── infra
├── port
├── service
└── usecase
```

#### 패키지 구조적 특징
1. 계층은 컨트롤러(Controller), 유스케이스(UseCase), 포트(Port), 도메인(Domain) 으로만 구분
2. 비즈니스 요구사항을 하나의 독립적인 기능으로 정의하기 위해 각 유스케이스 클래스로 분리
3. 서로 다른 도메인과의 상호작용은 포트를 통해 처리

#### 클린아키텍처의 유스케이스와 파사드패턴의 차이
1. 파사드
   - 여러 서비스를 조합해서 복잡한 비즈니스 요구사항을 처리하기 위해 사용
   - 무신사 같은 서드파티(카카오, 네이버, 택배 서비스 등등) 연결 포인트가 많은 서비스에 유리함
2. 유스케이스
   - 하나의 독립적인 비즈니스 요구사항을 단일 기능으로 처리하기 위해 사용
   - 토스 같은 자사 내에서 완결되는 경우가 많은 서비스에서는 코드 복잡성을 최소화 할 수 있다는 장점

### 기술 스택
* Framework - [Spring Boot](https://spring.io/projects/spring-boot)
* API Documentation - [Spring REST Docs](https://spring.io/projects/spring-restdocs)
* Database
   - [MySQL 8.4](https://dev.mysql.com/doc/refman/8.4/en/)
   - [Redis](https://redis.io/docs/latest/)
* Container - [Docker](https://docs.docker.com/)
* Message Broker
   - [Kafka](https://kafka.apache.org/documentation/)
   - [RabbitMQ](https://www.rabbitmq.com/docs)

### API 명세
* Spring REST Docs 명세 파일 - [index.adoc](./docs/asciidoc/index.adoc)

![API Documentation](./docs/images/api-documentation.png)
