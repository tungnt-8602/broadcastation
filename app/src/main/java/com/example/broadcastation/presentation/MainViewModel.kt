package com.example.broadcastation.presentation

import android.content.Context
import android.os.Build
import androidx.lifecycle.viewModelScope
import com.example.broadcastation.R
import com.example.broadcastation.common.base.BaseViewModel
import com.example.broadcastation.common.utility.EMPTY
import com.example.broadcastation.common.utility.ERROR
import com.example.broadcastation.common.utility.GET_SUCCESS
import com.example.broadcastation.entity.Remote
import com.example.broadcastation.entity.config.Config
import com.example.broadcastation.entity.config.HttpConfig
import com.example.broadcastation.entity.http_api.RetrofitAPI
import com.example.broadcastation.presentation.home.HomeFragment
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class MainViewModel : BaseViewModel() {
    /* **********************************************************************
     * Variable
     ********************************************************************** */
    val noticeQuit = R.string.quit_noti
    val colorStatusBar = R.color.scc_300

//    val deviceNoticeId = mutableMapOf<String, Int>()

    /* **********************************************************************
     * Function
     ********************************************************************** */
    fun mqtt(context: Context, isStart: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            if (isStart) {
                remote.createConnect(context)
            } else {
                remote.stopConnect()
            }
        }
    }

//    fun getDeviceMessage(data: String): BroadcastService.AdvertiseData? {
//        return gson.fromJson(data, BroadcastService.AdvertiseData::class.java)
//    }

    fun getDeviceName(): String = Build.MODEL

    fun addRemote(remote: Remote) {
        logger.i("added")
        local.saveRemote(remote)
    }

    fun saveAllRemote(remotes: MutableList<Remote>) {
        logger.i("added")
        local.saveAllRemote(remotes)
    }

    fun getAllRemote() = local.getAllRemote()

    fun postHttp(remote: Remote) {
        val type = object : TypeToken<HttpConfig>() {}.type
        val config: Config = gson.fromJson(remote.config, type)
        val url = (config as HttpConfig).url
        try {
            val retrofit = Retrofit.Builder()
                .baseUrl(url)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
            val retrofitAPI = retrofit.create(RetrofitAPI::class.java)
            val call: Call<Remote> = retrofitAPI.postRemoteContent(remote)
            call.enqueue(object : Callback<Remote?> {
                override fun onResponse(call: Call<Remote?>, response: Response<Remote?>) {
                    local.saveMessageBroadcast(response.body()?.describe ?: EMPTY)
                    logger.i("Post Http with body: ${response.body()}")
                }

                override fun onFailure(call: Call<Remote?>, t: Throwable) {
                    local.saveMessageBroadcast(ERROR + t.message)
                }
            })
        } catch (e: Exception) {
            local.saveMessageBroadcast(e.toString())
            logger.i("Invalid Url: $e")
        }
    }

    fun getHttp(remote: Remote) {
        val type = object : TypeToken<HttpConfig>() {}.type
        val config: Config = gson.fromJson(remote.config, type)
        val url = (config as HttpConfig).url
        try {
            val retrofit = Retrofit.Builder()
                .baseUrl(url)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
            val retrofitAPI = retrofit.create(RetrofitAPI::class.java)
            val call = retrofitAPI.getContent()
            call.enqueue(object : Callback<Any> {
                override fun onResponse(call: Call<Any>, response: Response<Any>) {
                    if (response.body().toString().isNotEmpty()) {
                        local.saveMessageBroadcast("$GET_SUCCESS $url")
                        logger.i("Get Http with body: ${response.body()}")
                    }
                }

                override fun onFailure(call: Call<Any>, t: Throwable) {
                    local.saveMessageBroadcast(ERROR + t.message)
                }
            })
        } catch (e: Exception) {
            local.saveMessageBroadcast(e.toString())
            logger.i("Invalid Url: $e")
        }
    }

    fun shareBluetooth(remoteR: Remote, callback: HomeFragment.Callback) {
        logger.i("Bluetooth broadcast: ${remoteR.name}")
        callback.startAdvertise(remoteR.name, remoteR.describe)
    }

    fun publishMqtt(remoteR: Remote, callback: HomeFragment.Callback, context: Context) {
        logger.i("Mqtt broadcast: ${remoteR.name}")
        remote.createConnect(context)
        callback.saveMessageBroadcast(remoteR.describe)
        remote.sendMessage(callback.getMessageBroadcast(), context)
    }

    fun setAdvertiseName(name: String?) {
        if (name != null) {
            storage.advertisingName = name
        }
    }

    fun getActionRemote(): String = local.getActionRemote()

    fun actionRemote(edit: String) = local.actionRemote(edit)

    fun getMessageAction(): String = local.getMessageAction()

    fun saveMessageAction(message: String) = local.saveMessageAction(message)

    fun getMessageBroadcast(): String = local.getMessageBroadcast()

    fun saveMessageBroadcast(message: String) = local.saveMessageBroadcast(message)

    /* **********************************************************************
    * Class
    ********************************************************************** */
}