package com.example.onlineshop.data.repository.order


import com.example.onlineshop.data.modal.OrderHistory
import com.example.onlineshop.data.repository.authentication.UserAuthentication
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class OrderHistoryRepoImpl(
    private val auth: UserAuthentication,
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) : OrderHistoryRepo {

    private fun getOrderHistoryReference (userId: String): CollectionReference {
        return db.collection("users").document(userId).collection("orders")
    }

    override suspend fun addOrderHistory(userId: String, orderHistory: List<OrderHistory>) {
        val batch = db.batch() // Create a batch operation for writes
        val orderHistoryReference = getOrderHistoryReference(userId)
        orderHistory.forEach { order ->
            val docReference = orderHistoryReference.document() // Generate a new document reference
            batch.set(docReference, order.toHash()) // Set the data of the document in the batch
        }
        batch.commit().await()  // Commit the batch operation to the Firestore database
    }

    override suspend fun getOrderHistory(userId: String): Flow<List<OrderHistory>> {
        return callbackFlow {
            val listener = getOrderHistoryReference(userId).addSnapshotListener { snapshot, _ ->
                val orders = snapshot?.toObjects(OrderHistory::class.java) ?: emptyList() // Convert snapshot to list of OrderHistory objects
                trySend(orders).isSuccess // Try to send the orders to the flow
            }
            awaitClose { listener.remove() }
        }
    }
}