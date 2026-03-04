package com.example.reciclerview

import android.graphics.Color as AndroidColor
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ColorAdapter(private val colors: List<Color>) : RecyclerView.Adapter<ColorAdapter.ColorViewHolder>() {

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
        try {
            holder.container.setBackgroundColor(AndroidColor.parseColor(color.hexCode))
        } catch (e: Exception) {
            holder.container.setBackgroundColor(AndroidColor.GRAY)
        }
    }

    override fun getItemCount() = colors.size
}
