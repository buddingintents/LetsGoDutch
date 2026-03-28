# Let's Go Dutch

Let's Go Dutch is an Android expense-sharing app for friend/family groups.  
It is built with Kotlin, Jetpack Compose, Firebase Auth, Firebase Realtime Database, and Firebase Cloud Messaging.

## Play Store Listing (Draft)

### Short Description

Split group expenses, track personal spends, and settle with shareable PDFs.

### What's New (Draft)

- Anonymous sign-in is now device-aware, so returning users on the same device can sync their existing data more reliably after reinstall.
- Group owners can add a group description, view invite expiry, auto-renew invite codes, and manually renew invites from Group Details.
- Expense entry now uses a compact multi-select member picker, with group-level defaults for whether members start selected or unselected.
- Owner permissions are more consistent across group management actions, and placeholder owner/member claim flows are clearer.
- Improved legacy account migration and safer handling of Firebase permission-denied responses to reduce app crashes.

### Full Description

Let's Go Dutch helps friends, families, and roommates manage shared money without confusion.

Create a group, invite members with a code or link, add expenses, and split the total your way:

- Equal split
- Exact amounts
- Percentage split
- Custom split

Keep everyone aligned with:

- A clear group ledger
- Insights showing who should pay and who should receive
- Suggested transfers to settle faster
- Owner-only settlement preview before final confirmation
- Settlement PDF generation and sharing

You also get personal money tools:

- Self Expense Tracker
- Search and filters (period and minimum amount)
- Monthly summaries
- Personal expense PDF export

Plus everyday utility features:

- To-Do task tracker with swipe actions (complete/cancel)
- Group update notifications
- Light, Dark, and System theme support

Built for INR-first usage, Let's Go Dutch is ideal for trips, flatmates, events, and family expense sharing.

## Features

- Google Sign-In authentication
- Continue with Name option with device-aware anonymous restore for same-device return users
- Group lifecycle:
  - create group
  - join with invite code
  - share invite deep links
  - group description support
  - invite expiry visibility and manual renew option
  - auto-renew invite support
  - owner-only delete group
  - owner-only remove member
  - placeholder member claim flow for users who join later
- Expense flow:
  - add group expenses
  - split modes: Equal, Exact, Percentage, Custom
  - compact multi-select participant picker
  - group-level default for selected/unselected expense participants
  - payer/member selection with member avatar support
  - owner-level delete permissions
- Group tabs:
  - Ledger
  - Insights
- Settlement flow:
  - owner-only trigger
  - dedicated **Settlement Preview** screen before final settle
  - preview includes PDF summary + suggested transfer list + final confirmation
  - final settlement generates PDF, dispatches to members, and clears active ledger/balances
- Personal expense tracking:
  - add/delete personal expenses
  - filter by period, title, and minimum amount
  - monthly report summary
  - export filtered report as PDF
- Personal to-do tasks:
  - add tasks
  - swipe right to mark complete
  - swipe left to cancel
- Push + local fallback notifications
- Theme switching (Light / Dark / System)
- AdMob banner placement on groups screen

## Tech Stack

- Kotlin + Coroutines + Flow
- Jetpack Compose + Navigation Compose
- Firebase:
  - Auth
  - Realtime Database
  - Cloud Messaging
  - Analytics
  - Crashlytics
- Google Mobile Ads SDK

## Project Modules

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

## Prerequisites

- Android Studio (latest stable recommended)
- JDK 17 (Android Studio JBR works)
- Firebase project with:
  - Google Sign-In enabled
  - Anonymous Sign-In enabled
  - Realtime Database created
  - FCM enabled
- `google-services.json` placed at `app/google-services.json`

## Deep Links

Supported invite link formats:

- `letsgodutch://join/<INVITE_CODE>`
- `https://buddingintents.com/join/<INVITE_CODE>`
- `http://buddingintents.com/join/<INVITE_CODE>`

## Build & Run

Windows:

```powershell
.\gradlew.bat :app:assembleDebug
```

macOS/Linux:

```bash
./gradlew :app:assembleDebug
```

Install debug app:

```bash
./gradlew :app:installDebug
```

## Versioning

The app version is auto-derived during build:

- `versionCode`:
  - `VERSION_CODE` env var (if provided), else
  - git commit count (`git rev-list --count HEAD`), else
  - fallback default
- `versionName`:
  - `VERSION_NAME` env var (if provided), else
  - `1.2.<versionCode>`

This enables automatic version bumps in CI without manual edits.

## Telemetry

Settlement funnel events now include:

- `no_expense_settle_attempt`
- `pdf_generate_fail`
- `dispatch_fail`

Use Firebase Analytics + Crashlytics dashboards to track and debug settlement failures.

## Firebase Realtime Database Setup Notes

- Authenticated users, including anonymous users, must be able to read and write their own:
  - `users/{userId}/profile`
  - `userGroups/{userId}`
  - `todoTasks/{userId}`
  - `personalExpenses/{userId}`
  - `notifications/{userId}`
  - `fcmTokens/{userId}`
- The current app also reads shared group data from:
  - `groups`
  - `groupMembers/{groupId}`
  - `expenses/{groupId}`
  - `balances/{groupId}`
  - `settlementDispatch/{groupId}`
- Device-aware anonymous restore and legacy-user migration depend on profile and membership reads succeeding during sign-in. If your rules block those reads, the app may stay signed in but older data will not merge automatically.
- Add Realtime Database indexes for the query paths currently used by the app:
  - `groups.ownerUserId`
  - `users.profile.identifier`
  - `users.profile.deviceId`
  - `notifications/{userId}.createdAtEpochMs`
- If you tighten rules for production, move cross-user migration and invite-code lookup to a trusted backend or Cloud Functions rather than broad client-side reads.

## Notes

- Currency is INR-first in current implementation.
- Keep Firebase Realtime Database indexes/rules in sync with query paths used by repositories.
- Avoid committing secrets (`google-services.json`, key files, local env files); use `.gitignore` and CI secret stores.
