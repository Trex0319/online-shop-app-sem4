package com.example.onlineshop.data.modal

import android.util.Log
import com.example.onlineshop.core.utils.Category

data class Product(
    var id: String? = null,
    var productName: String,
    var productInfo: String,
    var productPrice: String,
    var store: Int,
    var category: Category,
    var productImageUrl: String? = null
) {
    fun toHash(): Map<String, Any?> {
        return hashMapOf(
            "productName" to productName,
            "productInfo" to productInfo,
            "productPrice" to productPrice,
            "store" to store,
            "category" to category.categoryProductName,
            "productImageUrl" to productImageUrl
        )
    }

    companion object {
        fun fromMap(hash: Map<String, Any?>): Product {
            Log.d("hash","${hash}")
            return Product(
                id = hash["id"].toString(),
                productName = hash["productName"].toString(),
                productInfo = hash["productInfo"].toString(),
                productPrice = hash["productPrice"].toString(),
                store = hash["store"].toString().toInt(),
                category = when(hash["category"].toString()){
                    "Beauty" -> Category.Beauty
                    "Fashion" -> Category.Fashion
                    "Food" -> Category.Food
                    "Toys" -> Category.Toys
                    else -> Category.Others
                },
                productImageUrl = hash["productImageUrl"].toString()
            )
        }
    }
}
