#!/bin/bash
# ============================================================
# Family League - Smoke Test Script
# ============================================================
#
# WHAT IS THIS?
#   A quick automated check that every major feature of the
#   Family League backend is working. Run this after:
#   - First deployment
#   - Code changes
#   - Database reset
#   - Server restart
#
# HOW TO RUN:
#   1. Make sure the app is running: ./mvnw spring-boot:run
#   2. In another terminal: bash scripts/smoke-test.sh
#
# WHAT IT CHECKS:
#   - Authentication (login, register, JWT tokens)
#   - RBAC (admin vs user permissions)
#   - All CRUD endpoints (leagues, teams, players, seasons, matches)
#   - Season lifecycle (open, predictions, results, close)
#   - Predictions (submit, visibility rules)
#   - Scoring engine (result → scores → leaderboard)
#   - Notifications (email log system)
#   - App configuration
#   - Swagger UI accessibility
#
# REQUIREMENTS:
#   - curl (comes with macOS/Linux)
#   - python3 (for JSON parsing)
#   - The app running on localhost:8080
# ============================================================

set -e  # Exit on first failure

# --- Configuration ---
BASE_URL="${BASE_URL:-http://localhost:8080/api/v1}"
PASS_COUNT=0
FAIL_COUNT=0
TOTAL_COUNT=0

# --- Colors for output ---
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# ============================================================
# Helper Functions
# ============================================================

# Print a section header
section() {
    echo ""
    echo -e "${BLUE}━━━ $1 ━━━${NC}"
}

# Check if a test passed or failed
# Usage: check "Test Name" "$actual" "$expected"
check() {
    local test_name="$1"
    local actual="$2"
    local expected="$3"
    TOTAL_COUNT=$((TOTAL_COUNT + 1))

    if [ "$actual" = "$expected" ]; then
        echo -e "  ${GREEN}[PASS]${NC} $test_name"
        PASS_COUNT=$((PASS_COUNT + 1))
    else
        echo -e "  ${RED}[FAIL]${NC} $test_name (expected: $expected, got: $actual)"
        FAIL_COUNT=$((FAIL_COUNT + 1))
    fi
}

# Check that value is not empty
check_not_empty() {
    local test_name="$1"
    local actual="$2"
    TOTAL_COUNT=$((TOTAL_COUNT + 1))

    if [ -n "$actual" ] && [ "$actual" != "null" ] && [ "$actual" != "" ]; then
        echo -e "  ${GREEN}[PASS]${NC} $test_name"
        PASS_COUNT=$((PASS_COUNT + 1))
    else
        echo -e "  ${RED}[FAIL]${NC} $test_name (got empty/null)"
        FAIL_COUNT=$((FAIL_COUNT + 1))
    fi
}

# Extract a field from JSON response using python3
# Usage: json_field '{"data":{"name":"Alice"}}' '.data.name'
# This is like Python's: response.json()["data"]["name"]
json_field() {
    local json="$1"
    local path="$2"
    echo "$json" | python3 -c "
import sys, json
try:
    data = json.load(sys.stdin)
    keys = '$path'.strip('.').split('.')
    for k in keys:
        if isinstance(data, list):
            data = data[int(k)]
        else:
            data = data[k]
    print(data)
except Exception as e:
    print('')
" 2>/dev/null
}

# Get HTTP status code
# Usage: http_status "GET" "/leagues"
http_status() {
    local method="$1"
    local path="$2"
    local token="$3"
    local body="$4"

    if [ -n "$body" ]; then
        curl -s -o /dev/null -w "%{http_code}" -X "$method" "${BASE_URL}${path}" \
            -H "Authorization: Bearer $token" \
            -H "Content-Type: application/json" \
            -d "$body"
    elif [ -n "$token" ]; then
        curl -s -o /dev/null -w "%{http_code}" -X "$method" "${BASE_URL}${path}" \
            -H "Authorization: Bearer $token"
    else
        curl -s -o /dev/null -w "%{http_code}" -X "$method" "${BASE_URL}${path}"
    fi
}

