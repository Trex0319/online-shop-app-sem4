package com.example.onlineshop.data.modal

data class OrderHistory(
    val id: String? = null,
    val products: List<CartItem> = emptyList(),
    val totalPrice: String = "",
    val totalQuantity: Int = 0
) {

    fun toHash(): Map<String, Any?> {
        return hashMapOf(
            "products" to products.map { it.toHash() },
            "totalPrice" to totalPrice,
            "totalQuantity" to totalQuantity
        )
    }

    companion object {
        fun fromCart(cartItems: List<CartItem>, totalPrice: String, totalQuantity: Int) = OrderHistory(
            products = cartItems,
            totalPrice = totalPrice,
            totalQuantity = totalQuantity
        )
    }
}