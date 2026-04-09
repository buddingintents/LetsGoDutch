# LET'S GO DUTCH - UI Revamp Master Plan
**Fresh Mint Revamp - Phase 2 UI Modernization**
**Version:** 1.0.0 | **Created:** 2026-04-08 | **Last Updated:** 2026-04-09 | **Owner:** Budding Intents

---

## CODEX AGENT INSTRUCTIONS

> Read this file first before editing revamp-phase UI code or documentation.

### Session Protocol
1. Parse this file before starting revamp work.
2. Identify the active revamp milestone from Section 6, then open the detailed milestone file from `docs/Revamp/Milestones/`.
3. Check milestone-local Error Tracker entries before adding new UI work.
4. Check milestone-local Improvement Suggestion Tracker entries. If an item is `APPROVED` and `Implemented = NO`, close it before finishing the milestone.
5. Update checklist items from `[ ]` to `[x]` as deliverables land.
6. Log new UI defects in the active milestone file and also mirror them into the cross-milestone tracker in `UI_REVAMP_ERROR_TRACKER.md`.
7. Log new improvement ideas in the active milestone file and also mirror them into `UI_REVAMP_SUGGESTION_TRACKER.md`.
8. If milestone dates or status change, sync both the milestone file and the master summary table in this document.
9. Before marking a milestone `COMPLETED`, verify its Testing Gate checklist.
10. Update the `Last Updated` field in this file and the touched milestone file on every working session.

### Status Enum Reference
```
MILESTONE STATUS:
  NOT_STARTED       -> Work has not begun
  IN_PROGRESS       -> Actively being developed
  BLOCKED           -> Blocked by an unresolved error in THIS milestone
  BLOCKED_EXTERNAL  -> Blocked by prerequisite milestone not yet COMPLETED
  IN_REVIEW         -> Testing Gate checks in progress
  COMPLETED         -> All deliverables done, Testing Gate passed
  ROLLED_BACK       -> Milestone reverted to rollback plan state

ERROR STATUS:
  OPEN              -> Error identified, not yet fixed
  IN_PROGRESS       -> Fix being investigated or implemented
  RESOLVED          -> Fix applied and verified
  WONT_FIX          -> Accepted known issue, documented

SUGGESTION STATUS:
  PROPOSED          -> Potential improvement identified
  UNDER_REVIEW      -> Owner is evaluating the suggestion
  APPROVED          -> Owner approved; implement before milestone closes
  REJECTED          -> Owner rejected; do not implement
  IMPLEMENTED       -> Approved suggestion has been delivered and verified

TECH DEBT STATUS:
  OPEN              -> Debt exists, not yet addressed
  SCHEDULED         -> Assigned to a future milestone
  RESOLVED          -> Debt cleared
```

---

## 1. Revamp Overview

| Field | Value |
|---|---|
| **Application** | `Let's Go Dutch` |
| **Bundle ID** | `com.buddingintents.letsgodutch` |
| **Platform** | Android |
| **Language** | Kotlin |
| **UI Stack** | Jetpack Compose + Material 3 |
| **Current Phase** | Phase 1 complete, Phase 2 UI revamp R-02 completed and verified; R-03 is next, with partial R-04 screen polish already landed early in ledger and insights |
| **Revamp Theme** | `Fresh Mint` |
| **Primary Source Docs** | `docs/Revamp/ui_upgrade_recommendations1.html`, `docs/Revamp/ui_upgrade_recommendations2.md` |
| **Documentation Pattern Reference** | `C:\Users\shiva\Codex\Vigilens\Docs` |
| **Revamp Start Date** | 2026-04-08 |
| **Target Completion** | 2026-05-06 |
| **Repo Root** | `C:\Users\shiva\Codex\LGD\LGD_8Apr` |

### Revamp Mission

Modernize the app from a functionally solid but visually generic expense-sharing utility into a coherent, premium, INR-first financial product. The revamp must reduce cognitive load, improve trust and visual hierarchy, and standardize screen ownership so future UI work stops accumulating inside `app/LetsGoDutchApp.kt`.

### Revamp Non-Goals

