package com.example.onlineshop.ui.viewModel.user

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import android.util.Patterns
import androidx.lifecycle.MutableLiveData
import com.example.onlineshop.data.modal.User
import com.example.onlineshop.data.repository.authentication.UserAuthentication
import com.example.onlineshop.data.repository.user.UserRepo
import dagger.hilt.android.lifecycle.HiltViewModel


@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val authService: UserAuthentication,
    private val userRepo: UserRepo
) : ViewModel() {
    val snackbar: MutableLiveData<String?> = MutableLiveData()
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    fun register(name: String, email: String, phoneNumber: String, password: String, confirmPassword: String) {
        viewModelScope.launch(Dispatchers.IO) {
            // Validation
            val validationError = when {
                name.isEmpty() || email.isEmpty() || phoneNumber.isEmpty() || password.isEmpty() || confirmPassword.isEmpty() ->
                    "Please fill in all fields"
                !Patterns.EMAIL_ADDRESS.matcher(email).matches() ->
                    "Invalid email address format"
                password.length < 6 ->
                    "Password must be at least 6 characters"
                password != confirmPassword ->
                    "Password and Confirm Password do not match"
                else -> null
            }

            // If there are validation errors, post the error message to the snackbar
            if (validationError != null) {
                snackbar.postValue(validationError)
                return@launch
            }

            try {
                // Attempt to sign up the user
                val user = authService.signUp(email, password)
                if (user != null) {
                    snackbar.postValue("Register Successfully")

                    // Add the new user to the repository
                    userRepo.addNewUser(User(name = name, email = email, phoneNumber = phoneNumber))
                }
            } catch (e: Exception) {
                // If there's an exception, post the error message to the snackbar
                snackbar.postValue(e.message)
            }
        }
    }
}
