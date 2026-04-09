# LGD UI Audit Assessment

Source audit: `docs/Revamp/Audit/LGD_UIAudit_Roadmap.html`  
Assessment date: `2026-04-10`

This document translates the HTML audit into a working backlog with explicit scoring for criticality, usefulness, complexity, and solo-developer feasibility.

## Scope And Interpretation

- The HTML audit is inference-based. It was written from the README, commit history, and revamp recommendation docs, not from a full source-level review.
- The revamp source of truth remains `docs/Revamp/LGD_UI_REVAMP_MASTER_PLAN.md`.
- Repeated recommendations are preserved across sections because the audit itself repeats them in observations, critical gaps, roadmap items, prompts, and sprint planning.

## Status Corrections

| Audit statement or assumption | Corrected status | Source of truth |
|---|---|---|
| "Phase 3 complete" | Current documented state is: Phase 1 complete; Phase 2 revamp has `R-02` completed and verified; `R-03` is next; partial `R-04` polish has landed early in ledger and insights | `docs/Revamp/LGD_UI_REVAMP_MASTER_PLAN.md` |
| "README still describes Google-only auth" | Google sign-in and anonymous name-based entry are both supported. This README discrepancy was corrected on `2026-04-10` | `README.md` |
| `GroupsScreen.kt` treated as the active groups landing surface | The active landing surface is `GroupsListScreen.kt`; `GroupsScreen.kt` is deprecated | `docs/Revamp/LGD_UI_REVAMP_MASTER_PLAN.md` |
| Settings treated as feature-owned | Settings is still app-owned in `app/src/main/java/.../SettingsScreen.kt` | `docs/Revamp/LGD_UI_REVAMP_MASTER_PLAN.md` |
| Insights assumed to live in `feature/ledger/InsightsTab.kt` | Active Insights UI lives in `feature/insights/InsightsScreen.kt`; group-detail tab-shell styling still needs cleanup around app-owned routing | `docs/Revamp/LGD_UI_REVAMP_MASTER_PLAN.md` |
| Settlement assumed to be a feature-owned screen already | Settlement preview is still routed inline from `LetsGoDutchApp.kt`; `feature:settlement/SettlementCard.kt` exists but is currently unused | `docs/Revamp/LGD_UI_REVAMP_MASTER_PLAN.md` |

## Scoring Rubric

- Criticality
  - `High`: materially affects trust, layout correctness, or core task completion
  - `Medium`: improves clarity, polish, or repeated UX moments
  - `Low`: mostly delight or low-risk styling detail
- Usefulness
  - `High`: meaningfully improves daily product value or user comprehension
  - `Medium`: useful but not central to the main user loop
  - `Low`: nice-to-have or secondary flourish
- Complexity
  - `High`: cross-module work, backend work, data-model changes, or non-trivial UI architecture
  - `Medium`: feature-scoped UI plus state/data wiring
  - `Low`: isolated UI polish or small wiring
- Solo feasibility
  - `High`: realistic for one developer in a focused session or short sprint
  - `Medium`: feasible solo, but needs coordination across UI/data/backend or more testing time
  - `Low`: feasible solo only with significant time and scope management

## 1. Component Observation Matrix

### Design System

| ID | Observation | Audit signal | Criticality | Usefulness | Complexity | Solo feasibility |
|---|---|---|---|---|---|---|
| OBS-DS-01 | Fresh Mint colors mapped into the light color scheme | Likely done | Low | Medium | Low | High |
| OBS-DS-02 | Sora and DM Mono font wiring may be incomplete or dependency-dependent | Partial | Medium | Medium | Low | High |
| OBS-DS-03 | `Shape.kt` corner radii may still be default Material values | Partial | Low | Medium | Low | High |
| OBS-DS-04 | `GradientButton` may be missing or not fully built | Likely missing | High | High | Low | High |
| OBS-DS-05 | Dark color scheme may not be manually wired to the intended palette | Partial | High | High | Medium | High |

### Groups Landing

| ID | Observation | Audit signal | Criticality | Usefulness | Complexity | Solo feasibility |
|---|---|---|---|---|---|---|
| OBS-GR-01 | Contextual `TopGreetingBar` time-of-day greeting may be skipped | Partial | Low | Medium | Low | High |
| OBS-GR-02 | `BalanceSummaryCard` aggregate owed/owe hero may be missing | Likely missing | High | High | Medium | High |
| OBS-GR-03 | Group emoji derived from the group name hash may be omitted | Partial | Low | Low | Low | High |
| OBS-GR-04 | Per-group balance chip may be missing from group rows | Likely missing | High | High | Medium | High |
| OBS-GR-05 | Gradient FAB plus join CTA likely exists already | Likely done | Low | Medium | Low | High |