- No feature-parity rewrite of repositories or Firebase data flow.
- No back-end schema redesign during the visual revamp unless UI validation requires a small contract change.
- No broad Gradle module explosion at the start of the revamp. Prefer file extraction and shared UI primitives first.

---

## 2. Current UI Architecture Assessment

### 2.1 Confirmed Module Structure

- `app`
- `core:common`
- `core:model`
- `core:data`
- `core:designsystem`
- `feature:auth`
- `feature:groups`
- `feature:expenses`
- `feature:ledger`
- `feature:insights`
- `feature:settlement`

### 2.2 Active Surface Ownership

| Surface | Current File | Observation |
|---|---|---|
| Auth screen | `feature/auth/src/main/java/com/buddingintents/letsgodutch/feature/auth/AuthScreen.kt` | Active, feature-owned, and already aligned with the Fresh Mint revamp direction |
| Groups landing | `feature/groups/src/main/java/com/buddingintents/letsgodutch/feature/groups/GroupsListScreen.kt` | Active screen used by navigation |
| Groups screen variant | `feature/groups/src/main/java/com/buddingintents/letsgodutch/feature/groups/GroupsScreen.kt` | Present, deprecated, and no longer the canonical landing surface |
| App shell | `app/src/main/java/com/buddingintents/letsgodutch/AppScaffoldWithDrawer.kt` | Active revamp shell with custom drawer treatment and branded top-level structure |
| Drawer content | `app/src/main/java/com/buddingintents/letsgodutch/AppDrawerContent.kt` | Active and visually refreshed; still shares ownership with app-level navigation |
| Settings | `app/src/main/java/com/buddingintents/letsgodutch/SettingsScreen.kt` | Active but app-owned, not feature-owned |
| Personal expense tracker | `app/src/main/java/com/buddingintents/letsgodutch/PersonalExpenseTrackerScreen.kt` | Active but app-owned |
| Todo tracker | `app/src/main/java/com/buddingintents/letsgodutch/TodoTasksScreen.kt` | Active but app-owned |
| Add expense | `feature/expenses/src/main/java/com/buddingintents/letsgodutch/feature/expenses/AddExpenseDialog.kt` | Active, dialog-based, not bottom-sheet based |
| Ledger | `feature/ledger/src/main/java/com/buddingintents/letsgodutch/feature/ledger/LedgerScreen.kt` | Active with partial hierarchy and member-identity polish already landed ahead of the formal R-04 milestone |
| Insights | `feature/insights/src/main/java/com/buddingintents/letsgodutch/feature/insights/InsightsScreen.kt` | Active with summary cards, leader rows, and darker visual treatment already landed ahead of the formal R-04 milestone |
| Settlement preview | `app/src/main/java/com/buddingintents/letsgodutch/LetsGoDutchApp.kt` | Active, implemented inline in NavHost route |
| App tour overlay | `app/src/main/java/com/buddingintents/letsgodutch/ui/AppTourOverlay.kt` | Extracted and active; route wiring remains in `LetsGoDutchApp.kt` |
| Settlement helper card | `feature/settlement/src/main/java/com/buddingintents/letsgodutch/feature/settlement/SettlementCard.kt` | Present but currently unused |

### 2.3 Design System Findings

| Area | Current State | Impact |
|---|---|---|
| Color tokens | Fresh Mint palette is defined in `core:designsystem` and wired into the active theme | R-01 color foundation is in place for later milestones |
| Typography | Dedicated typography file now exists and supports stronger hierarchy than the default Material baseline | Major first-impression surfaces already benefit from the updated type scale |
| Shapes | Dedicated shape file now exists | Cards, sheets, chips, and dialogs can share a consistent geometry language |
| Reusable UI primitives | Shared revamp primitives now exist, but pill tabs and some analytics patterns are still not standardized | Later milestones can build on a stronger base instead of restyling from scratch |
| Motion language | Basic branded transitions and progress affordances exist in the shell and tour overlay | Motion system is improved, but still lighter than the final target state |

### 2.4 Structural Findings That Affect the Plan

