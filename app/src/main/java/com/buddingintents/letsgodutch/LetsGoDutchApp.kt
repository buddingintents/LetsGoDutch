package com.buddingintents.letsgodutch

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.ManageAccounts
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PersonAddAlt1
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.StarRate
import androidx.compose.material.icons.filled.TaskAlt
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color as ComposeColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.CustomCredential
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.NoCredentialException
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.buddingintents.letsgodutch.core.data.repository.AuthRepository
import com.buddingintents.letsgodutch.core.data.repository.ExpenseRepository
import com.buddingintents.letsgodutch.core.data.repository.GroupRepository
import com.buddingintents.letsgodutch.core.data.repository.PersonalExpenseRepository
import com.buddingintents.letsgodutch.core.data.repository.SettlementRepository
import com.buddingintents.letsgodutch.core.data.repository.TodoRepository
import com.buddingintents.letsgodutch.core.data.split.SplitCalculator
import com.buddingintents.letsgodutch.core.data.repository.firebase.FirebaseAuthRepository
import com.buddingintents.letsgodutch.core.data.repository.firebase.FirebasePersonalExpenseRepository
import com.buddingintents.letsgodutch.core.data.repository.firebase.FirebaseExpenseRepository
import com.buddingintents.letsgodutch.core.data.repository.firebase.FirebaseGroupRepository
import com.buddingintents.letsgodutch.core.data.repository.firebase.FirebaseSettlementRepository
import com.buddingintents.letsgodutch.core.data.repository.firebase.FirebaseTodoRepository
import com.buddingintents.letsgodutch.core.data.repository.inmemory.InMemoryAuthRepository
import com.buddingintents.letsgodutch.core.data.repository.inmemory.InMemoryPersonalExpenseRepository
import com.buddingintents.letsgodutch.core.data.repository.inmemory.InMemoryExpenseRepository
import com.buddingintents.letsgodutch.core.data.repository.inmemory.InMemoryGroupRepository
import com.buddingintents.letsgodutch.core.data.repository.inmemory.InMemorySettlementRepository
import com.buddingintents.letsgodutch.core.data.repository.inmemory.InMemoryTodoRepository
import com.buddingintents.letsgodutch.core.model.ExitLiabilityChoice
import com.buddingintents.letsgodutch.core.model.Expense
import com.buddingintents.letsgodutch.core.model.Group
import com.buddingintents.letsgodutch.core.model.JoinGroupPreview
import com.buddingintents.letsgodutch.core.model.Member
import com.buddingintents.letsgodutch.core.model.PersonalExpenseEntry
import com.buddingintents.letsgodutch.core.model.Role
import com.buddingintents.letsgodutch.core.model.SettlementUpiStatus
import com.buddingintents.letsgodutch.core.model.SettlementUpiTransaction
import com.buddingintents.letsgodutch.core.model.SplitShare
import com.buddingintents.letsgodutch.core.model.SplitType
import com.buddingintents.letsgodutch.core.model.TodoTask
import com.buddingintents.letsgodutch.core.model.TodoTaskStatus
import com.buddingintents.letsgodutch.core.model.UserProfile
import com.buddingintents.letsgodutch.core.model.buildSettlementTransfers
import com.buddingintents.letsgodutch.core.model.formatIndianCurrency
import com.buddingintents.letsgodutch.core.model.normalizeUpiId
import com.buddingintents.letsgodutch.core.model.successfulSettlementTransferKeys
import com.buddingintents.letsgodutch.core.designsystem.component.GradientButton
import com.buddingintents.letsgodutch.core.designsystem.component.PillTabSelector
import com.buddingintents.letsgodutch.core.designsystem.component.SectionLabel
import com.buddingintents.letsgodutch.core.designsystem.theme.Charcoal
import com.buddingintents.letsgodutch.core.designsystem.theme.CoralSoft
import com.buddingintents.letsgodutch.core.designsystem.theme.MintGlow
import com.buddingintents.letsgodutch.core.designsystem.theme.MintGreen
import com.buddingintents.letsgodutch.core.designsystem.theme.MintTeal
import com.buddingintents.letsgodutch.core.designsystem.theme.Night
import com.buddingintents.letsgodutch.core.designsystem.theme.NightSoft
import com.buddingintents.letsgodutch.core.designsystem.theme.TextOnDark
import com.buddingintents.letsgodutch.core.designsystem.theme.ThemeMode
import com.buddingintents.letsgodutch.feature.auth.AuthScreen
import com.buddingintents.letsgodutch.feature.expenses.AddExpenseDialog
import com.buddingintents.letsgodutch.feature.expenses.ExpenseMemberOption
import com.buddingintents.letsgodutch.feature.groups.GroupsListScreen
import com.buddingintents.letsgodutch.feature.insights.InsightSettlementTransferUi
import com.buddingintents.letsgodutch.feature.insights.InsightTrackedSettlementActivityUi
import com.buddingintents.letsgodutch.feature.insights.InsightsScreen
import com.buddingintents.letsgodutch.feature.ledger.LedgerScreen
import com.buddingintents.letsgodutch.feature.settlement.SettlementConfirmationCard
import com.buddingintents.letsgodutch.feature.settlement.SettlementPreviewSummaryCard
import com.buddingintents.letsgodutch.feature.settlement.SettlementPreviewSummaryUi
import com.buddingintents.letsgodutch.feature.settlement.SettlementPreviewScreen
import com.buddingintents.letsgodutch.feature.settlement.SettlementSuccessUi
import com.buddingintents.letsgodutch.feature.settlement.SettlementTrackedUpiResponseUi
import com.buddingintents.letsgodutch.feature.settlement.SettlementTransferUi
import com.buddingintents.letsgodutch.feature.settlement.SettlementTransfersCard
import com.buddingintents.letsgodutch.notifications.FcmTokenSyncManager
import com.buddingintents.letsgodutch.notifications.NotificationDisplayDeduper
import com.buddingintents.letsgodutch.notifications.showRealtimeDbNotification
import com.buddingintents.letsgodutch.telemetry.AppTelemetry
import com.buddingintents.letsgodutch.tour.isAppTourCompleted
import com.buddingintents.letsgodutch.tour.setAppTourCompleted
import com.buddingintents.letsgodutch.ui.AppTourOverlay
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import androidx.core.content.FileProvider
import androidx.compose.foundation.shape.RoundedCornerShape
import java.io.File
import java.io.FileOutputStream
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.abs
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import androidx.compose.material3.ExperimentalMaterial3Api


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LetsGoDutchApp(
    incomingInviteCode: StateFlow<String?>? = null,
    onInviteCodeConsumed: (() -> Unit)? = null,
    currentThemeMode: ThemeMode = ThemeMode.SYSTEM,
    onThemeModeChange: ((ThemeMode) -> Unit)? = null,
    appUpdateSummary: String = "The app checks for Google Play updates automatically when it starts.",
    isCheckingForAppUpdate: Boolean = false,
    isDownloadedUpdateReady: Boolean = false,
    onCheckForAppUpdateClick: () -> Unit = {},
    onInstallDownloadedUpdateClick: () -> Unit = {},
    onOpenPlayStoreUpdateClick: () -> Unit = {},
) {
    val context = LocalContext.current
    val repositories = remember(context) { createRepositoryBundle(context.applicationContext) }
    val reviewPromptStore = remember(context) { AppReviewPromptStore(context.applicationContext) }
    val settlementHistoryStore = remember(context) { LocalSettlementHistoryStore(context.applicationContext) }
    val realtimeDbConfigIssue = remember(context) { context.firebaseRealtimeDbConfigIssueOrNull() }
    val authRepository = repositories.authRepository
    val groupRepository = repositories.groupRepository
    val expenseRepository = repositories.expenseRepository
    val settlementRepository = repositories.settlementRepository
    val todoRepository = repositories.todoRepository
    val personalExpenseRepository = repositories.personalExpenseRepository

    val currentUser by authRepository.currentUser.collectAsState(initial = null)
    val anonymousNameHints by authRepository.observeRecentAnonymousDisplayNames()
        .collectAsState(initial = emptyList())
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val scope = rememberCoroutineScope()

    var authMessage by rememberSaveable {
        mutableStateOf(
            if (repositories.usingFirebase) "" else "Firebase is unavailable. Running in demo mode.",
        )
    }
    var isGoogleSignInInProgress by rememberSaveable { mutableStateOf(false) }
    var isAnonymousSignInInProgress by rememberSaveable { mutableStateOf(false) }
    var isSavingProfile by rememberSaveable { mutableStateOf(false) }
    var pendingInviteCode by rememberSaveable { mutableStateOf<String?>(null) }
    var pendingJoinClaimRequest by remember { mutableStateOf<PendingInviteJoinRequest?>(null) }
    var groupsMessage by rememberSaveable { mutableStateOf("") }
    var showCreateGroupDialog by rememberSaveable { mutableStateOf(false) }
    var showJoinGroupDialog by rememberSaveable { mutableStateOf(false) }
    var createGroupDialogOrigin by rememberSaveable { mutableStateOf("unknown") }
    var joinGroupDialogOrigin by rememberSaveable { mutableStateOf("unknown") }
    var isTourCompleted by remember(context) { mutableStateOf(context.isAppTourCompleted()) }
    var showAppTour by rememberSaveable { mutableStateOf(false) }
    var showAppReviewPrompt by rememberSaveable { mutableStateOf(false) }
    var isLaunchingReviewPrompt by remember { mutableStateOf(false) }

    val incomingInviteCodeValue by (incomingInviteCode?.collectAsState(initial = null)
        ?: remember { mutableStateOf<String?>(null) })

    fun registerHelpfulInteraction(interactionType: String) {
        AppTelemetry.logEvent("app_interaction_success", mapOf("type" to interactionType))
        if (showAppReviewPrompt || isLaunchingReviewPrompt) return
        if (reviewPromptStore.recordInteractionAndShouldPrompt()) {
            reviewPromptStore.markPromptShown()
            showAppReviewPrompt = true
        }
    }

    fun logFailureEvent(
        name: String,
        throwable: Throwable?,
        params: Map<String, Any?> = emptyMap(),
    ) {
        AppTelemetry.logEvent(
            name,
            params + mapOf(
                "reason" to throwable?.message.orEmpty().ifBlank {
                    throwable?.javaClass?.simpleName ?: "unknown"
                },
            ),
        )
    }

    fun logDialogOpen(
        name: String,
        origin: String,
        params: Map<String, Any?> = emptyMap(),
    ) {
        AppTelemetry.logEvent(
            "dialog_open",
            mapOf("name" to name, "origin" to origin) + params,
        )
    }

    fun openCreateGroupDialog(origin: String) {
        createGroupDialogOrigin = origin
        showCreateGroupDialog = true
        logDialogOpen(name = "create_group", origin = origin)
    }

    fun openJoinGroupDialog(origin: String) {
        joinGroupDialogOrigin = origin
        showJoinGroupDialog = true
        logDialogOpen(name = "join_group", origin = origin)
    }

    fun navigateToTodo(origin: String) {
        AppTelemetry.logEvent(
            "navigation_select",
            mapOf("origin" to origin, "destination" to "todo"),
        )
        navController.navigate(Destination.Todo.route) {
            launchSingleTop = true
        }
    }

    fun navigateToSelfExpenses(origin: String) {
        AppTelemetry.logEvent(
            "navigation_select",
            mapOf("origin" to origin, "destination" to "self_expenses"),
        )
        navController.navigate(Destination.SelfExpenses.route) {
            launchSingleTop = true
        }
    }

    fun navigateToSettings(origin: String) {
        AppTelemetry.logEvent(
            "navigation_select",
            mapOf("origin" to origin, "destination" to "settings"),
        )
        navController.navigate(Destination.Settings.route) {
            launchSingleTop = true
        }
    }

    fun navigateToGroup(groupId: String, origin: String) {
        AppTelemetry.logEvent(
            "group_open",
            mapOf("group_id" to groupId, "origin" to origin),
        )
        navController.navigate(Destination.Group.buildRoute(groupId)) {
            popUpTo(Destination.Groups.route) { inclusive = false }
            launchSingleTop = true
        }
    }

    fun signOut(origin: String) {
        AppTelemetry.logEvent("sign_out", mapOf("origin" to origin))
        scope.launch {
            authRepository.signOut()
            navController.navigate(Destination.Auth.route) {
                popUpTo(0) { inclusive = true }
            }
        }
    }

    fun launchReviewFlow() {
        if (isLaunchingReviewPrompt) return
        AppTelemetry.logEvent("app_review_click")
        isLaunchingReviewPrompt = true
        reviewPromptStore.markReviewRequested()
        showAppReviewPrompt = false
        AppTelemetry.logEvent("app_review_play_store_open")
        context.openPlayStoreReviewPage()
        isLaunchingReviewPrompt = false
    }

    LaunchedEffect(groupsMessage) {
        if (groupsMessage.isNotBlank()) {
            context.showShortToast(groupsMessage)
            groupsMessage = ""
        }
    }

    LaunchedEffect(currentUser?.userId, isTourCompleted) {
        val isSignedIn = !currentUser?.userId.isNullOrBlank()
        if (isSignedIn && !isTourCompleted) {
            showAppTour = true
        }
    }

    LaunchedEffect(currentUser?.userId) {
        if (currentUser?.userId.isNullOrBlank()) {
            showAppTour = false
            pendingJoinClaimRequest = null
        }
    }

    LaunchedEffect(
        navBackStackEntry?.destination?.route,
        navBackStackEntry?.arguments?.getString(Destination.Group.groupIdArg),
    ) {
        val entry = navBackStackEntry ?: return@LaunchedEffect
        val route = entry.destination.route ?: return@LaunchedEffect
        val params = mutableMapOf<String, Any?>("route_name" to route)
        val screenName = when (route) {
            Destination.Auth.route -> "auth"
            Destination.Groups.route -> "groups"
            Destination.Todo.route -> "todo"
            Destination.SelfExpenses.route -> "self_expenses"
            Destination.Settings.route -> "settings"
            Destination.Group.route -> {
                params["group_id"] = entry.arguments?.getString(Destination.Group.groupIdArg)
                    .orEmpty()
                    .ifBlank { "unknown" }
                "group_detail"
            }
            Destination.SettlementPreview.route -> {
                params["group_id"] = entry.arguments?.getString(Destination.SettlementPreview.groupIdArg)
                    .orEmpty()
                    .ifBlank { "unknown" }
                "settlement_preview"
            }
            else -> return@LaunchedEffect
        }
        AppTelemetry.logScreenView(
            screenName = screenName,
            params = params,
        )
    }

    suspend fun completeInviteJoin(
        inviteCode: String,
        source: String,
        claimMemberUserId: String?,
    ) {
        val resultJoin = groupRepository.joinGroupWithInvite(
            inviteCode = inviteCode,
            userId = currentUser?.userId.orEmpty(),
            claimMemberUserId = claimMemberUserId,
        )
        if (resultJoin.isSuccess) {
            val joinedGroup = resultJoin.getOrNull()
            groupsMessage = if (source == JOIN_SOURCE_DEEP_LINK) {
                "Joined group from invite link."
            } else {
                "Joined group."
            }
            AppTelemetry.logEvent(
                "group_join_success",
                mapOf(
                    "via" to source,
                    "group_id" to (joinedGroup?.groupId ?: "unknown"),
                    "claim_mode" to if (claimMemberUserId.isNullOrBlank()) "new_member" else "claimed_placeholder",
                ),
            )
            registerHelpfulInteraction("group_join")
            joinedGroup?.groupId?.let { groupId ->
                navigateToGroup(groupId = groupId, origin = "group_join_success")
            }
        } else {
            groupsMessage = resultJoin.exceptionOrNull()
                ?.toActionableMessage("Unable to join group.")
                .orEmpty()
            logFailureEvent(
                name = "group_join_failure",
                throwable = resultJoin.exceptionOrNull(),
                params = mapOf(
                    "via" to source,
                    "stage" to "complete",
                    "claim_mode" to if (claimMemberUserId.isNullOrBlank()) "new_member" else "claimed_placeholder",
                ),
            )
            resultJoin.exceptionOrNull()?.let { error ->
                AppTelemetry.recordNonFatal(
                    error,
                    tags = mapOf("op" to if (source == JOIN_SOURCE_DEEP_LINK) "join_group_deeplink" else "group_join"),
                )
            }
        }
    }

    suspend fun previewInviteJoin(
        inviteCode: String,
        source: String,
    ) {
        AppTelemetry.logEvent(
            "group_join_attempt",
            mapOf("via" to source),
        )
        val userId = currentUser?.userId.orEmpty()
        if (userId.isBlank()) {
            groupsMessage = "Please sign in again."
            return
        }
        if (!realtimeDbConfigIssue.isNullOrBlank()) {
            groupsMessage = realtimeDbConfigIssue
            return
        }

        val previewResult = groupRepository.previewJoinWithInvite(
            inviteCode = inviteCode,
            userId = userId,
        )
        if (previewResult.isFailure) {
            groupsMessage = previewResult.exceptionOrNull()
                ?.toActionableMessage("Unable to join group.")
                .orEmpty()
            logFailureEvent(
                name = "group_join_failure",
                throwable = previewResult.exceptionOrNull(),
                params = mapOf("via" to source, "stage" to "preview"),
            )
            previewResult.exceptionOrNull()?.let { error ->
                AppTelemetry.recordNonFatal(
                    error,
                    tags = mapOf("op" to if (source == JOIN_SOURCE_DEEP_LINK) "join_group_preview_deeplink" else "group_join_preview"),
                )
            }
            return
        }

        val preview = previewResult.getOrNull() ?: return
        if (preview.alreadyJoined) {
            groupsMessage = "You're already in this group."
            AppTelemetry.logEvent(
                "group_join_already_member",
                mapOf("via" to source, "group_id" to preview.group.groupId),
            )
            navigateToGroup(groupId = preview.group.groupId, origin = "group_join_already_member")
            return
        }

        if (preview.claimableMembers.isNotEmpty()) {
            AppTelemetry.logEvent(
                "group_join_claim_required",
                mapOf(
                    "via" to source,
                    "group_id" to preview.group.groupId,
                    "claimable_count" to preview.claimableMembers.size,
                ),
            )
            pendingJoinClaimRequest = PendingInviteJoinRequest(
                inviteCode = inviteCode,
                source = source,
                preview = preview,
            )
            return
        }

        completeInviteJoin(
            inviteCode = inviteCode,
            source = source,
            claimMemberUserId = null,
        )
    }

    LaunchedEffect(currentUser?.userId, currentUser?.displayName, currentUser?.email) {
        AppTelemetry.setUser(currentUser)
        val userId = currentUser?.userId.orEmpty()
        if (userId.isNotBlank()) {
            FcmTokenSyncManager.syncCurrentTokenForUser(
                userId = userId,
                source = "auth_state",
            )
        }
    }

    DisposableEffect(currentUser?.userId) {
        val userId = currentUser?.userId.orEmpty()
        if (userId.isBlank()) {
            return@DisposableEffect onDispose {}
        }

        val minCreatedAt = System.currentTimeMillis().toDouble()
        val query = FirebaseDatabase.getInstance()
            .reference
            .child("notifications")
            .child(userId)
            .orderByChild("createdAtEpochMs")
            .startAt(minCreatedAt)

        val listener = object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val notificationId = snapshot.key.orEmpty()
                if (
                    notificationId.isNotBlank() &&
                    !NotificationDisplayDeduper.tryAcquire(context, notificationId)
                ) {
                    return
                }
                val title = snapshot.child("title").getValue(String::class.java)
                    .orEmpty()
                    .ifBlank { context.getString(R.string.app_name) }
                val body = snapshot.child("body").getValue(String::class.java)
                    .orEmpty()
                    .ifBlank { "You have a new group update." }
                context.showRealtimeDbNotification(
                    title = title,
                    body = body,
                    notificationIdHint = notificationId,
                )
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) = Unit

            override fun onChildRemoved(snapshot: DataSnapshot) = Unit

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) = Unit

            override fun onCancelled(error: DatabaseError) = Unit
        }
        query.addChildEventListener(listener)

        onDispose {
            query.removeEventListener(listener)
        }
    }

    LaunchedEffect(incomingInviteCodeValue) {
        val code = incomingInviteCodeValue.toNormalizedInviteCode()
        if (code.isNotBlank()) {
            pendingInviteCode = code
            onInviteCodeConsumed?.invoke()
            AppTelemetry.logEvent(
                "invite_link_received",
                mapOf("source" to JOIN_SOURCE_DEEP_LINK),
            )
            if (currentUser?.userId.isNullOrBlank()) {
                authMessage = "Continue with Google or name to join this invite."
            }
        }
    }

    LaunchedEffect(showAppTour) {
        if (showAppTour) {
            logDialogOpen(name = "app_tour", origin = "system")
        }
    }

    LaunchedEffect(showAppReviewPrompt) {
        if (showAppReviewPrompt) {
            logDialogOpen(name = "app_review_prompt", origin = "system")
        }
    }

    LaunchedEffect(pendingJoinClaimRequest?.inviteCode) {
        val request = pendingJoinClaimRequest ?: return@LaunchedEffect
        logDialogOpen(
            name = "claim_existing_member",
            origin = request.source,
            params = mapOf("group_id" to request.preview.group.groupId),
        )
    }

    val activeSyncState = when {
        isSavingProfile -> BackendSyncUiState(
            label = "Sync",
            title = "Syncing your profile",
            supportingText = "Updating your profile and refreshing it across your groups.",
            badgeText = "Working",
        )
        isAnonymousSignInInProgress -> BackendSyncUiState(
            label = "Account",
            title = "Syncing your account",
            supportingText = "Restoring your groups and preparing your profile from the backend.",
            badgeText = "Working",
        )
        isGoogleSignInInProgress -> BackendSyncUiState(
            label = "Account",
            title = "Syncing your account",
            supportingText = "Linking your identity, merging any existing data, and loading your groups.",
            badgeText = "Working",
        )
        else -> null
    }

    LaunchedEffect(currentUser?.userId, pendingInviteCode) {
        val userId = currentUser?.userId.orEmpty()
        val invite = pendingInviteCode.orEmpty()
        if (userId.isBlank() || invite.isBlank()) return@LaunchedEffect
        // Clear after processing; clearing first cancels this effect because pendingInviteCode is a key.
        previewInviteJoin(inviteCode = invite, source = JOIN_SOURCE_DEEP_LINK)
        if (pendingInviteCode == invite) {
            pendingInviteCode = null
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
    NavHost(
        navController = navController,
        startDestination = Destination.Auth.route,
    ) {
        composable(route = Destination.Auth.route) {
            LaunchedEffect(currentUser?.userId) {
                if (!currentUser?.userId.isNullOrBlank()) {
                    navController.navigate(Destination.Groups.route) {
                        popUpTo(Destination.Auth.route) { inclusive = true }
                    }
                }
            }
            AuthScreen(
                modifier = Modifier.fillMaxSize(),
                onGoogleSignInClick = {
                    if (isGoogleSignInInProgress || isAnonymousSignInInProgress) {
                        authMessage = "Account sync already in progress. Please wait."
                        return@AuthScreen
                    }
                    AppTelemetry.logEvent("login_click")
                    if (!repositories.usingFirebase) {
                        scope.launch {
                            val resultSignIn = authRepository.signInWithGoogleIdToken("demo-token")
                            if (resultSignIn.isSuccess) {
                                AppTelemetry.logEvent("login_success", mapOf("path" to "demo_mode"))
                                navController.navigate(Destination.Groups.route) {
                                    popUpTo(Destination.Auth.route) { inclusive = true }
                                }
                            } else {
                                authMessage = resultSignIn.exceptionOrNull()?.message.orEmpty()
                                logFailureEvent(
                                    name = "login_failure",
                                    throwable = resultSignIn.exceptionOrNull(),
                                    params = mapOf("path" to "demo_mode"),
                                )
                                resultSignIn.exceptionOrNull()?.let { error ->
                                    AppTelemetry.recordNonFatal(error, tags = mapOf("op" to "login_demo"))
                                }
                            }
                        }
                    } else {
                        val webClientId = context.defaultWebClientIdOrNull()
                        if (webClientId.isNullOrBlank()) {
                            authMessage = context.googleSignInConfigIssueMessage()
                            AppTelemetry.logEvent("login_failure", mapOf("reason" to "missing_web_client_id"))
                        } else {
                            isGoogleSignInInProgress = true
                            val credentialManager = CredentialManager.create(context)
                            val signInContext = context.findActivity() ?: context
                            val request = buildGoogleSignInRequest(webClientId)

                            scope.launch {
                                try {
                                    val result = credentialManager.getCredential(
                                        context = signInContext,
                                        request = request,
                                    )
                                    val credential = result.credential
                                    if (credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                                        val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                                        val resultSignIn = authRepository.signInWithGoogleIdToken(googleIdTokenCredential.idToken)
                                        if (resultSignIn.isSuccess) {
                                            AppTelemetry.logEvent("login_success", mapOf("path" to "credential_manager"))
                                            navController.navigate(Destination.Groups.route) {
                                                popUpTo(Destination.Auth.route) { inclusive = true }
                                            }
                                        } else {
                                            authMessage = resultSignIn.exceptionOrNull()?.message ?: "Unable to sign in."
                                            logFailureEvent(
                                                name = "login_failure",
                                                throwable = resultSignIn.exceptionOrNull(),
                                                params = mapOf("path" to "credential_manager"),
                                            )
                                            resultSignIn.exceptionOrNull()?.let { error ->
                                                AppTelemetry.recordNonFatal(error, tags = mapOf("op" to "login_with_google"))
                                            }
                                        }
                                    } else {
                                        authMessage = "Unexpected credential type."
                                        AppTelemetry.logEvent("login_failure", mapOf("reason" to "unexpected_credential_type"))
                                    }
                                } catch (e: GetCredentialException) {
                                    authMessage = e.toGoogleSignInMessage()
                                    logFailureEvent(
                                        name = "login_failure",
                                        throwable = e,
                                        params = mapOf("path" to "credential_manager"),
                                    )
                                    AppTelemetry.recordNonFatal(e, tags = mapOf("op" to "google_sign_in_credman"))
                                } finally {
                                    isGoogleSignInInProgress = false
                                }
                            }
                        }
                    }
                },
                onAnonymousSignInClick = { displayName ->
                    if (displayName.isBlank()) {
                        authMessage = "Please enter your name."
                        return@AuthScreen
                    }
                    if (isGoogleSignInInProgress || isAnonymousSignInInProgress) {
                        authMessage = "Account sync already in progress. Please wait."
                        return@AuthScreen
                    }
                    AppTelemetry.logEvent("login_click", mapOf("method" to "anonymous"))
                    scope.launch {
                        isAnonymousSignInInProgress = true
                        try {
                            val resultSignIn = authRepository.signInAnonymously(displayName)
                            if (resultSignIn.isSuccess) {
                                AppTelemetry.logEvent("login_success", mapOf("path" to "anonymous"))
                                navController.navigate(Destination.Groups.route) {
                                    popUpTo(Destination.Auth.route) { inclusive = true }
                                }
                            } else {
                                authMessage = resultSignIn.exceptionOrNull()?.message
                                    ?: "Unable to continue with name."
                                logFailureEvent(
                                    name = "login_failure",
                                    throwable = resultSignIn.exceptionOrNull(),
                                    params = mapOf("path" to "anonymous"),
                                )
                                resultSignIn.exceptionOrNull()?.let { error ->
                                    AppTelemetry.recordNonFatal(error, tags = mapOf("op" to "login_anonymous"))
                                }
                            }
                        } finally {
                            isAnonymousSignInInProgress = false
                        }
                    }
                },
                message = authMessage,
                anonymousNameHints = anonymousNameHints,
                logoResId = R.drawable.ic_app_icon_full,
            )
        }

        composable(route = Destination.Todo.route) {
            val userId = currentUser?.userId.orEmpty()
            val isLandscape = LocalConfiguration.current.screenWidthDp > LocalConfiguration.current.screenHeightDp
            val tasksFlow: Flow<List<TodoTask>> = remember(userId, realtimeDbConfigIssue) {
                if (userId.isBlank() || !realtimeDbConfigIssue.isNullOrBlank()) {
                    flowOf(emptyList())
                } else {
                    todoRepository.observeTasks(userId)
                }
            }
            val tasks by tasksFlow.collectAsState(initial = emptyList())
            var showAddTaskDialog by rememberSaveable { mutableStateOf(false) }

            AppScaffoldWithDrawer(
                currentUser = currentUser,
                navController = navController,
                title = "To-Do Tasks",
                onSignOut = { signOut(origin = "drawer") },
                onNavigateToTodo = { navigateToTodo(origin = "drawer") },
                onNavigateToSelfExpenses = { navigateToSelfExpenses(origin = "drawer") },
                onCreateGroupClick = { openCreateGroupDialog(origin = "drawer") },
                onJoinGroupClick = { openJoinGroupDialog(origin = "drawer") },
                onNavigateToSettings = { navigateToSettings(origin = "drawer") },
                currentThemeMode = currentThemeMode,
                onThemeModeChange = onThemeModeChange,
                floatingActionButton = {
                    if (isLandscape) {
                        FloatingActionButton(
                            onClick = {
                                showAddTaskDialog = true
                                logDialogOpen(name = "add_task", origin = "todo_fab")
                            },
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Add Task",
                            )
                        }
                    } else {
                        ExtendedFloatingActionButton(
                            modifier = Modifier.padding(bottom = 72.dp),
                            onClick = {
                                showAddTaskDialog = true
                                logDialogOpen(name = "add_task", origin = "todo_fab")
                            },
                            icon = {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = null,
                                )
                            },
                            text = { Text("Add Task") },
                        )
                    }
                },
            ) {
                if (showAddTaskDialog) {
                    AddTodoTaskDialog(
                        onDismiss = { showAddTaskDialog = false },
                        onAddTask = { title ->
                            if (userId.isBlank()) {
                                groupsMessage = "Please sign in again."
                                return@AddTodoTaskDialog
                            }
                            if (!realtimeDbConfigIssue.isNullOrBlank()) {
                                groupsMessage = realtimeDbConfigIssue
                                return@AddTodoTaskDialog
                            }
                            scope.launch {
                                val addResult = todoRepository.addTask(
                                    userId = userId,
                                    title = title,
                                )
                                if (addResult.isSuccess) {
                                    groupsMessage = "Task added."
                                    showAddTaskDialog = false
                                    AppTelemetry.logEvent("todo_add_success")
                                } else {
                                    groupsMessage = addResult.exceptionOrNull()
                                        ?.toActionableMessage("Unable to add task.")
                                        .orEmpty()
                                    logFailureEvent(
                                        name = "todo_add_failure",
                                        throwable = addResult.exceptionOrNull(),
                                    )
                                    addResult.exceptionOrNull()?.let { error ->
                                        AppTelemetry.recordNonFatal(error, tags = mapOf("op" to "todo_add"))
                                    }
                                }
                            }
                        },
                    )
                }
                TodoTasksScreen(
                    tasks = tasks,
                    onMarkCompleted = { task ->
                        if (userId.isBlank()) {
                            groupsMessage = "Please sign in again."
                            return@TodoTasksScreen
                        }
                        scope.launch {
                            val updateResult = todoRepository.updateTaskStatus(
                                userId = userId,
                                taskId = task.taskId,
                                status = TodoTaskStatus.COMPLETED,
                            )
                            if (updateResult.isSuccess) {
                                AppTelemetry.logEvent("todo_complete_success")
                            } else {
                                groupsMessage = updateResult.exceptionOrNull()
                                    ?.toActionableMessage("Unable to mark task as completed.")
                                    .orEmpty()
                                logFailureEvent(
                                    name = "todo_complete_failure",
                                    throwable = updateResult.exceptionOrNull(),
                                )
                                updateResult.exceptionOrNull()?.let { error ->
                                    AppTelemetry.recordNonFatal(error, tags = mapOf("op" to "todo_complete"))
                                }
                            }
                        }
                    },
                    onCancelTask = { task ->
                        if (userId.isBlank()) {
                            groupsMessage = "Please sign in again."
                            return@TodoTasksScreen
                        }
                        scope.launch {
                            val updateResult = todoRepository.updateTaskStatus(
                                userId = userId,
                                taskId = task.taskId,
                                status = TodoTaskStatus.CANCELED,
                            )
                            if (updateResult.isSuccess) {
                                AppTelemetry.logEvent("todo_cancel_success")
                            } else {
                                groupsMessage = updateResult.exceptionOrNull()
                                    ?.toActionableMessage("Unable to cancel task.")
                                    .orEmpty()
                                logFailureEvent(
                                    name = "todo_cancel_failure",
                                    throwable = updateResult.exceptionOrNull(),
                                )
                                updateResult.exceptionOrNull()?.let { error ->
                                    AppTelemetry.recordNonFatal(error, tags = mapOf("op" to "todo_cancel"))
                                }
                            }
                        }
                    },
                    onDeleteTask = { task ->
                        if (userId.isBlank()) {
                            groupsMessage = "Please sign in again."
                            return@TodoTasksScreen
                        }
                        scope.launch {
                            val deleteResult = todoRepository.deleteTask(
                                userId = userId,
                                taskId = task.taskId,
                            )
                            if (deleteResult.isSuccess) {
                                groupsMessage = "Task deleted."
                                AppTelemetry.logEvent("todo_delete_success")
                            } else {
                                groupsMessage = deleteResult.exceptionOrNull()
                                    ?.toActionableMessage("Unable to delete task.")
                                    .orEmpty()
                                logFailureEvent(
                                    name = "todo_delete_failure",
                                    throwable = deleteResult.exceptionOrNull(),
                                )
                                deleteResult.exceptionOrNull()?.let { error ->
                                    AppTelemetry.recordNonFatal(error, tags = mapOf("op" to "todo_delete"))
                                }
                            }
                        }
                    },
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }

        composable(route = Destination.SelfExpenses.route) {
            val userId = currentUser?.userId.orEmpty()
            val isLandscape = LocalConfiguration.current.screenWidthDp > LocalConfiguration.current.screenHeightDp
            val personalExpenseFlow: Flow<List<PersonalExpenseEntry>> = remember(userId, realtimeDbConfigIssue) {
                if (userId.isBlank() || !realtimeDbConfigIssue.isNullOrBlank()) {
                    flowOf(emptyList())
                } else {
                    personalExpenseRepository.observeExpenses(userId)
                }
            }
            val personalExpenses by personalExpenseFlow.collectAsState(initial = emptyList())
            var showAddPersonalExpenseDialog by rememberSaveable { mutableStateOf(false) }

            AppScaffoldWithDrawer(
                currentUser = currentUser,
                navController = navController,
                title = "Self Expense Tracker",
                onSignOut = { signOut(origin = "drawer") },
                onNavigateToTodo = { navigateToTodo(origin = "drawer") },
                onNavigateToSelfExpenses = { navigateToSelfExpenses(origin = "drawer") },
                onCreateGroupClick = { openCreateGroupDialog(origin = "drawer") },
                onJoinGroupClick = { openJoinGroupDialog(origin = "drawer") },
                onNavigateToSettings = { navigateToSettings(origin = "drawer") },
                currentThemeMode = currentThemeMode,
                onThemeModeChange = onThemeModeChange,
                floatingActionButton = {
                    if (isLandscape) {
                        FloatingActionButton(
                            onClick = {
                                showAddPersonalExpenseDialog = true
                                logDialogOpen(name = "add_personal_expense", origin = "self_expenses_fab")
                            },
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Add Expense",
                            )
                        }
                    } else {
                        ExtendedFloatingActionButton(
                            modifier = Modifier.padding(bottom = 72.dp),
                            onClick = {
                                showAddPersonalExpenseDialog = true
                                logDialogOpen(name = "add_personal_expense", origin = "self_expenses_fab")
                            },
                            icon = {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = null,
                                )
                            },
                            text = { Text("Add Expense") },
                        )
                    }
                },
            ) {
                if (showAddPersonalExpenseDialog) {
                    AddPersonalExpenseDialog(
                        onDismiss = { showAddPersonalExpenseDialog = false },
                        onAddExpense = { title, amountPaise, spentAtEpochMs ->
                            if (userId.isBlank()) {
                                groupsMessage = "Please sign in again."
                                return@AddPersonalExpenseDialog
                            }
                            if (!realtimeDbConfigIssue.isNullOrBlank()) {
                                groupsMessage = realtimeDbConfigIssue
                                return@AddPersonalExpenseDialog
                            }
                            scope.launch {
                                val addResult = personalExpenseRepository.addExpense(
                                    userId = userId,
                                    title = title,
                                    amountPaise = amountPaise,
                                    spentAtEpochMs = spentAtEpochMs,
                                )
                                if (addResult.isSuccess) {
                                    groupsMessage = "Expense added."
                                    showAddPersonalExpenseDialog = false
                                    AppTelemetry.logEvent("personal_expense_add_success")
                                } else {
                                    groupsMessage = addResult.exceptionOrNull()
                                        ?.toActionableMessage("Unable to add expense.")
                                        .orEmpty()
                                    logFailureEvent(
                                        name = "personal_expense_add_failure",
                                        throwable = addResult.exceptionOrNull(),
                                    )
                                    addResult.exceptionOrNull()?.let { error ->
                                        AppTelemetry.recordNonFatal(
                                            error,
                                            tags = mapOf("op" to "personal_expense_add"),
                                        )
                                    }
                                }
                            }
                        },
                    )
                }
                PersonalExpenseTrackerScreen(
                    expenses = personalExpenses,
                    onDeleteExpense = { expense ->
                        if (userId.isBlank()) {
                            groupsMessage = "Please sign in again."
                            return@PersonalExpenseTrackerScreen
                        }
                        scope.launch {
                            val deleteResult = personalExpenseRepository.deleteExpense(
                                userId = userId,
                                expenseId = expense.expenseId,
                            )
                            if (deleteResult.isSuccess) {
                                groupsMessage = "Expense deleted."
                                AppTelemetry.logEvent("personal_expense_delete_success")
                            } else {
                                groupsMessage = deleteResult.exceptionOrNull()
                                    ?.toActionableMessage("Unable to delete expense.")
                                    .orEmpty()
                                logFailureEvent(
                                    name = "personal_expense_delete_failure",
                                    throwable = deleteResult.exceptionOrNull(),
                                )
                                deleteResult.exceptionOrNull()?.let { error ->
                                    AppTelemetry.recordNonFatal(error, tags = mapOf("op" to "personal_expense_delete"))
                                }
                            }
                        }
                    },
                    onExportPdf = { filteredExpenses, filterDescription ->
                        if (userId.isBlank()) {
                            groupsMessage = "Please sign in again."
                            return@PersonalExpenseTrackerScreen
                        }
                        if (filteredExpenses.isEmpty()) {
                            groupsMessage = "No expenses to export for current filter."
                            return@PersonalExpenseTrackerScreen
                        }
                        scope.launch {
                            val result = context.generatePersonalExpenseReportPdf(
                                userDisplayName = currentUser?.displayName
                                    ?.trim()
                                    .orEmpty()
                                    .ifBlank { "Member" },
                                filterDescription = filterDescription,
                                expenses = filteredExpenses,
                            )
                            if (result.isSuccess) {
                                val pdfPath = result.getOrNull().orEmpty()
                                if (pdfPath.isBlank()) {
                                    groupsMessage = "Unable to export report PDF."
                                    return@launch
                                }
                                context.sharePersonalExpensePdf(pdfPath)
                                groupsMessage = "Expense report PDF generated and shared."
                                AppTelemetry.logEvent(
                                    "personal_expense_export_success",
                                    mapOf("entry_count" to filteredExpenses.size),
                                )
                            } else {
                                val error = result.exceptionOrNull()
                                groupsMessage = error?.toActionableMessage("Unable to export report PDF.")
                                    .orEmpty()
                                logFailureEvent(
                                    name = "personal_expense_export_failure",
                                    throwable = error,
                                    params = mapOf("entry_count" to filteredExpenses.size),
                                )
                                error?.let {
                                    AppTelemetry.recordNonFatal(
                                        throwable = it,
                                        tags = mapOf("op" to "personal_expense_export_pdf"),
                                    )
                                }
                            }
                        }
                    },
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }

        composable(route = Destination.Settings.route) {
            AppScaffoldWithDrawer(
                currentUser = currentUser,
                navController = navController,
                title = "Settings",
                onSignOut = { signOut(origin = "drawer") },
                onNavigateToTodo = { navigateToTodo(origin = "drawer") },
                onNavigateToSelfExpenses = { navigateToSelfExpenses(origin = "drawer") },
                onCreateGroupClick = { openCreateGroupDialog(origin = "drawer") },
                onJoinGroupClick = { openJoinGroupDialog(origin = "drawer") },
                onNavigateToSettings = { navigateToSettings(origin = "drawer") },
                currentThemeMode = currentThemeMode,
                onThemeModeChange = onThemeModeChange,
            ) {
                SettingsScreen(
                    currentDisplayName = currentUser.toFriendlyDisplayName(),
                    currentUpiId = currentUser?.upiId.orEmpty(),
                    currentAccountId = currentUser?.displayId.orEmpty(),
                    currentAccountSummary = currentUser.toSettingsAccountSummary(),
                    currentAccountEmail = currentUser?.email.orEmpty(),
                    isSavingProfile = isSavingProfile,
                    appUpdateSummary = appUpdateSummary,
                    isCheckingForAppUpdate = isCheckingForAppUpdate,
                    isDownloadedUpdateReady = isDownloadedUpdateReady,
                    onSaveProfile = { updatedName, updatedUpiId ->
                        scope.launch {
                            isSavingProfile = true
                            val previousName = currentUser.toFriendlyDisplayName()
                            val previousUpiId = currentUser?.upiId.orEmpty().normalizeUpiId()
                            val result = authRepository.updateProfile(
                                displayName = updatedName,
                                upiId = updatedUpiId,
                            )
                            isSavingProfile = false
                            if (result.isSuccess) {
                                groupsMessage = "Profile updated across your groups."
                                if (updatedName != previousName) {
                                    AppTelemetry.logEvent("profile_name_update_success")
                                }
                                if (updatedUpiId != previousUpiId) {
                                    AppTelemetry.logEvent(
                                        "profile_upi_update_success",
                                        mapOf("has_upi_id" to updatedUpiId.isNotBlank()),
                                    )
                                }
                            } else {
                                groupsMessage = result.exceptionOrNull()
                                    ?.toActionableMessage("Unable to update profile.")
                                    .orEmpty()
                                logFailureEvent(
                                    name = "profile_update_failure",
                                    throwable = result.exceptionOrNull(),
                                    params = mapOf(
                                        "name_changed" to (updatedName != previousName),
                                        "upi_changed" to (updatedUpiId != previousUpiId),
                                    ),
                                )
                                result.exceptionOrNull()?.let { error ->
                                    AppTelemetry.recordNonFatal(error, tags = mapOf("op" to "profile_update"))
                                }
                            }
                        }
                    },
                    onResetTourClick = {
                        context.setAppTourCompleted(false)
                        isTourCompleted = false
                        if (!currentUser?.userId.isNullOrBlank()) {
                            showAppTour = true
                        }
                        groupsMessage = "App tour reset."
                        AppTelemetry.logEvent("app_tour_reset")
                    },
                    onCheckForAppUpdateClick = onCheckForAppUpdateClick,
                    onInstallDownloadedUpdateClick = onInstallDownloadedUpdateClick,
                    onOpenPlayStoreUpdateClick = onOpenPlayStoreUpdateClick,
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }

        composable(route = Destination.Groups.route) {
            LaunchedEffect(realtimeDbConfigIssue) {
                if (!realtimeDbConfigIssue.isNullOrBlank()) {
                    groupsMessage = realtimeDbConfigIssue
                }
            }
            val userId = currentUser?.userId.orEmpty()
            val groupsFlow: Flow<List<Group>> = remember(userId, realtimeDbConfigIssue) {
                if (userId.isBlank() || !realtimeDbConfigIssue.isNullOrBlank()) {
                    flowOf(emptyList())
                } else {
                    groupRepository.observeGroupsForUser(userId)
                }
            }
            val groups by groupsFlow.collectAsState(initial = emptyList())
            val groupNetPaiseByIdFlow: Flow<Map<String, Long>> = remember(
                userId,
                groups,
                realtimeDbConfigIssue,
            ) {
                if (userId.isBlank() || groups.isEmpty() || !realtimeDbConfigIssue.isNullOrBlank()) {
                    flowOf(emptyMap())
                } else {
                    combine(
                        groups.map { group ->
                            expenseRepository.observeBalances(group.groupId)
                                .map { balances ->
                                    group.groupId to (balances.firstOrNull { it.userId == userId }?.netPaise ?: 0L)
                                }
                        },
                    ) { groupBalances ->
                        groupBalances.associate { it }
                    }
                }
            }
            val groupNetPaiseById by groupNetPaiseByIdFlow.collectAsState(initial = emptyMap())

            AppScaffoldWithDrawer(
                currentUser = currentUser,
                navController = navController,
                title = "Your Groups",
                onSignOut = { signOut(origin = "drawer") },
                onNavigateToTodo = { navigateToTodo(origin = "drawer") },
                onNavigateToSelfExpenses = { navigateToSelfExpenses(origin = "drawer") },
                onCreateGroupClick = { openCreateGroupDialog(origin = "drawer") },
                onJoinGroupClick = { openJoinGroupDialog(origin = "drawer") },
                onNavigateToSettings = { navigateToSettings(origin = "drawer") },
                currentThemeMode = currentThemeMode,
                onThemeModeChange = onThemeModeChange,
            ) {
            GroupsListScreen(
                groups = groups,
                currentUserDisplayName = currentUser.toFriendlyDisplayName(),
                currentUserId = userId,
                groupNetPaiseById = groupNetPaiseById,
                appIconResId = R.drawable.ic_app_icon_full,
                onOpenGroup = { groupId ->
                    navigateToGroup(groupId = groupId, origin = "groups_list")
                },
                onShareGroupInvite = { group ->
                    if (group.inviteExpiryEpochMs < System.currentTimeMillis() && !group.autoRenewInvite) {
                        groupsMessage = "Invite has expired. An owner can renew it from Group Details."
                        AppTelemetry.logEvent(
                            "group_invite_share_blocked",
                            mapOf("group_id" to group.groupId, "reason" to "expired"),
                        )
                    } else {
                        AppTelemetry.logEvent(
                            "group_invite_share",
                            mapOf("group_id" to group.groupId, "surface" to "groups_list"),
                        )
                        context.shareJoinLink(group = group)
                    }
                },
                onCopyGroupInvite = { group ->
                    context.copyToClipboard(
                        label = "Invite code",
                        text = group.inviteCode.toNormalizedInviteCode(),
                    )
                    groupsMessage = "Invite code copied."
                    AppTelemetry.logEvent(
                        "group_invite_copy",
                        mapOf("group_id" to group.groupId, "surface" to "groups_list"),
                    )
                },
                onCreateGroupClick = { openCreateGroupDialog(origin = "groups_cta") },
                onJoinGroupClick = { openJoinGroupDialog(origin = "groups_cta") },
                modifier = Modifier.fillMaxSize(),
            )
            }
        }

        composable(
            route = Destination.Group.route,
            arguments = listOf(navArgument(Destination.Group.groupIdArg) { type = NavType.StringType }),
        ) { backStackEntry ->
            val groupId = backStackEntry.arguments?.getString(Destination.Group.groupIdArg).orEmpty()
            val currentUserId = currentUser?.userId.orEmpty()
            val members by groupRepository.observeMembers(groupId).collectAsState(initial = emptyList())
            val groupActivities by groupRepository.observeActivities(groupId).collectAsState(initial = emptyList())
            val expenses by expenseRepository.observeExpenses(groupId).collectAsState(initial = emptyList())
            val balances by expenseRepository.observeBalances(groupId).collectAsState(initial = emptyList())
            val settlementActivities by settlementRepository.observeSettlementActivities(groupId)
                .collectAsState(initial = emptyList())
            val memberNameById = remember(
                members,
                currentUser?.userId,
                currentUser?.displayName,
                currentUser?.email,
            ) {
                val names = members.associate { member ->
                    val resolvedName = member.toFriendlyDisplayName()
                    member.userId to resolvedName
                }.toMutableMap()
                currentUser?.userId
                    ?.takeIf { it.isNotBlank() }
                    ?.let { userId -> names[userId] = currentUser.toFriendlyDisplayName() }
                names
            }
            val memberPhotoUrlById = remember(members, currentUser?.userId, currentUser?.photoUrl) {
                val photos = members.associate { member -> member.userId to member.photoUrl }.toMutableMap()
                currentUser?.userId
                    ?.takeIf { it.isNotBlank() }
                    ?.let { userId ->
                        val photo = currentUser?.photoUrl?.takeIf { it.isNotBlank() }
                        if (!photo.isNullOrBlank()) photos[userId] = photo
                }
                photos
            }
            val memberUpiIdById = remember(
                members,
                currentUser?.userId,
                currentUser?.upiId,
            ) {
                val upiIds = members.associate { member ->
                    member.userId to member.upiId.normalizeUpiId()
                }.toMutableMap()
                currentUser?.userId
                    ?.takeIf { it.isNotBlank() }
                    ?.let { userId -> upiIds[userId] = currentUser?.upiId.orEmpty().normalizeUpiId() }
                upiIds
            }
            val isLandscape = LocalConfiguration.current.screenWidthDp > LocalConfiguration.current.screenHeightDp

            var showAddExpense by rememberSaveable(groupId) { mutableStateOf(false) }
            var showDeleteGroupDialog by rememberSaveable { mutableStateOf(false) }
            var showAddMemberDialog by rememberSaveable { mutableStateOf(false) }
            var showManageMembersDialog by rememberSaveable { mutableStateOf(false) }
            var showMembersListDialog by rememberSaveable { mutableStateOf(false) }
            var showGroupDetailsDialog by rememberSaveable { mutableStateOf(false) }
            var showRecentSettlementsDialog by rememberSaveable { mutableStateOf(false) }
            var showSettlementInfoDialog by rememberSaveable { mutableStateOf(false) }
            var expensePendingDeleteId by rememberSaveable(groupId) { mutableStateOf<String?>(null) }
            var memberPendingRemovalUserId by rememberSaveable(groupId) { mutableStateOf<String?>(null) }
            var memberPendingEditUserId by rememberSaveable(groupId) { mutableStateOf<String?>(null) }
            var recentSettlementPendingDeleteId by rememberSaveable(groupId) { mutableStateOf<String?>(null) }
            var selectedTab by rememberSaveable(groupId) { mutableIntStateOf(0) }
            var infoMessage by rememberSaveable(groupId) { mutableStateOf("") }
            var recentSettlements by remember(groupId) {
                mutableStateOf(settlementHistoryStore.readSettlements(groupId))
            }
            val groupsForTitle by groupRepository.observeGroupsForUser(currentUserId)
                .collectAsState(initial = emptyList())
            val activeCurrentMember = members.firstOrNull { it.userId == currentUserId && it.active }
            val isOwner = activeCurrentMember?.role == Role.OWNER
            val isGroupMember = activeCurrentMember != null
            val hasExpenses = expenses.isNotEmpty()
            val groupSummary = groupsForTitle.firstOrNull { it.groupId == groupId }
            val mainOwnerUserId = groupSummary?.ownerUserId.orEmpty()
            val groupTitle = groupSummary?.name ?: groupId
            val transferSuggestions = remember(balances) { buildSettlementTransfers(balances) }
            val successfulSettlementKeys = remember(settlementActivities) {
                successfulSettlementTransferKeys(settlementActivities)
            }
            val visibleInsightBalances = remember(balances, memberNameById) {
                balances.filter { balance ->
                    memberNameById.containsKey(balance.userId) || balance.netPaise != 0L
                }
            }
            val insightTransferRows = remember(
                transferSuggestions,
                memberNameById,
                memberUpiIdById,
                currentUserId,
                successfulSettlementKeys,
            ) {
                transferSuggestions.map { transfer ->
                    InsightSettlementTransferUi(
                        transferKey = transfer.transferKey,
                        payerUserId = transfer.fromUserId,
                        payerName = memberNameById[transfer.fromUserId].orEmpty().ifBlank { "Member" },
                        receiverUserId = transfer.toUserId,
                        receiverName = memberNameById[transfer.toUserId].orEmpty().ifBlank { "Member" },
                        receiverUpiId = memberUpiIdById[transfer.toUserId].orEmpty(),
                        amountPaise = transfer.amountPaise,
                        amountDisplay = transfer.amountPaise.toRupeeDisplay(),
                        canPayViaUpi = currentUserId.isNotBlank() &&
                            currentUserId == transfer.fromUserId &&
                            transfer.transferKey !in successfulSettlementKeys,
                    )
                }
            }
            val insightTrackedActivities = remember(settlementActivities) {
                settlementActivities
                    .sortedByDescending { it.handledAtEpochMs }
                    .map { activity ->
                        InsightTrackedSettlementActivityUi(
                            activityId = activity.activityId.ifBlank { "${activity.transferKey}_${activity.handledAtEpochMs}" },
                            payerName = activity.payerName,
                            receiverName = activity.receiverName,
                            amountDisplay = activity.amountPaise.toRupeeDisplay(),
                            status = activity.status,
                            handledAtDisplay = activity.handledAtEpochMs.toGroupDetailsDateTime(),
                            referenceDisplay = buildSettlementUpiReferenceLabel(activity),
                        )
                    }
            }
            var pendingInsightUpiSelection by remember(groupId) {
                mutableStateOf<PendingUpiAppSelection?>(null)
            }
            var pendingInsightUpiLaunch by remember(groupId) {
                mutableStateOf<PendingUpiLaunchRecord?>(null)
            }
            var pendingInsightUpiResultResolution by remember(groupId) {
                mutableStateOf<PendingUpiResultResolution?>(null)
            }
            fun recordInsightUpiTransaction(transaction: SettlementUpiTransaction) {
                scope.launch {
                    val recordResult = settlementRepository.recordSettlementActivity(
                        groupId = groupId,
                        activity = transaction,
                    )
                    infoMessage = if (recordResult.isSuccess) {
                        AppTelemetry.logEvent(
                            "settlement_upi_result_received",
                            mapOf(
                                "group_id" to groupId,
                                "entry_point" to "insights",
                                "status" to transaction.status.name.lowercase(),
                                "selected_app" to transaction.paymentAppPackageName.ifBlank { "unknown" },
                                "user_confirmed" to transaction.statusConfirmedByUser,
                                "excluded_from_final" to transaction.excludesFromFinalSettlement,
                            ),
                        )
                        transaction.status.toSettlementUpiUserMessage()
                    } else {
                        recordResult.exceptionOrNull()?.toActionableMessage(
                            "Unable to record the UPI response.",
                        ) ?: "Unable to record the UPI response."
                    }
                }
            }
            val insightUpiLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.StartActivityForResult(),
            ) { activityResult ->
                val launchRecord = pendingInsightUpiLaunch
                pendingInsightUpiLaunch = null
                val transfer = insightTransferRows.firstOrNull { it.transferKey == launchRecord?.transferKey }
                if (launchRecord == null || transfer == null) return@rememberLauncherForActivityResult

                val parseOutcome = parseSettlementUpiTransactionResult(
                    resultCode = activityResult.resultCode,
                    data = activityResult.data,
                    transfer = transfer.toSettlementTransferUi(),
                    launchAttemptId = launchRecord.attemptId,
                    selectedApp = launchRecord.selectedApp,
                    launchDurationMs = (System.currentTimeMillis() - launchRecord.startedAtEpochMs).coerceAtLeast(0L),
                )
                pendingInsightUpiResultResolution = PendingUpiResultResolution(
                    transaction = parseOutcome.transaction,
                    suggestedStatus = parseOutcome.suggestedStatus,
                )
            }

            fun refreshRecentSettlements() {
                recentSettlements = settlementHistoryStore.readSettlements(groupId)
            }

            LaunchedEffect(infoMessage) {
                if (infoMessage.isNotBlank()) {
                    context.showShortToast(infoMessage)
                    infoMessage = ""
                }
            }

            val insightUpiSelection = pendingInsightUpiSelection
            if (insightUpiSelection != null) {
                UpiAppPickerDialog(
                    amountDisplay = insightUpiSelection.transfer.amountDisplay,
                    receiverName = insightUpiSelection.transfer.receiverName,
                    appOptions = insightUpiSelection.appOptions,
                    onDismissRequest = { pendingInsightUpiSelection = null },
                    onAppSelected = { selectedApp ->
                        val selection = insightUpiSelection
                        pendingInsightUpiSelection = null
                        runCatching {
                            pendingInsightUpiLaunch = PendingUpiLaunchRecord(
                                transferKey = selection.transfer.transferKey,
                                attemptId = selection.attemptId,
                                selectedApp = selectedApp,
                                startedAtEpochMs = System.currentTimeMillis(),
                            )
                            insightUpiLauncher.launch(
                                Intent(selection.intent).setPackage(selectedApp.packageName),
                            )
                        }.onFailure {
                            pendingInsightUpiLaunch = null
                            infoMessage = "Unable to open ${selectedApp.displayName} for this payment."
                        }
                    },
                )
            }
            val insightUpiResultResolution = pendingInsightUpiResultResolution
            if (insightUpiResultResolution != null) {
                UpiResultConfirmationDialog(
                    resolution = insightUpiResultResolution,
                    onStatusConfirmed = { confirmedStatus ->
                        val resolution = insightUpiResultResolution
                        pendingInsightUpiResultResolution = null
                        recordInsightUpiTransaction(
                            resolution.transaction.copy(
                                status = confirmedStatus,
                                statusConfirmedByUser = true,
                            ),
                        )
                    },
                )
            }

            if (showAddExpense) {
                val memberOptions = members.map { member ->
                    ExpenseMemberOption(
                        userId = member.userId,
                        displayName = memberNameById[member.userId].orEmpty().ifBlank { "Member" },
                        photoUrl = memberPhotoUrlById[member.userId],
                    )
                }
                AddExpenseDialog(
                    members = memberOptions,
                    currentUserId = currentUserId,
                    selectAllMembersByDefaultForExpenses = groupSummary?.selectAllMembersByDefaultForExpenses == true,
                    onDismiss = { showAddExpense = false },
                    onSave = { draft ->
                        val amountPaise = draft.amountRupees.toPaise()
                        if (amountPaise == null || amountPaise <= 0L) {
                            infoMessage = "Please enter a valid amount."
                            return@AddExpenseDialog
                        }
                        val paymentDate = draft.paymentDate.toBackendPaymentDateOrNull()
                        if (paymentDate == null) {
                            infoMessage = "Please select a valid payment date."
                            return@AddExpenseDialog
                        }
                        if (paymentDate.isAfter(java.time.LocalDate.now())) {
                            infoMessage = "Future payment date is not allowed."
                            return@AddExpenseDialog
                        }
                        val participants = draft.participantUserIds.distinct()
                        if (participants.isEmpty()) {
                            infoMessage = "Select at least one participant."
                            return@AddExpenseDialog
                        }
                        val shares = buildSplitSharesFromDraft(
                            draft = draft,
                            totalPaise = amountPaise,
                            participants = participants,
                        ).getOrElse { error ->
                            infoMessage = error.message ?: "Invalid split values."
                            return@AddExpenseDialog
                        }
                        val splitValidation = SplitCalculator.allocate(
                            totalPaise = amountPaise,
                            participantUserIds = participants,
                            splitType = draft.splitType,
                            shares = shares,
                        )
                        if (splitValidation.isFailure) {
                            infoMessage = splitValidation.exceptionOrNull()?.message ?: "Split is invalid."
                            return@AddExpenseDialog
                        }

                        scope.launch {
                            val newExpense = Expense(
                                expenseId = "exp_${System.currentTimeMillis()}",
                                groupId = groupId,
                                title = draft.title,
                                amountPaise = amountPaise,
                                paymentDate = paymentDate.toBackendPaymentDate(),
                                paidByUserId = draft.paidByUserId,
                                participantUserIds = participants,
                                splitType = draft.splitType,
                                shares = shares,
                                category = draft.category,
                                note = draft.note,
                                createdByUserId = currentUserId,
                                createdAtEpochMs = System.currentTimeMillis(),
                                updatedAtEpochMs = System.currentTimeMillis(),
                            )
                            val result = expenseRepository.addExpense(newExpense)
                            infoMessage = if (result.isSuccess) "Expense added." else {
                                result.exceptionOrNull()?.message ?: "Unable to add expense."
                            }
                            if (result.isSuccess) {
                                AppTelemetry.logEvent(
                                    "expense_add_success",
                                    mapOf(
                                        "group_id" to groupId,
                                        "split_type" to draft.splitType.name.lowercase(),
                                        "category" to draft.category.name.lowercase(),
                                        "has_note" to draft.note.isNotBlank(),
                                    ),
                                )
                                registerHelpfulInteraction("expense_add")
                            } else {
                                logFailureEvent(
                                    name = "expense_add_failure",
                                    throwable = result.exceptionOrNull(),
                                    params = mapOf(
                                        "group_id" to groupId,
                                        "split_type" to draft.splitType.name.lowercase(),
                                        "category" to draft.category.name.lowercase(),
                                        "has_note" to draft.note.isNotBlank(),
                                    ),
                                )
                                result.exceptionOrNull()?.let { error ->
                                    AppTelemetry.recordNonFatal(error, tags = mapOf("op" to "expense_add"))
                                }
                            }
                            showAddExpense = false
                        }
                    },
                )
            }
            val onMarkAsSettledClick: () -> Unit = markAsSettled@{
                if (!isOwner) {
                    infoMessage = "Only an owner can mark settlement."
                    return@markAsSettled
                }
                if (!hasExpenses) {
                    infoMessage = "No expenses to settle."
                    AppTelemetry.logEvent(
                        "no_expense_settle_attempt",
                        mapOf("group_id" to groupId, "entry_point" to "group_overflow"),
                    )
                    return@markAsSettled
                }
                navController.navigate(Destination.SettlementPreview.buildRoute(groupId)) {
                    launchSingleTop = true
                }
                AppTelemetry.logEvent(
                    "settlement_preview_open",
                    mapOf("group_id" to groupId, "entry_point" to "group_overflow"),
                )
            }

            if (showSettlementInfoDialog) {
                AlertDialog(
                    onDismissRequest = { showSettlementInfoDialog = false },
                    title = { Text("About Settlement") },
                    text = {
                        Text(
                            "Settlement generates a final PDF report, sends it to group members, " +
                                "and clears all balances/expenses so a new cycle can begin. " +
                                "Any owner can run settlement and manage the group.",
                        )
                    },
                    confirmButton = {
                        TextButton(onClick = { showSettlementInfoDialog = false }) {
                            Text("Got it")
                        }
                    },
                )
            }

            val detailsGroup = groupSummary
            if (showGroupDetailsDialog && detailsGroup != null) {
                GroupDetailsDialog(
                    group = detailsGroup,
                    isOwner = isOwner,
                    onDismiss = { showGroupDetailsDialog = false },
                    onCopyInvite = {
                        context.copyToClipboard(
                            label = "Invite code",
                            text = detailsGroup.inviteCode.toNormalizedInviteCode(),
                        )
                        infoMessage = "Invite code copied."
                        AppTelemetry.logEvent(
                            "group_invite_copy",
                            mapOf("group_id" to groupId, "surface" to "group_details"),
                        )
                    },
                    onSave = { description, autoRenewInvite, selectAllMembersByDefaultForExpenses ->
                        if (!isOwner) {
                            infoMessage = "Only an owner can update group details."
                            return@GroupDetailsDialog
                        }
                        scope.launch {
                            val updateResult = groupRepository.updateGroupDetails(
                                groupId = groupId,
                                description = description,
                                autoRenewInvite = autoRenewInvite,
                                selectAllMembersByDefaultForExpenses = selectAllMembersByDefaultForExpenses,
                                actorUserId = currentUserId,
                            )
                            if (updateResult.isSuccess) {
                                infoMessage = "Group details updated."
                                showGroupDetailsDialog = false
                                AppTelemetry.logEvent(
                                    "group_details_update_success",
                                    mapOf("group_id" to groupId),
                                )
                                registerHelpfulInteraction("group_details_update")
                            } else {
                                infoMessage = updateResult.exceptionOrNull()?.message
                                    ?: "Unable to update group details."
                                logFailureEvent(
                                    name = "group_details_update_failure",
                                    throwable = updateResult.exceptionOrNull(),
                                    params = mapOf("group_id" to groupId),
                                )
                                updateResult.exceptionOrNull()?.let { error ->
                                    AppTelemetry.recordNonFatal(error, tags = mapOf("op" to "group_details_update"))
                                }
                            }
                        }
                    },
                    onRenewInvite = {
                        if (!isOwner) {
                            infoMessage = "Only an owner can renew the invite."
                            return@GroupDetailsDialog
                        }
                        scope.launch {
                            val renewResult = groupRepository.renewInvite(
                                groupId = groupId,
                                actorUserId = currentUserId,
                            )
                            if (renewResult.isSuccess) {
                                infoMessage = "Invite renewed."
                                AppTelemetry.logEvent(
                                    "group_invite_renew_success",
                                    mapOf("group_id" to groupId),
                                )
                            } else {
                                infoMessage = renewResult.exceptionOrNull()?.message
                                    ?: "Unable to renew invite."
                                logFailureEvent(
                                    name = "group_invite_renew_failure",
                                    throwable = renewResult.exceptionOrNull(),
                                    params = mapOf("group_id" to groupId),
                                )
                                renewResult.exceptionOrNull()?.let { error ->
                                    AppTelemetry.recordNonFatal(error, tags = mapOf("op" to "group_invite_renew"))
                                }
                            }
                        }
                    },
                )
            }

            if (showDeleteGroupDialog) {
                AlertDialog(
                    onDismissRequest = { showDeleteGroupDialog = false },
                    title = { Text("Delete Group?") },
                    text = {
                        Text("This will permanently delete this group for all members. This action cannot be undone.")
                    },
                    confirmButton = {
                        TextButton(
                            enabled = isOwner,
                            onClick = {
                                showDeleteGroupDialog = false
                                if (!isOwner) return@TextButton

                                AppTelemetry.logEvent("group_delete_attempt", mapOf("group_id" to groupId))
                                scope.launch {
                                    val deleteResult = groupRepository.deleteGroup(
                                        groupId = groupId,
                                        actorUserId = currentUserId,
                                    )
                                    if (deleteResult.isSuccess) {
                                        groupsMessage = "Group deleted."
                                        infoMessage = ""
                                        AppTelemetry.logEvent("group_delete_success", mapOf("group_id" to groupId))
                                        navController.navigate(Destination.Groups.route) {
                                            popUpTo(Destination.Groups.route) { inclusive = false }
                                            launchSingleTop = true
                                        }
                                    } else {
                                        infoMessage = deleteResult.exceptionOrNull()?.message
                                            ?: "Unable to delete group."
                                        logFailureEvent(
                                            name = "group_delete_failure",
                                            throwable = deleteResult.exceptionOrNull(),
                                            params = mapOf("group_id" to groupId),
                                        )
                                        deleteResult.exceptionOrNull()?.let { error ->
                                            AppTelemetry.recordNonFatal(error, tags = mapOf("op" to "group_delete"))
                                        }
                                    }
                                }
                            },
                        ) {
                            Text("Delete")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showDeleteGroupDialog = false }) {
                            Text("Cancel")
                        }
                    },
                )
            }

            if (showManageMembersDialog) {
                ManageMembersDialog(
                    members = members,
                    mainOwnerUserId = mainOwnerUserId,
                    onDismiss = { showManageMembersDialog = false },
                    onEditClick = { member ->
                        showManageMembersDialog = false
                        memberPendingEditUserId = member.userId
                    },
                    onToggleOwnerClick = { member, makeOwner ->
                        scope.launch {
                            val updateRoleResult = groupRepository.updateMemberRole(
                                groupId = groupId,
                                memberUserId = member.userId,
                                role = if (makeOwner) Role.OWNER else Role.MEMBER,
                                actorUserId = currentUserId,
                            )
                            if (updateRoleResult.isSuccess) {
                                infoMessage = if (makeOwner) {
                                    "${member.toFriendlyDisplayName()} is now an owner."
                                } else {
                                    "${member.toFriendlyDisplayName()} is now a member."
                                }
                                AppTelemetry.logEvent(
                                    "member_role_update_success",
                                    mapOf(
                                        "group_id" to groupId,
                                        "new_role" to if (makeOwner) "owner" else "member",
                                    ),
                                )
                                registerHelpfulInteraction("member_role_update")
                            } else {
                                infoMessage = updateRoleResult.exceptionOrNull()?.message
                                    ?: "Unable to update member role."
                                logFailureEvent(
                                    name = "member_role_update_failure",
                                    throwable = updateRoleResult.exceptionOrNull(),
                                    params = mapOf(
                                        "group_id" to groupId,
                                        "new_role" to if (makeOwner) "owner" else "member",
                                    ),
                                )
                                updateRoleResult.exceptionOrNull()?.let { error ->
                                    AppTelemetry.recordNonFatal(error, tags = mapOf("op" to "member_role_update"))
                                }
                            }
                        }
                    },
                    onRemoveClick = { member ->
                        showManageMembersDialog = false
                        memberPendingRemovalUserId = member.userId
                    },
                )
            }

            if (showMembersListDialog) {
                MembersListDialog(
                    members = members,
                    mainOwnerUserId = mainOwnerUserId,
                    onDismiss = { showMembersListDialog = false },
                )
            }

            if (showRecentSettlementsDialog) {
                RecentSettlementsDialog(
                    groupName = groupTitle,
                    settlements = recentSettlements,
                    onDismiss = { showRecentSettlementsDialog = false },
                    onOpenPdf = { settlement ->
                        if (settlement.pdfExists) {
                            AppTelemetry.logEvent(
                                "recent_settlement_open_pdf",
                                mapOf("group_id" to groupId),
                            )
                            context.openSettlementPdf(settlement.pdfPath)
                        } else {
                            infoMessage = "Saved PDF is missing from this device."
                            AppTelemetry.logEvent(
                                "recent_settlement_open_pdf_failed",
                                mapOf("group_id" to groupId, "reason" to "missing_pdf"),
                            )
                            refreshRecentSettlements()
                        }
                    },
                    onSharePdf = { settlement ->
                        if (settlement.pdfExists) {
                            AppTelemetry.logEvent(
                                "recent_settlement_share_pdf",
                                mapOf("group_id" to groupId),
                            )
                            context.shareSettlementPdf(settlement.pdfPath)
                        } else {
                            infoMessage = "Saved PDF is missing from this device."
                            AppTelemetry.logEvent(
                                "recent_settlement_share_pdf_failed",
                                mapOf("group_id" to groupId, "reason" to "missing_pdf"),
                            )
                            refreshRecentSettlements()
                        }
                    },
                    onDeleteEntry = { settlement ->
                        logDialogOpen(
                            name = "delete_settlement_pdf",
                            origin = "recent_settlements",
                            params = mapOf("group_id" to groupId),
                        )
                        recentSettlementPendingDeleteId = settlement.entryId
                    },
                )
            }

            if (showAddMemberDialog) {
                AddMemberDialog(
                    onDismiss = { showAddMemberDialog = false },
                    onAdd = { memberName ->
                        if (!isGroupMember) {
                            infoMessage = "Only active group members can add members."
                            return@AddMemberDialog
                        }
                        scope.launch {
                            val addResult = groupRepository.addManualMember(
                                groupId = groupId,
                                displayName = memberName,
                                actorUserId = currentUserId,
                            )
                            if (addResult.isSuccess) {
                                infoMessage = "Member added."
                                showAddMemberDialog = false
                                AppTelemetry.logEvent(
                                    "member_add_manual_success",
                                    mapOf("group_id" to groupId),
                                )
                                registerHelpfulInteraction("member_add_manual")
                            } else {
                                infoMessage = addResult.exceptionOrNull()?.message
                                    ?: "Unable to add member."
                                logFailureEvent(
                                    name = "member_add_manual_failure",
                                    throwable = addResult.exceptionOrNull(),
                                    params = mapOf("group_id" to groupId),
                                )
                                addResult.exceptionOrNull()?.let { error ->
                                    AppTelemetry.recordNonFatal(error, tags = mapOf("op" to "member_add_manual"))
                                }
                            }
                        }
                    },
                )
            }

            val editMember = members.firstOrNull { it.userId == memberPendingEditUserId }
            if (editMember != null) {
                EditMemberDialog(
                    initialName = editMember.displayName.ifBlank { editMember.toFriendlyDisplayName() },
                    onDismiss = { memberPendingEditUserId = null },
                    onSave = { updatedName ->
                        if (!isOwner) {
                            infoMessage = "Only an owner can edit member names."
                            return@EditMemberDialog
                        }
                        memberPendingEditUserId = null
                        scope.launch {
                            val updateResult = groupRepository.updateMemberDisplayName(
                                groupId = groupId,
                                memberUserId = editMember.userId,
                                displayName = updatedName,
                                actorUserId = currentUserId,
                            )
                            if (updateResult.isSuccess) {
                                infoMessage = "Member updated."
                                AppTelemetry.logEvent(
                                    "member_edit_manual_success",
                                    mapOf("group_id" to groupId),
                                )
                                registerHelpfulInteraction("member_edit_manual")
                            } else {
                                infoMessage = updateResult.exceptionOrNull()?.message ?: "Unable to update member."
                                logFailureEvent(
                                    name = "member_edit_manual_failure",
                                    throwable = updateResult.exceptionOrNull(),
                                    params = mapOf("group_id" to groupId),
                                )
                                updateResult.exceptionOrNull()?.let { error ->
                                    AppTelemetry.recordNonFatal(error, tags = mapOf("op" to "member_edit_manual"))
                                }
                            }
                        }
                    },
                )
            }

            val removeMember = members.firstOrNull { it.userId == memberPendingRemovalUserId }
            if (removeMember != null) {
                AlertDialog(
                    onDismissRequest = { memberPendingRemovalUserId = null },
                    title = { Text("Remove Member?") },
                    text = {
                        Text(
                            "Remove ${removeMember.toFriendlyDisplayName()} from this group? " +
                                "Any pending balance will be absorbed by the owner.",
                        )
                    },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                memberPendingRemovalUserId = null
                                scope.launch {
                                    val removeResult = groupRepository.removeMember(
                                        groupId = groupId,
                                        memberUserId = removeMember.userId,
                                        actorUserId = currentUserId,
                                        liabilityChoice = ExitLiabilityChoice.ABSORB_BY_OWNER,
                                    )
                                    infoMessage = if (removeResult.isSuccess) {
                                        AppTelemetry.logEvent(
                                            "member_remove_success",
                                            mapOf("group_id" to groupId),
                                        )
                                        "Member removed."
                                    } else {
                                        removeResult.exceptionOrNull()?.message
                                            ?: "Unable to remove member."
                                    }
                                    if (removeResult.isFailure) {
                                        logFailureEvent(
                                            name = "member_remove_failure",
                                            throwable = removeResult.exceptionOrNull(),
                                            params = mapOf("group_id" to groupId),
                                        )
                                        removeResult.exceptionOrNull()?.let { error ->
                                            AppTelemetry.recordNonFatal(error, tags = mapOf("op" to "member_remove"))
                                        }
                                    }
                                }
                            },
                        ) {
                            Text("Remove")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { memberPendingRemovalUserId = null }) {
                            Text("Cancel")
                        }
                    },
                )
            }

            val settlementToDelete = recentSettlements.firstOrNull {
                it.entryId == recentSettlementPendingDeleteId
            }
            if (settlementToDelete != null) {
                AlertDialog(
                    onDismissRequest = { recentSettlementPendingDeleteId = null },
                    title = { Text("Delete Settlement PDF?") },
                    text = {
                        Text(
                            "Delete the saved settlement entry from ${settlementToDelete.settledAtEpochMs.toGroupDetailsDateTime()}? " +
                                "The PDF will be removed permanently from this device.",
                        )
                    },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                recentSettlementPendingDeleteId = null
                                val deleted = settlementHistoryStore.deleteSettlement(
                                    groupId = groupId,
                                    entryId = settlementToDelete.entryId,
                                )
                                refreshRecentSettlements()
                                infoMessage = if (deleted) {
                                    AppTelemetry.logEvent(
                                        "recent_settlement_delete_success",
                                        mapOf("group_id" to groupId),
                                    )
                                    "Settlement entry deleted."
                                } else {
                                    AppTelemetry.logEvent(
                                        "recent_settlement_delete_failure",
                                        mapOf("group_id" to groupId, "reason" to "delete_returned_false"),
                                    )
                                    "Unable to delete settlement entry."
                                }
                            },
                        ) {
                            Text("Delete")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { recentSettlementPendingDeleteId = null }) {
                            Text("Cancel")
                        }
                    },
                )
            }

            val expenseToDelete = expenses.firstOrNull { it.expenseId == expensePendingDeleteId }
            if (expenseToDelete != null) {
                AlertDialog(
                    onDismissRequest = { expensePendingDeleteId = null },
                    title = { Text("Delete Entry?") },
                    text = {
                        Text(
                            "Delete expense \"${expenseToDelete.title}\" for " +
                                "${expenseToDelete.amountPaise.toRupeeDisplay()}?",
                        )
                    },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                expensePendingDeleteId = null
                                AppTelemetry.logEvent(
                                    "expense_delete_attempt",
                                    mapOf("group_id" to groupId, "expense_id" to expenseToDelete.expenseId),
                                )
                                scope.launch {
                                    val deleteResult = expenseRepository.deleteExpense(
                                        groupId = groupId,
                                        expenseId = expenseToDelete.expenseId,
                                        actorUserId = currentUserId,
                                    )
                                    infoMessage = if (deleteResult.isSuccess) {
                                        AppTelemetry.logEvent(
                                            "expense_delete_success",
                                            mapOf("group_id" to groupId, "expense_id" to expenseToDelete.expenseId),
                                        )
                                        "Entry deleted."
                                    } else {
                                        deleteResult.exceptionOrNull()
                                            ?.toActionableMessage("Unable to delete entry.")
                                            ?: "Unable to delete entry."
                                    }
                                    if (deleteResult.isFailure) {
                                        logFailureEvent(
                                            name = "expense_delete_failure",
                                            throwable = deleteResult.exceptionOrNull(),
                                            params = mapOf("group_id" to groupId, "expense_id" to expenseToDelete.expenseId),
                                        )
                                        deleteResult.exceptionOrNull()?.let { error ->
                                            AppTelemetry.recordNonFatal(
                                                error,
                                                tags = mapOf("op" to "expense_delete"),
                                            )
                                        }
                                    }
                                }
                            },
                        ) {
                            Text("Delete")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { expensePendingDeleteId = null }) {
                            Text("Cancel")
                        }
                    },
                )
            }

            AppScaffoldWithDrawer(
                currentUser = currentUser,
                navController = navController,
                title = groupTitle,
                onSignOut = { signOut(origin = "drawer") },
                onNavigateToTodo = { navigateToTodo(origin = "drawer") },
                onNavigateToSelfExpenses = { navigateToSelfExpenses(origin = "drawer") },
                onCreateGroupClick = { openCreateGroupDialog(origin = "drawer") },
                onJoinGroupClick = { openJoinGroupDialog(origin = "drawer") },
                onNavigateToSettings = { navigateToSettings(origin = "drawer") },
                currentThemeMode = currentThemeMode,
                onThemeModeChange = onThemeModeChange,
                topBarActions = {
                    IconButton(
                        onClick = {
                            showSettlementInfoDialog = true
                            logDialogOpen(name = "settlement_info", origin = "group_top_bar", params = mapOf("group_id" to groupId))
                        },
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.HelpOutline,
                            contentDescription = "Settlement info",
                        )
                    }
                    GroupOverflowMenu(
                        isOwner = isOwner,
                        canAddMember = isGroupMember,
                        canViewMembers = isGroupMember,
                        canViewRecentSettlements = isOwner,
                        hasExpenses = hasExpenses,
                        onMarkAsSettledClick = onMarkAsSettledClick,
                        onAddMemberClick = {
                            showAddMemberDialog = true
                            logDialogOpen(name = "add_member", origin = "group_overflow", params = mapOf("group_id" to groupId))
                        },
                        onManageMembersClick = {
                            showManageMembersDialog = true
                            logDialogOpen(name = "manage_members", origin = "group_overflow", params = mapOf("group_id" to groupId))
                        },
                        onGroupDetailsClick = {
                            showGroupDetailsDialog = true
                            logDialogOpen(name = "group_details", origin = "group_overflow", params = mapOf("group_id" to groupId))
                        },
                        onViewMembersClick = {
                            showMembersListDialog = true
                            logDialogOpen(name = "members_list", origin = "group_overflow", params = mapOf("group_id" to groupId))
                        },
                        onRecentSettlementsClick = {
                            refreshRecentSettlements()
                            showRecentSettlementsDialog = true
                            logDialogOpen(name = "recent_settlements", origin = "group_overflow", params = mapOf("group_id" to groupId))
                        },
                        onDeleteGroupClick = {
                            showDeleteGroupDialog = true
                            logDialogOpen(name = "delete_group", origin = "group_overflow", params = mapOf("group_id" to groupId))
                        },
                    )
                },
                floatingActionButton = {
                    if (isLandscape) {
                        FloatingActionButton(
                            onClick = {
                                showAddExpense = true
                                logDialogOpen(name = "add_group_expense", origin = "group_fab", params = mapOf("group_id" to groupId))
                            },
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Add Expense",
                            )
                        }
                    } else {
                        ExtendedFloatingActionButton(
                            onClick = {
                                showAddExpense = true
                                logDialogOpen(name = "add_group_expense", origin = "group_fab", params = mapOf("group_id" to groupId))
                            },
                            icon = {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = null,
                                )
                            },
                            text = { Text("Add Expense") },
                        )
                    }
                },
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                ) {
                    PillTabSelector(
                        tabs = listOf("Ledger", "Insights", "Activity"),
                        selectedIndex = selectedTab,
                        onSelectedIndexChange = { newIndex ->
                            if (selectedTab != newIndex) {
                                selectedTab = newIndex
                                AppTelemetry.logEvent(
                                    "group_tab_selected",
                                    mapOf(
                                        "group_id" to groupId,
                                        "tab" to when (newIndex) {
                                            0 -> "ledger"
                                            1 -> "insights"
                                            else -> "activity"
                                        },
                                    ),
                                )
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                    )

                    when (selectedTab) {
                        0 -> LedgerScreen(
                            expenses = expenses,
                            settlementActivities = settlementActivities,
                            memberNameById = memberNameById,
                            memberPhotoUrlById = memberPhotoUrlById,
                            allowDelete = isOwner && settlementActivities.isEmpty(),
                            onDeleteExpenseClick = { expense ->
                                logDialogOpen(
                                    name = "delete_expense",
                                    origin = "ledger",
                                    params = mapOf("group_id" to groupId, "expense_id" to expense.expenseId),
                                )
                                expensePendingDeleteId = expense.expenseId
                            },
                            modifier = Modifier.weight(1f),
                        )

                        1 -> InsightsScreen(
                            balances = visibleInsightBalances,
                            memberNameById = memberNameById,
                            expenses = expenses,
                            totalExpensePaise = expenses.sumOf { it.amountPaise },
                            settlementTransfers = insightTransferRows,
                            trackedActivities = insightTrackedActivities,
                            onSuggestedTransferPayClick = { transfer ->
                                if (transfer.transferKey in successfulSettlementKeys) {
                                    infoMessage = "A successful UPI payment is already recorded for this transfer."
                                    return@InsightsScreen
                                }
                                val settlementTransfer = transfer.toSettlementTransferUi()
                                val attemptId = buildSettlementUpiAttemptId()
                                val baseIntent = buildSettlementUpiPaymentIntent(
                                    upiId = transfer.receiverUpiId,
                                    receiverName = transfer.receiverName,
                                    amountPaise = transfer.amountPaise,
                                    groupName = groupTitle,
                                    attemptId = attemptId,
                                )
                                if (baseIntent == null) {
                                    infoMessage = "UPI payment is unavailable for this transfer."
                                } else {
                                    val availableApps = context.findAvailableUpiApps(baseIntent)
                                    if (availableApps.isEmpty()) {
                                        infoMessage = "No UPI app is installed on this device."
                                    } else {
                                        pendingInsightUpiSelection = PendingUpiAppSelection(
                                            transfer = settlementTransfer,
                                            attemptId = attemptId,
                                            intent = baseIntent,
                                            appOptions = availableApps,
                                        )
                                    }
                                }
                            },
                            modifier = Modifier.weight(1f),
                        )

                        else -> GroupActivityFeedScreen(
                            activities = groupActivities,
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
            }
        }

        composable(
            route = Destination.SettlementPreview.route,
            arguments = listOf(
                navArgument(Destination.SettlementPreview.groupIdArg) { type = NavType.StringType },
            ),
        ) { backStackEntry ->
            val groupId = backStackEntry.arguments
                ?.getString(Destination.SettlementPreview.groupIdArg)
                .orEmpty()
            val currentUserId = currentUser?.userId.orEmpty()
            val members by groupRepository.observeMembers(groupId).collectAsState(initial = emptyList())
            val expenses by expenseRepository.observeExpenses(groupId).collectAsState(initial = emptyList())
            val balances by expenseRepository.observeBalances(groupId).collectAsState(initial = emptyList())
            val settlementActivities by settlementRepository.observeSettlementActivities(groupId)
                .collectAsState(initial = emptyList())
            val memberNameById = remember(
                members,
                currentUser?.userId,
                currentUser?.displayName,
                currentUser?.email,
            ) {
                val names = members.associate { member ->
                    member.userId to member.toFriendlyDisplayName()
                }.toMutableMap()
                currentUser?.userId
                    ?.takeIf { it.isNotBlank() }
                    ?.let { userId -> names[userId] = currentUser.toFriendlyDisplayName() }
                names
            }
            val memberUpiIdById = remember(
                members,
                currentUser?.userId,
                currentUser?.upiId,
            ) {
                val upiIds = members.associate { member ->
                    member.userId to member.upiId.normalizeUpiId()
                }.toMutableMap()
                currentUser?.userId
                    ?.takeIf { it.isNotBlank() }
                    ?.let { userId -> upiIds[userId] = currentUser?.upiId.orEmpty().normalizeUpiId() }
                upiIds
            }
            val groupsForTitle by groupRepository.observeGroupsForUser(currentUserId)
                .collectAsState(initial = emptyList())
            val group = groupsForTitle.firstOrNull { it.groupId == groupId }
            var settlementGroupDisplayName by rememberSaveable(groupId) { mutableStateOf(groupId) }
            LaunchedEffect(group?.name) {
                group?.name
                    ?.takeIf { it.isNotBlank() }
                    ?.let { settlementGroupDisplayName = it }
            }
            val isOwner = members.firstOrNull { it.userId == currentUserId }?.role == Role.OWNER
            val activeMembersCount = members.count { it.active }
            val totalExpensePaise = expenses.sumOf { it.amountPaise }
            val transferSuggestions = remember(balances) { buildSettlementTransfers(balances) }
            val successfulSettlementKeys = remember(settlementActivities) {
                successfulSettlementTransferKeys(settlementActivities)
            }
            val settlementSummaryUi = remember(
                group?.name,
                groupId,
                activeMembersCount,
                expenses.size,
                totalExpensePaise,
                balances,
            ) {
                SettlementPreviewSummaryUi(
                    groupName = settlementGroupDisplayName,
                    activeMembersCount = activeMembersCount,
                    expenseEntriesCount = expenses.size,
                    totalAmountDisplay = totalExpensePaise.toRupeeDisplay(),
                    openBalancesCount = balances.count { it.netPaise != 0L },
                )
            }
            val allSettlementTransferRows = remember(
                transferSuggestions,
                memberNameById,
                memberUpiIdById,
                currentUserId,
                successfulSettlementKeys,
            ) {
                transferSuggestions.map { transfer ->
                    SettlementTransferUi(
                        transferKey = transfer.transferKey,
                        payerUserId = transfer.fromUserId,
                        receiverUserId = transfer.toUserId,
                        payerName = memberNameById[transfer.fromUserId].orEmpty().ifBlank { "Member" },
                        receiverName = memberNameById[transfer.toUserId].orEmpty().ifBlank { "Member" },
                        amountDisplay = transfer.amountPaise.toRupeeDisplay(),
                        amountPaise = transfer.amountPaise,
                        receiverUpiId = memberUpiIdById[transfer.toUserId].orEmpty(),
                        canPayViaUpi = currentUserId.isNotBlank() &&
                            currentUserId == transfer.fromUserId &&
                            transfer.transferKey !in successfulSettlementKeys,
                    )
                }
            }
            var pendingSettlementUpiSelection by remember(groupId) {
                mutableStateOf<PendingUpiAppSelection?>(null)
            }
            var pendingSettlementUpiLaunch by remember(groupId) {
                mutableStateOf<PendingUpiLaunchRecord?>(null)
            }
            var pendingSettlementUpiResultResolution by remember(groupId) {
                mutableStateOf<PendingUpiResultResolution?>(null)
            }
            val settlementTransferRows = allSettlementTransferRows
            val trackedUpiResponseRows = remember(settlementActivities) {
                settlementActivities
                    .sortedByDescending { it.handledAtEpochMs }
                    .map { transaction ->
                        SettlementTrackedUpiResponseUi(
                            payerName = transaction.payerName,
                            receiverName = transaction.receiverName,
                            amountDisplay = transaction.amountPaise.toRupeeDisplay(),
                            status = transaction.status,
                            handledAtDisplay = transaction.handledAtEpochMs.toGroupDetailsDateTime(),
                            referenceDisplay = buildSettlementUpiReferenceLabel(transaction),
                        )
                    }
            }

            var isFinalizingSettlement by rememberSaveable { mutableStateOf(false) }
            var infoMessage by remember { mutableStateOf("") }
            var settlementInterstitialAd by remember(groupId) { mutableStateOf<InterstitialAd?>(null) }
            var isSettlementAdLoading by remember(groupId) { mutableStateOf(false) }
            var settlementCelebrationState by remember(groupId) {
                mutableStateOf<PendingSettlementCelebration?>(null)
            }
            var isSettlementCelebrationVisible by remember(groupId) { mutableStateOf(false) }
            fun recordSettlementUpiTransaction(transaction: SettlementUpiTransaction) {
                scope.launch {
                    val recordResult = settlementRepository.recordSettlementActivity(
                        groupId = groupId,
                        activity = transaction,
                    )
                    infoMessage = if (recordResult.isSuccess) {
                        AppTelemetry.logEvent(
                            "settlement_upi_result_received",
                            mapOf(
                                "group_id" to groupId,
                                "entry_point" to "settlement_preview",
                                "status" to transaction.status.name.lowercase(),
                                "selected_app" to transaction.paymentAppPackageName.ifBlank { "unknown" },
                                "user_confirmed" to transaction.statusConfirmedByUser,
                                "response_code" to transaction.responseCode.ifBlank { "none" },
                                "excluded_from_final" to transaction.excludesFromFinalSettlement,
                            ),
                        )
                        transaction.status.toSettlementUpiUserMessage()
                    } else {
                        recordResult.exceptionOrNull()?.toActionableMessage(
                            "Unable to record the UPI response.",
                        ) ?: "Unable to record the UPI response."
                    }
                }
            }
            val settlementUpiLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.StartActivityForResult(),
            ) { activityResult ->
                val launchRecord = pendingSettlementUpiLaunch
                pendingSettlementUpiLaunch = null
                val transfer = allSettlementTransferRows.firstOrNull { it.transferKey == launchRecord?.transferKey }
                if (launchRecord == null || transfer == null) return@rememberLauncherForActivityResult

                val parseOutcome = parseSettlementUpiTransactionResult(
                    resultCode = activityResult.resultCode,
                    data = activityResult.data,
                    transfer = transfer,
                    launchAttemptId = launchRecord.attemptId,
                    selectedApp = launchRecord.selectedApp,
                    launchDurationMs = (System.currentTimeMillis() - launchRecord.startedAtEpochMs).coerceAtLeast(0L),
                )
                pendingSettlementUpiResultResolution = PendingUpiResultResolution(
                    transaction = parseOutcome.transaction,
                    suggestedStatus = parseOutcome.suggestedStatus,
                )
            }

            fun loadSettlementInterstitial() {
                if (isSettlementAdLoading || settlementInterstitialAd != null) return
                isSettlementAdLoading = true
                InterstitialAd.load(
                    context,
                    SETTLEMENT_INTERSTITIAL_AD_UNIT_ID,
                    AdRequest.Builder().build(),
                    object : InterstitialAdLoadCallback() {
                        override fun onAdLoaded(interstitialAd: InterstitialAd) {
                            isSettlementAdLoading = false
                            settlementInterstitialAd = interstitialAd
                        }

                        override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                            isSettlementAdLoading = false
                            settlementInterstitialAd = null
                        }
                    },
                )
            }

            fun startSettlementFinalization() {
                if (isFinalizingSettlement) return

                isFinalizingSettlement = true
                scope.launch {
                    val pdfPath = settlementRepository.generateSettlementPdf(
                        groupId = groupId,
                        actorUserId = currentUserId,
                    ).getOrElse { error ->
                        isFinalizingSettlement = false
                        infoMessage = error.toActionableMessage(
                            "Unable to generate settlement PDF.",
                        )
                        AppTelemetry.logEvent(
                            "pdf_generate_fail",
                            mapOf(
                                "group_id" to groupId,
                                "entry_point" to "settlement_preview",
                                "reason" to error.message.orEmpty().ifBlank { "unknown" },
                            ),
                        )
                        AppTelemetry.recordNonFatal(
                            error,
                            tags = mapOf("op" to "settlement_pdf_generate"),
                        )
                        return@launch
                    }

                    val dispatchResult = settlementRepository.dispatchSettlementPdfToMembers(
                        groupId = groupId,
                        actorUserId = currentUserId,
                        pdfPath = pdfPath,
                    )
                    if (dispatchResult.isFailure) {
                        isFinalizingSettlement = false
                        val error = dispatchResult.exceptionOrNull()
                        infoMessage = error?.toActionableMessage(
                            "Unable to dispatch settlement PDF.",
                        ) ?: "Unable to dispatch settlement PDF."
                        AppTelemetry.logEvent(
                            "dispatch_fail",
                            mapOf(
                                "group_id" to groupId,
                                "entry_point" to "settlement_preview",
                                "reason" to error?.message.orEmpty().ifBlank { "unknown" },
                            ),
                        )
                        error?.let {
                            AppTelemetry.recordNonFatal(
                                throwable = it,
                                tags = mapOf("op" to "settlement_pdf_dispatch"),
                            )
                        }
                        return@launch
                    }

                    val settleResult = settlementRepository.markGroupSettled(
                        groupId = groupId,
                        actorUserId = currentUserId,
                    )
                    if (settleResult.isFailure) {
                        isFinalizingSettlement = false
                        val error = settleResult.exceptionOrNull()
                        infoMessage = error?.toActionableMessage(
                            "Unable to complete settlement.",
                        ) ?: "Unable to complete settlement."
                        logFailureEvent(
                            name = "settlement_complete_failure",
                            throwable = error,
                            params = mapOf(
                                "group_id" to groupId,
                                "entry_point" to "settlement_preview",
                            ),
                        )
                        error?.let {
                            AppTelemetry.recordNonFatal(
                                throwable = it,
                                tags = mapOf("op" to "settlement_mark_group"),
                            )
                        }
                        return@launch
                    }

                    val settledAtEpochMs = System.currentTimeMillis()
                    settlementHistoryStore.addSettlement(
                        RecentSettlementRecord(
                            entryId = "settlement_$settledAtEpochMs",
                            groupId = groupId,
                            groupName = group?.name ?: groupId,
                            settledAtEpochMs = settledAtEpochMs,
                            pdfPath = pdfPath,
                        ),
                    )
                    AppTelemetry.logEvent(
                        "settlement_complete_success",
                        mapOf(
                            "group_id" to groupId,
                            "entry_point" to "settlement_preview",
                        ),
                    )
                    registerHelpfulInteraction("settlement_complete")
                    isFinalizingSettlement = false
                    settlementCelebrationState = PendingSettlementCelebration(
                        pdfPath = pdfPath,
                        successUi = SettlementSuccessUi(
                            groupName = settlementGroupDisplayName,
                            transferCount = settlementTransferRows.size,
                            totalAmountDisplay = settlementSummaryUi.totalAmountDisplay,
                        ),
                    )
                    isSettlementCelebrationVisible = true
                }
            }

            fun beginSettlementFlow() {
                if (!isOwner) {
                    infoMessage = "Only an owner can mark settlement."
                    return
                }
                if (expenses.isEmpty()) {
                    infoMessage = "No expenses to settle."
                    AppTelemetry.logEvent(
                        "no_expense_settle_attempt",
                        mapOf(
                            "group_id" to groupId,
                            "entry_point" to "settlement_preview",
                        ),
                    )
                    return
                }
                if (isFinalizingSettlement) return

                val loadedAd = settlementInterstitialAd
                val hostActivity = context.findActivity()
                AppTelemetry.logEvent(
                    "settlement_finalize_attempt",
                    mapOf(
                        "group_id" to groupId,
                        "entry_point" to "settlement_preview",
                        "has_interstitial" to (loadedAd != null && hostActivity != null),
                    ),
                )
                if (loadedAd == null || hostActivity == null) {
                    startSettlementFinalization()
                    loadSettlementInterstitial()
                    return
                }

                settlementInterstitialAd = null
                loadedAd.fullScreenContentCallback = object : FullScreenContentCallback() {
                    override fun onAdDismissedFullScreenContent() {
                        startSettlementFinalization()
                        loadSettlementInterstitial()
                    }

                    override fun onAdFailedToShowFullScreenContent(adError: com.google.android.gms.ads.AdError) {
                        startSettlementFinalization()
                        loadSettlementInterstitial()
                    }
                }
                loadedAd.show(hostActivity)
            }

            LaunchedEffect(infoMessage) {
                if (infoMessage.isNotBlank()) {
                    context.showShortToast(infoMessage)
                    infoMessage = ""
                }
            }

            LaunchedEffect(groupId) {
                loadSettlementInterstitial()
            }

            LaunchedEffect(settlementCelebrationState?.pdfPath) {
                val celebration = settlementCelebrationState ?: return@LaunchedEffect
                AppTelemetry.logEvent(
                    "settlement_success_overlay_impression",
                    mapOf(
                        "group_id" to groupId,
                        "entry_point" to "settlement_preview",
                        "transfer_count" to celebration.successUi.transferCount,
                    ),
                )
                delay(1600)
                isSettlementCelebrationVisible = false
                delay(260)
                groupsMessage = "Settlement done. PDF saved in Recent Settlements."
                settlementCelebrationState = null
                isSettlementCelebrationVisible = false
                navController.popBackStack()
            }

            DisposableEffect(groupId) {
                onDispose {
                    settlementInterstitialAd = null
                    isSettlementAdLoading = false
                    settlementCelebrationState = null
                    isSettlementCelebrationVisible = false
                }
            }

            val settlementUpiSelection = pendingSettlementUpiSelection
            if (settlementUpiSelection != null) {
                UpiAppPickerDialog(
                    amountDisplay = settlementUpiSelection.transfer.amountDisplay,
                    receiverName = settlementUpiSelection.transfer.receiverName,
                    appOptions = settlementUpiSelection.appOptions,
                    onDismissRequest = { pendingSettlementUpiSelection = null },
                    onAppSelected = { selectedApp ->
                        val selection = settlementUpiSelection
                        pendingSettlementUpiSelection = null
                        runCatching {
                            pendingSettlementUpiLaunch = PendingUpiLaunchRecord(
                                transferKey = selection.transfer.transferKey,
                                attemptId = selection.attemptId,
                                selectedApp = selectedApp,
                                startedAtEpochMs = System.currentTimeMillis(),
                            )
                            settlementUpiLauncher.launch(
                                Intent(selection.intent).setPackage(selectedApp.packageName),
                            )
                        }.onFailure {
                            pendingSettlementUpiLaunch = null
                            context.showShortToast("Unable to open ${selectedApp.displayName} for this payment.")
                        }
                    },
                )
            }
            val settlementUpiResultResolution = pendingSettlementUpiResultResolution
            if (settlementUpiResultResolution != null) {
                UpiResultConfirmationDialog(
                    resolution = settlementUpiResultResolution,
                    onStatusConfirmed = { confirmedStatus ->
                        val resolution = settlementUpiResultResolution
                        pendingSettlementUpiResultResolution = null
                        recordSettlementUpiTransaction(
                            resolution.transaction.copy(
                                status = confirmedStatus,
                                statusConfirmedByUser = true,
                            ),
                        )
                    },
                )
            }

            SettlementPreviewScreen(
                summary = settlementSummaryUi,
                transfers = settlementTransferRows,
                trackedUpiResponses = trackedUpiResponseRows,
                isOwner = isOwner,
                hasExpenses = expenses.isNotEmpty(),
                isFinalizing = isFinalizingSettlement,
                successState = settlementCelebrationState?.successUi,
                isSuccessVisible = isSettlementCelebrationVisible,
                onConfirm = { beginSettlementFlow() },
                onTransferUpiPayClick = { transfer ->
                    if (transfer.transferKey in successfulSettlementKeys) {
                        context.showShortToast("A successful UPI payment is already recorded for this transfer.")
                        return@SettlementPreviewScreen
                    }
                    val attemptId = buildSettlementUpiAttemptId()
                    val baseIntent = buildSettlementUpiPaymentIntent(
                        upiId = transfer.receiverUpiId,
                        receiverName = transfer.receiverName,
                        amountPaise = transfer.amountPaise,
                        groupName = settlementSummaryUi.groupName,
                        attemptId = attemptId,
                    )
                    val launched = if (baseIntent == null) {
                        context.showShortToast("UPI payment is unavailable for this transfer.")
                        false
                    } else {
                        val availableApps = context.findAvailableUpiApps(baseIntent)
                        if (availableApps.isEmpty()) {
                            context.showShortToast("No UPI app is installed on this device.")
                            false
                        } else {
                            pendingSettlementUpiSelection = PendingUpiAppSelection(
                                transfer = transfer,
                                attemptId = attemptId,
                                intent = baseIntent,
                                appOptions = availableApps,
                            )
                            true
                        }
                    }
                    AppTelemetry.logEvent(
                        if (launched) "settlement_upi_launch_success" else "settlement_upi_launch_failure",
                        mapOf(
                            "group_id" to groupId,
                            "entry_point" to "settlement_preview",
                            "receiver_has_upi" to transfer.receiverUpiId.isNotBlank(),
                        ),
                    )
                },
                navigationAction = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                        )
                    }
                },
                footerContent = {
                    LetsGoDutchBannerAd(
                        productionAdUnitId = SETTLEMENT_PREVIEW_BANNER_AD_UNIT_ID,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                    )
                },
            )
        }
    }

        if (showCreateGroupDialog) {
            CreateGroupDialog(
                onDismiss = { showCreateGroupDialog = false },
                onCreate = { draft ->
                    val userId = currentUser?.userId.orEmpty()
                    if (userId.isBlank()) {
                        groupsMessage = "Please sign in again."
                    } else if (!realtimeDbConfigIssue.isNullOrBlank()) {
                        groupsMessage = realtimeDbConfigIssue
                    } else {
                        AppTelemetry.logEvent(
                            "group_create_attempt",
                            mapOf("origin" to createGroupDialogOrigin),
                        )
                        scope.launch {
                            val resultCreate = groupRepository.createGroup(
                                name = draft.name,
                                ownerUserId = userId,
                                description = draft.description,
                                autoRenewInvite = draft.autoRenewInvite,
                                selectAllMembersByDefaultForExpenses = draft.selectAllMembersByDefaultForExpenses,
                            )
                            if (resultCreate.isSuccess) {
                                groupsMessage = "Group created."
                                showCreateGroupDialog = false
                                AppTelemetry.logEvent(
                                    "group_create_success",
                                    mapOf(
                                        "group_id" to (resultCreate.getOrNull()?.groupId ?: "unknown"),
                                        "origin" to createGroupDialogOrigin,
                                    ),
                                )
                                registerHelpfulInteraction("group_create")
                                resultCreate.getOrNull()?.groupId?.let { groupId ->
                                    navigateToGroup(groupId = groupId, origin = "group_create_success")
                                }
                            } else {
                                groupsMessage = resultCreate.exceptionOrNull()
                                    ?.toActionableMessage("Unable to create group.")
                                    .orEmpty()
                                logFailureEvent(
                                    name = "group_create_failure",
                                    throwable = resultCreate.exceptionOrNull(),
                                    params = mapOf("origin" to createGroupDialogOrigin),
                                )
                                resultCreate.exceptionOrNull()?.let { error ->
                                    AppTelemetry.recordNonFatal(error, tags = mapOf("op" to "group_create"))
                                }
                            }
                        }
                    }
                },
            )
        }

        if (showJoinGroupDialog) {
            JoinGroupDialog(
                onDismiss = { showJoinGroupDialog = false },
                onJoin = { inviteCode ->
                    val normalizedInviteCode = inviteCode.toNormalizedInviteCode()
                    if (normalizedInviteCode.isBlank()) {
                        groupsMessage = "Enter a valid invite code."
                        return@JoinGroupDialog
                    }
                    val userId = currentUser?.userId.orEmpty()
                    if (userId.isBlank()) {
                        groupsMessage = "Please sign in again."
                    } else if (!realtimeDbConfigIssue.isNullOrBlank()) {
                        groupsMessage = realtimeDbConfigIssue
                    } else {
                        showJoinGroupDialog = false
                        scope.launch {
                            previewInviteJoin(
                                inviteCode = normalizedInviteCode,
                                source = joinGroupDialogOrigin,
                            )
                        }
                    }
                },
            )
        }

        val joinClaimRequest = pendingJoinClaimRequest
        if (joinClaimRequest != null) {
            ClaimExistingMemberDialog(
                groupName = joinClaimRequest.preview.group.name,
                claimableMembers = joinClaimRequest.preview.claimableMembers,
                onDismiss = {
                    pendingJoinClaimRequest = null
                    AppTelemetry.logEvent(
                        "group_join_claim_dismissed",
                        mapOf("via" to joinClaimRequest.source, "group_id" to joinClaimRequest.preview.group.groupId),
                    )
                },
                onJoin = { claimMemberUserId ->
                    pendingJoinClaimRequest = null
                    AppTelemetry.logEvent(
                        "group_join_claim_selected",
                        mapOf("via" to joinClaimRequest.source, "group_id" to joinClaimRequest.preview.group.groupId),
                    )
                    scope.launch {
                        completeInviteJoin(
                            inviteCode = joinClaimRequest.inviteCode,
                            source = joinClaimRequest.source,
                            claimMemberUserId = claimMemberUserId,
                        )
                    }
                },
            )
        }

        if (showAppTour && !currentUser?.userId.isNullOrBlank()) {
            AppTourOverlay(
                onDismiss = {
                    context.setAppTourCompleted(true)
                    isTourCompleted = true
                    showAppTour = false
                    AppTelemetry.logEvent("app_tour_completed")
                },
            )
        }

        if (showAppReviewPrompt) {
            AppReviewPromptDialog(
                isLaunching = isLaunchingReviewPrompt,
                onDismiss = {
                    showAppReviewPrompt = false
                    AppTelemetry.logEvent("app_review_dismissed")
                },
                onReviewClick = { launchReviewFlow() },
            )
        }

        activeSyncState?.let { syncState ->
            BackendSyncDialog(state = syncState)
        }
    }
}

