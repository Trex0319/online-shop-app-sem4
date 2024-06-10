package com.example.onlineshop.ui.fragment.user

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.onlineshop.R
import com.example.onlineshop.databinding.FragmentLoginBinding
import com.example.onlineshop.ui.viewModel.user.LoginViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LoginFragment : Fragment() {
    private lateinit var binding: FragmentLoginBinding
    private val viewModel: LoginViewModel by viewModels()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentLoginBinding.inflate(
            layoutInflater,
            container,
            false
        )
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Check if the user is already logged in
        viewModel.isUserLoggedIn().observe(viewLifecycleOwner) { isLoggedIn ->
            if (isLoggedIn) {
                viewModel.getCurrentUser().observe(viewLifecycleOwner) { user ->
                    user?.let {
                        navigateToNextScreen(it)
                    }
                }
            }
        }

        binding.btnLogin.setOnClickListener {
            val email = binding.etEmail.text.toString()
            val password = binding.etPassword.text.toString()
            viewModel.login(email, password)
        }

        viewModel.loginResult.observe(viewLifecycleOwner) { result ->
            result.onSuccess { user ->
                user?.let {
                    // Handle successful login
                    Toast.makeText(requireContext(), "Welcome, ${user.name}", Toast.LENGTH_SHORT).show()
                    if (user.isAdmin) {
                        findNavController().navigate(
                            LoginFragmentDirections.loginToAdminDashboard()
                        )
                    } else {
                        findNavController().navigate(
                            LoginFragmentDirections.loginToTabView()
                        )
                    }
                }
            }
            result.onFailure {
                // Handle login failure
                Toast.makeText(requireContext(), "Failed: ${it.message}", Toast.LENGTH_SHORT).show()
            }
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.loadingOverlay.isVisible = isLoading
        }

        binding.btnRegisterPage.setOnClickListener {
            findNavController().navigate(
                LoginFragmentDirections.loginToRegister()
            )
        }
    }

    /*
    This function is responsible for navigating the user to the appropriate screen after a successful login based on their role.
     */
    private fun navigateToNextScreen(user: com.example.onlineshop.data.modal.User) {
        if (user.isAdmin) {
            findNavController().navigate(
                LoginFragmentDirections.loginToAdminDashboard()
            )
        } else {
            findNavController().navigate(
                LoginFragmentDirections.loginToTabView()
            )
        }
    }
}


