package com.example.mitiendapabloescobar.ui.main.cart

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.mitiendapabloescobar.databinding.ItemProductBinding
import com.example.mitiendapabloescobar.model.Product

class CartAdapter(private val onProductClick: (Product) -> Unit) :
    RecyclerView.Adapter<CartAdapter.CartViewHolder>() {

    private var products: List<Product> = emptyList()

    fun submitList(newList: List<Product>) {
        products = newList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CartViewHolder {
        val binding = ItemProductBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CartViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CartViewHolder, position: Int) {
        holder.bind(products[position])
    }

    override fun getItemCount(): Int = products.size

    inner class CartViewHolder(private val binding: ItemProductBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(product: Product) {
            binding.tvProductName.text = product.name
            binding.tvProductPrice.text = "${product.price} €"
            binding.tvProductCategory.text = product.category
            
            Glide.with(binding.root.context)
                .load(product.image)
                .into(binding.ivProduct)

            binding.root.setOnClickListener { 
                onProductClick(product)
            }
        }
    }
}
