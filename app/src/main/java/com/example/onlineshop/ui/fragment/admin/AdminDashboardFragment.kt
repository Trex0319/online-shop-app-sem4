package com.example.onlineshop.ui.fragment.admin

import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.SearchView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.bumptech.glide.Glide
import com.example.onlineshop.MainActivity
import com.example.onlineshop.R
import com.example.onlineshop.core.utils.Category
import com.example.onlineshop.data.modal.Product
import com.example.onlineshop.ui.adapter.AdminProductAdapter
import com.example.onlineshop.ui.viewModel.admin.AdminViewModel
import com.example.onlineshop.databinding.AlertDeleteProductBinding
import com.example.onlineshop.databinding.FragmentAdminDashboardBinding
import com.example.onlineshop.ui.viewModel.user.ProfileViewModel
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class AdminDashboardFragment : Fragment() {
    private lateinit var binding: FragmentAdminDashboardBinding
    private val viewModel: AdminViewModel by viewModels()
    private val profileViewModel: ProfileViewModel by activityViewModels()
    private lateinit var originalProduct: List<Product>
    private var selectedProductCategory: Category? = null
    private var productImageUri: Uri? = null
    private lateinit var pickImage: ActivityResultLauncher<PickVisualMediaRequest>  // For picking an image
    private lateinit var adminProductAdapter: AdminProductAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAdminDashboardBinding.inflate(
            layoutInflater,
            container,
            false
        )
        profileViewModel.getCurrentUser()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupAdapter() // Setup adapters, click listeners
        setupProductAdapter() // Setup productAdapter for RecyclerView
        setupImagePicker() // Setup image picker for selecting product images
        setupObservers() // Setup observers for LiveData objects

        lifecycleScope.launch {
            viewModel.fetchAllProducts().collect { adminProductAdapter.setProduct(it) }
        }

        binding.btnLogOut.setOnClickListener {
            profileViewModel.logout()
            findNavController().navigate(
                AdminDashboardFragmentDirections.adminDashboardToLogin()
            )
        }
    }

    private fun setupImagePicker() {
        // Register for activity result to pick an image
        pickImage = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri: Uri? ->
            uri?.let {
                productImageUri = it // Save the selected image URI
                Glide.with(this)
                    .load(it)
                    .placeholder(R.drawable.ic_image)
                    .into(binding.ivProduct)
            }
        }
    }

    private fun setupAdapter() {
        setupCategoryAdapter() // Setup category adapter for AutoCompleteTextView
        binding.run {

            tvHomePage.setOnClickListener{
                findNavController().navigate(
                    AdminDashboardFragmentDirections.adminDashboardToTabView()
                )
            }

            btnAddProductImage.setOnClickListener {
                pickImage.launch(
                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                ) // Launch image picker
            }

            btnAddProduct.setOnClickListener {
                val productName = binding.etProductName.text.toString()
                val productInfo = binding.etProductInfo.text.toString()
                val productPrice = binding.etProductPrice.text.toString()
                val productStore = binding.etProductStore.text.toString().toIntOrNull()
                // Validate input fields
                if (selectedProductCategory == null || productStore == null) {
                    viewModel.snackbar.postValue("Please fill all fields correctly")
                } else {
                    // Create a new Product object
                    val product = Product(
                        productName = productName,
                        productInfo = productInfo,
                        productPrice = productPrice,
                        store = productStore,
                        category = selectedProductCategory!!
                    )
                    viewModel.addProduct(product, productImageUri) // Add the product to the Firebase
                    clearFields()
                }
            }

            viewLifecycleOwner.lifecycleScope.launch {
                profileViewModel.user.collect { user ->
                    user?.let {
                        binding.tvCurrentUserName.text = it.name ?: "Unknown User"
                    }
                }
            }
        }
    }

    private fun setupCategoryAdapter() {
        // Populate AutoCompleteTextView with category options
        val categories = Category.entries.filter { it != Category.All }
        val arrayAdapter = ArrayAdapter(
            requireContext(),
            R.layout.selected_product_category,
            categories.map { it.categoryProductName })
            binding.run {
            actvCategory.setAdapter(arrayAdapter)
            actvCategory.setOnItemClickListener { _, _, position, _ ->
                selectedProductCategory = categories[position] // Passing selectedProductCategory value
            }
        }
    }

    private fun clearFields() {
        binding.etProductName.text?.clear()
        binding.etProductInfo.text?.clear()
        binding.etProductPrice.text?.clear()
        binding.etProductStore.text?.clear()
        binding.ivProduct.setImageResource(R.drawable.ic_person)
        productImageUri = null
        selectedProductCategory = null
    }

    private fun setupProductAdapter() {
        adminProductAdapter = AdminProductAdapter(emptyList())
        adminProductAdapter.listener = object : AdminProductAdapter.Listener{
            override fun onClick(product: Product) {
                // Navigate to ViewDoneDeleteFragment when a word is clicked
                findNavController().navigate(
                    AdminDashboardFragmentDirections.adminDashboardToProductView(product.id!!)
                )

            }

            override fun onDelete(id: String) {
                val alertView = AlertDeleteProductBinding.inflate(layoutInflater)
                val deleteDialog = AlertDialog.Builder(requireContext())
                deleteDialog.setView(alertView.root)
                alertView.tvTitle.text = "Are you sure?"
                alertView.tvBody.text = "You want to delete this word? \n Action can not be undone."

                val temporaryDeleteDialog = deleteDialog.create()

                alertView.btnDelete.setOnClickListener {
                    viewModel.deleteProduct(id)
                    Toast.makeText(
                        requireContext(),
                        "Deleted Successfully",
                        Toast.LENGTH_SHORT
                    ).show()
                    temporaryDeleteDialog.dismiss()
                }

                alertView.btnCancel.setOnClickListener {
                    temporaryDeleteDialog.dismiss() // Dismiss the dialog when cancel button is clicked
                }
                // Show the delete dialog
                temporaryDeleteDialog.show()

            }

            override fun onEdit(id: String) {
                findNavController().navigate(
                    AdminDashboardFragmentDirections.adminDashboardToAdminUpdateProduct(id)
                )
            }
        }
        val layoutManager = GridLayoutManager(requireContext(), 2)
        binding.rvProductAdmin.adapter = adminProductAdapter
        binding.rvProductAdmin.layoutManager = layoutManager
    }

    private fun setupObservers() {
        viewModel.snackbar.observe(viewLifecycleOwner) { message ->
            message?.let {
                Snackbar.make(binding.root, it, Snackbar.LENGTH_LONG).show()
                viewModel.snackbar.value = null // Reset the snackbar message
            }
        }
    }
}