private data class CreateGroupDraft(
    val name: String,
    val description: String,
    val autoRenewInvite: Boolean,
    val selectAllMembersByDefaultForExpenses: Boolean,
)

private data class BackendSyncUiState(
    val label: String,
    val title: String,
    val supportingText: String,
    val badgeText: String,
)

private data class RevampDialogPalette(
    val scrim: ComposeColor,
    val content: ComposeColor,
    val supportingContent: ComposeColor,
    val tertiaryContent: ComposeColor,
    val badgeSurface: ComposeColor,
    val cardSurface: ComposeColor,
    val border: ComposeColor,
    val strongBorder: ComposeColor,
    val dangerContent: ComposeColor,
    val dangerSurface: ComposeColor,
)

@Composable
private fun rememberRevampDialogPalette(): RevampDialogPalette {
    return remember {
        RevampDialogPalette(
            scrim = Night.copy(alpha = 0.70f),
            content = TextOnDark,
            supportingContent = TextOnDark.copy(alpha = 0.72f),
            tertiaryContent = TextOnDark.copy(alpha = 0.68f),
            badgeSurface = TextOnDark.copy(alpha = 0.10f),
            cardSurface = TextOnDark.copy(alpha = 0.05f),
            border = TextOnDark.copy(alpha = 0.10f),
            strongBorder = TextOnDark.copy(alpha = 0.18f),
            dangerContent = CoralSoft,
            dangerSurface = CoralSoft.copy(alpha = 0.18f),
        )
    }
}

