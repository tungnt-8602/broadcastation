package com.example.broadcastation.presentation

import android.content.Context
import android.view.View
import androidx.lifecycle.MutableLiveData
import com.example.broadcastation.R
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

class MainViewModel : BaseViewModel(){
    /* **********************************************************************
     * Variable
     ********************************************************************** */
    var remoteLiveList = MutableLiveData<MutableList<Remote>>()
    private var remoteList = mutableListOf<Remote>()
    val notice = MutableLiveData<String>()

    /* **********************************************************************
     * Function
     ********************************************************************** */

    fun getData(context: Context){
        storage.deviceId = context.getUUID()
    }

    fun addRemote(remote: Remote) {
        logger.i("added")
        local.saveRemote(remote)
        remoteList.add(remote)
        remoteLiveList.postValue(remoteList)
    }

    fun getAllRemote() = local.getAllRemote()

    fun postHttp(remote: Remote, loadView: View){
        loadView.visibility = View.VISIBLE
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        val retrofitAPI = retrofit.create(RetrofitAPI::class.java)
        val call: Call<Remote?>? = retrofitAPI.postRemoteContent(remote)
        call!!.enqueue(object : Callback<Remote?> {
            override fun onResponse(call: Call<Remote?>, response: Response<Remote?>) {
                loadView.visibility = View.GONE
                notice.value = response.body()?.describe ?: EMPTY
            }

            override fun onFailure(call: Call<Remote?>, t: Throwable) {
                loadView.visibility = View.GONE
                notice.value = ERROR + t.message
            }
        })
    }

    fun shareBluetooth(remote: Remote, loadView: View){
        notice.value = "Bluetooth broadcast: ${remote.name}"
    }

    fun publishMqtt(remote: Remote, loadView: View){
        notice.value = "Mqtt broadcast: ${remote.name}"
    }

    fun getEditRemote(): String = local.getEditRemote()

    fun editRemote(edit: String) = local.editRemote(edit)

    /* **********************************************************************
    * Class
    ********************************************************************** */
}