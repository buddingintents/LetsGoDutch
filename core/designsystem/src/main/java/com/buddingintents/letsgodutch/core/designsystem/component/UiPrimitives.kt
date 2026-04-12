package com.buddingintents.letsgodutch.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.buddingintents.letsgodutch.core.designsystem.theme.CoralSoft
import com.buddingintents.letsgodutch.core.designsystem.theme.MintGreen
import com.buddingintents.letsgodutch.core.designsystem.theme.MintGlow
import com.buddingintents.letsgodutch.core.designsystem.theme.MintTeal

@Composable
fun GradientButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    leadingContent: (@Composable RowScope.() -> Unit)? = null,
) {
    val shape = MaterialTheme.shapes.medium
    val brush = if (enabled) {
        Brush.horizontalGradient(listOf(MintGreen, MintTeal))
    } else {
        Brush.horizontalGradient(
            listOf(
                MaterialTheme.colorScheme.surfaceVariant,
                MaterialTheme.colorScheme.surfaceVariant,
            ),
        )
    }
    val contentColor = if (enabled) Color.White else MaterialTheme.colorScheme.onSurfaceVariant

    Box(
        modifier = modifier
            .shadow(
                elevation = if (enabled) 10.dp else 0.dp,
                shape = shape,
            )
            .clip(shape)
            .background(brush)
            .border(1.dp, if (enabled) MintGlow else Color.Transparent, shape)
            .clickable(
                enabled = enabled,
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick,
            )
            .defaultMinSize(minHeight = 52.dp)
            .padding(horizontal = 18.dp, vertical = 14.dp),
        contentAlignment = Alignment.Center,
    ) {
        CompositionLocalProvider(LocalContentColor provides contentColor) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterHorizontally),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                leadingContent?.invoke(this)
                Text(
                    text = text,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = contentColor,
                )
            }
        }
    }
}

@Composable
fun AvatarBadge(
    label: String,
    modifier: Modifier = Modifier,
    size: Dp = 44.dp,
) {
    val monogram = remember(label) { label.toAvatarMonogram() }
    val paletteOptions = listOf(
        AvatarBadgePalette(
            startColor = MintGreen,
            endColor = MintTeal,
            accentColor = MintGlow,
        ),
        AvatarBadgePalette(
            startColor = MaterialTheme.colorScheme.primary,
            endColor = MaterialTheme.colorScheme.tertiary,
            accentColor = MaterialTheme.colorScheme.tertiaryContainer,
        ),
        AvatarBadgePalette(
            startColor = CoralSoft,
            endColor = MaterialTheme.colorScheme.primary,
            accentColor = MaterialTheme.colorScheme.primaryContainer,
        ),
        AvatarBadgePalette(
            startColor = MaterialTheme.colorScheme.secondary,
            endColor = MintTeal,
            accentColor = MintGlow,
        ),
    )
    val paletteIndex = (label.trim().hashCode() and Int.MAX_VALUE) % paletteOptions.size
    val palette = paletteOptions[paletteIndex]
    Box(
        modifier = modifier
            .size(size)
            .shadow(8.dp, CircleShape)
            .clip(CircleShape)
            .background(
                Brush.linearGradient(
                    listOf(palette.startColor, palette.endColor),
                ),
            )
            .border(1.dp, Color.White.copy(alpha = 0.22f), CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(4.dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.18f),
                            Color.Transparent,
                        ),
                    ),
                ),
        )
        Text(
            text = monogram,
            color = Color.White,
            style = if (monogram.length > 1) {
                MaterialTheme.typography.titleSmall
            } else {
                MaterialTheme.typography.titleMedium
            },
            fontWeight = FontWeight.Bold,
        )
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(3.dp)
                .size(size * 0.24f)
                .clip(CircleShape)
                .background(palette.accentColor)
                .border(1.dp, Color.White.copy(alpha = 0.54f), CircleShape),
        )
    }
}

private data class AvatarBadgePalette(
    val startColor: Color,
    val endColor: Color,
    val accentColor: Color,
)

private fun String.toAvatarMonogram(): String {
    val parts = trim()
        .split(Regex("\\s+"))
        .filter { it.isNotBlank() }
    if (parts.isEmpty()) return "?"
    if (parts.size >= 2) {
        return buildString {
            append(parts[0].first().uppercaseChar())
            append(parts[1].first().uppercaseChar())
        }
    }
    val compact = parts[0].filter { it.isLetterOrDigit() }
    return compact
        .take(2)
        .uppercase()
        .ifBlank { "?" }
}

@Composable
fun SectionLabel(
    text: String,
    modifier: Modifier = Modifier,
) {
    Text(
        text = text.uppercase(),
        modifier = modifier,
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.primary,
    )
}

@Composable
fun PillTabSelector(
    tabs: List<String>,
    selectedIndex: Int,
    onSelectedIndexChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .clip(MaterialTheme.shapes.medium)
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        tabs.forEachIndexed { index, label ->
            val selected = index == selectedIndex
            val tabBrush = Brush.horizontalGradient(
                listOf(
                    if (selected) MintGreen else MaterialTheme.colorScheme.surfaceVariant,
                    if (selected) MintTeal else MaterialTheme.colorScheme.surfaceVariant,
                ),
            )
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(MaterialTheme.shapes.small)
                    .background(tabBrush)
                    .clickable { onSelectedIndexChange(index) }
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelLarge,
                    color = if (selected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}
