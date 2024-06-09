package com.example.onlineshop.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.onlineshop.data.modal.CartItem
import com.example.onlineshop.data.modal.OrderHistory
import com.example.onlineshop.databinding.ItemCartBinding
import com.example.onlineshop.databinding.ItemOrderHistoryBinding
import com.example.onlineshop.ui.viewModel.user.CartViewModel
import com.example.onlineshop.ui.viewModel.user.OrderHistoryViewModel

class OrderHistoryAdapter(
    private var items: List<OrderHistory>,
) : RecyclerView.Adapter<OrderHistoryAdapter.OrderHistoryViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderHistoryViewHolder {
        val binding = ItemOrderHistoryBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return OrderHistoryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: OrderHistoryViewHolder, position: Int) {
        val item = items[position]
        holder.bind(item)
    }

    override fun getItemCount() = items.size

    fun setOrderHistoryItems(newItems: List<OrderHistory>) {
        this.items = newItems
        notifyDataSetChanged()
    }

    inner class OrderHistoryViewHolder(
        private val binding: ItemOrderHistoryBinding
    ) :
        RecyclerView.ViewHolder(binding.root) {

        private val orderProductAdapter = BillingProductsAdapter(emptyList())

        init {
            binding.rvOrderProduct.apply {
                layoutManager = LinearLayoutManager(binding.root.context)
                adapter = orderProductAdapter
            }
        }

        fun bind(order: OrderHistory) {
            binding.orderProducts = order

            // Set products data to the inner RecyclerView adapter
            orderProductAdapter.setOrderProduct(order.products)
        }

    }
}
