package com.example.onlineshop.ui.viewModel.user

import android.util.Patterns
import androidx.lifecycle.*
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
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
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val userAuth = auth.signIn(email, password)
                userAuth?.let {
                    val user = userRepo.getUser()
                    _loginResult.value = Result.success(user)
                } ?: run {
                    _loginResult.value = Result.failure(Exception("User not found"))
                }
            } catch (e: Exception) {
                _loginResult.value = Result.failure(e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun isUserLoggedIn(): LiveData<Boolean> {
        return MutableLiveData(auth.getCurruntUser() != null)
    }

    /*
    This function is designed to return a LiveData object that contains the current user information
    if the user is logged in, or null if no user is logged in.
     */
    fun getCurrentUser(): LiveData<User?> {
        val currentUser = auth.getCurruntUser()
        return liveData {
            if (currentUser != null) {
                emit(userRepo.getUser())
            } else {
                emit(null)
            }
        }
    }
}