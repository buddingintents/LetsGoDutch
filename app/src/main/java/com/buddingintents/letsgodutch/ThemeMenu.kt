package com.buddingintents.letsgodutch

import androidx.compose.foundation.background
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import com.buddingintents.letsgodutch.core.designsystem.theme.ThemeMode

@Composable
fun ThemeMenu(
    onThemeModeChange: (ThemeMode) -> Unit,
) {
    var expanded by androidx.compose.runtime.mutableStateOf(false)

    IconButton(onClick = { expanded = true }) {
        Icon(
            imageVector = Icons.Filled.MoreVert,
            contentDescription = "Theme options",
        )
    }
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = { expanded = false },
        modifier = Modifier
            .clip(MaterialTheme.shapes.medium)
            .background(MaterialTheme.colorScheme.surface),
    ) {
        DropdownMenuItem(
            text = { Text("Light theme") },
            onClick = {
                onThemeModeChange(ThemeMode.LIGHT)
                expanded = false
            },
        )
        DropdownMenuItem(
            text = { Text("Dark theme") },
            onClick = {
                onThemeModeChange(ThemeMode.DARK)
                expanded = false
            },
        )
        DropdownMenuItem(
            text = { Text("System default") },
            onClick = {
                onThemeModeChange(ThemeMode.SYSTEM)
                expanded = false
            },
        )
    }
}
