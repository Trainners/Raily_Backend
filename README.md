# Raily Backend

> 기차 정기권·입석 승객을 위한 **구간별 빈자리 조회 서비스**의 API 서버

> 배포 URL : [Raily](https://raily-frontend.vercel.app)

> 시연영상 링크 : [영상](영상링크)

> 발표자료 링크 : [발표자료](발표자료)

코레일은 **출발역부터 도착역까지 통으로 비어 있는 좌석만** 알려줍니다. 그래서 "천안에서 수원까지는 비어 있고 수원부터 팔린 자리"를 찾으려면 검색 구간을 바꿔 가며 여러 번 조회해야 합니다.

Raily 백엔드는 이 반복 조회를 서버에서 대신 수행합니다.

1. 여정을 **정차역 단위 구간으로 쪼개** 코레일에 구간별로 조회합니다.
2. 결과를 **좌석 × 구간 매트릭스**로 합칩니다.
3. 오래 앉을 수 있는 순서로 **정렬해서** 내려줍니다.
4. 사용자가 앉은 자리는 **정차역마다 감시**하다가, 다음 구간에서 팔리면 **웹 푸시**로 알립니다.

팀 Trainners

---

## 기술 스택

| 영역 | 선택 | 버전 |
|---|---|---|
| 언어 | Java | 21 |
| 프레임워크 | Spring Boot (Web MVC, Data JPA, Security, Validation, Actuator) | 4.1.1 |
| DB | PostgreSQL | 15 |
| 인증 | JWT (jjwt) | 0.12.6 |
| 외부 API 호출 | Spring `RestClient` | — |
| 웹 푸시 | web-push (VAPID) + BouncyCastle | 5.1.1 |
| API 문서 | springdoc-openapi (Swagger UI) | 3.1.1 |
| 테스트 | JUnit 5, AssertJ | — |
| 배포 | Docker, Docker Compose, GitHub Actions (self-hosted runner) | — |

---

## 실행

### 사전 준비

- JDK 21
- PostgreSQL 15 (로컬에 `railydb` 데이터베이스 생성)
- `.env.example`을 참고해 환경 변수 설정

```bash
./gradlew bootRun   # http://localhost:8080
```

| 명령 | 설명 |
|---|---|
| `./gradlew bootRun` | 로컬 서버 실행 |
| `./gradlew test` | 단위 테스트 |
| `./gradlew bootJar` | 실행 가능한 jar 생성 (`build/libs`) |

로컬에서는 Swagger UI(`http://localhost:8080/swagger-ui/index.html`)로 전체 API를 확인할 수 있습니다. 운영 환경에서는 Swagger와 `/v3/api-docs`를 비활성화합니다.

### 환경 변수

| 변수 | 설명 |
|---|---|
| `DB_NAME` / `DB_USERNAME` / `DB_PASSWORD` | PostgreSQL 접속 정보 |
| `JWT_SECRET` | JWT 서명 키 |
| `JWT_ACCESS_TOKEN_VALIDITY` / `JWT_REFRESH_TOKEN_VALIDITY` | 토큰 유효 시간(ms). 기본 30분 / 14일 |
| `TRAINRUNPLAN_SERVICE_KEY` | 공공데이터포털 열차 운행 정보 API 키 |
| `WEBPUSH_VAPID_PUBLIC_KEY` / `WEBPUSH_VAPID_PRIVATE_KEY` / `WEBPUSH_VAPID_SUBJECT` | 웹 푸시 VAPID 키 |
| `CORS_ALLOWED_ORIGINS` | 허용할 프론트 출처 (기본 `https://raily-frontend.vercel.app`) |

---

## 아키텍처 — 도메인형 패키지

기능이 아니라 **도메인 단위**로 패키지를 나눴습니다. 각 도메인 안에서는 `controller → service → repository` 계층을 따르고, 외부 API 호출은 `client`로 분리합니다.

```
io.trainners.raily_backend
├── domain
│   ├── auth           로그인, 재발급, 로그아웃, JWT 필터
│   ├── user           회원가입, 회원 탈퇴
│   ├── train          열차 목록, 구간별 좌석 조회 API (조합 계층)
│   ├── seat           좌석 추천 — 구간 가용성 조립 + 우선순위 정렬
│   ├── korail         코레일 API 클라이언트와 응답 DTO
│   ├── trainRunPlan   공공데이터포털 열차 운행 정보 클라이언트
│   └── notification   좌석 감시, 스케줄러, 알림함, 웹 푸시
└── global
    ├── config         Security, CORS, Swagger, WebPush, Scheduling
    └── exception      ErrorCode, BusinessException, 전역 예외 처리
```

`train`은 외부 API 두 곳(`korail`, `trainRunPlan`)과 추천 로직(`seat`)을 엮는 조합 계층입니다. `seat`는 네트워크를 모르는 순수 로직만 담고 있어서, 코레일 응답 구조가 바뀌어도 추천 규칙은 영향을 받지 않습니다.

---

## 인증 — JWT + HttpOnly 쿠키

### 토큰 전달 방식

| 토큰 | 전달 방식 | 유효 시간 | 이유 |
|---|---|---|---|
| accessToken | 응답 본문 → 요청 시 `Authorization: Bearer` | 30분 | 프론트가 메모리에만 보관합니다 |
| refreshToken | **`Set-Cookie`** (`HttpOnly`, `Secure`, `SameSite=Strict`, `Path=/api/auth`) | 14일 | JS가 읽을 수 없고, `/api/auth` 요청에만 실립니다 |

- **`Path=/api/auth`**: 일반 API 요청에는 refresh 쿠키가 전송되지 않습니다. 노출 범위를 재발급·로그아웃 경로로 제한합니다.
- **`SameSite=Strict`**: 교차 사이트 요청에는 쿠키가 실리지 않습니다. 그래서 프론트는 Vercel rewrite를 거쳐 같은 출처로 호출합니다. 이 덕분에 CORS 프리플라이트도 생기지 않습니다.

### 토큰 검증 흐름

```
요청 → JwtAuthenticationFilter
        ├─ Authorization 헤더 없음        → 인증 없이 통과 (공개 API면 성공, 아니면 401)
        ├─ 서명·만료 검증 실패            → SecurityContext 비움 → 401
        ├─ type 클레임이 "access"가 아님   → 인증하지 않음 → 401
        └─ 정상                           → SecurityContext에 사용자 등록
```

- **`type` 클레임 분리**: access와 refresh는 같은 키로 서명합니다. 그래서 `type` 클레임으로 둘을 구분해, refresh 토큰을 access 토큰처럼 쓰지 못하게 막습니다.
- **서버 측 refresh 토큰 저장**: 발급한 refresh 토큰을 `users` 테이블에 저장하고, 재발급 시 **DB 값과 일치하는지** 확인합니다. 로그아웃하면 DB 값을 지우므로, 탈취된 refresh 토큰도 로그아웃 이후에는 쓸 수 없습니다.
- **401 응답 통일**: 인증 실패는 `CustomAuthenticationEntryPoint`가 JSON(`{"message":"Unauthorized"}`)으로 응답합니다. 로그인 실패도 403이 아니라 401을 반환해, 프론트의 재발급 로직이 상태 코드 하나만 보고 판단할 수 있습니다.
- **Stateless**: 세션을 만들지 않습니다(`SessionCreationPolicy.STATELESS`). 폼 로그인과 HTTP Basic은 끄고, CSRF 보호도 끕니다. 쿠키 인증은 `/api/auth` 경로에만 쓰이고 `SameSite=Strict`가 적용되기 때문입니다.

### 재발급 응답에 사용자 정보 포함

`POST /api/auth/reissue`는 새 accessToken과 함께 `email`·`name`을 반환합니다. 프론트는 앱 시작 시 재발급 한 번으로 세션 복원과 사용자 정보 조회를 함께 끝냅니다.

---

## 외부 API 연동

두 곳에서 데이터를 가져옵니다.

| 출처 | 용도 | 호출 시점 |
|---|---|---|
| **코레일** | 열차 목록, 호차별 잔여석, 좌석별 판매 여부 | 좌석 조회 시, 감시 윈도우가 열렸을 때 |
| **공공데이터포털** 열차 운행 정보 | 열차의 정차역 목록과 역별 도착·출발 시각 | 좌석 조회 시, 좌석 감시 등록 시 **1회** |

### 코레일 연동에서 처리한 문제

- **Content-Type 불일치**: 코레일은 본문이 JSON이어도 `text/html`로 응답합니다. 그래서 `text/html`과 `text/plain`도 JSON으로 파싱하는 Jackson 컨버터를 맨 앞에 등록했습니다.
- **세션 격리**: 코레일은 쿠키로 세션을 유지합니다. 요청마다 `KorailClient`를 새로 만들어, 사용자끼리 세션이 섞이지 않게 했습니다.
- **열차 번호 비교**: 응답에 따라 `"01122"`와 `"1122"`처럼 형식이 다르게 옵니다. 그래서 문자열이 아니라 **숫자로 변환해 비교**합니다.

### 공공데이터 연동에서 처리한 문제

- **미래 날짜 조회 불가**: 이 API는 어제까지의 운행 실적만 제공합니다. 그래서 조회 날짜가 어제 이후라면 **7일씩 거슬러 올라가 같은 요일**의 기록을 사용합니다(`lastWeekRunDate`). 정기 열차는 요일별 시간표가 같다는 점을 이용한 방식입니다.
- **열차 번호 형식**: 5자리로 0을 채워 요청합니다(`1122` → `01122`).
- **같은 역을 두 번 지나는 노선**: 출발역은 처음 등장한 위치, 도착역은 마지막으로 등장한 위치를 사용합니다.

---

## 구간별 좌석 조회

### 데이터 흐름

```
GET /api/trains/seats?departureStation=천안&arrivalStation=영등포&date=...&time=...&trainNum=...

1. 공공데이터 → 천안~영등포 사이 정차역   [천안, 평택, 수원, 영등포]
2. 구간마다 반복 (천안-평택, 평택-수원, 수원-영등포)
     ├─ ScheduleView     이 구간을 달리는 열차 목록에서 같은 열차 번호 찾기
     ├─ TrainResearch    호차별 잔여석 수
     └─ ResidualSeats    잔여석이 1 이상인 호차만 좌석별 판매 여부 조회
3. SeatRecommendationService → 좌석 × 구간 매트릭스로 조립 + 정렬
```

- **같은 열차 추적**: 다음 구간을 조회할 때 기준 시각을 **이전 구간의 도착 시각**으로 바꿉니다. 이렇게 해야 같은 열차가 검색 결과에 계속 나옵니다.
- **호출 수 절약**: 잔여석이 0인 호차는 좌석 목록을 조회하지 않습니다. 이 호차의 좌석은 해당 구간에서 `false`(판매됨)로 채워집니다.
- **불필요한 좌석 제외**: 전 구간이 판매된 좌석은 응답에서 뺍니다.

### 응답 형식

```json
{
  "stops": ["천안", "평택", "수원", "영등포"],
  "seats": [
    { "carNumber": "4", "seatNumber": "1A", "availabilityBySegment": [true, true, true] },
    { "carNumber": "3", "seatNumber": "7A", "availabilityBySegment": [true, true, false] },
    { "carNumber": "3", "seatNumber": "7B", "availabilityBySegment": [false, true, true] }
  ]
}
```

`availabilityBySegment`의 길이는 `stops.length - 1`입니다. `i`번째 원소가 `stops[i] → stops[i+1]` 구간의 상태를 뜻합니다.

### 추천 우선순위

좌석 목록은 **서버에서 정렬해서** 내려줍니다. 프론트는 다시 정렬하지 않고 첫 번째 좌석을 추천 좌석으로 사용합니다. 비교는 위 기준부터 차례로 적용합니다.

| 순서 | 기준 | 이유 |
|---|---|---|
| 1 | **지금부터** 연속으로 앉을 수 있는 구간 수 | 당장 앉을 수 있어야 의미가 있습니다. 0이면 자동으로 뒤로 밀립니다 |
| 2 | 여정 전체에서 가장 긴 연속 구간 수 | 자리를 옮기는 횟수를 줄입니다 |
| 3 | 앉을 수 있는 구간의 총 개수 | 총 착석 시간 |
| 4 | 호차 → 좌석 번호 오름차순 | 동점일 때 결과가 매번 바뀌지 않게 합니다 |

위 예시라면 `4호차 1A`(3구간 연속) → `3호차 7A`(2구간 연속) → `3호차 7B`(지금은 못 앉음) 순서가 됩니다.

---

## 좌석 판매 알림 (웹 푸시)

앉은 자리가 이후 구간에서 팔리면, 새 승객이 타기 전에 알려주는 기능입니다.

```
[등록] POST /api/notifications/seat-watch
     → 공공데이터에서 정차역별 도착·출발 시각을 1회 조회해 함께 저장
     → 같은 사용자의 기존 ACTIVE 감시는 자동으로 CANCELED 처리

[감시] SeatWatchScheduler (1분 간격)
     → 여정이 끝났으면                       EXPIRED
     → 감시 윈도우가 열린 정차역이 없으면     종료 (DB 조회만 하고 코레일은 호출하지 않음)
     → 윈도우가 열렸으면                     그 역에서 출발하는 구간의 내 좌석을 코레일에 확인

[감지] 판매 확인
     → 상태를 NOTIFIED로 변경 + 알림 저장 + 이벤트 발행 (한 트랜잭션)
     → 커밋 후 리스너가 사용자의 모든 기기로 웹 푸시 발송

[폴백] GET /api/notifications/seat-watch/{id}
     → 푸시를 못 받는 환경(권한 거부, iOS 미설치)을 위한 폴링용 상태 조회
```

### 감시 윈도우 — 코레일 호출을 최소화

좌석은 **다음 정차역에서 새 승객이 탈 때**만 의미 있게 팔립니다. 그래서 1분마다 모든 감시 건을 코레일에 조회하지 않고, **중간 정차역마다 "도착 10분 전 ~ 출발 시각"** 동안만 확인합니다.

```
          평택 도착 07:15   평택 출발 07:16
               │               │
   07:05 ──────┼───────────────┤          ← 이 12분 동안만 코레일 호출
               │               │
```

- 출발역과 종착역은 윈도우에서 제외합니다. 출발역에서는 이미 앉아 있고, 종착역에서는 내리기 때문입니다.
- **자정을 넘는 열차**: 출발 시각보다 이른 시각이면 다음 날로 봅니다(+1일).
- 정차역 시각은 **등록할 때 한 번만** 받아 `seat_watch_stops` 테이블에 저장합니다(`@ElementCollection`). 스케줄러는 매 실행마다 외부 API를 호출하지 않고 DB만 조회합니다.

### 스케줄러 설계

- **`fixedRate`가 아니라 `fixedDelay`를 씁니다**: 직전 실행이 **끝난 뒤**부터 60초를 셉니다. 코레일 응답이 느려져도 실행이 겹쳐 쌓이지 않습니다.
- **스케줄러 메서드에는 `@Transactional`을 붙이지 않습니다**: 감시 건마다 서비스가 트랜잭션을 따로 엽니다. 100건 중 한 건이 실패해도 나머지 처리 결과가 롤백되지 않습니다.
- **건별 예외 격리**: 한 건에서 예외가 나도 로그만 남기고 다음 건을 계속 처리합니다.

### 판정 원칙 — 확실할 때만 "판매됨"

`SeatAvailabilityChecker`는 **오탐보다 미탐을 택합니다**. 잘못된 알림을 받고 자리를 비우는 것이, 한 틱(1분) 늦게 알림을 받는 것보다 사용자에게 더 나쁘기 때문입니다.

| 상황 | 판정 |
|---|---|
| 코레일 오류, 응답 파싱 실패 | 판매 아님 (다음 틱에 다시 확인) |
| 열차가 목록에서 사라짐 | 판매 아님 (매진인지 시간표 변경인지 알 수 없음) |
| 열차는 있지만 호차 정보가 비어 있음 | **판매됨** (전 호차 매진) |
| 내 호차의 잔여석이 0 | **판매됨** (좌석 목록 조회를 건너뛰어 호출 1회 절약) |
| 내 좌석의 판매 가능 여부가 `N`이거나 목록에 없음 | **판매됨** |
| 잔여석 수 파싱 실패 | 판매 아님 (-1로 처리해 0으로 오인하지 않음) |

### 감시 상태

| 상태 | 의미 | 전이 조건 |
|---|---|---|
| `ACTIVE` | 감시 중. 스케줄러 대상 | 등록 시 |
| `NOTIFIED` | 판매 감지, 알림 발송 완료 | 판매 감지 (중복 알림 방지) |
| `EXPIRED` | 여정 종료 | 도착 시각이 지남 |
| `CANCELED` | 사용자가 자리를 비우거나 다른 자리로 옮김 | 취소 요청, 또는 새 감시 등록 |

취소할 때는 행을 **삭제하지 않고 상태만 바꿉니다**. 이미 발송된 알림이 존재하지 않는 감시 건을 가리키는 일을 막고, 착석 이력을 남기기 위해서입니다.

### 푸시 발송 — 커밋 이후에만

```java
@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
@Transactional(propagation = Propagation.REQUIRES_NEW)
public void onNotificationCreated(NotificationCreatedEvent event) { ... }
```

- **`AFTER_COMMIT`**: 푸시는 한 번 나가면 되돌릴 수 없습니다. 트랜잭션이 롤백됐는데 푸시가 이미 나갔다면 "알림은 받았는데 알림함에는 없는" 상태가 됩니다. 그래서 DB 저장이 확정된 뒤에만 보냅니다.
- **`REQUIRES_NEW`**: 원래 트랜잭션은 이미 커밋돼 끝났으므로, 알림을 조회하려면 새 트랜잭션이 필요합니다.

### 구독 관리

- **기기 1대 = 구독 1행**이고, 사용자와 구독은 1:N 관계입니다. 한 기기로 발송하다 실패해도 다른 기기 발송은 계속합니다.
- `endpoint`에 unique 제약이 있습니다. 같은 기기가 다시 구독하면 새 행을 만들지 않고 **소유자와 키를 갱신**합니다. 한 기기를 여러 계정이 번갈아 쓰는 경우, 이전 계정 앞으로 알림이 가지 않게 하기 위해서입니다.
- 푸시 서버가 **404/410**을 반환하면 브라우저가 폐기한 구독이므로 자동으로 삭제합니다.
- `POST /api/push/test`: 실제 판매를 기다리지 않고도 VAPID 서명, payload 암호화, 브라우저 수신까지 한 번에 확인할 수 있는 테스트 발송입니다.

### 알림함

알림의 원본은 **서버 DB**이고, 푸시는 빠르게 알려주는 수단일 뿐입니다. 푸시를 놓친 사용자는 알림함에서 지난 알림을 확인합니다.

- 최근 50건만 최신순으로 반환합니다. 감시 1건당 알림이 최대 1개라 양이 많지 않아서 페이징은 두지 않았습니다.
- 읽음 처리는 멱등입니다. 두 번 호출해도 `readAt`이 덮어써지지 않습니다.
- 알림은 감시 건을 FK가 아니라 **id 값으로만** 참조합니다. 감시 건의 상태가 바뀌거나 사라져도 알림은 그대로 남습니다.

---

## 예외 처리와 응답 규칙

### 에러 응답 형식 통일

모든 비즈니스 예외는 `ErrorCode` enum 하나에서 관리합니다. 상태 코드와 메시지를 한곳에서 정의하므로, 에러 문구를 바꿀 때 이 파일만 고치면 됩니다.

```json
{ "code": "STATION_NOT_ON_ROUTE", "message": "해당 열차는 입력하신 역에 정차하지 않습니다." }
```

`GlobalExceptionHandler`가 다음 예외를 같은 형식으로 변환합니다.

- `BusinessException`
- `@Valid` 요청 본문 검증 실패
- `@RequestParam` 검증 실패 (예: 날짜 `\d{8}`, 시각 `\d{6}` 형식)

### 남의 리소스에는 403이 아니라 404

감시 건과 알림은 `findByIdAndUserEmail`로 **조회와 권한 검사를 쿼리 한 번에** 합니다. 다른 사용자의 id를 넣으면 404를 반환합니다. 403을 반환하면 "그 id가 존재한다"는 정보가 노출되기 때문입니다.

### 회원 탈퇴 시 연관 데이터 정리

`seat_watches`, `notifications`, `push_subscriptions`의 사용자 FK에는 `ON DELETE CASCADE`가 걸려 있습니다. 탈퇴하면 DB가 연관 데이터를 함께 지웁니다.

---

## 배포

```
main 브랜치 push
   ↓ GitHub Actions (self-hosted runner)
docker compose up -d --build   (app + postgres)
   ↓
/actuator/health 확인 (5초 간격, 최대 30회)
   ├─ 성공 → 사용하지 않는 이미지 정리
   └─ 실패 → 컨테이너 로그 100줄 출력 후 워크플로 실패
```

### Dockerfile — 멀티 스테이지 빌드

- **빌드 단계(JDK)**: `build.gradle`만 먼저 복사해 의존성을 받습니다. 소스만 바뀌면 의존성 레이어를 캐시에서 재사용합니다.
- **실행 단계(JRE)**: jar만 복사해 이미지를 가볍게 유지합니다. root가 아닌 전용 사용자(`spring`)로 실행합니다.
- `JAVA_OPTS`로 `-XX:MaxRAMPercentage=75`와 `-Duser.timezone=Asia/Seoul`을 주입합니다. 스케줄러가 한국 시각 기준으로 윈도우를 계산하기 때문에 시간대 설정이 필수입니다.

### 운영 설정 (`docker-compose.prod.yml`)

- **DB 포트를 외부에 열지 않습니다**. 앱 컨테이너만 내부 네트워크로 접근합니다.
- postgres 헬스 체크가 통과한 뒤에 앱을 시작합니다(`depends_on: service_healthy`).
- Swagger와 `/v3/api-docs`를 끄고, SQL 로그 출력을 끕니다.
- 비밀값은 저장소가 아니라 서버의 `~/raily-deploy/.env`에서 읽습니다.
- `concurrency` 설정으로 배포가 겹쳐 실행되지 않게 합니다.

---

## 품질

```bash
./gradlew test
```

| 대상 | 검증 내용 |
|---|---|
| `SeatWatchTest` | 감시 윈도우 경계값(도착 10분 전 정각 포함, 출발 1초 후 제외), 다음 역 윈도우 전환, 여정 종료 판정 |
| `ScheduleViewApiResponseTest` | 코레일 응답 JSON의 3단 중첩 구조 파싱 |
| `RailyBackendApplicationTests` | 애플리케이션 컨텍스트 로딩 |

감시 윈도우 판정은 `SeatWatch` 엔티티 안의 **순수 메서드**라서 DB와 네트워크 없이 시각만 넣어 테스트할 수 있습니다.

---

## 한계점

- **좌석 조회 지연**: 구간 수 × 호차 수만큼 코레일을 **순차 호출**합니다. 정차역이 많은 열차는 응답이 느립니다. 병렬 호출과 짧은 캐시 도입이 후속 과제입니다.
- **의존성 주입 미적용**: `KorailClient`, `TrainService`, `KorailSeatService`를 컨트롤러에서 `new`로 생성합니다. 목(mock)으로 바꿔 끼울 수 없어서 통합 테스트가 어렵습니다.
- **공공데이터 시간표 차이**: 미래 날짜는 지난주 같은 요일의 기록으로 대체합니다. 임시 열차나 시간표 개편 직후에는 실제 정차 시각과 다를 수 있습니다.
- **refresh 토큰 미회전**: 재발급 시 accessToken만 새로 발급하고 refresh 토큰은 그대로 둡니다.
- **스키마 관리**: `ddl-auto: update`로 스키마를 관리합니다. Flyway 같은 마이그레이션 도구가 없어서 컬럼 삭제·변경 이력이 남지 않습니다.
- **단일 인스턴스 전제**: 스케줄러에 분산 락이 없습니다. 서버를 여러 대로 늘리면 같은 감시 건을 중복 처리할 수 있습니다.
- **테스트 범위**: 서비스·컨트롤러 계층의 테스트가 부족합니다.

---

## 관련 저장소

- 프론트엔드: [Trainners/Raily_Frontend](https://github.com/Trainners/Raily_Frontend) — React 19, TypeScript, Vite, RTK Query