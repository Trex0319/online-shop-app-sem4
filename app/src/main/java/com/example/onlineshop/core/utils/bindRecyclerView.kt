package com.example.onlineshop.core.utils

import androidx.databinding.BindingAdapter
import androidx.lifecycle.LiveData
import androidx.recyclerview.widget.RecyclerView
import com.example.onlineshop.data.modal.OrderHistory
import com.example.onlineshop.ui.adapter.OrderHistoryAdapter

@BindingAdapter("listItems")
fun bindRecyclerView(recyclerView: RecyclerView, items: LiveData<List<OrderHistory>>?) {
    items?.value?.let { orderHistoryItems ->
        val adapter = recyclerView.adapter as? OrderHistoryAdapter
        adapter?.setOrderHistoryItems(orderHistoryItems)
    }
}
