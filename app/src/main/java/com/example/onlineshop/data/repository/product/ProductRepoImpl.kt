package com.example.onlineshop.data.repository.product

import android.util.Log
import com.example.onlineshop.data.modal.Product
import com.example.onlineshop.data.repository.user.UserRepo
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class ProductRepoImpl(
    private val authService: UserRepo,
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance(),
) : ProductRepo {

    private fun getDBRef(): CollectionReference {
        return db.collection("products")
    }

    override suspend fun getAllProducts() = callbackFlow {
        val listener =
            getDBRef()
                .addSnapshotListener { value, error ->
                    if (error != null) {
                        throw error
                    }
                    val products = mutableListOf<Product>()
                    value?.documents?.let { docs ->
                        for (doc in docs) {
                            doc.data?.let {
                                it["id"] = doc.id
                                products.add(Product.fromMap(it))
                            }
                        }
                        trySend(products)
                    }
                }
        awaitClose {
            listener.remove()
        }
    }

    override suspend fun getProductsByCategory(category: String) = callbackFlow {
        val listener =
            getDBRef()
                .whereEqualTo("category", category)
                .addSnapshotListener { value, error ->
                    if (error != null) {
                        throw error
                    }
                    val products = mutableListOf<Product>()
                    value?.documents?.let { docs ->
                        for (doc in docs) {
                            doc.data?.let {
                                it["id"] = doc.id
                                products.add(Product.fromMap(it))
                            }
                        }
                        trySend(products)
                    }
                }
        awaitClose {
            listener.remove()
        }
    }

    override suspend fun getProductById(id: String): Product? {
        val doc = getDBRef().document(id).get().await()
        return doc.data?.let {
            it["id"] = doc.id
            Product.fromMap(it)
        }
    }

    override suspend fun addNewProduct(product: Product): String {
        val doc = getDBRef().add(
            Product(
                productName = product.productName,
                productInfo = product.productInfo,
                productPrice = product.productPrice,
                store = product.store,
                category = product.category,
            ).toHash()
        ).await()
        return doc.id
    }

    override suspend fun updateProduct(product: Product) {
        product.id.let {
            if (it.isNullOrEmpty()) throw Exception("Product id not found")
            else getDBRef().document(it).set(product.copy())
            Log.d("Product","${product}")
        }
    }


    override suspend fun deleteProduct(id: String) {
        getDBRef().document(id).delete().await()
    }

    override suspend fun updateProductStock(productId: String, delta: Int) {
        val productRef = getDBRef().document(productId)
        db.runTransaction { transaction ->
            val snapshot = transaction.get(productRef)
            val newStock = snapshot.getLong("store")!! + delta
            transaction.update(productRef, "store", newStock)
        }.await()
    }
}