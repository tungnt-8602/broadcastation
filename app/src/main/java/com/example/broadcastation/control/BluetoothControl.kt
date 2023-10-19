package com.example.broadcastation.control

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.le.AdvertiseCallback
import android.bluetooth.le.AdvertiseData
import android.bluetooth.le.AdvertiseSettings
import android.bluetooth.le.BluetoothLeAdvertiser
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.ParcelUuid
import android.provider.Settings
import com.example.broadcastation.common.base.BaseControl
import com.example.broadcastation.common.utility.getDeviceName

@SuppressLint("MissingPermission")
class BluetoothControl(val callback: Callback) : BaseControl() {
    /* **********************************************************************
     * Variable
     ********************************************************************** */
    private val adapterNameDefault = "Broadcastation"
    private var isChangeName: Boolean? = false
    private var adapterName = adapterNameDefault
    private var message = ""
    private var isPowerInclude = true
    private var nameMaxLength = 10
    private var bluetoothAdapter: BluetoothAdapter? = null
    private var bluetoothAdvertiser: BluetoothLeAdvertiser? = null
    private var bluetoothAdvertiserSetting: AdvertiseSettings? = null
    private var bluetoothAdvertiserData: AdvertiseData? = null

    /* **********************************************************************
    * Function
    ********************************************************************** */
    fun init(context: Context) {
        logger.i("system checking...")
        val isSystemSupported =
            context.packageManager.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)
        if (!isSystemSupported) {
            logger.e("error: Not Support FEATURE_BLUETOOTH_LE")
            return
        }

        logger.i("create bluetooth adapter")
        adapterName = getName(context)
        callback.getAdvertisingName(adapterName)
        try {
            bluetoothAdapter = if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                val bluetoothManager =
                    context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
                bluetoothManager.adapter
            } else {
                val bluetoothManager = context.getSystemService(BluetoothManager::class.java)
                bluetoothManager.adapter
            }
        } catch (e: Exception) {
            logger.w(e.message ?: "Bluetooth adapter initialization")
        }

    }

    private fun getName(context: Context): String {
        val display = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
            Settings.Global.getString(context.contentResolver, Settings.Global.DEVICE_NAME)
        } else {
            ""
        }
        val device = getDeviceName()
        val manufacture = Build.MANUFACTURER
        val model = Build.MODEL

        return if (display.isNotEmpty() && display.length < nameMaxLength) {
            display
        } else if (device.isNotEmpty() && device.length < nameMaxLength) {
            device
        } else if (manufacture.isNotEmpty() && manufacture.length < nameMaxLength) {
            manufacture
        } else if (model.isNotEmpty() && model.length < nameMaxLength) {
            model
        } else {
            adapterNameDefault
        }
    }

    private fun verifyAdvertise(): Boolean {
        return if (bluetoothAdapter == null) {
            logger.e("error: can't create adapter. Please call init() first")
            false
        } else if (bluetoothAdvertiser == null) {
            logger.e("error: can't create scanner. Please call init() first")
            false
        } else {
            true
        }
    }

    /* **********************************************************************
     * Advertising
     ********************************************************************** */
    private fun parcelFromShortValue(uuidShortValue: Int): ParcelUuid? {
        val data = String.format(
            "%04X", uuidShortValue and 0xffff
        )
        return ParcelUuid.fromString(
            "0000$data-0000-1000-8000-00805F9B34FB"
        )
    }

    private fun initAdvertising() {
        logger.i("set name")
        isChangeName = bluetoothAdapter?.setName(adapterName)

        logger.i("create bluetooth advertiser")
        bluetoothAdvertiser = bluetoothAdapter?.bluetoothLeAdvertiser

        logger.i("create bluetooth advertiser data")
        val pUuid = parcelFromShortValue(1234)
        val advertiseString = message
        logger.d("advertiser name[${adapterName}] - pUuid[$pUuid]")
        bluetoothAdvertiserData = AdvertiseData.Builder().apply {
            setIncludeDeviceName(isChangeName ?: false)
            setIncludeTxPowerLevel(isPowerInclude)
            addServiceData(
                pUuid, advertiseString.toByteArray(Charsets.UTF_8)
            )
        }.build()

        logger.i("create bluetooth advertiser setting")
        bluetoothAdvertiserSetting = AdvertiseSettings.Builder().apply {
            setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY)
            setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_HIGH)
            setTimeout(0)
            setConnectable(false)
        }.build()
        logger.i("The Tung")
    }

    fun startAdvertising(name: String, message: String = "") {
        adapterName = name
        this.message = message
        initAdvertising()
        logger.i("bluetooth advertise start $bluetoothAdvertiser")
        if (!verifyAdvertise()) {
            return
        }

        try {
            logger.i("bluetooth advertise start $bluetoothAdvertiser")
            bluetoothAdvertiser?.startAdvertising(
                bluetoothAdvertiserSetting, bluetoothAdvertiserData, bluetoothAdvertiseCallback
            )
        } catch (e: Exception) {
            logger.w(e.message ?: "startAdvertising")
        }
    }

    fun stopAdvertising() {
        if (!verifyAdvertise()) {
            return
        }

        try {
            logger.i("bluetooth advertise stop $bluetoothAdvertiser")
            bluetoothAdvertiser?.stopAdvertising(bluetoothAdvertiseCallback)
        } catch (e: Exception) {
            logger.w(e.message ?: "stopAdvertising")
        }
    }

    private val bluetoothAdvertiseCallback = object : AdvertiseCallback() {
        override fun onStartSuccess(settingsInEffect: AdvertiseSettings?) {
            super.onStartSuccess(settingsInEffect)
            logger.i("advertiser[$adapterName] success [$settingsInEffect]")
            callback.getAdvertisingName(adapterName)
            callback.notifyAdvertise("Advertising Success")
        }

        override fun onStartFailure(errorCode: Int) {
            super.onStartFailure(errorCode)
            logger.w("advertiser[$adapterName] fail code[$errorCode]")
            callback.notifyAdvertise(errorCode.toString())
            when (errorCode) {
                ADVERTISE_FAILED_ALREADY_STARTED -> logger.w("ADVERTISE_FAILED_ALREADY_STARTED")
                ADVERTISE_FAILED_DATA_TOO_LARGE -> logger.w("ADVERTISE_FAILED_DATA_TOO_LARGE")
                ADVERTISE_FAILED_FEATURE_UNSUPPORTED -> logger.w("ADVERTISE_FAILED_FEATURE_UNSUPPORTED")
                ADVERTISE_FAILED_INTERNAL_ERROR -> logger.w("ADVERTISE_FAILED_INTERNAL_ERROR")
                ADVERTISE_FAILED_TOO_MANY_ADVERTISERS -> logger.w("ADVERTISE_FAILED_TOO_MANY_ADVERTISERS")
                else -> logger.w("Unhandled error: $errorCode")
            }
        }
    }

    /* **********************************************************************
     * Class
     ********************************************************************** */
    interface Callback {
        fun getAdvertisingName(name: String)
        fun notifyAdvertise(error: String)
    }
}