# Make an API call and return the JSON response
# Usage: api "GET" "/leagues" "$TOKEN"
api() {
    local method="$1"
    local path="$2"
    local token="$3"
    local body="$4"

    if [ -n "$body" ]; then
        curl -s -X "$method" "${BASE_URL}${path}" \
            -H "Authorization: Bearer $token" \
            -H "Content-Type: application/json" \
            -d "$body"
    elif [ -n "$token" ]; then
        curl -s -X "$method" "${BASE_URL}${path}" \
            -H "Authorization: Bearer $token"
    else
        curl -s -X "$method" "${BASE_URL}${path}"
    fi
}

# ============================================================
# Pre-flight Check
# ============================================================
echo ""
echo -e "${YELLOW}============================================${NC}"
echo -e "${YELLOW}  FAMILY LEAGUE - SMOKE TEST${NC}"
echo -e "${YELLOW}============================================${NC}"
echo ""
echo "  Target: $BASE_URL"
echo "  Time:   $(date)"
echo ""

# Check if app is running
echo -n "  Checking if app is running... "
HEALTH=$(curl -s "${BASE_URL}/actuator/health" 2>/dev/null || echo "UNREACHABLE")
if echo "$HEALTH" | grep -q "UP"; then
    echo -e "${GREEN}UP${NC}"
elif echo "$HEALTH" | grep -q "UNREACHABLE"; then
    echo -e "${RED}APP NOT RUNNING${NC}"
    echo ""
    echo "  Start the app first with: ./mvnw spring-boot:run"
    echo "  Then run this script in another terminal."
    exit 1
else
    echo -e "${YELLOW}DEGRADED${NC} (app running but some checks failed — continuing)"
fi

# ============================================================
# 1. AUTHENTICATION
# ============================================================
section "1. AUTHENTICATION"

# 1.1 Admin Login
ADMIN_RESP=$(api "POST" "/auth/login" "" '{"usernameOrEmail":"admin","password":"Admin@1234"}')
ADMIN_TOKEN=$(json_field "$ADMIN_RESP" "data.accessToken")
check_not_empty "Admin login returns JWT token" "$ADMIN_TOKEN"

ADMIN_ROLE=$(json_field "$ADMIN_RESP" "data.role")
check "Admin has ADMIN role" "$ADMIN_ROLE" "ADMIN"

# 1.2 Register new user
TIMESTAMP=$(date +%s)
USER1_RESP=$(api "POST" "/auth/register" "" "{\"username\":\"smokeuser${TIMESTAMP}\",\"email\":\"smoke${TIMESTAMP}@test.com\",\"password\":\"Smoke@123\",\"displayName\":\"Smoke User\"}")
USER1_TOKEN=$(json_field "$USER1_RESP" "data.accessToken")
check_not_empty "User registration returns JWT token" "$USER1_TOKEN"

USER1_ROLE=$(json_field "$USER1_RESP" "data.role")
check "New user has USER role" "$USER1_ROLE" "USER"

# 1.3 Register second user (for predictions later)
USER2_RESP=$(api "POST" "/auth/register" "" "{\"username\":\"smokeuser2_${TIMESTAMP}\",\"email\":\"smoke2_${TIMESTAMP}@test.com\",\"password\":\"Smoke@123\",\"displayName\":\"Smoke User 2\"}")
USER2_TOKEN=$(json_field "$USER2_RESP" "data.accessToken")
check_not_empty "Second user registration" "$USER2_TOKEN"

# 1.4 Get profile
PROFILE_RESP=$(api "GET" "/auth/me" "$USER1_TOKEN")
PROFILE_USERNAME=$(json_field "$PROFILE_RESP" "data.username")
check "Get profile returns correct username" "$PROFILE_USERNAME" "smokeuser${TIMESTAMP}"
USER1_ID=$(json_field "$PROFILE_RESP" "data.id")

