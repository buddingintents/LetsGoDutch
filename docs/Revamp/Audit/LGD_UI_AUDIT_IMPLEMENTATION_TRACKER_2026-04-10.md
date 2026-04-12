# Let's Go Dutch - Active Audit Implementation Tracker

Source audit: `docs/Revamp/Audit/LGD_UIAudit_Revised_2026-04-10.html`

Completed archive: `docs/Revamp/Audit/LGD_UI_AUDIT_COMPLETED_2026-04-10.md`

Purpose: keep this file limited to active, partial, failed, or not-yet-started observations. Passed and fully closed observations are moved to the completed archive to keep working context small.

## Active Implementation Activities

### Cross-Cutting UI Closure

| ACT_ID | Source IDs | Validated Gap | Evidence | Target Files | Priority | Status |
|---|---|---|---|---|---|---|
| `ACT-AUD-CROSS-01` | `GAP-03`, `OBS-DM-03` | Theme cleanup landed, but one manual verification item is still open. | The Group Details invite-badge danger vs neutral state check is still deferred. | `app/.../LetsGoDutchApp.kt`, `feature/insights/.../InsightsScreen.kt`, `feature/groups/.../GroupsListScreen.kt`, `app/.../ui/AppTourOverlay.kt` | High | `DEFERRED` |
| `ACT-AUD-CROSS-03` | `GAP-05`, narrowed `OBS-DM-04` | Edge-to-edge and inset handling still needs a final audit on non-settings custom surfaces. | `SettingsScreen.kt` is already closed; remaining scope is auth, overlays, and other custom full-screen surfaces. | `feature/auth/.../AuthScreen.kt`, `app/.../ui/AppTourOverlay.kt`, `app/.../LetsGoDutchApp.kt` | Medium | `DEFERRED` |

Notes:

- `ACT-AUD-CROSS-01`
  - Main implementation history is archived.
  - Remaining open verification:
    - In `Group Details`, verify the expired invite badge reads as a danger state and the active invite badge reads as a neutral status badge.
- `ACT-AUD-CROSS-03`
  - Keep this as a focused audit only. Do not reopen already-closed Settings work.
  - Implementation pass on `2026-04-12`:
    - `AuthScreen` now applies safe-drawing insets at the screen root
    - `AppTourOverlay` now applies safe-drawing insets before its custom dialog padding
    - the shared revamp dialog shell and backend-sync overlay now apply safe-drawing insets, covering create/join/member/UPI/review custom surfaces
  - Build/install verification on `2026-04-12`:
    - `assembleDebug` passed
    - APK installed on `SM_A546E` (`RZCWA1260BK`)
  - Focused manual verification:
    - Open the auth screen and confirm headline card, background orbs, and actions no longer sit under the status or navigation bars.
    - Open the app tour and confirm the full-screen scrim plus card keep safe spacing from top and bottom system bars.
    - Open any revamp dialog such as `Create Group`, `Join Group`, or the UPI confirmation modal and confirm the custom shell keeps safe spacing from cutouts and navigation bars in both portrait and landscape.
  - Landscape corrective pass on `2026-04-12`:
    - shared revamp dialogs now use a larger landscape max width, a taller bounded max height, and internal scroll
    - app tour now uses a bounded card plus internal scroll in landscape
    - auth hero card width is capped to avoid stretched landscape layout
  - Landscape corrective build/install verification on `2026-04-12`:
    - `assembleDebug` passed
    - updated APK reinstalled on `SM_A546E` (`RZCWA1260BK`)
  - Orientation-state corrective pass on `2026-04-12`:
    - `BackendSyncDialog` now uses bounded height plus internal scroll, so the auth sync popup no longer overflows in landscape
    - group-detail state holders now use saveable state or saveable IDs for add-expense, ledger or insights tab selection, and edit or delete confirmation flows
    - auth status messaging now survives activity recreation during sign-in or sync
  - Orientation-state build/install verification on `2026-04-12`:
    - `assembleDebug` passed
    - updated APK reinstalled on `SM_A546E` (`RZCWA1260BK`)
  - Full landscape overflow corrective pass on `2026-04-12`:
    - shared revamp dialogs now apply IME padding so create and join flows stay within bounds when text input is active
    - create-group, join-group, group-details, claim-existing-member, manage-members, members-list, recent-settlements, and UPI app picker now use smaller landscape scroll-section heights instead of fixed tall content blocks
    - create-group and join-group action layouts now stack vertically in landscape to avoid cramped bottom-row overflow
    - group, to-do, and self-expense FABs now collapse to compact icon-only actions in landscape to avoid overflow on shorter screens
    - recent-settlements item actions now stack in landscape to avoid per-row action clipping
  - Full landscape overflow build/install verification on `2026-04-12`:
    - `assembleDebug` passed
    - updated APK reinstalled on `SM_A546E` (`RZCWA1260BK`)
  - Remaining focused manual verification:
    - Rotate the auth screen while `Syncing your account` or `Syncing your profile` is visible and confirm the popup stays within bounds and the sync state is preserved.
    - Open a group, switch to `Insights`, rotate, and confirm the selected tab is retained.
    - Open add-expense, delete-expense, edit-member, remove-member, and recent-settlement delete flows, rotate, and confirm the active dialog remains open on the same record.
    - In landscape, verify the group FAB is now compact and no longer overflows.
    - In landscape, open `Create Group`, `Join Group`, and `Recent Settlements` and confirm none of them overflow.
  - User direction on `2026-04-12`:
    - keep this observation deferred for now and continue with the next item

