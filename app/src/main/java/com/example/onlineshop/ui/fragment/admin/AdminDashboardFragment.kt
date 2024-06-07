package com.example.onlineshop.ui.fragment.admin

import android.app.AlertDialog
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
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
import com.example.onlineshop.R
import com.example.onlineshop.core.utils.Category
import com.example.onlineshop.data.modal.Product
import com.example.onlineshop.ui.adapter.AdminProductAdapter
import com.example.onlineshop.ui.adapter.ProductAdapter
import com.example.onlineshopappgroupproject.activity.ui.fragment.admin.AdminDashboardFragmentDirections
import com.example.onlineshop.ui.viewModel.admin.AdminViewModel
import com.example.onlineshop.ui.viewModel.profile.ProfileViewModel
import com.example.onlineshop.databinding.AlertDeleteProductBinding
import com.example.onlineshop.databinding.FragmentAdminDashboardBinding
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class AdminDashboardFragment : Fragment() {
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var binding: FragmentAdminDashboardBinding
    private val viewModel: AdminViewModel by viewModels()
    private val profileViewModel: ProfileViewModel by activityViewModels()
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

        lifecycleScope.launch {
            profileViewModel.loggedOut.collect {
                Log.d("AdminDashboardFragment", "Logged out observed: $it")
                if (it) {
                    viewModel.stopJob()
                    findNavController().navigate(AdminDashboardFragmentDirections.actionAdminDashboardFragmentToLoginFragment2())
                }
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

    private fun setupAdapter() {
        setupCategoryAdapter()

        binding.run {
            btnChangeBanner.setOnClickListener {
                profileViewModel.logout()
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
        }
    }

    private fun setupCategoryAdapter() {
        val categories = Category.values().filter { it != Category.all }
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
                    AdminDashboardFragmentDirections.actionAdminDashboardFragmentToProductView(product.id!!)
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
                    AdminDashboardFragmentDirections.actionAdminDashboardFragmentToUpdateDeleteProductFragment(id)
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