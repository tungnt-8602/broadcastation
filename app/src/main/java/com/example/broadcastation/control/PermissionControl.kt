package com.example.broadcastation.control

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.example.broadcastation.common.base.BaseControl

class PermissionControl(private val activity: AppCompatActivity)  : BaseControl() {
    /* **********************************************************************
     * Variable
     ********************************************************************** */
    private val maps = hashMapOf<String, PermissionCallback>()

    /* **********************************************************************
     * Callback
     ********************************************************************** */
    fun registerCallback(tag: String, callback: PermissionCallback) {
        maps[tag] = callback
    }

    private fun callbackCall(action: CallbackAction, message: String = "") {
        for (key in maps.keys) {
            maps[key]?.let {
                when (action) {
                    CallbackAction.SUCCESS -> it.grantSuccess()
                    CallbackAction.FAIL -> it.grantFail(message)
                }
            }
        }
    }

    enum class CallbackAction {
        SUCCESS, FAIL
    }

    /* **********************************************************************
     * On/Off
     ********************************************************************** */
    private val requestOnOffBluetooth =
        activity.registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == AppCompatActivity.RESULT_OK) {
                logger.i("Granted : ACTION_REQUEST_ENABLE BLUETOOTH")
            } else {
                logger.i("Denied : ACTION_REQUEST_ENABLE BLUETOOTH")
                callbackCall(CallbackAction.FAIL)
            }
        }

    fun turnOnBluetooth() {
        val bluetoothManager = activity.getSystemService(BluetoothManager::class.java)
        val bluetoothAdapter = bluetoothManager.adapter
        if (bluetoothAdapter != null && !bluetoothAdapter.isEnabled) {
            val intent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            requestOnOffBluetooth.launch(intent)
        }
    }

    fun openSettingPermission() {
        val intent = Intent(
            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
            Uri.parse("package:" + activity.packageName)
        )
        requestSetting.launch(intent)
    }

    fun grantPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            permissionS()
        } else {
            permissionBelowS()
        }
    }

    @RequiresApi(Build.VERSION_CODES.S)
    private fun permissionS() {
        val permissions = arrayListOf(
            Manifest.permission.BLUETOOTH,
            Manifest.permission.BLUETOOTH_ADMIN,
            Manifest.permission.BLUETOOTH_SCAN,
            Manifest.permission.BLUETOOTH_CONNECT,
            Manifest.permission.BLUETOOTH_ADVERTISE
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions.add(Manifest.permission.POST_NOTIFICATIONS)
        }
        requestM.launch(permissions.toTypedArray())
    }

    private fun permissionBelowS() {
        actionEnableBelowS()

        val permissions = arrayListOf(
            Manifest.permission.BLUETOOTH,
            Manifest.permission.BLUETOOTH_ADMIN,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
        )
        requestM.launch(permissions.toTypedArray())
    }

    private fun actionEnableBelowS() {
        val intent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
        requestO.launch(intent)
    }

    /* **********************************************************************
     * Grant
     ********************************************************************** */
    private val requestM = activity.registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { results ->
        var isGrantAll = true
        results.entries.forEach {
            val permissionName = it.key
            val isGranted = it.value
            if (isGranted) {
                logger.i("Granted : $permissionName")
            } else {
                logger.i("Denied : $permissionName")
                isGrantAll = false
            }
        }
        callbackCall(if (isGrantAll) CallbackAction.SUCCESS else CallbackAction.FAIL)
    }

    private val requestO =
        activity.registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == AppCompatActivity.RESULT_OK) {
                logger.i("Granted : ACTION_REQUEST_ENABLE")
            } else {
                logger.i("Denied : ACTION_REQUEST_ENABLE")
                callbackCall(CallbackAction.FAIL, "${result.resultCode}")
            }
        }

    private val requestSetting =
        activity.registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == AppCompatActivity.RESULT_OK) {
                logger.i("Granted : ACTION_REQUEST_ENABLE")
            } else {
                logger.i("Denied : ACTION_REQUEST_ENABLE")
            }
        }

    /* **********************************************************************
     * Other
     ********************************************************************** */
    interface PermissionCallback {
        fun grantSuccess()
        fun grantFail(error: String)
    }
}