@Composable
private fun BackendSyncDialog(
    state: BackendSyncUiState,
) {
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.screenWidthDp > configuration.screenHeightDp
    val availableHeight = (configuration.screenHeightDp.dp - 56.dp).coerceAtLeast(240.dp)
    val maxDialogHeight = minOf(if (isLandscape) 420.dp else 520.dp, availableHeight)
    val maxDialogWidth = if (isLandscape) 600.dp else 520.dp
    val dialogScrollState = rememberScrollState()
    val palette = rememberRevampDialogPalette()
    Dialog(
        onDismissRequest = {},
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = false,
            dismissOnClickOutside = false,
        ),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(palette.scrim)
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .imePadding()
                .padding(horizontal = 20.dp, vertical = 28.dp),
            contentAlignment = Alignment.Center,
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = maxDialogWidth)
                    .heightIn(max = maxDialogHeight),
                shape = RoundedCornerShape(28.dp),
                color = ComposeColor.Transparent,
                border = BorderStroke(1.dp, MintGlow),
                shadowElevation = 24.dp,
            ) {
                Column(
                    modifier = Modifier
                        .verticalScroll(dialogScrollState)
                        .background(
                            Brush.linearGradient(
                                listOf(Night, NightSoft, Charcoal),
                            ),
                        )
                        .padding(horizontal = 20.dp, vertical = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top,
                    ) {
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            SectionLabel(text = state.label)
                            Text(
                                text = state.title,
                                style = MaterialTheme.typography.headlineSmall,
                                color = palette.content,
                                fontWeight = FontWeight.SemiBold,
                            )
                            Text(
                                text = state.supportingText,
                                style = MaterialTheme.typography.bodyMedium,
                                color = palette.supportingContent,
                            )
                        }
                        LetsGoDutchDialogPill(
                            text = state.badgeText,
                            containerColor = MintGlow,
                            contentColor = MintGreen,
                        )
                    }

                    LetsGoDutchRevampCard {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            CircularProgressIndicator(
                                color = MintGreen,
                                trackColor = palette.border,
                            )
                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(4.dp),
                            ) {
                                Text(
                                    text = "Please wait while we sync the latest data.",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = palette.content,
                                )
                                Text(
                                    text = "This usually takes a few seconds.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = palette.tertiaryContent,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun rememberDialogSectionMaxHeight(
    portraitFraction: Float = 0.48f,
    landscapeFraction: Float = 0.34f,
    reservedHeight: Dp = 220.dp,
    minimumHeight: Dp = 140.dp,
): Dp {
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.screenWidthDp > configuration.screenHeightDp
    val fractionHeight = configuration.screenHeightDp.dp * if (isLandscape) landscapeFraction else portraitFraction
    val availableHeight = (configuration.screenHeightDp.dp - reservedHeight).coerceAtLeast(minimumHeight)
    return minOf(fractionHeight, availableHeight)
}

@Composable
private fun LetsGoDutchRevampDialog(
    label: String,
    title: String,
    supportingText: String,
    onDismissRequest: () -> Unit,
    badgeText: String? = null,
    icon: ImageVector? = null,
    content: @Composable androidx.compose.foundation.layout.ColumnScope.() -> Unit,
    actions: @Composable androidx.compose.foundation.layout.ColumnScope.() -> Unit,
) {
    val palette = rememberRevampDialogPalette()
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.screenWidthDp > configuration.screenHeightDp
    val maxDialogHeight = configuration.screenHeightDp.dp * if (isLandscape) 0.94f else 0.88f
    val maxDialogWidth = if (isLandscape) 720.dp else 560.dp
    val dialogScrollState = rememberScrollState()

    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(palette.scrim)
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .imePadding()
                .padding(horizontal = 20.dp, vertical = 28.dp),
            contentAlignment = Alignment.Center,
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = maxDialogWidth)
                    .heightIn(max = maxDialogHeight),
                shape = RoundedCornerShape(28.dp),
                color = ComposeColor.Transparent,
                border = BorderStroke(1.dp, MintGlow),
                shadowElevation = 24.dp,
            ) {
                Column(
                    modifier = Modifier
                        .background(
                            Brush.linearGradient(
                                listOf(Night, NightSoft, Charcoal),
                            ),
                        )
                        .verticalScroll(dialogScrollState)
                        .padding(horizontal = 20.dp, vertical = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(18.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top,
                    ) {
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            SectionLabel(text = label)
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                if (icon != null) {
                                    Surface(
                                        shape = RoundedCornerShape(16.dp),
                                        color = MintGlow,
                                        border = BorderStroke(1.dp, palette.border),
                                    ) {
                                        Icon(
                                            imageVector = icon,
                                            contentDescription = null,
                                            tint = MintGreen,
                                            modifier = Modifier.padding(10.dp),
                                        )
                                    }
                                }
                                Text(
                                    text = title,
                                    style = MaterialTheme.typography.headlineSmall,
                                    color = palette.content,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.weight(1f),
                                )
                            }
                            Text(
                                text = supportingText,
                                style = MaterialTheme.typography.bodyMedium,
                                color = palette.supportingContent,
                            )
                        }
                        if (!badgeText.isNullOrBlank()) {
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = palette.badgeSurface,
                                border = BorderStroke(1.dp, palette.border),
                            ) {
                                Text(
                                    text = badgeText,
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MintGreen,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                )
                            }
                        }
                    }

                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(14.dp),
                        content = content,
                    )

                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        content = actions,
                    )
                }
            }
        }
    }
}

