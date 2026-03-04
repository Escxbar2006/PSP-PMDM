package com.example.reciclerview

import android.graphics.Color as AndroidColor
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView

class ColorAdapter(
    private var colors: List<Color>,
    private val onItemClick: (Color) -> Unit,
    private val onItemLongClick: (Color) -> Unit
) : RecyclerView.Adapter<ColorAdapter.ColorViewHolder>() {

    class ColorViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val container: View = view.findViewById(R.id.itemContainer)
        val tvName: TextView = view.findViewById(R.id.tvColorName)
        val tvHex: TextView = view.findViewById(R.id.tvColorHex)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ColorViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_color, parent, false)
        return ColorViewHolder(view)
    }

    override fun onBindViewHolder(holder: ColorViewHolder, position: Int) {
        val color = colors[position]

        holder.tvName.text = color.name
        holder.tvHex.text = color.hexCode

        if (color.isInverted) {
            holder.container.setBackgroundColor(AndroidColor.WHITE)
            try {
                val parsedColor = AndroidColor.parseColor(color.hexCode)
                holder.tvName.setTextColor(parsedColor)
                holder.tvHex.setTextColor(parsedColor)
            } catch (e: Exception) {
                holder.tvName.setTextColor(AndroidColor.BLACK)
                holder.tvHex.setTextColor(AndroidColor.BLACK)
            }
        } else {
            try {
                holder.container.setBackgroundColor(AndroidColor.parseColor(color.hexCode))
            } catch (e: Exception) {
                holder.container.setBackgroundColor(AndroidColor.GRAY)
            }
            holder.tvName.setTextColor(AndroidColor.WHITE)
            holder.tvHex.setTextColor(AndroidColor.WHITE)
        }

        holder.itemView.setOnClickListener { onItemClick(color) }
        holder.itemView.setOnLongClickListener {
            onItemLongClick(color)
            true
        }
    }

    override fun getItemCount() = colors.size

    fun updateData(newColors: List<Color>) {
        val diffCallback = ColorDiffCallback(this.colors, newColors)
        val diffResult = DiffUtil.calculateDiff(diffCallback)
        this.colors = newColors
        diffResult.dispatchUpdatesTo(this)
    }

    class ColorDiffCallback(
        private val oldList: List<Color>,
        private val newList: List<Color>
    ) : DiffUtil.Callback() {
        override fun getOldListSize() = oldList.size
        override fun getNewListSize() = newList.size

        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldList[oldItemPosition].id == newList[newItemPosition].id
        }

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldList[oldItemPosition] == newList[newItemPosition]
        }
    }
}
