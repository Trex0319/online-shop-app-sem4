package com.example.onlineshop.ui.viewModel.user

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.SupervisorJob

class TabViewModel: ViewModel() {
    private val job = SupervisorJob()

    fun stopJob() {
        job.cancel()
    }
}