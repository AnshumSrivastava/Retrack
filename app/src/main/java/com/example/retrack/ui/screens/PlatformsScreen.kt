package com.example.retrack.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.retrack.service.ContentClassifier
import com.example.retrack.ui.MainUiState

@Composable
fun PlatformsScreen(
    state: MainUiState,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    val platforms = listOf(
        Triple(ContentClassifier.PLATFORM_YOUTUBE, Icons.Default.VideoLibrary, "YouTube Shorts"),
        Triple(ContentClassifier.PLATFORM_INSTAGRAM, Icons.Default.PhotoCamera, "Instagram Reels"),
        Triple(ContentClassifier.PLATFORM_TIKTOK, Icons.Default.MusicNote, "TikTok"),
        Triple(ContentClassifier.PLATFORM_FACEBOOK, Icons.Default.Public, "Facebook Reels")
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Column {
            Text(
                text = "Platforms",
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Bold,
                letterSpacing = (-1).sp
            )
            Text(
                text = "Layout-driven short-form video monitoring",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

        platforms.forEachIndexed { index, (platformKey, icon, label) ->
            val count = state.platformBreakdown[platformKey] ?: 0

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = label,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(28.dp)
                    )
                    Column {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Monitored container layout",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Text(
                    text = "$count",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.ExtraBold
                )
            }

            if (index < platforms.size - 1) {
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            }
        }
    }
}