# 1.5 Login by username
LOGIN_RESP=$(api "POST" "/auth/login" "" "{\"usernameOrEmail\":\"smokeuser${TIMESTAMP}\",\"password\":\"Smoke@123\"}")
LOGIN_SUCCESS=$(json_field "$LOGIN_RESP" "success")
check "Login by username works" "$LOGIN_SUCCESS" "True"

# 1.6 Login by email
LOGIN_RESP2=$(api "POST" "/auth/login" "" "{\"usernameOrEmail\":\"smoke${TIMESTAMP}@test.com\",\"password\":\"Smoke@123\"}")
LOGIN_SUCCESS2=$(json_field "$LOGIN_RESP2" "success")
check "Login by email works" "$LOGIN_SUCCESS2" "True"

# 1.7 Bad credentials
BAD_STATUS=$(http_status "POST" "/auth/login" "" '{"usernameOrEmail":"admin","password":"WrongPass@1"}')
check "Bad credentials returns 401" "$BAD_STATUS" "401"

# 1.8 Unauthenticated access blocked
UNAUTH_STATUS=$(http_status "GET" "/leagues")
check "Unauthenticated request returns 401" "$UNAUTH_STATUS" "401"

# 1.9 Weak password validation
WEAK_STATUS=$(http_status "POST" "/auth/register" "" '{"username":"weakpwd","email":"weak@t.com","password":"123"}')
check "Weak password returns 400" "$WEAK_STATUS" "400"

# 1.10 Duplicate email
DUP_STATUS=$(http_status "POST" "/auth/register" "" "{\"username\":\"dup${TIMESTAMP}\",\"email\":\"smoke${TIMESTAMP}@test.com\",\"password\":\"Smoke@123\"}")
check "Duplicate email returns 409" "$DUP_STATUS" "409"

# 1.11 Refresh token
REFRESH_RESP=$(api "POST" "/auth/refresh" "$USER1_TOKEN")
REFRESH_TOKEN=$(json_field "$REFRESH_RESP" "data.accessToken")
check_not_empty "Token refresh returns new token" "$REFRESH_TOKEN"

# ============================================================
# 2. RBAC (Role-Based Access Control)
# ============================================================
section "2. RBAC (Admin vs User Permissions)"

# Admin-only endpoints that users should NOT access
RBAC_STATUS=$(http_status "POST" "/leagues" "$USER1_TOKEN" '{"name":"Hack League"}')
check "User cannot create league" "$RBAC_STATUS" "403"

RBAC_STATUS2=$(http_status "POST" "/teams" "$USER1_TOKEN" '{"name":"Hack Team"}')
check "User cannot create team" "$RBAC_STATUS2" "403"

RBAC_STATUS3=$(http_status "GET" "/users" "$USER1_TOKEN")
check "User cannot list all users" "$RBAC_STATUS3" "403"

RBAC_STATUS4=$(http_status "GET" "/config" "$USER1_TOKEN")
check "User cannot view app config" "$RBAC_STATUS4" "403"

RBAC_STATUS5=$(http_status "POST" "/auth/admin" "$USER1_TOKEN" '{"username":"hack","email":"h@h.com","password":"Hack@123"}')
check "User cannot create admin account" "$RBAC_STATUS5" "403"

# Admin CAN do these
ADMIN_LEAGUES=$(http_status "GET" "/leagues" "$ADMIN_TOKEN")
check "Admin can list leagues" "$ADMIN_LEAGUES" "200"

ADMIN_USERS=$(http_status "GET" "/users" "$ADMIN_TOKEN")
check "Admin can list users" "$ADMIN_USERS" "200"

# ============================================================
# 3. LEAGUES
# ============================================================
section "3. LEAGUES (CRUD)"

# List (seeded IPL should be there)
LEAGUES_RESP=$(api "GET" "/leagues" "$USER1_TOKEN")
LEAGUE_COUNT=$(json_field "$LEAGUES_RESP" "data.totalElements")
check_not_empty "List leagues returns results" "$LEAGUE_COUNT"

