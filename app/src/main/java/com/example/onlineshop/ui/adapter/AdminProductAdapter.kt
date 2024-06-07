package com.example.onlineshop.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.onlineshop.data.modal.Product
import com.example.onlineshop.databinding.ProductItemLayoutAdminBinding
import com.example.onlineshop.databinding.ProductItemLayoutBinding

class AdminProductAdapter(
    private var products: List<Product>,
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    var listener: Listener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val binding = ProductItemLayoutAdminBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ProductViewHolder(binding)
    }

    override fun getItemCount() = products.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val product = products[position]
        if (holder is ProductViewHolder) {
            holder.bind(product)
        }
    }

    fun setProduct(products: List<Product>) {
        this.products = products
        notifyDataSetChanged()
    }
    inner class ProductViewHolder(private val binding: ProductItemLayoutAdminBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(product: Product) {
            binding.product = product
            binding.run {
                Glide.with(binding.root)
                    .load(product.productImageUrl)
                    .into(binding.productImageView)
                cvProduct.setOnClickListener {
                    listener?.onClick(product)
                }
                btnDelete.setOnClickListener {
                    listener?.onDelete(product.id!!)
                }
                btnCancel.setOnClickListener {
                    listener?.onEdit(product.id!!)
                }
            }
        }
    }


    interface Listener {
        fun onClick(product: Product)
        fun onDelete(id: String)
        fun onEdit(id:String)
    }

}