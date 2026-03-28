package com.buddingintents.letsgodutch

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.buddingintents.letsgodutch.core.designsystem.theme.LetsGoDutchTheme
import com.buddingintents.letsgodutch.core.designsystem.theme.ThemeMode
import com.buddingintents.letsgodutch.theme.loadThemeMode
import com.buddingintents.letsgodutch.theme.saveThemeMode
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow

class MainActivity : ComponentActivity() {

    private val incomingInviteCode = MutableStateFlow<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        var keepSystemSplash = true
        installSplashScreen().setKeepOnScreenCondition { keepSystemSplash }

        super.onCreate(savedInstanceState)
        incomingInviteCode.value = intent.extractInviteCode()
        requestNotificationsPermissionIfNeeded()
        enableEdgeToEdge()
        setContent {
            LaunchedEffect(Unit) {
                // Release the system splash quickly; custom Compose splash handles animation.
                keepSystemSplash = false
            }
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
            LetsGoDutchTheme(darkTheme = darkTheme) {
                Box(modifier = Modifier.fillMaxSize()) {
                    LetsGoDutchApp(
                        incomingInviteCode = incomingInviteCode,
                        onInviteCodeConsumed = { incomingInviteCode.value = null },
                        currentThemeMode = themeMode,
                        onThemeModeChange = { newMode ->
                            themeMode = newMode
                            saveThemeMode(newMode)
                        },
                    )
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
        incomingInviteCode.value = intent.extractInviteCode()
    }

    private fun requestNotificationsPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return
        val permission = Manifest.permission.POST_NOTIFICATIONS
        val granted = ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED
        if (granted) return
        ActivityCompat.requestPermissions(this, arrayOf(permission), NOTIFICATION_PERMISSION_REQUEST_CODE)
    }

    companion object {
        private const val NOTIFICATION_PERMISSION_REQUEST_CODE = 5001
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
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Image(
                painter = painterResource(id = R.mipmap.ic_launcher_foreground),
                contentDescription = stringResource(id = R.string.app_name),
                modifier = Modifier
                    .size(132.dp)
                    .alpha(logoAlpha)
                    .graphicsLayer {
                        scaleX = logoScale
                        scaleY = logoScale
                    },
            )
            Spacer(modifier = Modifier.height(14.dp))
            Text(
                text = stringResource(id = R.string.app_name),
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier
                    .alpha(appNameAlpha)
                    .graphicsLayer {
                        translationY = appNameOffsetY
                    },
            )
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
