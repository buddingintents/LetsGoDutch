# Play Store Release - 2026-04-09

## Release Type

Play Store release notes and verification record for the current `Fresh Mint` UI revamp increment.

## Release Scope

This release focuses on the post-login groups experience, login polish, notification behavior, and release-readiness cleanup.

## User-Facing Changes

### Groups Surface Revamp

- Added a `Group Summary` hero on the groups landing screen
- Added signed-in-user settlement visibility across groups
- Added per-group settlement status with amount:
  - `They owe you`
  - `You owe them`
  - `All settled`
- Reduced invite code pill width so settlement information shares the same row
- Reduced hero card height to fit more useful information above the fold
- Reduced individual group card height to show more groups on screen
- Added a dim, blurred ambient background treatment behind the groups surface without reducing text legibility
- Updated the launcher and Play Store icon to the new gold crest brand mark

### Login Screen Polish

- Removed the filled background behind the `Your name` input field
- Updated the anonymous-entry helper copy to:
  - `No email required. If you prioritize anonymity.`

### Daily Reminder

- Added a local self-notification scheduled for `10:00 AM` in the user's local timezone
- Notification is generated only when unsettled groups exist
- No notification is generated when all groups are already settled
- Reminder copy pattern:
  - `<group_count> groups need settlement. <You owe/They owe you> ₹<amount> to your friends`

## Technical Changes

- Added shared settlement summary logic for cross-group and per-group net balance presentation
- Wired groups landing UI to use per-group signed-in-user net balances
- Added local reminder scheduler and broadcast receiver
- Added reminder rescheduling for:
  - app start
  - device reboot
  - app update / package replace
  - timezone change
  - manual device time change
- Replaced Android launcher mipmaps and in-app brand image references with the new icon asset
- Updated repo hygiene to ignore local release artifacts and machine-local Android/Gradle state
- Removed the previously discussed hardcoded testing account ID approach so no test public account ID is baked into app code

## Verification Status

The changes for this release passed verification.

### Functional Verification

- Groups hero displays `Group Summary`
- Groups cards show per-group settlement state and amount
- Invite code and settlement row fit within the revised layout
- Denser hero and group cards improve information density
- Background treatment remains blurred and dim enough for readability
- Login field no longer shows the previous filled background
- Anonymous-entry copy updated correctly
- Daily reminder logic is suppressed when all groups are settled
- Daily reminder logic is enabled when unsettled groups exist

### Device Verification

- Installed and verified on Samsung Galaxy A54
- Device ID used during verification: `RZCWA1260BK`

## Play Store "What's New"

### Recommended Version

- Fresh Mint UI revamp continues with a refreshed Groups home
- New `Group Summary` hero with signed-in user settlement totals
- Per-group `You owe` / `They owe you` amounts on the Groups screen
- More compact group cards to fit more on screen
- New app icon and updated store branding
- Polished login experience and cleaner field styling
- Daily reminders for unsettled groups
- Stability, readability, and UX improvements

### Shorter Version

- New `Group Summary` hero on the Groups screen
- Per-group settlement amounts at a glance
- More compact cards and cleaner visuals
- New app icon and updated store branding
- Polished login experience
- Daily reminders for unsettled groups

## Release Readiness Notes

- `README.md` has been expanded with setup, architecture, release, and operational details
- Revamp milestone documentation has been updated to reflect `R-02` completion
- `.gitignore` has been updated to keep local release bundles and machine-local config out of Git
- Release signing is expected from local `keystore.properties` or environment variables

## Known Follow-Up

- Release-signed Android App Links verification for `buddingintents.com` still needs final confirmation outside this document
- This document does not generate the actual `.aab`; it records release scope and notes only
