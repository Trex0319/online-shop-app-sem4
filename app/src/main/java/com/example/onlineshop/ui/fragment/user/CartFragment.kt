    package com.example.onlineshop.ui.fragment.user

    import android.os.Bundle
    import androidx.fragment.app.Fragment
    import android.view.LayoutInflater
    import android.view.View
    import android.view.ViewGroup
    import androidx.fragment.app.activityViewModels
    import androidx.fragment.app.viewModels
    import androidx.recyclerview.widget.LinearLayoutManager
    import com.example.onlineshop.MainActivity
    import com.example.onlineshop.R
    import com.example.onlineshop.data.modal.CartItem
    import com.example.onlineshop.databinding.FragmentCartBinding
    import com.example.onlineshop.ui.adapter.CartAdapter
    import com.example.onlineshop.ui.viewModel.user.CartViewModel
    import com.example.onlineshop.ui.viewModel.user.ProfileViewModel
    import com.google.android.material.snackbar.Snackbar
    import dagger.hilt.android.AndroidEntryPoint

    @Suppress("CAST_NEVER_SUCCEEDS")
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

            viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
                if (isLoading) {
                    binding.loadingOverlay.visibility = View.VISIBLE
                } else {
                    binding.loadingOverlay.visibility = View.GONE
                }
            }

            viewModel.cartItems.observe(viewLifecycleOwner) { cartItems ->
                val manageMergedCartItems = manageMergedCartItems(cartItems)
                cartAdapter.setCartItems(manageMergedCartItems)
                binding.textView7.visibility = if (manageMergedCartItems.isEmpty()) View.VISIBLE else View.GONE
            }

            viewModel.snackbar.observe(viewLifecycleOwner) { message ->
                message?.let {
                    Snackbar.make(binding.root, it, Snackbar.LENGTH_SHORT).show()
                }
            }

            viewModel.totalPrice.observe(viewLifecycleOwner) { totalPrice ->
                binding.tvTotalPrice.text = "$ $totalPrice"
            }

            viewModel.totalPrice.observe(viewLifecycleOwner) { totalPrice ->
                binding.tvTotalPay.text = "$ $totalPrice"
            }

            binding.btnCheckOut.setOnClickListener {
                viewModel.checkout()
            }
        }

        private fun manageMergedCartItems(cartItems: List<CartItem>): List<CartItem> {
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