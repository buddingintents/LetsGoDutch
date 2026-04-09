# Deep Link Validation - 2026-04-08

## Scope

- Device: Samsung Galaxy A54 (`SM-A546E`)
- Package: `com.buddingintents.letsgodutch`
- Entry activity: `com.buddingintents.letsgodutch/.MainActivity`
- Related artifacts:
  - `qa_artifacts/deeplink_view.xml`
  - `qa_artifacts/deeplink_view.png`

## Verified

- `letsgodutch://join/<INVITE_CODE>` launches `MainActivity` on-device.
- Delivering `https://buddingintents.com/join/<INVITE_CODE>` directly to `MainActivity` also routes through the app's deep-link parser.
- Using the live invite code `HH6MSUYA`, both deep-link forms navigated into the `test` group on the connected device.

## Not Yet Signed Off

- A normal Android `VIEW` intent for `https://buddingintents.com/join/<INVITE_CODE>` did not auto-open the app from the system resolver path during this session.
- `adb shell pm get-app-links com.buddingintents.letsgodutch` returned:
  - `buddingintents.com: 1024`
  - `www.buddingintents.com: 1024`
- Android documents any domain verification state other than `verified` as a failed or incomplete verification result. States of `1024` or greater are device-specific custom verifier error codes.
- Live release verification on 2026-04-09 still reproduced the resolver path after the site JSON update:
  - release APK installed successfully on Samsung Galaxy A54
  - `adb shell pm verify-app-links --re-verify com.buddingintents.letsgodutch` completed without promoting either host to `verified`
  - `adb shell am start -W -a android.intent.action.VIEW -d "https://buddingintents.com/join/57B5H7JN"` launched `android/com.android.internal.app.ResolverActivity`

## Important Interpretation Note

- The build currently installed on the device is `DEBUGGABLE`.
- Android App Links verification is certificate-sensitive. A debug or sideloaded build can fail App Links verification even if the production website setup is correct, unless that exact signing certificate is also declared in `assetlinks.json`.
- Because of that, the latest browser-to-app HTTPS test is useful for diagnosis, but it is not a final production verdict.

## Signing Notes

- Installed device build certificate SHA-256:
  - `6A:5D:38:97:02:AE:90:44:CB:DD:91:F8:92:76:46:FD:44:51:23:52:E1:E3:9E:C2:5C:65:7F:73:73:D1:9E:29`
- Local release APK:
  - `app/build/outputs/apk/release/app-release.apk`
- Local release APK metadata:
  - version name `1.2.17`
  - version code `17`
  - file timestamp `2026-04-08 23:05:18`
- Local release APK certificate SHA-256:
  - `2994F798C718DDCA5EC3BEDF474D5B66BB83D0D5BBE54A480CD3E6C7F96D31AD`

## Recommended Next Step

1. Use the fresh release-signed artifact at `app/build/outputs/apk/release/app-release.apk`.
2. Confirm `https://buddingintents.com/.well-known/assetlinks.json` is served over HTTPS without redirects and includes the signing fingerprint for the exact production-distributed app variant.
   - Live site fingerprint observed on `2026-04-09`:
     - `77:03:09:05:15:13:65:CF:79:0F:7F:4E:A9:76:29:9F:84:90:E6:A2:BD:79:54:54:C2:D6:61:9D:28:5C:BA:66`
   - Local release keystore fingerprint:
     - `29:94:F7:98:C7:18:DD:CA:5E:C3:BE:DF:47:4D:5B:66:BB:83:D0:D5:BB:E5:4A:48:0C:D3:E6:C7:F9:6D:31:AD`
   - If production installs come from Google Play, `assetlinks.json` must use the Play App Signing certificate, not just the local upload keystore.
   - As of `2026-04-09`, the site JSON now includes both fingerprints, but normal HTTPS validation from system tooling still fails with an untrusted certificate chain.
3. Install the release-signed build on a clean test path and rerun:
   - `adb shell pm get-app-links com.buddingintents.letsgodutch`
   - open `https://buddingintents.com/join/<valid invite>` from Chrome or a messaging app
4. Capture the final verified run in `qa_artifacts`.

## References

- Android Developers: Verify App Links
  - https://developer.android.com/training/app-links/verify-applinks
- Android Developers: Troubleshoot App Links
  - https://developer.android.com/training/app-links/troubleshoot
