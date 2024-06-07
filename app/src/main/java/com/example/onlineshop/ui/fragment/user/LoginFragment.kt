package com.example.onlineshop.ui.fragment.user

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
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

        binding.btnLogin.setOnClickListener {
            val email = binding.etEmail.text.toString()
            val password = binding.etPassword.text.toString()
            viewModel.login(email, password)
        }

        viewModel.loginResult.observe(viewLifecycleOwner) { result ->
            result.onSuccess { user ->
                user?.let {
                    // Handle successful login
                    Toast.makeText(requireContext(), "Welcome, ${user.email}", Toast.LENGTH_SHORT).show()
                    if (user.isAdmin) {
                        findNavController().navigate(
                            LoginFragmentDirections.loginToAdminDashboard()
                        )
                    } else {
                        findNavController().navigate(
//                            LoginFragmentDirections.actionLoginFragmentToHomeFragment()
                            LoginFragmentDirections.actionLoginToTab()
                        )
                    }
                }
            }.onFailure { exception ->
                // Handle failed login
                Toast.makeText(requireContext(), "Login failed: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
        }

        binding.btnRegisterPage.setOnClickListener {
            findNavController().navigate(
                LoginFragmentDirections.actionLoginFragmentToRegisterFragment()
            )
        }
    }
}

