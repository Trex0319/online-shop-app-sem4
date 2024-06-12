package com.example.onlineshop.ui.viewModel.user

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.onlineshop.data.modal.User
import com.example.onlineshop.data.repository.FirebaseImageStorage.FirebaseImageStorage
import com.example.onlineshop.data.repository.authentication.UserAuthentication
import com.example.onlineshop.data.repository.user.UserRepo
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import javax.inject.Inject
@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val auth: UserAuthentication,
    private val db: FirebaseFirestore,
    private val firebaseImageStorage: FirebaseImageStorage,
    private val userRepo: UserRepo
    ) : ViewModel() {
    private val _user = MutableStateFlow<User?>(null) // To hold the current user data (set default value = null)
    val user: StateFlow<User?> = _user// Expose as read-only
    private val _currentUser = MutableLiveData<User?>()
    val currentUser: LiveData<User?> = _currentUser // Expose as read-only LiveData
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading
    private val _loggedOut = MutableStateFlow(false)
    val loggedOut: StateFlow<Boolean> = _loggedOut

    // Initialize the ViewModel by fetching the getCurrentUser()
    init {
        getCurrentUser()
    }

    fun getCurrentUser() {
        viewModelScope.launch {
            val currentUser = auth.getCurrentUser() // Get the current user authenticated
            if (currentUser != null) {
                fetchUserProfile(currentUser.uid) // If no null, fetch the user profile if the user is authenticated
            } else {
                _loggedOut.emit(true) // If user are no authenticated, will emit logout state
            }
        }
    }
    fun uploadProfileImageUri(uri: Uri) {
        viewModelScope.launch(Dispatchers.IO) {
                _isLoading.value = true
                val currentUser = auth.getCurrentUser()
                currentUser?.let {
                    val url = firebaseImageStorage.addImage("user_${it.displayName}_profileImage_${it.uid}.jpg", uri) // Upload the profile image
                    val updatedUser = _user.value?.copy(profileUrl = url) // Update the user object with the new profile URL
                    updatedUser?.let { userRepo.updateUserDetail(it) }  // Update the user details
                    withContext(Dispatchers.Main) {
                        getCurrentUser() // Fetch the updated user profile on the main thread
                    }
                }
                _isLoading.value = false
        }
    }

    private suspend fun fetchUserProfile(uid: String) {
        val userInfo = db.collection("users").document(uid).get().await() // Fetch the document by userID from Firestore
        userInfo?.data?.let {
            _user.emit(User.fromHashMap(it)) // Emit the user data as a User object
        }
    }
    fun logout() {
        viewModelScope.launch {
            auth.logout() // Logout user using the firebase authentication
        }
    }

}