### App Tour Overlay

| ID | Observation | Audit signal | Criticality | Usefulness | Complexity | Solo feasibility |
|---|---|---|---|---|---|---|
| OBS-AT-01 | Dark gradient dialog styling may only be partially applied | Partial | Low | Medium | Low | High |
| OBS-AT-02 | Animated pill progress may still be old dot indicators | Likely missing | Medium | Medium | Low | High |
| OBS-AT-03 | Uppercase feature-chip typography detail may be incomplete | Partial | Low | Low | Low | High |
| OBS-AT-04 | Backdrop blur layer may be omitted | Partial | Low | Low | Medium | Medium |
| OBS-AT-05 | Tour step data and navigation are probably complete | Likely done | Low | Medium | Low | High |

### Settings

| ID | Observation | Audit signal | Criticality | Usefulness | Complexity | Solo feasibility |
|---|---|---|---|---|---|---|
| OBS-ST-01 | Profile card dark premium surface is probably implemented | Likely done | Low | Medium | Low | High |
| OBS-ST-02 | Avatar initials may be missing or timing-dependent | Partial | Low | Medium | Low | High |
| OBS-ST-03 | Row-style About section likely exists | Likely done | Low | Low | Low | High |
| OBS-ST-04 | `GradientButton` may not be used for Save Changes | Partial | Medium | Medium | Low | High |
| OBS-ST-05 | `WindowInsets.statusBars` may be missing | Likely missing | High | High | Low | High |
| OBS-ST-06 | Identity-aware Settings details and Account ID row may be missing | Likely missing | Medium | Medium | Low | High |

### Add Expense

| ID | Observation | Audit signal | Criticality | Usefulness | Complexity | Solo feasibility |
|---|---|---|---|---|---|---|
| OBS-AE-01 | Modal bottom-sheet container behavior is probably correct | Likely done | Low | Medium | Low | High |
| OBS-AE-02 | Mint-focused outlined field glow may be only partially implemented | Partial | Low | Low | Low | High |
| OBS-AE-03 | INR-prefixed amount field styling may be incomplete | Partial | Medium | Medium | Low | High |
| OBS-AE-04 | Member-chip payer selector may still be a simpler dropdown | Likely missing | Medium | High | Medium | High |
| OBS-AE-05 | Segmented split tabs may need manual styling work | Partial | Medium | High | Medium | High |

### Insights

| ID | Observation | Audit signal | Criticality | Usefulness | Complexity | Solo feasibility |
|---|---|---|---|---|---|---|
| OBS-IN-01 | Dark surface background may not be fully applied | Likely missing | Medium | Medium | Low | High |
| OBS-IN-02 | Real charting library and data wiring may be absent | Likely missing | High | High | Medium | High |
| OBS-IN-03 | Animated chart fill behavior likely depends on missing chart implementation | Likely missing | Medium | Medium | Medium | High |
| OBS-IN-04 | Pill-style tab selector may be only partially done | Partial | Low | Low | Low | High |
| OBS-IN-05 | Chart legend row may be missing | Likely missing | Medium | Medium | Low | High |

### Settlement

| ID | Observation | Audit signal | Criticality | Usefulness | Complexity | Solo feasibility |
|---|---|---|---|---|---|---|
| OBS-SE-01 | Confetti or hero illustration on settlement success may be absent | Likely missing | Low | Medium | Medium | High |
| OBS-SE-02 | Dark summary card may be only partially applied | Partial | Medium | Medium | Low | High |
| OBS-SE-03 | Avatar -> arrow -> avatar payment row component may be missing | Likely missing | Medium | High | Medium | High |
| OBS-SE-04 | PDF-first gate probably works, but disabled/success styling may be generic | Partial | Medium | Medium | Low | High |
| OBS-SE-05 | Animated success checkmark may be absent | Likely missing | Low | Medium | Low | High |

### Dark Mode And Theme Discipline

