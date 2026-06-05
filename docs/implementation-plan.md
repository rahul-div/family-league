# Family League - Complete Implementation Plan

## Context

Build a production-ready Spring Boot backend for a family-and-friends sports prediction platform. Users predict match outcomes (winner, toss, POTM) and league standings (team final positions). Points are awarded for correct predictions. A leaderboard tracks rankings across seasons.

This plan is derived from `family-league-requirements.md` as the source of truth, applying software engineering best practices and production-ready patterns throughout.

---

## Tech Stack

| Technology | Version | Purpose |
|---|---|---|
| Java | 21 | Language |
| Spring Boot | 3.3.x | Framework |
| PostgreSQL | 15+ | Database |
| Spring Data JPA + Hibernate 6 | managed | ORM |
| Flyway | managed | DB migrations |
| Spring Security 6 | managed | Auth framework |
| JJWT | 0.12.6 | JWT tokens |
| Spring Mail | managed | SMTP email |
| SpringDoc OpenAPI | 2.6.0 | Swagger docs |
| Caffeine | managed | Caching |
| Spring Boot Actuator | managed | Monitoring |
| Lombok | managed | Entity boilerplate |
| JUnit 5 + Mockito + Testcontainers | managed | Testing |

---

## Package Structure (Feature-Based Vertical Slices)

```
src/main/java/com/familyleague/
├── FamilyLeagueApplication.java
├── common/                          # Cross-cutting shared code
│   ├── entity/
│   │   └── BaseEntity.java          # Abstract auditable base
│   ├── dto/
│   │   ├── ApiResponse.java         # Envelope response wrapper
│   │   └── PagedResponse.java       # Paginated response wrapper
│   ├── config/
│   │   ├── AppProperties.java       # @ConfigurationProperties
│   │   ├── AsyncConfig.java         # Thread pools
│   │   ├── AuditConfig.java         # JPA auditing
│   │   ├── CacheConfig.java         # Caffeine caches
│   │   ├── CorsConfig.java          # CORS rules
│   │   └── OpenApiConfig.java       # Swagger metadata
│   ├── exception/
│   │   ├── GlobalExceptionHandler.java
│   │   ├── ErrorResponse.java
│   │   ├── ResourceNotFoundException.java
│   │   ├── ConflictException.java
│   │   ├── BadRequestException.java
│   │   └── ForbiddenException.java
│   └── validation/
│       ├── ValidPassword.java        # Custom annotation
│       └── PasswordConstraintValidator.java
│
├── auth/                             # Authentication & JWT
│   ├── controller/
│   │   └── AuthController.java
│   ├── dto/
│   │   ├── RegisterRequest.java
│   │   ├── LoginRequest.java
│   │   └── AuthResponse.java
│   ├── service/
│   │   ├── AuthService.java          # Interface
│   │   └── AuthServiceImpl.java
│   └── security/
│       ├── SecurityConfig.java
│       ├── JwtTokenProvider.java
│       ├── JwtAuthenticationFilter.java
│       ├── JwtAuthenticationEntryPoint.java
│       ├── SecurityUser.java         # Utility: currentUserId(), isAdmin()
│       └── UserDetailsServiceImpl.java
│
├── user/                             # User management
│   ├── entity/
│   │   ├── User.java
│   │   └── UserRole.java            # Enum: ADMIN, USER
│   ├── controller/
│   │   └── UserController.java
│   ├── dto/
│   │   ├── UserResponse.java
│   │   ├── CreateUserRequest.java
│   │   └── UpdateProfileRequest.java
│   ├── repository/
│   │   └── UserRepository.java
│   ├── service/
│   │   ├── UserService.java          # Interface
│   │   └── UserServiceImpl.java
│   └── exception/
│       ├── UserNotFoundException.java
│       └── EmailAlreadyExistsException.java
│
├── league/                           # League + Season management
│   ├── entity/
│   │   ├── League.java
│   │   ├── Season.java
│   │   └── SeasonStatus.java        # Enum: 6 states
│   ├── controller/
│   │   ├── LeagueController.java
│   │   └── SeasonController.java
│   ├── dto/
│   │   ├── CreateLeagueRequest.java
│   │   ├── UpdateLeagueRequest.java
│   │   ├── LeagueResponse.java
│   │   ├── CreateSeasonRequest.java
│   │   ├── SeasonResponse.java
│   │   └── PublishFinalStandingsRequest.java
│   ├── repository/
│   │   ├── LeagueRepository.java
│   │   └── SeasonRepository.java
│   ├── service/
│   │   ├── LeagueService.java        # Interface
│   │   ├── LeagueServiceImpl.java
│   │   ├── SeasonService.java        # Interface
│   │   └── SeasonServiceImpl.java
│   └── exception/
│       ├── LeagueNotFoundException.java
│       ├── SeasonNotFoundException.java
│       ├── SeasonNotActivatableException.java
│       └── SeasonNotClosableException.java
│
├── team/                             # Team + Season enrollment
│   ├── entity/
│   │   ├── Team.java
│   │   └── SeasonTeam.java
│   ├── controller/
│   │   └── TeamController.java
│   ├── dto/
│   │   ├── CreateTeamRequest.java
│   │   ├── UpdateTeamRequest.java
│   │   ├── TeamResponse.java
│   │   ├── EnrollTeamRequest.java
│   │   └── SeasonTeamResponse.java
│   ├── repository/
│   │   ├── TeamRepository.java
│   │   └── SeasonTeamRepository.java
│   ├── service/
│   │   ├── TeamService.java          # Interface
│   │   ├── TeamServiceImpl.java
│   │   ├── SeasonTeamService.java    # Interface
│   │   └── SeasonTeamServiceImpl.java
│   └── exception/
│       ├── TeamNotFoundException.java
│       └── TeamAlreadyEnrolledException.java
│
├── player/                           # Player management
│   ├── entity/
│   │   ├── Player.java
│   │   └── PlayerRole.java          # Enum: BATSMAN, BOWLER, ALL_ROUNDER, WICKET_KEEPER
│   ├── controller/
│   │   └── PlayerController.java
│   ├── dto/
│   │   ├── CreatePlayerRequest.java
│   │   ├── UpdatePlayerRequest.java
│   │   └── PlayerResponse.java
│   ├── repository/
│   │   └── PlayerRepository.java
│   ├── service/
│   │   ├── PlayerService.java        # Interface
│   │   └── PlayerServiceImpl.java
│   └── exception/
│       └── PlayerNotFoundException.java
│
├── match/                            # Match scheduling + results
│   ├── entity/
│   │   ├── Match.java
│   │   ├── MatchStatus.java         # Enum: 5 states
│   │   └── MatchResult.java
│   ├── controller/
│   │   ├── MatchController.java
│   │   └── ResultController.java
│   ├── dto/
│   │   ├── CreateMatchRequest.java
│   │   ├── UpdateMatchRequest.java
│   │   ├── MatchResponse.java
│   │   ├── PublishResultRequest.java
│   │   └── MatchResultResponse.java
│   ���── repository/
│   │   ├── MatchRepository.java
│   │   └── MatchResultRepository.java
│   ├── service/
│   │   ├── MatchService.java         # Interface
│   │   ├── MatchServiceImpl.java
│   │   ├── ResultService.java        # Interface
│   │   └── ResultServiceImpl.java
│   └── exception/
│       ├── MatchNotFoundException.java
│       ├── MatchNotModifiableException.java
│       └── MatchAlreadyCompletedException.java
│
├── prediction/                       # Match + League predictions
│   ├── entity/
│   │   ├── MatchPrediction.java
│   │   └── LeaguePrediction.java
│   ├── controller/
│   │   └── PredictionController.java
│   ├── dto/
│   │   ├── SubmitMatchPredictionRequest.java
│   │   ├── MatchPredictionResponse.java
│   │   ├── SubmitLeaguePredictionRequest.java
│   │   ├── LeaguePredictionResponse.java
│   │   └── HeadToHeadResponse.java
│   ├── repository/
│   │   ├── MatchPredictionRepository.java
│   │   └── LeaguePredictionRepository.java
│   ├── service/
│   │   ├── PredictionService.java    # Interface
│   │   └── PredictionServiceImpl.java
│   └── exception/
│       ├── PredictionLockedException.java
│       ├── InvalidPredictionException.java
│       └── PredictionWindowOpenException.java
│
├── scoring/                          # Scoring engine + Leaderboard
│   ├── entity/
│   │   ├── MatchScoreDetail.java
│   │   ├── SeasonScoreDetail.java
│   │   └── UserSeasonScore.java
│   ├── controller/
│   │   └── LeaderboardController.java
│   ├── dto/
│   │   ├── LeaderboardEntryResponse.java
│   │   ├── MyRankResponse.java
│   │   ├── MatchScoreDetailResponse.java
│   │   └── SeasonScoreDetailResponse.java
│   ├── repository/
│   │   ├── MatchScoreDetailRepository.java
│   │   ├── SeasonScoreDetailRepository.java
│   │   └── UserSeasonScoreRepository.java
│   ├── service/
│   │   ├── ScoreCalculationService.java    # Interface
│   │   ├── ScoreCalculationServiceImpl.java
│   │   ├── LeaderboardService.java         # Interface
│   │   └── LeaderboardServiceImpl.java
│   └── exception/
│       └── ScoreNotFoundException.java
│
├── standing/                         # League standings (team W-D-L)
│   ├── entity/
│   │   └── LeagueStanding.java
│   ├── controller/
│   │   └── StandingController.java
│   ├── dto/
│   │   └── LeagueStandingResponse.java
│   ├── repository/
│   │   └── LeagueStandingRepository.java
│   ├── service/
│   │   ├── StandingService.java      # Interface
│   │   └── StandingServiceImpl.java
│   └── exception/
│       └── StandingNotFoundException.java
│
├── notification/                     # Email system
│   ├── entity/
│   │   ├── EmailNotification.java
│   │   ├── NotificationEventType.java
│   │   └── NotificationStatus.java
│   ├── controller/
│   │   └── NotificationController.java
│   ├── dto/
│   │   ├── BulkNotificationRequest.java
│   │   └── EmailNotificationResponse.java
│   ├── repository/
│   │   └── EmailNotificationRepository.java
│   ├── service/
│   │   ├── EmailNotificationService.java   # Interface
│   │   └── EmailNotificationServiceImpl.java
│   └── scheduler/
│       └── NotificationScheduler.java
│
├── appconfig/                        # Runtime-tunable config
│   ├── entity/
│   │   └── AppConfig.java
│   ├── controller/
│   │   └── AppConfigController.java
│   ├── dto/
│   │   ├── AppConfigResponse.java
│   │   └── UpdateAppConfigRequest.java
│   ├── repository/
│   │   └── AppConfigRepository.java
│   ├── service/
│   │   ├── AppConfigService.java     # Interface
│   │   └── AppConfigServiceImpl.java
│   └── exception/
│       └── AppConfigNotFoundException.java
│
└── scheduler/                        # Shared scheduled tasks
    ├── PredictionLockScheduler.java
    └── SeasonLifecycleScheduler.java

src/main/resources/
├── application.yml
├── application-dev.yml
├── application-prod.yml
├── application-https.yml
├── logback-spring.xml
├── keystore.p12                      # Self-signed cert for HTTPS
└── db/migration/
    ├── V1__create_users.sql
    ├── V2__create_leagues_seasons.sql
    ├── V3__create_teams_season_teams.sql
    ├── V4__create_players.sql
    ├── V5__create_matches_results.sql
    ├── V6__create_league_standings.sql
    ├── V7__create_predictions.sql
    ├── V8__create_scoring.sql
    ├── V9__create_email_notifications.sql
    ├── V10__create_app_config.sql
    ├── V11__seed_app_config.sql
    ├── V12__seed_admin_user.sql
    ├── V13__seed_ipl_data.sql
    └── V14__prediction_lock_triggers.sql

src/test/java/com/familyleague/
├── FamilyLeagueApplicationTests.java
├── auth/
│   ├── controller/AuthControllerTest.java        # Unit (MockMvc)
│   └── AuthIntegrationTest.java                  # Integration (Testcontainers)
├── user/
│   ├── controller/UserControllerTest.java
│   └── service/UserServiceTest.java
├── league/
│   ├── controller/LeagueControllerTest.java
│   ├── service/LeagueServiceTest.java
│   └── service/SeasonServiceTest.java
├── team/
│   ├── service/TeamServiceTest.java
│   └── service/SeasonTeamServiceTest.java
├── player/
│   └── service/PlayerServiceTest.java
├── match/
│   ├── controller/MatchControllerTest.java
│   ├── service/MatchServiceTest.java
│   └── service/ResultServiceTest.java
├── prediction/
│   ├── controller/PredictionControllerTest.java
│   ├── service/PredictionServiceTest.java
│   └── PredictionIntegrationTest.java            # Integration
├── scoring/
│   ├── service/ScoreCalculationServiceTest.java
│   └── service/LeaderboardServiceTest.java
├── notification/
│   └── service/EmailNotificationServiceTest.java
└── e2e/
    ├── BaseE2ETest.java                          # Shared setup (Testcontainers PostgreSQL)
    ├── AuthE2ETest.java
    ├── LeagueSeasonE2ETest.java
    ├── MatchPredictionE2ETest.java
    ├── ScoringLeaderboardE2ETest.java
    └── FullWorkflowE2ETest.java                  # Complete happy path

docs/
├── data-model.md
├── decision-log.md
├── api-reference.md
├── er-diagram.mmd
└── prompt-summary.md
```

