package com.example.onlineshop.ui.fragment.user

import android.graphics.Color
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.widget.ViewPager2
import com.example.onlineshop.R
import com.example.onlineshop.databinding.FragmentTabBinding
import com.example.onlineshop.databinding.TabLayoutBinding
import com.example.onlineshop.ui.adapter.TabAdapter
import com.example.onlineshop.ui.viewModel.user.ProfileViewModel
import com.example.onlineshop.ui.viewModel.user.TabViewModel
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class TabFragment : Fragment() {
    private lateinit var binding: FragmentTabBinding
    private val viewModel: TabViewModel by viewModels()
    private val profileViewModel: ProfileViewModel by activityViewModels()
    private lateinit var viewPager: ViewPager2

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentTabBinding.inflate(
            layoutInflater,
            container,
            false
        )
        profileViewModel.getCurrentUser()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewLifecycleOwner.lifecycleScope.launch {
            profileViewModel.loggedOut.collect {
                if (it) {
                    viewModel.stopJob()
                    findNavController().navigate(TabFragmentDirections.tabViewToLogin())
                }
            }
        }

        viewPager = binding.vpTabs

        binding.vpTabs.adapter = TabAdapter(
            this,
            listOf(
                HomeFragment(),
                CartFragment(),
                OrderHistoryFragment(),
                ProfileFragment()
            )
        )
        val tabIcons = listOf(
            R.drawable.ic_home,
            R.drawable.ic_shopping_cart,
            R.drawable.ic_history,
            R.drawable.ic_profile
        )
        val tabTexts = listOf("Home", "Cart", "History", "Profile")
        TabLayoutMediator(binding.tlTabs, binding.vpTabs) { tab, position ->
            val tabBinding = TabLayoutBinding.inflate(LayoutInflater.from(requireContext()))
            tabBinding.tabIcon.setImageResource(tabIcons[position])
            tabBinding.tabText.text = tabTexts[position]
            tab.customView = tabBinding.root
        }.attach()
    }

    fun navigateToTab(position: Int) {
        viewPager.currentItem = position
    }
}