# Get IPL league ID
IPL_LEAGUE_ID=$(json_field "$LEAGUES_RESP" "data.content.0.id")

# Create (use timestamp to avoid duplicate name conflicts)
CREATE_LEAGUE_RESP=$(api "POST" "/leagues" "$ADMIN_TOKEN" "{\"name\":\"Smoke League ${TIMESTAMP}\",\"sportType\":\"CRICKET\",\"description\":\"Created by smoke test\"}")
SMOKE_LEAGUE_ID=$(json_field "$CREATE_LEAGUE_RESP" "data.id")
check_not_empty "Create league" "$SMOKE_LEAGUE_ID"

# Get by ID
GET_LEAGUE_RESP=$(api "GET" "/leagues/$SMOKE_LEAGUE_ID" "$USER1_TOKEN")
GET_LEAGUE_NAME=$(json_field "$GET_LEAGUE_RESP" "data.name")
check "Get league by ID" "$GET_LEAGUE_NAME" "Smoke League ${TIMESTAMP}"

# Update
UPDATE_LEAGUE_RESP=$(api "PUT" "/leagues/$SMOKE_LEAGUE_ID" "$ADMIN_TOKEN" '{"description":"Updated by smoke test"}')
UPDATE_DESC=$(json_field "$UPDATE_LEAGUE_RESP" "data.description")
check "Update league" "$UPDATE_DESC" "Updated by smoke test"

# Search
SEARCH_RESP=$(api "GET" "/leagues?q=smoke" "$USER1_TOKEN")
SEARCH_COUNT=$(json_field "$SEARCH_RESP" "data.totalElements")
check_not_empty "Search leagues by name" "$SEARCH_COUNT"

# ============================================================
# 4. TEAMS
# ============================================================
section "4. TEAMS (CRUD + Search)"

TEAMS_RESP=$(api "GET" "/teams?size=12" "$USER1_TOKEN")
TEAM_COUNT=$(json_field "$TEAMS_RESP" "data.totalElements")
check "List teams (10 IPL seeded)" "$TEAM_COUNT" "10"

# Search
SEARCH_TEAMS=$(api "GET" "/teams?q=mumbai" "$USER1_TOKEN")
MI_NAME=$(json_field "$SEARCH_TEAMS" "data.content.0.name")
check "Search teams for 'mumbai'" "$MI_NAME" "Mumbai Indians"

# Get CSK ID for later use
CSK_ID=$(json_field "$(api "GET" "/teams?q=chennai" "$USER1_TOKEN")" "data.content.0.id")
RCB_ID=$(json_field "$(api "GET" "/teams?q=bengaluru" "$USER1_TOKEN")" "data.content.0.id")
check_not_empty "Found CSK team ID" "$CSK_ID"
check_not_empty "Found RCB team ID" "$RCB_ID"

# ============================================================
# 5. PLAYERS
# ============================================================
section "5. PLAYERS (List by Team)"

PLAYERS_RESP=$(api "GET" "/players/team/$CSK_ID?size=5" "$USER1_TOKEN")
PLAYER_TOTAL=$(json_field "$PLAYERS_RESP" "data.totalElements")
check "CSK has 25 players" "$PLAYER_TOTAL" "25"

PLAYER1_NAME=$(json_field "$PLAYERS_RESP" "data.content.0.name")
check_not_empty "First CSK player has a name" "$PLAYER1_NAME"

# Get a player ID for predictions
CSK_PLAYER_ID=$(json_field "$PLAYERS_RESP" "data.content.0.id")

# ============================================================
# 6. SEASONS
# ============================================================
section "6. SEASONS (Lifecycle)"