---

## Phase 1: Project Bootstrap & Configuration

### Step 1.1 — Initialize Spring Boot Project

**File: `pom.xml`**

Dependencies:
- `spring-boot-starter-web`
- `spring-boot-starter-data-jpa`
- `spring-boot-starter-validation`
- `spring-boot-starter-security`
- `spring-boot-starter-mail`
- `spring-boot-starter-cache`
- `spring-boot-starter-actuator`
- `spring-boot-devtools` (runtime, optional)
- `postgresql` (runtime)
- `flyway-core` + `flyway-database-postgresql`
- `io.jsonwebtoken:jjwt-api:0.12.6` + `jjwt-impl` + `jjwt-jackson`
- `org.springdoc:springdoc-openapi-starter-webmvc-ui:2.6.0`
- `com.github.ben-manes.caffeine:caffeine`
- `org.projectlombok:lombok` (provided)
- `spring-boot-starter-test` (test)
- `spring-security-test` (test)
- `org.testcontainers:postgresql` (test)
- `org.testcontainers:junit-jupiter` (test)

Java version: 21
Spring Boot parent: 3.3.x

### Step 1.2 — Application Entry Point

**File: `src/main/java/com/familyleague/FamilyLeagueApplication.java`**
- `@SpringBootApplication`
- `@EnableScheduling`
- `@EnableAsync`
- `@EnableCaching`
- `@EnableJpaAuditing`

### Step 1.3 — Configuration Files

**File: `src/main/resources/application.yml`**
```yaml
server:
  port: 8080
  servlet:
    context-path: /api/v1

spring:
  datasource:
    url: ${DB_URL:jdbc:postgresql://localhost:5432/family_league}
    username: ${DB_USERNAME:postgres}
    password: ${DB_PASSWORD:postgres}
  jpa:
    hibernate:
      ddl-auto: validate
    properties:
      hibernate:
        dialect: org.hibernate.dialect.PostgreSQLDialect
    open-in-view: false
  flyway:
    enabled: true
    baseline-on-migrate: true
  mail:
    host: ${MAIL_HOST:smtp.gmail.com}
    port: ${MAIL_PORT:587}
    username: ${MAIL_USERNAME:}
    password: ${MAIL_PASSWORD:}
    properties:
      mail.smtp.auth: true
      mail.smtp.starttls.enable: true
  cache:
    type: caffeine

app:
  jwt:
    secret: ${JWT_SECRET:<64-char-default>}
    expiration-ms: ${JWT_EXPIRATION_MS:86400000}
  prediction:
    league-lock-hours: 4
    match-lock-hours: 1
    reminder-hours-before-match: 2
  notification:
    admin-email: ${ADMIN_EMAIL:admin@familyleague.com}
    from-email: ${FROM_EMAIL:noreply@familyleague.com}
    max-retry-count: 3
  scheduler:
    prediction-lock-cron: "0 * * * * *"       # every minute
    email-process-cron: "0 */5 * * * *"       # every 5 minutes
    result-alert-cron: "0 */5 * * * *"        # every 5 minutes
  result:
    pending-alert-hours: 2

management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics

logging:
  level:
    com.familyleague: DEBUG
```

