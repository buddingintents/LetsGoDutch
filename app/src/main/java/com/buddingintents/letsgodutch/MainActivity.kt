package com.buddingintents.letsgodutch

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.buddingintents.letsgodutch.core.designsystem.component.GradientButton
import com.buddingintents.letsgodutch.core.designsystem.theme.Charcoal
import com.buddingintents.letsgodutch.core.designsystem.theme.MintGreen
import com.buddingintents.letsgodutch.core.designsystem.theme.MintTeal
import com.buddingintents.letsgodutch.core.designsystem.theme.NightSoft
import com.buddingintents.letsgodutch.core.designsystem.theme.TextOnDark
import com.buddingintents.letsgodutch.core.designsystem.theme.LetsGoDutchTheme
import com.buddingintents.letsgodutch.core.designsystem.theme.ThemeMode
import com.buddingintents.letsgodutch.telemetry.AppTelemetry
import com.google.android.play.core.appupdate.AppUpdateInfo
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.appupdate.AppUpdateOptions
import com.google.android.play.core.install.InstallStateUpdatedListener
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability
import com.buddingintents.letsgodutch.theme.loadThemeMode
import com.buddingintents.letsgodutch.theme.saveThemeMode
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow

class MainActivity : ComponentActivity() {

    private val incomingInviteCode = MutableStateFlow<String?>(null)
    private val appUpdateStatus = MutableStateFlow<AppUpdateStatus>(AppUpdateStatus.Idle)
    private val appUpdatePrompt = MutableStateFlow<AppUpdatePrompt?>(null)
    private var playStoreInstallAvailable = false

