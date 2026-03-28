package com.buddingintents.letsgodutch

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.GroupAdd
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SettingsSuggest
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.buddingintents.letsgodutch.core.designsystem.theme.ThemeMode
import com.buddingintents.letsgodutch.core.model.UserProfile

@Composable
fun AppDrawerContent(
    user: UserProfile?,
    currentRoute: String,
    onNavigateToGroups: () -> Unit,
    onNavigateToTodo: () -> Unit,
    onNavigateToSelfExpenses: () -> Unit,
    onCreateGroupClick: () -> Unit,
    onJoinGroupClick: () -> Unit,
    onSettingsClick: () -> Unit,
    currentThemeMode: ThemeMode,
    onThemeModeChange: ((ThemeMode) -> Unit)?,
    onSignOut: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val density = LocalDensity.current
    val versionLabel = remember(context.packageName) { context.resolveVersionLabel() }
    var showThemeMenu by rememberSaveable { mutableStateOf(false) }
    var themeItemWidthPx by remember { mutableStateOf(0) }
    Column(modifier = modifier) {
        // Header: user image and email
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.primaryContainer)
                .padding(24.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            val photoUrl = user?.photoUrl?.takeIf { it.isNotBlank() }
            val displayName = user?.displayName?.trim()?.takeIf { it.isNotBlank() }
                ?: user?.email?.substringBefore("@") ?: "User"
            val email = user?.email?.trim()?.takeIf { it.isNotBlank() } ?: ""

            if (!photoUrl.isNullOrBlank()) {
                AsyncImage(
                    model = photoUrl,
                    contentDescription = "Profile photo",
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop,
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = displayName.firstOrNull()?.uppercaseChar()?.toString() ?: "?",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = displayName,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                )
                if (email.isNotBlank()) {
                    Text(
                        text = email,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
                    )
                }
            }
        }

        HorizontalDivider()

        // Menu items
        val itemColors = NavigationDrawerItemDefaults.colors(
            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
            unselectedContainerColor = androidx.compose.ui.graphics.Color.Transparent,
            selectedTextColor = MaterialTheme.colorScheme.onSurface,
            unselectedTextColor = MaterialTheme.colorScheme.onSurface,
            selectedIconColor = MaterialTheme.colorScheme.primary,
            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        NavigationDrawerItem(
            icon = { Icon(Icons.Default.Group, contentDescription = null) },
            label = { Text("Your Groups") },
            selected = currentRoute == "groups",
            onClick = onNavigateToGroups,
            modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding),
            colors = itemColors,
        )

        NavigationDrawerItem(
            icon = { Icon(Icons.AutoMirrored.Filled.List, contentDescription = null) },
            label = { Text("To-Do Tasks") },
            selected = currentRoute == "todo",
            onClick = onNavigateToTodo,
            modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding),
            colors = itemColors,
        )

        NavigationDrawerItem(
            icon = { Icon(Icons.Default.AccountBalanceWallet, contentDescription = null) },
            label = { Text("Self Expenses") },
            selected = currentRoute == "self_expenses",
            onClick = onNavigateToSelfExpenses,
            modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding),
            colors = itemColors,
        )

        NavigationDrawerItem(
            icon = { Icon(Icons.Default.Add, contentDescription = null) },
            label = { Text("Create Group") },
            selected = false,
            onClick = onCreateGroupClick,
            modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding),
            colors = itemColors,
        )

        NavigationDrawerItem(
            icon = { Icon(Icons.Default.GroupAdd, contentDescription = null) },
            label = { Text("Join Group") },
            selected = false,
            onClick = onJoinGroupClick,
            modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding),
            colors = itemColors,
        )

        if (onThemeModeChange != null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(NavigationDrawerItemDefaults.ItemPadding),
            ) {
                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.Palette, contentDescription = null) },
                    label = { Text("Theme: ${currentThemeMode.label()}") },
                    badge = {
                        IconButton(onClick = { showThemeMenu = true }) {
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = "Change theme",
                            )
                        }
                    },
                    selected = false,
                    onClick = { showThemeMenu = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .onGloballyPositioned { coordinates ->
                            themeItemWidthPx = coordinates.size.width
                        },
                    colors = itemColors,
                )
                DropdownMenu(
                    expanded = showThemeMenu,
                    onDismissRequest = { showThemeMenu = false },
                    modifier = if (themeItemWidthPx > 0) {
                        Modifier.width(with(density) { themeItemWidthPx.toDp() })
                    } else {
                        Modifier
                    },
                ) {
                    DropdownMenuItem(
                        text = { Text("Light") },
                        leadingIcon = { Icon(Icons.Default.LightMode, contentDescription = null) },
                        onClick = {
                            onThemeModeChange(ThemeMode.LIGHT)
                            showThemeMenu = false
                        },
                    )
                    DropdownMenuItem(
                        text = { Text("Dark") },
                        leadingIcon = { Icon(Icons.Default.DarkMode, contentDescription = null) },
                        onClick = {
                            onThemeModeChange(ThemeMode.DARK)
                            showThemeMenu = false
                        },
                    )
                    DropdownMenuItem(
                        text = { Text("System") },
                        leadingIcon = { Icon(Icons.Default.SettingsSuggest, contentDescription = null) },
                        onClick = {
                            onThemeModeChange(ThemeMode.SYSTEM)
                            showThemeMenu = false
                        },
                    )
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        NavigationDrawerItem(
            icon = { Icon(Icons.Default.Settings, contentDescription = null) },
            label = { Text("Settings") },
            selected = currentRoute == "settings",
            onClick = onSettingsClick,
            modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding),
            colors = itemColors,
        )

        NavigationDrawerItem(
            icon = { Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null) },
            label = { Text("Sign Out") },
            selected = false,
            onClick = onSignOut,
            modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding),
            colors = itemColors,
        )
        Text(
            text = versionLabel,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(start = 20.dp, end = 20.dp, bottom = 14.dp),
        )
    }
}

private fun ThemeMode.label(): String = when (this) {
    ThemeMode.LIGHT -> "Light"
    ThemeMode.DARK -> "Dark"
    ThemeMode.SYSTEM -> "System"
}

private fun Context.resolveVersionLabel(): String {
    return runCatching {
        val packageInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            packageManager.getPackageInfo(packageName, PackageManager.PackageInfoFlags.of(0))
        } else {
            @Suppress("DEPRECATION")
            packageManager.getPackageInfo(packageName, 0)
        }
        val versionCode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            packageInfo.longVersionCode
        } else {
            @Suppress("DEPRECATION")
            packageInfo.versionCode.toLong()
        }
        "Version ${packageInfo.versionName} ($versionCode)"
    }.getOrElse {
        "Version unavailable"
    }
}
