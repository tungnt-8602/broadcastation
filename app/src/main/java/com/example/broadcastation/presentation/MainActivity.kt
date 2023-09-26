package com.example.broadcastation.presentation

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.lifecycle.LifecycleOwner
import com.example.broadcastation.R
import com.example.broadcastation.common.logger.Logger
import com.example.broadcastation.common.utility.MES_ADD_SUCCESS
import com.example.broadcastation.control.PermissionControl
import com.example.broadcastation.databinding.ActivityMainBinding
import com.example.broadcastation.entity.Remote
import com.example.broadcastation.presentation.home.HomeFragment
import com.google.android.material.snackbar.Snackbar

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
        window.statusBarColor = getColor(R.color.scc_100)

        logger.i("Add HomeFragment")
        val fragmentManager = supportFragmentManager
        val transaction = fragmentManager.beginTransaction()
        transaction.add(R.id.mainContainer, HomeFragment(callback = object : HomeFragment.Callback() {
            override fun getAllRemote(): MutableList<Remote> {
                logger.i("mutable : ${viewModel.getAllRemote()}")
                return viewModel.getAllRemote()
            }

            override fun updateNotice(owner: LifecycleOwner, view: View) {
                viewModel.notice.observe(owner) { message ->
                    logger.d(message)
                    Snackbar.make(binding.root, message, Snackbar.LENGTH_SHORT).setAnchorView(view)
                        .show()
                }
            }

            override fun grantBluetoothPermission() {
                grantPermission()
            }

            override fun addRemote(remote: Remote) {
//                viewModel.notice.value = MES_ADD_SUCCESS
                viewModel.addRemote(remote)
            }

            override fun updateRemote(remotes: MutableList<Remote>) {
                viewModel.savAllRemote(remotes)
            }

        }), "home")
            .addToBackStack(null)
            .commit()
    }

    /* **********************************************************************
     * Function
     ********************************************************************** */
     fun grantPermission(){
        permission.registerCallback(MainActivity::class.java.name,
            object : PermissionControl.PermissionCallback {
                override fun grantSuccess(){
                    permission.turnOnBluetooth()
                }

                override fun grantFail(error: String) {
                    logger.e(error)
                }
            })
        permission.grantPermissions()
    }
    /* **********************************************************************
     * Class
     ********************************************************************** */

}