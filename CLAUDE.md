# CLAUDE.md

이 파일은 이 저장소에서 코드 작업을 할 때 Claude Code (claude.ai/code)에 대한 가이드를 제공합니다.

## 프로젝트 개요

cozo.me의 Spring Boot 3.5.3 REST API 백엔드입니다. 이 애플리케이션은 다양한 한국 커뮤니티 웹사이트에서 콘텐츠를 크롤링하고, MySQL에 저장하며, 전문 검색을 위해 Elasticsearch를 사용하고, 프론트엔드를 위한 REST 엔드포인트를 제공합니다.

## 필수 명령어

### 개발 환경 설정

```bash
# 필요한 서비스 시작 (MySQL, Redis, Elasticsearch, Cerebro)
cd docker
docker compose up -d

# 프로젝트 빌드
./gradlew build

# 애플리케이션 실행
./gradlew bootRun

# Docker 이미지 빌드
./gradlew bootBuildImage
```

### 필수 환경 변수

프로덕션 배포 시 다음 환경 변수를 설정하세요:

- `SPRING_PROFILES_ACTIVE=production`
- `SPRING_DATASOURCE_URL`
- `SPRING_DATASOURCE_USERNAME`
- `SPRING_DATASOURCE_PASSWORD`
- `REDIS_HOST`
- `REDIS_PORT`
- `REDIS_PASSWORD`
- `ELASTICSEARCH_HOST`
- `YOUTUBE_API_KEY`
- `DISCORD_WEBHOOK`
- `CLOUDFLARE_SITE_KEY`
- `CLOUDFLARE_SECRET_KEY`

## 아키텍처 개요

이 코드베이스는 CQRS(Command Query Responsibility Segregation)를 사용한 클린 아키텍처 패턴을 따릅니다:

### 핵심 레이어

1. **Controller 레이어** (`controller/`): HTTP 요청을 처리하는 REST 엔드포인트
2. **Application 레이어** (`application/`): 커맨드, 핸들러, 비즈니스 로직 오케스트레이션
3. **Domain 레이어** (`domain/`): 엔티티, 리포지토리, DTO, 도메인 이벤트
4. **Infrastructure 레이어** (`infrastructure/`): 외부 서비스 통합 및 구현체

### 주요 아키텍처 패턴

- **CQRS 패턴**: 커맨드는 `CommandGateway`를 통해 적절한 핸들러로 전달됨
- **이벤트 기반**: 도메인 이벤트가 이벤트 리스너를 통해 부수 효과를 트리거
- **리포지토리 패턴**: Spring Data JPA 리포지토리를 통한 데이터 접근 추상화
- **스케줄 작업**: 콘텐츠 크롤링 및 유지보수를 위한 백그라운드 작업

### 데이터 흐름

1. REST API가 요청 수신 → Controller
2. Controller가 Command 생성 → CommandGateway
3. CommandHandler가 커맨드 처리 → 도메인 엔티티 업데이트
4. 도메인 이벤트 발행 → EventListener가 부수 효과 처리
5. 데이터는 MySQL에 저장, Elasticsearch에 인덱싱, Redis에 캐싱

### 크롤러 시스템

애플리케이션은 여러 사이트별 크롤러(`infrastructure/crawler/`)를 포함합니다:

- 공통 기능을 위한 베이스 크롤러 클래스 확장
- JSoup을 사용한 HTML 파싱
- 기사 콘텐츠, 메타데이터, 이미지 추출
- 사이트별 파싱 로직 처리

각 크롤러는 `CrawlScheduler`를 통해 주기적으로 실행됩니다.

## 주요 기술 및 패턴

- **Spring Integration**: 커맨드/이벤트 메시징 패턴
- **Spring WebFlux**: 논블로킹 I/O를 위한 리액티브 프로그래밍
- **Spring AI MCP Server**: AI 도구 통합을 위한 Model Context Protocol 서버
- **Lombok**: 보일러플레이트 코드 감소 (getter, setter, builder)
- **JSoup**: 웹 크롤링을 위한 HTML 파싱
- **OpenAPI/Swagger**: API 문서 자동 생성

### MCP (Model Context Protocol) 서버

애플리케이션은 Spring AI를 통해 MCP 서버 기능을 제공합니다:

- **설정**: `McpConfig`가 MCP 도구를 Spring Bean으로 등록
- **도구 구현**: `McpTools`가 AI 모델이 사용할 수 있는 도구 제공
  - `getTrendingKeywords`: 특정 날짜의 인기 키워드와 관련 게시글 조회
  - `getRecentLinks`: 커뮤니티에서 최근 공유된 링크 조회
  - `searchArticles`: 키워드로 커뮤니티 게시글 검색

MCP 도구는 `@Tool` 어노테이션으로 정의되며, AI 모델이 직접 호출할 수 있는 메서드들을 제공합니다.

## 테스트 접근법

현재 메인 애플리케이션에는 테스트 커버리지가 부족합니다. `webs` 서브프로젝트에는 HTTP 클라이언트 테스트가 포함되어 있습니다. 새로운 기능 추가 시:

- 커맨드 핸들러를 위한 통합 테스트 작성
- 외부 서비스(Discord, YouTube, 크롤링 대상 웹사이트) 모킹
- @DataJpaTest로 리포지토리 메서드 테스트
- 테스트 컨테이너로 Elasticsearch 쿼리 검증

## 데이터베이스 스키마

주요 엔티티:

- `Site`: 크롤링 대상 웹사이트 설정
- `Board`: 사이트 내 카테고리
- `Article`: 전문 검색 필드를 포함한 크롤링된 콘텐츠
- `Link`: 기사와 연관된 URL
- `Tag`: 콘텐츠 분류
- `TagTrend`: 태그 인기도 추적

관계는 성능을 위한 적절한 인덱스와 함께 표준 JPA 패턴을 따릅니다.