| ID | Observation | Audit signal | Criticality | Usefulness | Complexity | Solo feasibility |
|---|---|---|---|---|---|---|
| OBS-DM-01 | `isSystemInDarkTheme()` toggle is probably wired already | Likely done | Medium | High | Low | High |
| OBS-DM-02 | Dark-scheme primary/onPrimary values may not match the intended spec | Partial | Medium | Medium | Low | High |
| OBS-DM-03 | Hardcoded `Color(0x...)` literals may bypass the theme in screen files | Likely missing | High | High | Medium | Medium |
| OBS-DM-04 | Status-bar tint and edge-to-edge handling may be incomplete | Partial | Medium | Medium | Low | High |

## 2. Critical Gaps To Fix Now

| ID | Recommendation | Audit priority | Criticality | Usefulness | Complexity | Solo feasibility | Assessment note |
|---|---|---|---|---|---|---|---|
| GAP-01 | Add `BalanceSummaryCard` to the groups landing surface | Critical | High | High | Medium | High | Best UI-value-per-hour item in the report |
| GAP-02 | Replace placeholder analytics with real Insights charts | Critical | High | High | Medium | High | Needed to stop the Insights tab from feeling unfinished |
| GAP-03 | Remove hardcoded colors that bypass `MaterialTheme.colorScheme` | Critical | High | High | Medium | Medium | A full sweep is repetitive but important |
| GAP-04 | Use `GradientButton` consistently for primary CTAs | Important | Medium | High | Low | High | Mostly a design-system adoption task |
| GAP-05 | Apply `WindowInsets` and edge-to-edge cleanup across screens | Important | High | High | Low | High | High visual payoff on modern devices |
| GAP-06 | Replace app-tour dots with animated pill indicators | Important | Medium | Medium | Low | High | Small, visible polish task |
| GAP-07 | Show canonical Account ID in Settings | Important | Medium | Medium | Low | High | Very feasible and useful for debugging/trust |
| GAP-08 | Bundle or properly fall back the Sora font to avoid cold-launch flash | Polish | Low | Medium | Low | High | Nice polish, not core functionality |
| GAP-09 | Update README to remove Google-only auth wording | Polish | Medium | Medium | Low | High | Closed on `2026-04-10` in `README.md` |
| GAP-10 | Build richer settlement payment rows | Polish | Medium | High | Low | High | High screenshot/share value despite modest scope |

## 3. Competitor Gap Assessment

Opportunity summary from the audit: LGD already has strong settlement UX, split flexibility, and offline-friendly behavior. The largest competitive gaps are social activity, recurring expenses, and multi-currency support. The recommended narrative is INR-first and India-focused, not feature-for-feature parity.

| ID | Feature or gap | Audit stance | Criticality | Usefulness | Complexity | Solo feasibility | Assessment note |
|---|---|---|---|---|---|---|---|
| CMP-01 | Equal / Exact / Percentage / Custom splits | Existing strength | Low | High | Low | High | Preserve and market it; not a backlog issue |
| CMP-02 | PDF settlement export plus mark settled | Existing strength | Low | High | Low | High | Already a differentiator versus multiple competitors |
| CMP-03 | Anonymous no-account join | Existing strength | Low | High | Low | High | Important positioning strength for onboarding |
| CMP-04 | Activity feed / expense history | High-priority gap | High | High | Medium | Medium | Strong retention feature; not trivial but feasible solo |
| CMP-05 | Recurring / scheduled expenses | High-priority gap | High | High | High | Medium | Valuable but needs careful trigger architecture |
| CMP-06 | Expense comments / notes | Medium-priority gap | Medium | High | Low | High | Excellent solo-developer ROI |
| CMP-07 | Receipt photo attachment | Medium-priority gap | Medium | High | Medium | Medium | Needs storage, upload, and thumbnail handling |
| CMP-08 | Expense categories / tags | Medium-priority gap | Medium | High | Medium | High | Good foundation for better insights |
| CMP-09 | Push notifications for new expenses | High-priority gap with FCM already wired | High | High | Medium | Medium | Strong retention win if backend triggers are kept simple |
| CMP-10 | Multi-currency support | v2 gap | Medium | Medium | High | Low | Defer until core UI and India-first features settle |
| CMP-11 | In-app debt simplification | Existing strength | Low | High | Low | High | Keep documented and testable |
| CMP-12 | Offline mode | Existing strength | Low | Medium | Low | High | Useful positioning strength; no urgent work implied |
| CMP-13 | Group expense budgets | Differentiator gap | Medium | High | Medium | Medium | Worth doing before premium-only moonshots |
| CMP-14 | UPI payment deeplink | India differentiator gap | Medium | High | Low | High | One of the strongest solo-developer bets in the report |
| CMP-15 | WhatsApp share settlement summary | India differentiator gap | Medium | High | Low | High | Strong viral growth lever with minimal engineering cost |
| CMP-16 | Expense emoji reactions | Future delight gap | Low | Low | Low | High | Very deferrable compared with core product gaps |

