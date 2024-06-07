package com.example.onlineshop.data.repository.FirebaseImageStorage

import android.net.Uri

interface FirebaseImageStorage {
    suspend fun addImage(name:String,uri: Uri): String
    suspend fun getImage(name: String): Uri?
}