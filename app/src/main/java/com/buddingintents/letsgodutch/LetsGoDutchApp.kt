package com.buddingintents.letsgodutch

import android.Manifest
import android.app.Activity
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.ContextWrapper
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
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.CustomCredential
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.NoCredentialException
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
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
import com.buddingintents.letsgodutch.core.model.Balance
import com.buddingintents.letsgodutch.core.model.ExitLiabilityChoice
import com.buddingintents.letsgodutch.core.model.Expense
import com.buddingintents.letsgodutch.core.model.Group
import com.buddingintents.letsgodutch.core.model.JoinGroupPreview
import com.buddingintents.letsgodutch.core.model.Member
import com.buddingintents.letsgodutch.core.model.PersonalExpenseEntry
import com.buddingintents.letsgodutch.core.model.Role
import com.buddingintents.letsgodutch.core.model.SplitShare
import com.buddingintents.letsgodutch.core.model.SplitType
import com.buddingintents.letsgodutch.core.model.TodoTask
import com.buddingintents.letsgodutch.core.model.TodoTaskStatus
import com.buddingintents.letsgodutch.core.model.UserProfile
import com.buddingintents.letsgodutch.core.designsystem.theme.ThemeMode
import com.buddingintents.letsgodutch.feature.auth.AuthScreen
import com.buddingintents.letsgodutch.feature.expenses.AddExpenseDialog
import com.buddingintents.letsgodutch.feature.expenses.ExpenseMemberOption
import com.buddingintents.letsgodutch.feature.groups.GroupsListScreen
import com.buddingintents.letsgodutch.feature.insights.InsightsScreen
import com.buddingintents.letsgodutch.feature.ledger.LedgerScreen
import com.buddingintents.letsgodutch.notifications.FcmTokenSyncManager
import com.buddingintents.letsgodutch.notifications.LetsGoDutchMessagingService
import com.buddingintents.letsgodutch.telemetry.AppTelemetry
import com.buddingintents.letsgodutch.tour.isAppTourCompleted
import com.buddingintents.letsgodutch.tour.setAppTourCompleted
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.FileProvider
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
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flowOf
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
    val scope = rememberCoroutineScope()

    var authMessage by remember {
        mutableStateOf(
            if (repositories.usingFirebase) "" else "Firebase is unavailable. Running in demo mode.",
        )
    }
    var isGoogleSignInInProgress by rememberSaveable { mutableStateOf(false) }
    var pendingInviteCode by rememberSaveable { mutableStateOf<String?>(null) }
    var pendingJoinClaimRequest by remember { mutableStateOf<PendingInviteJoinRequest?>(null) }
    var groupsMessage by rememberSaveable { mutableStateOf("") }
    var showCreateGroupDialog by rememberSaveable { mutableStateOf(false) }
    var showJoinGroupDialog by rememberSaveable { mutableStateOf(false) }
    var isTourCompleted by remember(context) { mutableStateOf(context.isAppTourCompleted()) }
    var showAppTour by rememberSaveable { mutableStateOf(false) }

    val incomingInviteCodeValue by (incomingInviteCode?.collectAsState(initial = null)
        ?: remember { mutableStateOf<String?>(null) })

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
            joinedGroup?.groupId?.let { groupId ->
                navController.navigate(Destination.Group.buildRoute(groupId)) {
                    popUpTo(Destination.Groups.route) { inclusive = false }
                    launchSingleTop = true
                }
            }
        } else {
            groupsMessage = resultJoin.exceptionOrNull()
                ?.toActionableMessage("Unable to join group.")
                .orEmpty()
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
            navController.navigate(Destination.Group.buildRoute(preview.group.groupId)) {
                popUpTo(Destination.Groups.route) { inclusive = false }
                launchSingleTop = true
            }
            return
        }

        if (preview.claimableMembers.isNotEmpty()) {
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
                val title = snapshot.child("title").getValue(String::class.java)
                    .orEmpty()
                    .ifBlank { context.getString(R.string.app_name) }
                val body = snapshot.child("body").getValue(String::class.java)
                    .orEmpty()
                    .ifBlank { "You have a new group update." }
                context.showRealtimeDbNotification(
                    title = title,
                    body = body,
                    notificationIdHint = snapshot.key,
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
            if (currentUser?.userId.isNullOrBlank()) {
                authMessage = "Continue with Google or name to join invite: $code"
            }
        }
    }

    LaunchedEffect(currentUser?.userId, pendingInviteCode) {
        val userId = currentUser?.userId.orEmpty()
        val invite = pendingInviteCode.orEmpty()
        if (userId.isBlank() || invite.isBlank()) return@LaunchedEffect
        pendingInviteCode = null
        previewInviteJoin(inviteCode = invite, source = JOIN_SOURCE_DEEP_LINK)
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
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = { },
                        actions = {
                            if (onThemeModeChange != null) {
                                ThemeMenu(onThemeModeChange = onThemeModeChange)
                            }
                        },
                    )
                },
            ) { paddingValues ->
                AuthScreen(
                modifier = Modifier
                    .consumeWindowInsets(paddingValues)
                    .padding(paddingValues),
                onGoogleSignInClick = {
                    if (isGoogleSignInInProgress) {
                        authMessage = "Google sign-in already in progress. Please wait."
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
                    if (isGoogleSignInInProgress) {
                        authMessage = "Google sign-in already in progress. Please wait."
                        return@AuthScreen
                    }
                    AppTelemetry.logEvent("login_click", mapOf("method" to "anonymous"))
                    scope.launch {
                        val resultSignIn = authRepository.signInAnonymously(displayName)
                        if (resultSignIn.isSuccess) {
                            AppTelemetry.logEvent("login_success", mapOf("path" to "anonymous"))
                            navController.navigate(Destination.Groups.route) {
                                popUpTo(Destination.Auth.route) { inclusive = true }
                            }
                        } else {
                            authMessage = resultSignIn.exceptionOrNull()?.message
                                ?: "Unable to continue with name."
                            resultSignIn.exceptionOrNull()?.let { error ->
                                AppTelemetry.recordNonFatal(error, tags = mapOf("op" to "login_anonymous"))
                            }
                        }
                    }
                },
                message = authMessage,
                anonymousNameHints = anonymousNameHints,
                logoResId = R.mipmap.ic_launcher_foreground,
            )
            }
        }

        composable(route = Destination.Todo.route) {
            val userId = currentUser?.userId.orEmpty()
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
                onSignOut = {
                    scope.launch {
                        authRepository.signOut()
                        navController.navigate(Destination.Auth.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                },
                onNavigateToTodo = {
                    navController.navigate(Destination.Todo.route) {
                        launchSingleTop = true
                    }
                },
                onNavigateToSelfExpenses = {
                    navController.navigate(Destination.SelfExpenses.route) {
                        launchSingleTop = true
                    }
                },
                onCreateGroupClick = { showCreateGroupDialog = true },
                onJoinGroupClick = { showJoinGroupDialog = true },
                onNavigateToSettings = {
                    navController.navigate(Destination.Settings.route) {
                        launchSingleTop = true
                    }
                },
                currentThemeMode = currentThemeMode,
                onThemeModeChange = onThemeModeChange,
                floatingActionButton = {
                    ExtendedFloatingActionButton(
                        modifier = Modifier.padding(bottom = 72.dp),
                        onClick = { showAddTaskDialog = true },
                        icon = {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = null,
                            )
                        },
                        text = { Text("Add Task") },
                    )
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
                            if (updateResult.isFailure) {
                                groupsMessage = updateResult.exceptionOrNull()
                                    ?.toActionableMessage("Unable to mark task as completed.")
                                    .orEmpty()
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
                            if (updateResult.isFailure) {
                                groupsMessage = updateResult.exceptionOrNull()
                                    ?.toActionableMessage("Unable to cancel task.")
                                    .orEmpty()
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
                onSignOut = {
                    scope.launch {
                        authRepository.signOut()
                        navController.navigate(Destination.Auth.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                },
                onNavigateToTodo = {
                    navController.navigate(Destination.Todo.route) {
                        launchSingleTop = true
                    }
                },
                onNavigateToSelfExpenses = {
                    navController.navigate(Destination.SelfExpenses.route) {
                        launchSingleTop = true
                    }
                },
                onCreateGroupClick = { showCreateGroupDialog = true },
                onJoinGroupClick = { showJoinGroupDialog = true },
                onNavigateToSettings = {
                    navController.navigate(Destination.Settings.route) {
                        launchSingleTop = true
                    }
                },
                currentThemeMode = currentThemeMode,
                onThemeModeChange = onThemeModeChange,
                floatingActionButton = {
                    ExtendedFloatingActionButton(
                        modifier = Modifier.padding(bottom = 72.dp),
                        onClick = { showAddPersonalExpenseDialog = true },
                        icon = {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = null,
                            )
                        },
                        text = { Text("Add Expense") },
                    )
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
                            } else {
                                groupsMessage = deleteResult.exceptionOrNull()
                                    ?.toActionableMessage("Unable to delete expense.")
                                    .orEmpty()
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
            var isSavingDisplayName by rememberSaveable { mutableStateOf(false) }
            AppScaffoldWithDrawer(
                currentUser = currentUser,
                navController = navController,
                title = "Settings",
                onSignOut = {
                    scope.launch {
                        authRepository.signOut()
                        navController.navigate(Destination.Auth.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                },
                onNavigateToTodo = {
                    navController.navigate(Destination.Todo.route) {
                        launchSingleTop = true
                    }
                },
                onNavigateToSelfExpenses = {
                    navController.navigate(Destination.SelfExpenses.route) {
                        launchSingleTop = true
                    }
                },
                onCreateGroupClick = { showCreateGroupDialog = true },
                onJoinGroupClick = { showJoinGroupDialog = true },
                onNavigateToSettings = {
                    navController.navigate(Destination.Settings.route) {
                        launchSingleTop = true
                    }
                },
                currentThemeMode = currentThemeMode,
                onThemeModeChange = onThemeModeChange,
            ) {
                SettingsScreen(
                    currentDisplayName = currentUser.toFriendlyDisplayName(),
                    isSavingDisplayName = isSavingDisplayName,
                    appUpdateSummary = appUpdateSummary,
                    isCheckingForAppUpdate = isCheckingForAppUpdate,
                    isDownloadedUpdateReady = isDownloadedUpdateReady,
                    onUpdateDisplayName = { updatedName ->
                        scope.launch {
                            isSavingDisplayName = true
                            val result = authRepository.updateDisplayName(updatedName)
                            isSavingDisplayName = false
                            if (result.isSuccess) {
                                groupsMessage = "Name updated across your groups."
                            } else {
                                groupsMessage = result.exceptionOrNull()
                                    ?.toActionableMessage("Unable to update name.")
                                    .orEmpty()
                                result.exceptionOrNull()?.let { error ->
                                    AppTelemetry.recordNonFatal(error, tags = mapOf("op" to "profile_name_update"))
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

            AppScaffoldWithDrawer(
                currentUser = currentUser,
                navController = navController,
                title = "Your Groups",
                onSignOut = {
                    scope.launch {
                        authRepository.signOut()
                        navController.navigate(Destination.Auth.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                },
                onNavigateToTodo = {
                    navController.navigate(Destination.Todo.route) {
                        launchSingleTop = true
                    }
                },
                onNavigateToSelfExpenses = {
                    navController.navigate(Destination.SelfExpenses.route) {
                        launchSingleTop = true
                    }
                },
                onCreateGroupClick = { showCreateGroupDialog = true },
                onJoinGroupClick = { showJoinGroupDialog = true },
                onNavigateToSettings = {
                    navController.navigate(Destination.Settings.route) {
                        launchSingleTop = true
                    }
                },
                currentThemeMode = currentThemeMode,
                onThemeModeChange = onThemeModeChange,
            ) {
            GroupsListScreen(
                groups = groups,
                onOpenGroup = { groupId ->
                    navController.navigate(Destination.Group.buildRoute(groupId)) {
                        popUpTo(Destination.Groups.route) { inclusive = false }
                        launchSingleTop = true
                    }
                },
                onShareGroupInvite = { group ->
                    if (group.inviteExpiryEpochMs < System.currentTimeMillis() && !group.autoRenewInvite) {
                        groupsMessage = "Invite has expired. An owner can renew it from Group Details."
                    } else {
                        context.shareJoinLink(group = group)
                    }
                },
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
            val expenses by expenseRepository.observeExpenses(groupId).collectAsState(initial = emptyList())
            val balances by expenseRepository.observeBalances(groupId).collectAsState(initial = emptyList())
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

            var showAddExpense by remember { mutableStateOf(false) }
            var showDeleteGroupDialog by rememberSaveable { mutableStateOf(false) }
            var showAddMemberDialog by rememberSaveable { mutableStateOf(false) }
            var showManageMembersDialog by rememberSaveable { mutableStateOf(false) }
            var showMembersListDialog by rememberSaveable { mutableStateOf(false) }
            var showGroupDetailsDialog by rememberSaveable { mutableStateOf(false) }
            var showSettlementInfoDialog by rememberSaveable { mutableStateOf(false) }
            var expensePendingDelete by remember { mutableStateOf<Expense?>(null) }
            var memberPendingRemoval by remember { mutableStateOf<Member?>(null) }
            var memberPendingEdit by remember { mutableStateOf<Member?>(null) }
            var selectedTab by remember { mutableIntStateOf(0) }
            var infoMessage by remember { mutableStateOf("") }
            val groupsForTitle by groupRepository.observeGroupsForUser(currentUserId)
                .collectAsState(initial = emptyList())
            val activeCurrentMember = members.firstOrNull { it.userId == currentUserId && it.active }
            val isOwner = activeCurrentMember?.role == Role.OWNER
            val isGroupMember = activeCurrentMember != null
            val hasExpenses = expenses.isNotEmpty()
            val groupSummary = groupsForTitle.firstOrNull { it.groupId == groupId }
            val mainOwnerUserId = groupSummary?.ownerUserId.orEmpty()
            val groupTitle = groupSummary?.name
                ?.let { "Group: $it" } ?: "Group: $groupId"

            LaunchedEffect(infoMessage) {
                if (infoMessage.isNotBlank()) {
                    context.showShortToast(infoMessage)
                    infoMessage = ""
                }
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
                                    ),
                                )
                            } else {
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
                            } else {
                                infoMessage = updateResult.exceptionOrNull()?.message
                                    ?: "Unable to update group details."
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
                            } else {
                                infoMessage = renewResult.exceptionOrNull()?.message
                                    ?: "Unable to renew invite."
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
                        memberPendingEdit = member
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
                            } else {
                                infoMessage = updateRoleResult.exceptionOrNull()?.message
                                    ?: "Unable to update member role."
                                updateRoleResult.exceptionOrNull()?.let { error ->
                                    AppTelemetry.recordNonFatal(error, tags = mapOf("op" to "member_role_update"))
                                }
                            }
                        }
                    },
                    onRemoveClick = { member ->
                        showManageMembersDialog = false
                        memberPendingRemoval = member
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
                            } else {
                                infoMessage = addResult.exceptionOrNull()?.message
                                    ?: "Unable to add member."
                                addResult.exceptionOrNull()?.let { error ->
                                    AppTelemetry.recordNonFatal(error, tags = mapOf("op" to "member_add_manual"))
                                }
                            }
                        }
                    },
                )
            }

            val editMember = memberPendingEdit
            if (editMember != null) {
                EditMemberDialog(
                    initialName = editMember.displayName.ifBlank { editMember.toFriendlyDisplayName() },
                    onDismiss = { memberPendingEdit = null },
                    onSave = { updatedName ->
                        if (!isOwner) {
                            infoMessage = "Only an owner can edit member names."
                            return@EditMemberDialog
                        }
                        memberPendingEdit = null
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
                            } else {
                                infoMessage = updateResult.exceptionOrNull()?.message ?: "Unable to update member."
                                updateResult.exceptionOrNull()?.let { error ->
                                    AppTelemetry.recordNonFatal(error, tags = mapOf("op" to "member_edit_manual"))
                                }
                            }
                        }
                    },
                )
            }

            val removeMember = memberPendingRemoval
            if (removeMember != null) {
                AlertDialog(
                    onDismissRequest = { memberPendingRemoval = null },
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
                                memberPendingRemoval = null
                                scope.launch {
                                    val removeResult = groupRepository.removeMember(
                                        groupId = groupId,
                                        memberUserId = removeMember.userId,
                                        actorUserId = currentUserId,
                                        liabilityChoice = ExitLiabilityChoice.ABSORB_BY_OWNER,
                                    )
                                    infoMessage = if (removeResult.isSuccess) {
                                        "Member removed."
                                    } else {
                                        removeResult.exceptionOrNull()?.message
                                            ?: "Unable to remove member."
                                    }
                                    if (removeResult.isFailure) {
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
                        TextButton(onClick = { memberPendingRemoval = null }) {
                            Text("Cancel")
                        }
                    },
                )
            }

            val expenseToDelete = expensePendingDelete
            if (expenseToDelete != null) {
                AlertDialog(
                    onDismissRequest = { expensePendingDelete = null },
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
                                expensePendingDelete = null
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
                                        deleteResult.exceptionOrNull()?.message ?: "Unable to delete entry."
                                    }
                                    if (deleteResult.isFailure) {
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
                        TextButton(onClick = { expensePendingDelete = null }) {
                            Text("Cancel")
                        }
                    },
                )
            }

            AppScaffoldWithDrawer(
                currentUser = currentUser,
                navController = navController,
                title = groupTitle,
                onSignOut = {
                    scope.launch {
                        authRepository.signOut()
                        navController.navigate(Destination.Auth.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                },
                onNavigateToTodo = {
                    navController.navigate(Destination.Todo.route) {
                        launchSingleTop = true
                    }
                },
                onNavigateToSelfExpenses = {
                    navController.navigate(Destination.SelfExpenses.route) {
                        launchSingleTop = true
                    }
                },
                onCreateGroupClick = { showCreateGroupDialog = true },
                onJoinGroupClick = { showJoinGroupDialog = true },
                onNavigateToSettings = {
                    navController.navigate(Destination.Settings.route) {
                        launchSingleTop = true
                    }
                },
                currentThemeMode = currentThemeMode,
                onThemeModeChange = onThemeModeChange,
                topBarActions = {
                    IconButton(onClick = { showSettlementInfoDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.HelpOutline,
                            contentDescription = "Settlement info",
                        )
                    }
                    GroupOverflowMenu(
                        isOwner = isOwner,
                        canAddMember = isGroupMember,
                        canViewMembers = isGroupMember,
                        hasExpenses = hasExpenses,
                        onMarkAsSettledClick = onMarkAsSettledClick,
                        onAddMemberClick = { showAddMemberDialog = true },
                        onManageMembersClick = { showManageMembersDialog = true },
                        onGroupDetailsClick = { showGroupDetailsDialog = true },
                        onViewMembersClick = { showMembersListDialog = true },
                        onDeleteGroupClick = { showDeleteGroupDialog = true },
                    )
                },
                floatingActionButton = {
                    ExtendedFloatingActionButton(
                        onClick = { showAddExpense = true },
                        icon = {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = null,
                            )
                        },
                        text = { Text("Add Expense") },
                    )
                },
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                ) {
                    TabRow(selectedTabIndex = selectedTab) {
                        Tab(
                            selected = selectedTab == 0,
                            onClick = { selectedTab = 0 },
                            text = { Text("Ledger") },
                        )
                        Tab(
                            selected = selectedTab == 1,
                            onClick = { selectedTab = 1 },
                            text = { Text("Insights") },
                        )
                    }

                    when (selectedTab) {
                        0 -> LedgerScreen(
                            expenses = expenses,
                            memberNameById = memberNameById,
                            memberPhotoUrlById = memberPhotoUrlById,
                            allowDelete = isOwner,
                            onDeleteExpenseClick = { expense ->
                                expensePendingDelete = expense
                            },
                            modifier = Modifier.weight(1f),
                        )

                        else -> InsightsScreen(
                            balances = balances,
                            memberNameById = memberNameById,
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
            val groupsForTitle by groupRepository.observeGroupsForUser(currentUserId)
                .collectAsState(initial = emptyList())
            val group = groupsForTitle.firstOrNull { it.groupId == groupId }
            val isOwner = members.firstOrNull { it.userId == currentUserId }?.role == Role.OWNER
            val activeMembersCount = members.count { it.active }
            val totalExpensePaise = expenses.sumOf { it.amountPaise }
            val transferSuggestions = remember(balances) { buildSettlementTransfers(balances) }

            var isFinalizingSettlement by rememberSaveable { mutableStateOf(false) }
            var infoMessage by remember { mutableStateOf("") }

            LaunchedEffect(infoMessage) {
                if (infoMessage.isNotBlank()) {
                    context.showShortToast(infoMessage)
                    infoMessage = ""
                }
            }

            Scaffold(
                topBar = {
                    TopAppBar(
                        title = { Text("Settlement Preview") },
                        navigationIcon = {
                            IconButton(onClick = { navController.popBackStack() }) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Back",
                                )
                            }
                        },
                    )
                },
            ) { paddingValues ->
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .consumeWindowInsets(paddingValues)
                        .padding(paddingValues)
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    item {
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text("PDF Summary")
                                HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp))
                                Text("Group: ${group?.name ?: groupId}")
                                Text("Active members: $activeMembersCount")
                                Text("Expense entries: ${expenses.size}")
                                Text("Total amount: ${totalExpensePaise.toRupeeDisplay()}")
                                Text("Open balances: ${balances.count { it.netPaise != 0L }}")
                            }
                        }
                    }

                    item {
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text("Suggested Transfers")
                                HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp))
                                if (transferSuggestions.isEmpty()) {
                                    Text("No transfer is needed. Balances are already settled.")
                                } else {
                                    transferSuggestions.forEach { transfer ->
                                        val payer = memberNameById[transfer.fromUserId]
                                            .orEmpty()
                                            .ifBlank { "Member" }
                                        val receiver = memberNameById[transfer.toUserId]
                                            .orEmpty()
                                            .ifBlank { "Member" }
                                        Text(
                                            "$payer pays ${transfer.amountPaise.toRupeeDisplay()} to $receiver",
                                            modifier = Modifier.padding(bottom = 6.dp),
                                        )
                                    }
                                }
                            }
                        }
                    }

                    item {
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text("Confirmation")
                                HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp))
                                Text(
                                    "This action will generate the settlement PDF, dispatch it to members, " +
                                        "and clear all expenses and balances for a fresh cycle.",
                                )
                                if (!isOwner) {
                                    Text(
                                        text = "Only an owner can confirm settlement.",
                                        modifier = Modifier.padding(top = 10.dp),
                                    )
                                } else if (expenses.isEmpty()) {
                                    Text(
                                        text = "No expenses to settle.",
                                        modifier = Modifier.padding(top = 10.dp),
                                    )
                                }

                                Button(
                                    onClick = {
                                        if (!isOwner) {
                                            infoMessage = "Only an owner can mark settlement."
                                            return@Button
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
                                            return@Button
                                        }
                                        if (isFinalizingSettlement) return@Button

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
                                                error?.let {
                                                    AppTelemetry.recordNonFatal(
                                                        throwable = it,
                                                        tags = mapOf("op" to "settlement_mark_group"),
                                                    )
                                                }
                                                return@launch
                                            }

                                            context.shareSettlementPdf(pdfPath)
                                            AppTelemetry.logEvent(
                                                "settlement_complete_success",
                                                mapOf(
                                                    "group_id" to groupId,
                                                    "entry_point" to "settlement_preview",
                                                ),
                                            )
                                            groupsMessage = "Settlement done. PDF generated and shared."
                                            isFinalizingSettlement = false
                                            navController.popBackStack()
                                        }
                                    },
                                    enabled = !isFinalizingSettlement && isOwner && expenses.isNotEmpty(),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 14.dp),
                                ) {
                                    Text(
                                        if (isFinalizingSettlement) {
                                            "Finalizing..."
                                        } else {
                                            "Generate PDF & Settle"
                                        },
                                    )
                                }

                                if (isFinalizingSettlement) {
                                    CircularProgressIndicator(modifier = Modifier.padding(top = 12.dp))
                                }
                            }
                        }
                    }
                }
            }
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
                                    mapOf("group_id" to (resultCreate.getOrNull()?.groupId ?: "unknown")),
                                )
                                resultCreate.getOrNull()?.groupId?.let { groupId ->
                                    navController.navigate(Destination.Group.buildRoute(groupId)) {
                                        popUpTo(Destination.Groups.route) { inclusive = false }
                                        launchSingleTop = true
                                    }
                                }
                            } else {
                                groupsMessage = resultCreate.exceptionOrNull()
                                    ?.toActionableMessage("Unable to create group.")
                                    .orEmpty()
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
                                source = JOIN_SOURCE_DRAWER,
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
                onDismiss = { pendingJoinClaimRequest = null },
                onJoin = { claimMemberUserId ->
                    pendingJoinClaimRequest = null
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
            AppTourDialog(
                onDismiss = {
                    context.setAppTourCompleted(true)
                    isTourCompleted = true
                    showAppTour = false
                },
            )
        }
    }
}

