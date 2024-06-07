package com.example.onlineshop.data.repository.order

import com.example.onlineshop.data.modal.OrderHistory
import kotlinx.coroutines.flow.Flow

interface OrderHistoryRepo {
    suspend fun addOrderHistory(userId: String, orderHistory: List<OrderHistory>)
    suspend fun getOrderHistory(userId: String): Flow<List<OrderHistory>>
}