package com.example.onlineshop.ui.fragment.user

import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.onlineshop.R
import com.example.onlineshop.databinding.FragmentProfileBinding
import com.example.onlineshop.ui.viewModel.user.ProfileViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ProfileFragment : Fragment() {
    private lateinit var binding: FragmentProfileBinding
    private val profileViewModel: ProfileViewModel by activityViewModels()
    private lateinit var pickImage: ActivityResultLauncher<PickVisualMediaRequest>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        pickImage = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
            uri?.let {
                profileViewModel.uploadProfileImageUri(uri)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentProfileBinding.inflate(inflater, container, false)
        profileViewModel.getCurrentUser()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupCurrentUserInfo()
        observeViewModel()
    }

    private fun setupCurrentUserInfo() {
        binding.profileViewModel = profileViewModel

        binding.btnUploadProfileImage.setOnClickListener {
            pickImage.launch(
                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
            )
        }

        binding.btnLogOut.setOnClickListener {
            profileViewModel.logout()
        }

        lifecycleScope.launch {
            profileViewModel.loggedOut.collect { loggedOut ->
                if (loggedOut) {
                    findNavController().navigate(
                        TabFragmentDirections.tabViewToLogin()
                    )
                }
            }
        }

        lifecycleScope.launch {
            profileViewModel.user.collect { user ->
                binding.run {
                    user?.let {
                        tvCurrentUserName.text = it.name
                        tvCurrentUserEmail.text = it.email
                        tvCurrentUserPhone.text = it.phoneNumber
                        Glide.with(requireView())
                            .load(it.profileUrl)
                            .placeholder(R.drawable.ic_person)
                            .into(binding.ivProfileImage)
                    }
                }
            }
        }
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            profileViewModel.isLoading.collect { isLoading ->
                binding.loadingOverlay.isVisible = isLoading
            }
        }
    }
}
