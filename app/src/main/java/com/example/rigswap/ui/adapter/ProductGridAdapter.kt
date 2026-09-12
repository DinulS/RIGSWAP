package com.example.rigswap.ui.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.graphics.BitmapFactory
import android.net.Uri
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.rigswap.R
import com.example.rigswap.data.model.Product
import com.example.rigswap.databinding.ItemProductCardBinding
import java.io.File

class ProductGridAdapter(
    private val onProductClick: (Product) -> Unit,
    private val isCompatView: Boolean = false
) : RecyclerView.Adapter<ProductGridAdapter.ProductViewHolder>() {

    private val products = mutableListOf<Product>()

    fun submitList(newProducts: List<Product>) {
        products.clear()
        products.addAll(newProducts)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val binding = ItemProductCardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ProductViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        holder.bind(products[position])
    }

    override fun getItemCount(): Int = products.size

    inner class ProductViewHolder(private val binding: ItemProductCardBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(product: Product) {
            val context = binding.root.context
            binding.tvTitle.text = product.title
            binding.tvPrice.text = product.getFormattedPrice()
            binding.tvLocation.text = product.location

            // Resolve image resource or custom photo
            bindProductImage(binding.ivProductImage, product.imageResName)

            // Badges
            if (isCompatView) {
                binding.tvConditionBadge.text = "COMPAT"
                binding.tvConditionBadge.setTextColor(ContextCompat.getColor(context, R.color.badge_new_green))
                binding.tvConditionBadge.setBackgroundResource(R.drawable.bg_badge_compat)
            } else {
                binding.tvConditionBadge.text = product.condition
                when (product.condition.uppercase()) {
                    "NEW" -> {
                        binding.tvConditionBadge.setTextColor(ContextCompat.getColor(context, R.color.badge_new_green))
                        binding.tvConditionBadge.setBackgroundResource(R.drawable.bg_badge_new)
                    }
                    "USED" -> {
                        binding.tvConditionBadge.setTextColor(ContextCompat.getColor(context, R.color.badge_used_orange))
                        binding.tvConditionBadge.setBackgroundResource(R.drawable.bg_badge_used)
                    }
                    "REFURB", "REFURBISHED" -> {
                        binding.tvConditionBadge.setTextColor(ContextCompat.getColor(context, R.color.badge_refurb_red))
                        binding.tvConditionBadge.setBackgroundResource(R.drawable.bg_badge_refurb)
                    }
                    else -> {
                        binding.tvConditionBadge.setTextColor(ContextCompat.getColor(context, R.color.badge_shipped_blue))
                        binding.tvConditionBadge.setBackgroundResource(R.drawable.bg_badge_shipped)
                    }
                }
            }

            // Star 5 visibility / tint
            if (product.rating < 5.0f) {
                binding.ivStar5.alpha = 0.3f
            } else {
                binding.ivStar5.alpha = 1.0f
            }

            binding.root.setOnClickListener {
                onProductClick(product)
            }
        }
    }

    companion object {
        fun getDrawableIdByName(context: Context, resName: String): Int {
            val id = context.resources.getIdentifier(resName, "drawable", context.packageName)
            return if (id != 0) id else R.drawable.img_rtx4090
        }

        fun bindProductImage(imageView: ImageView, imageResOrPath: String) {
            val context = imageView.context
            // User requirement: all product images must be rotated 90 degrees clockwise
            imageView.rotation = 90f

            if (imageResOrPath.isNotBlank()) {
                try {
                    val file = File(imageResOrPath)
                    if (file.exists() && file.isFile) {
                        val bitmap = BitmapFactory.decodeFile(file.absolutePath)
                        if (bitmap != null) {
                            imageView.scaleType = ImageView.ScaleType.CENTER_CROP
                            imageView.setImageBitmap(bitmap)
                            return
                        }
                    } else if (imageResOrPath.startsWith("content://") || imageResOrPath.startsWith("file://")) {
                        imageView.scaleType = ImageView.ScaleType.CENTER_CROP
                        imageView.setImageURI(Uri.parse(imageResOrPath))
                        return
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            val resId = getDrawableIdByName(context, imageResOrPath)
            imageView.setImageResource(resId)
        }
    }
}
