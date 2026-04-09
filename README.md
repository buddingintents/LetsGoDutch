# Let's Go Dutch

Let's Go Dutch is an Android expense-sharing app for friend, family, flatmate, and event groups.
It is built with Kotlin, Jetpack Compose, Firebase Auth, Firebase Realtime Database, Firebase Cloud Messaging, and Google Mobile Ads.

The app is INR-first today, supports both Google sign-in and name-only anonymous entry, and is currently in the middle of the `Fresh Mint` UI revamp.

## Current Status

| Field | Value |
|---|---|
| Platform | Android |
| Package ID | `com.buddingintents.letsgodutch` |
| Min SDK | 26 |
| Target SDK | 35 |
| UI Stack | Jetpack Compose + Material 3 |
| Architecture | multi-module app with `core:*` and `feature:*` modules |
| Backend | Firebase Auth + Realtime Database + FCM |
| Current UI Phase | `Fresh Mint` revamp, Phase 1 complete, `R-02` completed and verified, `R-03` next, partial `R-04` polish already landed in ledger and insights |

## Revamp Status And Audit Note

The revamp source of truth is [docs/Revamp/LGD_UI_REVAMP_MASTER_PLAN.md](docs/Revamp/LGD_UI_REVAMP_MASTER_PLAN.md).

As of `2026-04-10`:

- Phase 1 is complete
- Phase 2 UI revamp has `R-02` completed and verified
- `R-03` is the next formal milestone
- partial `R-04` polish already exists in ledger and insights
- both Google sign-in and anonymous name-based entry are supported

The audit artifact at [docs/Revamp/Audit/LGD_UIAudit_Roadmap.html](docs/Revamp/Audit/LGD_UIAudit_Roadmap.html) is useful as a recommendation backlog, but it is inference-based from documentation, commit history, and revamp prompts. Treat it as a prioritization aid, not as a source-reviewed implementation checklist.

## What the App Does

### Shared Group Expenses

- Create a group
- Join a group with an invite code
- Share invite links or raw invite codes
- Add a group description
- View invite expiry and auto-renew behavior
- Renew non-auto-renewing invites manually
- Track owner and member roles
- Remove members or delete groups with owner-only permissions

### Expense Entry and Balance Tracking

- Add expenses into a group
- Split by:
  - Equal
  - Exact
  - Percentage
  - Custom
- Choose a payer
- Choose participants with a compact multi-select flow
- Respect a group-level default for whether participants start selected or unselected
- Recompute balances per group as expenses change

### Group Insights and Settlement

- View a ledger surface
- View an insights surface
- See who owes and who should receive
- Review suggested settlement transfers
- Open a dedicated settlement preview before final confirmation
- Generate and share a settlement PDF
- Dispatch settlement PDFs to members before final settlement
- Clear expenses and balances after a successful settlement

### Personal Money Tools

- Self Expense Tracker
- Title, period, and minimum-amount filters
- Monthly summary cards
- PDF export for filtered personal expenses
- To-do task tracker with swipe-to-complete and swipe-to-cancel

### Notifications

- Firebase-backed notification ingestion for group updates
- Local notification rendering for incoming notification records
- Daily local reminder at `10:00 AM` device-local time when unsettled groups still exist
- No daily reminder when all current groups are settled

## Recent Product Changes

### Groups Landing Revamp

Recent updates to the post-login groups landing surface include:

- New `Group Summary` hero card
- Aggregate signed-in-user settlement summary across groups
- Per-group settlement pill showing:
  - `They owe you`
  - `You owe them`
  - `All settled`
- More compact hero and group card heights to show more information on screen
- Ambient blurred background art behind the groups surface
- Invite-code pill resized so settlement status can share the same row

### Login Screen Polish

- Refined auth card and hierarchy in the `Fresh Mint` style
- Transparent outlined name field without the previous filled placeholder background
- Updated anonymous sign-in copy for clearer intent

### Reminder Flow

- Daily unsettled-group reminder scheduling at `10:00 AM` local time
- Alarm rescheduling on:
  - app start
  - reboot
  - package replace
  - time change
  - timezone change

## Audit-Driven Follow-Ups

Current high-value follow-ups called out by the audit backlog:

- groups landing balance summary and per-group balance context
- real insights charts instead of placeholder analytics
- theme cleanup: remove hardcoded colors, finish dark-mode wiring, and use branded primary CTAs consistently
- edge-to-edge cleanup with status-bar insets on all major scaffolds
- settings and settlement polish: account ID visibility, richer payment rows, and better success feedback

