package com.buddingintents.letsgodutch.feature.settlement

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun SettlementCard(
    isOwner: Boolean,
    modifier: Modifier = Modifier,
) {
    Card(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(
                text = "Settlement",
                style = MaterialTheme.typography.titleMedium,
            )
            Text(
                text = "Any owner can generate the PDF and clear transactions for a fresh cycle.",
                style = MaterialTheme.typography.bodyMedium,
            )
            Text(
                text = if (isOwner) {
                    "Use the top-right menu for group actions."
                } else {
                    "Only an owner can run settlement. Only the main owner can delete the group."
                },
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }
}
