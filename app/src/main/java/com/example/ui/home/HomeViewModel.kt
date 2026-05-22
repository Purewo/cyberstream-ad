package com.example.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.api.HomepageData
import com.example.data.api.RetrofitClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class HomeUiState {
    object Loading : HomeUiState()
    data class Success(val data: HomepageData) : HomeUiState()
    data class Error(val message: String) : HomeUiState()
}

class HomeViewModel : ViewModel() {
    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        fetchHomepage()
    }

    fun fetchHomepage() {
        viewModelScope.launch {
            _uiState.value = HomeUiState.Loading
            try {
                val response = RetrofitClient.api.getHomepage()
                if (response.isSuccessful) {
                    val data = response.body()?.data
                    if (data != null) {
                        _uiState.value = HomeUiState.Success(data)
                    } else {
                        _uiState.value = HomeUiState.Error("获取数据失败")
                    }
                } else {
                    _uiState.value = HomeUiState.Error("错误代码: ${response.code()}")
                }
            } catch (e: Exception) {
                _uiState.value = HomeUiState.Error("网络错误: ${e.message}")
            }
        }
    }
}
