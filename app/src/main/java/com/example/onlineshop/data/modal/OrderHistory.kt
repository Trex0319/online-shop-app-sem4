package com.example.onlineshop.data.modal

data class OrderHistory(
    val id: String? = null,
    val products: List<CartItem> = emptyList(),
    val totalPrice: String = "",
    val totalQuantity: Int = 0,
    val orderDate: Long = 0
) {

    fun toHash(): Map<String, Any?> {
        return hashMapOf(
            "products" to products.map { it.toHash() },
            "totalPrice" to totalPrice,
            "totalQuantity" to totalQuantity,
            "orderDate" to orderDate
        )
    }

    companion object {
        fun fromCart(cartItems: List<CartItem>, totalPrice: String, totalQuantity: Int): OrderHistory {
            val currentTimestamp: Long = System.currentTimeMillis()
            return OrderHistory(
                products = cartItems,
                totalPrice = totalPrice,
                totalQuantity = totalQuantity,
                orderDate = currentTimestamp
            )
        }
    }
}