package com.example.onlineshop.ui.viewModel.user

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.onlineshop.data.modal.OrderHistory
import com.example.onlineshop.data.repository.authentication.UserAuthentication
import com.example.onlineshop.data.repository.order.OrderHistoryRepo
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OrderHistoryViewModel @Inject constructor(
    private val orderHistoryRepo: OrderHistoryRepo,
    private val auth: UserAuthentication,
    private val db: FirebaseFirestore
) : ViewModel() {
    private val _order = MutableLiveData<List<OrderHistory>>()
    val order: LiveData<List<OrderHistory>> get() = _order

    fun getOrderHistory() {
        viewModelScope.launch {
            val userId = auth.getUid()
            orderHistoryRepo.getOrderHistory(userId).collect {
                _order.postValue(it)
            }
        }
    }
}

