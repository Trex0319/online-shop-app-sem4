package com.example.onlineshop.ui.viewModel.user

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.onlineshop.data.modal.CartItem
import com.example.onlineshop.data.modal.Product
import com.example.onlineshop.data.repository.cart.CartRepo
import com.example.onlineshop.data.repository.product.ProductRepo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class ProductViewModel @Inject constructor(
    private val productRepo: ProductRepo,
    private val cartRepo: CartRepo // Inject CartRepo
) : ViewModel() {

    private val _product: MutableLiveData<Product> = MutableLiveData()
    val product: LiveData<Product> = _product
    val productName: MutableLiveData<String> = MutableLiveData("")
    val productInfo: MutableLiveData<String> = MutableLiveData("")
    val productPrice: MutableLiveData<String> = MutableLiveData("")
    val store: MutableLiveData<String> = MutableLiveData("")
    val category: MutableLiveData<String> = MutableLiveData("")
    val productImageUrl: MutableLiveData<String> = MutableLiveData("")
    val finish: MutableSharedFlow<Unit> = MutableSharedFlow()
    val addToCartEvent: MutableSharedFlow<Product> = MutableSharedFlow()
    val snackbar: MutableLiveData<String?> = MutableLiveData()

    fun getProduct(id: String) {
        viewModelScope.launch(Dispatchers.IO) {
            _product.postValue(productRepo.getProductById(id))
        }
    }

    fun addToCart() {
        _product.value?.let { product ->
            if (product.store == 0) {
                snackbar.postValue("This product is out of stock and cannot be added to the cart.")
                return // Early return to ensure no further action is taken
            } else {
                viewModelScope.launch(Dispatchers.IO) {
                    val cartItem = product.productImageUrl?.let {
                        CartItem(
                            productId = product.id!!,
                            productName = product.productName,
                            productPrice = product.productPrice,
                            productImageUrl = it
                        )
                    }
                    if (cartItem != null) {
                        cartRepo.addToCart(cartItem)
                    } // Add to cart using CartRepo
                    addToCartEvent.emit(product) // Emit event
                    updateProductStock(product, -1)
                    withContext(Dispatchers.Main) {
                        snackbar.postValue("Product added to the cart successfully.")
                    }
                }
            }
        }
    }

    private suspend fun updateProductStock(product: Product, change: Int) {
        product.store += change
        productRepo.updateProduct(product)
    }
}