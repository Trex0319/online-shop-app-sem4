package com.example.onlineshop.data.repository.product

import android.util.Log
import com.example.onlineshop.data.modal.Product
import com.example.onlineshop.data.repository.authentication.UserAuthentication
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class ProductRepoImpl(
    private val auth: UserAuthentication,
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance(),
) : ProductRepo {

    private fun getDbReference (): CollectionReference {
        return db.collection("products")
    }

    override suspend fun getAllProducts() = callbackFlow {
        // callbackFlow to retrieve all products from Firestore
        val listener =
            getDbReference()
                .addSnapshotListener { value, error ->
                    if (error != null) {
                        throw error // If a error, throw it
                    }
                    val products = mutableListOf<Product>() // Create a list to hold products
                    value?.documents?.let { documents ->
                        for (document in documents) { // Process each document
                            document.data?.let { // Retrieve data from the document
                                it["id"] = document.id  // Add document ID to the data
                                products.add(Product.fromMap(it))// Convert data to Product object and add to list
                            }
                        }
                        trySend(products) // Try sending the list of products
                    }
                }
        awaitClose {
            listener.remove()  // When the flow is end, will remove the snapshot listener
        }
    }

    override suspend fun getProductsByCategory(category: String) = callbackFlow {
        // callbackFlow to retrieve products by category from Firestore
        val listener =
            getDbReference()
                .whereEqualTo("category", category)
                .addSnapshotListener { value, error ->
                    if (error != null) {
                        throw error
                    }
                    val products = mutableListOf<Product>() // Create a list to hold products
                    value?.documents?.let { documents ->
                        for (document in documents) {
                            document.data?.let {
                                it["id"] = document.id   // Add document ID to the data
                                products.add(Product.fromMap(it))// Convert data to Product object and add to list
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
        val document = getDbReference().document(id).get().await() // Retrieve document by ID
        return document.data?.let {// Convert document data to Product object
            it["id"] = document.id
            Product.fromMap(it)
        }
    }

    override suspend fun addNewProduct(product: Product): String {
        val document = getDbReference().add( // Add new document with product data to Firestore
            Product(
                productName = product.productName,
                productInfo = product.productInfo,
                productPrice = product.productPrice,
                store = product.store,
                category = product.category,
            ).toHash() // Convert Product object to HashMap
        ).await()
        return document.id
    }

    override suspend fun updateProduct(product: Product) {
        product.id.let {
            if (it.isNullOrEmpty()) throw Exception("Product id not found") // If ID is missing, throw an exception
            else getDbReference().document(it).set(product.copy()) // Ele's update document with new product data
            Log.d("Product","${product}")
        }
    }


    override suspend fun deleteProduct(id: String) {
        getDbReference().document(id).delete().await() // Delete document with specified ID
    }

    override suspend fun updateProductStock(productId: String, delta: Int) {
        val productReference = getDbReference().document(productId)  // Get specified ID to product document
        try {
            db.runTransaction { transaction ->  // Run a transaction to ensure data consistency
                val snapshot = transaction.get(productReference)
                val newStock = snapshot.getLong("store")!! + delta // Calculate new stock quantity
                transaction.update(productReference, "store", newStock)// Update stock quantity
            }.await()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

}