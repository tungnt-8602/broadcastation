package com.example.broadcastation

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.os.PowerManager
import androidx.annotation.RequiresApi
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.example.broadcastation.common.logger.Logger
import com.example.broadcastation.control.BluetoothControl
import com.google.gson.Gson

class BroadcastService : Service() {
    /* **********************************************************************
     * Variable
     ********************************************************************** */
    private val logger = Logger.instance
    private val gson = Gson()
    private var wakeLock: PowerManager.WakeLock? = null

    private val bluetooth = BluetoothControl(callback = object : BluetoothControl.Callback {
        override fun getAdvertisingName(name: String) {
            messageAdvertiseName(name)
        }

        override fun notifyAdvertise(error: String) {
            messageErrorAdvertise(error)
        }

    })

    /* **********************************************************************
     * Companion
     ********************************************************************** */
    companion object {
        private const val NAME = "BleService"
        private const val STA = "ServiceToApp"
        private const val ATS = "AppToService"

        const val STA_ACTION = "$NAME:$STA:Action"
        const val STA_DEVICE_DATA = "$NAME:$STA:DeviceData"
        const val STA_ADVERTISING_NAME = "$NAME:$STA:AdvertisingName"
        const val STA_ADVERTISING_ERROR = "$NAME:$STA:AdvertisingError"
        const val STA_ADVERTISING_MESSAGE = "$NAME:$STA:AdvertisingMessage"

        private const val ATS_ACTION = "$NAME:$ATS:Action"
        private const val ATS_ACTION_INIT = "$NAME:$ATS:Init"
        private const val ATS_ACTION_SET_DATA = "$NAME:$ATS:SetData"
        private const val ATS_ACTION_START_SCAN = "$NAME:$ATS:StartScan"
        private const val ATS_ACTION_STOP_SCAN = "$NAME:$ATS:StopScan"
        private const val ATS_ACTION_REFRESH_SCAN = "$NAME:$ATS:RefreshScan"
        private const val ATS_ACTION_START_ADVERTISE = "$NAME:$ATS:StartAdvertise"
        private const val ATS_ACTION_STOP_ADVERTISE = "$NAME:$ATS:StopAdvertise"
        private const val ATS_SERVICE_DATA = "$NAME:$ATS:ServiceData"
        private const val ATS_ADVERTISE_DATA = "$NAME:$ATS:AdvertiseData"
        private const val ATS_RESTART_MQTT = "$NAME:$ATS:Mqtt"

        fun initService(context: Context) {
            val intent = Intent(context, BroadcastService::class.java)
            val bundle = Bundle()
            bundle.putString(ATS_ACTION, ATS_ACTION_INIT)
            intent.putExtras(bundle)
            bindService(context, intent)
        }

        private fun bindService(context: Context, intent: Intent) {
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//                context.startForegroundService(intent)
//            } else {
            context.startService(intent)
//            }
        }

        fun stopService(context: Context) {
            val intent = Intent(context, BroadcastService::class.java)
            context.stopService(intent)
        }

        fun startAdvertise(context: Context, data: AdvertiseData) {
            val intent = Intent(context, BroadcastService::class.java)
            val bundle = Bundle()
            bundle.putString(ATS_ACTION, ATS_ACTION_START_ADVERTISE)
            bundle.putString(ATS_ADVERTISE_DATA, Gson().toJson(data))
            intent.putExtras(bundle)
            bindService(context, intent)
        }

        fun stopAdvertise(context: Context) {
            val intent = Intent(context, BroadcastService::class.java)
            intent.putExtra(ATS_ACTION, ATS_ACTION_STOP_ADVERTISE)
            bindService(context, intent)
        }
    }

    /* **********************************************************************
     * Life Cycle
     ********************************************************************** */

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate() {
        super.onCreate()
        logger.d("onCreate")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        logger.i("init bluetooth")
        intent?.extras?.let { bundle ->
            val action = bundle.getString(ATS_ACTION, null)
            action?.let {
                try {
                    when (it) {
                        ATS_ACTION_INIT -> {
                            bluetooth.init(this)
                        }

                        ATS_ACTION_START_ADVERTISE -> {
                            logger.i("start advertise")
                            val content = bundle.getString(ATS_ADVERTISE_DATA, "")
                            val data = gson.fromJson(content, AdvertiseData::class.java)
                            bluetooth.startAdvertising(
                                name = data.name,
                                message = data.message,
                            )
                        }

                        ATS_ACTION_STOP_ADVERTISE -> {
                            logger.i("stop advertise")
                            bluetooth.stopAdvertising()
                        }

                        else -> {}
                    }
                } catch (e: Exception) {
                    logger.w(e.message ?: "onStartCommand")
                }
            }
        }
        try {
            bluetooth.init(context = this)
        } catch (e: Exception) {
            logger.w(e.message ?: "bluetooth.init")
        }
        return START_STICKY
    }

    override fun onBind(p0: Intent?): IBinder? {
        return null
    }

    override fun onDestroy() {
        super.onDestroy()
        logger.d("Destroy")
        bluetooth.stopAdvertising()
        wakeLock?.release()
    }

    /* **********************************************************************
     * Service to Application
     ********************************************************************** */
    private fun messageAdvertiseName(name: String) {
        val intent = Intent(STA_ACTION)
        val bundle = Bundle()
        bundle.putString(STA_ADVERTISING_NAME, name)
        intent.putExtras(bundle)
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
    }

    private fun messageErrorAdvertise(error: String) {
        val intent = Intent(STA_ACTION)
        val bundle = Bundle()
        bundle.putString(STA_ADVERTISING_ERROR, error)
        intent.putExtras(bundle)
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
    }

    /* **********************************************************************
     * Class
     ********************************************************************** */
    data class AdvertiseData(
        val name: String, val message: String = ""
    )

}