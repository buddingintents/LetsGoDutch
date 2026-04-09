# R-03 - Expense Entry and Settings

Detailed milestone tracker for the Let's Go Dutch UI revamp.
Update this file first, then sync summary changes to [../LGD_UI_REVAMP_MASTER_PLAN.md](../LGD_UI_REVAMP_MASTER_PLAN.md).

| Field | Value |
|---|---|
| **Status** | `NOT_STARTED` |
| **Start Date** | 2026-04-18 |
| **Target End** | 2026-04-23 |
| **Actual End** | - |
| **Git Branch** | `revamp/r03-expense-settings` |
| **Merge Target** | `main` |
| **Prerequisites** | R-02 `COMPLETED` |

## In-Scope Files and Areas

- `feature/expenses/src/main/java/com/buddingintents/letsgodutch/feature/expenses/AddExpenseDialog.kt`
- `app/src/main/java/com/buddingintents/letsgodutch/SettingsScreen.kt`
- supporting route wiring in `LetsGoDutchApp.kt` where needed

## Deliverables Checklist

- [ ] Convert the current expense entry experience from a plain alert-style form into a bottom-sheet style flow or equivalent modern modal surface
- [ ] Replace payer selection dropdowns with clearer member-chip or avatar-driven selection controls where practical
- [ ] Replace split-type dropdown selection with a segmented control
- [ ] Add inline allocation validation for exact, percentage, and custom split modes
- [ ] Make amount entry visually primary and INR-first
- [ ] Redesign `SettingsScreen.kt` with a profile-led hierarchy, clearer action rows, and stronger account ownership cues
- [ ] Ensure reset-tour, update-check, install, and Play Store actions fit the new system without looking like default button stacks
- [ ] Preserve keyboard avoidance, date selection, and accessibility semantics during the redesign

## Testing Gate

- [ ] Add-expense flow still supports equal, exact, percentage, and custom split modes
- [ ] Split validation prevents obviously invalid save states without breaking valid saves
- [ ] Date selection still works and prevents future dates as before
- [ ] Settings actions still invoke the correct callbacks and state labels
- [ ] New forms remain readable and usable in both light and dark themes

## Rollback Plan

If the bottom-sheet redesign increases data-entry bugs, keep the new field styles and validation logic but temporarily fall back to the dialog container while the sheet behavior is stabilized.

## Error Tracker

| ERR_ID | Reported Date | Component | Error Description | Root Cause Analysis | Proposed Fix | Fix Date | Status |
|---|---|---|---|---|---|---|---|
| *(none logged)* | | | | | | | |

## Improvement Suggestion Tracker

| SUG_ID | Suggestion Date | Suggested By | Description | Proposed Changes | Approval Status | Approval Date | Implemented | Notes |
|---|---|---|---|---|---|---|---|---|
| *(none logged)* | | | | | | | | |