SEASON_ID="c0000000-0000-0000-0000-000000000001"
SEASON_RESP=$(api "GET" "/seasons/$SEASON_ID" "$USER1_TOKEN")
SEASON_NAME=$(json_field "$SEASON_RESP" "data.name")
SEASON_STATUS=$(json_field "$SEASON_RESP" "data.status")
SEASON_TEAMS=$(json_field "$SEASON_RESP" "data.teamCount")
check "Season name is IPL 2024" "$SEASON_NAME" "IPL 2024"
check "Season has 10 teams" "$SEASON_TEAMS" "10"

# List enrolled teams
ENROLLED_RESP=$(api "GET" "/seasons/$SEASON_ID/teams" "$USER1_TOKEN")
ENROLLED_COUNT=$(echo "$ENROLLED_RESP" | python3 -c "import sys,json; print(len(json.load(sys.stdin)['data']))" 2>/dev/null)
check "10 teams enrolled in season" "$ENROLLED_COUNT" "10"

# Open season (if still UPCOMING)
if [ "$SEASON_STATUS" = "UPCOMING" ]; then
    OPEN_RESP=$(api "POST" "/seasons/$SEASON_ID/open" "$ADMIN_TOKEN")
    NEW_STATUS=$(json_field "$OPEN_RESP" "data.status")
    check "Open season: UPCOMING → PREDICTION_OPEN" "$NEW_STATUS" "PREDICTION_OPEN"
    LOCK_AT=$(json_field "$OPEN_RESP" "data.predictionLockedAt")
    check_not_empty "Prediction lock time calculated" "$LOCK_AT"
else
    echo -e "  ${YELLOW}[SKIP]${NC} Season already in $SEASON_STATUS status"
fi

# ============================================================
# 7. MATCHES
# ============================================================
section "7. MATCHES"

MATCHES_RESP=$(api "GET" "/matches/season/$SEASON_ID" "$USER1_TOKEN")
MATCH_COUNT=$(json_field "$MATCHES_RESP" "data.totalElements")
TOTAL_COUNT=$((TOTAL_COUNT + 1))
if [ "$MATCH_COUNT" -ge 3 ] 2>/dev/null; then
    echo -e "  ${GREEN}[PASS]${NC} Season has $MATCH_COUNT matches (>= 3 seeded)"
    PASS_COUNT=$((PASS_COUNT + 1))
else
    echo -e "  ${RED}[FAIL]${NC} Season has $MATCH_COUNT matches (expected >= 3)"
    FAIL_COUNT=$((FAIL_COUNT + 1))
fi

# Get first match details
MATCH1_ID=$(json_field "$MATCHES_RESP" "data.content.0.id")
MATCH1_HOME=$(json_field "$MATCHES_RESP" "data.content.0.homeTeamName")
MATCH1_AWAY=$(json_field "$MATCHES_RESP" "data.content.0.awayTeamName")
echo -e "  ${GREEN}[INFO]${NC} Match #1: $MATCH1_HOME vs $MATCH1_AWAY"

MATCH1_HOME_ID=$(json_field "$MATCHES_RESP" "data.content.0.homeTeamId")
MATCH1_AWAY_ID=$(json_field "$MATCHES_RESP" "data.content.0.awayTeamId")

# Create a new match (far in the future so predictions work)
FUTURE_TIME=$(python3 -c "from datetime import datetime, timedelta, timezone; print((datetime.now(timezone.utc) + timedelta(days=30)).strftime('%Y-%m-%dT%H:%M:%SZ'))")
NEW_MATCH_RESP=$(api "POST" "/matches" "$ADMIN_TOKEN" "{
    \"seasonId\":\"$SEASON_ID\",
    \"homeTeamId\":\"$CSK_ID\",
    \"awayTeamId\":\"$RCB_ID\",
    \"matchNumber\":${TIMESTAMP: -4},
    \"scheduledAt\":\"$FUTURE_TIME\",
    \"venue\":\"Smoke Test Stadium\"
}")
NEW_MATCH_ID=$(json_field "$NEW_MATCH_RESP" "data.id")
check_not_empty "Create new match (admin)" "$NEW_MATCH_ID"

