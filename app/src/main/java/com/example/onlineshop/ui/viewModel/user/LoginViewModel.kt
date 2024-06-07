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
    private val userAuth: UserAuthentication,
    private val userRepo: UserRepo
) : ViewModel() {

    private val _loginResult = MutableLiveData<Result<User?>>()
    val loginResult: LiveData<Result<User?>> = _loginResult

    fun login(email: String, password: String) {
        viewModelScope.launch {
            try {
                val firebaseUser = userAuth.signIn(email, password)
                firebaseUser?.let {
                    val user = userRepo.getUser()
                    _loginResult.value = Result.success(user)
                } ?: run {
                    _loginResult.value = Result.failure(Exception("User not found"))
                }
            } catch (e: Exception) {
                _loginResult.value = Result.failure(e)
            }
        }
    }
}