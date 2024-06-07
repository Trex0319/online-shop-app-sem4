package com.example.onlineshop.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.onlineshop.data.modal.CartItem

class CartAdapter(private var cartItems: List<CartItem>, private val viewModel: CartViewModel) : RecyclerView.Adapter<CartAdapter.CartViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CartViewHolder {
        val binding = ItemCartBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return CartViewHolder(binding, viewModel)
    }

    override fun onBindViewHolder(holder: CartViewHolder, position: Int) {
        val cartItem = cartItems[position]
        holder.bind(cartItem)
    }

    override fun getItemCount(): Int = cartItems.size

    fun setCartItems(cartItems: List<CartItem>) {
        this.cartItems = cartItems
        notifyDataSetChanged()
    }

    inner class CartViewHolder(private val binding: ItemCartBinding, private val viewModel: CartViewModel) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(cartItem: CartItem) {
            binding.cartItem = cartItem
            binding.run {
                Glide.with(binding.root)
                    .load(cartItem.productImageUrl)
                    .into(binding.productImageView)

                btnDecrease.setOnClickListener { decreaseQuantity(cartItem) }
                btnIncrease.setOnClickListener { increaseQuantity(cartItem) }
            }
        }

        private fun decreaseQuantity(cartItem: CartItem) {
            viewModel.decreaseQuantity(cartItem)
        }

        private fun increaseQuantity(cartItem: CartItem) {
            viewModel.increaseQuantity(cartItem)
        }


    }
}
