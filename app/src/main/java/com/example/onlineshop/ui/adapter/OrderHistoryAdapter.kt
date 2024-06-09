package com.example.onlineshop.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.onlineshop.data.modal.OrderHistory
import com.example.onlineshop.databinding.ItemOrderHistoryBinding


class OrderHistoryAdapter (
    private var orderItems: List<OrderHistory>,
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    var listener: ProductAdapter.Listener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderHistoryViewHolder {
        val binding = ItemOrderHistoryBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return OrderHistoryViewHolder(binding)
    }

    override fun getItemCount(): Int = orderItems.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val order = orderItems[position]
        if (holder is OrderHistoryViewHolder) {
            holder.bind(order)
        }
    }

    fun setOrderHistoryItems(orderItems: List<OrderHistory>) {
        this.orderItems = orderItems
        notifyDataSetChanged()
    }


    inner class OrderHistoryViewHolder(private val binding: ItemOrderHistoryBinding,
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(orderHistory: OrderHistory) {
            binding.orderProducts = orderHistory

        }
    }
}