    private lateinit var appUpdateManager: AppUpdateManager
    private var pendingFlexibleUpdateInfo: AppUpdateInfo? = null
    private val appUpdateResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) {
            refreshAppUpdateState(
                showUpToDateResult = false,
                promptIfAvailable = false,
                source = "update_flow_result",
            )
        }
    private val installStateListener = InstallStateUpdatedListener { state ->
        when (state.installStatus()) {
            InstallStatus.PENDING,
            InstallStatus.DOWNLOADING,
            InstallStatus.DOWNLOADED,
            InstallStatus.INSTALLING,
            InstallStatus.INSTALLED,
            InstallStatus.CANCELED,
            InstallStatus.FAILED,
            InstallStatus.UNKNOWN -> handleInstallStatus(state.installStatus())
            else -> Unit
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        captureInviteCodeFromIntent(intent)
        requestNotificationsPermissionIfNeeded()
        playStoreInstallAvailable = isInstalledFromGooglePlay()
        if (playStoreInstallAvailable) {
            appUpdateManager = AppUpdateManagerFactory.create(this)
            appUpdateManager.registerListener(installStateListener)
            refreshAppUpdateState(
                showUpToDateResult = false,
                promptIfAvailable = true,
                source = "app_start",
            )
        } else {
            appUpdateStatus.value = AppUpdateStatus.NotPlayInstalled
        }
        setContent {
            var showAnimatedSplash by rememberSaveable { mutableStateOf(true) }
            var splashExit by rememberSaveable { mutableStateOf(false) }
            LaunchedEffect(Unit) {
                delay(1550)
                splashExit = true
                delay(260)
                showAnimatedSplash = false
            }
            var themeMode by remember { mutableStateOf(loadThemeMode()) }
            val darkTheme = when (themeMode) {
                ThemeMode.LIGHT -> false
                ThemeMode.DARK -> true
                ThemeMode.SYSTEM -> isSystemInDarkTheme()
            }
            val currentAppUpdateStatus by appUpdateStatus.collectAsState()
            val currentAppUpdatePrompt by appUpdatePrompt.collectAsState()
            LetsGoDutchTheme(darkTheme = darkTheme) {
                SystemBarAppearanceEffect(
                    darkTheme = darkTheme,
                    activity = this@MainActivity,
                )
                Box(modifier = Modifier.fillMaxSize()) {
                    LetsGoDutchApp(
                        incomingInviteCode = incomingInviteCode,
                        onInviteCodeConsumed = { incomingInviteCode.value = null },
                        currentThemeMode = themeMode,
                        onThemeModeChange = { newMode ->
                            if (themeMode != newMode) {
                                AppTelemetry.logEvent(
                                    "theme_mode_change",
                                    mapOf("mode" to newMode.name.lowercase()),
                                )
                            }
                            themeMode = newMode
                            saveThemeMode(newMode)
                        },
                        appUpdateSummary = currentAppUpdateStatus.toSummaryText(),
                        isCheckingForAppUpdate = currentAppUpdateStatus == AppUpdateStatus.Checking,
                        isDownloadedUpdateReady = currentAppUpdateStatus == AppUpdateStatus.ReadyToInstall,
                        onCheckForAppUpdateClick = {
                            AppTelemetry.logEvent(
                                "app_update_check_requested",
                                mapOf("source" to "settings"),
                            )
                            refreshAppUpdateState(
                                showUpToDateResult = true,
                                promptIfAvailable = true,
                                source = "settings",
                            )
                        },
                        onInstallDownloadedUpdateClick = {
                            completeFlexibleAppUpdate(source = "settings_inline_update")
                        },
                        onOpenPlayStoreUpdateClick = {
                            openPlayStoreInlineInstall(source = "settings_inline_update")
                        },
                    )
                    if (!showAnimatedSplash) {
                        when (val prompt = currentAppUpdatePrompt) {
                            is AppUpdatePrompt.Available -> {
                                AppUpdateAvailableDialog(
                                    prompt = prompt,
                                    onDismiss = { appUpdatePrompt.value = null },
                                    onDownloadClick = {
                                        startFlexibleAppUpdate(source = "startup_update_prompt")
                                    },
                                    onOpenPlayStoreClick = {
                                        appUpdatePrompt.value = null
                                        openPlayStoreInlineInstall(source = "startup_update_prompt")
                                    },
                                )
                            }

                            AppUpdatePrompt.ReadyToInstall -> {
                                AppUpdateReadyDialog(
                                    onDismiss = { appUpdatePrompt.value = null },
                                    onInstallClick = {
                                        completeFlexibleAppUpdate(source = "startup_update_prompt")
                                    },
                                )
                            }

                            null -> Unit
                        }
                    }
                    if (showAnimatedSplash) {
                        AnimatedSplashOverlay(
                            isExiting = splashExit,
                        )
                    }
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        captureInviteCodeFromIntent(intent)
    }

    override fun onResume() {
        super.onResume()
        if (playStoreInstallAvailable) {
            refreshAppUpdateState(
                showUpToDateResult = false,
                promptIfAvailable = false,
                source = "resume",
            )
        }
    }

    override fun onDestroy() {
        if (::appUpdateManager.isInitialized) {
            appUpdateManager.unregisterListener(installStateListener)
        }
        super.onDestroy()
    }

    private fun requestNotificationsPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return
        val permission = Manifest.permission.POST_NOTIFICATIONS
        val granted = ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED
        if (granted) return
        ActivityCompat.requestPermissions(this, arrayOf(permission), NOTIFICATION_PERMISSION_REQUEST_CODE)
    }

    private fun refreshAppUpdateState(
        showUpToDateResult: Boolean,
        promptIfAvailable: Boolean,
        source: String,
    ) {
        if (!playStoreInstallAvailable) {
            appUpdatePrompt.value = null
            appUpdateStatus.value = AppUpdateStatus.NotPlayInstalled
            return
        }
        if (!::appUpdateManager.isInitialized) return
        appUpdateStatus.value = AppUpdateStatus.Checking
        appUpdateManager.appUpdateInfo
            .addOnSuccessListener { appUpdateInfo ->
                when {
                    appUpdateInfo.installStatus() == InstallStatus.DOWNLOADED -> {
                        pendingFlexibleUpdateInfo = null
                        appUpdateStatus.value = AppUpdateStatus.ReadyToInstall
                        appUpdatePrompt.value = AppUpdatePrompt.ReadyToInstall
                        AppTelemetry.logEvent(
                            "app_update_ready_to_install",
                            mapOf("source" to source),
                        )
                    }

                    appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE &&
                        appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE) -> {
                        pendingFlexibleUpdateInfo = appUpdateInfo
                        appUpdateStatus.value = AppUpdateStatus.Available(
                            stalenessDays = appUpdateInfo.clientVersionStalenessDays(),
                            updatePriority = appUpdateInfo.updatePriority(),
                        )
                        if (promptIfAvailable) {
                            appUpdatePrompt.value = AppUpdatePrompt.Available(
                                stalenessDays = appUpdateInfo.clientVersionStalenessDays(),
                                updatePriority = appUpdateInfo.updatePriority(),
                            )
                        }
                        AppTelemetry.logEvent(
                            "app_update_available",
                            mapOf(
                                "source" to source,
                                "staleness_days" to (appUpdateInfo.clientVersionStalenessDays() ?: -1),
                                "priority" to appUpdateInfo.updatePriority(),
                            ),
                        )
                    }

                    else -> {
                        pendingFlexibleUpdateInfo = null
                        appUpdatePrompt.value = null
                        appUpdateStatus.value = if (showUpToDateResult) {
                            AppUpdateStatus.UpToDate
                        } else {
                            AppUpdateStatus.Idle
                        }
                        if (showUpToDateResult) {
                            AppTelemetry.logEvent(
                                "app_update_up_to_date",
                                mapOf("source" to source),
                            )
                        }
                    }
                }
            }
            .addOnFailureListener { error ->
                pendingFlexibleUpdateInfo = null
                appUpdatePrompt.value = null
                appUpdateStatus.value = AppUpdateStatus.Error(
                    error.localizedMessage ?: "Google Play update check is unavailable on this device.",
                )
                AppTelemetry.logEvent(
                    "app_update_check_failed",
                    mapOf(
                        "source" to source,
                        "reason" to error.localizedMessage.orEmpty().ifBlank { "unknown" },
                    ),
                )
            }
    }

    private fun startFlexibleAppUpdate(source: String) {
        val appUpdateInfo = pendingFlexibleUpdateInfo
        if (appUpdateInfo == null) {
            refreshAppUpdateState(
                showUpToDateResult = false,
                promptIfAvailable = true,
                source = "${source}_refresh",
            )
            return
        }
        AppTelemetry.logEvent(
            "app_update_download_requested",
            mapOf("source" to source),
        )
        val updateStarted = runCatching {
            appUpdateManager.startUpdateFlowForResult(
                appUpdateInfo,
                appUpdateResultLauncher,
                AppUpdateOptions.newBuilder(AppUpdateType.FLEXIBLE).build(),
            )
        }.getOrElse { error ->
            appUpdateStatus.value = AppUpdateStatus.Error(
                error.localizedMessage ?: "Unable to start the update flow.",
            )
            AppTelemetry.logEvent(
                "app_update_download_failed",
                mapOf(
                    "source" to source,
                    "reason" to error.localizedMessage.orEmpty().ifBlank { "unknown" },
                ),
            )
            false
        }
        if (updateStarted) {
            appUpdatePrompt.value = null
            appUpdateStatus.value = AppUpdateStatus.Downloading
            AppTelemetry.logEvent(
                "app_update_download_started",
                mapOf("source" to source),
            )
        } else {
            refreshAppUpdateState(
                showUpToDateResult = false,
                promptIfAvailable = true,
                source = "${source}_retry",
            )
        }
    }

    private fun completeFlexibleAppUpdate(source: String) {
        AppTelemetry.logEvent(
            "app_update_install_requested",
            mapOf("source" to source),
        )
        appUpdateManager.completeUpdate()
            .addOnFailureListener { error ->
                appUpdateStatus.value = AppUpdateStatus.Error(
                    error.localizedMessage ?: "Unable to finish installing the update.",
                )
                AppTelemetry.logEvent(
                    "app_update_install_failed",
                    mapOf(
                        "source" to source,
                        "reason" to error.localizedMessage.orEmpty().ifBlank { "unknown" },
                    ),
                )
            }
    }

    private fun handleInstallStatus(status: Int) {
        when (status) {
            InstallStatus.PENDING,
            InstallStatus.DOWNLOADING,
            InstallStatus.INSTALLING -> {
                appUpdateStatus.value = AppUpdateStatus.Downloading
            }

            InstallStatus.DOWNLOADED -> {
                appUpdateStatus.value = AppUpdateStatus.ReadyToInstall
                appUpdatePrompt.value = AppUpdatePrompt.ReadyToInstall
                AppTelemetry.logEvent("app_update_ready_to_install")
            }

            InstallStatus.INSTALLED -> {
                pendingFlexibleUpdateInfo = null
                appUpdatePrompt.value = null
                appUpdateStatus.value = AppUpdateStatus.UpToDate
                AppTelemetry.logEvent("app_update_installed")
            }

            InstallStatus.CANCELED -> {
                appUpdatePrompt.value = null
                appUpdateStatus.value = AppUpdateStatus.Available()
                AppTelemetry.logEvent("app_update_canceled")
            }

            InstallStatus.FAILED,
            InstallStatus.UNKNOWN -> {
                appUpdatePrompt.value = null
                appUpdateStatus.value = AppUpdateStatus.Error(
                    "The app update could not be completed.",
                )
                AppTelemetry.logEvent("app_update_install_failed")
            }
        }
    }

    companion object {
        private const val NOTIFICATION_PERMISSION_REQUEST_CODE = 5001
    }

    private fun captureInviteCodeFromIntent(sourceIntent: Intent?) {
        val extractedInviteCode = sourceIntent.extractInviteCode()
        incomingInviteCode.value = extractedInviteCode
        if (!extractedInviteCode.isNullOrBlank()) {
            setIntent(
                Intent(sourceIntent).apply {
                    data = null
                    removeExtra("inviteCode")
                    removeExtra("code")
                },
            )
        }
    }
}

private sealed interface AppUpdateStatus {
    data object Idle : AppUpdateStatus
    data object Checking : AppUpdateStatus
    data object NotPlayInstalled : AppUpdateStatus
    data object UpToDate : AppUpdateStatus
    data object Downloading : AppUpdateStatus
    data object ReadyToInstall : AppUpdateStatus
    data class Available(
        val stalenessDays: Int? = null,
        val updatePriority: Int = 0,
    ) : AppUpdateStatus
    data class Error(val message: String) : AppUpdateStatus
}

private sealed interface AppUpdatePrompt {
    data class Available(
        val stalenessDays: Int?,
        val updatePriority: Int,
    ) : AppUpdatePrompt
    data object ReadyToInstall : AppUpdatePrompt
}

private fun AppUpdateStatus.toSummaryText(): String = when (this) {
    AppUpdateStatus.Idle -> "The app checks for Google Play updates automatically when it starts."
    AppUpdateStatus.Checking -> "Checking Google Play for a newer version..."
    AppUpdateStatus.NotPlayInstalled -> "Automatic Play updates are available after the app is installed from Google Play."
    AppUpdateStatus.UpToDate -> "You're already on the latest Google Play version."
    AppUpdateStatus.Downloading -> "An update is downloading in the background."
    AppUpdateStatus.ReadyToInstall -> "An update is downloaded and ready to install."
    is AppUpdateStatus.Available -> {
        val details = buildList {
            stalenessDays?.takeIf { it >= 0 }?.let { add("available for $it day(s)") }
            updatePriority.takeIf { it > 0 }?.let { add("priority $it") }
        }.joinToString()
        if (details.isBlank()) {
            "A Google Play update is available."
        } else {
            "A Google Play update is available ($details)."
        }
    }

    is AppUpdateStatus.Error -> message
}

@Composable
private fun SystemBarAppearanceEffect(
    darkTheme: Boolean,
    activity: MainActivity,
) {
    val view = LocalView.current
    SideEffect {
        WindowInsetsControllerCompat(activity.window, view).run {
            isAppearanceLightStatusBars = !darkTheme
            isAppearanceLightNavigationBars = !darkTheme
        }
    }
}

@Composable
private fun AppUpdateAvailableDialog(
    prompt: AppUpdatePrompt.Available,
    onDismiss: () -> Unit,
    onDownloadClick: () -> Unit,
    onOpenPlayStoreClick: () -> Unit,
) {
    val detailLine = buildList {
        prompt.stalenessDays?.takeIf { it >= 0 }?.let { add("Available for $it day(s)") }
        prompt.updatePriority.takeIf { it > 0 }?.let { add("Priority $it") }
    }.joinToString(" • ")

    MintUpdatePromptDialog(
        title = "Update Available",
        body = buildString {
            append("A newer version is available on Google Play.")
            if (detailLine.isNotBlank()) {
                append("\n\n")
                append(detailLine)
            }
        },
        confirmLabel = "Download Update",
        dismissLabel = "Later",
        tertiaryLabel = "Play Store",
        onDismiss = onDismiss,
        onConfirm = onDownloadClick,
        onTertiary = onOpenPlayStoreClick,
    )
}

@Composable
private fun AppUpdateReadyDialog(
    onDismiss: () -> Unit,
    onInstallClick: () -> Unit,
) {
    MintUpdatePromptDialog(
        title = "Install Update",
        body = "The latest version has finished downloading. Install it now to restart the app on the updated build.",
        confirmLabel = "Install Now",
        dismissLabel = "Later",
        onDismiss = onDismiss,
        onConfirm = onInstallClick,
    )
}

@Composable
private fun MintUpdatePromptDialog(
    title: String,
    body: String,
    confirmLabel: String,
    dismissLabel: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    tertiaryLabel: String? = null,
    onTertiary: (() -> Unit)? = null,
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = Charcoal,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(
                modifier = Modifier
                    .background(
                        Brush.linearGradient(
                            colors = listOf(MintTeal, NightSoft, Charcoal),
                        ),
                    )
                    .padding(horizontal = 20.dp, vertical = 20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                Text(
                    text = "Fresh Mint Update",
                    style = MaterialTheme.typography.labelLarge,
                    color = MintGreen,
                )
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineSmall,
                    color = TextOnDark,
                )
                Text(
                    text = body,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextOnDark.copy(alpha = 0.82f),
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                    ) {
                        Text(dismissLabel)
                    }
                    GradientButton(
                        text = confirmLabel,
                        onClick = onConfirm,
                        modifier = Modifier.weight(1f),
                    )
                }
                if (!tertiaryLabel.isNullOrBlank() && onTertiary != null) {
                    TextButton(onClick = onTertiary) {
                        Text(
                            text = tertiaryLabel,
                            color = TextOnDark,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AnimatedSplashOverlay(
    isExiting: Boolean,
    modifier: Modifier = Modifier,
) {
    var startAnimation by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(80)
        startAnimation = true
    }

    val overlayAlpha by animateFloatAsState(
        targetValue = if (isExiting) 0f else 1f,
        animationSpec = tween(durationMillis = 240, easing = FastOutSlowInEasing),
        label = "splash_overlay_alpha",
    )
    val logoAlpha by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0f,
        animationSpec = tween(durationMillis = 430, easing = FastOutSlowInEasing),
        label = "splash_logo_alpha",
    )
    val logoScale by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0.72f,
        animationSpec = tween(durationMillis = 760, easing = FastOutSlowInEasing),
        label = "splash_logo_scale",
    )
    val appNameAlpha by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0f,
        animationSpec = tween(durationMillis = 680, delayMillis = 210, easing = FastOutSlowInEasing),
        label = "splash_name_alpha",
    )
    val appNameOffsetY by animateFloatAsState(
        targetValue = if (startAnimation) 0f else 24f,
        animationSpec = tween(durationMillis = 680, delayMillis = 210, easing = FastOutSlowInEasing),
        label = "splash_name_offset_y",
    )
    val launchTitleContainerColor = if (isSystemInDarkTheme()) {
        MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.94f)
    } else {
        MaterialTheme.colorScheme.secondary.copy(alpha = 0.96f)
    }
    val launchTitleTextColor = if (isSystemInDarkTheme()) {
        MaterialTheme.colorScheme.onSecondaryContainer
    } else {
        MaterialTheme.colorScheme.onSecondary
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .alpha(overlayAlpha)
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.surface,
                        MaterialTheme.colorScheme.background,
                    ),
                ),
            ),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_app_icon_full),
                contentDescription = stringResource(id = R.string.app_name),
                modifier = Modifier
                    .size(132.dp)
                    .alpha(logoAlpha)
                    .graphicsLayer {
                        scaleX = logoScale
                        scaleY = logoScale
                    },
            )
            Spacer(modifier = Modifier.height(20.dp))
            Surface(
                modifier = Modifier
                    .alpha(appNameAlpha)
                    .graphicsLayer {
                        translationY = appNameOffsetY
                    },
                shape = MaterialTheme.shapes.extraLarge,
                color = launchTitleContainerColor,
                shadowElevation = 14.dp,
                tonalElevation = 4.dp,
            ) {
                Text(
                    text = stringResource(id = R.string.app_name),
                    style = MaterialTheme.typography.displaySmall.copy(
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = (-0.4).sp,
                    ),
                    color = launchTitleTextColor,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp),
                )
            }
        }
    }
}

