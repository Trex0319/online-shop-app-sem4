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
    private val authService: UserAuthentication,
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) : OrderHistoryRepo {

    private fun getOrderHistoryReference (userId: String): CollectionReference {
        return db.collection("users").document(userId).collection("orders")
    }

    override suspend fun addOrderHistory(userId: String, orderHistory: List<OrderHistory>) {
        val batch = db.batch()
        val orderHistoryRef = getOrderHistoryReference(userId)
        orderHistory.forEach { order ->
            val docRef = orderHistoryRef.document()
            batch.set(docRef, order.toHash())
        }
        batch.commit().await()
    }

    override suspend fun getOrderHistory(userId: String): Flow<List<OrderHistory>> {
        return callbackFlow {
            val listener = getOrderHistoryReference(userId).addSnapshotListener { snapshot, _ ->
                val orders = snapshot?.toObjects(OrderHistory::class.java) ?: emptyList()
                trySend(orders).isSuccess
            }
            awaitClose { listener.remove() }
        }
    }
}