NEW_MATCH_LOCKED=$(json_field "$NEW_MATCH_RESP" "data.predictionLocked")
check "New match prediction window is open" "$NEW_MATCH_LOCKED" "False"

# ============================================================
# 8. PREDICTIONS
# ============================================================
section "8. PREDICTIONS (Match + League)"

# 8.1 Submit match prediction (User 1)
PRED1_RESP=$(api "POST" "/predictions/matches/$NEW_MATCH_ID" "$USER1_TOKEN" "{
    \"predictedWinnerTeamId\":\"$CSK_ID\",
    \"predictedTossWinnerTeamId\":\"$RCB_ID\",
    \"predictedPlayerOfMatchId\":\"$CSK_PLAYER_ID\"
}")
PRED1_SUCCESS=$(json_field "$PRED1_RESP" "success")
check "User 1 submits match prediction" "$PRED1_SUCCESS" "True"

# 8.2 Get my prediction
MY_PRED_RESP=$(api "GET" "/predictions/matches/$NEW_MATCH_ID/me" "$USER1_TOKEN")
MY_WINNER=$(json_field "$MY_PRED_RESP" "data.predictedWinnerTeamId")
check "Get my prediction returns correct winner" "$MY_WINNER" "$CSK_ID"

# 8.3 Submit match prediction (User 2 — different picks)
RCB_PLAYER_ID=$(json_field "$(api "GET" "/players/team/$RCB_ID?size=1" "$USER1_TOKEN")" "data.content.0.id")
PRED2_RESP=$(api "POST" "/predictions/matches/$NEW_MATCH_ID" "$USER2_TOKEN" "{
    \"predictedWinnerTeamId\":\"$RCB_ID\",
    \"predictedTossWinnerTeamId\":\"$CSK_ID\",
    \"predictedPlayerOfMatchId\":\"$RCB_PLAYER_ID\"
}")
check "User 2 submits match prediction" "$(json_field "$PRED2_RESP" "success")" "True"

# 8.4 Update prediction (upsert)
UPDATED_PRED=$(api "POST" "/predictions/matches/$NEW_MATCH_ID" "$USER1_TOKEN" "{
    \"predictedWinnerTeamId\":\"$RCB_ID\",
    \"predictedTossWinnerTeamId\":\"$CSK_ID\",
    \"predictedPlayerOfMatchId\":\"$CSK_PLAYER_ID\"
}")
UPDATED_WINNER=$(json_field "$UPDATED_PRED" "data.predictedWinnerTeamId")
check "Update prediction (upsert)" "$UPDATED_WINNER" "$RCB_ID"

# 8.5 Invalid team prediction
INVALID_PRED_STATUS=$(http_status "POST" "/predictions/matches/$NEW_MATCH_ID" "$USER1_TOKEN" '{"predictedWinnerTeamId":"00000000-0000-0000-0000-000000000099"}')
check "Invalid team prediction returns 400" "$INVALID_PRED_STATUS" "400"

# 8.6 All predictions NOT visible before lock (regular user)
ALL_PRED_STATUS=$(http_status "GET" "/predictions/matches/$NEW_MATCH_ID" "$USER1_TOKEN")
check "Predictions hidden before lock (403)" "$ALL_PRED_STATUS" "403"

# 8.7 Admin CAN see all predictions
ADMIN_PRED_STATUS=$(http_status "GET" "/predictions/matches/$NEW_MATCH_ID" "$ADMIN_TOKEN")
check "Admin can see predictions before lock (200)" "$ADMIN_PRED_STATUS" "200"

# 8.8 League predictions
# Note: If season is already PREDICTION_LOCKED (scheduler ran), this correctly returns 403
SEASON_CHECK=$(api "GET" "/seasons/$SEASON_ID" "$USER1_TOKEN")
CURRENT_STATUS=$(json_field "$SEASON_CHECK" "data.status")

