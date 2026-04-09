package com.buddingintents.letsgodutch

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
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
import androidx.compose.material3.NavigationDrawerItemColors
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.buddingintents.letsgodutch.core.designsystem.component.AvatarBadge
import com.buddingintents.letsgodutch.core.designsystem.component.SectionLabel
import com.buddingintents.letsgodutch.core.designsystem.theme.Charcoal
import com.buddingintents.letsgodutch.core.designsystem.theme.MintGreen
import com.buddingintents.letsgodutch.core.designsystem.theme.MintTeal
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
    var themeItemWidthPx by remember { mutableIntStateOf(0) }

    val photoUrl = user?.photoUrl?.takeIf { it.isNotBlank() }
    val displayName = user?.displayName?.trim()?.takeIf { it.isNotBlank() }
        ?: user?.email?.substringBefore("@")
        ?: "User"
    val email = user?.email?.trim()?.takeIf { it.isNotBlank() } ?: "Shared expenses, cleaner settlements"

    val itemColors = NavigationDrawerItemDefaults.colors(
        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
        unselectedContainerColor = androidx.compose.ui.graphics.Color.Transparent,
        selectedTextColor = MaterialTheme.colorScheme.onPrimaryContainer,
        unselectedTextColor = MaterialTheme.colorScheme.onSurface,
        selectedIconColor = MaterialTheme.colorScheme.primary,
        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
    )

    Column(
        modifier = modifier
            .background(MaterialTheme.colorScheme.surface)
            .verticalScroll(rememberScrollState()),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.linearGradient(listOf(MintTeal, Charcoal)))
                .padding(horizontal = 20.dp, vertical = 24.dp),
        ) {
            SectionLabel(text = "Let's Go Dutch")
            androidx.compose.foundation.layout.Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (!photoUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = photoUrl,
                        contentDescription = "Profile photo",
                        modifier = Modifier
                            .size(58.dp)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop,
                    )
                } else {
                    AvatarBadge(label = displayName, size = 58.dp)
                }
                Spacer(modifier = Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = displayName,
                        style = MaterialTheme.typography.titleLarge,
                        color = androidx.compose.ui.graphics.Color.White,
                    )
                    Text(
                        text = email,
                        style = MaterialTheme.typography.bodySmall,
                        color = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.72f),
                        modifier = Modifier.padding(top = 4.dp),
                    )
                }
            }
        }

        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.35f))

        SectionLabel(
            text = "Workspace",
            modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 18.dp, bottom = 8.dp),
        )
        DrawerActionItem(
            icon = { Icon(Icons.Default.Group, contentDescription = null) },
            label = "Your Groups",
            selected = currentRoute == "groups",
            onClick = onNavigateToGroups,
            colors = itemColors,
        )
        DrawerActionItem(
            icon = { Icon(Icons.AutoMirrored.Filled.List, contentDescription = null) },
            label = "To-Do Tasks",
            selected = currentRoute == "todo",
            onClick = onNavigateToTodo,
            colors = itemColors,
        )
        DrawerActionItem(
            icon = { Icon(Icons.Default.AccountBalanceWallet, contentDescription = null) },
            label = "Self Expenses",
            selected = currentRoute == "self_expenses",
            onClick = onNavigateToSelfExpenses,
            colors = itemColors,
        )
        DrawerActionItem(
            icon = { Icon(Icons.Default.Add, contentDescription = null) },
            label = "Create Group",
            selected = false,
            onClick = onCreateGroupClick,
            colors = itemColors,
        )
        DrawerActionItem(
            icon = { Icon(Icons.Default.GroupAdd, contentDescription = null) },
            label = "Join Group",
            selected = false,
            onClick = onJoinGroupClick,
            colors = itemColors,
        )

        if (onThemeModeChange != null) {
            SectionLabel(
                text = "Preferences",
                modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 8.dp),
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp),
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
                    shape = MaterialTheme.shapes.medium,
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
                        Modifier
                            .width(with(density) { themeItemWidthPx.toDp() })
                            .clip(MaterialTheme.shapes.medium)
                            .background(MaterialTheme.colorScheme.surface)
                    } else {
                        Modifier
                            .clip(MaterialTheme.shapes.medium)
                            .background(MaterialTheme.colorScheme.surface)
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

        Spacer(modifier = Modifier.size(16.dp))

        SectionLabel(
            text = "Account",
            modifier = Modifier.padding(start = 20.dp, end = 20.dp, bottom = 8.dp),
        )
        DrawerActionItem(
            icon = { Icon(Icons.Default.Settings, contentDescription = null) },
            label = "Settings",
            selected = currentRoute == "settings",
            onClick = onSettingsClick,
            colors = itemColors,
        )
        DrawerActionItem(
            icon = { Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null) },
            label = "Sign Out",
            selected = false,
            onClick = onSignOut,
            colors = itemColors,
        )
        Text(
            text = versionLabel,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(start = 20.dp, end = 20.dp, bottom = 16.dp, top = 8.dp),
        )
    }
}

@Composable
private fun DrawerActionItem(
    icon: @Composable () -> Unit,
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    colors: NavigationDrawerItemColors,
) {
    NavigationDrawerItem(
        icon = icon,
        label = { Text(label) },
        selected = selected,
        onClick = onClick,
        shape = MaterialTheme.shapes.medium,
        modifier = Modifier.padding(horizontal = 12.dp, vertical = 2.dp),
        colors = colors,
    )
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
