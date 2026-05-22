package com.example.ui.scan

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.api.RetrofitClient
import com.example.data.api.ScanStatus
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class ScanViewModel : ViewModel() {
    private val _scanStatus = MutableStateFlow<ScanStatus?>(null)
    val scanStatus: StateFlow<ScanStatus?> = _scanStatus.asStateFlow()

    init {
        startPolling()
    }

    private fun startPolling() {
        viewModelScope.launch {
            while (isActive) {
                try {
                    val response = RetrofitClient.api.getScanStatus()
                    if (response.isSuccessful) {
                        val status = response.body()?.data
                        _scanStatus.value = status
                    }
                } catch (e: Exception) {
                    // ignore mapping errors momentarily in poll loops
                }
                delay(2000) // Poll every 2 seconds
            }
        }
    }
}