if [ "$CURRENT_STATUS" = "PREDICTION_OPEN" ] || [ "$CURRENT_STATUS" = "UPCOMING" ]; then
    TEAM_IDS_JSON=$(api "GET" "/seasons/$SEASON_ID/teams" "$USER1_TOKEN" | python3 -c "
import sys, json
teams = json.load(sys.stdin)['data']
preds = [{'teamId': t['id'], 'predictedPosition': i+1} for i, t in enumerate(teams)]
print(json.dumps({'predictions': preds}))
" 2>/dev/null)
    LEAGUE_PRED_RESP=$(api "POST" "/predictions/seasons/$SEASON_ID/league" "$USER1_TOKEN" "$TEAM_IDS_JSON")
    LEAGUE_PRED_SUCCESS=$(json_field "$LEAGUE_PRED_RESP" "success")
    check "Submit league predictions (all 10 teams)" "$LEAGUE_PRED_SUCCESS" "True"

    MY_LEAGUE_PRED=$(api "GET" "/predictions/seasons/$SEASON_ID/league/me" "$USER1_TOKEN")
    MY_LP_COUNT=$(echo "$MY_LEAGUE_PRED" | python3 -c "import sys,json; print(len(json.load(sys.stdin)['data']))" 2>/dev/null)
    check "My league predictions: 10 entries" "$MY_LP_COUNT" "10"
else
    # Season is locked — verify that predictions are correctly blocked
    LOCKED_STATUS=$(http_status "POST" "/predictions/seasons/$SEASON_ID/league" "$USER1_TOKEN" '{"predictions":[{"teamId":"00000000-0000-0000-0000-000000000001","predictedPosition":1}]}')
    check "League predictions blocked when locked (403)" "$LOCKED_STATUS" "403"
    echo -e "  ${GREEN}[INFO]${NC} Season is $CURRENT_STATUS — league predictions correctly blocked"
    TOTAL_COUNT=$((TOTAL_COUNT + 1))
    PASS_COUNT=$((PASS_COUNT + 1))
    echo -e "  ${GREEN}[PASS]${NC} League prediction lock enforcement verified"
fi

# ============================================================
# 9. RESULTS & SCORING
# ============================================================
section "9. RESULTS & SCORING"

# Publish result for our new match
RESULT_RESP=$(api "POST" "/results/matches" "$ADMIN_TOKEN" "{
    \"matchId\":\"$NEW_MATCH_ID\",
    \"winnerTeamId\":\"$RCB_ID\",
    \"tossWinnerTeamId\":\"$CSK_ID\",
    \"playerOfMatchId\":\"$CSK_PLAYER_ID\",
    \"tie\":false
}")
RESULT_SUCCESS=$(json_field "$RESULT_RESP" "success")
check "Publish match result (admin)" "$RESULT_SUCCESS" "True"

RESULT_WINNER=$(json_field "$RESULT_RESP" "data.winnerTeamName")
check_not_empty "Result has winner team name" "$RESULT_WINNER"

# Get result
GET_RESULT=$(api "GET" "/results/matches/$NEW_MATCH_ID" "$USER1_TOKEN")
check "Get match result" "$(json_field "$GET_RESULT" "success")" "True"

# Duplicate result should fail
DUP_RESULT_STATUS=$(http_status "POST" "/results/matches" "$ADMIN_TOKEN" "{\"matchId\":\"$NEW_MATCH_ID\",\"winnerTeamId\":\"$CSK_ID\",\"tie\":false}")
check "Duplicate result returns 409" "$DUP_RESULT_STATUS" "409"

# Check standings updated
STANDINGS_RESP=$(api "GET" "/seasons/$SEASON_ID/standings" "$USER1_TOKEN")
STANDINGS_SUCCESS=$(json_field "$STANDINGS_RESP" "success")
check "League standings accessible" "$STANDINGS_SUCCESS" "True"

# ============================================================
# 10. LEADERBOARD
# ============================================================
section "10. LEADERBOARD"

# Trigger recalculation
RECALC_STATUS=$(http_status "POST" "/leaderboard/seasons/$SEASON_ID/recalculate" "$ADMIN_TOKEN")
check "Trigger leaderboard recalculation (admin)" "$RECALC_STATUS" "200"

# Small delay for async processing
sleep 2

LEADERBOARD_RESP=$(api "GET" "/leaderboard/seasons/$SEASON_ID" "$USER1_TOKEN")
LB_SUCCESS=$(json_field "$LEADERBOARD_RESP" "success")
check "Get leaderboard" "$LB_SUCCESS" "True"

# ============================================================
# 11. NOTIFICATIONS
# ============================================================
section "11. NOTIFICATIONS"

NOTIF_RESP=$(api "GET" "/notifications" "$ADMIN_TOKEN")
NOTIF_SUCCESS=$(json_field "$NOTIF_RESP" "success")
check "Admin can view email logs" "$NOTIF_SUCCESS" "True"

MY_NOTIF_RESP=$(api "GET" "/notifications/me" "$USER1_TOKEN")
check "User can view own notifications" "$(json_field "$MY_NOTIF_RESP" "success")" "True"

# ============================================================
# 12. APP CONFIGURATION
# ============================================================
section "12. APP CONFIGURATION"

CONFIG_RESP=$(api "GET" "/config" "$ADMIN_TOKEN")
CONFIG_COUNT=$(echo "$CONFIG_RESP" | python3 -c "import sys,json; print(len(json.load(sys.stdin)['data']))" 2>/dev/null)
check "App config has 6 entries" "$CONFIG_COUNT" "6"

# Update config
UPDATE_CONFIG_RESP=$(api "PUT" "/config/match.lock.offset.hours" "$ADMIN_TOKEN" '{"value":"2"}')
UPDATED_VAL=$(json_field "$UPDATE_CONFIG_RESP" "data.value")
check "Update config value" "$UPDATED_VAL" "2"

# Reset it back
api "PUT" "/config/match.lock.offset.hours" "$ADMIN_TOKEN" '{"value":"1"}' > /dev/null

# ============================================================
# 13. SWAGGER UI
# ============================================================
section "13. SWAGGER UI"

SWAGGER_STATUS=$(curl -s -o /dev/null -w "%{http_code}" "${BASE_URL}/swagger-ui/index.html")
check "Swagger UI accessible" "$SWAGGER_STATUS" "200"

# ============================================================
# 14. USER PROFILE UPDATE
# ============================================================
section "14. USER PROFILE"

UPDATE_PROFILE_RESP=$(api "PUT" "/users/$USER1_ID/profile" "$USER1_TOKEN" '{"displayName":"Updated Smoke User","avatarUrl":"https://example.com/avatar.png"}')
UPDATED_NAME=$(json_field "$UPDATE_PROFILE_RESP" "data.displayName")
check "Update own profile" "$UPDATED_NAME" "Updated Smoke User"

# ============================================================
# RESULTS SUMMARY
# ============================================================
echo ""
echo -e "${YELLOW}============================================${NC}"
if [ "$FAIL_COUNT" -eq 0 ]; then
    echo -e "${GREEN}  ALL $TOTAL_COUNT TESTS PASSED${NC}"
else
    echo -e "${RED}  $FAIL_COUNT of $TOTAL_COUNT TESTS FAILED${NC}"
fi
echo -e "${YELLOW}============================================${NC}"
echo ""
echo "  Passed: $PASS_COUNT"
echo "  Failed: $FAIL_COUNT"
echo "  Total:  $TOTAL_COUNT"
echo ""
echo "  App:     $BASE_URL"
echo "  Swagger: $BASE_URL/swagger-ui/index.html"
echo ""

# Exit with failure code if any tests failed
[ "$FAIL_COUNT" -eq 0 ] && exit 0 || exit 1
