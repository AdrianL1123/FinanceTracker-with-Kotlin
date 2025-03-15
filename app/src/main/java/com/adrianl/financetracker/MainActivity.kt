package com.adrianl.financetracker

import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.navigation.findNavController
import com.adrianl.financetracker.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        setupBottomNavigation()
    }

    private fun setupBottomNavigation() {
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.home -> {
                    findNavController(R.id.nav_host_fragment).navigate(R.id.homeFragment)
                    true
                }

                R.id.mainAdd -> {
                    findNavController(R.id.nav_host_fragment).navigate(R.id.addExpensesFragment)
                    true
                }

                R.id.charts -> {
                    findNavController(R.id.nav_host_fragment).navigate(R.id.chartsFragment)
                    true
                }

                else -> false
            }
        }
    }


    fun showBtmNav() {
        binding.bottomNavigation.visibility = View.VISIBLE
    }

    fun hideBtmNav() {
        binding.bottomNavigation.visibility = View.GONE
    }

}


