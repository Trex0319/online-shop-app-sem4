package com.example.onlineshop.ui.viewModel.user

import androidx.lifecycle.*
import com.example.onlineshop.data.modal.User
import com.example.onlineshop.data.repository.authentication.UserAuthentication
import com.example.onlineshop.data.repository.user.UserRepo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val auth: UserAuthentication,
    private val userRepo: UserRepo
) : ViewModel() {
    private val _loginResult = MutableLiveData<Result<User?>>()
    val loginResult: LiveData<Result<User?>> = _loginResult
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    fun login(email: String, password: String) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val userAuth = auth.signIn(email, password) // Attempt to sign in the user
                val user = userAuth?.let { userRepo.getUser() } // Get user data if sign-in is successful
                _loginResult.value = user?.let { Result.success(it) }
                    ?: Result.failure(Exception("User not found")) // Set failure result if user is null
            } catch (e: Exception) {
                _loginResult.value = Result.failure(e) // if the user email or password wrong will throw the error massage
            }
            _isLoading.value = false
        }
    }

    fun isUserLoggedIn(): LiveData<Boolean> {
        // Check a user is logged in, will return LiveData indicating whether a user is logged in
        return MutableLiveData(auth.getCurrentUser() != null)
    }

    /*
    This function is designed to return a LiveData object that contains the current user information
    if the user is logged in, or null if no user is logged in.
     */
    fun getCurrentUser(): LiveData<User?> {
        val currentUser = auth.getCurrentUser() // Get the current user
        return liveData {
            _isLoading.value = true
            if (currentUser != null) {
                emit(userRepo.getUser()) // Emit user data if user is logged in
            } else {
                emit(null) // If no emit null
            }
            _isLoading.value = false
        }
    }
}