1. `LetsGoDutchApp.kt` is still carrying too much UI responsibility. App tour extraction is complete, but navigation, state wiring, group detail, and settlement preview remain concentrated there.
2. Recommended file targets in the HTML report do not always exist in this repo. The implementation plan must translate recommendation intent into actual file paths.
3. `SettlementCard.kt` is still a dormant surface, and `GroupsScreen.kt` has now been explicitly deprecated. Leaving either path ambiguous will still create drift unless cleanup continues.
4. The active groups landing screen is `GroupsListScreen.kt`, not `GroupsScreen.kt`.
5. `SettingsScreen`, `PersonalExpenseTrackerScreen`, and `TodoTasksScreen` still live in `app`, which makes feature-level polish and ownership harder.
6. Analytics surfaces are mid-transition. `InsightsScreen.kt` already contains stronger cards and balance storytelling, but the group-detail shell and settlement route have not been brought up to the same level yet.

### 2.5 Recommendation-to-Code Translation Map

| Recommendation Target | Actual Repo Reality | Planned Translation |
|---|---|---|
| `feature/settings/SettingsScreen.kt` | Screen currently lives in `app/SettingsScreen.kt` | Revamp existing file first, then evaluate extraction |
| `feature/groups/GroupsScreen.kt` | `GroupsListScreen.kt` is active, `GroupsScreen.kt` is unused | Make `GroupsListScreen.kt` the canonical landing screen and retire the unused variant |
| `feature/expenses/AddExpenseSheet.kt` | Active file is `AddExpenseDialog.kt` | Convert dialog behavior into a bottom-sheet style screen or component in the same feature |
| `feature/ledger/InsightsTab.kt` | Active file is `feature/insights/InsightsScreen.kt`; tab container lives in `LetsGoDutchApp.kt` | Revamp `InsightsScreen.kt` and extract tab-shell styling from the app container |
| `app/ui/AppTourOverlay.kt` | Dedicated file now exists and is wired from `LetsGoDutchApp.kt` | Keep the extracted overlay as the canonical app-tour implementation |
| `feature/settlement/SettlementScreen.kt` | Settlement preview route is inline in `LetsGoDutchApp.kt`; `SettlementCard.kt` is unused | Create a dedicated feature-owned settlement preview screen and retire the dormant helper |

---

## 3. Recommendation Synthesis

### 3.1 Shared Direction Across Both Reports

- Replace legacy purple accents with a `Fresh Mint` palette built around mint, teal, charcoal, foam, amber, and coral.
- Increase perceived trust with darker premium surfaces, stronger amount typography, and cleaner section hierarchy.
- Reduce cognitive load with progressive disclosure, inline validation, and less raw form density.
- Improve tactile feel with spring-based transitions, better state feedback, and clearer CTAs.
- Make analytics and settlement surfaces feel high-value and trustworthy instead of administrative.

### 3.2 Cross-Cutting Acceptance Principles

1. Amounts and net balances must be visually dominant and easy to scan.
2. Payable states should bias toward amber or warm warning hues instead of aggressive pure red.
3. Receivable and healthy states should use mint or sage-like positive semantics.
4. INR formatting must remain first-class across chips, summaries, and validation.
5. Dark analytics surfaces should not sacrifice WCAG contrast.
6. Empty, loading, disabled, and success states must be designed, not left as defaults.
7. The revamp must use `MaterialTheme.colorScheme` and shared primitives instead of screen-local hardcoded colors.

---

## 4. Target UI Architecture

### 4.1 Design System Target State

The first implementation gate is a stronger `core:designsystem` layer with:

- Brand color tokens for light and dark modes
- Dedicated typography file
- Dedicated shape file
- Shared gradient CTA component
- Shared avatar, chip, and section-header patterns
- Shared tab-selector and analytics card patterns
- Shared semantic colors for owe, owed, neutral, warning, success, and destructive states

### 4.2 Screen Ownership Strategy

| Scope | Short-Term Plan | Long-Term Plan |
|---|---|---|
| Navigation graph | Keep in `app` | Keep in `app` |
| Embedded app tour UI | Extract into `app/ui/` immediately | Stable app-owned overlay |
| Settlement preview UI | Move out of `LetsGoDutchApp.kt` into `feature:settlement` | Feature-owned route content |
| Settings, personal tracker, todo tracker | Revamp in place first | Evaluate extraction once shell primitives settle |
| Groups landing | Use `GroupsListScreen.kt` as canonical | Remove or archive unused `GroupsScreen.kt` |

