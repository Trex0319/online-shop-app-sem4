package com.example.onlineshop.ui.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.onlineshop.core.utils.Category
import com.example.onlineshop.databinding.ProductsItemHorizontalLayoutBinding

class HorizontalCategoryAdapter(
    private var selectedCategory: String,
    private var onClickCategory: (Category) -> Unit
) : RecyclerView.Adapter<HorizontalCategoryAdapter.CategoryClickViewHolder>() {

    private var categories: Array<Category> = Category.entries.toTypedArray()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HorizontalCategoryAdapter.CategoryClickViewHolder {
        val binding = ProductsItemHorizontalLayoutBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return CategoryClickViewHolder(binding)
    }

    override fun getItemCount() = categories.size

    override fun onBindViewHolder(holder: CategoryClickViewHolder, position: Int) {
        val items = categories[position]
        holder.bind(items)
    }

    fun setSelected(category: String) {
        selectedCategory = category
        notifyDataSetChanged()
    }

    inner class CategoryClickViewHolder(
        private val binding: ProductsItemHorizontalLayoutBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(category: Category) {
            binding.apply {
                tvCategoryName.text = category.categoryProductName
                tvCategoryName.setTextColor(
                    if (category.categoryProductName == selectedCategory) {
                        Color.BLACK
                    } else {
                        Color.GRAY
                    }
                )
                itemView.setOnClickListener {
                    onClickCategory(category)
                    setSelected(category.categoryProductName)
                }
            }
        }
    }
}