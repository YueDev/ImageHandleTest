package com.yuedev.imagehandletest

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import com.yuedev.imagehandletest.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private val navController by lazy {
        findNavController(R.id.fragment)
    }

    private val appBarConfig by lazy {
        AppBarConfiguration(navController.graph)
    }

    private val binding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        setupActionBarWithNavController(navController, appBarConfig)

    }

    override fun onSupportNavigateUp() = navController.navigateUp() || super.onSupportNavigateUp()
}