@Composable
private fun LetsGoDutchRevampCard(
    modifier: Modifier = Modifier,
    content: @Composable androidx.compose.foundation.layout.ColumnScope.() -> Unit,
) {
    val palette = rememberRevampDialogPalette()
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = palette.cardSurface,
        border = BorderStroke(1.dp, palette.border),
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            content = content,
        )
    }
}

@Composable
private fun LetsGoDutchDialogLabelValueRow(
    label: String,
    value: String,
    valueColor: ComposeColor = TextOnDark,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = TextOnDark.copy(alpha = 0.62f),
            modifier = Modifier.weight(1f),
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = valueColor,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

@Composable
private fun LetsGoDutchDialogPill(
    text: String,
    containerColor: ComposeColor,
    contentColor: ComposeColor,
) {
    Surface(
        shape = RoundedCornerShape(999.dp),
        color = containerColor,
        border = BorderStroke(1.dp, contentColor.copy(alpha = 0.24f)),
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            color = contentColor,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
        )
    }
}

@Composable
private fun revampDialogTextFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedContainerColor = ComposeColor.Transparent,
    unfocusedContainerColor = ComposeColor.Transparent,
    focusedTextColor = TextOnDark,
    unfocusedTextColor = TextOnDark,
    focusedBorderColor = MintGreen,
    unfocusedBorderColor = TextOnDark.copy(alpha = 0.22f),
    cursorColor = MintGreen,
    focusedLabelColor = MintGreen,
    unfocusedLabelColor = TextOnDark.copy(alpha = 0.72f),
)