**File: `src/main/resources/application-dev.yml`**
- Verbose SQL logging, H2 console (optional)

**File: `src/main/resources/application-prod.yml`**
- INFO level logging, connection pooling tuning

**File: `src/main/resources/application-https.yml`**
```yaml
server:
  port: 8443
  ssl:
    key-store: classpath:keystore.p12
    key-store-type: PKCS12
    key-store-password: ${SSL_KEYSTORE_PASSWORD:changeit}
    key-alias: ${SSL_KEY_ALIAS:familyleague}
```

**File: `src/main/resources/logback-spring.xml`**
- Console appender (always)
- RollingFile appender (`logs/family-league.log`, daily rotation, 30-day retention, 1GB cap)
- Profile-specific levels: dev=DEBUG, prod=INFO

### Step 1.4 — AppProperties

**File: `common/config/AppProperties.java`**
- `@ConfigurationProperties(prefix = "app")`
- Nested records: `Jwt(secret, expirationMs)`, `Prediction(leagueLockHours, matchLockHours, reminderHoursBeforeMatch)`, `Notification(adminEmail, fromEmail, maxRetryCount)`, `Scheduler(predictionLockCron, emailProcessCron, resultAlertCron)`, `Result(pendingAlertHours)`

---

## Phase 2: Common/Shared Layer

### Step 2.1 — BaseEntity

**File: `common/entity/BaseEntity.java`**
```
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
@Getter @Setter
abstract class BaseEntity {
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    UUID id;

    @CreatedDate  Instant createdAt;
    @CreatedBy    UUID createdBy;
    @LastModifiedDate  Instant updatedAt;
    @LastModifiedBy    UUID updatedBy;

    boolean isDeleted = false;
    Instant deletedAt;
    UUID deletedBy;

    void softDelete(UUID deletedByUser) { ... }
}
```

### Step 2.2 — AuditConfig

**File: `common/config/AuditConfig.java`**
- `@Configuration` + `@EnableJpaAuditing`
- `AuditorAware<UUID>` bean that extracts user ID from SecurityContextHolder

### Step 2.3 — API Response Envelope

**File: `common/dto/ApiResponse.java`** (Java record)
```java
record ApiResponse<T>(boolean success, String message, T data, String path, Instant timestamp) {
    static <T> ApiResponse<T> success(T data) { ... }
    static <T> ApiResponse<T> success(String message, T data) { ... }
    static ApiResponse<Void> error(String message) { ... }
}
```

**File: `common/dto/PagedResponse.java`** (Java record)
```java
record PagedResponse<T>(List<T> content, int page, int size,
    long totalElements, int totalPages, boolean last) {
    static <T> PagedResponse<T> from(Page<?> page, List<T> content) { ... }
}
```

### Step 2.4 — Exception Hierarchy & Global Handler

**Custom exceptions** (all extend RuntimeException):
| Exception | HTTP Status |
|---|---|
| `ResourceNotFoundException` | 404 |
| `ConflictException` | 409 |
| `BadRequestException` | 400 |
| `ForbiddenException` | 403 |

**File: `common/exception/ErrorResponse.java`** (Java record)
- `timestamp`, `status`, `error`, `message`, `path`, `validationErrors` (Map)

**File: `common/exception/GlobalExceptionHandler.java`**
- `@RestControllerAdvice`
- Handlers for: `ResourceNotFoundException` (404), `ConflictException` (409), `BadRequestException` (400), `ForbiddenException` (403), `MethodArgumentNotValidException` (400 with field errors), `HttpMessageNotReadableException` (400), `BadCredentialsException` (401), `AccessDeniedException` (403), `DataIntegrityViolationException` (409, with special handling for prediction lock trigger messages containing "PREDICTION_LOCKED"), catch-all Exception (500)

### Step 2.5 — Password Validation

**File: `common/validation/ValidPassword.java`** — Custom annotation
**File: `common/validation/PasswordConstraintValidator.java`** — Min 8 chars, 1 upper, 1 lower, 1 digit, 1 special

### Step 2.6 — Async Config

**File: `common/config/AsyncConfig.java`**
Three thread pools:
| Bean Name | Core | Max | Queue | Prefix |
|---|---|---|---|---|
| `scoreExecutor` | 2 | 5 | 50 | `score-` |
| `emailExecutor` | 3 | 10 | 100 | `email-` |
| `taskExecutor` (default) | 5 | 20 | 100 | `async-` |

### Step 2.7 — Cache Config

**File: `common/config/CacheConfig.java`**
Caffeine caches:
| Cache Name | TTL | Max Size |
|---|---|---|
| `app-config` | 5 min | 100 |
| `leagues` | 5 min | 500 |
| `teams` | 5 min | 500 |
| `players` | 5 min | 2000 |

### Step 2.8 — CORS Config

**File: `common/config/CorsConfig.java`**
- Allow all origins for development, configurable for production

### Step 2.9 — OpenAPI Config

**File: `common/config/OpenApiConfig.java`**
- API title, version, description
- JWT Bearer security scheme definition

---

## Phase 3: Database Migrations (Flyway)

All migrations in `src/main/resources/db/migration/`.

### V1__create_users.sql
```sql
CREATE TABLE users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    username VARCHAR(50) UNIQUE NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    display_name VARCHAR(100) NOT NULL,
    avatar_url VARCHAR(500),
    role VARCHAR(20) NOT NULL DEFAULT 'USER'
        CHECK (role IN ('ADMIN', 'USER')),
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    -- audit columns
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    created_by UUID,
    updated_at TIMESTAMPTZ,
    updated_by UUID,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at TIMESTAMPTZ,
    deleted_by UUID
);
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_username ON users(username);
CREATE INDEX idx_users_is_deleted ON users(is_deleted);
```

### V2__create_leagues_seasons.sql
```sql
CREATE TABLE leagues (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(255) NOT NULL UNIQUE,
    description TEXT,
    sport_type VARCHAR(50),
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    -- audit columns (same as users)
);

CREATE TABLE seasons (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    league_id UUID NOT NULL REFERENCES leagues(id),
    name VARCHAR(255) NOT NULL,
    season_number INTEGER,
    description TEXT,
    status VARCHAR(30) NOT NULL DEFAULT 'UPCOMING'
        CHECK (status IN ('UPCOMING','PREDICTION_OPEN','PREDICTION_LOCKED',
                          'IN_PROGRESS','COMPLETED','CLOSED')),
    league_prediction_lock_hours INTEGER DEFAULT 4,
    match_prediction_lock_hours INTEGER DEFAULT 1,
    first_match_at TIMESTAMPTZ,
    prediction_locked_at TIMESTAMPTZ,
    started_at TIMESTAMPTZ,
    completed_at TIMESTAMPTZ,
    closed_at TIMESTAMPTZ,
    -- audit columns
);
CREATE INDEX idx_seasons_league_id ON seasons(league_id);
CREATE INDEX idx_seasons_status ON seasons(status);
```

### V3__create_teams_season_teams.sql
```sql
CREATE TABLE teams (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(255) NOT NULL UNIQUE,
    short_name VARCHAR(10),
    logo_url VARCHAR(500),
    description TEXT,
    -- audit columns
);

CREATE TABLE season_teams (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    season_id UUID NOT NULL REFERENCES seasons(id),
    team_id UUID NOT NULL REFERENCES teams(id),
    seed_position INTEGER,
    current_position INTEGER,
    UNIQUE(season_id, team_id),
    -- audit columns
);
```

### V4__create_players.sql
```sql
CREATE TABLE players (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    team_id UUID NOT NULL REFERENCES teams(id),
    name VARCHAR(255) NOT NULL,
    jersey_number INTEGER,
    player_role VARCHAR(30) CHECK (player_role IN
        ('BATSMAN','BOWLER','ALL_ROUNDER','WICKET_KEEPER')),
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    -- audit columns
);
CREATE INDEX idx_players_team_id ON players(team_id);
```

