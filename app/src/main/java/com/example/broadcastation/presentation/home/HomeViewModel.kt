package com.example.broadcastation.presentation.home

import android.view.View
import androidx.lifecycle.MutableLiveData
import com.example.broadcastation.R
import com.example.broadcastation.common.base.BaseViewModel
import com.example.broadcastation.entity.Remote
import com.example.broadcastation.entity.http.RetrofitAPI
import com.google.android.material.snackbar.Snackbar
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


class HomeViewModel : BaseViewModel() {
    /* **********************************************************************
     * Variable
     ********************************************************************** */
    var remoteLiveList = MutableLiveData<MutableList<Remote>>()
    var remoteList = mutableListOf<Remote>()
    private val BASE_URL = "https://reqres.in/api/"
    val notice = MutableLiveData<String>()

    init {
        remoteList.add(Remote("Home", "Bluetooth", 1, R.drawable.ic_local))
        remoteList.add(Remote("TV", "Http", 2, R.drawable.ic_http))
        remoteList.add(Remote("Mobile", "Bluetooth", 1, R.drawable.ic_local))
        remoteList.add(Remote("Ipad", "Http", 2, R.drawable.ic_http))
        remoteList.add(Remote("Web", "Mqtt", 3, R.drawable.ic_mqtt))
        remoteList.add(Remote("Ipad", "Http", 2, R.drawable.ic_http))
        remoteList.add(Remote("Web", "Mqtt", 3, R.drawable.ic_mqtt))
        remoteList.add(Remote("Ipad", "Http", 2, R.drawable.ic_http))
        remoteList.add(Remote("Web", "Mqtt", 3, R.drawable.ic_mqtt))
        remoteList.add(Remote("TV", "Http", 2, R.drawable.ic_http))
        remoteList.add(Remote("Mobile", "Bluetooth", 1, R.drawable.ic_local))
        remoteList.add(Remote("TV", "Http", 2, R.drawable.ic_http))
        remoteList.add(Remote("Mobile", "Bluetooth", 1, R.drawable.ic_local))
        remoteList.add(Remote("Mobile", "Bluetooth", 1, R.drawable.ic_local))
        remoteList.add(Remote("Ipad", "Http", 2, R.drawable.ic_http))
        remoteList.add(Remote("Web", "Mqtt", 3, R.drawable.ic_mqtt))
        remoteList.add(Remote("Mobile", "Bluetooth", 1, R.drawable.ic_local))
        remoteList.add(Remote("Ipad", "Http", 2, R.drawable.ic_http))
        remoteList.add(Remote("Web", "Mqtt", 3, R.drawable.ic_mqtt))
        remoteLiveList.postValue(remoteList)
    }

    /* **********************************************************************
     * Function
     ********************************************************************** */
    fun addRemote(remote: Remote) {
        logger.i("added")
        remoteList.add(remote)
        remoteLiveList.postValue(remoteList)
    }

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
                notice.value = response.body()?.describe ?: "Nothing to show"
            }

            override fun onFailure(call: Call<Remote?>, t: Throwable) {
                loadView.visibility = View.GONE
                notice.value = "Error found is : " + t.message
            }
        })
    }

    fun shareBluetooth(remote: Remote, loadView: View){
        notice.value = "Bluetooth broadcast: ${remote.name}"
    }

    fun publishMqtt(remote: Remote, loadView: View){
        notice.value = "Mqtt broadcast: ${remote.name}"
    }

    /* **********************************************************************
    * Class
    ********************************************************************** */
}