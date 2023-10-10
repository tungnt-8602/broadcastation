package com.example.broadcastation.presentation

import android.bluetooth.BluetoothManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.os.Handler
import android.os.StrictMode
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentFactory
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.example.broadcastation.BroadcastService
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
     * Broadcast & Service
     ********************************************************************** */
    private val bleServiceReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val bundle = intent.extras ?: return

            logger.i("get advertise name")
            val advertiseName = bundle.getString(BroadcastService.STA_ADVERTISING_NAME)
            if (!advertiseName.isNullOrEmpty()) {
                viewModel.setAdvertiseName(advertiseName)
            }

            logger.i("error advertise name")
            val notify = bundle.getString(BroadcastService.STA_ADVERTISING_ERROR)
            notify?.let { Snackbar.make(binding.root, "$notify", Snackbar.LENGTH_SHORT).show() }

        }
    }


    /* **********************************************************************
     * Life Cycle
     ********************************************************************** */

    override fun onCreate(savedInstanceState: Bundle?) {
        val fm = supportFragmentManager
        fm.fragmentFactory = CustomFragmentFactory()
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
            HomeFragment(callback = object : HomeFragment.Callback {
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

                override fun grantBluetoothPermission(
                    remote: Remote,
                    callback: HomeFragment.Callback
                ) {
                    grantPermission(remote, callback)
                }

                override fun shareBluetooth(remote: Remote, callback: HomeFragment.Callback) {
                    viewModel.shareBluetooth(remote, callback)
                }

                override fun postHttp(remote: Remote) {
                    viewModel.postHttp(remote)
                }

                override fun getHttp(remote: Remote) {
                    viewModel.getHttp()
                }

                override fun publishMqtt(remote: Remote, callback: HomeFragment.Callback) {
                    viewModel.publishMqtt(remote, callback)
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

                override fun startAdvertise(advertise: String, message: String) {
                    permission.turnOnBluetooth()
                    BroadcastService.startAdvertise(
                        this@MainActivity,
                        BroadcastService.AdvertiseData(advertise, message)
                    )
                }

                override fun stopAdvertise() {
                    BroadcastService.stopAdvertise(this@MainActivity)
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

        logger.i("register broadcast")
        val filter = IntentFilter(BroadcastService.STA_ACTION)
        LocalBroadcastManager.getInstance(this).registerReceiver(bleServiceReceiver, filter)

        val builder = StrictMode.VmPolicy.Builder()
        StrictMode.setVmPolicy(builder.build())
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
            Snackbar.make(
                binding.root,
                resources.getString(R.string.quit_noti),
                Snackbar.LENGTH_SHORT
            ).show()

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
        startService()
        super.onStart()
    }

    override fun onDestroy() {
        try {
            logger.i("stop services")
            BroadcastService.stopService(this)
        } catch (e: Exception) {
            logger.w(e.message ?: "onDestroy")
        }
        super.onDestroy()
    }

    /* **********************************************************************
     * Function
     ********************************************************************** */
    fun grantPermission(remote: Remote, callback: HomeFragment.Callback) {
        permission.registerCallback(MainActivity::class.java.name,
            object : PermissionControl.PermissionCallback {
                override fun grantSuccess() {
                    permission.turnOnBluetooth()
                    val isTurnedOn =
                        getSystemService(BluetoothManager::class.java)?.adapter?.isEnabled
                    if (isTurnedOn == true) {
                        viewModel.shareBluetooth(remote, callback)
                        return
                    }
                }

                override fun grantFail(error: String) {
                    logger.e(error)
                }
            })

        permission.grantPermissions()
    }

    private fun startService() {
        logger.i("start service")
        BroadcastService.initService(this)
    }

    /* **********************************************************************
     * Class
     ********************************************************************** */
    enum class Navigate {
        UP, DOWN
    }

    inner class CustomFragmentFactory : FragmentFactory() {
        override fun instantiate(classLoader: ClassLoader, className: String): Fragment {
            if (className == HomeFragment::class.java.name) {
                return HomeFragment(object : HomeFragment.Callback {
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

                    override fun grantBluetoothPermission(
                        remote: Remote,
                        callback: HomeFragment.Callback
                    ) {
                        grantPermission(remote, callback)
                    }

                    override fun shareBluetooth(remote: Remote, callback: HomeFragment.Callback) {
                        viewModel.shareBluetooth(remote, callback)
                    }

                    override fun postHttp(remote: Remote) {
                        viewModel.postHttp(remote)

                    }

                    override fun getHttp(remote: Remote) {
                        viewModel.getHttp()
                    }

                    override fun publishMqtt(remote: Remote, callback: HomeFragment.Callback) {
                        viewModel.publishMqtt(remote, callback)
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

                    override fun startAdvertise(advertise: String, message: String) {
                        permission.turnOnBluetooth()
                        BroadcastService.startAdvertise(
                            this@MainActivity,
                            BroadcastService.AdvertiseData(advertise, message)
                        )
                    }

                    override fun stopAdvertise() {
                        BroadcastService.stopAdvertise(this@MainActivity)
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

                })
            }
            return super.instantiate(classLoader, className)
        }
    }
}