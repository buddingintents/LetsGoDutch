# R-01 - Design System and Shell

Detailed milestone tracker for the Let's Go Dutch UI revamp.
Update this file first, then sync summary changes to [../LGD_UI_REVAMP_MASTER_PLAN.md](../LGD_UI_REVAMP_MASTER_PLAN.md).

| Field | Value |
|---|---|
| **Status** | `COMPLETED` |
| **Start Date** | 2026-04-08 |
| **Target End** | 2026-04-12 |
| **Actual End** | 2026-04-08 |
| **Git Branch** | `revamp/r01-design-system` |
| **Merge Target** | `main` |
| **Prerequisites** | R-00 `COMPLETED` |

## In-Scope Files and Areas

- `core/designsystem/src/main/java/com/buddingintents/letsgodutch/core/designsystem/theme/Color.kt`
- `core/designsystem/src/main/java/com/buddingintents/letsgodutch/core/designsystem/theme/Theme.kt`
- new typography and shape files in `core:designsystem`
- shared UI primitives in `core:designsystem`
- `app/src/main/java/com/buddingintents/letsgodutch/AppScaffoldWithDrawer.kt`
- `app/src/main/java/com/buddingintents/letsgodutch/AppDrawerContent.kt`
- extraction of app tour UI from `LetsGoDutchApp.kt` into a dedicated file

## Deliverables Checklist

- [x] Replace legacy purple-first theme tokens with the Fresh Mint palette
- [x] Add dedicated typography configuration for stronger amount and header hierarchy
- [x] Add dedicated shape configuration for cards, sheets, chips, dialogs, and FABs
- [x] Create shared UI primitives such as `GradientButton`, `AvatarBadge`, `SectionLabel`, and `PillTabSelector`
- [x] Update `LetsGoDutchTheme` to expose the new semantic palette cleanly in both light and dark modes
- [x] Refresh `AppScaffoldWithDrawer.kt` with the new shell treatments and spacing rules
- [x] Refresh `AppDrawerContent.kt` so profile, navigation items, and theme switching align with the new visual language
- [x] Extract the app tour UI from `LetsGoDutchApp.kt` into a dedicated composable file to reduce app-shell sprawl
- [x] Decide whether brand fonts are bundled locally or loaded dynamically, and document the decision

## Testing Gate

- [x] App builds successfully after design-system changes
- [x] Light, dark, and system theme modes all render without broken contrast
- [x] Drawer, top app bar, and shared CTA primitives render consistently across app shell screens
- [x] No active screen relies on hardcoded legacy purple values for primary brand states
- [x] Extracted app tour file compiles and behaves the same before deeper redesign work starts

## Rollback Plan

If the new token system destabilizes multiple screens at once, revert shell and theme changes together, then reintroduce the palette in smaller slices: color tokens first, then typography, then shared components.

## Execution Notes

- The current working tree already contains the main `R-01` implementation slices: Fresh Mint theme tokens, typography, shapes, shared primitives, shell updates, and extracted app-tour UI.
- Font direction for `R-01`: do not introduce runtime Google Font loading. Keep platform `SansSerif` for now; if a branded font is approved later, ship it as bundled app assets rather than fetching it at runtime.
- Build verification passed on 2026-04-08 with `:app:assembleDebug --console=plain`.
- Device validation completed on 2026-04-08 on `SM-A546E` (`RZCWA1260BK`).
- Theme validation evidence: drawer screenshots captured in all three modes; sampled average luminance was `53.35` in dark mode, `215.27` in light mode, and `53.34` in system mode, confirming system followed the device's dark setting and light/dark rendering diverged correctly.
- Extracted app-tour validation evidence: the overlay appeared on launch, dismissed cleanly, and reappeared immediately after triggering `Reset App Tour` from `Settings`, returning to step `1 of 5`.

## Error Tracker

| ERR_ID | Reported Date | Component | Error Description | Root Cause Analysis | Proposed Fix | Fix Date | Status |
|---|---|---|---|---|---|---|---|
| *(none logged)* | | | | | | | |

## Improvement Suggestion Tracker

| SUG_ID | Suggestion Date | Suggested By | Description | Proposed Changes | Approval Status | Approval Date | Implemented | Notes |
|---|---|---|---|---|---|---|---|---|
| SUG-REV-001 | 2026-04-08 | Codex | Prefer bundled `Sora` font resources over runtime Google Font lookup | Bundle fonts if APK impact is acceptable so typography remains stable offline and during low-connectivity use | `UNDER_REVIEW` | - | NO | R-01 currently stays on platform `SansSerif`; no runtime font lookup was introduced |
| SUG-REV-002 | 2026-04-08 | Codex | Create a shared `ui-components` package before deeper screen work | Build the primitive set in `core:designsystem` so later milestones stay consistent and faster to implement | `IMPLEMENTED` | 2026-04-08 | YES | Delivered in `core:designsystem/component/UiPrimitives.kt` |
