package com.example.ui.storage

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.api.AddStorageSourceRequest
import com.example.data.api.RetrofitClient
import com.example.data.api.StorageBrowseItem
import com.example.data.api.StoragePreviewRequest
import com.example.data.api.StorageProviderType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AddSourceViewModel : ViewModel() {
    private val _uiState = MutableStateFlow<AddSourceUiState>(AddSourceUiState.Loading)
    val uiState: StateFlow<AddSourceUiState> = _uiState.asStateFlow()

    private val _isSubmitting = MutableStateFlow(false)
    val isSubmitting: StateFlow<Boolean> = _isSubmitting.asStateFlow()

    private val _submitSuccess = MutableStateFlow(false)
    val submitSuccess: StateFlow<Boolean> = _submitSuccess.asStateFlow()

    private val _previewData = MutableStateFlow<com.example.data.api.StoragePreviewData?>(null)
    val previewData: StateFlow<com.example.data.api.StoragePreviewData?> = _previewData.asStateFlow()

    private val _previewError = MutableStateFlow<String?>(null)
    val previewError: StateFlow<String?> = _previewError.asStateFlow()

    private val _showBrowser = MutableStateFlow(false)
    val showBrowser: StateFlow<Boolean> = _showBrowser.asStateFlow()

    fun setShowBrowser(show: Boolean) {
        _showBrowser.value = show
    }

    init {
        fetchProviderTypes()
    }

    private fun fetchProviderTypes() {
        viewModelScope.launch {
            _uiState.value = AddSourceUiState.Loading
            try {
                val response = RetrofitClient.api.getProviderTypes()
                if (response.isSuccessful) {
                    val data = response.body()?.data ?: emptyList()
                    _uiState.value = AddSourceUiState.Success(data)
                } else {
                    _uiState.value = AddSourceUiState.Error("无法获取协议类型: ${response.code()}")
                }
            } catch (e: Exception) {
                _uiState.value = AddSourceUiState.Error(e.message ?: "未知错误")
            }
        }
    }

    private fun parseConfig(providerType: StorageProviderType, configValues: Map<String, String>): Map<String, Any> {
        val typedConfig = mutableMapOf<String, Any>()
        for ((key, value) in configValues) {
            if (value.isBlank()) continue
            val field = providerType.configFields.find { it.name == key }
            if (field != null) {
                when (field.type) {
                    "integer" -> {
                        val intVal = value.toIntOrNull()
                        if (intVal != null && intVal > 0) {
                            typedConfig[key] = intVal
                        }
                    }
                    "boolean" -> typedConfig[key] = value.toBooleanStrictOrNull() ?: false
                    else -> typedConfig[key] = value
                }
            } else {
                typedConfig[key] = value
            }
        }
        return typedConfig
    }

    fun previewSource(providerType: StorageProviderType, configValues: Map<String, String>, targetPath: String = "/") {
        viewModelScope.launch {
            _isSubmitting.value = true
            _previewError.value = null
            try {
                val typedConfig = parseConfig(providerType, configValues)
                val request = StoragePreviewRequest(
                    type = providerType.type,
                    config = typedConfig,
                    targetPath = targetPath,
                    dirsOnly = false
                )

                val response = RetrofitClient.api.previewStorage(request)
                if (response.isSuccessful) {
                    val resBody = response.body()
                    if (resBody?.code == 200) {
                        _previewData.value = resBody.data
                        _showBrowser.value = true
                    } else {
                        _previewError.value = resBody?.msg ?: "预览失败"
                    }
                } else {
                    _previewError.value = "连接失败: ${response.code()}"
                }
            } catch (e: Exception) {
                _previewError.value = "连接异常: ${e.message}"
            } finally {
                _isSubmitting.value = false
            }
        }
    }

    fun submitSource(name: String, providerType: StorageProviderType, configValues: Map<String, String>) {
        viewModelScope.launch {
            _isSubmitting.value = true
            try {
                val typedConfig = parseConfig(providerType, configValues)

                val request = AddStorageSourceRequest(
                    name = name,
                    type = providerType.type,
                    config = typedConfig
                )

                val response = RetrofitClient.api.addStorageSource(request)
                if (response.isSuccessful && response.body()?.code == 200) {
                    _submitSuccess.value = true
                } else {
                    // Just error state for now, could be shown as toast/snackbar later
                }
            } catch (e: Exception) {
                // Ignore for now
            } finally {
                _isSubmitting.value = false
            }
        }
    }
}

sealed class AddSourceUiState {
    object Loading : AddSourceUiState()
    data class Success(val providerTypes: List<StorageProviderType>) : AddSourceUiState()
    data class Error(val message: String) : AddSourceUiState()
}