### V5__create_matches_results.sql
```sql
CREATE TABLE matches (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    season_id UUID NOT NULL REFERENCES seasons(id),
    home_team_id UUID NOT NULL REFERENCES teams(id),
    away_team_id UUID NOT NULL REFERENCES teams(id),
    match_number INTEGER,
    scheduled_at TIMESTAMPTZ NOT NULL,
    venue VARCHAR(200),
    prediction_lock_time TIMESTAMPTZ NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'SCHEDULED'
        CHECK (status IN ('SCHEDULED','IN_PROGRESS','COMPLETED','CANCELLED','POSTPONED')),
    CHECK (home_team_id <> away_team_id),
    UNIQUE(season_id, match_number),
    -- audit columns
);
CREATE INDEX idx_matches_season_id ON matches(season_id);
CREATE INDEX idx_matches_scheduled_at ON matches(scheduled_at);
CREATE INDEX idx_matches_status ON matches(status);
CREATE INDEX idx_matches_lock_time ON matches(prediction_lock_time);

CREATE TABLE match_results (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    match_id UUID NOT NULL UNIQUE REFERENCES matches(id),
    winner_team_id UUID REFERENCES teams(id),
    toss_winner_team_id UUID REFERENCES teams(id),
    player_of_match_id UUID REFERENCES players(id),
    is_tie BOOLEAN NOT NULL DEFAULT FALSE,
    published_at TIMESTAMPTZ,
    published_by UUID,
    -- audit columns
);
```

### V6__create_league_standings.sql
```sql
CREATE TABLE league_standings (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    season_id UUID NOT NULL REFERENCES seasons(id),
    team_id UUID NOT NULL REFERENCES teams(id),
    current_position INTEGER,
    matches_played INTEGER DEFAULT 0,
    wins INTEGER DEFAULT 0,
    draws INTEGER DEFAULT 0,
    losses INTEGER DEFAULT 0,
    points_in_league INTEGER DEFAULT 0,
    UNIQUE(season_id, team_id),
    -- audit columns
);
```

### V7__create_predictions.sql
```sql
CREATE TABLE match_predictions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    match_id UUID NOT NULL REFERENCES matches(id),
    user_id UUID NOT NULL REFERENCES users(id),
    predicted_winner_team_id UUID REFERENCES teams(id),
    predicted_toss_winner_team_id UUID REFERENCES teams(id),
    predicted_potm_player_id UUID REFERENCES players(id),
    is_locked BOOLEAN NOT NULL DEFAULT FALSE,
    submitted_at TIMESTAMPTZ,
    UNIQUE(match_id, user_id),
    -- audit columns
);
CREATE INDEX idx_match_predictions_user_id ON match_predictions(user_id);
CREATE INDEX idx_match_predictions_match_id ON match_predictions(match_id);

CREATE TABLE league_predictions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    season_id UUID NOT NULL REFERENCES seasons(id),
    user_id UUID NOT NULL REFERENCES users(id),
    team_id UUID NOT NULL REFERENCES teams(id),
    predicted_position INTEGER NOT NULL,
    is_locked BOOLEAN NOT NULL DEFAULT FALSE,
    submitted_at TIMESTAMPTZ,
    UNIQUE(season_id, user_id, team_id),
    UNIQUE(season_id, user_id, predicted_position),
    -- audit columns
);
CREATE INDEX idx_league_predictions_user_id ON league_predictions(user_id);
CREATE INDEX idx_league_predictions_season_id ON league_predictions(season_id);
```

### V8__create_scoring.sql
```sql
CREATE TABLE match_score_details (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    match_id UUID NOT NULL REFERENCES matches(id),
    user_id UUID NOT NULL REFERENCES users(id),
    season_id UUID NOT NULL REFERENCES seasons(id),
    winner_correct BOOLEAN DEFAULT FALSE,
    toss_winner_correct BOOLEAN DEFAULT FALSE,
    potm_correct BOOLEAN DEFAULT FALSE,
    total_match_points INTEGER DEFAULT 0,
    calculated_at TIMESTAMPTZ,
    UNIQUE(match_id, user_id),
    -- audit columns
);
CREATE INDEX idx_match_scores_season_user ON match_score_details(season_id, user_id);

CREATE TABLE season_score_details (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    season_id UUID NOT NULL REFERENCES seasons(id),
    user_id UUID NOT NULL REFERENCES users(id),
    team_id UUID NOT NULL REFERENCES teams(id),
    predicted_position INTEGER NOT NULL,
    actual_position INTEGER,
    points_earned INTEGER DEFAULT 0,
    calculated_at TIMESTAMPTZ,
    UNIQUE(season_id, user_id, team_id),
    -- audit columns
);
CREATE INDEX idx_season_scores_season_user ON season_score_details(season_id, user_id);

CREATE TABLE user_season_scores (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id),
    season_id UUID NOT NULL REFERENCES seasons(id),
    match_points INTEGER DEFAULT 0,
    season_prediction_points INTEGER DEFAULT 0,
    total_points INTEGER DEFAULT 0,
    rank INTEGER,
    last_calculated_at TIMESTAMPTZ,
    UNIQUE(user_id, season_id),
    -- audit columns
);
CREATE INDEX idx_user_season_scores_season ON user_season_scores(season_id);
```

### V9__create_email_notifications.sql
```sql
CREATE TABLE email_notifications (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    to_email VARCHAR(255) NOT NULL,
    user_id UUID REFERENCES users(id),
    subject VARCHAR(500),
    body TEXT,
    event_type VARCHAR(50) CHECK (event_type IN (
        'MATCH_PREDICTION_REMINDER','MATCH_PREDICTION_LOCKED',
        'RESULT_PUBLISHED','LEADERBOARD_UPDATED',
        'SEASON_STARTED','SEASON_CLOSED',
        'BULK_NOTIFICATION','WELCOME','PENDING_RESULT_ALERT')),
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING'
        CHECK (status IN ('PENDING','SENT','FAILED')),
    sent_at TIMESTAMPTZ,
    retry_count INTEGER DEFAULT 0,
    error_message TEXT,
    reference_id UUID,
    -- audit columns
);
CREATE INDEX idx_email_notifications_status ON email_notifications(status);
CREATE INDEX idx_email_notifications_user_id ON email_notifications(user_id);
CREATE INDEX idx_email_notifications_event_type ON email_notifications(event_type);
```

### V10__create_app_config.sql
```sql
CREATE TABLE app_config (
    key VARCHAR(255) PRIMARY KEY,
    value VARCHAR(500) NOT NULL,
    description TEXT,
    updated_at TIMESTAMPTZ DEFAULT now()
);
```

### V11__seed_app_config.sql
Inserts default config:
| Key | Value | Description |
|---|---|---|
| `match.lock.offset.hours` | `1` | Hours before match to lock predictions |
| `league.lock.offset.hours` | `4` | Hours before first match to lock league predictions |
| `match.reminder.offset.hours` | `2` | Hours before lock to send reminders |
| `jwt.expiry.seconds` | `86400` | JWT token TTL |
| `leaderboard.recalc.async` | `true` | Async leaderboard recalculation |
| `result.pending.alert.hours` | `2` | Hours after match to alert admin |

### V12__seed_admin_user.sql
Insert admin user (admin@familyleague.com / Admin@1234, BCrypt hashed)

### V13__seed_ipl_data.sql
- 1 League: "Indian Premier League" (sport_type: CRICKET)
- 1 Season: "IPL 2024" (UPCOMING status)
- 10 Teams: CSK, DC, GT, KKR, LSG, MI, PBKS, RR, RCB, SRH (with short_names)
- 10 Season-Team enrollments
- 25 Players per team (250 total, real names, with player_role assigned)
- 3 sample scheduled matches

### V14__prediction_lock_triggers.sql
Two PostgreSQL `BEFORE INSERT OR UPDATE` triggers:

**`trg_enforce_match_prediction_lock`** on `match_predictions`:
- On INSERT: block if current_time > match.prediction_lock_time
- On UPDATE of prediction fields: block if is_locked=true OR current_time > lock_time
- Permits soft-delete and audit-only updates
- Raises: `PREDICTION_LOCKED: Match prediction window has closed`

**`trg_enforce_league_prediction_lock`** on `league_predictions`:
- On INSERT: block if season status in (PREDICTION_LOCKED, IN_PROGRESS, COMPLETED, CLOSED) OR current_time > prediction_locked_at
- On UPDATE of position: block when locked
- Raises: `PREDICTION_LOCKED: League prediction window has closed`

---

## Phase 4: Domain Entities

Build in dependency order. All extend `BaseEntity`. All use `@SQLRestriction("is_deleted = false")` for automatic soft-delete filtering.

