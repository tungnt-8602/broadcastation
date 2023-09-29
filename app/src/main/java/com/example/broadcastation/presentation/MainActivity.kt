package com.example.broadcastation.presentation

import android.os.Bundle
import android.os.Handler
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.broadcastation.R
import com.example.broadcastation.common.logger.Logger
import com.example.broadcastation.common.utility.DELAY_TIME_TO_QUIT
import com.example.broadcastation.common.utility.FIRST_STACK
import com.example.broadcastation.common.utility.TAG_HOME_FRAGMENT
import com.example.broadcastation.control.PermissionControl
import com.example.broadcastation.databinding.ActivityMainBinding
import com.example.broadcastation.entity.Remote
import com.example.broadcastation.presentation.home.HomeFragment
import com.google.android.material.snackbar.Snackbar

@Suppress("DEPRECATION")
class MainActivity : AppCompatActivity() {
    /* **********************************************************************
     * Variable
     ********************************************************************** */
    private lateinit var binding: ActivityMainBinding
    private val permission = PermissionControl(this)
    private val viewModel: MainViewModel by viewModels()
    private val logger = Logger.instance
    private var doubleBackToExitPressedOnce = false

    /* **********************************************************************
     * Life Cycle
     ********************************************************************** */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        logger.i("Inflate home view")
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        window.statusBarColor = getColor(R.color.scc_300)

        logger.i("Add HomeFragment")
        val fragmentManager = supportFragmentManager
        val transaction = fragmentManager.beginTransaction()
        transaction.add(
            R.id.mainContainer,
            HomeFragment.instance(callback = object : HomeFragment.Callback() {
                override fun getAllRemote(): MutableList<Remote> {
                    return viewModel.getAllRemote()
                }

                override fun getActionRemote(): String {
                    return viewModel.getActionRemote()
                }

                override fun saveMessage(message: String) {
                    viewModel.saveMessageAction(message)
                }

                override fun getDeviceName(): String {
                    return viewModel.getDeviceName()
                }

                override fun updateNotice(): String {
                    return viewModel.getMessageAction()
                }

                override fun grantBluetoothPermission() {
                    grantPermission()
                }

                override fun shareBluetooth(remote: Remote) {
                    viewModel.shareBluetooth(remote)
                }

                override fun postHttp(remote: Remote) {
//                    viewModel.postHttp(remote)
                    viewModel.getHttp()
                }

                override fun publishMqtt(remote: Remote) {
                    viewModel.publishMqtt(remote)
                }

                override fun saveMessageAction(message: String) {
                    viewModel.saveMessageAction(message)
                }

                override fun getMessageAction(): String {
                    return viewModel.getMessageAction()
                }

                override fun getMessageBroadcast(): String {
                    return viewModel.getMessageBroadcast()
                }

                override fun saveMessageBroadcast(message: String) {
                    viewModel.saveMessageBroadcast(message)
                }

                override fun addRemote(remote: Remote) {
                    viewModel.addRemote(remote)
                }

                override fun findRemoteById(id: Int): Remote {
                    return viewModel.getAllRemote().find { it.id == id }!!
                }

                override fun updateRemote(remotes: MutableList<Remote>) {
                    viewModel.saveAllRemote(remotes)
                }

            }),
            TAG_HOME_FRAGMENT
        )
            .addToBackStack(null)
            .commit()
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        if (supportFragmentManager.backStackEntryCount == FIRST_STACK) {
            if (doubleBackToExitPressedOnce) {
                super.onBackPressed()
                finish()
                return
            }
            doubleBackToExitPressedOnce = true
            Snackbar.make(binding.root, resources.getString(R.string.quit_noti), Snackbar.LENGTH_SHORT).show()

            Handler().postDelayed({
                doubleBackToExitPressedOnce =
                    false
            }, DELAY_TIME_TO_QUIT)
        } else {
            super.onBackPressed()
        }
    }

    override fun onStart() {
        viewModel.saveMessageAction("")
        super.onStart()
    }

    /* **********************************************************************
     * Function
     ********************************************************************** */
    fun grantPermission() {
        permission.registerCallback(MainActivity::class.java.name,
            object : PermissionControl.PermissionCallback {
                override fun grantSuccess() {
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
    enum class Navigate {
        UP, DOWN
    }
}