## Play Store Listing Draft

### Short Description

Split group expenses, track personal spends, and settle with shareable PDFs.

### What's New Draft

- Fresh Mint UI revamp continues with a refreshed Groups home
- New `Group Summary` hero with signed-in user settlement totals
- Per-group `You owe` / `They owe you` amounts on the Groups screen
- More compact group cards to fit more on screen
- Polished login experience and cleaner field styling
- Daily reminders for unsettled groups
- Stability, readability, and UX improvements

### Full Description Draft

Let's Go Dutch helps friends, families, roommates, and trip groups manage shared money without confusion.

Create a group, invite members with a code or link, add expenses, and split totals your way:

- Equal split
- Exact amounts
- Percentage split
- Custom split

Keep everyone aligned with:

- a clear group ledger
- insights showing who should pay and who should receive
- suggested transfers to settle faster
- owner-only settlement preview before final confirmation
- settlement PDF generation and sharing

You also get personal money tools:

- Self Expense Tracker
- search and filters
- monthly summaries
- personal expense PDF export
- a to-do task tracker with swipe actions

Built for INR-first usage, Let's Go Dutch is designed for trips, flatmates, family spending, events, and everyday shared expense tracking.

## Module Layout

| Module | Responsibility |
|---|---|
| `app` | application bootstrap, navigation, app shell, notifications, app-owned screens |
| `core:common` | shared coroutine helpers and common utilities |
| `core:model` | serializable domain models such as `Group`, `Expense`, `Balance`, `UserProfile`, and money helpers |
| `core:data` | Firebase and in-memory repositories, split engine, auth/data orchestration |
| `core:designsystem` | Fresh Mint theme, colors, type, shapes, and shared UI primitives |
| `feature:auth` | authentication screen UI |
| `feature:groups` | groups landing UI and deprecated legacy groups surface |
| `feature:expenses` | add-expense dialog UI |
| `feature:ledger` | group ledger screen |
| `feature:insights` | group insights screen |
| `feature:settlement` | settlement-related feature code and helper UI |

## Important Screens and Ownership

| Surface | File |
|---|---|
| Auth | `feature/auth/src/main/java/com/buddingintents/letsgodutch/feature/auth/AuthScreen.kt` |
| Canonical groups landing | `feature/groups/src/main/java/com/buddingintents/letsgodutch/feature/groups/GroupsListScreen.kt` |
| Deprecated groups variant | `feature/groups/src/main/java/com/buddingintents/letsgodutch/feature/groups/GroupsScreen.kt` |
| Add expense | `feature/expenses/src/main/java/com/buddingintents/letsgodutch/feature/expenses/AddExpenseDialog.kt` |
| Ledger | `feature/ledger/src/main/java/com/buddingintents/letsgodutch/feature/ledger/LedgerScreen.kt` |
| Insights | `feature/insights/src/main/java/com/buddingintents/letsgodutch/feature/insights/InsightsScreen.kt` |
| Settings | `app/src/main/java/com/buddingintents/letsgodutch/SettingsScreen.kt` |
| Personal tracker | `app/src/main/java/com/buddingintents/letsgodutch/PersonalExpenseTrackerScreen.kt` |
| Todo tracker | `app/src/main/java/com/buddingintents/letsgodutch/TodoTasksScreen.kt` |
| App tour | `app/src/main/java/com/buddingintents/letsgodutch/ui/AppTourOverlay.kt` |
| Notification receiver | `app/src/main/java/com/buddingintents/letsgodutch/notifications/` |

## Tech Stack

### Language and UI

- Kotlin
- Coroutines
- Flow
- Jetpack Compose
- Navigation Compose
- Material 3

### Firebase

- Firebase Authentication
- Firebase Realtime Database
- Firebase Cloud Messaging
- Firebase Analytics
- Firebase Crashlytics

### Google / Android Libraries

- Credential Manager
- Google Identity Services
- Google Mobile Ads SDK
- Google Play in-app update APIs
- Google Play review APIs

## Authentication Modes

### Continue with Google

- Uses Firebase Auth with Google identity
- Intended for portable identity across reinstalls and device changes

### Continue with Name

- Uses anonymous Firebase-backed sessions
- Intended for fast entry and better anonymity
- The app keeps device-aware anonymous identity hints for same-device return flows when backend rules and identifiers allow it

## Core Business Rules

- Group max members: `50`
- Currency is INR-first in the current product
- Invite links are reusable
- Invite expiry is `7` days unless auto-renew behavior keeps them live
- Settlement is blocked if the settlement PDF generation fails
- Successful settlement clears active expenses and balances for the group
- Ownership transfer happens automatically if the current owner exits and another active member remains

