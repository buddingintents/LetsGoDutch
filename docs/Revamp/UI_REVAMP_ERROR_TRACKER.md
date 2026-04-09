# Let's Go Dutch - UI Revamp Error Tracker

Cross-milestone summary tracker for UI revamp defects.
Detailed error analysis must still live in the active milestone file first.

| ERR_ID | Milestone | Reported Date | Component | Error Description | Root Cause Analysis | Proposed Fix | Fix Date | Status |
|---|---|---|---|---|---|---|---|---|
| ERR-REV-001 | R-05 | 2026-04-09 | Production app links | Release-installed `https://buddingintents.com/join/<INVITE_CODE>` links still open the Android resolver/browser path instead of defaulting into the app | Live `assetlinks.json` fingerprints were corrected, but on-device domain verification remains in custom error state `1024`; normal TLS validation against `buddingintents.com` still fails with an untrusted certificate chain | Fix the production TLS certificate chain for `buddingintents.com` and `www.buddingintents.com`, then rerun Android App Links verification on a release-installed build | - | `OPEN` |
| ERR-REV-002 | R-05 | 2026-04-09 | Brand icon rollout | Launcher icon and in-app brand icon usage are inconsistent; the old installed release build still showed the previous launcher asset, and round-placeholder contexts were reusing rounded-rectangle artwork | Current brand rollout mixed the correct rounded-rectangle mark with round launcher contexts and did not yet ship a dedicated circular brand asset for round placeholders | Ship a dedicated circular icon variant for round launcher contexts, keep the rounded-rectangle asset for hero and card badges, and reinstall a freshly built APK so the device launcher cache updates from the new resource set | - | `IN_PROGRESS` |
