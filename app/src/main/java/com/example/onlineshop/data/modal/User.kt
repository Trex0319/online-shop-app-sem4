package com.example.onlineshop.data.modal

import android.util.Log

data class User(
    val id:String? = null,
    val name:String,
    val email:String,
    val phoneNumber:String,
    var profileUrl: String? = null,
    val isAdmin: Boolean = false,
){
    fun toHash(): Map<String, Any?> {
        return hashMapOf(
            "name" to name,
            "email" to email,
            "phoneNumber" to phoneNumber,
            "profileUrl" to profileUrl,
            "isAdmin" to isAdmin,
        )
    }

    companion object{
        fun fromHashMap(hash: Map<String, Any?>): User {
            return User(
                id = hash["id"].toString(),
                name = hash["name"].toString(),
                email = hash["email"].toString(),
                phoneNumber = hash["phoneNumber"].toString(),
                profileUrl = hash["profileUrl"].toString(),
                isAdmin = hash["isAdmin"] as? Boolean ?: false,
            )
        }
    }
}