## Notifications

### In-App / Push Notification Path

- The app reads notification records from `notifications/{userId}/{notificationId}`
- FCM token storage is expected under `fcmTokens/{userId}/{tokenId}`
- Push delivery itself must come from trusted backend code, not directly from the Android client

### Daily Local Reminder

The app schedules a local daily reminder for unsettled groups:

- time: `10:00 AM` local device time
- message behavior:
  - generated only when unsettled groups exist
  - suppressed when all groups are settled
- schedule recovery:
  - app start
  - reboot
  - app replace/update
  - timezone change
  - manual time change

### Notification Channel

- channel id: `letsgodutch_updates`

More detail: [docs/FIREBASE_TELEMETRY_AND_FCM_SETUP.md](docs/FIREBASE_TELEMETRY_AND_FCM_SETUP.md)

## Firebase Realtime Database Expectations

### User-Owned Paths

Authenticated users, including anonymous users, should be able to read and write their own:

- `users/{userId}/profile`
- `userGroups/{userId}`
- `todoTasks/{userId}`
- `personalExpenses/{userId}`
- `notifications/{userId}`
- `fcmTokens/{userId}`

### Shared Group Paths

The app reads or writes shared group data from:

- `groups`
- `groupMembers/{groupId}`
- `expenses/{groupId}`
- `balances/{groupId}`
- `settlementDispatch/{groupId}`

### Indexes and Query Support

Add Realtime Database indexes for:

- `groups.ownerUserId`
- `users.profile.identifier`
- `users.profile.deviceId`
- `notifications/{userId}.createdAtEpochMs`

### Restore / Migration Note

Device-aware anonymous restore and older membership migration depend on profile and membership reads succeeding during sign-in.
If production rules block those reads, sign-in may still succeed but historical same-device data may not merge.

## Deep Links

Supported invite link formats:

- `letsgodutch://join/<INVITE_CODE>`
- `https://buddingintents.com/join/<INVITE_CODE>`
- `http://buddingintents.com/join/<INVITE_CODE>`

Validation status as of `2026-04-08`:

- Custom-scheme invite links are working on-device
- The app correctly parses `https://buddingintents.com/join/<INVITE_CODE>` after that URI reaches `MainActivity`
- Automatic browser-to-app opening for `https://buddingintents.com/join/<INVITE_CODE>` still needs release-signed Android App Links verification

Detailed notes: [docs/DEEP_LINK_VALIDATION_2026-04-08.md](docs/DEEP_LINK_VALIDATION_2026-04-08.md)

## Local Development Setup

### Prerequisites

- Android Studio, latest stable preferred
- JDK 17
- Android SDK with platform tools
- Firebase project with:
  - Google sign-in enabled
  - Anonymous sign-in enabled
  - Realtime Database created
  - FCM enabled
- `app/google-services.json` present locally

### Required Local Files

| File | Purpose | Tracked |
|---|---|---|
| `app/google-services.json` | Firebase Android app configuration | No |
| `local.properties` | local Android SDK and machine paths | No |
| `keystore.properties` | release signing config | No |

### Local Safety

The repository `.gitignore` already ignores:

- Firebase config files
- keystores and signing files
- local Gradle / Android sandbox state
- release bundles and APKs
- generic credential files and secrets

## Build Commands

### Debug Build

Windows:

```powershell
.\gradlew.bat :app:assembleDebug
```

macOS / Linux:

```bash
./gradlew :app:assembleDebug
```

### Install Debug Build

```powershell
.\gradlew.bat :app:installDebug
```

### Compile a Specific Module

```powershell
.\gradlew.bat :feature:auth:compileDebugKotlin
```

### Release APK

```powershell
.\gradlew.bat :app:assembleRelease
```

### Release App Bundle

```powershell
.\gradlew.bat :app:bundleRelease
```

## Release Signing

Release artifact tasks fail fast if signing is not configured.

The app accepts signing values from either:

- `keystore.properties` at the repo root
- environment variables:
  - `ANDROID_STORE_FILE`
  - `ANDROID_STORE_PASSWORD`
  - `ANDROID_KEY_ALIAS`
  - `ANDROID_KEY_PASSWORD`

### Generate an Upload Keystore

PowerShell helper:

```powershell
.\scripts\generate-upload-keystore.ps1
```

The script:

