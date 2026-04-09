# R-05 - Personal Tools and Production Polish

Detailed milestone tracker for the Let's Go Dutch UI revamp.
Update this file first, then sync summary changes to [../LGD_UI_REVAMP_MASTER_PLAN.md](../LGD_UI_REVAMP_MASTER_PLAN.md).

| Field | Value |
|---|---|
| **Status** | `NOT_STARTED` |
| **Start Date** | 2026-04-30 |
| **Target End** | 2026-05-06 |
| **Actual End** | - |
| **Last Updated** | 2026-04-09 |
| **Git Branch** | `revamp/r05-polish` |
| **Merge Target** | `main` |
| **Prerequisites** | R-04 `COMPLETED` |

## In-Scope Files and Areas

- `app/src/main/java/com/buddingintents/letsgodutch/PersonalExpenseTrackerScreen.kt`
- `app/src/main/java/com/buddingintents/letsgodutch/TodoTasksScreen.kt`
- empty states, banners, and final shell cleanup across the app
- accessibility review for all revamp surfaces
- production branding and deep-link verification polish

## Deliverables Checklist

- [ ] Redesign `PersonalExpenseTrackerScreen.kt` with stronger summary cards, chart hierarchy, and more intentional filter presentation
- [ ] Redesign `TodoTasksScreen.kt` to improve swipe-action feedback, status grouping, and empty states
- [ ] Unify empty, loading, disabled, and success states across all revamp surfaces
- [ ] Review accessibility semantics, contrast, and large-text resilience across the full app
- [ ] Review ad placement and spacing against the final premium layout system
- [ ] Remove or archive any remaining dormant pre-revamp UI artifacts
- [ ] Capture final screenshots or validation notes for each major surface

## Testing Gate

- [ ] Personal tracker filters, export action, and delete flows still work after redesign
- [ ] Todo swipe actions still complete and cancel tasks correctly
- [ ] Empty states exist for major surfaces with no data
- [ ] Accessibility review issues are closed or logged as conscious debt
- [ ] Banner placements do not visually break core user flows
- [ ] Final revamp walkthrough confirms a coherent visual system across auth, groups, expenses, settings, analytics, settlement, personal tools, and todo

## Rollback Plan

If final polish work reveals instability, keep the milestone focused on accessibility and functional cleanup first, and defer low-signal visual extras into approved suggestions or tech debt.

## Error Tracker

| ERR_ID | Reported Date | Component | Error Description | Root Cause Analysis | Proposed Fix | Fix Date | Status |
|---|---|---|---|---|---|---|---|
| ERR-REV-001 | 2026-04-09 | Production app links | Release-installed `https://buddingintents.com/join/<INVITE_CODE>` links still open the Android resolver/browser path instead of defaulting into the app | Live `assetlinks.json` fingerprints were corrected, but on-device domain verification remains in custom error state `1024`; normal TLS validation against `buddingintents.com` still fails with an untrusted certificate chain | Fix the production TLS certificate chain for `buddingintents.com` and `www.buddingintents.com`, then rerun Android App Links verification on a release-installed build | - | `OPEN` |
| ERR-REV-002 | 2026-04-09 | Brand icon rollout | Launcher icon and in-app brand icon usage are inconsistent; the old installed release build still showed the previous launcher asset, and round-placeholder contexts were reusing rounded-rectangle artwork | Current brand rollout mixed the correct rounded-rectangle mark with round launcher contexts and did not yet ship a dedicated circular brand asset for round placeholders | Ship a dedicated circular icon variant for round launcher contexts, keep the rounded-rectangle asset for hero and card badges, and reinstall a freshly built APK so the device launcher cache updates from the new resource set | - | `IN_PROGRESS` |

## Improvement Suggestion Tracker

| SUG_ID | Suggestion Date | Suggested By | Description | Proposed Changes | Approval Status | Approval Date | Implemented | Notes |
|---|---|---|---|---|---|---|---|---|
| SUG-REV-004 | 2026-04-08 | Codex | Reassess ad placement against the premium visual hierarchy after the main screen revamp | Review banner density, spacing, and placement rules after the redesigned shell and card system are stable | `UNDER_REVIEW` | - | NO | Mirrors cross-milestone tracker |
| SUG-REV-008 | 2026-04-08 | User | Polish the group details dialog so it matches the Fresh Mint theme and feels less plain | Rework the dialog hierarchy, spacing, semantic labels, and owner-action treatment so group details no longer read like a stock alert stack | `APPROVED` | 2026-04-08 | NO | Deferred from the current pass because the functional copy and invite-status fixes landed first; the visual dialog pass should be handled during production polish. |
