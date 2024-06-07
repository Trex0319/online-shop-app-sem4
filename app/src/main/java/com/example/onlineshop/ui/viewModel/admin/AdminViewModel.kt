package com.example.onlineshop.ui.viewModel.admin

import android.content.Context
import android.net.Uri
import android.util.Patterns
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.onlineshop.core.utils.Category
import com.example.onlineshop.data.modal.Product
import com.example.onlineshop.data.modal.User
import com.example.onlineshop.data.repository.FirebaseImageStorage.FirebaseImageStorage
import com.example.onlineshop.data.repository.product.ProductRepo
import com.example.onlineshop.data.repository.user.UserRepo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class AdminViewModel @Inject constructor(
    private val productRepo: ProductRepo,
    private val storageService: FirebaseImageStorage
) : ViewModel() {

    val snackbar: MutableLiveData<String?> = MutableLiveData()
    private var job: Job? = null
    private val _products = MutableStateFlow<List<Product>>(emptyList())
    val products: StateFlow<List<Product>> get() = _products
    val finish: MutableSharedFlow<Unit> = MutableSharedFlow()
    suspend fun fetchAllProducts(): Flow<List<Product>> = productRepo.getAllProducts()

    fun addProduct(product: Product, uri: Uri?) {
        viewModelScope.launch(Dispatchers.IO) {
            val errorMessage: String? = when {
                product.store == 0 -> "Store can't be zero"
                product.productName.isEmpty() || product.productInfo.isEmpty() || product.productPrice.isEmpty() -> "Please fill up all fields"
                else -> null
            }

            if (errorMessage != null) {
                withContext(Dispatchers.Main) {
                    snackbar.value = errorMessage
                }
            } else {
                try {
                    val id = productRepo.addNewProduct(product)
                    uri?.let {
                        val url = storageService.addImage("product_$id.jpg", it)
                        productRepo.updateProduct(product.copy(id = id, productImageUrl = url))
                    }
                    withContext(Dispatchers.Main) {
                        snackbar.value = "Product added successfully"
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        snackbar.value = e.message
                    }
                }
            }
        }
    }

    fun deleteProduct(id: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                productRepo.deleteProduct(id)
                withContext(Dispatchers.Main) {
                    snackbar.value = "Product deleted successfully"
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    snackbar.value = e.message
                }
            }
        }
    }

    fun stopJob() {
        job?.cancel()
    }
}