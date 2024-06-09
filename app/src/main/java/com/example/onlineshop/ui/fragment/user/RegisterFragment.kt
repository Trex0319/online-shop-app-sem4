package com.example.onlineshop.ui.fragment.user

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.onlineshop.R
import com.example.onlineshop.databinding.FragmentRegisterBinding
import com.example.onlineshop.ui.viewModel.user.RegisterViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class RegisterFragment : Fragment() {
    private lateinit var binding: FragmentRegisterBinding
    private val viewModel: RegisterViewModel by viewModels()
    private var loadingView: View? = null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentRegisterBinding.inflate(
            layoutInflater,
            container,
            false
        )
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnRegister.setOnClickListener {
            val name = binding.etName.text.toString()
            val email = binding.etEmail.text.toString()
            val phoneNumber = binding.etPhoneNumber.text.toString()
            val password = binding.etPassword.text.toString()
            val confirmPassword = binding.etConfirmPassword.text.toString()

            viewModel.register(name, email, phoneNumber, password, confirmPassword)
        }

        lifecycleScope.launch {
            viewModel.isLoading.collect { isLoading ->
                handleLoadingState(isLoading)
            }
        }

        lifecycleScope.launch {
            viewModel.snackbar.observe(viewLifecycleOwner) { message ->
                message?.let {
                    Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
                }
            }
        }

        viewModel.snackbar.observe(viewLifecycleOwner) { message ->
            message?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
                if (it == "Register Successfully") {
                    findNavController().navigate(
                        RegisterFragmentDirections.registerToTabView()
                    )
                }
            }
        }

        binding.btnLoginPage.setOnClickListener {
            findNavController().navigate(
                RegisterFragmentDirections.registerToLogin()
            )
        }
    }
    private fun handleLoadingState(isLoading: Boolean) {
        if (isLoading) {
            showLoadingView()
        } else {
            hideLoadingView()
        }
    }

    private fun showLoadingView() {
        binding.loadingOverlay.visibility = View.VISIBLE
    }

    private fun hideLoadingView() {
        binding.loadingOverlay.visibility = View.GONE
    }

}