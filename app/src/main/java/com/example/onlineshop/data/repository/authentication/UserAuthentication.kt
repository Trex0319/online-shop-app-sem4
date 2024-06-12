package com.example.onlineshop.data.repository.authentication

import com.google.firebase.auth.FirebaseUser

interface UserAuthentication {
    suspend fun signUp(email:String, password:String): FirebaseUser?
    suspend fun signIn(email:String, password:String): FirebaseUser?
    fun getCurrentUser(): FirebaseUser?
    fun logout()
    fun getUid():String
}