### Step 4.1 — Enums
- `UserRole`: ADMIN, USER
- `SeasonStatus`: UPCOMING, PREDICTION_OPEN, PREDICTION_LOCKED, IN_PROGRESS, COMPLETED, CLOSED
- `MatchStatus`: SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED, POSTPONED
- `PlayerRole`: BATSMAN, BOWLER, ALL_ROUNDER, WICKET_KEEPER
- `NotificationEventType`: MATCH_PREDICTION_REMINDER, MATCH_PREDICTION_LOCKED, RESULT_PUBLISHED, LEADERBOARD_UPDATED, SEASON_STARTED, SEASON_CLOSED, BULK_NOTIFICATION, WELCOME, PENDING_RESULT_ALERT
- `NotificationStatus`: PENDING, SENT, FAILED

### Step 4.2 — User Entity
- Fields: username (unique), email (unique), passwordHash, displayName, avatarUrl, role (UserRole, default USER), isActive (default true)
- `@Enumerated(EnumType.STRING)` for role

### Step 4.3 — League Entity
- Fields: name (unique), description, sportType, isActive
- `@OneToMany(mappedBy = "league", cascade = ALL, fetch = LAZY)` to Season

### Step 4.4 — Season Entity
- Fields: league (ManyToOne LAZY), name, seasonNumber, description, status (SeasonStatus, default UPCOMING), leaguePredictionLockHours, matchPredictionLockHours, firstMatchAt, predictionLockedAt, startedAt, completedAt, closedAt
- Helper methods: `isClosed()`, `isPredictionLocked()`
- `@OneToMany` to SeasonTeam, Match

### Step 4.5 — Team Entity
- Fields: name (unique), shortName, logoUrl, description
- `@OneToMany` to Player

### Step 4.6 — SeasonTeam Entity
- Fields: season (ManyToOne), team (ManyToOne), seedPosition, currentPosition
- `@Table(uniqueConstraints = @UniqueConstraint(columns = {"season_id", "team_id"}))`

### Step 4.7 — Player Entity
- Fields: team (ManyToOne), name, jerseyNumber, playerRole (PlayerRole), isActive
- Setting team auto-updates if needed

### Step 4.8 — Match Entity
- Fields: season (ManyToOne), homeTeam, awayTeam (both ManyToOne), matchNumber, scheduledAt, venue, predictionLockTime, status (MatchStatus)
- `@Table(uniqueConstraints = @UniqueConstraint(columns = {"season_id", "match_number"}))`
- Check constraint: `home_team_id <> away_team_id`
- Helper: `isPredictionLocked()` — true if now > lockTime OR status in (IN_PROGRESS, COMPLETED, CANCELLED)

### Step 4.9 — MatchResult Entity
- Fields: match (OneToOne unique), winnerTeam, tossWinnerTeam (Team), playerOfMatch (Player), isTie, publishedAt, publishedBy

### Step 4.10 — MatchPrediction Entity
- Fields: match, user (ManyToOne), predictedWinnerTeam, predictedTossWinnerTeam (Team), predictedPotmPlayer (Player), isLocked, submittedAt
- Unique on (match_id, user_id)

### Step 4.11 — LeaguePrediction Entity
- Fields: season, user, team (ManyToOne), predictedPosition, isLocked, submittedAt
- Unique on (season_id, user_id, team_id) AND (season_id, user_id, predicted_position)

### Step 4.12 — LeagueStanding Entity
- Fields: season, team (ManyToOne), currentPosition, matchesPlayed, wins, draws, losses, pointsInLeague
- Unique on (season_id, team_id)

### Step 4.13 — MatchScoreDetail Entity
- Fields: match, user, season (ManyToOne), winnerCorrect, tossWinnerCorrect, potmCorrect, totalMatchPoints, calculatedAt
- Unique on (match_id, user_id)

### Step 4.14 — SeasonScoreDetail Entity
- Fields: season, user, team (ManyToOne), predictedPosition, actualPosition, pointsEarned, calculatedAt
- Unique on (season_id, user_id, team_id)

### Step 4.15 — UserSeasonScore Entity
- Fields: user, season (ManyToOne), matchPoints, seasonPredictionPoints, totalPoints, rank, lastCalculatedAt
- Unique on (user_id, season_id)

### Step 4.16 — EmailNotification Entity
- Fields: toEmail, userId, subject, body, eventType (NotificationEventType), status (NotificationStatus, default PENDING), sentAt, retryCount, errorMessage, referenceId

### Step 4.17 — AppConfig Entity (does NOT extend BaseEntity)
- Fields: key (PK String), value, description, updatedAt

---

## Phase 5: Repositories

All extend `JpaRepository<Entity, UUID>`. Key custom queries:

| Repository | Notable Custom Methods |
|---|---|
| `UserRepository` | `findByEmail()`, `findByUsername()`, `findByEmailOrUsername()`, `existsByEmail()`, `existsByUsername()`, `searchUsers(query, Pageable)` — JPQL LIKE on username/email/displayName, `findAllAdmins()` |
| `LeagueRepository` | `search(query, Pageable)` — case-insensitive name LIKE |
| `SeasonRepository` | `findByLeagueId(Pageable)`, `findSeasonsToLock()` — status=PREDICTION_OPEN AND predictionLockedAt <= now |
| `TeamRepository` | `search(query, Pageable)` |
| `SeasonTeamRepository` | `findBySeasonId()`, `findBySeasonIdAndTeamId()`, `existsBySeasonIdAndTeamId()`, `countBySeasonId()`, `findBySeasonIdOrderByCurrentPositionNullsLast()` |
| `PlayerRepository` | `findByTeamId(Pageable)`, `findByTeamIdIn(teamIds)` |
| `MatchRepository` | `findBySeasonId(Pageable)`, `findBySeasonIdAndStatus()`, `findMatchesToLock()` — SCHEDULED with lockTime within window, `findFirstMatchTime(seasonId)`, `findMatchesNeedingReminder()` |
| `MatchResultRepository` | `findByMatchId()`, `existsByMatchId()` |
| `MatchPredictionRepository` | `findByMatchIdAndUserId()`, `findByMatchId(Pageable)`, `findUserIdsWhoSubmittedPrediction(matchId)`, `findAllByMatchId()` |
| `LeaguePredictionRepository` | `findBySeasonIdAndUserId()`, `findBySeasonId()`, `deleteBySeasonIdAndUserId()` |
| `LeagueStandingRepository` | `findBySeasonIdOrderByCurrentPosition()`, `findBySeasonIdAndTeamId()` |
| `MatchScoreDetailRepository` | `sumMatchPointsForUserInSeason(userId, seasonId)` — SUM with COALESCE |
| `SeasonScoreDetailRepository` | `sumSeasonPointsForUserInSeason(userId, seasonId)`, `deleteBySeasonId()` |
| `UserSeasonScoreRepository` | `findBySeasonIdOrderByTotalPointsDesc(Pageable)`, `findByUserIdAndSeasonId()` |
| `EmailNotificationRepository` | `findPendingWithRetryBelow(maxRetry)`, `findByUserId(Pageable)`, `findByEventTypeAndReferenceIdAndUserId()` — for idempotency, `findAll(Pageable)` with filtering |
| `AppConfigRepository` | extends `JpaRepository<AppConfig, String>` |

---

## Phase 6: Security Layer

### Step 6.1 — JwtTokenProvider
- Generate token: subject=userId, claims={username, role}, signed with HMAC-SHA256
- Validate token: parse, check expiry
- Extract userId, username, role from token

### Step 6.2 — JwtAuthenticationFilter (`OncePerRequestFilter`)
- Extract Bearer token from Authorization header
- Validate via JwtTokenProvider
- Load UserDetails by userId
- Set `UsernamePasswordAuthenticationToken` in SecurityContextHolder

### Step 6.3 — JwtAuthenticationEntryPoint
- Return JSON 401 response for unauthenticated requests

### Step 6.4 — UserDetailsServiceImpl
- Load by UUID (from JWT subject)
- Filter out deleted and inactive users
- Map role to `ROLE_ADMIN` or `ROLE_USER` GrantedAuthority

### Step 6.5 — SecurityUser Utility
- `currentUserId()` — extract UUID from security context
- `currentUsername()` — extract username
- `isAdmin()` — check ROLE_ADMIN authority

