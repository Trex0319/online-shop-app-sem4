package com.example.onlineshop.ui.fragment.user

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.onlineshop.data.modal.CartItem
import com.example.onlineshop.databinding.FragmentCartBinding
import com.example.onlineshop.ui.adapter.CartAdapter
import com.example.onlineshop.ui.viewModel.user.CartViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CartFragment : Fragment() {

    private lateinit var binding: FragmentCartBinding
    private val viewModel: CartViewModel by viewModels()
    private val profileViewModel: ProfileViewModel by activityViewModels()
    private lateinit var cartAdapter: CartAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentCartBinding.inflate(inflater, container, false)
        profileViewModel.getCurrentUser()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        cartAdapter = CartAdapter(emptyList(), viewModel)
        binding.rvCart.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = cartAdapter
        }

        viewModel.cartItems.observe(viewLifecycleOwner) { cartItems ->
            val mergedCartItems = mergeCartItems(cartItems)
            cartAdapter.setCartItems(mergedCartItems)
            binding.textView7.visibility = if (mergedCartItems.isEmpty()) View.VISIBLE else View.GONE
        }

        binding.btnCheckOut.setOnClickListener {
            viewModel.checkout()
        }
    }

    private fun mergeCartItems(cartItems: List<CartItem>): List<CartItem> {
        val mergedMap = mutableMapOf<String, CartItem>()
        cartItems.forEach { cartItem ->
            val existingItem = mergedMap[cartItem.productId]
            if (existingItem != null) {
                existingItem.quantity += cartItem.quantity
            } else {
                mergedMap[cartItem.productId] = cartItem
            }
        }
        return mergedMap.values.toList()
    }


}