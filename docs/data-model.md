# Data Model

## Entity Relationship Overview

```
users ──< match_predictions >── matches ──< match_results
  │              │                  │
  │              │                  ├── season_id → seasons ── league_id → leagues
  │              │                  ├── home_team_id → teams ──< players
  │              │                  └── away_team_id → teams
  │              │
  ├──< league_predictions >── seasons
  │                              │
  │                              └──< season_teams >── teams
  │
  ├──< match_score_details
  ├──< season_score_details
  ├──< user_season_scores (leaderboard)
  └──< email_notifications

league_standings ── seasons + teams

app_config (standalone key-value)
```

## Tables

### Core Domain
| Table | Description | Soft Delete | Key Relationships |
|---|---|---|---|
| `users` | Platform users (admin + regular) | Yes | - |
| `leagues` | Sports leagues (e.g., IPL) | Yes | Has many seasons |
| `seasons` | Season within a league | Yes | Belongs to league, has teams + matches |
| `teams` | Sports teams | Yes | Has many players |
| `season_teams` | Team enrollment in season (join) | Yes | Season + Team |
| `players` | Team players | Yes | Belongs to team |
| `matches` | Scheduled matches | Yes | Belongs to season, has home/away teams |
| `match_results` | Published match outcomes | Yes | One-to-one with match |

### Predictions
| Table | Description | Key Constraints |
|---|---|---|
| `match_predictions` | User match predictions (winner, toss, POTM) | UNIQUE(match_id, user_id) |
| `league_predictions` | User season predictions (team positions) | UNIQUE(season_id, user_id, team_id), UNIQUE(season_id, user_id, predicted_position) |

### Scoring
| Table | Description | Aggregation |
|---|---|---|
| `match_score_details` | Per-user per-match score breakdown | UNIQUE(match_id, user_id) |
| `season_score_details` | Per-user per-team position score | UNIQUE(season_id, user_id, team_id) |
| `user_season_scores` | Aggregated leaderboard source | UNIQUE(user_id, season_id) |

### Supporting
| Table | Description |
|---|---|
| `league_standings` | Team W-D-L records within season |
| `email_notifications` | Email log with retry support |
| `app_config` | Runtime-tunable configuration (no soft delete) |

## Audit Columns (all tables except app_config)
- `created_at` (TIMESTAMPTZ, NOT NULL, DEFAULT now())
- `created_by` (UUID)
- `updated_at` (TIMESTAMPTZ)
- `updated_by` (UUID)
- `is_deleted` (BOOLEAN, NOT NULL, DEFAULT FALSE)
- `deleted_at` (TIMESTAMPTZ)
- `deleted_by` (UUID)

## Database Triggers
- `trg_enforce_match_prediction_lock` — Prevents match prediction writes after lock time
- `trg_enforce_league_prediction_lock` — Prevents league prediction writes after season lock
