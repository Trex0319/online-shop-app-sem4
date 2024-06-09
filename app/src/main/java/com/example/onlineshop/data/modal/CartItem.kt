package com.example.onlineshop.data.modal

import com.example.onlineshop.core.utils.Category

data class CartItem(
    var id: String = "",
    val productId: String = "",
    val productName: String = "",
    val productPrice: String = "",
    val productImageUrl: String = "",
    var quantity: Int = 1
) {
    fun toHash(): Map<String, Any> {
        return mapOf(
            "productId" to productId,
            "productName" to productName,
            "productPrice" to productPrice,
            "quantity" to quantity,
            "productImageUrl" to productImageUrl
        )
    }

//    fun toOrderHistory(): OrderHistory {
//        return OrderHistory(
//            productId = this.productId,
//            productName = this.productName,
//            productPrice = this.productPrice,
//            quantity = this.quantity,
//            productImageUrl = this.productImageUrl
//        )
//    }

    fun toProduct(): Product {
        return Product(
            id = this.productId,
            productName = this.productName,
            productInfo = "",  // Default value
            productPrice = this.productPrice,
            store = 0, // Assuming store quantity isn't tracked in the cart item and needs to be fetched from the product repository
            category = Category.Others,  // Default value
            productImageUrl = this.productImageUrl
        )
    }
}