### India-First Differentiators Called Out Separately By The Audit

| ID | Differentiator | Criticality | Usefulness | Complexity | Solo feasibility | Assessment note |
|---|---|---|---|---|---|---|
| IND-01 | UPI deep-link settlement | Medium | High | Low | High | Highest-value India-specific differentiator |
| IND-02 | WhatsApp share settlement summary | Medium | High | Low | High | Strong viral loop without backend dependency |
| IND-03 | Indian number formatting | Medium | High | Low | High | Small implementation, broad product payoff |
| IND-04 | Group budgets | Medium | High | Medium | Medium | Good differentiator after core UI closure |

## 4. Roadmap Recommendation Assessment

### Phase A - UI Completions

| ID | Feature | Audit effort tag | Criticality | Usefulness | Complexity | Solo feasibility | Assessment note |
|---|---|---|---|---|---|---|---|
| RM-A01 | Real charts in InsightsTab | Medium | High | High | Medium | High | Same recommendation as `GAP-02`; should stay near the top |
| RM-A02 | BalanceSummaryCard on Groups | Medium | High | High | Medium | High | Same recommendation as `GAP-01`; likely the best next UI task |
| RM-A03 | Settlement success animation | Low | Low | Medium | Medium | High | Nice delight layer once settlement layout is solid |
| RM-A04 | Account ID in Settings | Trivial | Medium | Medium | Low | High | Fast win; should ship with the settings cleanup pass |

### Phase B - Core Feature Upgrades

| ID | Feature | Audit effort tag | Criticality | Usefulness | Complexity | Solo feasibility | Assessment note |
|---|---|---|---|---|---|---|---|
| RM-B01 | FCM push notifications | Low because FCM is already wired | High | High | Medium | Medium | Strong retention feature; requires backend discipline more than UI work |
| RM-B02 | UPI deep-link settlement | Low | Medium | High | Low | High | Excellent India-first differentiator |
| RM-B03 | WhatsApp share settlement | Low | Medium | High | Low | High | High leverage for sharing and reminder nudges |
| RM-B04 | Expense categories | Medium | Medium | High | Medium | High | Good setup for richer analytics |
| RM-B05 | Expense notes / comments | Low | Medium | High | Low | High | Strong trust feature with very manageable scope |
| RM-B06 | Expense date picker | Low | Medium | High | Low | High | Practical and important for real-world backdated entry |
| RM-B07 | Activity feed (group timeline) | Medium | High | High | Medium | Medium | Worth doing, but after the current UI completion work |
| RM-B08 | Indian number formatting | Trivial | Medium | High | Low | High | Tiny task with product-wide improvement |

### Phase C - Advanced Features

| ID | Feature | Audit effort tag | Criticality | Usefulness | Complexity | Solo feasibility | Assessment note |
|---|---|---|---|---|---|---|---|
| RM-C01 | Receipt photo attachment | Medium | Medium | High | Medium | Medium | Good medium-term enhancement once storage rules are settled |
| RM-C02 | Recurring expenses | High | High | High | High | Medium | Valuable but architecture-heavy for a solo developer |
| RM-C03 | Group budget tracker | Medium | Medium | High | Medium | Medium | A better solo-developer differentiator than OCR or multi-currency |
| RM-C04 | Smart expense OCR | High | Low | Medium | High | Low | Attractive, but easy to overinvest in too early |
| RM-C05 | Group archive (settled history) | Medium | Medium | Medium | Medium | Medium | Good candidate after settlement UX and data retention rules are clarified |
| RM-C06 | Multi-currency (v2) | High | Medium | Medium | High | Low | Should stay explicitly deferred to v2 |

## 5. Prompt Coverage From The Audit

The prompt section is mostly execution scaffolding for items already listed above. It is still included here for completeness.