### 4.3 Proposed Shared Component Inventory

- `GradientButton`
- `MintOutlinedField`
- `HeroSummaryCard`
- `AvatarBadge`
- `BalanceChip`
- `PillTabSelector`
- `SectionLabel`
- `ActionRowItem`
- `EmptyStateCard`
- `ConfirmationSwipeTrack` for settlement

### 4.4 Charting and Visualization Strategy

- Prefer a Compose-friendly chart solution such as Vico before adopting heavier view-based chart libraries.
- Keep chart adoption limited to real value surfaces: group insights and personal tracker summaries.
- If a third-party chart library slows delivery, fall back to custom Compose cards for Milestone R-04 and log the gap in tech debt.

---

## 5. Revamp Goals by Surface

| Surface | Primary Objective | Secondary Objective |
|---|---|---|
| Auth | Improve first impression and trust | Better reuse of prior anonymous-name hints |
| Groups landing | Make group entry feel premium and actionable | Surface balances and obvious create/join actions |
| Drawer and shell | Unify navigation identity | Reduce stock-Material feel |
| Add expense | Reduce data-entry friction | Inline split validation and better participant selection |
| Settings | Upgrade account ownership cues | Make updates/tour/reset actions feel intentional |
| Ledger | Improve scanability and hierarchy | Prepare shared visuals for group detail shell |
| Insights | Make balances intuitive, not spreadsheet-like | Introduce dark analytics surface |
| Settlement | Increase trust and prevent accidental actions | Add clear progressive states |
| Personal tracker | Make summaries feel useful and premium | Improve filters, charts, and export affordances |
| Todo tracker | Make swipe actions feel tactile | Improve empty and history states |

---

## 6. Milestone Summary

| Milestone | Status | Start Date | Target End | Scope | Detail |
|---|---|---|---|---|---|
| R-00 | `COMPLETED` | 2026-04-08 | 2026-04-08 | Discovery and planning | [R-00](./Milestones/R-00-discovery-and-planning.md) |
| R-01 | `COMPLETED` | 2026-04-08 | 2026-04-12 | Design system and UI shell | [R-01](./Milestones/R-01-design-system-and-shell.md) |
| R-02 | `COMPLETED` | 2026-04-08 | 2026-04-17 | First impressions and navigation | [R-02](./Milestones/R-02-first-impressions-and-navigation.md) |
| R-03 | `NOT_STARTED` | 2026-04-18 | 2026-04-23 | Expense entry and settings | [R-03](./Milestones/R-03-expense-entry-and-settings.md) |
| R-04 | `NOT_STARTED` | 2026-04-24 | 2026-04-29 | Ledger, insights, and settlement | [R-04](./Milestones/R-04-ledger-insights-and-settlement.md) |
| R-05 | `NOT_STARTED` | 2026-04-30 | 2026-05-06 | Personal tools and production polish | [R-05](./Milestones/R-05-personal-tools-and-polish.md) |

`R-04` remains formally unopened in the schedule, but ledger and insights already contain partial revamp work that should be treated as milestone input rather than as completed delivery.

---

## 7. Dependency Map

### Default Order

`R-00 -> R-01 -> R-02 -> R-03 -> R-04 -> R-05`

### Notes

- `R-01` must land before any other milestone because all later screens depend on brand tokens, shapes, and shared components.
- `R-02` depends on `R-01` because groups, auth, and app tour are first-impression surfaces and must use the new shell.
- `R-03` depends on `R-01` and partially on `R-02` for shared CTA, field, and avatar treatments.
- `R-04` depends on `R-01` and `R-03` because settlement and insights need the analytics shell and refined semantic colors.
- `R-05` depends on all previous milestones for consistency, accessibility, and final polish.

---

## 8. Risks and Mitigations

