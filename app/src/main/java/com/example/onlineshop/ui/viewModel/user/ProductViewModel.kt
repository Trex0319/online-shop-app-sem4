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
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class ProductViewModel @Inject constructor(
    private val productRepo: ProductRepo,
    private val cartRepo: CartRepo
) : ViewModel() {

    private val _product: MutableLiveData<Product> = MutableLiveData()
    val product: LiveData<Product> = _product
    val productName: MutableLiveData<String> = MutableLiveData("")
    val productInfo: MutableLiveData<String> = MutableLiveData("")
    val productPrice: MutableLiveData<String> = MutableLiveData("")
    val store: MutableLiveData<String> = MutableLiveData("")
    val category: MutableLiveData<String> = MutableLiveData("")
    val productImageUrl: MutableLiveData<String> = MutableLiveData("")
    val snackbar: MutableLiveData<String?> = MutableLiveData()

    fun getProduct(id: String) {
        viewModelScope.launch(Dispatchers.IO) {
            _product.postValue(productRepo.getProductById(id))
        }
    }

    fun addToCart(product: Product? = _product.value) {
        // Checking if the product is not null
         product?.let { p ->
             // Checking if the product is out of stock, display a message
            if (p.store == 0) {
                snackbar.postValue("This product is out of stock.")
                return
            } else {
                // If the product is in stock, proceed to add it to the cart
                viewModelScope.launch(Dispatchers.IO) {
                    val cartItem = p.productImageUrl?.let {
                        CartItem(
                            productId = p.id!!,
                            productName = p.productName,
                            productInfo = p.productInfo,
                            productPrice = p.productPrice,
                            productImageUrl = it
                        )
                    }
                    // Adding the CartItem to the cart
                    if (cartItem != null) {
                        cartRepo.addToCart(cartItem)
                    }
                    // Updating the product stock
                    updateProductStock(p, -1)
                    withContext(Dispatchers.Main) {
                        snackbar.postValue("Product added to the cart successfully.")
                        _product.value = _product.value // LiveData update
                    }
                }
            }
        }
    }

    private suspend fun updateProductStock(product: Product, change: Int) {
        product.store += change // Updating the product stock in a Firestore
        productRepo.updateProduct(product)
    }
}