package com.example.onlineshop.ui.fragment.user

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
import com.example.onlineshop.databinding.FragmentTabBinding
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
    private lateinit var tabLayout: TabLayout

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentTabBinding.inflate(
            layoutInflater, container, false
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
//                    findNavController().navigate(HomeFragmentDirections.homeToLogin())
                    findNavController().navigate(TabFragmentDirections.tabToLogin())
                }
            }
        }

        binding.vpTabs.adapter = TabAdapter(this, listOf(HomeFragment(),CartFragment(),OrderHistoryFragment(), ProfileFragment()))

        TabLayoutMediator(binding.tlTabs, binding.vpTabs) { tab, position ->
            when (position) {
                0 -> tab.text = "Home"
                1 -> tab.text = "Cart"
                2 -> tab.text = "Order"
                else -> tab.text = "Profile"
            }
        }.attach()
    }

    fun navigateToTab(position: Int) {
        viewPager.currentItem = position
    }
}