package com.example.mitiendapabloescobar

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.mitiendapabloescobar.api.TokenManager
import com.example.mitiendapabloescobar.databinding.ActivityMainBinding
import com.example.mitiendapabloescobar.ui.login.LoginActivity
import com.example.mitiendapabloescobar.ui.main.MainPagerAdapter
import com.example.mitiendapabloescobar.ui.main.MainViewModel
import com.google.android.material.tabs.TabLayoutMediator

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Verificamos sesión de forma robusta
        val token = intent.getStringExtra("EXTRA_TOKEN") ?: TokenManager(this).getToken()
        val username = intent.getStringExtra("EXTRA_USERNAME") ?: "Usuario"

        if (token.isNullOrBlank()) {
            logout()
            return
        }

        viewModel.setUserData(token, username)
        setupToolbar(username)
        setupViewPager()
    }

    private fun setupToolbar(username: String) {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.title = getString(R.string.welcome_user, username)
        
        binding.toolbar.setNavigationOnClickListener {
            logout()
        }
    }

    private fun setupViewPager() {
        binding.viewPager.adapter = MainPagerAdapter(this)
        
        val tabIcons = listOf(
            R.drawable.ic_home,
            R.drawable.ic_instruments,
            R.drawable.ic_cart
        )
        
        val tabTitles = listOf(
            getString(R.string.home_tab),
            getString(R.string.products_tab),
            getString(R.string.cart_tab)
        )

        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = tabTitles[position]
            tab.setIcon(tabIcons[position])
        }.attach()
    }

    private fun logout() {
        TokenManager(this).clearToken()
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}
