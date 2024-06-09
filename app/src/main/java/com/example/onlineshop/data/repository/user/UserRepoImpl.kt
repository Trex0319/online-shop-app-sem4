package com.example.onlineshop.data.repository.user

import com.example.onlineshop.data.modal.User
import com.example.onlineshop.data.repository.authentication.UserAuthentication
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class UserRepoImpl(
    private val UserAuthentication: UserAuthentication,
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
): UserRepo {

    private fun getDbReference(): CollectionReference {
        return db.collection("users")
    }

    private fun getUid(): String {
        val firebaseUser = UserAuthentication.getCurruntUser()
        return firebaseUser?.uid ?: throw Exception("No user found")
    }

    override suspend fun getUser(): User? {
        val userId = getDbReference().document(getUid()).get().await()
        return userId.data?.let {
            it["id"] = userId.id
            User.fromHashMap(it)
        }
    }

    override suspend fun addNewUser(user: User) {
        getDbReference().document(getUid()).set(user.toHash()).await()
    }

    override suspend fun updateUserDetail(user: User) {
        val userId = getUid()
        getDbReference().document(userId).set(user.toHash()).await()
    }
}