private data class CreateGroupDraft(
    val name: String,
    val description: String,
    val autoRenewInvite: Boolean,
    val selectAllMembersByDefaultForExpenses: Boolean,
)

@Composable
private fun CreateGroupDialog(
    onDismiss: () -> Unit,
    onCreate: (CreateGroupDraft) -> Unit,
) {
    var groupName by rememberSaveable { mutableStateOf("") }
    var groupDescription by rememberSaveable { mutableStateOf("") }
    var autoRenewInvite by rememberSaveable { mutableStateOf(true) }
    var selectAllMembersByDefaultForExpenses by rememberSaveable { mutableStateOf(false) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Create Group") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = groupName,
                    onValueChange = { groupName = it },
                    label = { Text("Group name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = groupDescription,
                    onValueChange = { groupDescription = it },
                    label = { Text("Group description") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    maxLines = 3,
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { autoRenewInvite = !autoRenewInvite },
                ) {
                    Checkbox(
                        checked = autoRenewInvite,
                        onCheckedChange = { autoRenewInvite = it },
                    )
                    Text(
                        text = "Auto-renew the invite code after expiry",
                        modifier = Modifier.padding(top = 12.dp, start = 8.dp),
                    )
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            selectAllMembersByDefaultForExpenses = !selectAllMembersByDefaultForExpenses
                        },
                ) {
                    Checkbox(
                        checked = selectAllMembersByDefaultForExpenses,
                        onCheckedChange = { selectAllMembersByDefaultForExpenses = it },
                    )
                    Text(
                        text = "Select all members by default in new expenses",
                        modifier = Modifier.padding(top = 12.dp, start = 8.dp),
                    )
                }
                Text(
                    text = "Leave the last option unchecked to start each expense with no participants selected.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val name = groupName.trim()
                    if (name.isNotBlank()) {
                        onCreate(
                            CreateGroupDraft(
                                name = name,
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
                enabled = groupName.trim().isNotBlank(),
            ) {
                Text("Create")
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
private fun GroupDetailsDialog(
    group: Group,
    isOwner: Boolean,
    onDismiss: () -> Unit,
    onSave: (description: String, autoRenewInvite: Boolean, selectAllMembersByDefaultForExpenses: Boolean) -> Unit,
    onRenewInvite: () -> Unit,
) {
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

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Group Details") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Name: ${group.name}")
                Text("Invite code: ${group.inviteCode}")
                Text("Invite expires: ${group.inviteExpiryEpochMs.toGroupDetailsDateTime()}")
                Text(
                    text = if (group.inviteExpiryEpochMs < System.currentTimeMillis()) {
                        "Invite status: Expired"
                    } else {
                        "Invite status: Active"
                    },
                )
                if (isOwner) {
                    TextButton(onClick = onRenewInvite) {
                        Text("Renew Invite Now")
                    }
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Group description") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2,
                        maxLines = 4,
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { autoRenewInvite = !autoRenewInvite },
                    ) {
                        Checkbox(
                            checked = autoRenewInvite,
                            onCheckedChange = { autoRenewInvite = it },
                        )
                        Text(
                            text = "Auto-renew invite after expiry",
                            modifier = Modifier.padding(top = 12.dp, start = 8.dp),
                        )
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                selectAllMembersByDefaultForExpenses = !selectAllMembersByDefaultForExpenses
                            },
                    ) {
                        Checkbox(
                            checked = selectAllMembersByDefaultForExpenses,
                            onCheckedChange = { selectAllMembersByDefaultForExpenses = it },
                        )
                        Text(
                            text = "Select all members by default in new expenses",
                            modifier = Modifier.padding(top = 12.dp, start = 8.dp),
                        )
                    }
                } else {
                    Text(
                        text = "Description: ${group.description.ifBlank { "No description added." }}",
                    )
                    Text(
                        text = "Invite auto-renew: ${if (group.autoRenewInvite) "On" else "Off"}",
                    )
                    Text(
                        text = "Expense default: ${
                            if (group.selectAllMembersByDefaultForExpenses) {
                                "All members selected"
                            } else {
                                "All members unselected"
                            }
                        }",
                    )
                }
            }
        },
        confirmButton = {
            if (isOwner) {
                Button(
                    onClick = {
                        onSave(
                            description.trim(),
                            autoRenewInvite,
                            selectAllMembersByDefaultForExpenses,
                        )
                    },
                ) {
                    Text("Save")
                }
            } else {
                TextButton(onClick = onDismiss) {
                    Text("Done")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(if (isOwner) "Cancel" else "Close")
            }
        },
    )
}

@Composable
private fun JoinGroupDialog(
    onDismiss: () -> Unit,
    onJoin: (inviteCode: String) -> Unit,
) {
    var inviteCode by rememberSaveable { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Join Group") },
        text = {
            OutlinedTextField(
                value = inviteCode,
                onValueChange = { inviteCode = it.toNormalizedInviteCode() },
                label = { Text("Invite code") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
        },
        confirmButton = {
            Button(
                onClick = {
                    val code = inviteCode.toNormalizedInviteCode()
                    if (code.isNotBlank()) {
                        onJoin(code)
                        inviteCode = ""
                    }
                },
                enabled = inviteCode.toNormalizedInviteCode().isNotBlank(),
            ) {
                Text("Join")
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
private fun ClaimExistingMemberDialog(
    groupName: String,
    claimableMembers: List<Member>,
    onDismiss: () -> Unit,
    onJoin: (claimMemberUserId: String?) -> Unit,
) {
    var selectedClaimMemberUserId by rememberSaveable(groupName) { mutableStateOf<String?>(null) }

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
                        .heightIn(max = 260.dp),
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
private fun AppTourDialog(
    onDismiss: () -> Unit,
) {
    data class AppTourStep(
        val title: String,
        val description: String,
    )
    val steps = remember {
        listOf(
            AppTourStep(
                title = "Groups",
                description = "Create or join groups from the drawer and share invites quickly.",
            ),
            AppTourStep(
                title = "Expenses",
                description = "Add expenses with equal, exact, percentage, or custom split styles.",
            ),
            AppTourStep(
                title = "Insights",
                description = "Use Ledger and Insights tabs to track balances and smart transfers.",
            ),
            AppTourStep(
                title = "Settlement",
                description = "Owners can preview, generate settlement PDF, and close the cycle.",
            ),
            AppTourStep(
                title = "Personal Tools",
                description = "Track self expenses, manage to-do tasks, and export reports.",
            ),
        )
    }
    var stepIndex by rememberSaveable { mutableIntStateOf(0) }
    val isLastStep = stepIndex == steps.lastIndex

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("App Tour") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Text(
                    text = "Step ${stepIndex + 1} of ${steps.size}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 150.dp),
                ) {
                    AnimatedContent(
                        targetState = stepIndex,
                        transitionSpec = {
                            if (targetState > initialState) {
                                (slideInHorizontally { it } + fadeIn()) togetherWith
                                    (slideOutHorizontally { -it / 3 } + fadeOut())
                            } else {
                                (slideInHorizontally { -it } + fadeIn()) togetherWith
                                    (slideOutHorizontally { it / 3 } + fadeOut())
                            }
                        },
                        label = "tour_card_slide",
                    ) { index ->
                        val step = steps[index]
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                        ) {
                            Text(
                                text = step.title,
                                style = MaterialTheme.typography.titleLarge,
                            )
                            Text(
                                text = step.description,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(top = 8.dp),
                            )
                        }
                    }
                }
                Text(
                    text = steps.indices.joinToString(" ") { index ->
                        if (index == stepIndex) "*" else "o"
                    },
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (isLastStep) {
                        onDismiss()
                    } else {
                        stepIndex += 1
                    }
                },
            ) {
                Text(if (isLastStep) "Finish" else "Next")
            }
        },
        dismissButton = {
            if (stepIndex == 0) {
                TextButton(onClick = onDismiss) {
                    Text("Skip")
                }
            } else {
                TextButton(onClick = { stepIndex -= 1 }) {
                    Text("Back")
                }
            }
        },
    )
}

@Composable
private fun AddMemberDialog(
    onDismiss: () -> Unit,
    onAdd: (memberName: String) -> Unit,
) {
    var memberName by rememberSaveable { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Member") },
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
                        onAdd(name)
                    }
                },
                enabled = memberName.trim().isNotBlank(),
            ) {
                Text("Add")
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
    val manageableMembers = members
        .filter { member -> member.active && member.userId != mainOwnerUserId }
        .sortedWith(compareByDescending<Member> { it.role == Role.OWNER }.thenBy { it.joinedAtEpochMs })

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Manage Members") },
        text = {
            if (manageableMembers.isEmpty()) {
                Text("No manageable members in this group.")
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 320.dp),
                ) {
                    items(manageableMembers, key = { it.userId }) { member ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(member.toFriendlyDisplayName())
                                Text(
                                    text = if (member.role == Role.OWNER) "Owner" else "Member",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End,
                                ) {
                                    if (isManualMemberUserId(member.userId)) {
                                        TextButton(onClick = { onEditClick(member) }) {
                                            Text("Edit")
                                        }
                                    }
                                    TextButton(
                                        onClick = { onToggleOwnerClick(member, member.role != Role.OWNER) },
                                    ) {
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
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Done")
            }
        },
    )
}

@Composable
private fun MembersListDialog(
    members: List<Member>,
    mainOwnerUserId: String,
    onDismiss: () -> Unit,
) {
    val activeMembers = members
        .filter { it.active }
        .sortedWith(
            compareByDescending<Member> { it.userId == mainOwnerUserId }
                .thenByDescending { it.role == Role.OWNER }
                .thenBy { it.joinedAtEpochMs },
        )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Group Members") },
        text = {
            if (activeMembers.isEmpty()) {
                Text("No active members in this group.")
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 320.dp),
                ) {
                    items(activeMembers, key = { it.userId }) { member ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                            ) {
                                Text(member.toFriendlyDisplayName())
                                Text(
                                    text = when {
                                        member.userId == mainOwnerUserId -> "Main Owner"
                                        member.role == Role.OWNER -> "Owner"
                                        else -> "Member"
                                    },
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Done")
            }
        },
    )
}

@Composable
private fun GroupOverflowMenu(
    isOwner: Boolean,
    canAddMember: Boolean,
    canViewMembers: Boolean,
    hasExpenses: Boolean,
    onMarkAsSettledClick: () -> Unit,
    onAddMemberClick: () -> Unit,
    onManageMembersClick: () -> Unit,
    onGroupDetailsClick: () -> Unit,
    onViewMembersClick: () -> Unit,
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
    ) {
        DropdownMenuItem(
            text = { Text("Mark as Settled") },
            enabled = isOwner && hasExpenses,
            onClick = {
                expanded = false
                onMarkAsSettledClick()
            },
        )
        DropdownMenuItem(
            text = { Text("Add Member") },
            enabled = canAddMember,
            onClick = {
                expanded = false
                onAddMemberClick()
            },
        )
        DropdownMenuItem(
            text = { Text("Manage Members") },
            enabled = isOwner,
            onClick = {
                expanded = false
                onManageMembersClick()
            },
        )
        DropdownMenuItem(
            text = { Text("Group Details") },
            enabled = canViewMembers,
            onClick = {
                expanded = false
                onGroupDetailsClick()
            },
        )
        DropdownMenuItem(
            text = { Text("List Members") },
            enabled = canViewMembers,
            onClick = {
                expanded = false
                onViewMembersClick()
            },
        )
        DropdownMenuItem(
            text = { Text("Delete Group") },
            enabled = isOwner,
            onClick = {
                expanded = false
                onDeleteGroupClick()
            },
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

private fun buildSettlementTransfers(balances: List<Balance>): List<SettlementTransfer> {
    val creditors = balances
        .filter { it.netPaise > 0L }
        .map { MutableSettlementParty(userId = it.userId, amountPaise = it.netPaise) }
        .sortedByDescending { it.amountPaise }
        .toMutableList()
    val debtors = balances
        .filter { it.netPaise < 0L }
        .map { MutableSettlementParty(userId = it.userId, amountPaise = abs(it.netPaise)) }
        .sortedByDescending { it.amountPaise }
        .toMutableList()

    val transfers = mutableListOf<SettlementTransfer>()
    var creditorIndex = 0
    var debtorIndex = 0
    while (creditorIndex < creditors.size && debtorIndex < debtors.size) {
        val creditor = creditors[creditorIndex]
        val debtor = debtors[debtorIndex]
        val amount = minOf(creditor.amountPaise, debtor.amountPaise)
        if (amount > 0L) {
            transfers += SettlementTransfer(
                fromUserId = debtor.userId,
                toUserId = creditor.userId,
                amountPaise = amount,
            )
        }
        creditor.amountPaise -= amount
        debtor.amountPaise -= amount
        if (creditor.amountPaise <= 0L) creditorIndex += 1
        if (debtor.amountPaise <= 0L) debtorIndex += 1
    }

    return transfers
}

private data class MutableSettlementParty(
    val userId: String,
    var amountPaise: Long,
)

private data class SettlementTransfer(
    val fromUserId: String,
    val toUserId: String,
    val amountPaise: Long,
)

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
    val abs = kotlin.math.abs(this)
    val rupees = abs / 100
    val paise = abs % 100
    val prefix = if (this < 0) "-₹" else "₹"
    return "$prefix$rupees.${paise.toString().padStart(2, '0')}"
}

private fun Long.toRupeeDisplay(): String {
    val abs = kotlin.math.abs(this)
    val rupees = abs / 100
    val paise = abs % 100
    val prefix = if (this < 0) "-\u20B9" else "\u20B9"
    return "$prefix$rupees.${paise.toString().padStart(2, '0')}"
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
    val webJoinLink = "https://letsgodutch.app/join/$inviteCode"
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

private fun Context.startChooser(baseIntent: Intent, title: String) {
    val chooser = Intent.createChooser(baseIntent, title)
    if (this !is Activity) {
        chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    startActivity(chooser)
}

private fun Context.showShortToast(message: String) {
    Toast.makeText(applicationContext, message, Toast.LENGTH_SHORT).show()
}

private fun Context.showRealtimeDbNotification(
    title: String,
    body: String,
    notificationIdHint: String?,
) {
    ensureLetsGoDutchNotificationChannel()

    if (
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
        ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
    ) {
        return
    }

    val launchIntent = Intent(this, MainActivity::class.java).apply {
        addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
    }
    val pendingIntent = PendingIntent.getActivity(
        this,
        0,
        launchIntent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
    )

    val notificationId = notificationIdHint?.hashCode() ?: System.currentTimeMillis().toInt()
    val builder = NotificationCompat.Builder(this, LetsGoDutchMessagingService.CHANNEL_ID)
        .setSmallIcon(R.drawable.ic_notification)
        .setContentTitle(title)
        .setContentText(body)
        .setStyle(NotificationCompat.BigTextStyle().bigText(body))
        .setAutoCancel(true)
        .setPriority(NotificationCompat.PRIORITY_HIGH)
        .setGroup(NOTIFICATION_GROUP_KEY_UPDATES)
        .setGroupAlertBehavior(NotificationCompat.GROUP_ALERT_SUMMARY)
        .setContentIntent(pendingIntent)

    val manager = NotificationManagerCompat.from(this)
    manager.notify(notificationId, builder.build())

    val summaryBuilder = NotificationCompat.Builder(this, LetsGoDutchMessagingService.CHANNEL_ID)
        .setSmallIcon(R.drawable.ic_notification)
        .setContentTitle(getString(R.string.app_name))
        .setContentText("You have new group updates.")
        .setStyle(
            NotificationCompat.InboxStyle()
                .setSummaryText("Group updates"),
        )
        .setGroup(NOTIFICATION_GROUP_KEY_UPDATES)
        .setGroupSummary(true)
        .setAutoCancel(true)
        .setContentIntent(pendingIntent)
    manager.notify(NOTIFICATION_GROUP_SUMMARY_ID, summaryBuilder.build())
}

private fun Context.ensureLetsGoDutchNotificationChannel() {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
    val manager = getSystemService(NotificationManager::class.java) ?: return
    val existing = manager.getNotificationChannel(LetsGoDutchMessagingService.CHANNEL_ID)
    if (existing != null) return

    val channel = NotificationChannel(
        LetsGoDutchMessagingService.CHANNEL_ID,
        getString(R.string.notification_channel_name),
        NotificationManager.IMPORTANCE_HIGH,
    ).apply {
        description = getString(R.string.notification_channel_desc)
    }
    manager.createNotificationChannel(channel)
}

private const val NOTIFICATION_GROUP_KEY_UPDATES = "letsgodutch_updates_group"
private const val NOTIFICATION_GROUP_SUMMARY_ID = 1001
private const val JOIN_SOURCE_DEEP_LINK = "deep_link"
private const val JOIN_SOURCE_DRAWER = "drawer"

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