| Risk ID | Risk | Impact | Mitigation |
|---|---|---|---|
| RISK-001 | `LetsGoDutchApp.kt` remains oversized during UI extraction | Merge conflicts and slow iteration | Extract UI routes one at a time and keep navigation wiring stable |
| RISK-002 | Recommendation files target paths that do not exist | Team may implement against wrong files | Use the translation table in Section 2.5 as the implementation map |
| RISK-003 | Bundled charting introduces dependency churn | Milestone delay | Gate chart library adoption inside `R-04` with fallback plan |
| RISK-004 | Dark analytics surfaces reduce readability | Accessibility regression | Validate contrast and amount hierarchy in both themes |
| RISK-005 | Too much architectural refactor expands scope | Delivery slip | Prefer file extraction and shared primitives before new Gradle modules |
| RISK-006 | Ads clash with premium layout hierarchy | Perceived quality drop | Review banner placement in `R-05` and adjust spacing or placement rules |

---

## 9. Decision Log

| DEC_ID | Date | Decision | Rationale | Follow-Up |
|---|---|---|---|---|
| DEC-REV-001 | 2026-04-08 | Start the revamp with shared design system work before touching screen-level polish | Both recommendation docs and the current code audit show `core:designsystem` is the leverage point | Execute in `R-01` |
| DEC-REV-002 | 2026-04-08 | Use actual repo paths rather than recommendation-path names when implementing | Several recommendation targets do not match the current repo structure | Keep Section 2.5 as source of truth |
| DEC-REV-003 | 2026-04-08 | Prefer file extraction and canonical screen cleanup before adding new Gradle modules | Reduces risk while still fixing current ownership drift | Re-evaluate after `R-03` |
| DEC-REV-004 | 2026-04-08 | Keep milestone-local trackers as the canonical detail source and add cross-milestone tracker docs for quick scanning | Matches the Vigilens operating pattern while making revamp status easier to review | Maintain both in parallel |
| DEC-REV-005 | 2026-04-08 | Do not introduce runtime font loading during `R-01`; keep platform sans-serif until a branded font is approved | Avoids connectivity-dependent typography while the shell and token work stabilizes | Revisit bundled brand-font adoption after `R-01` validation |

---

## 10. Tech Debt Register

| DEBT_ID | Date Logged | Description | Affected Area | Planned Resolution | Status |
|---|---|---|---|---|---|
| DEBT-REV-001 | 2026-04-08 | `LetsGoDutchApp.kt` still mixes navigation, state orchestration, route content, and inline settlement preview even after app-tour extraction | `app` | Finish settlement preview extraction, then continue route-shell cleanup around the group-detail container | `SCHEDULED` |
| DEBT-REV-002 | 2026-04-08 | Feature ownership is inconsistent because several active screens still live in `app` | `app`, feature boundaries | Normalize ownership gradually during milestones R-03 to R-05 | `OPEN` |
| DEBT-REV-003 | 2026-04-08 | Dormant UI files (`GroupsScreen.kt`, `SettlementCard.kt`) can cause implementation drift | `feature:groups`, `feature:settlement` | Retire or repurpose once canonical revamp screens land | `OPEN` |
| DEBT-REV-004 | 2026-04-08 | Same-device anonymous reinstall restore can fail when legacy identifier bindings or cross-user migration reads are unavailable, leaving prior memberships unsynced after reinstall | `core:data`, Firebase auth and database migration paths | After the active UI revamp slice, verify live bindings and rules first, then implement a safer restore strategy, ideally backend-assisted, instead of speculative client-only migration edits | `SCHEDULED` |

---

## 11. Execution Notes

### Immediate Next Step

Start `R-03` with:

1. carry the stabilized Fresh Mint hierarchy into settings and self-serve tools without reviving deprecated pre-revamp surfaces
2. keep `feature/groups/GroupsScreen.kt` deprecated and preserve the single canonical groups landing path now that `R-02` is closed
3. treat the existing ledger and insights refresh work as preparation for `R-04`, not as a reason to skip settlement-route extraction later
4. track the production app-links verifier failure and the launcher/icon-shape consistency pass inside `R-05` so release polish closes both website-coupled and brand-asset issues before final sign-off

### Review Rule

No later milestone should start until the milestone before it has:

- all deliverables checked
- Testing Gate items reviewed
- local Error Tracker resolved or consciously documented
- cross-milestone trackers synced
