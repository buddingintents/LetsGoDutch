# Firebase Analytics, Crashlytics, and FCM Setup

This project is now wired in-app for:
- Firebase Analytics event logging
- Firebase Crashlytics non-fatal + fatal capture
- FCM token registration and notification display handling

You still need the Firebase Console / backend setup below.

## 1) Enable Analytics

1. Open Firebase Console -> Project Settings -> Integrations.
2. Ensure Google Analytics is linked to the project.
3. In Project Settings -> General -> Your apps -> Android app, confirm your stream exists.

To verify events quickly on device:

```powershell
adb shell setprop debug.firebase.analytics.app com.buddingintents.letsgodutch
```

Then use the app and check Firebase Console -> Analytics -> DebugView.

To disable debug mode later:

```powershell
adb shell setprop debug.firebase.analytics.app .none.
```

## 2) Enable Crashlytics

1. Firebase Console -> Crashlytics -> Enable for Android app.
2. Build and run app once.
3. Trigger a crash in debug or release, relaunch app, wait 1-5 minutes.
4. Check Crashlytics dashboard for the new issue.

Notes:
- Crashlytics Gradle plugin is now enabled in app build.
- Mapping file ID injection task runs during debug/release builds.

## 3) Enable Cloud Messaging

1. Firebase Console -> Cloud Messaging:
   - Ensure Firebase Cloud Messaging API is enabled.
2. App requests notification permission on Android 13+ and creates channel:
   - Channel ID: `letsgodutch_updates`

## 4) Required Backend for FCM Delivery

FCM push cannot be sent securely from Android client directly.
This repo now includes a deployable Cloud Functions package under `functions/` that sends push messages from notification records written by the Android client.

Use Cloud Functions (or your own server) to send messages to tokens stored under:

`fcmTokens/{userId}/{tokenId}`

### Included Cloud Function flow

Trigger: Realtime Database write at `notifications/{userId}/{notificationId}`

Steps:
1. Read all tokens from `fcmTokens/{userId}`.
2. Send multicast message via Firebase Admin SDK.
3. Remove invalid tokens (`registration-token-not-registered`, etc.).

### Deploy commands

```powershell
cd functions
npm install
firebase deploy --only functions
```

The included function:

1. Watches `notifications/{userId}/{notificationId}` on create.
2. Reads `fcmTokens/{userId}`.
3. Sends a high-priority data-only FCM payload with `notificationId`, `title`, `body`, `type`, and `groupId`.
4. Removes invalid tokens.
5. Writes push delivery status back under `notifications/{userId}/{notificationId}/pushDelivery`.

Android now deduplicates Realtime Database-driven local notifications against FCM by `notificationId`, so backend delivery does not create duplicate alerts when the app is already active.

## 5) Realtime Database Rules (minimum idea)

Allow users to write their own token nodes:

```json
{
  "rules": {
    "fcmTokens": {
      "$uid": {
        ".read": "auth != null && auth.uid === $uid",
        ".write": "auth != null && auth.uid === $uid"
      }
    }
  }
}
```

Merge this with your full ruleset.
