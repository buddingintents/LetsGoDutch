# R-02 - First Impressions and Navigation

Detailed milestone tracker for the Let's Go Dutch UI revamp.
Update this file first, then sync summary changes to [../LGD_UI_REVAMP_MASTER_PLAN.md](../LGD_UI_REVAMP_MASTER_PLAN.md).

| Field | Value |
|---|---|
| **Status** | `COMPLETED` |
| **Start Date** | 2026-04-08 |
| **Target End** | 2026-04-17 |
| **Actual End** | 2026-04-09 |
| **Git Branch** | `revamp/r02-first-impressions` |
| **Merge Target** | `main` |
| **Prerequisites** | R-01 `COMPLETED` |

## In-Scope Files and Areas

- `feature/auth/src/main/java/com/buddingintents/letsgodutch/feature/auth/AuthScreen.kt`
- `feature/groups/src/main/java/com/buddingintents/letsgodutch/feature/groups/GroupsListScreen.kt`
- `feature/groups/src/main/java/com/buddingintents/letsgodutch/feature/groups/GroupsScreen.kt`
- extracted app tour file from `app/ui/`
- create and join group entry points invoked from the app shell

## Deliverables Checklist

- [x] Redesign `AuthScreen.kt` to improve brand impression, spacing, and trust signals
- [x] Redesign `GroupsListScreen.kt` as the canonical post-login landing surface
- [x] Add a greeting and summary layer to the groups screen using real user and balance data where available
- [x] Make create and join group actions visually primary and consistent with the new CTA system
- [x] Redesign the app tour overlay with animated progress indicators, branded cards, and stronger hierarchy
- [x] Align drawer entry points and top-level group actions with the updated groups landing behavior
- [x] Retire, repurpose, or clearly deprecate the unused `GroupsScreen.kt` so only one groups-landing path remains active

## Verified Execution Notes

- `LetsGoDutchApp.kt` routes the active landing path through `GroupsListScreen`, and `GroupsScreen.kt` is now explicitly deprecated rather than left as a silent parallel surface.
- `AppTourOverlay.kt` exists as an extracted app-owned overlay and is wired from the main app container.
- QA captures exist for the refreshed auth screen, app tour relaunch, drawer navigation, and the current groups landing / group-action surfaces under `qa_artifacts/`.
- Live device verification on 2026-04-08 confirmed auth-to-groups navigation from the actual auth screen for both the name-only path and the Google credential path. Both returned to the refreshed groups landing without broken routing.
- Live device verification on 2026-04-08 confirmed the tour reset and dismiss cycle end to end: the stored `letsgodutch_app_tour.completed` preference flipped from `true` to `false`, the overlay rendered on the next eligible screen open, and dismissing it flipped the preference back to `true` while returning to the underlying screen.
- Live device verification on 2026-04-08 now covers all three landing-state densities for `GroupsListScreen`: zero groups in `qa_artifacts/live_zero_group_check.xml`, one group in `qa_artifacts/live_restored_state.xml`, and many groups in `qa_artifacts/live_after_google_tap.xml`.
- Live verification on 2026-04-09 confirmed the denser groups landing layout: the hero now shows `Group Summary`, the cards are shorter, the ambient background stays dim enough for legibility, and the invite-code row now shares space with per-group settlement status and amount.
- Live verification on 2026-04-09 confirmed the signed-in user summary now reads from real per-group balances on both the hero card and each individual group card rather than metadata-only placeholders.
- Live verification on 2026-04-09 confirmed the new local 10:00 AM reminder path only targets unsettled groups and suppresses reminder noise when all tracked groups are settled.
- The refreshed debug build was installed successfully on the connected Samsung A54 (`SM-A546E`) after the verification pass on 2026-04-09.
- `qa_artifacts/post_login.xml` is identical to the tour overlay capture and should not be treated as standalone proof of a clean auth-to-groups transition.
- The connected device was restored to the original single-group anonymous state after verification so the QA pass did not leave it on the alternate seven-group Google-backed session.

## Testing Gate

- [x] New-user flow from auth to groups works without broken navigation
- [x] App tour renders correctly on first eligible launch and dismisses correctly
- [x] Groups landing screen remains usable with zero groups, one group, and many groups
- [x] Create group and join group actions are still reachable without drawer confusion
- [x] Hero summary and per-group settlement pills reflect real balance data without regressing invite actions
- [x] Denser groups cards and the blurred ambient background remain readable on-device
- [x] Daily local unsettled-group reminder only appears when open balances still exist
- [x] No duplicate or stale groups landing implementation remains on the active path

## Rollback Plan

If the new landing experience causes navigation or data-surface regressions, keep the new design system primitives but temporarily revert the groups landing layout and app tour only, then reapply screen changes in smaller slices.

## Error Tracker

| ERR_ID | Reported Date | Component | Error Description | Root Cause Analysis | Proposed Fix | Fix Date | Status |
|---|---|---|---|---|---|---|---|
| *(none logged)* | | | | | | | |

## Improvement Suggestion Tracker

| SUG_ID | Suggestion Date | Suggested By | Description | Proposed Changes | Approval Status | Approval Date | Implemented | Notes |
|---|---|---|---|---|---|---|---|---|
| SUG-REV-005 | 2026-04-08 | User + Codex | Defer anonymous reinstall and existing-group restore hardening to a safe post-revamp pass | Verify live Firebase identifier bindings and Realtime Database migration reads first, then design a backend-assisted or otherwise low-risk restore flow before changing client merge logic | `APPROVED` | 2026-04-08 | NO | Important non-UI follow-up. Do not mix speculative account-migration changes into active UI milestone work without validating production data state and rules. |
| SUG-REV-006 | 2026-04-08 | User | Add aggregate `Your Groups` financial totals for the signed-in user | Compute cross-group totals for current-user expense contribution, total receivable, and total payable, then surface them in the groups landing hero or summary layer | `IMPLEMENTED` | 2026-04-08 | YES | Delivered and verified on 2026-04-09 through the new hero summary path backed by per-group balance aggregation. |
| SUG-REV-007 | 2026-04-08 | User | Replace non-essential group-card secondary metadata with a per-group pending settlement amount | Derive an outstanding amount per group and show it in the secondary pill so group cards communicate settlement value instead of low-signal invite metadata | `IMPLEMENTED` | 2026-04-08 | YES | Delivered and verified on 2026-04-09 by splitting the invite row into invite-code and settlement-status halves on each group card. |
