package com.example.ui.storage

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.api.StorageSourceResponse

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StorageScreen(
    viewModel: StorageViewModel = viewModel(),
    onAddClick: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val isScanning by viewModel.isScanning.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("资源库") },
                actions = {
                    IconButton(onClick = onAddClick) {
                        Icon(Icons.Filled.Add, contentDescription = "Add Source")
                    }
                    IconButton(onClick = { viewModel.fetchSources() }) {
                        Icon(Icons.Filled.Refresh, contentDescription = "Refresh Overall")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (isScanning) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                Text(
                    text = "正在扫描中...",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                )
            }

            when (val state = uiState) {
                is StorageUiState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                is StorageUiState.Error -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(text = state.message, color = MaterialTheme.colorScheme.error)
                    }
                }
                is StorageUiState.Success -> {
                    if (state.sources.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text(text = "暂无挂载的资源库")
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            item { Spacer(modifier = Modifier.height(8.dp)) }
                            items(state.sources) { source ->
                                StorageCard(
                                    source = source,
                                    onScanClick = {
                                        viewModel.scanSource(source.id)
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StorageCard(source: StorageSourceResponse, onScanClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (source.type == "local") Icons.Filled.Folder else Icons.Filled.Cloud,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(40.dp)
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(text = source.name, style = MaterialTheme.typography.titleMedium)
                Text(
                    text = "${source.type.uppercase()} • ${
                        when(source.status) {
                            "online" -> "在线 / 已挂载"
                            "offline" -> "离线"
                            else -> "未知"
                        }
                    }", 
                    style = MaterialTheme.typography.bodySmall,
                    color = if (source.status == "online") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                )
            }
            
            Button(onClick = onScanClick, modifier = Modifier.padding(start = 8.dp)) {
                Text("扫描")
            }
        }
    }
}

