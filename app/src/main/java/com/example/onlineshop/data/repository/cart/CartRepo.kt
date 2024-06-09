package com.example.onlineshop.data.repository.cart


import com.example.onlineshop.data.modal.CartItem
import kotlinx.coroutines.flow.Flow

interface CartRepo {
    suspend fun addToCart(cartItem: CartItem)
    suspend fun removeFromCart(cartItemId: String)
    suspend fun getCartItems(userId: String): Flow<List<CartItem>>
    suspend fun clearCart(userId: String)
    suspend fun updateCartItem(cartItem: CartItem)
}
