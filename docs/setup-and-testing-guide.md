# Family League — Complete Setup, Run & Testing Guide

This guide walks you through installing every dependency, running the application, and testing every feature end-to-end. Follow each section in order.

---

## Table of Contents

1. [Prerequisites Installation](#1-prerequisites-installation)
2. [Database Setup](#2-database-setup)
3. [Project Setup & First Run](#3-project-setup--first-run)
4. [Verify Application is Running](#4-verify-application-is-running)
5. [Testing Every Feature](#5-testing-every-feature)
   - [5.1 Authentication](#51-authentication)
   - [5.2 User Management](#52-user-management)
   - [5.3 Leagues](#53-leagues)
   - [5.4 Teams](#54-teams)
   - [5.5 Players](#55-players)
   - [5.6 Seasons](#56-seasons)
   - [5.7 Matches](#57-matches)
   - [5.8 Predictions](#58-predictions)
   - [5.9 Results & Scoring](#59-results--scoring)
   - [5.10 Leaderboard](#510-leaderboard)
   - [5.11 Notifications](#511-notifications)
   - [5.12 App Configuration](#512-app-configuration)
   - [5.13 Season Lifecycle (Full Flow)](#513-season-lifecycle-full-flow)
6. [Running Automated Tests](#6-running-automated-tests)
7. [HTTPS Setup (Optional)](#7-https-setup-optional)
8. [Troubleshooting](#8-troubleshooting)

---

## 1. Prerequisites Installation

### 1.1 Install Java 21

**macOS (using Homebrew):**
```bash
# Install Homebrew if not installed
/bin/bash -c "$(curl -fsSL https://raw.githubusercontent.com/Homebrew/install/HEAD/install.sh)"

# Install Java 21
brew install openjdk@21

# Add to PATH (add this to ~/.zshrc or ~/.bash_profile)
export PATH="/opt/homebrew/opt/openjdk@21/bin:$PATH"
export JAVA_HOME="/opt/homebrew/opt/openjdk@21"

# Apply changes
source ~/.zshrc

# Verify
java --version
# Expected output: openjdk 21.x.x
```

**macOS (using SDKMAN — recommended):**
```bash
# Install SDKMAN
curl -s "https://get.sdkman.io" | bash
source "$HOME/.sdkman/bin/sdkman-init.sh"

# Install Java 21
sdk install java 21.0.4-tem

# Verify
java --version
```

**Windows:**
1. Download from https://adoptium.net/temurin/releases/?version=21
2. Run the installer (check "Add to PATH")
3. Open new terminal and verify: `java --version`

**Linux (Ubuntu/Debian):**
```bash
sudo apt update
sudo apt install openjdk-21-jdk -y
java --version
```

---

### 1.2 Install Maven

**macOS:**
```bash
brew install maven

# Verify
mvn --version
# Expected: Apache Maven 3.9.x, Java version: 21
```

**Windows:**
1. Download from https://maven.apache.org/download.cgi (Binary zip archive)
2. Extract to `C:\Program Files\Maven`
3. Add `C:\Program Files\Maven\bin` to your System PATH
4. Verify: `mvn --version`

**Linux:**
```bash
sudo apt install maven -y
mvn --version
```

**Alternative — Use Maven Wrapper (no install needed):**
The project includes `mvnw` (Maven Wrapper). You can use `./mvnw` instead of `mvn` for all commands. It auto-downloads the correct Maven version.

---

### 1.3 Install PostgreSQL 15+

**macOS:**
```bash
brew install postgresql@15

# Start PostgreSQL service
brew services start postgresql@15

# Verify it's running
psql --version
# Expected: psql (PostgreSQL) 15.x

# Check connection
psql -U postgres -c "SELECT version();"
```

If `psql -U postgres` gives "role does not exist" error:
```bash
# Create the postgres superuser role
createuser -s postgres

# Or connect with your macOS username first
psql -d postgres -c "CREATE ROLE postgres WITH LOGIN SUPERUSER PASSWORD 'postgres';"
```

**Windows:**
1. Download installer from https://www.enterprisedb.com/downloads/postgres-postgresql-downloads
2. Run installer, set password for `postgres` user (remember this!)
3. Use default port 5432
4. Verify: Open pgAdmin or run in cmd: `psql -U postgres`

**Linux:**
```bash
sudo apt install postgresql postgresql-contrib -y
sudo systemctl start postgresql
sudo systemctl enable postgresql

# Set password for postgres user
sudo -u postgres psql -c "ALTER USER postgres PASSWORD 'postgres';"
```

---

### 1.4 Install Docker (Required for Automated Tests)

Testcontainers needs Docker to spin up PostgreSQL containers for integration/E2E tests.

**macOS:**
```bash
brew install --cask docker
# Open Docker Desktop app and wait for it to start
# Verify
docker --version
```

**Windows:**
1. Download Docker Desktop from https://www.docker.com/products/docker-desktop/
2. Install and restart
3. Open Docker Desktop and wait for it to start

**Linux:**
```bash
sudo apt install docker.io -y
sudo systemctl start docker
sudo usermod -aG docker $USER
# Log out and back in, then verify
docker --version
```

---

### 1.5 Install curl (for API testing)

**macOS:** Already installed by default.

**Windows:** Already included in Windows 10+. Or install Git Bash which includes curl.

**Linux:**
```bash
sudo apt install curl -y
```

---

### 1.6 (Optional) Install Postman

For a GUI-based API testing experience:
- Download from https://www.postman.com/downloads/
- Or use the VS Code extension "Thunder Client"

---

## 2. Database Setup

### 2.1 Create the Database

```bash
# Connect to PostgreSQL
psql -U postgres

# Create the database
CREATE DATABASE family_league;

# Verify it was created
\l

# Exit
\q
```

### 2.2 Verify Connection

```bash
psql -U postgres -d family_league -c "SELECT 1 AS connected;"
```

Expected output:
```
 connected
-----------
         1
```

If your PostgreSQL uses a different username/password, note them — you'll set them as environment variables in the next step.

---

## 3. Project Setup & First Run

### 3.1 Navigate to the Project

```bash
cd /Users/rahul/Desktop/family_league
```

### 3.2 Set Environment Variables (if your DB credentials differ from defaults)

The defaults are `postgres`/`postgres`. If yours differ:

```bash
export DB_URL=jdbc:postgresql://localhost:5432/family_league
export DB_USERNAME=postgres
export DB_PASSWORD=your_password_here
```

### 3.3 Build the Project

```bash
# Using Maven Wrapper (recommended — no Maven install needed)
./mvnw clean compile

# OR using installed Maven
mvn clean compile
```

Expected: `BUILD SUCCESS` with zero errors.

If you see Lombok annotation processing errors, ensure your Java version is 21.

### 3.4 Run the Application

```bash
./mvnw spring-boot:run
```

Expected startup logs:
```
  .   ____          _            __ _ _
 /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
...
Flyway: Successfully applied 14 migration(s)
...
Tomcat started on port 8080
Started FamilyLeagueApplication in X.XXX seconds
```

**What happens on first run:**
- Flyway runs all 14 SQL migrations automatically
- Creates all 15 database tables
- Seeds app configuration (6 default values)
- Seeds admin user: `admin@familyleague.com` / `Admin@1234`
- Seeds IPL data: 1 league, 1 season, 10 teams, 250 players, 3 matches
- Creates PostgreSQL triggers for prediction lock enforcement

### 3.5 Verify Database Was Seeded

```bash
psql -U postgres -d family_league -c "SELECT count(*) FROM users;"
# Expected: 1 (admin user)

psql -U postgres -d family_league -c "SELECT count(*) FROM teams;"
# Expected: 10

psql -U postgres -d family_league -c "SELECT count(*) FROM players;"
# Expected: 250

psql -U postgres -d family_league -c "SELECT count(*) FROM app_config;"
# Expected: 6
```

---

## 4. Verify Application is Running

### 4.1 Health Check

```bash
curl http://localhost:8080/api/v1/actuator/health
```

Expected:
```json
{"status":"UP"}
```

### 4.2 Swagger UI

Open in your browser:
```
http://localhost:8080/api/v1/swagger-ui.html
```

You should see the full API documentation with all endpoints grouped by tags:
Authentication, Users, Leagues, Seasons, Teams, Players, Matches, Results, Predictions, Leaderboard, Notifications, Configuration.

### 4.3 Test Unauthenticated Access (should be blocked)

```bash
curl -s http://localhost:8080/api/v1/leagues | python3 -m json.tool
```

Expected: `401 Unauthorized` response.

---

## 5. Testing Every Feature

We'll use `curl` commands. Save tokens in shell variables for convenience.

> **Tip:** Pipe any curl output through `python3 -m json.tool` for pretty printing:
> ```bash
> curl -s ... | python3 -m json.tool
> ```

---

### 5.1 Authentication

#### 5.1.1 Login as Admin (Seeded User)

```bash
ADMIN_TOKEN=$(curl -s -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"usernameOrEmail": "admin", "password": "Admin@1234"}' \
  | python3 -c "import sys,json; print(json.load(sys.stdin)['data']['accessToken'])")

echo "Admin Token: $ADMIN_TOKEN"
```

#### 5.1.2 Register a New User

```bash
USER1_TOKEN=$(curl -s -X POST http://localhost:8080/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "alice",
    "email": "alice@example.com",
    "password": "Alice@123",
    "displayName": "Alice Johnson"
  }' | python3 -c "import sys,json; print(json.load(sys.stdin)['data']['accessToken'])")

echo "User1 Token: $USER1_TOKEN"
```

#### 5.1.3 Register a Second User

```bash
USER2_TOKEN=$(curl -s -X POST http://localhost:8080/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "bob",
    "email": "bob@example.com",
    "password": "Bob@12345",
    "displayName": "Bob Smith"
  }' | python3 -c "import sys,json; print(json.load(sys.stdin)['data']['accessToken'])")

echo "User2 Token: $USER2_TOKEN"
```

#### 5.1.4 Login by Username

```bash
curl -s -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"usernameOrEmail": "alice", "password": "Alice@123"}' | python3 -m json.tool
```

#### 5.1.5 Get Current User Profile

```bash
curl -s http://localhost:8080/api/v1/auth/me \
  -H "Authorization: Bearer $USER1_TOKEN" | python3 -m json.tool
```

#### 5.1.6 Refresh Token

```bash
curl -s -X POST http://localhost:8080/api/v1/auth/refresh \
  -H "Authorization: Bearer $USER1_TOKEN" | python3 -m json.tool
```

#### 5.1.7 Test Validation — Weak Password (Expect 400)

```bash
curl -s -X POST http://localhost:8080/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username": "weak", "email": "weak@test.com", "password": "123"}' | python3 -m json.tool
```

Expected: 400 with validation errors about password requirements.

#### 5.1.8 Test Duplicate Email (Expect 409)

```bash
curl -s -X POST http://localhost:8080/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username": "alice2", "email": "alice@example.com", "password": "Alice@123"}' | python3 -m json.tool
```

Expected: 409 Conflict "Email already registered".

#### 5.1.9 Test Bad Credentials (Expect 401)

```bash
curl -s -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"usernameOrEmail": "alice", "password": "WrongPassword@1"}' | python3 -m json.tool
```

Expected: 401 "Invalid email/username or password".

#### 5.1.10 Create Admin Account (Admin Only)

```bash
curl -s -X POST http://localhost:8080/api/v1/auth/admin \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin2",
    "email": "admin2@familyleague.com",
    "password": "Admin@5678",
    "displayName": "Admin Two"
  }' | python3 -m json.tool
```

#### 5.1.11 Non-Admin Cannot Create Admin (Expect 403)

```bash
curl -s -X POST http://localhost:8080/api/v1/auth/admin \
  -H "Authorization: Bearer $USER1_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "username": "hacker",
    "email": "hacker@test.com",
    "password": "Hacker@123"
  }' | python3 -m json.tool
```

Expected: 403 Forbidden.

---

### 5.2 User Management

#### 5.2.1 List All Users (Admin Only)

```bash
curl -s "http://localhost:8080/api/v1/users?page=0&size=10" \
  -H "Authorization: Bearer $ADMIN_TOKEN" | python3 -m json.tool
```

#### 5.2.2 Search Users

```bash
curl -s "http://localhost:8080/api/v1/users?q=alice" \
  -H "Authorization: Bearer $ADMIN_TOKEN" | python3 -m json.tool
```

#### 5.2.3 Get User by ID

First, get Alice's user ID from the profile:
```bash
ALICE_ID=$(curl -s http://localhost:8080/api/v1/auth/me \
  -H "Authorization: Bearer $USER1_TOKEN" \
  | python3 -c "import sys,json; print(json.load(sys.stdin)['data']['id'])")

curl -s "http://localhost:8080/api/v1/users/$ALICE_ID" \
  -H "Authorization: Bearer $USER1_TOKEN" | python3 -m json.tool
```

#### 5.2.4 Update Own Profile

```bash
curl -s -X PUT "http://localhost:8080/api/v1/users/$ALICE_ID/profile" \
  -H "Authorization: Bearer $USER1_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"displayName": "Alice J.", "avatarUrl": "https://example.com/alice.png"}' | python3 -m json.tool
```

#### 5.2.5 Non-Admin Cannot List Users (Expect 403)

```bash
curl -s "http://localhost:8080/api/v1/users" \
  -H "Authorization: Bearer $USER1_TOKEN" | python3 -m json.tool
```

Expected: 403 Forbidden.

---

### 5.3 Leagues

#### 5.3.1 List Leagues (Seeded IPL Should Appear)

```bash
curl -s "http://localhost:8080/api/v1/leagues" \
  -H "Authorization: Bearer $USER1_TOKEN" | python3 -m json.tool
```

#### 5.3.2 Create a New League (Admin Only)

```bash
curl -s -X POST http://localhost:8080/api/v1/leagues \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"name": "English Premier League", "sportType": "FOOTBALL", "description": "The top tier of English football"}' \
  | python3 -m json.tool
```

Save the league ID:
```bash
NEW_LEAGUE_ID=$(curl -s -X POST http://localhost:8080/api/v1/leagues \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"name": "Test League", "sportType": "CRICKET"}' \
  | python3 -c "import sys,json; print(json.load(sys.stdin)['data']['id'])")
```

#### 5.3.3 Get League by ID

```bash
curl -s "http://localhost:8080/api/v1/leagues/$NEW_LEAGUE_ID" \
  -H "Authorization: Bearer $USER1_TOKEN" | python3 -m json.tool
```

#### 5.3.4 Update League (Admin Only)

```bash
curl -s -X PUT "http://localhost:8080/api/v1/leagues/$NEW_LEAGUE_ID" \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"description": "Updated description"}' | python3 -m json.tool
```

#### 5.3.5 Search Leagues

```bash
curl -s "http://localhost:8080/api/v1/leagues?q=premier" \
  -H "Authorization: Bearer $USER1_TOKEN" | python3 -m json.tool
```

#### 5.3.6 Non-Admin Cannot Create League (Expect 403)

```bash
curl -s -X POST http://localhost:8080/api/v1/leagues \
  -H "Authorization: Bearer $USER1_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"name": "Unauthorized League"}' | python3 -m json.tool
```

---

### 5.4 Teams

#### 5.4.1 List All Teams (10 IPL Teams Seeded)

```bash
curl -s "http://localhost:8080/api/v1/teams?size=15" \
  -H "Authorization: Bearer $USER1_TOKEN" | python3 -m json.tool
```

#### 5.4.2 Search Teams

```bash
curl -s "http://localhost:8080/api/v1/teams?q=chennai" \
  -H "Authorization: Bearer $USER1_TOKEN" | python3 -m json.tool
```

#### 5.4.3 Get Team by ID

```bash
# Get first team's ID from list
TEAM_ID=$(curl -s "http://localhost:8080/api/v1/teams?size=1" \
  -H "Authorization: Bearer $USER1_TOKEN" \
  | python3 -c "import sys,json; print(json.load(sys.stdin)['data']['content'][0]['id'])")

curl -s "http://localhost:8080/api/v1/teams/$TEAM_ID" \
  -H "Authorization: Bearer $USER1_TOKEN" | python3 -m json.tool
```

#### 5.4.4 Create a Team (Admin Only)

```bash
curl -s -X POST http://localhost:8080/api/v1/teams \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"name": "Test Warriors", "shortName": "TW", "description": "A test team"}' \
  | python3 -m json.tool
```

#### 5.4.5 Duplicate Team Name (Expect 409)

```bash
curl -s -X POST http://localhost:8080/api/v1/teams \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"name": "Chennai Super Kings"}' | python3 -m json.tool
```

Expected: 409 "Team with name 'Chennai Super Kings' already exists".

---

### 5.5 Players

#### 5.5.1 List Players by Team

```bash
# Use CSK's team ID
CSK_ID=$(curl -s "http://localhost:8080/api/v1/teams?q=chennai" \
  -H "Authorization: Bearer $USER1_TOKEN" \
  | python3 -c "import sys,json; print(json.load(sys.stdin)['data']['content'][0]['id'])")

curl -s "http://localhost:8080/api/v1/players/team/$CSK_ID?size=30" \
  -H "Authorization: Bearer $USER1_TOKEN" | python3 -m json.tool
```

#### 5.5.2 Get Player by ID

```bash
PLAYER_ID=$(curl -s "http://localhost:8080/api/v1/players/team/$CSK_ID?size=1" \
  -H "Authorization: Bearer $USER1_TOKEN" \
  | python3 -c "import sys,json; print(json.load(sys.stdin)['data']['content'][0]['id'])")

curl -s "http://localhost:8080/api/v1/players/$PLAYER_ID" \
  -H "Authorization: Bearer $USER1_TOKEN" | python3 -m json.tool
```

#### 5.5.3 Create a Player (Admin Only)

```bash
curl -s -X POST http://localhost:8080/api/v1/players \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d "{\"teamId\": \"$CSK_ID\", \"name\": \"Test Player\", \"jerseyNumber\": 99, \"playerRole\": \"ALL_ROUNDER\"}" \
  | python3 -m json.tool
```

---

### 5.6 Seasons

#### 5.6.1 List Seasons for IPL League

```bash
IPL_LEAGUE_ID=$(curl -s "http://localhost:8080/api/v1/leagues?q=indian" \
  -H "Authorization: Bearer $USER1_TOKEN" \
  | python3 -c "import sys,json; print(json.load(sys.stdin)['data']['content'][0]['id'])")

curl -s "http://localhost:8080/api/v1/seasons/league/$IPL_LEAGUE_ID" \
  -H "Authorization: Bearer $USER1_TOKEN" | python3 -m json.tool
```

#### 5.6.2 Get Season Details

```bash
SEASON_ID=$(curl -s "http://localhost:8080/api/v1/seasons/league/$IPL_LEAGUE_ID" \
  -H "Authorization: Bearer $USER1_TOKEN" \
  | python3 -c "import sys,json; print(json.load(sys.stdin)['data']['content'][0]['id'])")

curl -s "http://localhost:8080/api/v1/seasons/$SEASON_ID" \
  -H "Authorization: Bearer $USER1_TOKEN" | python3 -m json.tool
```

#### 5.6.3 List Enrolled Teams

```bash
curl -s "http://localhost:8080/api/v1/seasons/$SEASON_ID/teams" \
  -H "Authorization: Bearer $USER1_TOKEN" | python3 -m json.tool
```

Expected: 10 IPL teams.

#### 5.6.4 Open the Season (Admin Only)

The seeded IPL season has 10 teams and 3 matches, so it meets all requirements:

```bash
curl -s -X POST "http://localhost:8080/api/v1/seasons/$SEASON_ID/open" \
  -H "Authorization: Bearer $ADMIN_TOKEN" | python3 -m json.tool
```

Expected: Status changes to `PREDICTION_OPEN`, `predictionLockedAt` is set.

#### 5.6.5 Verify Season is Now Open

```bash
curl -s "http://localhost:8080/api/v1/seasons/$SEASON_ID" \
  -H "Authorization: Bearer $USER1_TOKEN" | python3 -m json.tool
```

Check that `status` is `PREDICTION_OPEN`.

---

### 5.7 Matches

#### 5.7.1 List Matches for the Season

```bash
curl -s "http://localhost:8080/api/v1/matches/season/$SEASON_ID" \
  -H "Authorization: Bearer $USER1_TOKEN" | python3 -m json.tool
```

Expected: 3 seeded matches (CSK vs RCB, PBKS vs DC, KKR vs SRH).

#### 5.7.2 Get Match Details

```bash
MATCH_ID=$(curl -s "http://localhost:8080/api/v1/matches/season/$SEASON_ID?size=1" \
  -H "Authorization: Bearer $USER1_TOKEN" \
  | python3 -c "import sys,json; print(json.load(sys.stdin)['data']['content'][0]['id'])")

curl -s "http://localhost:8080/api/v1/matches/$MATCH_ID" \
  -H "Authorization: Bearer $USER1_TOKEN" | python3 -m json.tool
```

Note the `homeTeamId`, `awayTeamId`, and `predictionLockTime` from the response. You'll need these for predictions.

#### 5.7.3 Create a New Match (Admin Only)

```bash
# Get two team IDs from the enrolled teams
TEAM_A=$(curl -s "http://localhost:8080/api/v1/seasons/$SEASON_ID/teams" \
  -H "Authorization: Bearer $USER1_TOKEN" \
  | python3 -c "import sys,json; print(json.load(sys.stdin)['data'][0]['id'])")

TEAM_B=$(curl -s "http://localhost:8080/api/v1/seasons/$SEASON_ID/teams" \
  -H "Authorization: Bearer $USER1_TOKEN" \
  | python3 -c "import sys,json; print(json.load(sys.stdin)['data'][1]['id'])")

# Schedule match 7 days from now
MATCH_TIME=$(date -u -v+7d '+%Y-%m-%dT%H:%M:%SZ' 2>/dev/null || date -u -d '+7 days' '+%Y-%m-%dT%H:%M:%SZ')

curl -s -X POST http://localhost:8080/api/v1/matches \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d "{
    \"seasonId\": \"$SEASON_ID\",
    \"homeTeamId\": \"$TEAM_A\",
    \"awayTeamId\": \"$TEAM_B\",
    \"matchNumber\": 4,
    \"scheduledAt\": \"$MATCH_TIME\",
    \"venue\": \"Test Stadium\"
  }" | python3 -m json.tool
```

#### 5.7.4 Same Team Match (Expect 400)

```bash
curl -s -X POST http://localhost:8080/api/v1/matches \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d "{
    \"seasonId\": \"$SEASON_ID\",
    \"homeTeamId\": \"$TEAM_A\",
    \"awayTeamId\": \"$TEAM_A\",
    \"matchNumber\": 99,
    \"scheduledAt\": \"$MATCH_TIME\"
  }" | python3 -m json.tool
```

Expected: 400 "Home team and away team cannot be the same".

---

### 5.8 Predictions

For predictions to work, the match must be in `SCHEDULED` status and the current time must be before `predictionLockTime`. The seeded matches have lock times in 2024, so we'll use the new match created above (7 days in the future).

#### 5.8.1 Get the New Match ID and Team IDs

```bash
# Get the match we just created (match #4)
NEW_MATCH=$(curl -s "http://localhost:8080/api/v1/matches/season/$SEASON_ID?sort=matchNumber,desc&size=1" \
  -H "Authorization: Bearer $USER1_TOKEN" \
  | python3 -c "import sys,json; d=json.load(sys.stdin)['data']['content'][0]; print(d['id'], d['homeTeamId'], d['awayTeamId'])")

NEW_MATCH_ID=$(echo $NEW_MATCH | cut -d' ' -f1)
HOME_TEAM_ID=$(echo $NEW_MATCH | cut -d' ' -f2)
AWAY_TEAM_ID=$(echo $NEW_MATCH | cut -d' ' -f3)

echo "Match: $NEW_MATCH_ID"
echo "Home: $HOME_TEAM_ID"
echo "Away: $AWAY_TEAM_ID"
```

#### 5.8.2 Get a Player from the Home Team

```bash
HOME_PLAYER_ID=$(curl -s "http://localhost:8080/api/v1/players/team/$HOME_TEAM_ID?size=1" \
  -H "Authorization: Bearer $USER1_TOKEN" \
  | python3 -c "import sys,json; print(json.load(sys.stdin)['data']['content'][0]['id'])")

echo "Home player: $HOME_PLAYER_ID"
```

#### 5.8.3 Alice Submits a Match Prediction

```bash
curl -s -X POST "http://localhost:8080/api/v1/predictions/matches/$NEW_MATCH_ID" \
  -H "Authorization: Bearer $USER1_TOKEN" \
  -H "Content-Type: application/json" \
  -d "{
    \"predictedWinnerTeamId\": \"$HOME_TEAM_ID\",
    \"predictedTossWinnerTeamId\": \"$AWAY_TEAM_ID\",
    \"predictedPlayerOfMatchId\": \"$HOME_PLAYER_ID\"
  }" | python3 -m json.tool
```

#### 5.8.4 Bob Submits a Different Prediction

```bash
AWAY_PLAYER_ID=$(curl -s "http://localhost:8080/api/v1/players/team/$AWAY_TEAM_ID?size=1" \
  -H "Authorization: Bearer $USER1_TOKEN" \
  | python3 -c "import sys,json; print(json.load(sys.stdin)['data']['content'][0]['id'])")

curl -s -X POST "http://localhost:8080/api/v1/predictions/matches/$NEW_MATCH_ID" \
  -H "Authorization: Bearer $USER2_TOKEN" \
  -H "Content-Type: application/json" \
  -d "{
    \"predictedWinnerTeamId\": \"$AWAY_TEAM_ID\",
    \"predictedTossWinnerTeamId\": \"$HOME_TEAM_ID\",
    \"predictedPlayerOfMatchId\": \"$AWAY_PLAYER_ID\"
  }" | python3 -m json.tool
```

#### 5.8.5 Get My Prediction

```bash
curl -s "http://localhost:8080/api/v1/predictions/matches/$NEW_MATCH_ID/me" \
  -H "Authorization: Bearer $USER1_TOKEN" | python3 -m json.tool
```

#### 5.8.6 Update Prediction (Upsert)

```bash
curl -s -X POST "http://localhost:8080/api/v1/predictions/matches/$NEW_MATCH_ID" \
  -H "Authorization: Bearer $USER1_TOKEN" \
  -H "Content-Type: application/json" \
  -d "{
    \"predictedWinnerTeamId\": \"$AWAY_TEAM_ID\",
    \"predictedTossWinnerTeamId\": \"$HOME_TEAM_ID\",
    \"predictedPlayerOfMatchId\": \"$HOME_PLAYER_ID\"
  }" | python3 -m json.tool
```

#### 5.8.7 Invalid Team Prediction (Expect 400)

```bash
RANDOM_UUID="00000000-0000-0000-0000-000000000099"
curl -s -X POST "http://localhost:8080/api/v1/predictions/matches/$NEW_MATCH_ID" \
  -H "Authorization: Bearer $USER1_TOKEN" \
  -H "Content-Type: application/json" \
  -d "{\"predictedWinnerTeamId\": \"$RANDOM_UUID\"}" | python3 -m json.tool
```

Expected: 400 "Predicted winner must be one of the match teams".

#### 5.8.8 All Predictions (Before Lock — Expect 403 for Regular Users)

```bash
curl -s "http://localhost:8080/api/v1/predictions/matches/$NEW_MATCH_ID" \
  -H "Authorization: Bearer $USER1_TOKEN" | python3 -m json.tool
```

Expected: 403 "Predictions are only visible after the prediction window closes".

Admin can see them:
```bash
curl -s "http://localhost:8080/api/v1/predictions/matches/$NEW_MATCH_ID" \
  -H "Authorization: Bearer $ADMIN_TOKEN" | python3 -m json.tool
```

#### 5.8.9 Submit League Predictions

Get all enrolled team IDs first:
```bash
TEAM_IDS=$(curl -s "http://localhost:8080/api/v1/seasons/$SEASON_ID/teams" \
  -H "Authorization: Bearer $USER1_TOKEN" \
  | python3 -c "
import sys, json
teams = json.load(sys.stdin)['data']
preds = [{'teamId': t['id'], 'predictedPosition': i+1} for i, t in enumerate(teams)]
print(json.dumps({'predictions': preds}))
")

echo "$TEAM_IDS" | python3 -m json.tool

curl -s -X POST "http://localhost:8080/api/v1/predictions/seasons/$SEASON_ID/league" \
  -H "Authorization: Bearer $USER1_TOKEN" \
  -H "Content-Type: application/json" \
  -d "$TEAM_IDS" | python3 -m json.tool
```

#### 5.8.10 Get My League Predictions

```bash
curl -s "http://localhost:8080/api/v1/predictions/seasons/$SEASON_ID/league/me" \
  -H "Authorization: Bearer $USER1_TOKEN" | python3 -m json.tool
```

---

### 5.9 Results & Scoring

To test result publishing, we need to publish a result for our match. Note: the seeded matches have past lock times, so the scheduler won't interfere with our new match.

#### 5.9.1 Publish Match Result (Admin Only)

```bash
curl -s -X POST http://localhost:8080/api/v1/results/matches \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d "{
    \"matchId\": \"$NEW_MATCH_ID\",
    \"winnerTeamId\": \"$AWAY_TEAM_ID\",
    \"tossWinnerTeamId\": \"$HOME_TEAM_ID\",
    \"playerOfMatchId\": \"$HOME_PLAYER_ID\",
    \"tie\": false
  }" | python3 -m json.tool
```

Expected: Result published, match status becomes COMPLETED, standings updated, scoring triggered.

#### 5.9.2 Get Match Result

```bash
curl -s "http://localhost:8080/api/v1/results/matches/$NEW_MATCH_ID" \
  -H "Authorization: Bearer $USER1_TOKEN" | python3 -m json.tool
```

#### 5.9.3 Duplicate Result (Expect 409)

```bash
curl -s -X POST http://localhost:8080/api/v1/results/matches \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d "{\"matchId\": \"$NEW_MATCH_ID\", \"winnerTeamId\": \"$HOME_TEAM_ID\", \"tie\": false}" \
  | python3 -m json.tool
```

Expected: 409 "Match result already published".

#### 5.9.4 Check League Standings Were Updated

```bash
curl -s "http://localhost:8080/api/v1/seasons/$SEASON_ID/standings" \
  -H "Authorization: Bearer $USER1_TOKEN" | python3 -m json.tool
```

---

### 5.10 Leaderboard

#### 5.10.1 View Leaderboard

```bash
curl -s "http://localhost:8080/api/v1/leaderboard/seasons/$SEASON_ID" \
  -H "Authorization: Bearer $USER1_TOKEN" | python3 -m json.tool
```

#### 5.10.2 Get My Rank & Score Breakdown

```bash
curl -s "http://localhost:8080/api/v1/leaderboard/seasons/$SEASON_ID/me" \
  -H "Authorization: Bearer $USER1_TOKEN" | python3 -m json.tool
```

This shows your rank, total points, match score breakdown (which predictions were correct/incorrect), and season prediction scores.

#### 5.10.3 Manually Recalculate Leaderboard (Admin Only)

```bash
curl -s -X POST "http://localhost:8080/api/v1/leaderboard/seasons/$SEASON_ID/recalculate" \
  -H "Authorization: Bearer $ADMIN_TOKEN" | python3 -m json.tool
```

---

### 5.11 Notifications

#### 5.11.1 View All Email Logs (Admin Only)

```bash
curl -s "http://localhost:8080/api/v1/notifications" \
  -H "Authorization: Bearer $ADMIN_TOKEN" | python3 -m json.tool
```

You should see emails for: welcome emails (registration), score updates, admin notifications.

#### 5.11.2 View My Notifications

```bash
curl -s "http://localhost:8080/api/v1/notifications/me" \
  -H "Authorization: Bearer $USER1_TOKEN" | python3 -m json.tool
```

#### 5.11.3 Send Bulk Notification (Admin Only)

```bash
BOB_ID=$(curl -s "http://localhost:8080/api/v1/users?q=bob" \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  | python3 -c "import sys,json; print(json.load(sys.stdin)['data']['content'][0]['id'])")

curl -s -X POST http://localhost:8080/api/v1/notifications/bulk \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d "{
    \"userIds\": [\"$ALICE_ID\", \"$BOB_ID\"],
    \"eventType\": \"BULK_NOTIFICATION\",
    \"subject\": \"Weekend Special!\",
    \"message\": \"Don't forget to submit predictions for this weekend's matches!\"
  }" | python3 -m json.tool
```

---

### 5.12 App Configuration

#### 5.12.1 View All Config (Admin Only)

```bash
curl -s http://localhost:8080/api/v1/config \
  -H "Authorization: Bearer $ADMIN_TOKEN" | python3 -m json.tool
```

Expected: 6 config entries (match lock hours, league lock hours, etc.)

#### 5.12.2 Update a Config Value

```bash
curl -s -X PUT "http://localhost:8080/api/v1/config/match.lock.offset.hours" \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"value": "2"}' | python3 -m json.tool
```

---

### 5.13 Season Lifecycle (Full Flow)

This tests the complete season lifecycle from creation to closure.

```bash
echo "=== Step 1: Create a fresh league ==="
TEST_LEAGUE=$(curl -s -X POST http://localhost:8080/api/v1/leagues \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"name": "Lifecycle Test League", "sportType": "CRICKET"}' \
  | python3 -c "import sys,json; print(json.load(sys.stdin)['data']['id'])")

echo "=== Step 2: Create a season ==="
TEST_SEASON=$(curl -s -X POST http://localhost:8080/api/v1/seasons \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d "{\"leagueId\": \"$TEST_LEAGUE\", \"name\": \"Test Season 1\"}" \
  | python3 -c "import sys,json; print(json.load(sys.stdin)['data']['id'])")

echo "=== Step 3: Create two teams ==="
TEAM_X=$(curl -s -X POST http://localhost:8080/api/v1/teams \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"name": "Lifecycle Team X", "shortName": "LTX"}' \
  | python3 -c "import sys,json; print(json.load(sys.stdin)['data']['id'])")

TEAM_Y=$(curl -s -X POST http://localhost:8080/api/v1/teams \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"name": "Lifecycle Team Y", "shortName": "LTY"}' \
  | python3 -c "import sys,json; print(json.load(sys.stdin)['data']['id'])")

echo "=== Step 4: Enroll teams ==="
curl -s -X POST "http://localhost:8080/api/v1/seasons/$TEST_SEASON/teams" \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d "{\"teamId\": \"$TEAM_X\"}" > /dev/null

curl -s -X POST "http://localhost:8080/api/v1/seasons/$TEST_SEASON/teams" \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d "{\"teamId\": \"$TEAM_Y\"}" > /dev/null

echo "=== Step 5: Schedule a match ==="
FUTURE_TIME=$(date -u -v+14d '+%Y-%m-%dT%H:%M:%SZ' 2>/dev/null || date -u -d '+14 days' '+%Y-%m-%dT%H:%M:%SZ')
curl -s -X POST http://localhost:8080/api/v1/matches \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d "{
    \"seasonId\": \"$TEST_SEASON\",
    \"homeTeamId\": \"$TEAM_X\",
    \"awayTeamId\": \"$TEAM_Y\",
    \"matchNumber\": 1,
    \"scheduledAt\": \"$FUTURE_TIME\",
    \"venue\": \"Lifecycle Stadium\"
  }" > /dev/null

echo "=== Step 6: Open season (UPCOMING -> PREDICTION_OPEN) ==="
curl -s -X POST "http://localhost:8080/api/v1/seasons/$TEST_SEASON/open" \
  -H "Authorization: Bearer $ADMIN_TOKEN" | python3 -c "import sys,json; print('Status:', json.load(sys.stdin)['data']['status'])"

echo "=== Step 7: Verify season status ==="
curl -s "http://localhost:8080/api/v1/seasons/$TEST_SEASON" \
  -H "Authorization: Bearer $USER1_TOKEN" \
  | python3 -c "import sys,json; d=json.load(sys.stdin)['data']; print(f\"Status: {d['status']}, Lock at: {d['predictionLockedAt']}\")"

echo "=== Step 8: Publish final standings (-> COMPLETED) ==="
curl -s -X POST "http://localhost:8080/api/v1/seasons/$TEST_SEASON/publish-result" \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d "{\"standings\": [{\"teamId\": \"$TEAM_X\", \"finalPosition\": 1}, {\"teamId\": \"$TEAM_Y\", \"finalPosition\": 2}]}" \
  | python3 -c "import sys,json; print('Status:', json.load(sys.stdin)['data']['status'])"

echo "=== Step 9: Close season (COMPLETED -> CLOSED) ==="
curl -s -X POST "http://localhost:8080/api/v1/seasons/$TEST_SEASON/close" \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  | python3 -c "import sys,json; print('Status:', json.load(sys.stdin)['data']['status'])"

echo "=== LIFECYCLE TEST COMPLETE ==="
```

Expected output:
```
Status: PREDICTION_OPEN
Status: PREDICTION_OPEN, Lock at: <timestamp>
Status: COMPLETED
Status: CLOSED
LIFECYCLE TEST COMPLETE
```

---

## 6. Running Automated Tests

### 6.1 Prerequisites
- Docker must be running (Testcontainers spins up PostgreSQL containers)

### 6.2 Run All Tests

```bash
# Run unit tests + integration tests + E2E tests
./mvnw test

# Or with verbose output
./mvnw test -X
```

### 6.3 Run Specific Test Classes

```bash
# Only AuthService unit tests
./mvnw test -Dtest=AuthServiceTest

# Only SeasonService tests
./mvnw test -Dtest=SeasonServiceTest

# Only Prediction tests
./mvnw test -Dtest=PredictionServiceTest

# Only Scoring tests
./mvnw test -Dtest=ScoreCalculationServiceTest

# Only E2E tests
./mvnw test -Dtest="com.familyleague.e2e.*"

# Full workflow E2E
./mvnw test -Dtest=FullWorkflowE2ETest
```

### 6.4 What Each Test Class Verifies

| Test Class | Tests | What It Verifies |
|---|---|---|
| `AuthServiceTest` | 6 | Register, login, duplicates, bad credentials, inactive user |
| `SeasonServiceTest` | 7 | Open/close lifecycle, validation (min teams, matches, status) |
| `PredictionServiceTest` | 6 | Match/league prediction submission, locking, validation, visibility |
| `ScoreCalculationServiceTest` | 3 | Scoring engine: all correct (3pts), partial (1pt), tie handling |
| `AuthE2ETest` | 4 | Full register→login→profile→update flow, 401/400 checks |
| `FullWorkflowE2ETest` | 1 | Admin setup → user predictions → RBAC enforcement (complete flow) |

### 6.5 Expected Output

```
Tests run: 27, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

---

## 7. HTTPS Setup (Optional)

### 7.1 Generate Self-Signed Certificate

```bash
keytool -genkeypair \
  -alias familyleague \
  -keyalg RSA \
  -keysize 2048 \
  -storetype PKCS12 \
  -keystore src/main/resources/keystore.p12 \
  -validity 365 \
  -storepass changeit \
  -dname "CN=localhost, OU=FamilyLeague, O=FamilyLeague, L=City, ST=State, C=US"
```

### 7.2 Run with HTTPS Profile

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=https
```

### 7.3 Test HTTPS

```bash
# -k flag ignores self-signed cert warning
curl -k https://localhost:8443/api/v1/actuator/health
```

Access Swagger UI at: `https://localhost:8443/api/v1/swagger-ui.html`

---

## 8. Troubleshooting

### "Port 8080 already in use"
```bash
# Find what's using the port
lsof -i :8080
# Kill it
kill -9 <PID>

# Or run on a different port
SERVER_PORT=9090 ./mvnw spring-boot:run
```

### "Connection refused" to PostgreSQL
```bash
# Check if PostgreSQL is running
pg_isready
# Expected: /tmp:5432 - accepting connections

# Start it if not running
brew services start postgresql@15    # macOS
sudo systemctl start postgresql      # Linux
```

### "Role postgres does not exist"
```bash
createuser -s postgres
# Then set password
psql -d postgres -c "ALTER USER postgres PASSWORD 'postgres';"
```

### "Flyway migration failed"
```bash
# Drop and recreate the database
psql -U postgres -c "DROP DATABASE IF EXISTS family_league;"
psql -U postgres -c "CREATE DATABASE family_league;"
# Then restart the application
```

### "Testcontainers: Docker not available"
- Make sure Docker Desktop is running
- On Linux: `sudo systemctl start docker`
- Verify: `docker ps` should work without errors

### "Java version mismatch"
```bash
java --version
# Must be 21.x.x

# If you have multiple Java versions, set JAVA_HOME:
export JAVA_HOME=$(/usr/libexec/java_home -v 21)  # macOS
```

### Application starts but APIs return 500
- Check logs in `logs/family-league.log`
- Common cause: database connection issue — verify DB_URL, DB_USERNAME, DB_PASSWORD

### "JWT token expired"
- Tokens expire after 24 hours by default
- Simply re-login to get a fresh token
- Or change the expiry: update `jwt.expiry.seconds` via the config API

---

## Quick Reference — All API Endpoints

| Method | Endpoint | Access | Purpose |
|---|---|---|---|
| POST | `/auth/register` | Public | Register new user |
| POST | `/auth/login` | Public | Login |
| POST | `/auth/admin` | Admin | Create admin account |
| POST | `/auth/refresh` | Auth | Refresh JWT |
| GET | `/auth/me` | Auth | Current user profile |
| GET | `/users` | Admin | List users (search: `?q=`) |
| GET | `/users/{id}` | Auth | Get user |
| PUT | `/users/{id}/profile` | Owner/Admin | Update profile |
| DELETE | `/users/{id}` | Admin | Deactivate user |
| POST | `/leagues` | Admin | Create league |
| GET | `/leagues` | Auth | List leagues (search: `?q=`) |
| GET | `/leagues/{id}` | Auth | Get league |
| PUT | `/leagues/{id}` | Admin | Update league |
| DELETE | `/leagues/{id}` | Admin | Delete league |
| POST | `/seasons` | Admin | Create season |
| GET | `/seasons/league/{leagueId}` | Auth | List seasons |
| GET | `/seasons/{id}` | Auth | Get season |
| POST | `/seasons/{id}/open` | Admin | Open season |
| POST | `/seasons/{id}/close` | Admin | Close season |
| POST | `/seasons/{id}/teams` | Admin | Enroll team |
| DELETE | `/seasons/{id}/teams/{teamId}` | Admin | Remove team |
| GET | `/seasons/{id}/teams` | Auth | List enrolled teams |
| GET | `/seasons/{id}/standings` | Auth | Get standings |
| POST | `/seasons/{id}/publish-result` | Admin | Publish final standings |
| POST | `/teams` | Admin | Create team |
| GET | `/teams` | Auth | List teams (search: `?q=`) |
| GET | `/teams/{id}` | Auth | Get team |
| PUT | `/teams/{id}` | Admin | Update team |
| DELETE | `/teams/{id}` | Admin | Delete team |
| POST | `/players` | Admin | Create player |
| GET | `/players/{id}` | Auth | Get player |
| GET | `/players/team/{teamId}` | Auth | Players by team |
| PUT | `/players/{id}` | Admin | Update player |
| DELETE | `/players/{id}` | Admin | Delete player |
| POST | `/matches` | Admin | Schedule match |
| GET | `/matches/season/{seasonId}` | Auth | List matches |
| GET | `/matches/{id}` | Auth | Get match |
| PUT | `/matches/{id}` | Admin | Update match |
| DELETE | `/matches/{id}` | Admin | Delete match |
| POST | `/results/matches` | Admin | Publish match result |
| GET | `/results/matches/{matchId}` | Auth | Get match result |
| POST | `/predictions/matches/{matchId}` | Auth | Submit match prediction |
| GET | `/predictions/matches/{matchId}/me` | Auth | My match prediction |
| GET | `/predictions/matches/{matchId}` | Auth | All predictions (after lock) |
| GET | `/predictions/matches/{matchId}/head-to-head` | Auth | Head-to-head (after lock) |
| POST | `/predictions/seasons/{seasonId}/league` | Auth | Submit league predictions |
| GET | `/predictions/seasons/{seasonId}/league/me` | Auth | My league predictions |
| GET | `/predictions/seasons/{seasonId}/league` | Auth | All league predictions (after lock) |
| GET | `/leaderboard/seasons/{seasonId}` | Auth | Leaderboard |
| GET | `/leaderboard/seasons/{seasonId}/me` | Auth | My rank + breakdown |
| POST | `/leaderboard/seasons/{seasonId}/recalculate` | Admin | Recalculate leaderboard |
| GET | `/notifications` | Admin | Email logs |
| GET | `/notifications/me` | Auth | My notifications |
| POST | `/notifications/bulk` | Admin | Bulk notification |
| GET | `/config` | Admin | List config |
| PUT | `/config/{key}` | Admin | Update config |

All endpoints are prefixed with `/api/v1`.
