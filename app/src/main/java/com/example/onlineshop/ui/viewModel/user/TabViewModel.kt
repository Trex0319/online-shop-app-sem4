package com.example.onlineshop.ui.viewModel.user

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch

class TabViewModel: ViewModel() {
    private val job = SupervisorJob()

    fun stopJob() {
        job.cancel()
    }
}