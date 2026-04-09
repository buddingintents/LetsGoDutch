package com.buddingintents.letsgodutch

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.buddingintents.letsgodutch.core.designsystem.theme.ThemeMode
import com.buddingintents.letsgodutch.core.model.UserProfile
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppScaffoldWithDrawer(
    currentUser: UserProfile?,
    navController: NavController,
    title: String,
    onSignOut: () -> Unit,
    onNavigateToTodo: () -> Unit,
    onNavigateToSelfExpenses: () -> Unit,
    onCreateGroupClick: () -> Unit,
    onJoinGroupClick: () -> Unit,
    onNavigateToSettings: () -> Unit,
    currentThemeMode: ThemeMode,
    onThemeModeChange: ((ThemeMode) -> Unit)?,
    modifier: Modifier = Modifier,
    topBarActions: @Composable RowScope.() -> Unit = {},
    floatingActionButton: @Composable (() -> Unit)? = null,
    content: @Composable () -> Unit,
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStackEntry?.destination?.route ?: "groups"

    ModalNavigationDrawer(
        drawerState = drawerState,
        scrimColor = MaterialTheme.colorScheme.scrim.copy(alpha = 0.48f),
        drawerContent = {
            ModalDrawerSheet(
                modifier = Modifier
                    .fillMaxHeight()
                    .widthIn(max = 344.dp),
                drawerContainerColor = MaterialTheme.colorScheme.surface,
                drawerContentColor = MaterialTheme.colorScheme.onSurface,
                drawerShape = MaterialTheme.shapes.large,
            ) {
                AppDrawerContent(
                    user = currentUser,
                    currentRoute = currentRoute,
                    onNavigateToGroups = {
                        scope.launch { drawerState.close() }
                        navController.navigate("groups") {
                            popUpTo(navController.graph.startDestinationId) { inclusive = true }
                            launchSingleTop = true
                        }
                    },
                    onNavigateToTodo = {
                        scope.launch { drawerState.close() }
                        onNavigateToTodo()
                    },
                    onNavigateToSelfExpenses = {
                        scope.launch { drawerState.close() }
                        onNavigateToSelfExpenses()
                    },
                    onCreateGroupClick = {
                        scope.launch { drawerState.close() }
                        onCreateGroupClick()
                    },
                    onJoinGroupClick = {
                        scope.launch { drawerState.close() }
                        onJoinGroupClick()
                    },
                    onSettingsClick = {
                        scope.launch { drawerState.close() }
                        onNavigateToSettings()
                    },
                    currentThemeMode = currentThemeMode,
                    onThemeModeChange = onThemeModeChange,
                    onSignOut = {
                        scope.launch { drawerState.close() }
                        onSignOut()
                    },
                )
            }
        },
        modifier = modifier,
    ) {
        Scaffold(
            containerColor = MaterialTheme.colorScheme.background,
            topBar = {
                TopAppBar(
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background,
                        titleContentColor = MaterialTheme.colorScheme.onBackground,
                        actionIconContentColor = MaterialTheme.colorScheme.onBackground,
                        navigationIconContentColor = MaterialTheme.colorScheme.onBackground,
                    ),
                    title = {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleLarge,
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(
                                imageVector = Icons.Default.Menu,
                                contentDescription = "Open menu",
                            )
                        }
                    },
                    actions = {
                        topBarActions()
                    },
                )
            },
            floatingActionButton = {
                floatingActionButton?.invoke()
            },
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .consumeWindowInsets(paddingValues)
                    .padding(paddingValues),
            ) {
                content()
            }
        }
    }
}