| ID | Prompt title | Maps to | Criticality | Usefulness | Complexity | Solo feasibility | Assessment note |
|---|---|---|---|---|---|---|---|
| PR-01 | Build BalanceSummaryCard for GroupsScreen | `GAP-01`, `RM-A02` | High | High | Medium | High | Good first coding session |
| PR-02 | Add Vico charts to InsightsTab | `GAP-02`, `RM-A01` | High | High | Medium | High | Depends on final file ownership and chart-library choice |
| PR-03 | UPI deep-link plus WhatsApp share on Settlement | `RM-B02`, `RM-B03` | Medium | High | Low | High | Very strong solo-developer candidate |
| PR-04 | Activate FCM push notifications | `RM-B01` | High | High | Medium | Medium | Backend function setup is the main constraint |
| PR-05 | Expense categories plus notes | `RM-B04`, `RM-B05` | Medium | High | Medium | High | Good combined data-model and UX sprint |

## 6. Sprint Plan Coverage From The Audit

| Sprint | Audit sprint theme | Criticality | Usefulness | Complexity | Solo feasibility | Assessment note |
|---|---|---|---|---|---|---|
| SP-01 | BalanceSummaryCard plus Indian currency formatter | High | High | Medium | High | Excellent first sprint for visible progress |
| SP-02 | InsightsTab real charts | High | High | Medium | High | Best second sprint if screen ownership is already clear |
| SP-03 | AppTour pill dots plus settlement animation plus Account ID | Medium | Medium | Medium | High | Good polish sprint after the biggest gaps close |
| SP-04 | UPI deep-link plus WhatsApp share | Medium | High | Low | High | Very feasible and strategically strong |
| SP-05 | FCM notifications plus expense date picker | High | High | Medium | Medium | Good, but notifications need careful backend handling |
| SP-06 | Expense categories plus notes plus category insights | Medium | High | Medium | High | Strong mid-stage feature/value sprint |
| SP-07 | Activity feed (group timeline) | High | High | Medium | Medium | Bigger scope; keep after UI and data-model cleanup |
| SP-08 | Dark-mode audit plus WindowInsets plus GradientButton sweep plus README update | High | High | Medium | High | Necessary cleanup sprint; README portion is now partially complete |

## 7. Recommended Solo-Developer Sequencing

### Do Now

| Order | Item | Why |
|---|---|---|
| 1 | `GAP-01` / `RM-A02` BalanceSummaryCard on groups | Highest UI payoff with moderate scope |
| 2 | `GAP-02` / `RM-A01` Real insights charts | Removes the most obvious unfinished screen state |
| 3 | `GAP-03` / `SP-08` Theme cleanup and hardcoded-color sweep | Prevents drift while the revamp is still moving |
| 4 | `GAP-05` / `SP-08` WindowInsets and edge-to-edge cleanup | Fixes visible layout quality across many screens |
| 5 | `GAP-07` / `RM-A04` Account ID in Settings | Very fast and useful trust/debug win |
| 6 | `RM-B08` Indian number formatting | Small task with product-wide benefit |

### Do Next

| Order | Item | Why |
|---|---|---|
| 7 | `RM-B02` UPI deep-link settlement | Strong India-first differentiation with low scope |
| 8 | `RM-B03` WhatsApp share settlement | Viral and practical follow-on to UPI |
| 9 | `RM-B05` Expense notes | Low-complexity trust feature |
| 10 | `RM-B06` Expense date picker | Practical everyday improvement |
| 11 | `RM-B04` Expense categories | Sets up richer insights and categorization |
| 12 | `RM-B01` FCM push notifications | High value, but slightly more operational complexity |

### Defer Until Core UX Is Stable

| Order | Item | Why |
|---|---|---|
| 13 | `CMP-04` / `RM-B07` Activity feed | Useful, but larger than the immediate UI completion tasks |
| 14 | `RM-C03` Group budget tracker | Good differentiator, but not before the core backlog above |
| 15 | `RM-C01` Receipt attachment | Needs storage and media flow discipline |
| 16 | `RM-C05` Group archive | Better after settlement ownership is cleaned up |
| 17 | `RM-C02` Recurring expenses | High-complexity workflow and scheduler problem |
| 18 | `RM-C04` Smart OCR | Good demo feature, weaker near-term ROI |
| 19 | `RM-C06` Multi-currency | Keep explicitly as v2 |
| 20 | `CMP-16` Emoji reactions | Easy to defer without product risk |

## 8. Bottom Line

- The audit is directionally useful, but it mixed real gaps with documentation drift and inference.
- The best solo-developer path is to finish the visible UI completions first, then ship India-first differentiators, then move into deeper retention and data-richness features.
- The single highest-risk documentation gap from the audit was the auth/status mismatch; that is now corrected in `README.md`.
