package com.example.onlineshop.ui.fragment.user

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isInvisible
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.onlineshop.R
import com.example.onlineshop.ui.viewModel.user.ProductViewModel
import com.example.onlineshop.databinding.FragmentProductViewBinding
import com.example.onlineshop.ui.viewModel.user.ProfileViewModel
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ProductViewFragment : Fragment() {

    private lateinit var binding: FragmentProductViewBinding
    private val viewModel: ProductViewModel by viewModels()
    private val profileViewModel: ProfileViewModel by activityViewModels()
    private var selectedProductId: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentProductViewBinding.inflate(inflater, container, false)
        profileViewModel.getCurrentUser()
        arguments?.let {
            selectedProductId = ProductViewFragmentArgs.fromBundle(it).id
            selectedProductId?.let { id ->
                viewModel.getProduct(id)
            }
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner

        viewModel.product.observe(viewLifecycleOwner) { product ->
            product?.let {
                binding.run {
                    tvProductName.text = it.productName
                    tvProductInfo.text = it.productInfo
                    tvProductPrice.text = it.productPrice
                    tvProductStore.text = it.store.toString()
                    Glide.with(this@ProductViewFragment)
                        .load(it.productImageUrl)
                        .placeholder(R.drawable.ic_image)
                        .into(binding.ivProduct)
                    tvSoldOut.isInvisible = it.store != 0
                }
            }
        }

        binding.ivBack.setOnClickListener {
            findNavController().navigate(
                ProductViewFragmentDirections.productViewToTabView()
            )
        }

        binding.btnAddToCart.setOnClickListener {
            viewModel.addToCart()
        }

        viewModel.snackbar.observe(viewLifecycleOwner) { message ->
            message?.let {
                Snackbar.make(binding.root, it, Snackbar.LENGTH_SHORT).show()
            }
        }
    }
}