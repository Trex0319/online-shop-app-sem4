package com.example.onlineshop.ui.fragment.admin

import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.onlineshop.R
import com.example.onlineshop.core.utils.Category
import com.example.onlineshop.data.modal.Product
import com.example.onlineshop.ui.viewModel.admin.AdminUpdateProductViewModel
import com.example.onlineshop.databinding.FragmentAdminUpdateProductBinding
import com.example.onlineshop.ui.fragment.user.ProductViewFragmentArgs
import com.example.onlineshop.ui.viewModel.user.ProfileViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class AdminUpdateProductFragment : Fragment() {
    private lateinit var binding: FragmentAdminUpdateProductBinding
    private val viewModel: AdminUpdateProductViewModel by viewModels()
    private val profileViewModel: ProfileViewModel by activityViewModels()
    private var selectedProductCategory: Category? = null
    private var productImageUri: Uri? = null
    private lateinit var pickImage: ActivityResultLauncher<PickVisualMediaRequest>
    private var productId: String? = null
    private var selectedWordId = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentAdminUpdateProductBinding.inflate(
            layoutInflater,
            container,
            false
        )
        profileViewModel.getCurrentUser()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        arguments?.let {
            selectedWordId = ProductViewFragmentArgs.fromBundle(it).id.toString()
            viewModel.getProductById(selectedWordId)
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.selectedProduct.collect { product ->
                product?.let {
                    binding.etProductName.setText(it.productName)
                    binding.etProductInfo.setText(it.productInfo)
                    binding.etProductPrice.setText(it.productPrice)
                    binding.etProductStore.setText(it.store.toString())
                    selectedProductCategory = it.category
                    binding.actvCategory.setText(it.category.categoryProductName)
                    setupCategoryAdapter()
                    it.productImageUrl?.let { url ->
                        Glide.with(requireView())
                            .load(url)
                            .placeholder(R.drawable.ic_image)
                            .into(binding.ivProduct)
                    }
                }
            }
        }
        // Collect loading state and handle it
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.isLoading.collect { isLoading ->
                binding.loadingOverlay.visibility = if (isLoading) View.VISIBLE else View.GONE
            }
        }


        setupAdapter()
        setupImagePicker()
    }

    private fun setupAdapter() {
        setupCategoryAdapter()

        binding.run {

            btnAddProductImage.setOnClickListener {
                pickImage.launch(
                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                )
            }
            binding.btnUpdateProduct.setOnClickListener {
                val productName = binding.etProductName.text.toString()
                val productInfo = binding.etProductInfo.text.toString()
                val productPrice = binding.etProductPrice.text.toString()
                val productStore = binding.etProductStore.text.toString().toIntOrNull()

                if (selectedProductCategory == null || productStore == null) {
                    viewModel.snackbar.postValue("Please fill all fields correctly")
                } else {
                    val product = Product(
                        id = selectedWordId, // Use the existing product ID
                        productName = productName,
                        productInfo = productInfo,
                        productPrice = productPrice,
                        store = productStore,
                        category = selectedProductCategory!!,
                        productImageUrl = viewModel.selectedProduct.value?.productImageUrl
                    )
                    viewModel.updateProduct(product, productImageUri)
                }
            }
        }


        // Observe completion state
        lifecycleScope.launch {
            viewModel.finish.collect { finished ->
                if (finished) {
                    findNavController().popBackStack()
                }
            }
        }
    }

    private fun setupCategoryAdapter() {
        val categories = Category.entries.filter { it != Category.All }
        val arrayAdapter = ArrayAdapter(
            requireContext(),
            R.layout.selected_product_category,
            categories.map { it.categoryProductName })
        binding.run {
            actvCategory.setAdapter(arrayAdapter)
            actvCategory.setOnItemClickListener { _, _, position, _ ->
                selectedProductCategory = categories[position]
                Log.d("UpdateProduct", "Selected Category: ${selectedProductCategory?.categoryProductName}")
            }
        }
    }

    private fun setupImagePicker() {
        pickImage = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri: Uri? ->
            uri?.let {
                productImageUri = it
                Glide.with(this)
                    .load(it)
                    .placeholder(R.drawable.ic_person)
                    .into(binding.ivProduct)
            }
        }
    }
}