private fun Intent?.extractInviteCode(): String? {
    val extraCode = sequenceOf(
        this?.getStringExtra("inviteCode"),
        this?.getStringExtra("code"),
    ).firstOrNull { !it.isNullOrBlank() }
    if (!extraCode.isNullOrBlank()) {
        return extraCode
            .trim()
            .uppercase()
            .filter { it.isLetterOrDigit() }
            .takeIf { it.isNotBlank() }
    }

    val uri: Uri = this?.data ?: return null
    val normalizedHost = uri.host.orEmpty().removePrefix("www.")
    val hasJoinPath = uri.path.orEmpty().startsWith("/join") ||
        uri.pathSegments.any { it.equals("join", ignoreCase = true) }

    val fromCustomScheme = uri.scheme.equals("letsgodutch", ignoreCase = true) &&
        (uri.host.equals("join", ignoreCase = true) || hasJoinPath)
    val fromWebLink = (uri.scheme.equals("https", ignoreCase = true) ||
        uri.scheme.equals("http", ignoreCase = true)) &&
        WEB_JOIN_LINK_HOSTS.any { host -> normalizedHost.equals(host, ignoreCase = true) } &&
        hasJoinPath
    if (!fromCustomScheme && !fromWebLink) return null

    val queryCode = uri.getQueryParameter("code").orEmpty()
    val hostPathCode = if (uri.host.equals("join", ignoreCase = true)) {
        uri.pathSegments.firstOrNull().orEmpty()
    } else {
        ""
    }
    val pathCode = uri.pathSegments
        .dropWhile { !it.equals("join", ignoreCase = true) }
        .drop(1)
        .firstOrNull()
        .orEmpty()
    val rawCode = when {
        queryCode.isNotBlank() -> queryCode
        hostPathCode.isNotBlank() -> hostPathCode
        else -> pathCode
    }
    return rawCode
        .trim()
        .uppercase()
        .filter { it.isLetterOrDigit() }
        .takeIf { it.isNotBlank() }
}
