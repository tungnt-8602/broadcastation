package com.example.broadcastation.presentation

import android.content.Context
import androidx.lifecycle.MutableLiveData
import com.example.broadcastation.common.base.BaseViewModel
import com.example.broadcastation.common.utility.BASE_URL
import com.example.broadcastation.common.utility.EMPTY
import com.example.broadcastation.common.utility.ERROR
import com.example.broadcastation.common.utility.getUUID
import com.example.broadcastation.entity.Remote
import com.example.broadcastation.entity.http.RetrofitAPI
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class MainViewModel : BaseViewModel() {
    /* **********************************************************************
     * Variable
     ********************************************************************** */
    val notice = MutableLiveData<String>()

    /* **********************************************************************
     * Function
     ********************************************************************** */

    fun getData(context: Context) {
        storage.deviceId = context.getUUID()
    }

    fun addRemote(remote: Remote) {
        logger.i("added")
        local.saveRemote(remote)
    }

    fun saveAllRemote(remotes: MutableList<Remote>) {
        logger.i("added")
        local.saveAllRemote(remotes)
    }

    fun getAllRemote() = local.getAllRemote()

    fun postHttp(remote: Remote){
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        val retrofitAPI = retrofit.create(RetrofitAPI::class.java)
        val call: Call<Remote?>? = retrofitAPI.postRemoteContent(remote)
        call!!.enqueue(object : Callback<Remote?> {
            override fun onResponse(call: Call<Remote?>, response: Response<Remote?>) {
                local.saveMessageBroadcast(response.body()?.describe ?: EMPTY)
            }

            override fun onFailure(call: Call<Remote?>, t: Throwable) {
                local.saveMessageBroadcast(ERROR + t.message)
            }
        })
    }

    fun shareBluetooth(remote: Remote) {
        logger.i("Bluetooth broadcast: ${remote.name}")
    }

    fun publishMqtt(remote: Remote) {
        logger.i("Mqtt broadcast: ${remote.name}")
    }

    fun getActionRemote(): String = local.getActionRemote()

    fun actionRemote(edit: String) = local.actionRemote(edit)

    fun getMessageAction() :String = local.getMessageAction()

    fun saveMessageAction(message: String) = local.saveMessageAction(message)

    fun getMessageBroadcast() :String = local.getMessageBroadcast()

    fun saveMessageBroadcast(message: String) = local.saveMessageBroadcast(message)

    /* **********************************************************************
    * Class
    ********************************************************************** */
}