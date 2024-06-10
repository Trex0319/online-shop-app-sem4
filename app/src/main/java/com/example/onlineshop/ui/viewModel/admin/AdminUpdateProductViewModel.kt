package com.example.onlineshop.ui.viewModel.admin

import android.net.Uri
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.onlineshop.data.modal.Product
import com.example.onlineshop.data.repository.FirebaseImageStorage.FirebaseImageStorage
import com.example.onlineshop.data.repository.product.ProductRepo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class AdminUpdateProductViewModel @Inject constructor(
    private val productRepo: ProductRepo,
    private val storageService: FirebaseImageStorage
) : ViewModel() {

    val snackbar: MutableLiveData<String?> = MutableLiveData()
    private var job: Job? = null
    private val _products = MutableStateFlow<List<Product>>(emptyList())
    val products: StateFlow<List<Product>> get() = _products

    private val _selectedProduct = MutableStateFlow<Product?>(null)
    val selectedProduct: StateFlow<Product?> get() = _selectedProduct

    private val _finish = MutableStateFlow(false)
    val finish: StateFlow<Boolean> get() = _finish

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> get() = _isLoading

    fun getProductById(productId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val product = productRepo.getProductById(productId)
                withContext(Dispatchers.Main) {
                    _selectedProduct.value = product
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    snackbar.value = e.message
                }
            }
        }
    }

    fun updateProduct(product: Product, uri: Uri?) {
        viewModelScope.launch(Dispatchers.IO) {
            _isLoading.value = true
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
                    // If there's a new image URI, upload it and update the product's image URL
                    uri?.let {
                        val url = storageService.addImage("product_${product.id}.jpg", it)
                        product.productImageUrl = url
                    }
                    productRepo.updateProduct(product)

                    withContext(Dispatchers.Main) {
                        snackbar.value = "Product updated successfully"
                        _finish.value = true
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        snackbar.value = e.message
                    }
                }
            }
            _isLoading.value = false
        }
    }
}