@Composable
private fun LetsGoDutchDialogSettingRow(
    checked: Boolean,
    title: String,
    supportingText: String,
    onCheckedChange: (Boolean) -> Unit,
) {
    val palette = rememberRevampDialogPalette()
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) },
        shape = RoundedCornerShape(20.dp),
        color = ComposeColor.Transparent,
        border = BorderStroke(
            1.dp,
            if (checked) MintGlow.copy(alpha = 0.72f) else palette.border.copy(alpha = 0.80f),
        ),
        shadowElevation = 0.dp,
    ) {
        Row(
            modifier = Modifier
                .background(
                    brush = Brush.linearGradient(
                        if (checked) {
                            listOf(
                                Night.copy(alpha = 0.94f),
                                NightSoft.copy(alpha = 0.98f),
                                Charcoal.copy(alpha = 0.94f),
                            )
                        } else {
                            listOf(
                                Night.copy(alpha = 0.88f),
                                Charcoal.copy(alpha = 0.92f),
                                Night.copy(alpha = 0.86f),
                            )
                        },
                    ),
                    shape = RoundedCornerShape(20.dp),
                )
                .padding(horizontal = 12.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    color = palette.content,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = supportingText,
                    style = MaterialTheme.typography.bodySmall,
                    color = palette.supportingContent,
                )
                LetsGoDutchDialogPill(
                    text = if (checked) "Enabled" else "Disabled",
                    containerColor = if (checked) {
                        MintGreen.copy(alpha = 0.16f)
                    } else {
                        palette.badgeSurface
                    },
                    contentColor = if (checked) MintGreen else palette.tertiaryContent,
                )
            }
            LetsGoDutchDialogToggle(
                checked = checked,
                palette = palette,
            )
        }
    }
}

