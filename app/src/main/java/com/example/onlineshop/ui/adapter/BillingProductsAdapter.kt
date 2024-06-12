package com.example.onlineshop.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.onlineshop.data.modal.CartItem
import com.example.onlineshop.databinding.ItemBillingProductLayoutBinding

class BillingProductsAdapter(
    private var billingProduct: List<CartItem>
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderProductViewHolder {
        val binding = ItemBillingProductLayoutBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return OrderProductViewHolder(binding)
    }

    override fun getItemCount(): Int = billingProduct.size
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val order = billingProduct[position]
        if (holder is OrderProductViewHolder) {
            holder.bind(order)
        }
    }

    fun setOrderProduct(newBillingProduct: List<CartItem>) {
        billingProduct = newBillingProduct
        notifyDataSetChanged()
    }

    inner class OrderProductViewHolder(
        private val binding: ItemBillingProductLayoutBinding,
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(orderProduct: CartItem) {
            binding.product = orderProduct
            binding.run {
                // Load the product image using Glide library
                Glide.with(binding.root)
                    .load(orderProduct.productImageUrl)
                    .into(binding.productImageView)
            }
        }
    }
}