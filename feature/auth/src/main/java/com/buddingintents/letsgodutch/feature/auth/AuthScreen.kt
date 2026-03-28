package com.buddingintents.letsgodutch.feature.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.Image
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AuthScreen(
    onGoogleSignInClick: () -> Unit,
    onAnonymousSignInClick: (displayName: String) -> Unit,
    message: String,
    anonymousNameHints: List<String> = emptyList(),
    logoResId: Int = R.drawable.ic_auth_brand,
    modifier: Modifier = Modifier,
) {
    var anonymousDisplayName by rememberSaveable { mutableStateOf("") }
    val normalizedHints = anonymousNameHints
        .map { it.trim() }
        .filter { it.isNotBlank() }
        .distinctBy { it.lowercase() }
        .take(5)

    LaunchedEffect(normalizedHints) {
        if (anonymousDisplayName.isBlank() && normalizedHints.isNotEmpty()) {
            anonymousDisplayName = normalizedHints.first()
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Image(
            painter = painterResource(id = logoResId),
            contentDescription = "Let's Go Dutch logo",
            modifier = Modifier.size(116.dp),
        )
        Spacer(modifier = Modifier.size(14.dp))
        Text(
            text = "Let's Go Dutch",
            style = MaterialTheme.typography.headlineMedium,
        )
        Text(
            text = "Track shared expenses with your group",
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(top = 8.dp, bottom = 20.dp),
        )
        Button(onClick = onGoogleSignInClick) {
            Text("Continue with Google")
        }
        Spacer(modifier = Modifier.size(12.dp))
        OutlinedTextField(
            value = anonymousDisplayName,
            onValueChange = { anonymousDisplayName = it },
            singleLine = true,
            label = { Text("Your name") },
            modifier = Modifier.fillMaxWidth(),
        )
        if (normalizedHints.isNotEmpty()) {
            Text(
                text = "Use previous name",
                style = MaterialTheme.typography.labelMedium,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp),
            )
            FlowRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                normalizedHints.forEach { hint ->
                    AssistChip(
                        onClick = { anonymousDisplayName = hint },
                        label = { Text(hint) },
                    )
                }
            }
        }
        Spacer(modifier = Modifier.size(8.dp))
        Button(
            onClick = { onAnonymousSignInClick(anonymousDisplayName.trim()) },
            enabled = anonymousDisplayName.trim().isNotBlank(),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Continue with Name")
        }
        if (message.isNotBlank()) {
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 16.dp),
            )
        }
    }
}