@Composable
private fun LetsGoDutchDialogToggle(
    checked: Boolean,
    palette: RevampDialogPalette,
) {
    Surface(
        shape = RoundedCornerShape(999.dp),
        color = ComposeColor.Transparent,
        border = BorderStroke(
            1.dp,
            if (checked) MintGlow.copy(alpha = 0.72f) else palette.strongBorder.copy(alpha = 0.80f),
        ),
    ) {
        Row(
            modifier = Modifier
                .background(
                    if (checked) {
                        Brush.horizontalGradient(
                            listOf(
                                MintGreen.copy(alpha = 0.88f),
                                MintTeal.copy(alpha = 0.88f),
                            ),
                        )
                    } else {
                        Brush.horizontalGradient(
                            listOf(
                                Night.copy(alpha = 0.92f),
                                Charcoal.copy(alpha = 0.96f),
                            ),
                        )
                    },
                )
                .padding(horizontal = 4.dp, vertical = 4.dp)
                .widthIn(min = 52.dp),
            horizontalArrangement = if (checked) Arrangement.End else Arrangement.Start,
        ) {
            Surface(
                modifier = Modifier.size(22.dp),
                shape = CircleShape,
                color = if (checked) Night else TextOnDark,
                border = BorderStroke(1.dp, ComposeColor.White.copy(alpha = 0.16f)),
                shadowElevation = 0.dp,
            ) {}
        }
    }
}

@Composable
private fun CreateGroupDialog(
    onDismiss: () -> Unit,
    onCreate: (CreateGroupDraft) -> Unit,
) {
    val isLandscape = LocalConfiguration.current.screenWidthDp > LocalConfiguration.current.screenHeightDp
    val contentMaxHeight = rememberDialogSectionMaxHeight(
        portraitFraction = 0.50f,
        landscapeFraction = 0.34f,
    )
    var groupName by rememberSaveable { mutableStateOf("") }
    var groupDescription by rememberSaveable { mutableStateOf("") }
    var autoRenewInvite by rememberSaveable { mutableStateOf(true) }
    var selectAllMembersByDefaultForExpenses by rememberSaveable { mutableStateOf(false) }
    val normalizedName = groupName.trim()

    LetsGoDutchRevampDialog(
        label = "Groups",
        title = "Create Group",
        supportingText = "Set up a shared space for a trip, flat, or event. You can adjust invite and expense defaults now or later.",
        onDismissRequest = onDismiss,
        badgeText = "New",
        icon = Icons.Default.Add,
        content = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = contentMaxHeight)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                LetsGoDutchRevampCard {
                    SectionLabel(text = "Basics")
                    Text(
                        text = "Choose a clear name so members can recognize this group in invites, balances, and settlements.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextOnDark.copy(alpha = 0.72f),
                    )
                    OutlinedTextField(
                        value = groupName,
                        onValueChange = { groupName = it },
                        label = { Text("Group name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = revampDialogTextFieldColors(),
                    )
                    OutlinedTextField(
                        value = groupDescription,
                        onValueChange = { groupDescription = it },
                        label = { Text("Group description") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3,
                        maxLines = 4,
                        colors = revampDialogTextFieldColors(),
                    )
                }

                LetsGoDutchRevampCard {
                    SectionLabel(text = "Invite Access")
                    LetsGoDutchDialogSettingRow(
                        checked = autoRenewInvite,
                        title = "Auto-renew invite code",
                        supportingText = "Keeps a usable code ready after the current invite expires.",
                        onCheckedChange = { autoRenewInvite = it },
                    )
                }

                LetsGoDutchRevampCard {
                    SectionLabel(text = "Expense Defaults")
                    LetsGoDutchDialogSettingRow(
                        checked = selectAllMembersByDefaultForExpenses,
                        title = "Select all members by default",
                        supportingText = "New expenses start with the whole group selected as participants.",
                        onCheckedChange = { selectAllMembersByDefaultForExpenses = it },
                    )
                    Text(
                        text = "Leave this off if you want new expenses to start with no participants selected.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextOnDark.copy(alpha = 0.68f),
                    )
                }
            }
        },
        actions = {
            if (isLandscape) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    GradientButton(
                        text = "Create",
                        onClick = {
                            if (normalizedName.isNotBlank()) {
                                onCreate(
                                    CreateGroupDraft(
                                        name = normalizedName,
                                        description = groupDescription.trim(),
                                        autoRenewInvite = autoRenewInvite,
                                        selectAllMembersByDefaultForExpenses = selectAllMembersByDefaultForExpenses,
                                    ),
                                )
                                groupName = ""
                                groupDescription = ""
                                autoRenewInvite = true
                                selectAllMembersByDefaultForExpenses = false
                            }
                        },
                        enabled = normalizedName.isNotBlank(),
                        modifier = Modifier.fillMaxWidth(),
                    )
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 52.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = TextOnDark,
                        ),
                        border = BorderStroke(1.dp, TextOnDark.copy(alpha = 0.18f)),
                    ) {
                        Text("Cancel")
                    }
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .weight(1f)
                            .heightIn(min = 52.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = TextOnDark,
                        ),
                        border = BorderStroke(1.dp, TextOnDark.copy(alpha = 0.18f)),
                    ) {
                        Text("Cancel")
                    }
                    GradientButton(
                        text = "Create",
                        onClick = {
                            if (normalizedName.isNotBlank()) {
                                onCreate(
                                    CreateGroupDraft(
                                        name = normalizedName,
                                        description = groupDescription.trim(),
                                        autoRenewInvite = autoRenewInvite,
                                        selectAllMembersByDefaultForExpenses = selectAllMembersByDefaultForExpenses,
                                    ),
                                )
                                groupName = ""
                                groupDescription = ""
                                autoRenewInvite = true
                                selectAllMembersByDefaultForExpenses = false
                            }
                        },
                        enabled = normalizedName.isNotBlank(),
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        },
    )
}

@Composable
private fun GroupDetailsDialog(
    group: Group,
    isOwner: Boolean,
    onDismiss: () -> Unit,
    onCopyInvite: () -> Unit,
    onSave: (description: String, autoRenewInvite: Boolean, selectAllMembersByDefaultForExpenses: Boolean) -> Unit,
    onRenewInvite: () -> Unit,
) {
    val contentMaxHeight = rememberDialogSectionMaxHeight(
        portraitFraction = 0.50f,
        landscapeFraction = 0.34f,
    )
    var description by rememberSaveable(group.groupId, group.description) { mutableStateOf(group.description) }
    var autoRenewInvite by rememberSaveable(group.groupId, group.autoRenewInvite) {
        mutableStateOf(group.autoRenewInvite)
    }
    var selectAllMembersByDefaultForExpenses by rememberSaveable(
        group.groupId,
        group.selectAllMembersByDefaultForExpenses,
    ) {
        mutableStateOf(group.selectAllMembersByDefaultForExpenses)
    }
    val palette = rememberRevampDialogPalette()
    val inviteExpired = group.inviteExpiryEpochMs < System.currentTimeMillis()
    val inviteStatus = when {
        inviteExpired -> "Expired"
        group.autoRenewInvite -> "Auto-renews"
        else -> "Active"
    }

    LetsGoDutchRevampDialog(
        label = "Group Details",
        title = group.name,
        supportingText = if (isOwner) {
            "Update the description, invite behavior, and expense defaults for this group."
        } else {
            "Review the invite, description, and expense defaults configured for this group."
        },
        onDismissRequest = onDismiss,
        badgeText = if (isOwner) "Owner" else "Read only",
        icon = Icons.Default.Info,
        content = {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = contentMaxHeight),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                item {
                    LetsGoDutchRevampCard {
                        SectionLabel(text = "Invite Access")
                        LetsGoDutchDialogLabelValueRow(label = "Invite code", value = group.inviteCode)
                        LetsGoDutchDialogLabelValueRow(
                            label = "Expires",
                            value = group.inviteExpiryEpochMs.toGroupDetailsDateTime(),
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            LetsGoDutchDialogPill(
                                text = inviteStatus,
                                containerColor = if (inviteExpired) {
                                    palette.dangerSurface
                                } else {
                                    palette.badgeSurface
                                },
                                contentColor = if (inviteExpired) {
                                    palette.dangerContent
                                } else {
                                    MintGreen
                                },
                            )
                            Text(
                                text = if (group.autoRenewInvite && !inviteExpired) {
                                    "A fresh code will stay available after the current one expires."
                                } else {
                                    "Owners can renew the current code when needed."
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = palette.supportingContent,
                                modifier = Modifier.weight(1f),
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            TextButton(onClick = onCopyInvite) {
                                Text("Copy invite code")
                            }
                            if (isOwner) {
                                TextButton(onClick = onRenewInvite) {
                                    Text("Renew invite now")
                                }
                            }
                        }
                    }
                }

                item {
                    LetsGoDutchRevampCard {
                        SectionLabel(text = "Group Description")
                        if (isOwner) {
                            OutlinedTextField(
                                value = description,
                                onValueChange = { description = it },
                                label = { Text("Description") },
                                modifier = Modifier.fillMaxWidth(),
                                minLines = 3,
                                maxLines = 4,
                                colors = revampDialogTextFieldColors(),
                            )
                        } else {
                            Text(
                                text = group.description.ifBlank { "No description added yet." },
                                style = MaterialTheme.typography.bodyLarge,
                                color = palette.content,
                                fontWeight = FontWeight.Medium,
                            )
                        }
                    }
                }

                item {
                    LetsGoDutchRevampCard {
                        SectionLabel(text = if (isOwner) "Expense Defaults" else "Current Defaults")
                        if (isOwner) {
                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                LetsGoDutchDialogSettingRow(
                                    checked = autoRenewInvite,
                                    title = "Auto-renew invite",
                                    supportingText = "Keeps a usable join code available after the current one expires.",
                                    onCheckedChange = { autoRenewInvite = it },
                                )
                                LetsGoDutchDialogSettingRow(
                                    checked = selectAllMembersByDefaultForExpenses,
                                    title = "Select all members by default",
                                    supportingText = "New expenses start with the whole group selected as participants.",
                                    onCheckedChange = { selectAllMembersByDefaultForExpenses = it },
                                )
                            }
                        } else {
                            LetsGoDutchDialogLabelValueRow(
                                label = "Invite auto-renew",
                                value = if (group.autoRenewInvite) "On" else "Off",
                            )
                            LetsGoDutchDialogLabelValueRow(
                                label = "New expense participants",
                                value = if (group.selectAllMembersByDefaultForExpenses) {
                                    "All members selected"
                                } else {
                                    "Start unselected"
                                },
                            )
                        }
                    }
                }
            }
        },
        actions = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.align(Alignment.End),
            ) {
                Text(if (isOwner) "Cancel" else "Close")
            }
            GradientButton(
                text = if (isOwner) "Save Details" else "Done",
                onClick = {
                    if (isOwner) {
                        onSave(
                            description.trim(),
                            autoRenewInvite,
                            selectAllMembersByDefaultForExpenses,
                        )
                    } else {
                        onDismiss()
                    }
                },
                modifier = Modifier.fillMaxWidth(),
            )
        },
    )
}

@Composable
private fun JoinGroupDialog(
    onDismiss: () -> Unit,
    onJoin: (inviteCode: String) -> Unit,
) {
    val isLandscape = LocalConfiguration.current.screenWidthDp > LocalConfiguration.current.screenHeightDp
    val contentMaxHeight = rememberDialogSectionMaxHeight(
        portraitFraction = 0.42f,
        landscapeFraction = 0.30f,
    )
    var inviteCode by rememberSaveable { mutableStateOf("") }
    val normalizedInviteCode = inviteCode.toNormalizedInviteCode()

    LetsGoDutchRevampDialog(
        label = "Groups",
        title = "Join Group",
        supportingText = "Enter the invite code shared by an owner. We will validate it and show any claim options before adding you.",
        onDismissRequest = onDismiss,
        badgeText = if (normalizedInviteCode.isBlank()) "Invite" else "Ready",
        icon = Icons.Default.PersonAddAlt1,
        content = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = contentMaxHeight)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                LetsGoDutchRevampCard {
                    SectionLabel(text = "Invite Code")
                    Text(
                        text = "Codes are case-insensitive. Spaces and separators are ignored automatically.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextOnDark.copy(alpha = 0.72f),
                    )
                    OutlinedTextField(
                        value = inviteCode,
                        onValueChange = { inviteCode = it.toNormalizedInviteCode() },
                        label = { Text("Invite code") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = revampDialogTextFieldColors(),
                    )
                    LetsGoDutchDialogLabelValueRow(
                        label = "Entered code",
                        value = normalizedInviteCode.ifBlank { "Waiting for input" },
                        valueColor = if (normalizedInviteCode.isBlank()) {
                            TextOnDark.copy(alpha = 0.52f)
                        } else {
                            MintGreen
                        },
                    )
                }

                LetsGoDutchRevampCard {
                    SectionLabel(text = "What Happens Next")
                    Text(
                        text = "If the owner already created a placeholder for you, the next step will let you claim it instead of joining as a duplicate member.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextOnDark.copy(alpha = 0.72f),
                    )
                }
            }
        },
        actions = {
            if (isLandscape) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    GradientButton(
                        text = "Join",
                        onClick = {
                            if (normalizedInviteCode.isNotBlank()) {
                                onJoin(normalizedInviteCode)
                                inviteCode = ""
                            }
                        },
                        enabled = normalizedInviteCode.isNotBlank(),
                        modifier = Modifier.fillMaxWidth(),
                    )
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 52.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = TextOnDark,
                        ),
                        border = BorderStroke(1.dp, TextOnDark.copy(alpha = 0.18f)),
                    ) {
                        Text("Cancel")
                    }
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .weight(1f)
                            .heightIn(min = 52.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = TextOnDark,
                        ),
                        border = BorderStroke(1.dp, TextOnDark.copy(alpha = 0.18f)),
                    ) {
                        Text("Cancel")
                    }
                    GradientButton(
                        text = "Join",
                        onClick = {
                            if (normalizedInviteCode.isNotBlank()) {
                                onJoin(normalizedInviteCode)
                                inviteCode = ""
                            }
                        },
                        enabled = normalizedInviteCode.isNotBlank(),
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        },
    )
}

@Composable
private fun ClaimExistingMemberDialog(
    groupName: String,
    claimableMembers: List<Member>,
    onDismiss: () -> Unit,
    onJoin: (claimMemberUserId: String?) -> Unit,
) {
    var selectedClaimMemberUserId by rememberSaveable(groupName) { mutableStateOf<String?>(null) }
    val listMaxHeight = rememberDialogSectionMaxHeight(
        portraitFraction = 0.36f,
        landscapeFraction = 0.28f,
        reservedHeight = 260.dp,
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Join $groupName") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    "The owner already added some placeholder names for this group. " +
                        "Claim one to merge your account with it, or continue as a new member.",
                )
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = listMaxHeight),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    item {
                        ClaimableMemberRow(
                            title = "Join as a new member",
                            supportingText = "Do not merge with an existing placeholder.",
                            selected = selectedClaimMemberUserId == null,
                            onClick = { selectedClaimMemberUserId = null },
                        )
                    }
                    items(claimableMembers, key = { it.userId }) { member ->
                        ClaimableMemberRow(
                            title = member.toFriendlyDisplayName(),
                            supportingText = if (member.role == Role.OWNER) {
                                "Placeholder owner added before the person joined."
                            } else {
                                "Placeholder member added by an owner."
                            },
                            selected = selectedClaimMemberUserId == member.userId,
                            onClick = { selectedClaimMemberUserId = member.userId },
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = { onJoin(selectedClaimMemberUserId) }) {
                Text(
                    if (selectedClaimMemberUserId.isNullOrBlank()) {
                        "Join as New"
                    } else {
                        "Claim & Join"
                    },
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
    )
}

@Composable
private fun ClaimableMemberRow(
    title: String,
    supportingText: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        RadioButton(
            selected = selected,
            onClick = onClick,
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
            )
            Text(
                text = supportingText,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun AddMemberDialog(
    onDismiss: () -> Unit,
    onAdd: (memberName: String) -> Unit,
) {
    var memberName by rememberSaveable { mutableStateOf("") }
    val normalizedName = memberName.trim()

    LetsGoDutchRevampDialog(
        label = "Members",
        title = "Add a Member",
        supportingText = "Add someone to the group now. They can still claim or rename this slot later if needed.",
        onDismissRequest = onDismiss,
        badgeText = "New",
        icon = Icons.Default.PersonAddAlt1,
        content = {
            LetsGoDutchRevampCard {
                SectionLabel(text = "Member Name")
                Text(
                    text = "This name appears in expenses, balances, and settlements until the person joins the app or you rename the entry.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextOnDark.copy(alpha = 0.72f),
                )
                OutlinedTextField(
                    value = memberName,
                    onValueChange = { memberName = it },
                    label = { Text("Member name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = revampDialogTextFieldColors(),
                )
            }
        },
        actions = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = 52.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = TextOnDark,
                    ),
                    border = BorderStroke(1.dp, TextOnDark.copy(alpha = 0.18f)),
                ) {
                    Text("Cancel")
                }
                GradientButton(
                    text = "Add",
                    onClick = { onAdd(normalizedName) },
                    enabled = normalizedName.isNotBlank(),
                    modifier = Modifier.weight(1f),
                )
            }
        },
    )
}

@Composable
private fun EditMemberDialog(
    initialName: String,
    onDismiss: () -> Unit,
    onSave: (memberName: String) -> Unit,
) {
    var memberName by rememberSaveable(initialName) { mutableStateOf(initialName) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Member") },
        text = {
            OutlinedTextField(
                value = memberName,
                onValueChange = { memberName = it },
                label = { Text("Member name") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
        },
        confirmButton = {
            Button(
                onClick = {
                    val name = memberName.trim()
                    if (name.isNotBlank()) {
                        onSave(name)
                    }
                },
                enabled = memberName.trim().isNotBlank(),
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
    )
}

@Composable
private fun ManageMembersDialog(
    members: List<Member>,
    mainOwnerUserId: String,
    onDismiss: () -> Unit,
    onEditClick: (Member) -> Unit,
    onToggleOwnerClick: (member: Member, makeOwner: Boolean) -> Unit,
    onRemoveClick: (Member) -> Unit,
) {
    val palette = rememberRevampDialogPalette()
    val listMaxHeight = rememberDialogSectionMaxHeight(
        portraitFraction = 0.42f,
        landscapeFraction = 0.30f,
    )
    val manageableMembers = members
        .filter { member -> member.active && member.userId != mainOwnerUserId }
        .sortedWith(compareByDescending<Member> { it.role == Role.OWNER }.thenBy { it.joinedAtEpochMs })
    val ownerCount = manageableMembers.count { it.role == Role.OWNER }
    val manualCount = manageableMembers.count { isManualMemberUserId(it.userId) }

    LetsGoDutchRevampDialog(
        label = "Members",
        title = "Manage Members",
        supportingText = "Adjust roles, rename manual placeholders, and remove stand-ins cleanly.",
        onDismissRequest = onDismiss,
        badgeText = "${manageableMembers.size} active",
        icon = Icons.Default.ManageAccounts,
        content = {
            LetsGoDutchRevampCard {
                SectionLabel(text = "Quick Summary")
                LetsGoDutchDialogLabelValueRow(
                    label = "Owners in this list",
                    value = ownerCount.toString(),
                )
                LetsGoDutchDialogLabelValueRow(
                    label = "Manual placeholders",
                    value = manualCount.toString(),
                )
            }

            if (manageableMembers.isEmpty()) {
                LetsGoDutchRevampCard {
                    Text(
                        text = "No manageable members are available in this group right now.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = palette.supportingContent,
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = listMaxHeight),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    items(manageableMembers, key = { it.userId }) { member ->
                        LetsGoDutchRevampCard {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = member.toFriendlyDisplayName(),
                                        style = MaterialTheme.typography.titleMedium,
                                        color = palette.content,
                                        fontWeight = FontWeight.SemiBold,
                                    )
                                    Text(
                                        text = if (isManualMemberUserId(member.userId)) {
                                            "Manual placeholder"
                                        } else {
                                            "Linked app member"
                                        },
                                        style = MaterialTheme.typography.bodySmall,
                                        color = palette.supportingContent,
                                    )
                                }
                                LetsGoDutchDialogPill(
                                    text = if (member.role == Role.OWNER) "Owner" else "Member",
                                    containerColor = palette.badgeSurface,
                                    contentColor = if (member.role == Role.OWNER) MintGreen else palette.content,
                                )
                            }

                            if (isManualMemberUserId(member.userId)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End,
                                ) {
                                    TextButton(onClick = { onEditClick(member) }) {
                                        Text("Edit")
                                    }
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End,
                            ) {
                                TextButton(
                                    onClick = { onToggleOwnerClick(member, member.role != Role.OWNER) },
                                ) {
                                    if (member.role != Role.OWNER) {
                                        Icon(
                                            imageVector = Icons.Default.WorkspacePremium,
                                            contentDescription = null,
                                            tint = MintGreen,
                                            modifier = Modifier.padding(end = 6.dp),
                                        )
                                    }
                                    Text(if (member.role == Role.OWNER) "Make Member" else "Make Owner")
                                }
                                TextButton(onClick = { onRemoveClick(member) }) {
                                    Text("Remove")
                                }
                            }
                        }
                    }
                }
            }
        },
        actions = {
            GradientButton(
                text = "Done",
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth(),
            )
        },
    )
}

@Composable
private fun MembersListDialog(
    members: List<Member>,
    mainOwnerUserId: String,
    onDismiss: () -> Unit,
) {
    val palette = rememberRevampDialogPalette()
    val listMaxHeight = rememberDialogSectionMaxHeight(
        portraitFraction = 0.42f,
        landscapeFraction = 0.30f,
    )
    val activeMembers = members
        .filter { it.active }
        .sortedWith(
            compareByDescending<Member> { it.userId == mainOwnerUserId }
                .thenByDescending { it.role == Role.OWNER }
                .thenBy { it.joinedAtEpochMs },
        )
    val ownerCount = activeMembers.count { it.role == Role.OWNER || it.userId == mainOwnerUserId }

    LetsGoDutchRevampDialog(
        label = "Members",
        title = "Group Members",
        supportingText = "Everyone currently active in this group is listed here with their present role.",
        onDismissRequest = onDismiss,
        badgeText = "${activeMembers.size} total",
        icon = Icons.Default.ManageAccounts,
        content = {
            LetsGoDutchRevampCard {
                SectionLabel(text = "Quick Summary")
                LetsGoDutchDialogLabelValueRow(
                    label = "Active members",
                    value = activeMembers.size.toString(),
                )
                LetsGoDutchDialogLabelValueRow(
                    label = "Owners",
                    value = ownerCount.toString(),
                )
            }

            if (activeMembers.isEmpty()) {
                LetsGoDutchRevampCard {
                    Text(
                        text = "No active members are available in this group.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = palette.supportingContent,
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = listMaxHeight),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    items(activeMembers, key = { it.userId }) { member ->
                        val roleLabel = when {
                            member.userId == mainOwnerUserId -> "Main Owner"
                            member.role == Role.OWNER -> "Owner"
                            else -> "Member"
                        }
                        LetsGoDutchRevampCard {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = member.toFriendlyDisplayName(),
                                        style = MaterialTheme.typography.titleMedium,
                                        color = palette.content,
                                        fontWeight = FontWeight.SemiBold,
                                    )
                                    Text(
                                        text = if (isManualMemberUserId(member.userId)) {
                                            "Manual placeholder"
                                        } else {
                                            "Linked app member"
                                        },
                                        style = MaterialTheme.typography.bodySmall,
                                        color = palette.supportingContent,
                                    )
                                }
                                LetsGoDutchDialogPill(
                                    text = roleLabel,
                                    containerColor = palette.badgeSurface,
                                    contentColor = if (roleLabel.contains("Owner")) MintGreen else palette.content,
                                )
                            }
                        }
                    }
                }
            }
        },
        actions = {
            GradientButton(
                text = "Done",
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth(),
            )
        },
    )
}

