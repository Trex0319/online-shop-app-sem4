package com.example.onlineshop.data.repository.product

import com.example.onlineshop.data.modal.Product
import kotlinx.coroutines.flow.Flow

interface ProductRepo {
    suspend fun getAllProducts(): Flow<List<Product>>
    suspend fun getProductsByCategory(category: String): Flow<List<Product>>
    suspend fun getProductById(id: String): Product?

    suspend fun addNewProduct(product: Product): String
    suspend fun updateProduct(product: Product)
    suspend fun deleteProduct(id: String)
    suspend fun updateProductStock(productId: String, delta: Int)
}