- generates a PKCS12 keystore
- writes `keystore.properties` unless told not to
- prints SHA-1 and SHA-256 fingerprints
- reminds you to register the fingerprints in Firebase

## Versioning

Versioning is derived during the build:

- `versionCode`:
  - `VERSION_CODE` env var if provided
  - otherwise git commit count
  - otherwise a fallback minimum
- `versionName`:
  - `VERSION_NAME` env var if provided
  - otherwise `1.2.<versionCode>`

This supports CI-based versioning without editing source files for every release.

## Play Store Release Checklist

Before publishing:

1. Confirm `app/google-services.json` is correct for the production Firebase project and remains untracked.
2. Confirm release signing is configured through `keystore.properties` or environment variables.
3. Build the release bundle:

```powershell
.\gradlew.bat :app:bundleRelease
```

4. Verify version code and version name.
5. Review release notes and Play Store `What's New` text.
6. Confirm no local secrets, bundles, or keystores are staged for commit.
7. Validate release-signed Android App Links for `buddingintents.com`.
8. Upload the generated `.aab` to Play Console.

## Telemetry and Monitoring

The app is wired for:

- Firebase Analytics event logging
- Firebase Crashlytics fatal and non-fatal reporting
- FCM token registration
- notification display handling on-device

Examples of settlement funnel events currently called out in project docs:

- `no_expense_settle_attempt`
- `pdf_generate_fail`
- `dispatch_fail`

## Testing and QA Notes

Recent revamp documentation records:

- `R-02` first-impression milestone completed and verified
- revamp source of truth remains the master plan, with `R-03` next and partial `R-04` polish already landed early
- groups landing verified across zero, one, and many-group states
- on-device verification for the new groups hero, denser cards, and per-group settlement pills
- on-device verification for daily unsettled-group reminder behavior
- [docs/Revamp/Audit/LGD_UIAudit_Roadmap.html](docs/Revamp/Audit/LGD_UIAudit_Roadmap.html) captures inferred UI gaps and roadmap ideas from the documentation state at audit time
- [docs/Revamp/Audit/LGD_UI_AUDIT_ASSESSMENT_2026-04-10.md](docs/Revamp/Audit/LGD_UI_AUDIT_ASSESSMENT_2026-04-10.md) translates that audit into a criticality/usefulness/complexity/solo-feasibility matrix

Revamp references:

- [docs/Revamp/LGD_UI_REVAMP_MASTER_PLAN.md](docs/Revamp/LGD_UI_REVAMP_MASTER_PLAN.md)
- [docs/Revamp/Milestones/R-02-first-impressions-and-navigation.md](docs/Revamp/Milestones/R-02-first-impressions-and-navigation.md)

## Known Limitations and Follow-Ups

- Automatic Android App Links verification for release-signed builds still needs final end-to-end confirmation
- Settlement preview UI is still routed inline from `LetsGoDutchApp.kt` and has not yet been fully extracted into a feature-owned screen
- Some active screens still live in `app/` and not in dedicated feature modules
- Backend push sending is not performed by the Android client and still requires Cloud Functions or another trusted server path

## Related Documentation

- [docs/V1_TECH_SPEC.md](docs/V1_TECH_SPEC.md)
- [docs/FIREBASE_TELEMETRY_AND_FCM_SETUP.md](docs/FIREBASE_TELEMETRY_AND_FCM_SETUP.md)
- [docs/DEEP_LINK_VALIDATION_2026-04-08.md](docs/DEEP_LINK_VALIDATION_2026-04-08.md)
- [docs/Revamp/LGD_UI_REVAMP_MASTER_PLAN.md](docs/Revamp/LGD_UI_REVAMP_MASTER_PLAN.md)
- [docs/Revamp/UI_REVAMP_SUGGESTION_TRACKER.md](docs/Revamp/UI_REVAMP_SUGGESTION_TRACKER.md)
- [docs/Revamp/Audit/LGD_UIAudit_Roadmap.html](docs/Revamp/Audit/LGD_UIAudit_Roadmap.html)
- [docs/Revamp/Audit/LGD_UI_AUDIT_ASSESSMENT_2026-04-10.md](docs/Revamp/Audit/LGD_UI_AUDIT_ASSESSMENT_2026-04-10.md)

## Security and Repo Hygiene

- Do not commit:
  - `google-services.json`
  - `keystore.properties`
  - keystores or certificates
  - generated APKs or AABs
  - local env or credentials files
- Review `qa_artifacts/` before external sharing if screenshots or captured device data may contain personal information
- Keep Firebase rules and indexes aligned with repository query paths