@Composable
private fun RecentSettlementsDialog(
    groupName: String,
    settlements: List<RecentSettlementRecord>,
    onDismiss: () -> Unit,
    onOpenPdf: (RecentSettlementRecord) -> Unit,
    onSharePdf: (RecentSettlementRecord) -> Unit,
    onDeleteEntry: (RecentSettlementRecord) -> Unit,
) {
    val palette = rememberRevampDialogPalette()
    val isLandscape = LocalConfiguration.current.screenWidthDp > LocalConfiguration.current.screenHeightDp
    val listMaxHeight = rememberDialogSectionMaxHeight(
        portraitFraction = 0.44f,
        landscapeFraction = 0.30f,
    )
    LetsGoDutchRevampDialog(
        label = "Settlements",
        title = "Recent Settlements",
        supportingText = "Saved settlement reports for $groupName on this device.",
        onDismissRequest = onDismiss,
        badgeText = if (settlements.isEmpty()) null else "${settlements.size} saved",
        icon = Icons.Default.History,
        content = {
            if (settlements.isEmpty()) {
                LetsGoDutchRevampCard {
                    Text(
                        text = "No recent settlement PDFs are saved for this group on this device yet.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = palette.supportingContent,
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = listMaxHeight),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    items(settlements, key = { it.entryId }) { settlement ->
                        LetsGoDutchRevampCard {
                            SectionLabel(text = settlement.settledAtEpochMs.toGroupDetailsDateTime())
                            Text(
                                text = settlement.pdfFileName,
                                style = MaterialTheme.typography.titleMedium,
                                color = palette.content,
                                fontWeight = FontWeight.SemiBold,
                            )
                            Text(
                                text = if (settlement.pdfExists) {
                                    "Saved locally on this device."
                                } else {
                                    "PDF file is missing from this device."
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = if (settlement.pdfExists) {
                                    palette.supportingContent
                                } else {
                                    palette.dangerContent
                                },
                            )
                            if (isLandscape) {
                                Column(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalAlignment = Alignment.End,
                                ) {
                                    TextButton(
                                        onClick = { onOpenPdf(settlement) },
                                        enabled = settlement.pdfExists,
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.PictureAsPdf,
                                            contentDescription = null,
                                            tint = MintGreen,
                                            modifier = Modifier.padding(end = 6.dp),
                                        )
                                        Text("Open")
                                    }
                                    TextButton(
                                        onClick = { onSharePdf(settlement) },
                                        enabled = settlement.pdfExists,
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Share,
                                            contentDescription = null,
                                            tint = palette.content,
                                            modifier = Modifier.padding(end = 6.dp),
                                        )
                                        Text("Share")
                                    }
                                    TextButton(onClick = { onDeleteEntry(settlement) }) {
                                        Icon(
                                            imageVector = Icons.Default.DeleteOutline,
                                            contentDescription = null,
                                            tint = palette.dangerContent,
                                            modifier = Modifier.padding(end = 6.dp),
                                        )
                                        Text("Delete")
                                    }
                                }
                            } else {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End,
                                ) {
                                    TextButton(
                                        onClick = { onOpenPdf(settlement) },
                                        enabled = settlement.pdfExists,
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.PictureAsPdf,
                                            contentDescription = null,
                                            tint = MintGreen,
                                            modifier = Modifier.padding(end = 6.dp),
                                        )
                                        Text("Open")
                                    }
                                    TextButton(
                                        onClick = { onSharePdf(settlement) },
                                        enabled = settlement.pdfExists,
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Share,
                                            contentDescription = null,
                                            tint = palette.content,
                                            modifier = Modifier.padding(end = 6.dp),
                                        )
                                        Text("Share")
                                    }
                                    TextButton(onClick = { onDeleteEntry(settlement) }) {
                                        Icon(
                                            imageVector = Icons.Default.DeleteOutline,
                                            contentDescription = null,
                                            tint = palette.dangerContent,
                                            modifier = Modifier.padding(end = 6.dp),
                                        )
                                        Text("Delete")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        actions = {
            GradientButton(
                text = "Done",
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth(),
            )
        },
    )
}

@Composable
private fun AppReviewPromptDialog(
    isLaunching: Boolean,
    onDismiss: () -> Unit,
    onReviewClick: () -> Unit,
) {
    val palette = rememberRevampDialogPalette()
    LetsGoDutchRevampDialog(
        label = "Support",
        title = "Enjoying Let's Go Dutch?",
        supportingText = "If the app is helping, please rate it and share a short review on Google Play so we can keep improving it.",
        onDismissRequest = onDismiss,
        badgeText = "Quick favor",
        icon = Icons.Default.StarRate,
        content = {
            LetsGoDutchRevampCard {
                SectionLabel(text = "Why this helps")
                Text(
                    text = "Ratings and reviews help other people discover the app and help us prioritize the fixes and improvements that matter most.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = palette.supportingContent,
                )
            }
        },
        actions = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = 52.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = palette.content,
                    ),
                    border = BorderStroke(1.dp, palette.strongBorder),
                ) {
                    Text("Later")
                }
                GradientButton(
                    text = if (isLaunching) "Opening..." else "Rate the App",
                    onClick = onReviewClick,
                    enabled = !isLaunching,
                    modifier = Modifier.weight(1f),
                    leadingContent = {
                        Icon(
                            imageVector = Icons.Default.StarRate,
                            contentDescription = null,
                            tint = palette.content,
                        )
                    },
                )
            }
        },
    )
}

@Composable
private fun GroupOverflowMenu(
    isOwner: Boolean,
    canAddMember: Boolean,
    canViewMembers: Boolean,
    canViewRecentSettlements: Boolean,
    hasExpenses: Boolean,
    onMarkAsSettledClick: () -> Unit,
    onAddMemberClick: () -> Unit,
    onManageMembersClick: () -> Unit,
    onGroupDetailsClick: () -> Unit,
    onViewMembersClick: () -> Unit,
    onRecentSettlementsClick: () -> Unit,
    onDeleteGroupClick: () -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }

    IconButton(onClick = { expanded = true }) {
        Icon(
            imageVector = Icons.Default.MoreVert,
            contentDescription = "Group actions",
        )
    }
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = { expanded = false },
        shape = RoundedCornerShape(20.dp),
        containerColor = NightSoft,
        border = BorderStroke(1.dp, MintGlow),
        shadowElevation = 16.dp,
    ) {
        DropdownMenuItem(
            text = { Text("Settle Group") },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.TaskAlt,
                    contentDescription = null,
                )
            },
            enabled = isOwner && hasExpenses,
            onClick = {
                expanded = false
                onMarkAsSettledClick()
            },
            colors = MenuDefaults.itemColors(
                textColor = TextOnDark,
                leadingIconColor = MintGreen,
                disabledTextColor = TextOnDark.copy(alpha = 0.40f),
                disabledLeadingIconColor = TextOnDark.copy(alpha = 0.30f),
            ),
        )
        DropdownMenuItem(
            text = { Text("Add Member") },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.PersonAddAlt1,
                    contentDescription = null,
                )
            },
            enabled = canAddMember,
            onClick = {
                expanded = false
                onAddMemberClick()
            },
            colors = MenuDefaults.itemColors(
                textColor = TextOnDark,
                leadingIconColor = MintGreen,
                disabledTextColor = TextOnDark.copy(alpha = 0.40f),
                disabledLeadingIconColor = TextOnDark.copy(alpha = 0.30f),
            ),
        )
        DropdownMenuItem(
            text = { Text("Manage Members") },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.ManageAccounts,
                    contentDescription = null,
                )
            },
            enabled = isOwner,
            onClick = {
                expanded = false
                onManageMembersClick()
            },
            colors = MenuDefaults.itemColors(
                textColor = TextOnDark,
                leadingIconColor = MintGreen,
                disabledTextColor = TextOnDark.copy(alpha = 0.40f),
                disabledLeadingIconColor = TextOnDark.copy(alpha = 0.30f),
            ),
        )
        DropdownMenuItem(
            text = { Text("Group Details") },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                )
            },
            enabled = canViewMembers,
            onClick = {
                expanded = false
                onGroupDetailsClick()
            },
            colors = MenuDefaults.itemColors(
                textColor = TextOnDark,
                leadingIconColor = MintGreen,
                disabledTextColor = TextOnDark.copy(alpha = 0.40f),
                disabledLeadingIconColor = TextOnDark.copy(alpha = 0.30f),
            ),
        )
        DropdownMenuItem(
            text = { Text("List Members") },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.ManageAccounts,
                    contentDescription = null,
                )
            },
            enabled = canViewMembers,
            onClick = {
                expanded = false
                onViewMembersClick()
            },
            colors = MenuDefaults.itemColors(
                textColor = TextOnDark,
                leadingIconColor = MintGreen,
                disabledTextColor = TextOnDark.copy(alpha = 0.40f),
                disabledLeadingIconColor = TextOnDark.copy(alpha = 0.30f),
            ),
        )
        if (canViewRecentSettlements) {
            DropdownMenuItem(
                text = { Text("Recent Settlements") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.History,
                        contentDescription = null,
                    )
                },
                onClick = {
                    expanded = false
                    onRecentSettlementsClick()
                },
                colors = MenuDefaults.itemColors(
                    textColor = TextOnDark,
                    leadingIconColor = MintGreen,
                ),
            )
        }
        DropdownMenuItem(
            text = { Text("Delete Group") },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.DeleteOutline,
                    contentDescription = null,
                )
            },
            enabled = isOwner,
            onClick = {
                expanded = false
                onDeleteGroupClick()
            },
            colors = MenuDefaults.itemColors(
                textColor = TextOnDark,
                leadingIconColor = CoralSoft,
                disabledTextColor = TextOnDark.copy(alpha = 0.40f),
                disabledLeadingIconColor = TextOnDark.copy(alpha = 0.30f),
            ),
        )
    }
}

private data class RepositoryBundle(
    val authRepository: AuthRepository,
    val groupRepository: GroupRepository,
    val expenseRepository: ExpenseRepository,
    val settlementRepository: SettlementRepository,
    val todoRepository: TodoRepository,
    val personalExpenseRepository: PersonalExpenseRepository,
    val usingFirebase: Boolean,
)

private fun createRepositoryBundle(appContext: Context): RepositoryBundle {
    return runCatching {
        val auth = FirebaseAuth.getInstance()
        val database = FirebaseDatabase.getInstance()
        RepositoryBundle(
            authRepository = FirebaseAuthRepository(auth = auth, database = database, context = appContext),
            groupRepository = FirebaseGroupRepository(database = database),
            expenseRepository = FirebaseExpenseRepository(database = database),
            settlementRepository = FirebaseSettlementRepository(
                database = database,
                appContext = appContext,
            ),
            todoRepository = FirebaseTodoRepository(database = database),
            personalExpenseRepository = FirebasePersonalExpenseRepository(database = database),
            usingFirebase = true,
        )
    }.getOrElse { error ->
        AppTelemetry.recordNonFatal(error, tags = mapOf("op" to "repository_bundle_init"))
        RepositoryBundle(
            authRepository = InMemoryAuthRepository(),
            groupRepository = InMemoryGroupRepository(),
            expenseRepository = InMemoryExpenseRepository(),
            settlementRepository = InMemorySettlementRepository(),
            todoRepository = InMemoryTodoRepository(),
            personalExpenseRepository = InMemoryPersonalExpenseRepository(),
            usingFirebase = false,
        )
    }
}

private sealed interface Destination {
    val route: String

    data object Auth : Destination {
        override val route: String = "auth"
    }

    data object Groups : Destination {
        override val route: String = "groups"
    }

    data object Todo : Destination {
        override val route: String = "todo"
    }

    data object SelfExpenses : Destination {
        override val route: String = "self_expenses"
    }

    data object Settings : Destination {
        override val route: String = "settings"
    }

    data object Group : Destination {
        override val route: String = "group/{groupId}"
        const val groupIdArg: String = "groupId"

        fun buildRoute(groupId: String): String = "group/$groupId"
    }

    data object SettlementPreview : Destination {
        override val route: String = "group/{groupId}/settlement-preview"
        const val groupIdArg: String = "groupId"

        fun buildRoute(groupId: String): String = "group/$groupId/settlement-preview"
    }
}

private fun InsightSettlementTransferUi.toSettlementTransferUi(): SettlementTransferUi {
    return SettlementTransferUi(
        transferKey = transferKey,
        payerUserId = payerUserId,
        receiverUserId = receiverUserId,
        payerName = payerName,
        receiverName = receiverName,
        amountDisplay = amountDisplay,
        amountPaise = amountPaise,
        receiverUpiId = receiverUpiId,
        canPayViaUpi = canPayViaUpi,
    )
}

private data class PendingSettlementCelebration(
    val pdfPath: String,
    val successUi: SettlementSuccessUi,
)

private data class UpiAppOption(
    val packageName: String,
    val displayName: String,
)

private data class PendingUpiAppSelection(
    val transfer: SettlementTransferUi,
    val attemptId: String,
    val intent: Intent,
    val appOptions: List<UpiAppOption>,
)

private data class PendingUpiLaunchRecord(
    val transferKey: String,
    val attemptId: String,
    val selectedApp: UpiAppOption,
    val startedAtEpochMs: Long,
)

private data class PendingUpiResultResolution(
    val transaction: SettlementUpiTransaction,
    val suggestedStatus: SettlementUpiStatus,
)

private data class ParsedSettlementUpiResult(
    val transaction: SettlementUpiTransaction,
    val suggestedStatus: SettlementUpiStatus,
)

@Composable
private fun UpiAppPickerDialog(
    amountDisplay: String,
    receiverName: String,
    appOptions: List<UpiAppOption>,
    onDismissRequest: () -> Unit,
    onAppSelected: (UpiAppOption) -> Unit,
) {
    val listMaxHeight = rememberDialogSectionMaxHeight(
        portraitFraction = 0.44f,
        landscapeFraction = 0.30f,
    )
    LetsGoDutchRevampDialog(
        label = "Payment App",
        title = "Choose UPI App",
        supportingText = "Pick the app for $amountDisplay to $receiverName. The selected app will be recorded with the settlement activity.",
        onDismissRequest = onDismissRequest,
        badgeText = "${appOptions.size} apps",
        icon = Icons.Default.ManageAccounts,
        content = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = listMaxHeight)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                appOptions.forEach { option ->
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onAppSelected(option) },
                        shape = RoundedCornerShape(20.dp),
                        color = MintGlow.copy(alpha = 0.12f),
                        border = BorderStroke(1.dp, MintGlow.copy(alpha = 0.46f)),
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = option.displayName,
                                style = MaterialTheme.typography.titleMedium,
                                color = TextOnDark,
                                fontWeight = FontWeight.SemiBold,
                            )
                            Text(
                                text = "Open",
                                style = MaterialTheme.typography.labelLarge,
                                color = MintGreen,
                                fontWeight = FontWeight.SemiBold,
                            )
                        }
                    }
                }
            }
        },
        actions = {
            OutlinedButton(
                onClick = onDismissRequest,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Cancel")
            }
        },
    )
}