### Step 6.6 — SecurityConfig
```java
@Bean SecurityFilterChain filterChain(HttpSecurity http) {
    http.csrf(disable)
        .sessionManagement(STATELESS)
        .exceptionHandling(entryPoint)
        .authorizeHttpRequests(auth -> auth
            .requestMatchers(POST, "/auth/register", "/auth/login").permitAll()
            .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
            .requestMatchers("/actuator/health").permitAll()
            .requestMatchers(POST, "/auth/admin").hasRole("ADMIN")
            .requestMatchers(POST, "/leagues", "/teams", "/players", "/matches", "/seasons").hasRole("ADMIN")
            .requestMatchers(PUT, "/leagues/**", "/teams/**", "/players/**", "/matches/**").hasRole("ADMIN")
            .requestMatchers(DELETE, "/**").hasRole("ADMIN")
            .requestMatchers("/seasons/*/open", "/seasons/*/close").hasRole("ADMIN")
            .requestMatchers("/results/**").hasRole("ADMIN")       // publish results
            .requestMatchers("/leaderboard/*/recalculate").hasRole("ADMIN")
            .requestMatchers("/notifications/bulk").hasRole("ADMIN")
            .requestMatchers("/notifications").hasRole("ADMIN")    // admin email logs
            .requestMatchers("/config/**").hasRole("ADMIN")
            .anyRequest().authenticated()
        )
        .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);
}
```

---

## Phase 7: Service Interfaces + Implementations

Build in dependency order. Each service has an interface + implementation class.

### Step 7.1 — AppConfigService
- `String getValue(String key)` — `@Cacheable("app-config")`
- `int getIntValue(String key)`
- `List<AppConfigResponse> getAll()`
- `AppConfigResponse update(String key, UpdateAppConfigRequest)` — `@CacheEvict("app-config")`

### Step 7.2 — AuthService
- `AuthResponse register(RegisterRequest)` — validate email/username uniqueness, BCrypt encode, save, queue welcome email, return JWT
- `AuthResponse login(LoginRequest)` — find by email OR username, verify password, return JWT
- `AuthResponse createAdmin(RegisterRequest)` — same as register but with ADMIN role
- `AuthResponse refreshToken()` — generate fresh JWT for current user

### Step 7.3 — UserService
- `PagedResponse<UserResponse> getAllUsers(String query, Pageable)` — search + paginate
- `UserResponse getUserById(UUID id)`
- `UserResponse getCurrentUser()` — from security context
- `UserResponse updateProfile(UUID id, UpdateProfileRequest)` — owner or admin
- `void deleteUser(UUID id)` — soft-delete + deactivate (admin only)

### Step 7.4 — LeagueService
- `LeagueResponse create(CreateLeagueRequest)` — `@CacheEvict("leagues")`
- `PagedResponse<LeagueResponse> getAll(String query, Pageable)` — `@Cacheable("leagues")` (without query)
- `LeagueResponse getById(UUID id)`
- `LeagueResponse update(UUID id, UpdateLeagueRequest)` — `@CacheEvict("leagues")`
- `void delete(UUID id)` — soft-delete, `@CacheEvict("leagues")`

### Step 7.5 — SeasonService
- `SeasonResponse create(UUID leagueId, CreateSeasonRequest)`
- `PagedResponse<SeasonResponse> getByLeagueId(UUID leagueId, Pageable)`
- `SeasonResponse getById(UUID id)`
- `SeasonResponse openSeason(UUID id)` — validate: status=UPCOMING, >=2 teams enrolled, >=1 match scheduled, firstMatchAt set; calculate predictionLockedAt = firstMatchAt - lockHours; transition to PREDICTION_OPEN
- `SeasonResponse closeSeason(UUID id)` — validate: status=COMPLETED; transition to CLOSED with timestamp
- `void addTeam(UUID seasonId, EnrollTeamRequest)` — validate season not closed, team not already enrolled
- `void removeTeam(UUID seasonId, UUID teamId)` — soft-delete SeasonTeam
- `List<TeamResponse> getSeasonTeams(UUID seasonId)`
- `SeasonResponse publishFinalStandings(UUID seasonId, PublishFinalStandingsRequest)` — update standings, calculate season scores, transition to COMPLETED, trigger leaderboard recalc

### Step 7.6 — TeamService
- CRUD + search + pagination
- All mutations `@CacheEvict("teams")`

### Step 7.7 — SeasonTeamService
- Enrollment/unenrollment logic
- Get enrolled teams for season

### Step 7.8 — PlayerService
- CRUD + filter by team + pagination
- Transfer player between teams (update teamId)
- All mutations `@CacheEvict("players")`

### Step 7.9 — MatchService
- `MatchResponse create(UUID seasonId, CreateMatchRequest)` — validate both teams enrolled in season, home != away; auto-calculate predictionLockTime = scheduledAt - lockHours; update season.firstMatchAt if earlier
- `PagedResponse<MatchResponse> getBySeason(UUID seasonId, MatchStatus filter, Pageable)`
- `MatchResponse getById(UUID id)`
- `MatchResponse update(UUID id, UpdateMatchRequest)` — only SCHEDULED matches
- `void delete(UUID id)` — only SCHEDULED matches

### Step 7.10 — ResultService
- `MatchResultResponse publishMatchResult(PublishResultRequest)` — validate: season not UPCOMING/CLOSED, match not already COMPLETED, winner team in match (or isTie), toss team in match, POTM player belongs to match teams; save result; set match status=COMPLETED; update league standings (W-D-L, points: 2 win, 1 draw); trigger async score calculation + leaderboard recalc (via TransactionSynchronization.afterCommit); notify admins
- `MatchResultResponse getMatchResult(UUID matchId)`
- `void publishSeasonResult(UUID seasonId, PublishFinalStandingsRequest)` — save/replace season results; update SeasonTeam positions; calculate season prediction scores; trigger leaderboard recalc; transition season to COMPLETED
- **calculateMatchScores(UUID matchId):** For each user prediction: compare winner (handle ties), toss, POTM → 1 point each; save MatchScoreDetail
- **calculateSeasonScores(UUID seasonId):** For each user's league predictions: compare predicted vs actual position → 1 point if match; save SeasonScoreDetail

### Step 7.11 — PredictionService
- `MatchPredictionResponse submitMatchPrediction(UUID matchId, SubmitMatchPredictionRequest)` — validate: match SCHEDULED + before lockTime; winner and toss must be one of match teams; POTM must belong to match teams; upsert (create or update)
- `MatchPredictionResponse getMyMatchPrediction(UUID matchId)` — current user's prediction
- `PagedResponse<MatchPredictionResponse> getAllMatchPredictions(UUID matchId, Pageable)` — only visible after lock time (or admin)
- `HeadToHeadResponse getHeadToHead(UUID matchId, UUID opponentId)` — only after lock
- `List<LeaguePredictionResponse> submitLeaguePredictions(UUID seasonId, SubmitLeaguePredictionRequest)` — validate: window open (before predictionLockedAt); must include ALL enrolled teams; positions 1..N unique; delete old + save new
- `List<LeaguePredictionResponse> getMyLeaguePredictions(UUID seasonId)`
- `List<LeaguePredictionResponse> getAllLeaguePredictions(UUID seasonId)` — only after lock

### Step 7.12 — LeaderboardService
- `PagedResponse<LeaderboardEntryResponse> getLeaderboard(UUID seasonId, Pageable)` — from UserSeasonScore, ordered by totalPoints DESC, then lastCalculatedAt ASC, then displayName
- `MyRankResponse getMyRank(UUID seasonId)` — current user's rank + score breakdown (match details + season details)
- `void recalculateLeaderboard(UUID seasonId)` — `@Async("scoreExecutor")`; for all active users: sum match points + season points; compute total; rank; persist UserSeasonScore; send admin email with top-10 summary

### Step 7.13 — StandingService
- `List<LeagueStandingResponse> getStandings(UUID seasonId)` — ordered by currentPosition
- `void initializeStandings(UUID seasonId)` — create entries for all enrolled teams
- `void updateStandingsAfterResult(MatchResult result)` — increment W/D/L, recalculate positions by points

