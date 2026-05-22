package com.example.ui.storage

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.api.RetrofitClient
import com.example.data.api.StorageSourceResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class StorageViewModel : ViewModel() {
    private val _uiState = MutableStateFlow<StorageUiState>(StorageUiState.Loading)
    val uiState: StateFlow<StorageUiState> = _uiState.asStateFlow()

    private val _isScanning = MutableStateFlow(false)
    val isScanning: StateFlow<Boolean> = _isScanning.asStateFlow()

    init {
        fetchSources()
    }

    fun fetchSources() {
        viewModelScope.launch {
            _uiState.value = StorageUiState.Loading
            try {
                val response = RetrofitClient.api.getStorageSources()
                if (response.isSuccessful) {
                    val data = response.body()?.data ?: emptyList()
                    _uiState.value = StorageUiState.Success(data)
                } else {
                    _uiState.value = StorageUiState.Error("获取失败: ${response.code()}")
                }
            } catch (e: Exception) {
                _uiState.value = StorageUiState.Error(e.message ?: "未知错误")
            }
        }
    }

    fun scanSource(id: Int) {
        viewModelScope.launch {
            _isScanning.value = true
            try {
                RetrofitClient.api.scanSource(id)
                // Just simulate scan time and refresh states locally without polling for now
                kotlinx.coroutines.delay(2000)
            } catch (e: Exception) {
                // Ignore error for now
            } finally {
                _isScanning.value = false
            }
        }
    }
}

sealed class StorageUiState {
    object Loading : StorageUiState()
    data class Success(val sources: List<StorageSourceResponse>) : StorageUiState()
    data class Error(val message: String) : StorageUiState()
}
