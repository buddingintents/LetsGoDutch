# Let's Go Dutch - Completed Audit Observations Archive

Active tracker: `docs/Revamp/Audit/LGD_UI_AUDIT_IMPLEMENTATION_TRACKER_2026-04-10.md`

Purpose: store passed and fully closed audit observations removed from the active tracker.

## Passed Implementation Activities

| ACT_ID | Source IDs | Completed On | Result | Summary |
|---|---|---|---|---|
| `ACT-AUD-R03-01` | `OBS-AE-01`, `OBS-AE-03` | `2026-04-10` | `PASSED` | Add Expense moved from alert dialog to bottom sheet with amount-first hierarchy. |
| `ACT-AUD-R03-02` | `OBS-AE-04` | `2026-04-10` | `PASSED` | Payer and participant selection moved from dropdowns to inline chips. |
| `ACT-AUD-R03-03` | `OBS-AE-05` | `2026-04-10` | `PASSED` | Split type moved from dropdown to pill selector. |
| `ACT-AUD-CROSS-02` | `GAP-04`, normalized `OBS-DS-04` | `2026-04-11` | `PASSED` | Primary CTA usage standardized on `GradientButton` for auth, add-expense, and settlement. |
| `ACT-AUD-CROSS-04` | `IND-03`, merged `RM-B08` | `2026-04-10` | `PASSED` | Indian number grouping applied consistently across user-visible money surfaces. |
| `ACT-AUD-R04-04` | `OBS-SE-02`, `OBS-SE-03`, `OBS-SE-04`, `GAP-10` | `2026-04-11` | `PASSED` | Settlement Preview moved to themed sections with dedicated transfer rows. |
| `ACT-AUD-R04-05` | `Routing debt` | `2026-04-11` | `PASSED` | Settlement Preview route extraction completed without regression. |
| `ACT-AUD-R04-01` | `OBS-IN-01` | `2026-04-12` | `PASSED` | Insights moved from plain cards to a darker analytics-focused panel surface without changing UPI behavior. |
| `ACT-AUD-R04-02` | `OBS-IN-02`, `OBS-IN-05`, `GAP-02` | `2026-04-12` | `PASSED` | Insights now includes a real settlement distribution chart, legend rows, and a compact balance bar chart. |
| `ACT-AUD-R04-03` | `OBS-IN-04` | `2026-04-12` | `PASSED` | Group detail now uses the shared pill selector for the `Ledger` / `Insights` switch. |
| `ACT-AUD-FEAT-02` | `IND-02` | `2026-04-12` | `PASSED` | Settlement completion now points users to `Recent Settlements`, where they can open or share the generated PDF with any compatible app. |
| `ACT-AUD-FEAT-04` | `RM-B04` | `2026-04-12` | `PASSED` | Expense categories now persist end-to-end, with category chips in Add Expense and a category breakdown panel in Insights. |
| `ACT-AUD-FEAT-05` | `RM-B05` | `2026-04-12` | `PASSED` | Expense notes now persist end-to-end, with note entry in Add Expense and note display in ledger and settlement PDF. |
| `ACT-AUD-FEAT-06` | `RM-B07` | `2026-04-12` | `PASSED` | Group activity feed now persists `activities/{groupId}` and renders create, join, expense, member, and settlement events in a new `Activity` tab. |
| `ACT-AUD-R05-01` | `OBS-AT-02`, `GAP-06` | `2026-04-11` | `PASSED` | App tour progress moved from linear bar to animated pill progress. |
| `ACT-AUD-R05-02` | `OBS-GR-01`, `OBS-GR-03` | `2026-04-11` | `PASSED` | Groups personality pass completed after dark-theme toggle-row corrections. |
| `ACT-AUD-R05-03` | `OBS-SE-01` | `2026-04-11` | `PASSED` | Settlement success celebration added and corrected to avoid duplicate share flow. |
| `ACT-AUD-FEAT-01` | `IND-01` | `2026-04-12` | `PASSED` | UPI activity ledger, manual confirmation flow, guard rails, and settlement/PDF tracking were completed and verified. |

## Archived Notes

- `ACT-AUD-CROSS-01` is not archived because one invite-badge verification item is still open.

## Normalized Observations Archived

| Source ID | Archived Handling |
|---|---|
| `OBS-DS-04` | Closed through `ACT-AUD-CROSS-02`. |
| `OBS-AE-01` | Closed through `ACT-AUD-R03-01`. |
| `OBS-SE-03` / `GAP-10` | Closed through `ACT-AUD-R04-04`. |
| `OBS-IN-05` | Closed through `ACT-AUD-R04-02`. |

## Closed Or Rejected Audit Items

| Source ID | Outcome | Reason |
|---|---|---|
| `Hero / balance card` | `CLOSED_IMPLEMENTED` | Groups landing already ships the hero summary card. |
| `Per-group balance pills` | `CLOSED_IMPLEMENTED` | Group cards already show settlement-state pills. |
| `Backdrop blur background art` | `CLOSED_IMPLEMENTED` | Ambient backdrop art already exists. |
| `GAP-01` | `CLOSED_IMPLEMENTED` | Balance-summary equivalent already shipped earlier. |
| `OBS-DS-02`, `GAP-08` | `REJECTED_INVALID` | Typography audit assumption was wrong for this repo. |
| `OBS-DS-03` | `CLOSED_IMPLEMENTED` | Custom shape radii already exist. |
| `OBS-DS-05` | `CLOSED_IMPLEMENTED` | Dark theme already sets the required `onPrimary`. |
| `OBS-AT-01` | `CLOSED_IMPLEMENTED` | App tour already used a dark gradient shell. |
| `OBS-AT-03` | `CLOSED_IMPLEMENTED` | Uppercase section-label treatment already existed. |
| `OBS-AT-05` | `CLOSED_IMPLEMENTED` | The tour already had the expected five-step structure. |
| `OBS-ST-01` | `CLOSED_IMPLEMENTED` | Settings already had the intended premium profile-led hierarchy. |
| `OBS-ST-04` | `CLOSED_IMPLEMENTED` | Settings already used `GradientButton` for save/install actions. |
| `OBS-ST-05` | `CLOSED_IMPLEMENTED` | The specific Settings inset concern was already handled. |
| `OBS-ST-06`, `GAP-07` | `CLOSED_IMPLEMENTED` | Stable account ID was already visible. |
| `RM-B06` | `CLOSED_IMPLEMENTED` | Expense date selection already existed and blocked future dates. |
| `OBS-DM-01` | `CLOSED_IMPLEMENTED` | Theme mode switching already existed. |
| `OBS-DM-02` | `CLOSED_IMPLEMENTED` | Dark-scheme spec details were already set in theme code. |
| `RM-B08` | `REJECTED_DUPLICATE` | Duplicate of `IND-03`. |
