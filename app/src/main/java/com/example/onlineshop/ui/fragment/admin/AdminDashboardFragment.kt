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
    private lateinit var pickImage: ActivityResultLauncher<PickVisualMediaRequest>
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
        setupAdapter()
        setupProductAdapter()
        setupImagePicker()
        setupObservers()

        lifecycleScope.launch {
            viewModel.fetchAllProducts().collect { adminProductAdapter.setProduct(it) }
        }

        binding.btnLogOut.setOnClickListener {
            try {
                profileViewModel.logout()
                val intent = Intent(requireActivity(), MainActivity::class.java)
                startActivity(intent)
                requireActivity().finish()
            } catch (e: Exception) {
                Log.e("AdminDashboardFragment", "Error during logout", e)
            }
        }
    }

    private fun setupImagePicker() {
        pickImage = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri: Uri? ->
            uri?.let {
                productImageUri = it
                Glide.with(this)
                    .load(it)
                    .placeholder(R.drawable.ic_image)
                    .into(binding.ivProduct)
            }
        }
    }

    private fun setupAdapter() {
        setupCategoryAdapter()

        binding.run {

            tvHomePage.setOnClickListener{
                findNavController().navigate(
                    AdminDashboardFragmentDirections.adminDashboardToTabView()
                )
            }

            btnAddProductImage.setOnClickListener {
                pickImage.launch(
                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                )
            }

            btnAddProduct.setOnClickListener {
                val productName = binding.etProductName.text.toString()
                val productInfo = binding.etProductInfo.text.toString()
                val productPrice = binding.etProductPrice.text.toString()
                val productStore = binding.etProductStore.text.toString().toIntOrNull()

                if (selectedProductCategory == null || productStore == null) {
                    viewModel.snackbar.postValue("Please fill all fields correctly")
                } else {
                    val product = Product(
                        productName = productName,
                        productInfo = productInfo,
                        productPrice = productPrice,
                        store = productStore,
                        category = selectedProductCategory!!
                    )
                    viewModel.addProduct(product, productImageUri)
                    clearFields()
                }
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

    private fun filterProducts(query: String?) {
        if (::originalProduct.isInitialized) {
            if (query.isNullOrBlank()) {
                // Show all products if the query is null or blank
                adminProductAdapter.setProduct(originalProduct)
            } else {
                val filteredProducts = originalProduct.filter {
                    it.productName.contains(query, ignoreCase = true)
                }
                // Show filtered products
                adminProductAdapter.setProduct(filteredProducts)
            }
        }
    }
}