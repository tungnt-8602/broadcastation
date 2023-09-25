package com.example.broadcastation.presentation

import android.content.Context
import android.view.View
import androidx.lifecycle.MutableLiveData
import com.example.broadcastation.R
import com.example.broadcastation.common.base.BaseViewModel
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
    private val BASE_URL = "https://reqres.in/api/"

//    init {
//        remoteList.add(Remote(1,"Home", "Bluetooth", 1, R.drawable.ic_local))
//        remoteList.add(Remote(2,"TV", "Http", 2, R.drawable.ic_http))
//        remoteList.add(Remote(3,"Mobile", "Bluetooth", 1, R.drawable.ic_local))
//        remoteList.add(Remote(4,"Ipad", "Http", 2, R.drawable.ic_http))
//        remoteLiveList.postValue(remoteList)
//    }
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

    fun deleteRemote(remote: Remote) {
        logger.i("added")
        remoteList.remove(remote)
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

    fun isEditRemote(): Boolean = local.isEditRemote()

    fun editRemote(edit: Boolean) = local.editRemote(edit)

    /* **********************************************************************
    * Class
    ********************************************************************** */
}