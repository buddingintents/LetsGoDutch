package com.buddingintents.letsgodutch.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.buddingintents.letsgodutch.core.designsystem.component.GradientButton
import com.buddingintents.letsgodutch.core.designsystem.component.SectionLabel
import com.buddingintents.letsgodutch.core.designsystem.theme.Charcoal
import com.buddingintents.letsgodutch.core.designsystem.theme.MintGlow
import com.buddingintents.letsgodutch.core.designsystem.theme.MintGreen
import com.buddingintents.letsgodutch.core.designsystem.theme.MintTeal
import com.buddingintents.letsgodutch.core.designsystem.theme.Night
import com.buddingintents.letsgodutch.core.designsystem.theme.NightSoft
import com.buddingintents.letsgodutch.core.designsystem.theme.TextOnDark

@Composable
fun AppTourOverlay(
    onDismiss: () -> Unit,
) {
    val contentColor = TextOnDark
    val mutedContentColor = contentColor.copy(alpha = 0.72f)
    val supportingContentColor = contentColor.copy(alpha = 0.62f)
    val subtleSurfaceColor = contentColor.copy(alpha = 0.06f)
    val subtleBorderColor = contentColor.copy(alpha = 0.10f)
    val scrimColor = Night.copy(alpha = 0.70f)
    val configuration = androidx.compose.ui.platform.LocalConfiguration.current
    val isLandscape = configuration.screenWidthDp > configuration.screenHeightDp
    val maxCardHeight = configuration.screenHeightDp.dp * if (isLandscape) 0.94f else 0.88f
    val maxCardWidth = if (isLandscape) 760.dp else 620.dp
    val overlayScrollState = rememberScrollState()
    val steps = remember {
        listOf(
            TourStep(
                icon = Icons.Default.Group,
                label = "Groups",
                title = "Start from the group hub",
                description = "Create or join a shared space quickly, then keep the active group list close at hand.",
                highlights = listOf(
                    "Create and join actions stay visible on the landing screen.",
                    "Share fresh invites without leaving the group card.",
                    "Owners and active invite states are easier to scan.",
                ),
            ),
            TourStep(
                icon = Icons.Default.AccountBalanceWallet,
                label = "Expenses",
                title = "Add expenses without friction",
                description = "Capture an expense fast, pick the payer, and split it the way the group actually agreed.",
                highlights = listOf(
                    "Equal, exact, percentage, and custom splits stay available.",
                    "Participant selection stays close to the amount entry flow.",
                    "The app stays focused on INR-first shared-expense use.",
                ),
            ),
            TourStep(
                icon = Icons.AutoMirrored.Filled.List,
                label = "Insights",
                title = "Read balances before anyone asks",
                description = "Use the ledger and insights views together to understand who owes, who receives, and what to settle next.",
                highlights = listOf(
                    "Ledger history stays separate from balance interpretation.",
                    "Settlement suggestions remain one step away.",
                    "Group-level financial context stays readable in light and dark themes.",
                ),
            ),
            TourStep(
                icon = Icons.AutoMirrored.Filled.HelpOutline,
                label = "Settlement",
                title = "Close the cycle deliberately",
                description = "Owners can review the settlement preview, generate the PDF, and settle the group with more confidence.",
                highlights = listOf(
                    "Settlement remains an explicit owner flow.",
                    "The PDF preview remains part of the confirmation sequence.",
                    "The app favors clear review over one-tap finalization.",
                ),
            ),
            TourStep(
                icon = Icons.Default.Settings,
                label = "Personal Tools",
                title = "Keep your own finances tidy too",
                description = "Track self expenses, manage to-do tasks, and keep your display name aligned with the groups you use.",
                highlights = listOf(
                    "Personal tools stay available without crowding group flows.",
                    "Settings remain the place for theme and tour resets.",
                    "Your account layer is easier to understand before deeper screen redesigns land.",
                ),
            ),
        )
    }
    var stepIndex by rememberSaveable { mutableIntStateOf(0) }
    val isLastStep = stepIndex == steps.lastIndex

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = false,
            dismissOnClickOutside = false,
        ),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(scrimColor)
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .padding(horizontal = 20.dp, vertical = 28.dp),
            contentAlignment = Alignment.Center,
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = maxCardWidth)
                    .heightIn(max = maxCardHeight),
                shape = RoundedCornerShape(28.dp),
                color = Color.Transparent,
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
                        .verticalScroll(overlayScrollState)
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
                            SectionLabel(text = "Let's Go Dutch Tour")
                            Text(
                                text = "Know your app",
                                style = MaterialTheme.typography.headlineSmall,
                                color = contentColor,
                            )
                            Text(
                                text = "A quick walkthrough of the screens you will use most to split, track, and settle cleanly.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = mutedContentColor,
                            )
                        }
                        Surface(
                            shape = MaterialTheme.shapes.medium,
                            color = subtleSurfaceColor,
                            border = BorderStroke(1.dp, subtleBorderColor),
                        ) {
                            Column(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                            ) {
                                Text(
                                    text = "${stepIndex + 1}/${steps.size}",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = contentColor,
                                    fontWeight = FontWeight.Bold,
                                )
                                Text(
                                    text = "steps",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = supportingContentColor,
                                )
                            }
                        }
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        TourProgressPills(
                            totalSteps = steps.size,
                            currentStepIndex = stepIndex,
                            modifier = Modifier
                                .fillMaxWidth()
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Text(
                                text = "Current focus",
                                style = MaterialTheme.typography.labelMedium,
                                color = supportingContentColor,
                            )
                            Text(
                                text = steps[stepIndex].label,
                                style = MaterialTheme.typography.labelMedium,
                                color = MintGreen,
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(24.dp),
                        color = subtleSurfaceColor,
                        border = BorderStroke(1.dp, subtleBorderColor),
                    ) {
                        AnimatedContent(
                            targetState = stepIndex,
                            transitionSpec = {
                                if (targetState > initialState) {
                                    (slideInHorizontally { it / 3 } + fadeIn()) togetherWith
                                        (slideOutHorizontally { -it / 5 } + fadeOut())
                                } else {
                                    (slideInHorizontally { -it / 3 } + fadeIn()) togetherWith
                                        (slideOutHorizontally { it / 5 } + fadeOut())
                                }
                            },
                            label = "tour_step_card",
                        ) { index ->
                            val step = steps[index]
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 18.dp, vertical = 18.dp),
                                verticalArrangement = Arrangement.spacedBy(14.dp),
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(18.dp),
                                        color = Color.Transparent,
                                        border = BorderStroke(1.dp, MintGlow),
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .background(
                                                    Brush.horizontalGradient(
                                                        listOf(MintGreen, MintTeal),
                                                    ),
                                                )
                                                .padding(horizontal = 14.dp, vertical = 10.dp),
                                            contentAlignment = Alignment.Center,
                                        ) {
                                            Icon(
                                                imageVector = step.icon,
                                                contentDescription = step.label,
                                                modifier = Modifier.size(20.dp),
                                                tint = contentColor,
                                            )
                                        }
                                    }
                                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                        SectionLabel(text = step.label)
                                        Text(
                                            text = step.title,
                                            style = MaterialTheme.typography.headlineSmall,
                                            color = contentColor,
                                        )
                                    }
                                }

                                Text(
                                    text = step.description,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = contentColor.copy(alpha = 0.74f),
                                )

                                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                    step.highlights.forEach { highlight ->
                                        TourHighlightRow(text = highlight)
                                    }
                                }
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        TextButton(
                            onClick = {
                                if (stepIndex == 0) onDismiss() else stepIndex -= 1
                            },
                        ) {
                            Text(
                                text = if (stepIndex == 0) "Skip" else "Back",
                                color = contentColor.copy(alpha = 0.64f),
                            )
                        }
                        GradientButton(
                            text = if (isLastStep) "Finish Tour" else "Next Step",
                            onClick = {
                                if (isLastStep) {
                                    onDismiss()
                                } else {
                                    stepIndex += 1
                                }
                            },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TourProgressPills(
    totalSteps: Int,
    currentStepIndex: Int,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        repeat(totalSteps) { index ->
            val isCompleted = index < currentStepIndex
            val isCurrent = index == currentStepIndex
            val pillWidth by animateDpAsState(
                targetValue = if (isCurrent) 72.dp else 24.dp,
                animationSpec = spring(),
                label = "tour_pill_width_$index",
            )
            val emphasis by animateFloatAsState(
                targetValue = when {
                    isCurrent -> 1f
                    isCompleted -> 0.78f
                    else -> 0.22f
                },
                animationSpec = spring(),
                label = "tour_pill_emphasis_$index",
            )
            val borderAlpha = when {
                isCurrent -> 0.95f
                isCompleted -> 0.55f
                else -> 0.18f
            }
            Surface(
                modifier = Modifier
                    .height(12.dp)
                    .width(pillWidth),
                shape = RoundedCornerShape(100.dp),
                color = Color.Transparent,
                border = BorderStroke(1.dp, MintGlow.copy(alpha = borderAlpha)),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            brush = when {
                                isCurrent -> Brush.horizontalGradient(
                                    listOf(MintGreen, MintTeal),
                                )
                                isCompleted -> Brush.horizontalGradient(
                                    listOf(
                                        MintGreen.copy(alpha = emphasis),
                                        MintTeal.copy(alpha = emphasis),
                                    ),
                                )
                                else -> Brush.horizontalGradient(
                                    listOf(
                                        TextOnDark.copy(alpha = emphasis),
                                        TextOnDark.copy(alpha = emphasis * 0.82f),
                                    ),
                                )
                            },
                        ),
                )
            }
        }
    }
}

@Composable
private fun TourHighlightRow(
    text: String,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Box(
            modifier = Modifier
                .padding(top = 6.dp)
                .width(8.dp)
                .height(8.dp)
                .clip(RoundedCornerShape(100.dp))
                .background(MintGreen),
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = TextOnDark.copy(alpha = 0.76f),
        )
    }
}

private data class TourStep(
    val icon: ImageVector,
    val label: String,
    val title: String,
    val description: String,
    val highlights: List<String>,
)
