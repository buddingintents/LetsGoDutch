package com.buddingintents.letsgodutch.feature.groups

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.buddingintents.letsgodutch.core.model.Group
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView

/**
 * Groups list only - shows joined groups.
 * Create Group and Join Group are accessed via the navigation drawer.
 */
@Composable
fun GroupsListScreen(
    groups: List<Group>,
    onOpenGroup: (groupId: String) -> Unit,
    onShareGroupInvite: (group: Group) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
    ) {
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            items(groups, key = { it.groupId }) { group ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth(),
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { onOpenGroup(group.groupId) },
                        ) {
                            Text(
                                text = group.name,
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                            Text(
                                text = "Invite: ${group.inviteCode}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        Button(onClick = { onShareGroupInvite(group) }) {
                            Text("Share Link")
                        }
                    }
                }
            }
        }
        GroupsBannerAd(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
        )
    }
}

@Composable
private fun GroupsBannerAd(
    modifier: Modifier = Modifier,
) {
    val adWidthDp = LocalConfiguration.current.screenWidthDp.coerceAtLeast(320)

    key(adWidthDp) {
        AndroidView(
            modifier = modifier,
            factory = { viewContext ->
                AdView(viewContext).apply {
                    adUnitId = GROUPS_BANNER_AD_UNIT_ID
                    setAdSize(
                        AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(
                            viewContext,
                            adWidthDp,
                        ),
                    )
                    loadAd(AdRequest.Builder().build())
                }
            },
        )
    }
}

private const val GROUPS_BANNER_AD_UNIT_ID = "ca-app-pub-2020561089374332/8701762571"
