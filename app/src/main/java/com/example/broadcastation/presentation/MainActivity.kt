package com.example.broadcastation.presentation

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.activity.viewModels
import com.example.broadcastation.R
import com.example.broadcastation.common.logger.Logger
import com.example.broadcastation.control.PermissionControl
import com.example.broadcastation.databinding.ActivityMainBinding
import com.example.broadcastation.presentation.home.HomeFragment

class MainActivity : AppCompatActivity() {
    /* **********************************************************************
     * Variable
     ********************************************************************** */
    private lateinit var binding: ActivityMainBinding
    private val permission = PermissionControl(this)
    private val viewModel: MainViewModel by viewModels()
    private val logger = Logger.instance

    /* **********************************************************************
     * Life Cycle
     ********************************************************************** */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        logger.i("Inflate home view")
        viewModel.getData(this)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        window.statusBarColor = getColor(R.color.transparent)

        logger.i("Add HomeFragment")
        val fragmentManager = supportFragmentManager
        val transaction = fragmentManager.beginTransaction()
        transaction.add(R.id.mainContainer, HomeFragment(), "home")
            .addToBackStack(null)
            .commit()
    }
}