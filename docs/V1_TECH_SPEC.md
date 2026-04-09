# Let's Go Dutch - V1 Technical Specification

## Product Scope

- Android-only v1 (`minSdk 26`, phone layout focus)
- Authenticated usage via Google Sign-In only
- Group expense tracking with:
  - all-member or selected-member participation
  - split modes: equal, exact, percentage, custom
- Two group views:
  - ledger tab (table-style records)
  - insights tab (pie/bar/monthly trend)
- Owner-only full settlement:
  - generate PDF first
  - only then clear all transactions
  - no in-app cycle history after settlement

## Roles and Permissions

- `OWNER` (group creator/admin)
  - edit/delete any expense in group
  - mark settlement complete
  - handles exited member liability choice
- `MEMBER`
  - edit/delete only own expenses
  - can leave with outstanding balance

Ownership transfer in v1:
- if owner exits, earliest joined active member becomes new owner automatically.

## Core Business Rules

1. Group max members: 50
2. Currency: INR in v1; schema keeps `currencyCode` for future multi-currency
3. Invite links:
- reusable
- 7-day expiry
- open join for Google-authenticated accounts
4. Exit with outstanding:
- owner chooses:
  - distribute equally among active remaining members
  - absorb by owner
5. Settlement:
- blocked if PDF generation fails
- on success, transactions are deleted from group

## Realtime Database Node Proposal

```text
users/{userId}
  profile: { displayName, email, photoUrl, createdAt }

groups/{groupId}
  meta: { name, ownerUserId, currencyCode, maxMembers, createdAt, active }
  invite: { code, expiresAt, reusable }

groupMembers/{groupId}/{userId}
  { role, joinedAt, active }

expenses/{groupId}/{expenseId}
  {
    title, amountPaise, currencyCode, paidByUserId,
    splitType, participantUserIds[],
    shares: [{ userId, amountPaise?, percentage?, customUnits? }],
    createdByUserId, createdAt, updatedAt
  }

balances/{groupId}/{userId}
  { netPaise, updatedAt }

notifications/{userId}/{notificationId}
  { type, message, groupId, createdAt, read }
```

## Firebase Security Rule Intent (V1)

- authenticated users only
- group reads allowed only for active members
- expense write allowed if:
  - actor is expense creator, or
  - actor is group owner
- settlement write allowed only for group owner

## Modules

- `app`: navigation, orchestration, shell screens
- `core:common`: coroutine dispatchers and shared helpers
- `core:model`: serializable domain models
- `core:data`: repositories + split engine
- `core:designsystem`: Fresh Mint theme (light/dark)
- `feature:*`: auth, groups, expenses, ledger, insights, settlement UI

## Implemented in Scaffold

- multi-module Gradle structure
- Fresh Mint base theme with dark mode support
- navigation shell with auth -> groups -> group detail
- group detail with:
  - tab switch (ledger / insights)
  - add expense popup
  - owner-only settlement action
- split engine with unit tests
- Firebase-backed repositories for auth/groups/expenses/settlement
- in-memory repositories retained as runtime fallback
- Google Sign-In integration in app shell
- group create + invite-code join UI on groups screen
- invite link sharing and deep-link join processing
- full split-entry popup UX for exact/percentage/custom modes
- client-side branded settlement PDF generation with chart + table output
- settlement dispatch tracking to members before settlement finalization

## Next Build Milestones

1. Integrate FCM event routing and in-app notification presentation.
2. Replace deprecated Google Sign-In API path with Credential Manager.
3. Add instrumentation tests for auth/group/expense/settlement flows.
4. Add explicit member-side acknowledgment flow for settlement PDF receipt.
5. Validate release-signed Android App Links against `buddingintents.com` and capture final browser-to-app QA evidence.
