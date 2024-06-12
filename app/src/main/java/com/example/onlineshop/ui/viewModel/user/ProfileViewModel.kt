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
    private val _user = MutableStateFlow<User?>(null)
    val user: StateFlow<User?> = _user
    private val _currentUser = MutableLiveData<User?>()
    val currentUser: LiveData<User?> get() = _currentUser
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading
    private val _loggedOut = MutableStateFlow(false)
    val loggedOut: StateFlow<Boolean> = _loggedOut
    init {
        getCurrentUser()

    }
    fun getCurrentUser() {
        viewModelScope.launch {
            val currentUser = auth.getCurruntUser()
            if (currentUser != null) {
                fetchUserProfile(currentUser.uid)
            } else {
                _loggedOut.emit(true)
            }
        }
    }
    fun uploadProfileImageUri(uri: Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                _isLoading.value = true
                val currentUser = auth.getCurruntUser()
                currentUser?.let {
                    val url = firebaseImageStorage.addImage("user_profileImage_${it.uid}.jpg", uri)
                    val updatedUser = _user.value?.copy(profileUrl = url)
                    updatedUser?.let { userRepo.updateUserDetail(it) }
                    withContext(Dispatchers.Main) {
                        getCurrentUser()
                    }
                }
                _isLoading.value = false
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }


    private suspend fun fetchUserProfile(uid: String) {
        val doc = db.collection("users").document(uid).get().await()
        doc?.data?.let {
            _user.emit(User.fromHashMap(it))
        }
    }
    fun logout() {
        viewModelScope.launch {
            auth.logout()
        }
    }

}