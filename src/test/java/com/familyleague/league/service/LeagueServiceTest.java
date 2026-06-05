package com.familyleague.league.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import com.familyleague.auth.security.SecurityUser;
import com.familyleague.common.exception.ConflictException;
import com.familyleague.common.exception.ResourceNotFoundException;
import com.familyleague.league.dto.CreateLeagueRequest;
import com.familyleague.league.dto.LeagueResponse;
import com.familyleague.league.entity.League;
import com.familyleague.league.repository.LeagueRepository;

@ExtendWith(MockitoExtension.class)
class LeagueServiceTest {

    @Mock private LeagueRepository leagueRepository;
    @InjectMocks private LeagueServiceImpl leagueService;

    @Test
    void create_success() {
        when(leagueRepository.existsByName("Test")).thenReturn(false);
        when(leagueRepository.save(any())).thenAnswer(i -> {
            League l = i.getArgument(0);
            l.setId(UUID.randomUUID());
            l.setCreatedAt(Instant.now());
            return l;
        });

        LeagueResponse response = leagueService.create(new CreateLeagueRequest("Test", "Desc", "CRICKET"));
        assertThat(response.name()).isEqualTo("Test");
        assertThat(response.sportType()).isEqualTo("CRICKET");
    }

    @Test
    void create_duplicateName_throws() {
        when(leagueRepository.existsByName("Existing")).thenReturn(true);

        assertThatThrownBy(() -> leagueService.create(new CreateLeagueRequest("Existing", null, null)))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("already exists");
    }

    @Test
    void getById_notFound_throws() {
        when(leagueRepository.findById(any())).thenReturn(Optional.empty());
        assertThatThrownBy(() -> leagueService.getById(UUID.randomUUID()))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void delete_softDeletes() {
        League league = League.builder().name("Del").build();
        league.setId(UUID.randomUUID());

        try (MockedStatic<SecurityUser> mocked = mockStatic(SecurityUser.class)) {
            mocked.when(SecurityUser::currentUserId).thenReturn(UUID.randomUUID());
            when(leagueRepository.findById(any())).thenReturn(Optional.of(league));
            when(leagueRepository.save(any())).thenReturn(league);

            leagueService.delete(league.getId());
            assertThat(league.isDeleted()).isTrue();
        }
    }
}