### Product Backlog Derived From The Audit

| ACT_ID | Source IDs | Validated Gap | Evidence | Suggested Order | Status |
|---|---|---|---|---|---|
| `ACT-AUD-FEAT-03` | `RM-B01` | FCM server-push scaffold is added, but backend deployment and end-to-end verification are still pending. | The repo now contains Cloud Functions code for `notifications/{userId}/{notificationId}` push delivery plus Android-side dedupe by `notificationId`. | After current UI closure | `DEFERRED` |
| `ACT-AUD-FEAT-07` | `RM-C03` | Group budget tracker is absent. | No group budget field or spend-vs-budget UI exists. | After `ACT-AUD-FEAT-04` | `DEFERRED` |
| `ACT-AUD-FEAT-08` | `RM-C02` | Recurring expenses are absent. | No recurring-expense model or scheduler-driven creation flow exists. | Defer until core UI and data features stabilize | `DEFERRED` |

Notes:

- `ACT-AUD-FEAT-03`
  - Implementation pass on `2026-04-12`:
    - added a Firebase Cloud Functions scaffold under `functions/`
    - added `pushUserNotification`, which watches `notifications/{userId}/{notificationId}` and sends data-only FCM to `fcmTokens/{userId}`
    - invalid registration tokens are now removed automatically during push send
    - push delivery status is written back under each notification record
    - Android now deduplicates locally rendered Realtime Database notifications against incoming FCM using `notificationId`
  - Verification on `2026-04-12`:
    - `assembleDebug` passed after the Android dedupe changes
    - `functions/index.js` passed `node --check`
    - updated APK installed on `SM_A546E` (`RZCWA1260BK`)
  - Deployment follow-up:
    - run `cd functions && npm install`
    - deploy with `firebase deploy --only functions`
    - verify one expense-add and one settlement-PDF notification on two real devices/accounts
  - Focused manual verification after deployment:
    - With the target account backgrounded, add an expense from another account and confirm exactly one push notification arrives.
    - With the target account foregrounded, add another expense and confirm only one local notification appears, not a duplicate pair.
    - Verify `notifications/{userId}/{notificationId}/pushDelivery` records `SENT`, `PARTIAL`, `FAILED`, or `NO_TOKENS` as expected.
- `ACT-AUD-FEAT-06`
  - User marked this passed on `2026-04-12`.
  - Moved to the completed archive to keep this file limited to deferred or still-open work.

## Active Normalized Observations

| Source ID | Problem In Audit Wording | Active Handling |
|---|---|---|
| `OBS-ST-05` | Flagged `SettingsScreen` for inset handling, but settings already handles insets. | Keep only `ACT-AUD-CROSS-03` as a narrowed custom-surface review. |

## Next Execution Order

1. Remaining active observations are intentionally deferred by user direction on `2026-04-12`.
