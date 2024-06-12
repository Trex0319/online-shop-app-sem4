package com.example.onlineshop.data.repository.FirebaseImageStorage

import android.net.Uri
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.coroutines.tasks.await
import java.lang.Exception

class FirebaseImageStorageImpl(
    private val storage: StorageReference = FirebaseStorage.getInstance().reference
):FirebaseImageStorage {
    override suspend fun addImage(name: String, uri: Uri): String {
        storage.child(name).putFile(uri).await()   // Upload the image file to Firebase Storage
        val url = storage.child(name).downloadUrl.await()
        return url.toString() // Return the URL as a string
    }

    override suspend fun getImage(name: String): Uri? {
        return try {
            storage.child(name).downloadUrl.await() // To get the download URL for the image
        }catch (e:Exception) {
            e.printStackTrace()
            null // Return null if not error
        }
    }
}