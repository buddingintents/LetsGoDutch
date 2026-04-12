# Home Group Settlement RCA

Date: `2026-04-11`
Device: `SM_A546E` (`RZCWA1260BK`)
Group: `home group`
Group ID: `-OptKUmYfNk9_q3rxpwL`

## Summary

The group is not blocked by UPI capture anymore. It is blocked by identity drift across `groups`, `groupMembers`, `expenses`, `balances`, and `settlementActivities`.

The recorded UPI success is tied to the old account ID for `ankit`, while current expenses and owner state use a newer account ID. Because balance recomputation trusts both sources, the payment is applied to the wrong user identity and the group stays unsettled.

## Device Evidence

Pulled from local Firebase cache:

- `groups/-OptKUmYfNk9_q3rxpwL.ownerUserId = jPeMkZM9JBhPiPEXmeynlNSmlau1`
- `groupMembers/-OptKUmYfNk9_q3rxpwL` still contains:
  - `kwjVfqfWrbfVTl7J1GEffXoyPox2` as `OWNER`, display name `ankit`
  - no member row for `jPeMkZM9JBhPiPEXmeynlNSmlau1`
- `userGroups` contains the same group under both `jPe...` and `kwj...`
- current expenses under the group now reference `jPe...` as participant/creator
- successful UPI activity still references `kwj... -> Tr2NNmlE3hRtBx5Edxo9yObLPzd2`
- balances contain mixed identities:
  - `jPe... = -100`
  - `kwj... = +100`
  - `Tr2... = 0`

This is why the UI can surface a phantom `Member` row: balances can reference a user ID that is not present in active `groupMembers`.

## Root Cause

Primary gap:

- [FirebaseAuthRepository.kt](C:/Users/shiva/Codex/LGD/LGD_10Apr/core/data/src/main/java/com/buddingintents/letsgodutch/core/data/repository/firebase/FirebaseAuthRepository.kt#L669) merges `groupMembers`, `expenses`, `balances`, and `settlementDispatch.members`, but does not migrate `settlementActivities`.

Secondary gap:

- [FirebaseSettlementActivitySupport.kt](C:/Users/shiva/Codex/LGD/LGD_10Apr/core/data/src/main/java/com/buddingintents/letsgodutch/core/data/repository/firebase/FirebaseSettlementActivitySupport.kt#L49) recomputes balances from canonical expenses plus successful settlement activities, so any old user ID left in `settlementActivities` becomes part of the live balance state.

Accounting gap:

- [SettlementUpiTransaction.kt](C:/Users/shiva/Codex/LGD/LGD_10Apr/core/model/src/main/java/com/buddingintents/letsgodutch/core/model/SettlementUpiTransaction.kt#L53) grouped successful payments by `transferKey` and kept only the latest record. That is incorrect when the same pair owes the same amount again after later expenses, because two real successful payments can share the same `transferKey` at different times.

Symptom amplifier:

- [LetsGoDutchApp.kt](C:/Users/shiva/Codex/LGD/LGD_10Apr/app/src/main/java/com/buddingintents/letsgodutch/LetsGoDutchApp.kt#L5600) falls back to the label `Member` when a balance user ID has no active member record.

## Fix Proposal

1. Add settlement-activity identity migration to both account-merge paths:
   - `FirebaseAuthRepository.mergeGroupMemberships(...)`
   - `FirebaseGroupRepository.mergeClaimedMemberIntoUser(...)`

2. Rewrite every matching settlement activity field when `fromUserId -> toUserId`:
   - `payerUserId`
   - `receiverUserId`
   - optionally `payerName` / `receiverName` refresh from the merged profile

3. After merge, recompute balances from canonical expenses plus migrated settlement activities.

4. Add a repair invariant:
   - if `groups.ownerUserId` is not present in active `groupMembers`, repair the owner member row
   - if balances contain unknown user IDs, flag and repair instead of silently surfacing `Member`

5. Add a regression test:
   - settle using old account ID
   - merge or restore to new account ID
   - confirm balances, insights, and settlement preview use only the merged user ID

## Recommendation

Do not patch UI fallbacks first. The data-repair path is the real fix.

## Implementation Status

Applied on `2026-04-11`.

- Added settlement-activity identity migration in both merge paths.
- Added owner-membership self-repair for groups where `ownerUserId` exists but the owner member row is missing or stale.
- Added alias repair for legacy user IDs preserved in placeholder email metadata.
- Fixed settlement accounting so separate successful payments with the same `transferKey` are both counted.
- Added regression tests for settlement activity ID or transfer-key migration and balance recomputation.

Validation:

- `assembleDebug` passed.
- `:core:data:testDebugUnitTest --tests com.buddingintents.letsgodutch.core.data.repository.GroupJoinMergeTest` passed.
- Debug APK installed on `SM_A546E` (`RZCWA1260BK`).
