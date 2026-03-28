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
Use Cloud Functions (or your own server) to send messages to tokens stored under:

`fcmTokens/{userId}/{tokenId}`

### Suggested Cloud Function flow

Trigger: Realtime Database write at `notifications/{userId}/{notificationId}`

Steps:
1. Read all tokens from `fcmTokens/{userId}`.
2. Send multicast message via Firebase Admin SDK.
3. Remove invalid tokens (`registration-token-not-registered`, etc.).

### Example function (Node.js, pseudo-template)

```js
exports.pushUserNotification = onValueCreated(
  "/notifications/{userId}/{notificationId}",
  async (event) => {
    const { userId } = event.params;
    const payload = event.data.val() || {};
    const snap = await admin.database().ref(`fcmTokens/${userId}`).get();
    const tokens = [];
    snap.forEach((child) => {
      const token = child.child("token").val();
      if (token) tokens.push(token);
    });
    if (!tokens.length) return;

    const response = await admin.messaging().sendEachForMulticast({
      tokens,
      notification: {
        title: payload.title || "Let's Go Dutch",
        body: payload.body || "You have a new update.",
      },
      data: {
        type: String(payload.type || "GENERIC"),
        groupId: String(payload.groupId || ""),
      },
      android: { priority: "high" },
    });

    // Remove invalid tokens
    const updates = {};
    response.responses.forEach((r, i) => {
      if (!r.success && r.error?.code === "messaging/registration-token-not-registered") {
        // map token back to tokenId if stored
      }
    });
    if (Object.keys(updates).length) {
      await admin.database().ref(`fcmTokens/${userId}`).update(updates);
    }
  }
);
```

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

