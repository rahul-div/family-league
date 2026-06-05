package com.familyleague.e2e;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * Complete end-to-end test: Admin setup -> User predictions -> Result -> Scoring -> Leaderboard
 */
class FullWorkflowE2ETest extends BaseE2ETest {

    @Test
    void completeHappyPathWorkflow() throws Exception {
        // 1. Admin logs in
        String adminToken = getAdminToken();
        assertThat(adminToken).isNotBlank();

        // 2. Create league
        ResponseEntity<String> leagueResp = post("/leagues",
                Map.of("name", "E2E Test League", "sportType", "CRICKET"), adminToken);
        assertThat(leagueResp.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        String leagueId = objectMapper.readTree(leagueResp.getBody()).path("data").path("id").asText();

        // 3. Create two teams
        String team1Id = objectMapper.readTree(
                post("/teams", Map.of("name", "E2E Alpha", "shortName", "ALP"), adminToken).getBody()
        ).path("data").path("id").asText();

        String team2Id = objectMapper.readTree(
                post("/teams", Map.of("name", "E2E Beta", "shortName", "BET"), adminToken).getBody()
        ).path("data").path("id").asText();

        // 4. Create a player per team
        String player1Id = objectMapper.readTree(
                post("/players", Map.of("teamId", team1Id, "name", "Player Alpha", "playerRole", "BATSMAN"), adminToken).getBody()
        ).path("data").path("id").asText();

        String player2Id = objectMapper.readTree(
                post("/players", Map.of("teamId", team2Id, "name", "Player Beta", "playerRole", "BOWLER"), adminToken).getBody()
        ).path("data").path("id").asText();

        // 5. Create season
        String seasonId = objectMapper.readTree(
                post("/seasons", Map.of("leagueId", leagueId, "name", "E2E Season 1"), adminToken).getBody()
        ).path("data").path("id").asText();

        // 6. Enroll both teams
        post("/seasons/" + seasonId + "/teams", Map.of("teamId", team1Id), adminToken);
        post("/seasons/" + seasonId + "/teams", Map.of("teamId", team2Id), adminToken);

        // 7. Verify enrolled teams
        ResponseEntity<String> teamsResp = get("/seasons/" + seasonId + "/teams", adminToken);
        assertThat(teamsResp.getStatusCode()).isEqualTo(HttpStatus.OK);
        int teamCount = objectMapper.readTree(teamsResp.getBody()).path("data").size();
        assertThat(teamCount).isEqualTo(2);

        // 8. Schedule a match (30 days in future)
        Instant matchTime = Instant.now().plus(30, ChronoUnit.DAYS);
        String matchId = objectMapper.readTree(
                post("/matches", Map.of(
                        "seasonId", seasonId,
                        "homeTeamId", team1Id,
                        "awayTeamId", team2Id,
                        "matchNumber", 1,
                        "scheduledAt", matchTime.toString(),
                        "venue", "E2E Stadium"
                ), adminToken).getBody()
        ).path("data").path("id").asText();

        // 9. Open season
        ResponseEntity<String> openResp = post("/seasons/" + seasonId + "/open", "", adminToken);
        assertThat(openResp.getStatusCode()).isEqualTo(HttpStatus.OK);
        String seasonStatus = objectMapper.readTree(openResp.getBody()).path("data").path("status").asText();
        assertThat(seasonStatus).isEqualTo("PREDICTION_OPEN");

        // 10. Register a user and submit prediction
        String userToken = registerAndGetToken("e2eworkflow", "e2eworkflow@test.com", "Password@1");

        ResponseEntity<String> predResp = post("/predictions/matches/" + matchId,
                Map.of("predictedWinnerTeamId", team1Id,
                        "predictedTossWinnerTeamId", team2Id,
                        "predictedPlayerOfMatchId", player1Id),
                userToken);
        assertThat(predResp.getStatusCode()).isEqualTo(HttpStatus.OK);

        // 11. Verify user can get their prediction
        ResponseEntity<String> myPred = get("/predictions/matches/" + matchId + "/me", userToken);
        assertThat(myPred.getStatusCode()).isEqualTo(HttpStatus.OK);
        String predictedWinner = objectMapper.readTree(myPred.getBody()).path("data").path("predictedWinnerTeamId").asText();
        assertThat(predictedWinner).isEqualTo(team1Id);

        // 12. RBAC: user cannot create league
        int forbiddenStatus = getStatus("POST", "/leagues", userToken, Map.of("name", "Forbidden"));
        assertThat(forbiddenStatus).isEqualTo(403);

        // 13. Admin publishes match result (team2 wins)
        ResponseEntity<String> resultResp = post("/results/matches",
                Map.of("matchId", matchId,
                        "winnerTeamId", team2Id,
                        "tossWinnerTeamId", team2Id,
                        "playerOfMatchId", player1Id,
                        "tie", false),
                adminToken);
        assertThat(resultResp.getStatusCode()).isEqualTo(HttpStatus.OK);

        // 14. Verify match result
        ResponseEntity<String> getResult = get("/results/matches/" + matchId, userToken);
        assertThat(getResult.getStatusCode()).isEqualTo(HttpStatus.OK);
        String winnerName = objectMapper.readTree(getResult.getBody()).path("data").path("winnerTeamName").asText();
        assertThat(winnerName).isEqualTo("E2E Beta");

        // 15. Verify standings
        ResponseEntity<String> standings = get("/seasons/" + seasonId + "/standings", userToken);
        assertThat(standings.getStatusCode()).isEqualTo(HttpStatus.OK);

        // 16. Recalculate leaderboard
        post("/leaderboard/seasons/" + seasonId + "/recalculate", "", adminToken);
        Thread.sleep(2000);

        // 17. Get leaderboard
        ResponseEntity<String> leaderboard = get("/leaderboard/seasons/" + seasonId, userToken);
        assertThat(leaderboard.getStatusCode()).isEqualTo(HttpStatus.OK);

        // 18. Duplicate result should fail
        int dupStatus = getStatus("POST", "/results/matches", adminToken,
                Map.of("matchId", matchId, "winnerTeamId", team1Id, "tie", false));
        assertThat(dupStatus).isEqualTo(409);

        // 19. Verify email notifications exist
        ResponseEntity<String> notifs = get("/notifications", adminToken);
        assertThat(notifs.getStatusCode()).isEqualTo(HttpStatus.OK);
    }
}
