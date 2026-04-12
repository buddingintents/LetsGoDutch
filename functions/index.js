const admin = require("firebase-admin");
const functions = require("firebase-functions");

admin.initializeApp();

exports.pushUserNotification = functions.database
  .ref("/notifications/{userId}/{notificationId}")
  .onCreate(async (snapshot, context) => {
    const userId = normalizeString(context.params.userId);
    const notificationId = normalizeString(context.params.notificationId);
    if (!userId || !notificationId) {
      return null;
    }

    const recordRef = admin
      .database()
      .ref(`notifications/${userId}/${notificationId}`);
    const currentSnapshot = await recordRef.get();
    const payload = currentSnapshot.val() || snapshot.val() || {};
    const existingEventId = normalizeString(
      payload.pushDelivery && payload.pushDelivery.lastEventId
    );
    if (existingEventId === context.eventId) {
      return null;
    }

    const tokensSnapshot = await admin.database().ref(`fcmTokens/${userId}`).get();
    const tokenEntries = [];
    tokensSnapshot.forEach((child) => {
      const token = normalizeString(child.child("token").val()).trim();
      if (token) {
        tokenEntries.push({ tokenId: child.key, token });
      }
    });

    const now = Date.now();
    if (!tokenEntries.length) {
      await recordRef.child("pushDelivery").update({
        lastEventId: context.eventId,
        status: "NO_TOKENS",
        updatedAtEpochMs: now,
      });
      return null;
    }

    const title = normalizeString(payload.title) || "Let's Go Dutch";
    const body = normalizeString(payload.body) || "You have a new group update.";
    const message = {
      tokens: tokenEntries.map((entry) => entry.token),
      data: buildDataPayload(payload, notificationId, title, body),
      android: {
        priority: "high",
      },
    };

    const response = await admin.messaging().sendEachForMulticast(message);
    const updates = {
      [`notifications/${userId}/${notificationId}/pushDelivery`]: {
        lastEventId: context.eventId,
        status: deriveStatus(response),
        attemptedAtEpochMs: now,
        successCount: response.successCount,
        failureCount: response.failureCount,
      },
    };

    const invalidTokenIds = [];
    response.responses.forEach((result, index) => {
      if (!result.success && isInvalidTokenError(result.error && result.error.code)) {
        const tokenId = tokenEntries[index] && tokenEntries[index].tokenId;
        if (tokenId) {
          invalidTokenIds.push(tokenId);
          updates[`fcmTokens/${userId}/${tokenId}`] = null;
        }
      }
    });
    if (invalidTokenIds.length) {
      updates[`notifications/${userId}/${notificationId}/pushDelivery/removedTokenIds`] =
        invalidTokenIds;
    }

    await admin.database().ref().update(updates);
    return null;
  });

function buildDataPayload(payload, notificationId, title, body) {
  const values = {
    notificationId,
    title,
    body,
    type: normalizeString(payload.type),
    groupId: normalizeString(payload.groupId),
    settlementId: normalizeString(payload.settlementId),
    targetUserId: normalizeString(payload.targetUserId),
    byUserId: normalizeString(payload.byUserId),
  };

  return Object.entries(values).reduce((result, [key, value]) => {
    if (value) {
      result[key] = value;
    }
    return result;
  }, {});
}

function deriveStatus(response) {
  if (response.successCount && !response.failureCount) {
    return "SENT";
  }
  if (response.successCount) {
    return "PARTIAL";
  }
  return "FAILED";
}

function isInvalidTokenError(code) {
  return (
    code === "messaging/registration-token-not-registered" ||
    code === "messaging/invalid-registration-token"
  );
}

function normalizeString(value) {
  if (value === null || value === undefined) {
    return "";
  }
  return String(value);
}
