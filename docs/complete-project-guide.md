# Family League — Complete Project Guide (For Demo & Review)

> **Who is this for?** You — a Python/FastAPI developer who needs to explain this Java/Spring Boot project in a review call. Every concept is mapped to Python equivalents you already know.

---

## Table of Contents

1. [What This Project Does (Business Overview)](#1-what-this-project-does)
2. [Java/Spring Boot vs Python/FastAPI Cheat Sheet](#2-javaspring-boot-vs-pythonfastapi)
3. [High-Level Architecture](#3-high-level-architecture)
4. [Folder Structure Explained](#4-folder-structure-explained)
5. [Database Schema Design](#5-database-schema-design)
6. [Entity Relationship Diagram](#6-entity-relationship-diagram)
7. [All API Endpoints (With Examples)](#7-all-api-endpoints)
8. [Business Logic — Every Workflow](#8-business-logic--every-workflow)
9. [Security Design](#9-security-design)
10. [Scoring Engine Logic](#10-scoring-engine-logic)
11. [Email & Notification System](#11-email--notification-system)
12. [Scheduled Tasks (Cron Jobs)](#12-scheduled-tasks)
13. [Caching Strategy](#13-caching-strategy)
14. [Testing Strategy](#14-testing-strategy)
15. [Demo Script (Step-by-Step)](#15-demo-script)
16. [Common Questions & Answers](#16-common-questions--answers)

---

## 1. What This Project Does

### One-Line Summary
A backend API where family and friends predict cricket match outcomes and compete on a leaderboard.

### The Real-World Analogy
Imagine you're watching IPL with 10 friends. Before each match, everyone predicts:
- Who wins the match?
- Who wins the toss?
- Who will be Player of the Match?

After the match, whoever guessed correctly gets points. At the end of the season, whoever has the most points wins.

**This app automates all of that.**

### Key Personas

| Persona | What They Do |
|---|---|
| **Admin** | Creates leagues, teams, matches. Publishes results. Manages everything. |
| **User** | Registers, submits predictions, views leaderboard. Cannot modify data. |

### Core Features
1. **League Management** — Create leagues (e.g., "Indian Premier League") with seasons
2. **Team & Player Management** — 10 IPL teams, 250 players pre-loaded
3. **Match Scheduling** — Schedule matches with automatic prediction lock times
4. **Match Predictions** — Users predict winner, toss winner, and Player of the Match
5. **League Predictions** — Users predict final team standings (1st through 10th)
6. **Result Publishing** — Admin manually publishes results after real matches
7. **Scoring Engine** — Automatically calculates points (1 point per correct prediction)
8. **Leaderboard** — Ranks users by total points
9. **Email Notifications** — Reminders, score updates, admin alerts
10. **Prediction Lock** — Predictions auto-lock before match start (enforced at database level!)

---

## 2. Java/Spring Boot vs Python/FastAPI

If you know FastAPI, you already know 80% of the concepts. Here's the translation:

| Concept | Python/FastAPI | Java/Spring Boot (Our Project) |
|---|---|---|
| **Framework** | FastAPI | Spring Boot |
| **Language** | Python | Java 21 |
| **Package manager** | pip + requirements.txt | Maven + pom.xml |
| **Run command** | `uvicorn main:app` | `./mvnw spring-boot:run` |
| **API docs** | Auto Swagger at `/docs` | SpringDoc Swagger at `/swagger-ui/index.html` |
| **Route definition** | `@app.get("/users")` | `@GetMapping("/users")` |
| **Request body** | Pydantic `BaseModel` | Java `record` (our DTOs) |
| **ORM** | SQLAlchemy | JPA/Hibernate |
| **Models** | SQLAlchemy `class User(Base)` | `@Entity class User extends BaseEntity` |
| **Migrations** | Alembic | Flyway (SQL files in `db/migration/`) |
| **Dependency Injection** | `Depends()` | `@Autowired` / constructor injection |
| **Auth** | `OAuth2PasswordBearer` | Spring Security + JWT filter |
| **Background tasks** | `BackgroundTasks` | `@Async` with thread pools |
| **Cron jobs** | Celery Beat | `@Scheduled` |
| **Caching** | Redis / `@cache` | Caffeine (in-memory) |
| **Tests** | pytest | JUnit 5 + Mockito |
| **Environment vars** | `.env` + `python-dotenv` | `.env` + `${VAR:default}` in YAML |
| **Error handling** | `@app.exception_handler` | `@RestControllerAdvice` (GlobalExceptionHandler) |

### Key Java Concepts You'll Hear

| Term | What It Means |
|---|---|
| **Bean** | An object managed by Spring (like a singleton). When you see `@Service`, `@Repository`, `@Component` — those are beans. |
| **Interface + Impl** | We define a contract (interface) then implement it. Like Python's ABC but enforced. Example: `PredictionService` (interface) → `PredictionServiceImpl` (implementation) |
| **Record** | Java's version of Pydantic model. Immutable, auto-generates equals/hashCode/toString. Used for all our DTOs. |
| **Lombok** | A library that auto-generates getters/setters/constructors via annotations (`@Getter`, `@Setter`, `@Builder`). Reduces boilerplate on entities. |
| **JPA Entity** | A Java class mapped to a database table. Like SQLAlchemy model. |
| **Repository** | An interface that gives you DB operations. Spring auto-implements `findById()`, `save()`, `delete()` — you just declare the interface. |
| **`@Transactional`** | Like a database transaction context. If anything fails inside, everything rolls back. |

---

## 3. High-Level Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                        CLIENT                                │
│           (Swagger UI / Postman / curl / Frontend)           │
└─────────────────┬───────────────────────────────────────────┘
                  │ HTTP (JSON)
                  ▼
┌─────────────────────────────────────────────────────────────┐
│                   SPRING BOOT APPLICATION                     │
│                                                               │
│  ┌──────────┐   ┌──────────┐   ┌──────────┐   ┌──────────┐ │
│  │  JWT     │──▶│Controller│──▶│ Service  │──▶│Repository│ │
│  │  Filter  │   │ (Routes) │   │ (Logic)  │   │  (DB)    │ │
│  └──────────┘   └──────────┘   └──────────┘   └──────────┘ │
│                                      │                       │
│                                      ▼                       │
│                              ┌──────────────┐                │
│                              │  Schedulers  │                │
│                              │  (Cron Jobs) │                │
│                              └──────────────┘                │
│                                                               │
│  ┌─────────────────────────────────────────────────────────┐ │
│  │  Cross-Cutting: Security, Caching, Async, Exceptions    │ │
│  └─────────────────────────────────────────────────────────┘ │
└─────────────────┬───────────────────────────────────────────┘
                  │ JDBC
                  ▼
┌─────────────────────────────────────────────────────────────┐
│                     POSTGRESQL DATABASE                       │
│                                                               │
│  15 Tables + 2 Triggers + Indexes                            │
│  Flyway manages schema (14 migration scripts)                │
└─────────────────────────────────────────────────────────────┘
```

### Request Flow (What happens when a user calls an API)

```
1. HTTP Request arrives (e.g., POST /api/v1/predictions/matches/123)
       │
2. JWT Authentication Filter
   └── Extracts "Bearer <token>" from header
   └── Validates token signature + expiry
   └── Loads user from DB, sets SecurityContext
       │
3. Spring Security Authorization
   └── Checks: Is this endpoint public? Admin-only? Auth-required?
   └── Returns 401 (no token) or 403 (wrong role) if denied
       │
4. Controller (Route Handler)
   └── Validates request body (@Valid annotations)
   └── Calls the Service layer
       │
5. Service (Business Logic)
   └── Applies business rules (is prediction window open? is team valid?)
   └── Calls Repository to read/write database
   └── May trigger async tasks (email, scoring)
       │
6. Repository (Database Access)
   └── JPA translates Java objects to SQL
   └── PostgreSQL executes query
       │
7. Response flows back up
   └── Service returns DTO → Controller wraps in ApiResponse → JSON sent to client
```

---

## 4. Folder Structure Explained

```
family-league/
├── pom.xml                          # Like requirements.txt — lists all dependencies
├── .env.example                     # Template for environment variables
├── README.md                        # Project overview + setup instructions
├── family-league-requirements.md    # Original business requirements
│
├── docs/                            # Documentation
│   ├── implementation-plan.md       # How we planned the build
│   ├── decision-log.md              # Why we chose X over Y (14 decisions)
│   ├── data-model.md                # Database schema description
│   ├── er-diagram.mmd               # Visual entity-relationship diagram
│   ├── setup-and-testing-guide.md   # Detailed setup + 50 curl test commands
│   └── complete-project-guide.md    # THIS FILE — complete project explanation
│
├── scripts/
│   └── smoke-test.sh                # Automated 59-test bash script
│
└── src/
    ├── main/java/com/familyleague/  # APPLICATION CODE (145 files)
    │   │
    │   ├── FamilyLeagueApplication.java   # Entry point (like main.py)
    │   │
    │   ├── common/                  # Shared code used by all features
    │   │   ├── entity/BaseEntity.java     # Base class for all DB models
    │   │   ├── dto/ApiResponse.java       # Standard JSON response wrapper
    │   │   ├── dto/PagedResponse.java     # Paginated response wrapper
    │   │   ├── config/                    # App configuration classes
    │   │   ├── exception/                 # Error handling (GlobalExceptionHandler)
    │   │   └── validation/                # Custom password validator
    │   │
    │   ├── auth/                    # Authentication feature
    │   │   ├── controller/          # API endpoints (/auth/login, /auth/register)
    │   │   ├── dto/                 # Request/response models
    │   │   ├── service/             # Business logic (register, login, JWT)
    │   │   └── security/            # JWT filter, security config
    │   │
    │   ├── user/                    # User management feature
    │   ├── league/                  # League + Season feature
    │   ├── team/                    # Team + Season enrollment feature
    │   ├── player/                  # Player management feature
    │   ├── match/                   # Match + Result feature
    │   ├── prediction/              # Match + League predictions feature
    │   ├── scoring/                 # Score calculation + Leaderboard feature
    │   ├── standing/                # League standings (W-D-L) feature
    │   ├── notification/            # Email notification feature
    │   ├── appconfig/               # Runtime configuration feature
    │   └── scheduler/               # Scheduled background tasks
    │
    ├── main/resources/              # Configuration + SQL
    │   ├── application.yml          # Main config (like .env but structured)
    │   ├── application-dev.yml      # Dev-specific overrides
    │   ├── application-prod.yml     # Production overrides
    │   ├── application-https.yml    # HTTPS/SSL config
    │   ├── logback-spring.xml       # Logging config (console + file)
    │   └── db/migration/            # 14 Flyway SQL migration files
    │       ├── V1__create_users.sql
    │       ├── ...
    │       └── V14__prediction_lock_triggers.sql
    │
    └── test/java/com/familyleague/  # TEST CODE (13 files)
        ├── auth/service/AuthServiceTest.java
        ├── league/service/SeasonServiceTest.java
        ├── match/service/MatchServiceTest.java
        ├── match/service/ResultServiceTest.java
        ├── prediction/service/PredictionServiceTest.java
        ├── scoring/service/ScoreCalculationServiceTest.java
        ├── user/service/UserServiceTest.java
        ├── league/service/LeagueServiceTest.java
        ├── notification/service/EmailNotificationServiceTest.java
        └── e2e/                     # End-to-end tests
            ├── BaseE2ETest.java
            ├── AuthE2ETest.java
            └── FullWorkflowE2ETest.java
```

### Each Feature Follows the Same Pattern

```
feature/
├── entity/          # Database model (like SQLAlchemy model)
├── dto/             # Request/Response shapes (like Pydantic model)
├── repository/      # Database queries (auto-generated by Spring)
├── service/         # Business logic (interface + implementation)
├── controller/      # API routes (like FastAPI @app.get)
└── exception/       # Feature-specific errors
```

This is called **"vertical slice architecture"** — each feature is self-contained.

---

## 5. Database Schema Design

### 15 Tables Overview

```
CORE DOMAIN:
  users              — Platform users (admin + regular)
  leagues            — Sports leagues (e.g., IPL)
  seasons            — Season within a league (e.g., IPL 2024)
  teams              — Sports teams (e.g., CSK, MI)
  season_teams       — Which teams are in which season (join table)
  players            — Players belonging to teams
  matches            — Scheduled matches between two teams
  match_results      — Published results for completed matches

PREDICTIONS:
  match_predictions  — User's match-level predictions
  league_predictions — User's season-level team position predictions

SCORING:
  match_score_details    — Per-user per-match score breakdown
  season_score_details   — Per-user season prediction scores
  user_season_scores     — Aggregated leaderboard (total points + rank)

STANDINGS:
  league_standings   — Team W-D-L records within a season

SUPPORTING:
  email_notifications — Email log with retry support
  app_config          — Runtime-tunable key-value configuration
```

### Every Table Has These Audit Columns (except app_config)

| Column | Type | Purpose |
|---|---|---|
| `id` | UUID | Primary key (auto-generated) |
| `created_at` | TIMESTAMPTZ | When record was created (auto-filled) |
| `created_by` | UUID | Who created it (auto-filled from JWT) |
| `updated_at` | TIMESTAMPTZ | When last modified |
| `updated_by` | UUID | Who modified it |
| `is_deleted` | BOOLEAN | Soft delete flag (default: false) |
| `deleted_at` | TIMESTAMPTZ | When soft-deleted |
| `deleted_by` | UUID | Who deleted it |

**No record is ever permanently deleted.** We set `is_deleted = true` instead. All queries automatically filter out deleted records via `@SQLRestriction("is_deleted = false")`.

### Key Relationships

```
League  ──1:N──  Season  ──1:N──  Match
                    │                 │
                    │              belongs to
                    │                 │
                 1:N(via            home_team ──▶ Team ──1:N──▶ Player
                 season_teams)     away_team ──▶ Team
                    │
                    │
                    ▼
              SeasonTeam (join)
```

---

## 6. Entity Relationship Diagram

```
USERS ─────────< MATCH_PREDICTIONS >───── MATCHES
  │                                          │
  │                                       belongs to
  │                                          │
  ├──────────< LEAGUE_PREDICTIONS >──── SEASONS ────── LEAGUES
  │                                       │
  ├──< MATCH_SCORE_DETAILS               1:N
  ├──< SEASON_SCORE_DETAILS          SEASON_TEAMS >── TEAMS ──< PLAYERS
  ├──< USER_SEASON_SCORES
  └──< EMAIL_NOTIFICATIONS

MATCHES ──1:1── MATCH_RESULTS
SEASONS ──1:N── LEAGUE_STANDINGS ──N:1── TEAMS

APP_CONFIG (standalone, no relationships)
```

---

## 7. All API Endpoints

### Authentication (`/auth`)

| # | Method | Endpoint | Access | What It Does | Example |
|---|---|---|---|---|---|
| 1 | POST | `/auth/register` | Public | Create new user account | `{"username":"alice", "email":"alice@test.com", "password":"Alice@123", "displayName":"Alice"}` |
| 2 | POST | `/auth/login` | Public | Login with email OR username | `{"usernameOrEmail":"alice", "password":"Alice@123"}` |
| 3 | POST | `/auth/admin` | Admin | Create admin account | Same as register |
| 4 | POST | `/auth/refresh` | Auth | Get fresh JWT token | (no body, uses existing token) |
| 5 | GET | `/auth/me` | Auth | Get current user's profile | Returns: username, email, role |

**Response example (login):**
```json
{
  "success": true,
  "message": "Login successful",
  "data": {
    "accessToken": "eyJhbGciOiJIUzUxMiJ9...",
    "tokenType": "Bearer",
    "userId": "a0000000-...",
    "username": "admin",
    "email": "admin@familyleague.com",
    "role": "ADMIN"
  }
}
```

### Users (`/users`)

| # | Method | Endpoint | Access | What It Does |
|---|---|---|---|---|
| 6 | GET | `/users` | Admin | List all users (paginated, searchable via `?q=`) |
| 7 | GET | `/users/{id}` | Auth | Get user by ID |
| 8 | PUT | `/users/{id}/profile` | Owner/Admin | Update display name + avatar |
| 9 | DELETE | `/users/{id}` | Admin | Soft-delete (deactivate) user |

### Leagues (`/leagues`)

| # | Method | Endpoint | Access | What It Does |
|---|---|---|---|---|
| 10 | POST | `/leagues` | Admin | Create a league |
| 11 | GET | `/leagues` | Auth | List all leagues (search: `?q=cricket`) |
| 12 | GET | `/leagues/{id}` | Auth | Get league details |
| 13 | PUT | `/leagues/{id}` | Admin | Update league |
| 14 | DELETE | `/leagues/{id}` | Admin | Soft-delete league |

### Seasons (`/seasons`)

| # | Method | Endpoint | Access | What It Does |
|---|---|---|---|---|
| 15 | POST | `/seasons` | Admin | Create a season |
| 16 | GET | `/seasons/league/{leagueId}` | Auth | List seasons for a league |
| 17 | GET | `/seasons/{id}` | Auth | Get season details |
| 18 | POST | `/seasons/{id}/open` | Admin | Open season for predictions (UPCOMING → PREDICTION_OPEN) |
| 19 | POST | `/seasons/{id}/close` | Admin | Close season (COMPLETED → CLOSED) |
| 20 | POST | `/seasons/{id}/teams` | Admin | Enroll a team in the season |
| 21 | DELETE | `/seasons/{id}/teams/{teamId}` | Admin | Remove team from season |
| 22 | GET | `/seasons/{id}/teams` | Auth | List enrolled teams |
| 23 | GET | `/seasons/{id}/standings` | Auth | Get team W-D-L standings |
| 24 | POST | `/seasons/{id}/publish-result` | Admin | Publish final team positions → COMPLETED |

### Teams (`/teams`)

| # | Method | Endpoint | Access | What It Does |
|---|---|---|---|---|
| 25 | POST | `/teams` | Admin | Create a team |
| 26 | GET | `/teams` | Auth | List all teams (search: `?q=mumbai`) |
| 27 | GET | `/teams/{id}` | Auth | Get team details |
| 28 | PUT | `/teams/{id}` | Admin | Update team |
| 29 | DELETE | `/teams/{id}` | Admin | Soft-delete team |

### Players (`/players`)

| # | Method | Endpoint | Access | What It Does |
|---|---|---|---|---|
| 30 | POST | `/players` | Admin | Create a player |
| 31 | GET | `/players/{id}` | Auth | Get player details |
| 32 | GET | `/players/team/{teamId}` | Auth | List players for a team |
| 33 | PUT | `/players/{id}` | Admin | Update player (can transfer teams) |
| 34 | DELETE | `/players/{id}` | Admin | Soft-delete player |

### Matches (`/matches`)

| # | Method | Endpoint | Access | What It Does |
|---|---|---|---|---|
| 35 | POST | `/matches` | Admin | Schedule a match |
| 36 | GET | `/matches/season/{seasonId}` | Auth | List matches (filter: `?status=SCHEDULED`) |
| 37 | GET | `/matches/{id}` | Auth | Get match details |
| 38 | PUT | `/matches/{id}` | Admin | Update schedule (SCHEDULED only) |
| 39 | DELETE | `/matches/{id}` | Admin | Soft-delete (SCHEDULED only) |

### Results (`/results`)

| # | Method | Endpoint | Access | What It Does |
|---|---|---|---|---|
| 40 | POST | `/results/matches` | Admin | Publish match result → triggers scoring |
| 41 | GET | `/results/matches/{matchId}` | Auth | Get match result |

### Predictions (`/predictions`)

| # | Method | Endpoint | Access | What It Does |
|---|---|---|---|---|
| 42 | POST | `/predictions/matches/{matchId}` | Auth | Submit/update match prediction |
| 43 | GET | `/predictions/matches/{matchId}/me` | Auth | Get my prediction |
| 44 | GET | `/predictions/matches/{matchId}` | Auth | All predictions (visible ONLY after lock) |
| 45 | GET | `/predictions/matches/{matchId}/head-to-head?opponentId=` | Auth | Compare with another user (after lock) |
| 46 | POST | `/predictions/seasons/{seasonId}/league` | Auth | Submit league predictions (all teams ranked) |
| 47 | GET | `/predictions/seasons/{seasonId}/league/me` | Auth | My league predictions |
| 48 | GET | `/predictions/seasons/{seasonId}/league` | Auth | All league predictions (after lock) |

### Leaderboard (`/leaderboard`)

| # | Method | Endpoint | Access | What It Does |
|---|---|---|---|---|
| 49 | GET | `/leaderboard/seasons/{seasonId}` | Auth | View leaderboard (rank, points) |
| 50 | GET | `/leaderboard/seasons/{seasonId}/me` | Auth | My rank + score breakdown |
| 51 | POST | `/leaderboard/seasons/{seasonId}/recalculate` | Admin | Force leaderboard recalculation |

### Notifications (`/notifications`)

| # | Method | Endpoint | Access | What It Does |
|---|---|---|---|---|
| 52 | GET | `/notifications` | Admin | View all email logs |
| 53 | GET | `/notifications/me` | Auth | View my notification history |
| 54 | POST | `/notifications/bulk` | Admin | Send custom email to selected users |

### App Config (`/config`)

| # | Method | Endpoint | Access | What It Does |
|---|---|---|---|---|
| 55 | GET | `/config` | Admin | View all runtime config values |
| 56 | PUT | `/config/{key}` | Admin | Update a config value |

---

## 8. Business Logic — Every Workflow

### Workflow 1: Season Lifecycle

This is the most important workflow. A season goes through these states:

```
UPCOMING ──▶ PREDICTION_OPEN ──▶ PREDICTION_LOCKED ──▶ IN_PROGRESS ──▶ COMPLETED ──▶ CLOSED
   │              │                    │                                   │              │
   │         Users submit          Scheduler auto-               Admin publishes    Admin closes
   │         predictions           locks when time               final standings    (no more
   │                               passes                                           changes)
Admin opens                                                  
(needs ≥2                                                    
teams + ≥1 match)                                           
```

**Step by step:**
1. Admin creates a season (status = `UPCOMING`)
2. Admin enrolls teams and schedules matches
3. Admin opens the season → status becomes `PREDICTION_OPEN`
   - System calculates: `predictionLockedAt = firstMatchAt - 4 hours`
4. Users submit league predictions (rank all teams 1 to N)
5. Scheduler auto-locks → status becomes `PREDICTION_LOCKED` when time passes
6. Matches are played, admin publishes results one by one
7. Admin publishes final team standings → status becomes `COMPLETED`
8. Admin closes the season → status becomes `CLOSED` (read-only forever)

### Workflow 2: Match Prediction Flow

```
Match Created (SCHEDULED)
    │
    ▼
Prediction Window OPEN ◄── Users submit predictions
    │                       (can update until lock)
    │
    ▼  (1 hour before match)
Prediction Window LOCKED ──▶ Users can NOW see each other's predictions
    │                         Head-to-head comparison available
    │
    ▼
Match Played (real world)
    │
    ▼
Admin Publishes Result ──▶ Scoring engine runs automatically
    │                       │
    │                       ├── Each user gets: 0-3 points
    │                       ├── Leaderboard recalculated
    │                       └── Email notifications sent
    ▼
Match COMPLETED
```

**Prediction rules:**
- Winner must be one of the two playing teams
- Toss winner must be one of the two playing teams
- Player of the Match must belong to one of the two playing teams
- Cannot predict after lock time (enforced in code AND in database triggers)

### Workflow 3: Result Publishing & Scoring

When admin publishes a result, this chain happens:

```
Admin calls: POST /results/matches
    │
    ▼
1. VALIDATION
   ├── Is match not already completed? (409 if yes)
   ├── Is season not UPCOMING or CLOSED? (400 if yes)
   ├── Is winner one of the match teams? (400 if not)
   ├── Is toss winner one of the match teams? (400 if not)
   └── Is POTM player from one of the teams? (400 if not)
    │
    ▼
2. SAVE RESULT
   ├── Create MatchResult record
   └── Set match status = COMPLETED
    │
    ▼
3. UPDATE STANDINGS
   ├── Winner gets: wins+1, points+2
   ├── Loser gets: losses+1
   ├── Tie: both get draws+1, points+1
   └── Recalculate position rankings
    │
    ▼
4. ASYNC (after DB transaction commits)
   ├── SCORE CALCULATION (for each user who predicted):
   │   ├── Winner correct? → +1 point
   │   ├── Toss correct? → +1 point
   │   ├── POTM correct? → +1 point
   │   └── Save MatchScoreDetail (max 3 points per match)
   │
   ├── LEADERBOARD RECALCULATION:
   │   ├── Sum all match points per user
   │   ├── Sum all season prediction points per user
   │   ├── Rank by total (descending)
   │   └── Save UserSeasonScore with rank
   │
   └── EMAIL NOTIFICATIONS:
       ├── Each predictor gets: "You scored X points!"
       └── Admins get: "Leaderboard updated - Top 10: ..."
```

### Workflow 4: League Prediction Flow

```
Season opened (PREDICTION_OPEN)
    │
    ▼
User submits league prediction:
{
  "predictions": [
    {"teamId": "csk-id", "predictedPosition": 1},
    {"teamId": "mi-id",  "predictedPosition": 2},
    {"teamId": "rcb-id", "predictedPosition": 3},
    ... (must include ALL enrolled teams)
    ... (each position must be unique, 1 to N)
  ]
}
    │
    ▼
VALIDATION:
├── Is prediction window still open? (403 if not)
├── Are ALL enrolled teams included? (400 if not)
├── Are positions unique? (400 if duplicates)
├── Are positions in range 1..N? (400 if out of range)
└── Old predictions deleted, new ones saved (replace pattern)
    │
    ▼
At season end, admin publishes final standings
    │
    ▼
SEASON SCORE CALCULATION:
For each team the user predicted:
  If predictedPosition == actualPosition → +1 point
  Else → 0 points

Example: User predicted CSK at position 1, CSK actually finished 1st → +1 point
         User predicted MI at position 2, MI actually finished 5th → 0 points
```

---

## 9. Security Design

### Authentication Flow

```
                    Register/Login
                         │
                         ▼
              ┌─────────────────────┐
              │  AuthService        │
              │  - Validates creds  │
              │  - BCrypt password  │
              │  - Generates JWT    │
              └────────┬────────────┘
                       │
                       ▼
              ┌─────────────────────┐
              │  JWT Token          │
              │  Contains:          │
              │  - userId (subject) │
              │  - username         │
              │  - role (ADMIN/USER)│
              │  - expiry (24h)     │
              │  Signed: HMAC-SHA512│
              └─────────────────────┘
                       │
          User sends token in header:
          Authorization: Bearer eyJhbG...
                       │
                       ▼
              ┌─────────────────────┐
              │  JwtAuthFilter      │
              │  (runs on EVERY     │
              │   request)          │
              │  1. Extract token   │
              │  2. Validate sig    │
              │  3. Load user from  │
              │     database        │
              │  4. Set security    │
              │     context         │
              └─────────────────────┘
```

### Role-Based Access Control (RBAC)

| Action | USER | ADMIN |
|---|---|---|
| Register / Login | Yes | Yes |
| View leagues, teams, matches | Yes | Yes |
| Submit predictions | Yes | Yes |
| View leaderboard | Yes | Yes |
| Create/update/delete leagues, teams, players, matches | No (403) | Yes |
| Publish results | No (403) | Yes |
| Open/close seasons | No (403) | Yes |
| View all users | No (403) | Yes |
| Send bulk notifications | No (403) | Yes |
| View/update app config | No (403) | Yes |

### Database-Level Prediction Lock (Defense in Depth)

Even if someone bypasses the application code, PostgreSQL triggers prevent late predictions:

```sql
-- Trigger on match_predictions table:
-- If current time > match.prediction_lock_time → RAISE EXCEPTION
-- If match status ≠ 'SCHEDULED' → RAISE EXCEPTION

-- Trigger on league_predictions table:
-- If season status IN ('PREDICTION_LOCKED', 'IN_PROGRESS', 'COMPLETED', 'CLOSED') → RAISE EXCEPTION
-- If current time > season.prediction_locked_at → RAISE EXCEPTION
```

This means: **even direct SQL INSERT to the predictions table will fail** if the window is closed.

---

## 10. Scoring Engine Logic

### Match Scoring (0-3 points per match)

```
For each user who predicted on a match:

1. Winner Prediction:
   - Normal match: predicted winner == actual winner? → +1 point
   - Tie match: ANY prediction counts as correct → +1 point
   
2. Toss Prediction:
   - predicted toss winner == actual toss winner? → +1 point
   
3. POTM Prediction:
   - predicted player == actual player of match? → +1 point

Maximum per match: 3 points
Stored in: match_score_details table
```

### Season Scoring (0-N points, N = number of teams)

```
For each team the user predicted a position for:
   predicted position == actual final position? → +1 point

Example with 10 teams:
  User predicted: CSK=1, MI=2, RCB=3, KKR=4, DC=5, GT=6, LSG=7, PBKS=8, RR=9, SRH=10
  Actual results: CSK=1, MI=5, RCB=3, KKR=2, DC=4, GT=6, LSG=8, PBKS=7, RR=9, SRH=10
  
  CSK: 1==1 ✓ (+1)    MI: 2≠5 ✗    RCB: 3==3 ✓ (+1)    KKR: 4≠2 ✗    DC: 5≠4 ✗
  GT: 6==6 ✓ (+1)     LSG: 7≠8 ✗   PBKS: 8≠7 ✗         RR: 9==9 ✓ (+1)  SRH: 10==10 ✓ (+1)
  
  Season score: 5 points (out of maximum 10)

Stored in: season_score_details table
```

### Leaderboard Ranking

```
total_points = match_points + season_prediction_points

Ranking order:
  1. Highest total_points first
  2. If tied: earlier lastCalculatedAt wins (first to reach the score)

Stored in: user_season_scores table (with rank column)
```

---

## 11. Email & Notification System

### How It Works

```
                    Any Service
                        │
                        │ calls queueEmail()
                        ▼
              ┌─────────────────────┐
              │  EmailNotification   │
              │  record created in   │
              │  DB with status      │
              │  = PENDING           │
              └──────────┬──────────┘
                         │
         Every 5 minutes (NotificationScheduler)
                         │
                         ▼
              ┌─────────────────────┐
              │  processPendingEmails│
              │                     │
              │  For each PENDING:  │
              │  ├── Try SMTP send  │
              │  │   ├── Success → SENT     │
              │  │   └── Failure → retry+1  │
              │  └── If retry ≥ 3 → FAILED  │
              └─────────────────────┘
```

### Email Types

| Event | Who Gets It | When |
|---|---|---|
| `WELCOME` | New user | On registration |
| `MATCH_PREDICTION_REMINDER` | Users who haven't predicted | 2 hours before lock |
| `RESULT_PUBLISHED` | Users who predicted that match | After admin publishes result |
| `LEADERBOARD_UPDATED` | Admins | After leaderboard recalculation |
| `PENDING_RESULT_ALERT` | Admins | If match result not published within 2 hours |
| `BULK_NOTIFICATION` | Selected users | Admin sends custom message |

### Simulated Mode
When `MAIL_USERNAME` is empty (our default), emails are logged to console instead of actually sent. The `email_notifications` table still records everything.

---

## 12. Scheduled Tasks

Three background jobs run automatically:

| Task | Frequency | What It Does |
|---|---|---|
| **Lock Match Predictions** | Every minute | Finds SCHEDULED matches past lock time → sets all their predictions to `is_locked = true` |
| **Lock Season Predictions** | Every minute | Finds PREDICTION_OPEN seasons past lock time → transitions to PREDICTION_LOCKED, locks all league predictions |
| **Process Emails** | Every 5 minutes | Finds PENDING emails → attempts SMTP send → marks SENT or retries |
| **Pending Result Alerts** | Every 5 minutes | Finds completed matches without published results → emails admins |
| **Prediction Reminders** | Every hour | Finds matches closing soon → emails users who haven't predicted |

---

## 13. Caching Strategy

We use **Caffeine** (in-memory cache, like Python's `@functools.lru_cache` but more powerful):

| Cache | What's Cached | TTL | Why |
|---|---|---|---|
| `app-config` | Runtime config values | 5 min | Config rarely changes, avoid DB hit on every request |
| `leagues` | League list | 5 min | Leagues rarely change |
| `teams` | Team list | 5 min | Teams rarely change |
| `players` | Player data | 5 min | Players rarely change |

**Cache eviction:** When admin creates/updates/deletes a league/team/player, the entire cache for that type is cleared (`@CacheEvict`).

---

## 14. Testing Strategy

### Three Levels of Testing

```
┌─────────────────────────────────────────┐
│  Level 3: E2E Tests (6 tests)          │  Full app + real PostgreSQL
│  Tests complete HTTP workflows          │  (via Testcontainers Docker)
├─────────────────────────────────────────┤
│  Level 2: Unit Tests (54 tests)        │  Service logic only
│  Tests business rules with mock DB     │  (Mockito mocks)
├─────────────────────────────────────────┤
│  Level 1: Smoke Tests (59 tests)       │  curl against running app
│  Tests every API endpoint works        │  (bash script)
└─────────────────────────────────────────┘
```

### What Each Test File Covers

| Test File | # Tests | Key Scenarios |
|---|---|---|
| AuthServiceTest | 7 | Register, duplicate email (409), duplicate username (409), login success, bad password (401), inactive user (401), user not found (401) |
| SeasonServiceTest | 7 | Open season success, not UPCOMING (400), <2 teams (400), no matches (400), auto-calculate lock time, close success, close not COMPLETED (400) |
| MatchServiceTest | 7 | Create match, lock time = scheduledAt - 1hr, same team (400), team not enrolled (400), update only SCHEDULED, delete only SCHEDULED, updates firstMatchAt |
| ResultServiceTest | 7 | Publish success, tie result, already completed (409), season UPCOMING (400), season CLOSED (400), winner not in match (400), POTM not in match (400) |
| PredictionServiceTest | 7 | Submit match pred, locked (403), invalid team (400), league pred locked (403), wrong team count (400), duplicate position (400), visibility before lock (403) |
| ScoreCalculationServiceTest | 3 | All correct = 3pts, partial = 1pt, tie = winner counts |
| UserServiceTest | 6 | Get by ID, search, update own profile, non-owner blocked (403), soft delete |
| LeagueServiceTest | 4 | Create, duplicate name (409), not found (404), soft delete |
| EmailNotificationServiceTest | 6 | Queue email, simulated SENT, real SMTP SENT, failure retry, max retry FAILED, notify admins |
| AuthE2ETest | 4 | Register→login flow, unauth 401, weak password 400, profile update |
| FullWorkflowE2ETest | 1 | Admin creates league/teams/players/match → user registers + predicts → admin publishes result → verify scoring + standings + leaderboard + RBAC |

---

## 15. Demo Script

Follow this exact sequence during your demo to show every feature:

### Step 1: Show the app is running
```bash
curl http://localhost:8080/api/v1/actuator/health
# Shows: {"status":"UP"}
```

Open Swagger UI in browser: `http://localhost:8080/api/v1/swagger-ui/index.html`

### Step 2: Admin Login
```bash
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"usernameOrEmail":"admin","password":"Admin@1234"}'
```
Copy the `accessToken` from response. This is the admin JWT.

### Step 3: Show seed data
```bash
# 10 IPL teams
curl http://localhost:8080/api/v1/teams -H "Authorization: Bearer <ADMIN_TOKEN>"

# 25 CSK players
curl "http://localhost:8080/api/v1/players/team/<CSK_ID>" -H "Authorization: Bearer <ADMIN_TOKEN>"
```

### Step 4: Register a user
```bash
curl -X POST http://localhost:8080/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username":"demo_user","email":"demo@test.com","password":"Demo@123","displayName":"Demo User"}'
```

### Step 5: Show RBAC (user cannot create team)
```bash
curl -X POST http://localhost:8080/api/v1/teams \
  -H "Authorization: Bearer <USER_TOKEN>" \
  -H "Content-Type: application/json" \
  -d '{"name":"Hack Team"}'
# Returns 403 Forbidden
```

### Step 6: Open the IPL season
```bash
curl -X POST http://localhost:8080/api/v1/seasons/<SEASON_ID>/open \
  -H "Authorization: Bearer <ADMIN_TOKEN>"
# Status changes to PREDICTION_OPEN
```

### Step 7: Submit a match prediction
```bash
curl -X POST http://localhost:8080/api/v1/predictions/matches/<MATCH_ID> \
  -H "Authorization: Bearer <USER_TOKEN>" \
  -H "Content-Type: application/json" \
  -d '{"predictedWinnerTeamId":"<CSK_ID>","predictedTossWinnerTeamId":"<RCB_ID>","predictedPlayerOfMatchId":"<PLAYER_ID>"}'
```

### Step 8: Publish a result
```bash
curl -X POST http://localhost:8080/api/v1/results/matches \
  -H "Authorization: Bearer <ADMIN_TOKEN>" \
  -H "Content-Type: application/json" \
  -d '{"matchId":"<MATCH_ID>","winnerTeamId":"<CSK_ID>","tossWinnerTeamId":"<RCB_ID>","playerOfMatchId":"<PLAYER_ID>","tie":false}'
```

### Step 9: Check leaderboard
```bash
curl http://localhost:8080/api/v1/leaderboard/seasons/<SEASON_ID> \
  -H "Authorization: Bearer <USER_TOKEN>"
```

### Step 10: Run the automated smoke test
```bash
bash scripts/smoke-test.sh
# Shows: ALL 59 TESTS PASSED
```

### Step 11: Run unit tests
```bash
./mvnw test
# Shows: Tests run: 60, Failures: 0, Errors: 0
```

---

## 16. Common Questions & Answers

**Q: Why Spring Boot instead of FastAPI?**
A: The requirements document specifically asked for "A Spring Boot based backend web application" (Section 4.1).

**Q: Why PostgreSQL?**
A: Required by the spec. Also gives us: UUID support, CHECK constraints, trigger functions for prediction lock enforcement, and TIMESTAMPTZ for timezone handling (Decision DL-001).

**Q: Why feature-based packages instead of layer-based?**
A: Better modularity — each feature (auth, league, prediction, etc.) is self-contained. You can understand one feature without reading the others (Decision DL-002).

**Q: Why interface + implementation for services?**
A: Required by the spec ("Interface driven development"). Enables mocking in tests and supports future AOP proxying (Decision DL-012).

**Q: Why Caffeine cache instead of Redis?**
A: Single-instance app doesn't need distributed cache. Caffeine is in-process, zero-config, and sufficient for our needs (Decision DL-006).

**Q: How are predictions locked?**
A: Two layers of defense: (1) Application code checks `match.isPredictionLocked()` before allowing writes. (2) PostgreSQL triggers reject any INSERT/UPDATE to prediction tables after lock time — even if someone bypasses the app (Decision DL-004).

**Q: Why are points never accepted via API?**
A: Security requirement. Points are calculated server-side only when admin publishes a result. No endpoint accepts point values. This prevents score manipulation.

**Q: What happens when an email fails?**
A: Retry up to 3 times. After 3 failures, status becomes FAILED. All attempts are logged in the `email_notifications` table with error messages (Decision DL-014).

**Q: What's soft delete?**
A: Instead of `DELETE FROM users WHERE id = X`, we do `UPDATE users SET is_deleted = true`. The record stays in the DB for audit purposes. All queries automatically filter out deleted records.

**Q: How does the `@Async` scoring work?**
A: When admin publishes a result, the main transaction commits first (saving the result). Then, using `TransactionSynchronization.afterCommit()`, scoring runs on a separate thread pool (`scoreExecutor`). This prevents slow scoring from blocking the admin's API response.

**Q: What's the difference between match predictions and league predictions?**
A: Match predictions = who wins THIS specific match (per-match). League predictions = what will be the FINAL standings at end of season (one-time, all teams ranked).

**Q: How do I know if all requirements are met?**
A: See `family-league-requirements.md` — every requirement maps to implemented features. The decision log (`docs/decision-log.md`) documents every architectural choice with justification.
