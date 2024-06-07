package com.example.onlineshop.ui.viewModel.user

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.onlineshop.data.modal.CartItem
import com.example.onlineshop.data.modal.OrderHistory
import com.example.onlineshop.data.modal.Product
import com.example.onlineshop.data.repository.authentication.UserAuthentication
import com.example.onlineshop.data.repository.cart.CartRepo
import com.example.onlineshop.data.repository.order.OrderHistoryRepo
import com.example.onlineshop.data.repository.product.ProductRepo
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

@HiltViewModel
class CartViewModel @Inject constructor(
    private val cartRepo: CartRepo,
    private val productRepo: ProductRepo,
    private val orderHistoryRepo: OrderHistoryRepo,
    private val authService: UserAuthentication,
    private val db: FirebaseFirestore
) : ViewModel() {

    private val _cartItems = MutableLiveData<List<CartItem>>()
    val cartItems: LiveData<List<CartItem>> get() = _cartItems

    init {
        fetchCartItems()
    }

    private fun fetchCartItems() {
        viewModelScope.launch {
            val userId = authService.getUid()
            cartRepo.getCartItems(userId).collect { items ->
                _cartItems.postValue(items)
            }
        }
    }

//    private fun calculatePrice(cartItem: CartItem) {
//        return cartItem.productPrice.sumOf { total ->
//            (cartItem.productPrice * cartItem.quantity)
//        }
//    }


    fun addToCart(product: Product) {
        viewModelScope.launch {
            val cartItem = CartItem(
                productId = product.id ?: "",
                productName = product.productName,
                productPrice = product.productPrice,
                quantity = 1,
            )
            cartRepo.addToCart(cartItem)
            updateProductStock(product, -1)
        }
    }


    fun increaseQuantity(cartItem: CartItem) {
        viewModelScope.launch {
            val existingItem = _cartItems.value?.find { it.productId == cartItem.productId }
            if (existingItem != null) {
                existingItem.quantity += 1
                cartRepo.updateCartItem(existingItem)
                updateProductStock(existingItem.toProduct(), -1)
            } else {
                cartItem.quantity += 1
                cartRepo.addToCart(cartItem)
                updateProductStock(cartItem.toProduct(), -1)
            }
        }
    }


    fun decreaseQuantity(cartItem: CartItem) {
        viewModelScope.launch {
            if (cartItem.quantity == 1) {
                println("Removing cart item: ${cartItem.productId}")
                removeFromCart(cartItem)
            } else {
                cartItem.quantity -= 1
                println("Decreasing quantity of cart item: ${cartItem.productId}, new quantity: ${cartItem.quantity}")
                cartRepo.updateCartItem(cartItem)
                updateProductStock(cartItem.toProduct(), 1)
            }
        }
    }

    fun removeFromCart(cartItem: CartItem) {
        viewModelScope.launch {
            cartRepo.removeFromCart(cartItem.productId)
            val product = cartItem.toProduct()
            product?.let { updateProductStock(it, cartItem.quantity) }
        }
    }

//    fun checkout() {
//        viewModelScope.launch {
//            val userId = authService.getUid()
//            val cartItems = cartRepo.getCartItems(userId).first()
//            cartItems.forEach { cartItem ->
//                val orderHistoryItem = OrderHistory.fromCart(items, totalPrice, totalQuantity )
//                orderHistoryRepo.addOrderHistory(userId, listOf(orderHistoryItem))
//            }
//            cartRepo.clearCart(userId)
//        }
//    }

    fun checkout() {
        viewModelScope.launch {

                val userId = authService.getUid()
                val items = cartRepo.getCartItems(userId).first()
                if (items.isNotEmpty()) {
                    val totalQuantity = items.sumBy { it.quantity }
                    val totalPrice = items.sumByDouble { it.productPrice.toDouble() * it.quantity }.toString()

                    val orderHistory = OrderHistory.fromCart(items, totalPrice, totalQuantity)
                    orderHistoryRepo.addOrderHistory(userId, listOf(orderHistory))
                    cartRepo.clearCart(userId)
        }}
    }

    private suspend fun updateProductStock(product: Product, change: Int) {
        product.store += change
        productRepo.updateProduct(product)
    }

    private suspend fun addToOrderHistory(orderHistory: OrderHistory) {
        val userId = authService.getUid()
        db.collection("users").document(userId).collection("order_history")
            .add(orderHistory.toHash()).await()
    }
}
