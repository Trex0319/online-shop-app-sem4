package com.example.onlineshop.ui.fragment.user

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.onlineshop.databinding.FragmentHomeBinding
import com.example.onlineshop.ui.adapter.ProductAdapter
import com.example.onlineshop.ui.viewModel.user.HomeViewModel

import com.example.onlineshopappgroupproject.activity.ui.fragment.admin.AdminDashboardFragmentDirections

import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
@AndroidEntryPoint
class HomeFragment : Fragment() {
    private lateinit var binding: FragmentHomeBinding
    private val viewModel: HomeViewModel by viewModels()
    private val profileViewModel: ProfileViewModel by activityViewModels()
    private lateinit var horizontalAdapter: HorizontalCategoryAdapter
    private lateinit var productAdapter: ProductAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        profileViewModel.getCurrentUser()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupAdapters()
        setupViewModelObservers()
    }

    private fun setupAdapters() {
        horizontalAdapter = HorizontalCategoryAdapter(viewModel.selectedCat.value.categoryProductName) { category ->
            viewModel.selectCategory(category)
        }

        binding.rvHorizontalCategories.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            adapter = horizontalAdapter
        }

        productAdapter = ProductAdapter(emptyList())
        productAdapter.listener = object : ProductAdapter.Listener {
            override fun onClick(product: Product) {
                product.id?.let {
                    findNavController().navigate(
                        TabFragmentDirections.tabToProductView(it)
                    )
                }
            }
        }


        binding.rvViewPopular.apply {
            layoutManager = GridLayoutManager(requireContext(), 2)
            adapter = productAdapter

        }
    }

    private fun setupViewModelObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.products.collect { products ->
                productAdapter.setProduct(products)
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.isLoading.collect { isLoading ->
                binding.progressBar.isVisible = isLoading
                binding.rvViewPopular.isVisible = !isLoading
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            profileViewModel.user.collect { user ->
                user?.let {
                    binding.tvCurrentUserName.text = it.name ?: "Unknown User"
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            profileViewModel.loggedOut.collect {
                if (it) {
                    viewModel.stopJob()
//                    findNavController().navigate(HomeFragmentDirections.homeToLogin())
                    findNavController().navigate(TabFragmentDirections.tabToLogin())
                }
            }
        }
    }
}
