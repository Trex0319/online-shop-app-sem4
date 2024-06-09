package com.example.onlineshop.ui.fragment.user

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SearchView
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.onlineshop.data.modal.Product
import com.example.onlineshop.databinding.FragmentHomeBinding
import com.example.onlineshop.ui.adapter.HorizontalCategoryAdapter
import com.example.onlineshop.ui.adapter.ProductAdapter
import com.example.onlineshop.ui.viewModel.user.HomeViewModel
import com.example.onlineshop.ui.viewModel.user.ProfileViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class HomeFragment : Fragment() {
    private lateinit var binding: FragmentHomeBinding
    private val viewModel: HomeViewModel by viewModels()
    private lateinit var originalProduct: List<Product>
    private val profileViewModel: ProfileViewModel by activityViewModels()
    private lateinit var horizontalAdapter: HorizontalCategoryAdapter
    private lateinit var productAdapter: ProductAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeBinding.inflate(
            inflater,
            container,
            false)
        profileViewModel.getCurrentUser()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupAdapters()
        setupViewModelObservers()
    }

    private fun setupAdapters() {
        horizontalAdapter = HorizontalCategoryAdapter(viewModel.selectedCategory.value.categoryProductName) { category ->
            viewModel.selectCategory(category)
        }

        binding.rvHorizontalCategories.apply {
            layoutManager = LinearLayoutManager(
                context,
                LinearLayoutManager.HORIZONTAL,
                false
            )
            adapter = horizontalAdapter
        }

        productAdapter = ProductAdapter(emptyList())
        productAdapter.listener = object : ProductAdapter.Listener {
            override fun onClick(product: Product) {
                product.id?.let {
                    findNavController().navigate(
                        TabFragmentDirections.tabViewToProductView(it)
                    )
                }
            }
        }

        binding.rvViewPopular.apply {
            layoutManager = GridLayoutManager(requireContext(), 2)
            adapter = productAdapter
        }

        binding.btnSearch.setOnClickListener {
            binding.searchBarLayout.visibility = if (binding.searchBarLayout.visibility == View.VISIBLE) {
                View.GONE
            } else {
                View.VISIBLE
            }
        }

        // Search functionality
        binding.svSearchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                // Return false to indicate no submission
                return false
            }

            // This method is called whenever the text in the SearchView changes
            override fun onQueryTextChange(newText: String?): Boolean {
                // Filter products based on search query
                filterProducts(newText)
                // Return true to indicate the text change has been handled
                return true
            }
        })
    }

    private fun setupViewModelObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.products.collect { products ->
                originalProduct = products
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
                    findNavController().navigate(TabFragmentDirections.tabViewToLogin())
                }
            }
        }
    }

    private fun filterProducts(query: String?) {
        if (::originalProduct.isInitialized) {
            if (query.isNullOrBlank()) {
                // Show all products if the query is null or blank
                productAdapter.setProduct(originalProduct)
            } else {
                val filteredProducts = originalProduct.filter {
                    it.productName.contains(query, ignoreCase = true)
                }
                // Show filtered products
                productAdapter.setProduct(filteredProducts)
            }
        }
    }
}
