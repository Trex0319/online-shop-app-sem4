package com.example.onlineshop.ui.viewModel.user

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.onlineshop.core.utils.Category
import com.example.onlineshop.data.modal.Product
import com.example.onlineshop.data.repository.product.ProductRepo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val productRepo: ProductRepo
) : ViewModel() {

    private val _products = MutableStateFlow<List<Product>>(emptyList())
    val products: StateFlow<List<Product>> = _products

    private val _selectedCat = MutableStateFlow(Category.all)
    val selectedCat: StateFlow<Category> = _selectedCat

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val job = SupervisorJob()
    private val scope = CoroutineScope(Dispatchers.IO + job)

    init {
        onCreateView()
    }

    fun onCreateView() {
        if (_selectedCat.value == Category.all)
            getProducts()
        else
            getProducts(_selectedCat.value.categoryProductName)
    }

    fun getProducts() {
        viewModelScope.launch(Dispatchers.IO) {
            _selectedCat.emit(Category.all)
            _isLoading.emit(true)
            val products = productRepo.getAllProducts().first() // Assuming getAllProducts() returns a Flow
            _products.emit(products)
            _isLoading.emit(false)
        }
    }

    fun getProducts(category: String) {
        viewModelScope.launch(Dispatchers.IO) {
            _selectedCat.emit(Category.values().find { it.categoryProductName == category } ?: Category.all)
            _isLoading.emit(true)
            val products = productRepo.getProductsByCategory(category).first() // Assuming getProductsByCategory() returns a Flow
            _products.emit(products)
            _isLoading.emit(false)
        }
    }

    fun selectCategory(category: Category) {
        _selectedCat.value = category
        if (category == Category.all) {
            onCreateView()
        } else {
            getProducts(category.categoryProductName)
        }
    }

    fun stopJob() {
        job.cancel()
    }
}
