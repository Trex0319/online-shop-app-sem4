package com.example.onlineshop.data.modal

import com.example.onlineshop.core.utils.Category

data class CartItem(
    var id: String = "",
    val productId: String = "",
    val productName: String = "",
    val productInfo: String = "",
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

    fun toProduct(): Product {
        return Product(
            id = this.productId,
            productName = this.productName,
            productInfo = this.productInfo,
            productPrice = this.productPrice,
            store = 0,
            category = Category.Others,
            productImageUrl = this.productImageUrl
        )
    }
}