### Step 7.14 — EmailNotificationService
- `void queueEmail(String toEmail, UUID userId, String subject, String body, NotificationEventType eventType, UUID referenceId)` — `@Async("emailExecutor")`; create with PENDING status
- `void processPendingEmails()` — find PENDING with retryCount < maxRetry; attempt SMTP send; on success: SENT + sentAt; on failure: increment retry, store error, FAILED if maxRetry reached
- `void notifyAdmins(String subject, String body, NotificationEventType eventType)` — queue to all active admins
- `void sendBulkNotification(BulkNotificationRequest)` — filter valid users, queue individual emails
- `PagedResponse<EmailNotificationResponse> getNotificationLogs(Pageable)` — admin
- `PagedResponse<EmailNotificationResponse> getMyNotifications(Pageable)` — current user
- **Simulated mode:** When `spring.mail.username` is blank, log instead of SMTP send

---

## Phase 8: Controllers

All return `ApiResponse<T>` envelope. Use `@Tag` for Swagger grouping.

### Step 8.1 — AuthController (`/auth`)
| Method | Path | Access | Body | Response |
|---|---|---|---|---|
| POST | `/register` | Public | RegisterRequest | AuthResponse (201) |
| POST | `/login` | Public | LoginRequest | AuthResponse |
| POST | `/admin` | ADMIN | RegisterRequest | AuthResponse (201) |
| POST | `/refresh` | Auth | - | AuthResponse |
| GET | `/me` | Auth | - | UserResponse |

### Step 8.2 — UserController (`/users`)
| Method | Path | Access | Params | Response |
|---|---|---|---|---|
| GET | `/` | ADMIN | q, page, size, sort | PagedResponse\<UserResponse\> |
| GET | `/{id}` | Auth | - | UserResponse |
| PUT | `/{id}/profile` | Owner/Admin | UpdateProfileRequest | UserResponse |
| DELETE | `/{id}` | ADMIN | - | Void |

### Step 8.3 — LeagueController (`/leagues`)
| Method | Path | Access | Params/Body | Response |
|---|---|---|---|---|
| POST | `/` | ADMIN | CreateLeagueRequest | LeagueResponse (201) |
| GET | `/` | Auth | q, page, size, sort | PagedResponse\<LeagueResponse\> |
| GET | `/{id}` | Auth | - | LeagueResponse |
| PUT | `/{id}` | ADMIN | UpdateLeagueRequest | LeagueResponse |
| DELETE | `/{id}` | ADMIN | - | Void |

### Step 8.4 — SeasonController (`/seasons`)
| Method | Path | Access | Params/Body | Response |
|---|---|---|---|---|
| POST | `/` | ADMIN | CreateSeasonRequest | SeasonResponse (201) |
| GET | `/league/{leagueId}` | Auth | page, size, sort | PagedResponse\<SeasonResponse\> |
| GET | `/{id}` | Auth | - | SeasonResponse |
| POST | `/{id}/open` | ADMIN | - | SeasonResponse |
| POST | `/{id}/close` | ADMIN | - | SeasonResponse |
| POST | `/{id}/teams` | ADMIN | EnrollTeamRequest | Void (201) |
| DELETE | `/{id}/teams/{teamId}` | ADMIN | - | Void |
| GET | `/{id}/teams` | Auth | - | List\<TeamResponse\> |
| GET | `/{id}/standings` | Auth | - | List\<LeagueStandingResponse\> |
| POST | `/{id}/publish-result` | ADMIN | PublishFinalStandingsRequest | SeasonResponse |

### Step 8.5 — TeamController (`/teams`)
| Method | Path | Access | Params/Body | Response |
|---|---|---|---|---|
| POST | `/` | ADMIN | CreateTeamRequest | TeamResponse (201) |
| GET | `/` | Auth | q, page, size, sort | PagedResponse\<TeamResponse\> |
| GET | `/{id}` | Auth | - | TeamResponse |
| PUT | `/{id}` | ADMIN | UpdateTeamRequest | TeamResponse |
| DELETE | `/{id}` | ADMIN | - | Void |

### Step 8.6 — PlayerController (`/players`)
| Method | Path | Access | Params/Body | Response |
|---|---|---|---|---|
| POST | `/` | ADMIN | CreatePlayerRequest | PlayerResponse (201) |
| GET | `/{id}` | Auth | - | PlayerResponse |
| GET | `/team/{teamId}` | Auth | page, size | PagedResponse\<PlayerResponse\> |
| PUT | `/{id}` | ADMIN | UpdatePlayerRequest | PlayerResponse |
| DELETE | `/{id}` | ADMIN | - | Void |

### Step 8.7 — MatchController (`/matches`)
| Method | Path | Access | Params/Body | Response |
|---|---|---|---|---|
| POST | `/` | ADMIN | CreateMatchRequest | MatchResponse (201) |
| GET | `/season/{seasonId}` | Auth | status, page, size, sort | PagedResponse\<MatchResponse\> |
| GET | `/{id}` | Auth | - | MatchResponse |
| PUT | `/{id}` | ADMIN | UpdateMatchRequest | MatchResponse |
| DELETE | `/{id}` | ADMIN | - | Void |

### Step 8.8 — ResultController (`/results`)
| Method | Path | Access | Body | Response |
|---|---|---|---|---|
| POST | `/matches` | ADMIN | PublishResultRequest | MatchResultResponse |
| GET | `/matches/{matchId}` | Auth | - | MatchResultResponse |
| POST | `/seasons` | ADMIN | PublishFinalStandingsRequest | SeasonResponse |
| GET | `/seasons/{seasonId}` | Auth | - | List\<LeagueStandingResponse\> |

### Step 8.9 — PredictionController (`/predictions`)
| Method | Path | Access | Params/Body | Response |
|---|---|---|---|---|
| POST | `/matches/{matchId}` | Auth | SubmitMatchPredictionRequest | MatchPredictionResponse |
| GET | `/matches/{matchId}/me` | Auth | - | MatchPredictionResponse |
| GET | `/matches/{matchId}` | Auth | page, size | PagedResponse\<MatchPredictionResponse\> (after lock) |
| GET | `/matches/{matchId}/head-to-head` | Auth | opponentId | HeadToHeadResponse (after lock) |
| POST | `/seasons/{seasonId}/league` | Auth | SubmitLeaguePredictionRequest | List\<LeaguePredictionResponse\> |
| GET | `/seasons/{seasonId}/league/me` | Auth | - | List\<LeaguePredictionResponse\> |
| GET | `/seasons/{seasonId}/league` | Auth | - | List\<LeaguePredictionResponse\> (after lock) |

### Step 8.10 — LeaderboardController (`/leaderboard`)
| Method | Path | Access | Params | Response |
|---|---|---|---|---|
| GET | `/seasons/{seasonId}` | Auth | page, size | PagedResponse\<LeaderboardEntryResponse\> |
| GET | `/seasons/{seasonId}/me` | Auth | - | MyRankResponse |
| POST | `/seasons/{seasonId}/recalculate` | ADMIN | - | Void |

### Step 8.11 — NotificationController (`/notifications`)
| Method | Path | Access | Params/Body | Response |
|---|---|---|---|---|
| GET | `/` | ADMIN | page, size, eventType, status | PagedResponse\<EmailNotificationResponse\> |
| GET | `/me` | Auth | page, size | PagedResponse\<EmailNotificationResponse\> |
| POST | `/bulk` | ADMIN | BulkNotificationRequest | Void |

### Step 8.12 — AppConfigController (`/config`)
| Method | Path | Access | Body | Response |
|---|---|---|---|---|
| GET | `/` | ADMIN | - | List\<AppConfigResponse\> |
| PUT | `/{key}` | ADMIN | UpdateAppConfigRequest | AppConfigResponse |

---

## Phase 9: Scheduled Tasks

### Step 9.1 — PredictionLockScheduler
Three `@Scheduled` methods (cron from config):
1. **lockMatchPredictions**: Find SCHEDULED matches where lockTime <= now; set all their predictions to locked=true
2. **lockSeasonPredictions**: Find PREDICTION_OPEN seasons where predictionLockedAt <= now; transition to PREDICTION_LOCKED; lock all league predictions
3. **sendPredictionReminders**: Find matches closing within reminder window; find users who have NOT submitted predictions; queue reminder emails (idempotent via emailNotification check)

### Step 9.2 — SeasonLifecycleScheduler
1. **alertPendingResults** (every 5 min): Find COMPLETED matches without published results exceeding alert threshold; notify admins

### Step 9.3 — NotificationScheduler
1. **processEmails** (every 5 min): Call `emailNotificationService.processPendingEmails()`

---

## Phase 10: Testing

