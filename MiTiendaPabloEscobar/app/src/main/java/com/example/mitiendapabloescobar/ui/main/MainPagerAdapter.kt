package com.example.mitiendapabloescobar.ui.main

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.mitiendapabloescobar.ui.main.cart.CartFragment
import com.example.mitiendapabloescobar.ui.main.home.HomeFragment
import com.example.mitiendapabloescobar.ui.main.products.ProductsFragment

class MainPagerAdapter(fragmentActivity: FragmentActivity) : FragmentStateAdapter(fragmentActivity) {
    override fun getItemCount(): Int = 3

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> HomeFragment()
            1 -> ProductsFragment()
            2 -> CartFragment()
            else -> HomeFragment()
        }
    }
}
