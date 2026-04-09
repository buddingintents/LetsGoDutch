# R-04 - Ledger, Insights, and Settlement

Detailed milestone tracker for the Let's Go Dutch UI revamp.
Update this file first, then sync summary changes to [../LGD_UI_REVAMP_MASTER_PLAN.md](../LGD_UI_REVAMP_MASTER_PLAN.md).

| Field | Value |
|---|---|
| **Status** | `NOT_STARTED` |
| **Start Date** | 2026-04-24 |
| **Target End** | 2026-04-29 |
| **Actual End** | - |
| **Git Branch** | `revamp/r04-ledger-settlement` |
| **Merge Target** | `main` |
| **Prerequisites** | R-03 `COMPLETED` |

Formal milestone execution is still gated by `R-03`, but the current codebase already contains partial ledger and insights revamp work that should be counted as pre-milestone progress.

## In-Scope Files and Areas

- `feature/ledger/src/main/java/com/buddingintents/letsgodutch/feature/ledger/LedgerScreen.kt`
- `feature/insights/src/main/java/com/buddingintents/letsgodutch/feature/insights/InsightsScreen.kt`
- `feature/settlement/src/main/java/com/buddingintents/letsgodutch/feature/settlement/SettlementCard.kt`
- settlement preview route content currently embedded in `app/src/main/java/com/buddingintents/letsgodutch/LetsGoDutchApp.kt`
- group-detail tab shell inside `LetsGoDutchApp.kt`

## Deliverables Checklist

- [ ] Redesign the group-detail tab selector to match the new pill-based shell style
- [ ] Improve `LedgerScreen.kt` readability with clearer metadata hierarchy, member identity, and amount emphasis
- [ ] Redesign `InsightsScreen.kt` around a darker analytics surface and more visual storytelling
- [ ] Evaluate and, if approved, adopt a Compose-friendly chart library for insight cards
- [ ] Extract settlement preview UI from `LetsGoDutchApp.kt` into a dedicated feature-owned screen
- [ ] Redesign settlement preview to feel high-trust, progressive, and deliberate
- [ ] Replace the plain confirmation stack with clearer summary, transfer visualization, and stronger final-action states
- [ ] Retire or repurpose the currently unused `SettlementCard.kt` to avoid duplicate settlement surfaces

## Current Implementation Snapshot

- `LedgerScreen.kt` already contains clearer member identity, metadata grouping, and amount emphasis than the pre-revamp baseline.
- `InsightsScreen.kt` already contains summary cards, leader rows, and stronger balance storytelling than the original light admin-style layout.
- The group-detail shell in `LetsGoDutchApp.kt` still uses a stock `TabRow`, so the milestone tab treatment is not done.
- Settlement preview still lives inline in `LetsGoDutchApp.kt`, and `SettlementCard.kt` remains unused.
- Treat the existing ledger and insights polish as input to this milestone, not as evidence that the milestone is complete or ready to review.

## Testing Gate

- [ ] Ledger tab still supports delete affordances and dense group histories without truncation failures
- [ ] Insights tab remains readable with realistic positive, negative, and near-zero balance mixes
- [ ] Settlement preview still generates PDF, dispatches it, and marks the group settled successfully
- [ ] Ownership rules remain intact: non-owners cannot finalize settlement
- [ ] The new settlement route compiles without inline UI logic left stranded in `LetsGoDutchApp.kt`

## Rollback Plan

If the analytics or settlement redesign slows delivery too much, prioritize extracting the settlement route and restyling the existing logic first, then layer charts and advanced visuals afterward.

## Error Tracker

| ERR_ID | Reported Date | Component | Error Description | Root Cause Analysis | Proposed Fix | Fix Date | Status |
|---|---|---|---|---|---|---|---|
| *(none logged)* | | | | | | | |

## Improvement Suggestion Tracker

| SUG_ID | Suggestion Date | Suggested By | Description | Proposed Changes | Approval Status | Approval Date | Implemented | Notes |
|---|---|---|---|---|---|---|---|---|
| SUG-REV-003 | 2026-04-08 | Codex | Evaluate a Compose-native chart library before building custom analytics visuals | Trial Vico for pie and trend visualizations, then keep or reject it based on integration cost and visual payoff | `UNDER_REVIEW` | - | NO | Mirrors cross-milestone tracker |
| SUG-REV-009 | 2026-04-08 | User | Add banner ad slots to ledger, insights, and settlement preview | Introduce banner containers on those three surfaces with layout-safe spacing, keeping primary financial actions readable and reachable | `APPROVED` | 2026-04-08 | NO | Implementation belongs with the ledger/insights/settlement milestone; final density review should still be revisited in R-05 polish. |
| SUG-REV-010 | 2026-04-08 | User | Gate `Mark as settled` into settlement preview behind a completed rewarded fullscreen video | Show a video-only rewarded ad on `Mark as settled`; only navigate to settlement preview after the reward-completion callback confirms full watch | `APPROVED` | 2026-04-08 | NO | Treat this as a monetization-sensitive settlement-flow change, not a cosmetic add-on. Keep ownership and failure states explicit. |
