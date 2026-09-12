package com.example.rigswap

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.rigswap.databinding.ActivityMainBinding
import com.example.rigswap.ui.chat.ChatFragment
import com.example.rigswap.ui.home.MarketplaceHomeFragment
import com.example.rigswap.ui.profile.ProfileFragment
import com.example.rigswap.ui.search.SearchFragment
import com.example.rigswap.ui.sell.CreateListingFragment

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    companion object {
        const val EXTRA_DESTINATION_TAB = "extra_dest_tab"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupBottomNavigation()

        val destinationTab = intent.getIntExtra(EXTRA_DESTINATION_TAB, R.id.nav_home)
        binding.bottomNavigation.selectedItemId = destinationTab
    }

    private fun setupBottomNavigation() {
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            val fragment: Fragment = when (item.itemId) {
                R.id.nav_home -> MarketplaceHomeFragment()
                R.id.nav_search -> SearchFragment()
                R.id.nav_sell -> CreateListingFragment()
                R.id.nav_chat -> ChatFragment()
                R.id.nav_profile -> ProfileFragment()
                else -> MarketplaceHomeFragment()
            }
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, fragment)
                .commit()
            true
        }
    }
}
