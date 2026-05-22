package com.example.ui.storage

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.InsertDriveFile
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.api.StorageProviderType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddSourceScreen(
    onNavigateBack: () -> Unit,
    viewModel: AddSourceViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val isSubmitting by viewModel.isSubmitting.collectAsState()
    val submitSuccess by viewModel.submitSuccess.collectAsState()
    val previewData by viewModel.previewData.collectAsState()
    val previewError by viewModel.previewError.collectAsState()
    val showBrowser by viewModel.showBrowser.collectAsState()

    var name by remember { mutableStateOf("") }
    var selectedProviderIndex by remember { mutableStateOf(0) }
    var hasSelectedProvider by remember { mutableStateOf(false) }
    
    val configValues = remember { mutableStateMapOf<String, String>() }

    LaunchedEffect(submitSuccess) {
        if (submitSuccess) {
            onNavigateBack()
        }
    }

    if (showBrowser && previewData != null) {
        val currentProvider = (uiState as? AddSourceUiState.Success)?.providerTypes?.get(selectedProviderIndex)
        DirectoryBrowserScreen(
            previewData = previewData!!,
            isSubmitting = isSubmitting,
            onClose = { viewModel.setShowBrowser(false) },
            onNavigateUp = {
                val parent = previewData!!.parentPath
                if (parent != null && currentProvider != null) {
                    viewModel.previewSource(currentProvider, configValues.toMap(), parent)
                }
            },
            onNavigateToDir = { newPath ->
                if (currentProvider != null) {
                    viewModel.previewSource(currentProvider, configValues.toMap(), newPath)
                }
            },
            onSelectCurrentDir = {
                // Update config with this path. For config_root_key, we might just assume "path", "root_path", or "root".
                // Since providerType schema isn't fully providing config_root_key easily, we'll try common ones:
                val keys = listOf("path", "root_path", "root")
                keys.forEach { key ->
                    if (configValues.containsKey(key)) {
                        configValues[key] = previewData!!.currentPath
                    } else if (currentProvider?.configFields?.any { it.name == key } == true) {
                        configValues[key] = previewData!!.currentPath
                    }
                }
                viewModel.setShowBrowser(false)
            }
        )
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("添加资源库") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    val state = uiState
                    if (state is AddSourceUiState.Success && hasSelectedProvider && name.isNotBlank()) {
                        IconButton(
                            onClick = {
                                viewModel.submitSource(
                                    name,
                                    state.providerTypes[selectedProviderIndex],
                                    configValues.toMap()
                                )
                            },
                            enabled = !isSubmitting
                        ) {
                            Icon(Icons.Filled.Check, contentDescription = "Save")
                        }
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
            if (isSubmitting) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }

            when (val state = uiState) {
                is AddSourceUiState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                is AddSourceUiState.Error -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(text = state.message, color = MaterialTheme.colorScheme.error)
                    }
                }
                is AddSourceUiState.Success -> {
                    val providerTypes = state.providerTypes
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                    ) {
                        item {
                            OutlinedTextField(
                                value = name,
                                onValueChange = { name = it },
                                label = { Text("别名 (可选，自定义名)") },
                                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                                singleLine = true
                            )
                        }

                        item {
                            Text("存储协议", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(bottom = 8.dp))
                            
                            var expanded by remember { mutableStateOf(false) }
                            ExposedDropdownMenuBox(
                                expanded = expanded,
                                onExpandedChange = { expanded = it },
                                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
                            ) {
                                OutlinedTextField(
                                    value = if (hasSelectedProvider) providerTypes[selectedProviderIndex].displayName else "选择协议...",
                                    onValueChange = {},
                                    readOnly = true,
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                                    modifier = Modifier.menuAnchor().fillMaxWidth(),
                                    colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
                                )
                                ExposedDropdownMenu(
                                    expanded = expanded,
                                    onDismissRequest = { expanded = false }
                                ) {
                                    providerTypes.forEachIndexed { index, providerType ->
                                        DropdownMenuItem(
                                            text = { Text(providerType.displayName) },
                                            onClick = {
                                                selectedProviderIndex = index
                                                hasSelectedProvider = true
                                                expanded = false
                                                configValues.clear()
                                                // pre-fill defaults
                                                providerType.configFields.forEach { field ->
                                                    field.defaultVal?.let {
                                                        configValues[field.name] = it.toString()
                                                    }
                                                }
                                                if (name.isBlank()) {
                                                    name = providerType.displayName
                                                }
                                            }
                                        )
                                    }
                                }
                            }
                        }

                        if (hasSelectedProvider) {
                            val currentProvider = providerTypes[selectedProviderIndex]
                            item {
                                Text("配置项", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(bottom = 8.dp))
                            }
                            items(currentProvider.configFields) { field ->
                                val currentValue = configValues[field.name] ?: ""
                                if (field.type == "boolean") {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(text = field.name, style = MaterialTheme.typography.bodyLarge)
                                            if (!field.description.isNullOrEmpty()) {
                                                Text(
                                                    text = field.description,
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }
                                        Switch(
                                            checked = currentValue.toBooleanStrictOrNull() ?: false,
                                            onCheckedChange = { configValues[field.name] = it.toString() }
                                        )
                                    }
                                } else {
                                    OutlinedTextField(
                                        value = currentValue,
                                        onValueChange = { configValues[field.name] = it },
                                        label = { Text(field.name + if(field.required) " *" else "") },
                                        supportingText = field.description?.let { { Text(it) } },
                                        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                                        singleLine = true
                                    )
                                }
                            }
                            
                            item {
                                Spacer(modifier = Modifier.height(16.dp))
                                Button(
                                    onClick = { 
                                        viewModel.previewSource(currentProvider, configValues.toMap())
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    enabled = !isSubmitting
                                ) {
                                    Text("验证连接与预览目录")
                                }
                                
                                Spacer(modifier = Modifier.height(16.dp))
                                
                                if (previewError != null) {
                                    Text(
                                        text = previewError!!,
                                        color = MaterialTheme.colorScheme.error,
                                        modifier = Modifier.padding(bottom = 16.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DirectoryBrowserScreen(
    previewData: com.example.data.api.StoragePreviewData,
    isSubmitting: Boolean,
    onClose: () -> Unit,
    onNavigateUp: () -> Unit,
    onNavigateToDir: (String) -> Unit,
    onSelectCurrentDir: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Column {
                        Text("目录浏览", style = MaterialTheme.typography.titleMedium)
                        Text(
                            text = previewData.currentPath, 
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onClose) {
                        Icon(androidx.compose.material.icons.Icons.Filled.ArrowBack, contentDescription = "Close Browser")
                    }
                },
                actions = {
                    Button(
                        onClick = onSelectCurrentDir,
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Text("绑定此目录")
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
            if (isSubmitting) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize()
            ) {
                if (previewData.parentPath != null) {
                    item {
                        ListItem(
                            headlineContent = { Text(".. (上一级)") },
                            leadingContent = { 
                                Icon(
                                    imageVector = androidx.compose.material.icons.Icons.Filled.Folder,
                                    contentDescription = "Up",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            },
                            modifier = Modifier.clickable(!isSubmitting) { onNavigateUp() }
                        )
                        HorizontalDivider()
                    }
                }

                val items = previewData.items ?: emptyList()
                if (items.isEmpty()) {
                    item {
                        Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                            Text("空目录", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                } else {
                    items(items) { item ->
                        ListItem(
                            headlineContent = { Text(item.name) },
                            leadingContent = {
                                Icon(
                                    imageVector = if (item.type == "dir") androidx.compose.material.icons.Icons.Filled.Folder else androidx.compose.material.icons.Icons.Filled.InsertDriveFile,
                                    contentDescription = null,
                                    tint = if (item.type == "dir") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            },
                            modifier = Modifier.clickable(enabled = !isSubmitting && item.type == "dir") {
                                onNavigateToDir(item.path)
                            }
                        )
                        HorizontalDivider()
                    }
                }
            }
        }
    }
}
