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
    val order: LiveData<List<OrderHistory>> = _order

    fun getOrderHistory() {
        viewModelScope.launch {
            val userId = auth.getUid()  // get the userID using Firestore authentication
            orderHistoryRepo.getOrderHistory(userId).collect { orderHistoryList ->
                // Sort the orderHistory list by orderDate in descending order
                val sortedOrderHistoryList = orderHistoryList.sortedByDescending { it.orderDate }
                // Update the MutableLiveData with the sortedOrderHistoryList
                _order.postValue(sortedOrderHistoryList)
            }
        }
    }
}

