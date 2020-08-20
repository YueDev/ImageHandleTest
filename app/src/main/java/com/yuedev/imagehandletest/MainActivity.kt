package com.yuedev.imagehandletest

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController

class MainActivity : AppCompatActivity() {

    private val navController by lazy {
        findNavController(R.id.fragment)
    }

    private val appBarConfig by lazy {
        AppBarConfiguration(navController.graph)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setupActionBarWithNavController(navController, appBarConfig)

    }

    override fun onSupportNavigateUp() = navController.navigateUp() || super.onSupportNavigateUp()
}