### Step 10.1 — Test Infrastructure

**File: `src/test/resources/application-test.yml`**
- Testcontainers PostgreSQL
- JWT test secret
- Flyway enabled (migrations run against test DB)
- Email disabled (simulated mode)

**File: `e2e/BaseE2ETest.java`**
```java
@SpringBootTest(webEnvironment = RANDOM_PORT)
@Testcontainers
@ActiveProfiles("test")
abstract class BaseE2ETest {
    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15");

    @Autowired TestRestTemplate restTemplate;

    // Helper methods:
    String registerAndGetToken(String email, String password)
    String loginAndGetToken(String email, String password)
    HttpHeaders authHeaders(String token)
    String getAdminToken()
}
```

### Step 10.2 — Unit Tests (Service Layer, ~80+ tests)

Test each service method with Mockito mocking dependencies.

**AuthServiceTest** (~6 tests): register success, duplicate email, duplicate username, login success, login bad credentials, login inactive user

**UserServiceTest** (~5 tests): get all paginated, get by ID, update profile, delete soft-delete, not found

**LeagueServiceTest** (~5 tests): CRUD operations, duplicate name, search

**SeasonServiceTest** (~8 tests): create, open (success + failures: not UPCOMING, < 2 teams, no matches, no firstMatchAt), close success, close not COMPLETED, add/remove team

**TeamServiceTest** (~4 tests): CRUD, duplicate name

**PlayerServiceTest** (~4 tests): CRUD, transfer team

**MatchServiceTest** (~6 tests): create (success, same team, team not in season), update only SCHEDULED, delete only SCHEDULED, lock time calculation

**ResultServiceTest** (~6 tests): publish match result (success, already completed, tie handling), publish season result, standings update, score calculation trigger

**PredictionServiceTest** (~10 tests): submit match prediction (success, locked, invalid team, invalid player), submit league prediction (success, locked, missing teams, duplicate positions, position out of range), visibility rules (before/after lock), head-to-head

**ScoreCalculationServiceTest** (~5 tests): match scoring (all correct, partial, tie), season scoring, leaderboard ranking

**LeaderboardServiceTest** (~4 tests): get leaderboard paginated, my rank, recalculate, tiebreaker

**EmailNotificationServiceTest** (~5 tests): queue email, process pending, retry on failure, max retry exceeded, simulated mode

### Step 10.3 — Controller Tests (MockMvc, ~30+ tests)

Test HTTP layer: status codes, request validation, auth enforcement, response structure.

**AuthControllerTest**: register 201, register validation 400, login 200, login bad credentials 401
**LeagueControllerTest**: unauthenticated 401, list 200, get 200, not found 404, create 201 admin, create 403 user, validation 400
**MatchControllerTest**: CRUD endpoints, auth checks
**PredictionControllerTest**: submit, get my, visibility rules
**LeaderboardControllerTest**: get leaderboard, my rank

### Step 10.4 — Integration Tests (Testcontainers, ~10 tests)

Test service + repository + database together (real PostgreSQL).

**AuthIntegrationTest**: Full register → login → access protected endpoint
**PredictionIntegrationTest**: Submit prediction → verify DB state → test lock enforcement → verify DB trigger blocks late submission

### Step 10.5 — E2E Tests (Full Stack, ~15 tests)

Test complete workflows via HTTP with real database.

**AuthE2ETest**: Register → login → get profile → update profile
**LeagueSeasonE2ETest**: Create league → create season → enroll teams → open season
**MatchPredictionE2ETest**: Create match → submit prediction → verify lock → verify visibility
**ScoringLeaderboardE2ETest**: Publish result → verify scores → verify leaderboard
**FullWorkflowE2ETest**: Complete happy path from registration through season close:
1. Admin creates league, season, teams, players, matches
2. Users register and submit predictions
3. Prediction window closes
4. Users can see each other's predictions
5. Admin publishes match results
6. Scores calculated, leaderboard updated
7. Admin publishes final standings
8. Season scores calculated
9. Final leaderboard verified
10. Admin closes season
11. Verify no modifications possible on closed season

---

## Phase 11: Documentation

### Step 11.1 — `docs/data-model.md`
Comprehensive ERD description with all tables, columns, types, relationships, constraints, and indexes.

### Step 11.2 — `docs/er-diagram.mmd`
Mermaid ER diagram showing all entity relationships.

### Step 11.3 — `docs/decision-log.md`
| # | Decision | Justification |
|---|---|---|
| DL-001 | PostgreSQL over MySQL | Better UUID support, CHECK constraints, trigger functions, TIMESTAMPTZ |
| DL-002 | Feature-based package structure | Better modularity, easier to navigate, each feature is self-contained |
| DL-003 | Soft delete for all entities | Requirement: no permanent deletes. Only app_config uses no soft-delete (simple key-value, no audit needed) |
| DL-004 | DB triggers for prediction lock | Requirement: "enforced at database level" — defense in depth |
| DL-005 | JWT without revocation | Acceptable for family-scale app; token expiry provides sufficient security |
| DL-006 | Caffeine over Redis | Single-instance app, no need for distributed cache; simpler deployment |
| DL-007 | Testcontainers for integration tests | Real PostgreSQL in tests catches DB-specific issues (triggers, constraints) |
| DL-008 | 3 separate async pools | Isolate scoring, email, and general tasks to prevent resource contention |
| DL-009 | Season result hard-delete on re-publish | Justified: admin may need to correct standings; old scores become invalid |
| DL-010 | Simulated email mode | When SMTP unconfigured, log emails for development without external dependency |

### Step 11.4 — `docs/api-reference.md`
Full endpoint documentation (auto-generated from Swagger, with additional notes).

### Step 11.5 — `docs/prompt-summary.md`
Document AI tool used and prompts given.

### Step 11.6 — `README.md`
- Project overview
- Prerequisites (Java 21, PostgreSQL 15+, Maven)
- Setup instructions (clone, create DB, configure env vars, run)
- Links to all docs
- API access (Swagger URL)
- Test execution instructions

---

## Phase 12: HTTPS Setup

### Step 12.1 — Generate Self-Signed Certificate
```bash
keytool -genkeypair -alias familyleague -keyalg RSA -keysize 2048 \
  -storetype PKCS12 -keystore src/main/resources/keystore.p12 \
  -validity 365 -storepass changeit
```

### Step 12.2 — application-https.yml already configured in Phase 1

Run with: `--spring.profiles.active=https`

---

## Execution Order Summary

| Phase | What | Depends On |
|---|---|---|
| 1 | Project bootstrap, pom.xml, configs | Nothing |
| 2 | Common layer (BaseEntity, ApiResponse, exceptions, validation, async, cache, CORS, OpenAPI) | Phase 1 |
| 3 | Flyway migrations (all 14 SQL scripts) | Phase 1 |
| 4 | Domain entities (all 17 entities + 6 enums) | Phase 2, 3 |
| 5 | Repositories (15 interfaces) | Phase 4 |
| 6 | Security layer (JWT, filters, config) | Phase 4, 5 |
| 7 | Service interfaces + implementations (14 services) | Phase 5, 6 |
| 8 | Controllers (12 controllers) | Phase 7 |
| 9 | Scheduled tasks (3 schedulers) | Phase 7 |
| 10 | Testing (unit + integration + E2E) | Phase 8, 9 |
| 11 | Documentation | Phase 8 |
| 12 | HTTPS setup | Phase 1 |

---

## Verification Plan

1. **Build:** `mvn clean compile` — zero errors
2. **Database:** Start PostgreSQL, create `family_league` DB, run `mvn spring-boot:run` — Flyway runs all 14 migrations
3. **Swagger:** Open `http://localhost:8080/api/v1/swagger-ui.html` — all endpoints visible
4. **Auth flow:** Register → Login → Use JWT in subsequent requests
5. **Admin flow:** Create league → season → teams → players → matches → open season
6. **Prediction flow:** Submit match + league predictions → verify lock enforcement
7. **Result flow:** Publish match result → verify scores calculated → verify leaderboard updated
8. **Notification flow:** Verify emails queued in DB, processed by scheduler
9. **Unit tests:** `mvn test` — all pass
10. **Integration tests:** `mvn verify -P integration` — Testcontainers spins up PostgreSQL, all pass
11. **HTTPS:** Run with `--spring.profiles.active=https`, access via `https://localhost:8443`