@Composable
private fun UpiResultConfirmationDialog(
    resolution: PendingUpiResultResolution,
    onStatusConfirmed: (SettlementUpiStatus) -> Unit,
) {
    val transaction = resolution.transaction
    val negativeStatus = when (resolution.suggestedStatus) {
        SettlementUpiStatus.FAILED -> SettlementUpiStatus.FAILED
        else -> SettlementUpiStatus.CANCELLED
    }
    LetsGoDutchRevampDialog(
        label = "Payment Result",
        title = "Confirm Transaction Status",
        supportingText = "Confirm what happened in ${transaction.paymentAppName.ifBlank { "the selected UPI app" }}. This result will be recorded in payment activity and the settlement PDF. Final balances stay unchanged until owner settlement.",
        onDismissRequest = {},
        badgeText = transaction.amountPaise.toRupeeDisplay(),
        icon = Icons.Default.TaskAlt,
        content = {
            LetsGoDutchRevampCard {
                LetsGoDutchDialogLabelValueRow(label = "Payer", value = transaction.payerName.ifBlank { "Member" })
                LetsGoDutchDialogLabelValueRow(label = "Receiver", value = transaction.receiverName.ifBlank { "Member" })
                transaction.paymentAppName.takeIf { it.isNotBlank() }?.let {
                    LetsGoDutchDialogLabelValueRow(label = "App", value = it)
                }
                if (resolution.suggestedStatus != SettlementUpiStatus.UNKNOWN) {
                    LetsGoDutchDialogLabelValueRow(
                        label = "Detected",
                        value = resolution.suggestedStatus.displayLabel,
                        valueColor = when (resolution.suggestedStatus) {
                            SettlementUpiStatus.SUCCESS -> MintGreen
                            SettlementUpiStatus.PENDING -> MintTeal
                            SettlementUpiStatus.CANCELLED,
                            SettlementUpiStatus.FAILED,
                            -> CoralSoft
                            SettlementUpiStatus.UNKNOWN -> TextOnDark
                        },
                    )
                }
            }
        },
        actions = {
            GradientButton(
                text = "Completed",
                onClick = { onStatusConfirmed(SettlementUpiStatus.SUCCESS) },
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedButton(
                onClick = { onStatusConfirmed(SettlementUpiStatus.PENDING) },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Pending")
            }
            OutlinedButton(
                onClick = { onStatusConfirmed(negativeStatus) },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(if (negativeStatus == SettlementUpiStatus.FAILED) "Failed" else "Not Completed")
            }
        },
    )
}

private fun buildSplitSharesFromDraft(
    draft: com.buddingintents.letsgodutch.feature.expenses.ExpenseDraft,
    totalPaise: Long,
    participants: List<String>,
): Result<List<SplitShare>> {
    return runCatching {
        require(participants.isNotEmpty()) { "At least one participant is required." }
        val inputByUserId = draft.splitInputs.associate { it.userId to it.value.trim() }

        when (draft.splitType) {
            SplitType.EQUAL -> emptyList()
            SplitType.EXACT -> {
                val shares = participants.map { userId ->
                    val value = inputByUserId[userId].orEmpty()
                    val paise = value.toPaise()
                        ?: error("Exact split requires amount for each selected member.")
                    SplitShare(userId = userId, amountPaise = paise)
                }
                val exactTotal = shares.sumOf { it.amountPaise ?: 0L }
                require(exactTotal == totalPaise) {
                    "Exact split total ${exactTotal.toRupeeDisplay()} must equal expense ${totalPaise.toRupeeDisplay()}."
                }
                shares
            }

            SplitType.PERCENTAGE -> {
                val shares = participants.map { userId ->
                    val value = inputByUserId[userId].orEmpty()
                    val pct = value.toDoubleOrNull()
                        ?: error("Percentage split requires numeric values for each selected member.")
                    require(pct >= 0.0) { "Percentage cannot be negative." }
                    SplitShare(userId = userId, percentage = pct)
                }
                val pctTotal = shares.sumOf { it.percentage ?: 0.0 }
                require(kotlin.math.abs(pctTotal - 100.0) < 0.01) {
                    "Percentage split must add up to 100."
                }
                shares
            }

            SplitType.CUSTOM -> {
                val shares = participants.map { userId ->
                    val value = inputByUserId[userId].orEmpty()
                    val units = value.toDoubleOrNull()
                        ?: error("Custom split requires numeric units for each selected member.")
                    require(units > 0.0) { "Custom units must be greater than zero." }
                    SplitShare(userId = userId, customUnits = units)
                }
                shares
            }
        }
    }
}

private fun String.toPaise(): Long? {
    val normalized = trim()
    if (normalized.isEmpty()) return null
    val rupees = normalized.toBigDecimalOrNull() ?: return null
    return rupees
        .multiply(BigDecimal(100))
        .setScale(0, RoundingMode.HALF_UP)
        .toLong()
}

private val backendPaymentDateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy", Locale.US)
private val groupDetailsDateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a", Locale.US)

private fun String.toBackendPaymentDateOrNull(): LocalDate? {
    val normalized = trim()
    if (normalized.isEmpty()) return null
    return runCatching {
        LocalDate.parse(normalized, backendPaymentDateFormatter)
    }.getOrNull()
}

private fun LocalDate.toBackendPaymentDate(): String = format(backendPaymentDateFormatter)

private fun Long.toGroupDetailsDateTime(): String {
    return Instant.ofEpochMilli(this)
        .atZone(ZoneId.systemDefault())
        .toLocalDateTime()
        .format(groupDetailsDateFormatter)
}

private fun String?.toNormalizedInviteCode(): String {
    return this.orEmpty()
        .trim()
        .uppercase()
        .filter { it.isLetterOrDigit() }
}

private fun isManualMemberUserId(userId: String): Boolean {
    return userId.startsWith("guest_")
}

private fun Long.toInrDisplay(): String {
    return formatIndianCurrency(this)
}

private fun Long.toRupeeDisplay(): String {
    return formatIndianCurrency(this)
}

private fun Context.defaultWebClientIdOrNull(): String? {
    val resourceId = resources.getIdentifier("default_web_client_id", "string", packageName)
    if (resourceId == 0) return null
    return getString(resourceId).takeIf { it.isNotBlank() && !it.contains("YOUR_WEB_CLIENT_ID") }
}

private fun buildGoogleSignInRequest(serverClientId: String): GetCredentialRequest {
    val googleSignInOption = GetSignInWithGoogleOption.Builder(serverClientId)
        .build()
    return GetCredentialRequest.Builder()
        .addCredentialOption(googleSignInOption)
        .build()
}

private fun Context.googleSignInConfigIssueMessage(): String {
    val appIdRes = resources.getIdentifier("google_app_id", "string", packageName)
    return if (appIdRes != 0) {
        "google-services.json is loaded, but it has no web OAuth client. " +
            "Enable Google sign-in in Firebase Auth, add SHA keys, then download a fresh google-services.json."
    } else {
        "default_web_client_id not found. Place a valid google-services.json under app/ and sync."
    }
}

private fun Context.firebaseRealtimeDbConfigIssueOrNull(): String? {
    val dbUrlRes = resources.getIdentifier("firebase_database_url", "string", packageName)
    if (dbUrlRes == 0) {
        return "Realtime Database is not configured. Create a Realtime Database instance in Firebase, " +
            "download a fresh app/google-services.json, then reinstall the app."
    }
    val url = getString(dbUrlRes).trim()
    if (url.isBlank() || !url.startsWith("https://")) {
        return "Realtime Database URL is invalid in configuration. Download a fresh app/google-services.json."
    }
    return null
}

private fun Throwable.toActionableMessage(defaultMessage: String): String {
    val raw = (localizedMessage ?: message).orEmpty()
    val normalized = raw.lowercase()
    return when {
        normalized.contains("permission denied") -> {
            "Realtime Database Rules denied access. Ensure authenticated users, including anonymous users, can read and write their profile and group data. For device-id anonymous restore, the previous anonymous data must also be readable for migration or merged by a trusted backend."
        }

        normalized.contains("index not defined") -> {
            "Database query index is missing in Realtime Database Rules. Add the required .indexOn or use a non-indexed read path."
        }

        normalized.contains("database url") ||
            normalized.contains("invalid firebase database") ||
            normalized.contains("can't determine firebase database") ||
            normalized.contains("404") -> {
            "Realtime Database is unavailable for this project. Create the database in Firebase and download a fresh app/google-services.json."
        }

        raw.isNotBlank() -> raw
        else -> defaultMessage
    }
}

private fun GetCredentialException.toGoogleSignInMessage(): String {
    if (this is NoCredentialException) {
        return "No Google account is available for sign-in on this device. " +
            "Add one in Android settings or update Google Play services, then try again."
    }

    val raw = (localizedMessage ?: message).orEmpty().trim()
    val normalized = raw.lowercase()
    return when {
        normalized.contains("account reauth failed") ||
            normalized.contains("[16]") ||
            normalized.contains("status code: 16") -> {
            "Google sign-in failed because this build's signing key is not fully configured " +
                "for Google/Firebase auth. Add the SHA-1 and SHA-256 for the app variant " +
                "you are running (debug, release, and Play App Signing if applicable), " +
                "download a fresh app/google-services.json, then reinstall the app."
        }

        raw.isBlank() -> "Google sign-in failed. Please try again."
        else -> "Google sign-in failed. $raw"
    }
}

private tailrec fun Context.findActivity(): Activity? {
    return when (this) {
        is Activity -> this
        is ContextWrapper -> baseContext.findActivity()
        else -> null
    }
}

private fun Context.shareJoinLink(group: Group) {
    val inviteCode = group.inviteCode.toNormalizedInviteCode()
    val webJoinLink = "$WEB_JOIN_LINK_URL_PREFIX$inviteCode"
    copyToClipboard(label = "Invite link", text = webJoinLink)
    val message = buildString {
        append("Join ${group.name} on Let's Go Dutch\n")
        append("$webJoinLink\n")
        append("Invite code: $inviteCode\n\n")
        append("New to Let's Go Dutch? Download the app here:\n")
        append(PLAY_STORE_DOWNLOAD_URL)
    }
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_SUBJECT, "Join ${group.name} on Let's Go Dutch")
        putExtra(Intent.EXTRA_TEXT, message)
    }
    startChooser(intent, "Share Invite")
}

private fun Context.copyToClipboard(label: String, text: String) {
    val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as? android.content.ClipboardManager
        ?: return
    clipboard.setPrimaryClip(android.content.ClipData.newPlainText(label, text))
}

private fun Context.shareSettlementPdf(pdfPath: String) {
    val file = File(pdfPath)
    if (!file.exists()) return
    val uri = FileProvider.getUriForFile(
        this,
        "$packageName.fileprovider",
        file,
    )
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "application/pdf"
        putExtra(Intent.EXTRA_STREAM, uri)
        putExtra(Intent.EXTRA_SUBJECT, "Let's Go Dutch Settlement Report")
        putExtra(Intent.EXTRA_TEXT, "Settlement report from Let's Go Dutch.")
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    startChooser(intent, "Share Settlement PDF")
}

private fun Context.openSettlementPdf(pdfPath: String) {
    val file = File(pdfPath)
    if (!file.exists()) {
        showShortToast("Saved PDF is missing from this device.")
        return
    }
    val uri = FileProvider.getUriForFile(
        this,
        "$packageName.fileprovider",
        file,
    )
    val intent = Intent(Intent.ACTION_VIEW).apply {
        setDataAndType(uri, "application/pdf")
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    startChooser(intent, "Open Settlement PDF")
}

private fun buildSettlementUpiPaymentIntent(
    upiId: String,
    receiverName: String,
    amountPaise: Long,
    groupName: String,
    attemptId: String,
): Intent? {
    val normalizedUpiId = upiId.normalizeUpiId()
    if (normalizedUpiId.isBlank() || amountPaise <= 0L) {
        return null
    }
    return Intent(
        Intent.ACTION_VIEW,
        Uri.Builder()
            .scheme("upi")
            .authority("pay")
            .appendQueryParameter("pa", normalizedUpiId)
            .appendQueryParameter("pn", receiverName.ifBlank { "Member" })
            .appendQueryParameter("am", amountPaise.toUpiAmount())
            .appendQueryParameter("cu", "INR")
            .appendQueryParameter("tr", attemptId)
            .appendQueryParameter("tn", buildSettlementUpiNote(groupName))
            .build(),
    )
}

private fun buildSettlementUpiNote(groupName: String): String {
    return "Let's Go Dutch settlement - ${groupName.ifBlank { "Group" }}"
}

private fun Context.findAvailableUpiApps(baseIntent: Intent): List<UpiAppOption> {
    val matches = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        packageManager.queryIntentActivities(
            baseIntent,
            PackageManager.ResolveInfoFlags.of(PackageManager.MATCH_DEFAULT_ONLY.toLong()),
        )
    } else {
        @Suppress("DEPRECATION")
        packageManager.queryIntentActivities(baseIntent, PackageManager.MATCH_DEFAULT_ONLY)
    }
    return matches
        .mapNotNull { resolveInfo ->
            val packageName = resolveInfo.activityInfo?.packageName.orEmpty()
            if (packageName.isBlank()) {
                null
            } else {
                UpiAppOption(
                    packageName = packageName,
                    displayName = resolveInfo.loadLabel(packageManager)
                        ?.toString()
                        .orEmpty()
                        .ifBlank { packageName },
                )
            }
        }
        .distinctBy { it.packageName }
        .sortedBy { it.displayName.lowercase(Locale.US) }
}

private fun Context.openPlayStoreReviewPage() {
    val marketIntent = Intent(
        Intent.ACTION_VIEW,
        Uri.parse("market://details?id=$packageName"),
    ).apply {
        if (this@openPlayStoreReviewPage !is Activity) {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
    }
    val webIntent = Intent(
        Intent.ACTION_VIEW,
        Uri.parse("https://play.google.com/store/apps/details?id=$packageName"),
    ).apply {
        if (this@openPlayStoreReviewPage !is Activity) {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
    }

    runCatching { startActivity(marketIntent) }
        .recoverCatching { startActivity(webIntent) }
}

private suspend fun Context.generatePersonalExpenseReportPdf(
    userDisplayName: String,
    filterDescription: String,
    expenses: List<PersonalExpenseEntry>,
): Result<String> {
    return runCatching {
        require(expenses.isNotEmpty()) { "No expenses to export." }
        withContext(Dispatchers.IO) {
            val outputDir = File(filesDir, "personal_expenses").apply { mkdirs() }
            val fileName = "personal_expense_report_${System.currentTimeMillis()}.pdf"
            val outputFile = File(outputDir, fileName)

            val document = PdfDocument()
            val titlePaint = Paint().apply {
                color = Color.BLACK
                textSize = 32f
                isFakeBoldText = true
            }
            val sectionPaint = Paint().apply {
                color = Color.BLACK
                textSize = 21f
                isFakeBoldText = true
            }
            val bodyPaint = Paint().apply {
                color = Color.BLACK
                textSize = 18f
            }
            val smallPaint = Paint().apply {
                color = Color.DKGRAY
                textSize = 15f
            }

            var pageNumber = 1
            var page = document.startPage(
                PdfDocument.PageInfo.Builder(1080, 1920, pageNumber).create(),
            )
            var canvas = page.canvas
            var y = 70f

            canvas.drawText("Personal Expense Report", 42f, y, titlePaint)
            y += 40f
            canvas.drawText("User: $userDisplayName", 42f, y, bodyPaint)
            y += 30f
            canvas.drawText("Generated: ${System.currentTimeMillis().toReportDateTime()}", 42f, y, smallPaint)
            y += 26f
            canvas.drawText("Filter: $filterDescription", 42f, y, smallPaint)
            y += 36f

            val total = expenses.sumOf { it.amountPaise }
            val average = if (expenses.isNotEmpty()) total / expenses.size else 0L
            val highest = expenses.maxByOrNull { it.amountPaise }?.amountPaise ?: 0L

            canvas.drawText("Summary", 42f, y, sectionPaint)
            y += 26f
            canvas.drawText("Entries: ${expenses.size}", 42f, y, bodyPaint)
            y += 24f
            canvas.drawText("Total: ${total.toAsciiInrDisplay()}", 42f, y, bodyPaint)
            y += 24f
            canvas.drawText("Average: ${average.toAsciiInrDisplay()}", 42f, y, bodyPaint)
            y += 24f
            canvas.drawText("Highest: ${highest.toAsciiInrDisplay()}", 42f, y, bodyPaint)
            y += 35f

            canvas.drawText("Expenses", 42f, y, sectionPaint)
            y += 26f
            canvas.drawText("Date", 42f, y, smallPaint)
            canvas.drawText("Title", 230f, y, smallPaint)
            canvas.drawText("Amount", 860f, y, smallPaint)
            y += 18f
            canvas.drawLine(42f, y, 1038f, y, smallPaint)
            y += 18f

            expenses.sortedByDescending { it.spentAtEpochMs }.forEach { expense ->
                if (y > 1820f) {
                    document.finishPage(page)
                    pageNumber += 1
                    page = document.startPage(
                        PdfDocument.PageInfo.Builder(1080, 1920, pageNumber).create(),
                    )
                    canvas = page.canvas
                    y = 70f
                    canvas.drawText("Expenses (contd.)", 42f, y, sectionPaint)
                    y += 30f
                    canvas.drawText("Date", 42f, y, smallPaint)
                    canvas.drawText("Title", 230f, y, smallPaint)
                    canvas.drawText("Amount", 860f, y, smallPaint)
                    y += 18f
                    canvas.drawLine(42f, y, 1038f, y, smallPaint)
                    y += 18f
                }

                val dateText = expense.spentAtEpochMs.toReportDateTime()
                val titleText = if (expense.title.length > 42) {
                    expense.title.take(39) + "..."
                } else {
                    expense.title
                }
                canvas.drawText(dateText, 42f, y, bodyPaint)
                canvas.drawText(titleText, 230f, y, bodyPaint)
                canvas.drawText(expense.amountPaise.toAsciiInrDisplay(), 860f, y, bodyPaint)
                y += 24f
            }

            document.finishPage(page)
            FileOutputStream(outputFile).use { output ->
                document.writeTo(output)
            }
            document.close()
            outputFile.absolutePath
        }
    }
}

private fun Context.sharePersonalExpensePdf(pdfPath: String) {
    val file = File(pdfPath)
    if (!file.exists()) return
    val uri = FileProvider.getUriForFile(
        this,
        "$packageName.fileprovider",
        file,
    )
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "application/pdf"
        putExtra(Intent.EXTRA_STREAM, uri)
        putExtra(Intent.EXTRA_SUBJECT, "Let's Go Dutch Personal Expense Report")
        putExtra(Intent.EXTRA_TEXT, "Personal expense report from Let's Go Dutch.")
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    startChooser(intent, "Share Expense Report PDF")
}

private val personalExpenseReportDateFormatter: DateTimeFormatter =
    DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a", Locale.US)

private fun Long.toReportDateTime(): String {
    return Instant.ofEpochMilli(this)
        .atZone(ZoneId.systemDefault())
        .toLocalDateTime()
        .format(personalExpenseReportDateFormatter)
}

private fun Long.toAsciiInrDisplay(): String {
    val absolute = kotlin.math.abs(this)
    val rupees = absolute / 100
    val paise = absolute % 100
    val prefix = if (this < 0L) "-INR " else "INR "
    return "$prefix$rupees.${paise.toString().padStart(2, '0')}"
}

private fun Long.toUpiAmount(): String {
    return BigDecimal(this)
        .movePointLeft(2)
        .setScale(2, RoundingMode.HALF_UP)
        .toPlainString()
}

private fun Context.startChooser(baseIntent: Intent, title: String) {
    val chooser = Intent.createChooser(baseIntent, title)
    if (this !is Activity) {
        chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    startActivity(chooser)
}

private fun parseSettlementUpiTransactionResult(
    resultCode: Int,
    data: Intent?,
    transfer: SettlementTransferUi,
    launchAttemptId: String,
    selectedApp: UpiAppOption,
    launchDurationMs: Long,
): ParsedSettlementUpiResult {
    val rawResponse = firstNotBlank(
        data?.getStringExtra("response"),
        data?.getStringExtra("RESP"),
        data?.getStringExtra("txn_response"),
        data?.getStringExtra("Status"),
        data?.dataString,
    )
    val responseMap = parseUpiResponseMap(rawResponse)
    val statusValue = firstNotBlank(
        data?.getStringExtra("status"),
        data?.getStringExtra("Status"),
        responseMap["status"],
    ).uppercase(Locale.US)
    val resolvedStatus = when {
        statusValue.contains("SUCCESS") -> SettlementUpiStatus.SUCCESS
        statusValue.contains("SUBMITTED") || statusValue.contains("PENDING") -> SettlementUpiStatus.PENDING
        statusValue.contains("CANCEL") -> SettlementUpiStatus.CANCELLED
        statusValue.contains("FAIL") -> {
            if (resultCode == Activity.RESULT_CANCELED) SettlementUpiStatus.CANCELLED else SettlementUpiStatus.FAILED
        }
        rawResponse.isBlank() && resultCode == Activity.RESULT_OK -> SettlementUpiStatus.SUCCESS
        rawResponse.isBlank() && launchDurationMs >= 8_000L -> SettlementUpiStatus.UNKNOWN
        else -> SettlementUpiStatus.UNKNOWN
    }

    val transaction = SettlementUpiTransaction(
        activityId = launchAttemptId,
        transferKey = transfer.transferKey,
        payerUserId = transfer.payerUserId,
        payerName = transfer.payerName,
        receiverUserId = transfer.receiverUserId,
        receiverName = transfer.receiverName,
        receiverUpiId = transfer.receiverUpiId,
        amountPaise = transfer.amountPaise,
        status = resolvedStatus,
        paymentAppName = selectedApp.displayName,
        paymentAppPackageName = selectedApp.packageName,
        transactionRef = firstNotBlank(
            data?.getStringExtra("txnRef"),
            data?.getStringExtra("tr"),
            responseMap["txnref"],
            responseMap["tr"],
            responseMap["refid"],
            responseMap["approvalrefno"],
            launchAttemptId,
        ),
        transactionId = firstNotBlank(
            data?.getStringExtra("txnId"),
            responseMap["txnid"],
            responseMap["transactionid"],
            launchAttemptId,
        ),
        approvalRefNo = firstNotBlank(
            responseMap["approvalrefno"],
            responseMap["approvalref"],
        ),
        responseCode = firstNotBlank(
            responseMap["responsecode"],
            responseMap["code"],
        ),
        rawResponse = rawResponse,
    )
    return ParsedSettlementUpiResult(
        transaction = transaction,
        suggestedStatus = resolvedStatus,
    )
}

private fun parseUpiResponseMap(rawResponse: String): Map<String, String> {
    if (rawResponse.isBlank()) return emptyMap()
    return rawResponse
        .split("&")
        .mapNotNull { segment ->
            val separatorIndex = segment.indexOf('=')
            if (separatorIndex <= 0) return@mapNotNull null
            val key = Uri.decode(segment.substring(0, separatorIndex)).trim().lowercase(Locale.US)
            val value = Uri.decode(segment.substring(separatorIndex + 1)).trim()
            key.takeIf { it.isNotBlank() }?.let { it to value }
        }
        .toMap()
}

private fun SettlementUpiStatus.toSettlementUpiUserMessage(): String {
    return when (this) {
        SettlementUpiStatus.SUCCESS ->
            "UPI payment recorded. Further UPI attempts are blocked until the group is settled."
        SettlementUpiStatus.CANCELLED ->
            "Cancelled UPI response recorded. The transfer stays in the suggestion list."
        SettlementUpiStatus.PENDING ->
            "UPI payment is pending. The transfer stays in the suggestion list."
        SettlementUpiStatus.FAILED ->
            "UPI payment failed. The transfer stays in the suggestion list."
        SettlementUpiStatus.UNKNOWN ->
            "UPI response captured. Review it before you settle the group."
    }
}

private fun buildSettlementUpiReferenceLabel(transaction: SettlementUpiTransaction): String {
    val segments = mutableListOf<String>()
    transaction.paymentAppName.takeIf { it.isNotBlank() }?.let { segments += "App: $it" }
    if (transaction.statusConfirmedByUser) {
        segments += "User confirmed"
    }
    transaction.transactionId.takeIf { it.isNotBlank() }?.let { segments += "Txn: $it" }
    transaction.transactionRef.takeIf { it.isNotBlank() && it != transaction.transactionId }?.let { segments += "Ref: $it" }
    transaction.approvalRefNo.takeIf {
        it.isNotBlank() &&
            it != transaction.transactionId &&
            it != transaction.transactionRef
    }?.let { segments += "Approval: $it" }
    transaction.responseCode.takeIf { it.isNotBlank() }?.let { segments += "Code: $it" }
    return segments.joinToString(" | ")
}

private fun buildSettlementUpiAttemptId(): String {
    return "ATTEMPT-${System.currentTimeMillis()}"
}

private fun firstNotBlank(vararg values: String?): String {
    return values.firstOrNull { !it.isNullOrBlank() }?.trim().orEmpty()
}

private fun Context.showShortToast(message: String) {
    Toast.makeText(applicationContext, message, Toast.LENGTH_SHORT).show()
}
private const val JOIN_SOURCE_DEEP_LINK = "deep_link"
private const val SETTLEMENT_PREVIEW_BANNER_AD_UNIT_ID = "ca-app-pub-2020561089374332/8664878132"
private const val SETTLEMENT_INTERSTITIAL_AD_UNIT_ID = "ca-app-pub-2020561089374332/2345409615"

private data class PendingInviteJoinRequest(
    val inviteCode: String,
    val source: String,
    val preview: JoinGroupPreview,
)

private fun googleSignInErrorMessage(statusCode: Int, rawMessage: String?): String {
    return when (statusCode) {
        1 -> "Google sign-in canceled."
        else -> {
            val base = "Google sign-in failed (code: $statusCode)."
            if (rawMessage.isNullOrBlank()) base else "$base $rawMessage"
        }
    }
}

private fun Member.toFriendlyDisplayName(): String {
    val direct = displayName.trim()
    if (direct.isNotBlank() && !direct.equals(userId, ignoreCase = true)) return direct

    val emailPrefix = email.substringBefore("@").trim()
    if (emailPrefix.isNotBlank() && !emailPrefix.equals(userId, ignoreCase = true)) {
        return emailPrefix
    }

    if (direct.isNotBlank()) return direct
    if (emailPrefix.isNotBlank()) return emailPrefix
    return "Member"
}

private fun UserProfile?.toFriendlyDisplayName(): String {
    if (this == null) return "Member"
    val direct = displayName.trim()
    if (direct.isNotBlank()) return direct

    val emailPrefix = email.substringBefore("@").trim()
    if (emailPrefix.isNotBlank()) return emailPrefix

    return "Member"
}

private fun UserProfile?.toSettingsAccountSummary(): String {
    if (this == null) return "Stable account identity"
    return when {
        isAnonymous -> "Anonymous account"
        primaryAuthProvider == "google.com" && upgradedFromAnonymousAtEpochMs != null ->
            "Google-linked account, upgraded in place"
        primaryAuthProvider == "google.com" -> "Google-linked account"
        else -> "Stable account identity"
    }
}
