# Family League

**Repository:** [https://github.com/rahul-div/family-league](https://github.com/rahul-div/family-league)

A production-ready Spring Boot backend for a family-and-friends sports prediction platform. Users predict match outcomes (winner, toss, player of the match) and league standings. Points are awarded for correct predictions, with a leaderboard tracking rankings across seasons.

## Tech Stack

| Technology | Purpose |
|---|---|
| Java 21 | Language |
| Spring Boot 3.3 | Framework (think of it as "FastAPI for Java") |
| PostgreSQL 14+ | Database |
| Spring Data JPA + Hibernate | ORM (like SQLAlchemy) |
| Flyway | Database migrations (like Alembic) |
| Spring Security + JWT | Authentication & Authorization |
| Caffeine | In-memory caching |
| SpringDoc OpenAPI | Swagger UI / API docs |
| Testcontainers | Real PostgreSQL in tests via Docker |

---

## Step-by-Step Setup (Fresh Machine)

### 1. Install Prerequisites

| Tool | macOS | Windows | Linux |
|---|---|---|---|
| **Java 21** | `brew install openjdk@21` | [Download](https://adoptium.net/temurin/releases/?version=21) | `sudo apt install openjdk-21-jdk` |
| **Maven** | `brew install maven` | [Download](https://maven.apache.org/download.cgi) | `sudo apt install maven` |
| **PostgreSQL** | `brew install postgresql@15` | [Download](https://www.enterprisedb.com/downloads/postgres-postgresql-downloads) | `sudo apt install postgresql` |
| **Docker** | `brew install --cask docker` | [Download](https://www.docker.com/products/docker-desktop/) | `sudo apt install docker.io` |

After installing, verify:
```bash
java --version       # Should show 21.x.x
mvn --version        # Should show 3.9.x
psql --version       # Should show 14.x or higher
docker --version     # Should show 20.x or higher
```

> **Note:** Maven is optional — the project includes `./mvnw` (Maven Wrapper) which auto-downloads the correct Maven version. Use `./mvnw` instead of `mvn` in all commands below if you prefer.

### 2. Clone the Repository

```bash
git clone <your-repo-url>
cd family-league
```

### 3. Create the Database

```bash
# Start PostgreSQL if not running
brew services start postgresql@15    # macOS
sudo systemctl start postgresql      # Linux

# Create the database
psql -U postgres -c "CREATE DATABASE family_league;"

# Verify
psql -U postgres -d family_league -c "SELECT 1;"
```

**If you get "role postgres does not exist":**
```bash
createuser -s postgres
# OR
psql -d postgres -c "CREATE ROLE postgres WITH LOGIN SUPERUSER PASSWORD 'postgres';"
```

### 4. Configure Environment Variables

```bash
# Copy the example file
cp .env.example .env

# Edit with your values (only DB_PASSWORD typically needs changing)
nano .env    # or open in any editor
```

**What's in `.env`:**

| Variable | Required? | Default | What It Does |
|---|---|---|---|
| `DB_URL` | Yes | `jdbc:postgresql://localhost:5432/family_league` | PostgreSQL connection URL |
| `DB_USERNAME` | Yes | `postgres` | Database username |
| `DB_PASSWORD` | Yes | `postgres` | Database password |
| `JWT_SECRET` | No | (built-in default) | Secret key for signing JWT tokens. Change in production! |
| `JWT_EXPIRATION_MS` | No | `86400000` (24 hours) | How long JWT tokens last |
| `MAIL_HOST` | No | `smtp.gmail.com` | SMTP server. Leave empty for simulated email |
| `MAIL_PORT` | No | `587` | SMTP port |
| `MAIL_USERNAME` | No | (empty) | **When empty, emails are logged to console instead of sent** |
| `MAIL_PASSWORD` | No | (empty) | SMTP password |
| `ADMIN_EMAIL` | No | `admin@familyleague.com` | Where admin notifications go |
| `FROM_EMAIL` | No | `noreply@familyleague.com` | "From" address on outgoing emails |
| `SERVER_PORT` | No | `8080` | HTTP port |

**For local development, the defaults work out of the box.** Just make sure your PostgreSQL password matches `DB_PASSWORD`.

### 5. Load Environment Variables & Run

```bash
# Load the .env file into your shell
source .env

# Build and run (first time downloads dependencies — takes ~2 minutes)
./mvnw spring-boot:run
```

**What happens on first run:**
1. Maven downloads all Java dependencies (cached after first run)
2. Flyway automatically creates all 15 database tables
3. Seeds: 1 admin user, 1 IPL league, 1 season, 10 teams, 250 players, 3 matches
4. Starts web server on port 8080

**You should see:**
```
Started FamilyLeagueApplication in 4.3 seconds
```

### 6. Verify It Works

```bash
# Health check
curl http://localhost:8080/api/v1/actuator/health
# Expected: {"status":"UP"}

# Login as admin
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"usernameOrEmail": "admin", "password": "Admin@1234"}'
# Expected: JSON with accessToken
```

**Open Swagger UI in your browser:**
```
http://localhost:8080/api/v1/swagger-ui/index.html
```

---

## Seed Data (Pre-loaded)

| Data | Count | Details |
|---|---|---|
| Admin user | 1 | `admin` / `Admin@1234` |
| League | 1 | Indian Premier League (Cricket) |
| Season | 1 | IPL 2024 (UPCOMING status) |
| Teams | 10 | CSK, DC, GT, KKR, LSG, MI, PBKS, RR, RCB, SRH |
| Players | 250 | 25 per team (real IPL player names) |
| Matches | 3 | CSK vs RCB, PBKS vs DC, KKR vs SRH |
| Config | 6 | Lock hours, JWT expiry, etc. |

---

## Running Tests

### Smoke Test (requires running app)

```bash
# In one terminal: start the app
source .env && ./mvnw spring-boot:run

# In another terminal: run smoke test
bash scripts/smoke-test.sh
```

This runs 59 automated curl tests covering every feature.

### Unit Tests (no app needed, no Docker needed)

```bash
./mvnw test -Dtest="AuthServiceTest,SeasonServiceTest,PredictionServiceTest,ScoreCalculationServiceTest,MatchServiceTest,ResultServiceTest,UserServiceTest,LeagueServiceTest,EmailNotificationServiceTest"
```

### E2E Integration Tests (requires Docker)

```bash
# Make sure Docker is running, then:
./mvnw test -Dtest="AuthE2ETest,FullWorkflowE2ETest"
```

These spin up a real PostgreSQL in Docker via Testcontainers.

### All Tests

```bash
./mvnw clean test
# Expected: Tests run: 60, Failures: 0, Errors: 0
```

---

## HTTPS Setup (Optional)

```bash
# 1. Generate self-signed certificate
keytool -genkeypair -alias familyleague -keyalg RSA -keysize 2048 \
  -storetype PKCS12 -keystore src/main/resources/keystore.p12 \
  -validity 365 -storepass changeit \
  -dname "CN=localhost, OU=FamilyLeague, O=FamilyLeague, L=City, ST=State, C=US"

# 2. Run with HTTPS profile
source .env && ./mvnw spring-boot:run -Dspring-boot.run.profiles=https

# 3. Access (use -k to skip cert warning)
curl -k https://localhost:8443/api/v1/actuator/health
# Swagger UI: https://localhost:8443/api/v1/swagger-ui/index.html
```

---

## API Quick Reference

All endpoints are prefixed with `/api/v1`. Full docs at Swagger UI.

| Endpoint | Method | Access | Description |
|---|---|---|---|
| `/auth/register` | POST | Public | Register new user |
| `/auth/login` | POST | Public | Login (email or username) |
| `/auth/me` | GET | Auth | Current user profile |
| `/auth/admin` | POST | Admin | Create admin account |
| `/users` | GET | Admin | List/search users |
| `/users/{id}/profile` | PUT | Owner/Admin | Update profile |
| `/leagues` | GET/POST | Auth/Admin | List or create leagues |
| `/seasons` | POST | Admin | Create season |
| `/seasons/{id}/open` | POST | Admin | Open for predictions |
| `/seasons/{id}/close` | POST | Admin | Close season |
| `/seasons/{id}/teams` | GET/POST | Auth/Admin | List or enroll teams |
| `/teams` | GET/POST | Auth/Admin | List or create teams |
| `/players` | POST | Admin | Create player |
| `/players/team/{teamId}` | GET | Auth | Players by team |
| `/matches` | POST | Admin | Schedule match |
| `/matches/season/{id}` | GET | Auth | List matches |
| `/predictions/matches/{id}` | POST | Auth | Submit match prediction |
| `/predictions/matches/{id}/me` | GET | Auth | My prediction |
| `/predictions/seasons/{id}/league` | POST | Auth | Submit league predictions |
| `/results/matches` | POST | Admin | Publish match result |
| `/leaderboard/seasons/{id}` | GET | Auth | View leaderboard |
| `/leaderboard/seasons/{id}/me` | GET | Auth | My rank + breakdown |
| `/notifications/bulk` | POST | Admin | Send bulk notification |
| `/config` | GET/PUT | Admin | Runtime configuration |

---

## Project Structure

```
src/main/java/com/familyleague/
├── common/          # Shared: BaseEntity, ApiResponse, exceptions, config
├── auth/            # JWT auth, login, register, security
├── user/            # User management
├── league/          # Leagues + Seasons
├── team/            # Teams + Season enrollment
├── player/          # Players
├── match/           # Matches + Results
├── prediction/      # Match + League predictions
├── scoring/         # Score calculation + Leaderboard
├── standing/        # League standings (W-D-L)
├── notification/    # Email system
├── appconfig/       # Runtime config
└── scheduler/       # Scheduled tasks (lock, reminders)
```

---

## Documentation

| Doc | Description |
|---|---|
| [Setup & Testing Guide](docs/setup-and-testing-guide.md) | Detailed installation + 50 curl test commands |
| [Implementation Plan](docs/implementation-plan.md) | Architecture, all 12 phases |
| [Data Model](docs/data-model.md) | All 15 tables with relationships |
| [ER Diagram](docs/er-diagram.mmd) | Mermaid entity-relationship diagram |
| [Decision Log](docs/decision-log.md) | 14 architectural decisions with justifications |

---

## Troubleshooting

| Problem | Solution |
|---|---|
| `Port 8080 already in use` | `lsof -ti:8080 \| xargs kill -9` or set `SERVER_PORT=9090` |
| `Connection refused` to PostgreSQL | `brew services start postgresql@15` (macOS) or `sudo systemctl start postgresql` (Linux) |
| `Role postgres does not exist` | `createuser -s postgres` |
| `Flyway migration failed` | Drop & recreate DB: `psql -U postgres -c "DROP DATABASE family_league; CREATE DATABASE family_league;"` |
| `Docker not available` (tests) | Start Docker Desktop, verify with `docker ps` |
| `JWT token expired` | Re-login to get a fresh token (24h default) |
| App starts but APIs return 500 | Check `logs/family-league.log` for details |
