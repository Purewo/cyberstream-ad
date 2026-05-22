package com.example.ui.scan

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.api.ScanStatus

@Composable
fun ScanProgressBanner(scanStatus: ScanStatus?) {
    val isScanning = scanStatus != null && scanStatus.status == "scanning"

    AnimatedVisibility(
        visible = isScanning,
        enter = expandVertically(expandFrom = Alignment.Top),
        exit = shrinkVertically(shrinkTowards = Alignment.Top)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "正在扫描: ${scanStatus?.currentSource ?: "未知源"}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = scanStatus?.phase ?: "",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Subtitle info
            val currentItem = scanStatus?.currentItem ?: scanStatus?.currentPath ?: ""
            if (currentItem.isNotBlank()) {
                Text(
                    text = currentItem,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(6.dp))
            }

            // Progress bar
            val totalItems = scanStatus?.totalItems ?: 0
            val processedItems = scanStatus?.processedItems ?: 0
            
            if (totalItems > 0) {
                val progress = processedItems.toFloat() / totalItems.toFloat()
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.fillMaxWidth().height(4.dp)
                )
                Text(
                    text = "$processedItems / $totalItems",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp).align(Alignment.End)
                )
            } else {
                LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth().height(4.dp)
                )
                val files = scanStatus?.discoveredFiles ?: scanStatus?.processedFiles ?: 0
                if (files > 0) {
                     Text(
                        text = "已发现文件: $files",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 4.dp).align(Alignment.End)
                    )
                }
            }
        }
    }
}
