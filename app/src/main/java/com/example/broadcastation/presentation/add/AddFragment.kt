package com.example.broadcastation.presentation.add

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ListPopupWindow
import androidx.annotation.RequiresApi
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import com.example.broadcastation.R
import com.example.broadcastation.common.base.BaseFragment
import com.example.broadcastation.common.utility.GET_METHOD
import com.example.broadcastation.common.utility.ID_ARG
import com.example.broadcastation.common.utility.ID_REQUEST_KEY
import com.example.broadcastation.common.utility.PASSWORD
import com.example.broadcastation.common.utility.POST_METHOD
import com.example.broadcastation.common.utility.TAG_ADD_FRAGMENT
import com.example.broadcastation.common.utility.TAG_UPDATE_FRAGMENT
import com.example.broadcastation.common.utility.USER_NAME
import com.example.broadcastation.common.utility.showCategoryDialog
import com.example.broadcastation.common.utility.showMenu
import com.example.broadcastation.databinding.AddFragmentBinding
import com.example.broadcastation.entity.BluetoothConfig
import com.example.broadcastation.entity.MqttConfig
import com.example.broadcastation.entity.Remote
import com.example.broadcastation.entity.http.HttpConfig
import com.example.broadcastation.presentation.home.HomeFragment
import com.example.broadcastation.presentation.home.ItemRemoteAdapter
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.lang.reflect.Type

class AddFragment(private val callback: HomeFragment.Callback) :
    BaseFragment<AddFragmentBinding>(AddFragmentBinding::inflate) {
    /* **********************************************************************
     * Variable
     ********************************************************************** */
    private var typeBroadcast: ItemRemoteAdapter.Type = ItemRemoteAdapter.Type.BLUETOOTH
    private lateinit var remoteUpdate: Remote
    private val addViewModel: AddViewModel by viewModels()

    /* **********************************************************************
     * Life Cycle
     ********************************************************************** */
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (!isAdded) {
            return
        }
        initView()
        addViewModel.notice.observe(viewLifecycleOwner) {
            Snackbar.make(binding.root, it, Snackbar.LENGTH_SHORT).setAnchorView(binding.saveRemote)
                .show()
        }
    }


    /* ***********************************************************************
     * Function
     ********************************************************************** */
    private fun saveInputAsRemote(
        name: String,
        describe: String,
        category: String,
        type: ItemRemoteAdapter.Type,
        callback: HomeFragment.Callback
    ) {
        val icon: Int
        val gson = Gson()
        val config: String
        logger.i("Handle icon, type and config")
        when (type) {
            ItemRemoteAdapter.Type.HTTP -> {
                icon = R.drawable.ic_http
                config = gson.toJson(
                    HttpConfig(
                        binding.http.httpUrlText.text.toString(),
                        when (binding.http.httpMethod.text.toString().uppercase()) {
                            POST_METHOD -> ItemRemoteAdapter.HttpMethod.POST
                            else -> ItemRemoteAdapter.HttpMethod.GET
                        },
                        binding.http.httpContentText.text.toString()
                    )
                )
            }

            ItemRemoteAdapter.Type.BLUETOOTH -> {
                icon = R.drawable.ic_local
                config = gson.toJson(
                    BluetoothConfig(
                        binding.local.deviceIdText.text.toString(),
                        binding.local.localContentText.text.toString()
                    )
                )
            }

            ItemRemoteAdapter.Type.MQTT -> {
                icon = R.drawable.ic_mqtt
                config = gson.toJson(
                    MqttConfig(
                        USER_NAME,
                        PASSWORD,
                        binding.mqtt.domainText.text.toString(),
                        binding.mqtt.portText.text.toString(),
                        binding.mqtt.channelText.text.toString(),
                        binding.mqtt.mqttContentText.text.toString()
                    )
                )
            }
        }
        if (name.isEmpty()) {
            addViewModel.noticeVerify(resources.getString(R.string.empty_name))
        } else if (binding.http.httpUrlText.text.isNullOrEmpty() && type == ItemRemoteAdapter.Type.HTTP) {
            addViewModel.noticeVerify(resources.getString(R.string.empty_http_url))
        } else if ((binding.mqtt.domainText.text.isNullOrEmpty() || binding.mqtt.channelText.text.isNullOrEmpty()) && type == ItemRemoteAdapter.Type.MQTT) {
            addViewModel.noticeVerify(resources.getString(R.string.empty_mqtt_domain_channel))
        } else {
            logger.i("Handle add or update")
            if (callback.updateNotice() == TAG_ADD_FRAGMENT) {
                callback.saveMessage(resources.getString(R.string.mes_add_success))
                var lastId = 1
                val remoteArray = callback.getAllRemote()
                if (remoteArray.isNotEmpty()) {
                    lastId += remoteArray.last().id
                }
                callback.addRemote(Remote(lastId, name, describe,category, type, icon, config))
            } else if (callback.updateNotice() == TAG_UPDATE_FRAGMENT) {
                callback.saveMessage(resources.getString(R.string.mes_update_success))
                val oldRemoteList = callback.getAllRemote()
                oldRemoteList.forEach {
                    if (it.id == remoteUpdate.id) {
                        it.name = name
                        it.describe = describe
                        it.category = category
                        it.type = type
                        it.icon = icon
                        it.config = config
                    }
                }
                callback.updateRemote(oldRemoteList)
            }
            parentFragmentManager.popBackStack()
        }
    }

    @SuppressLint("ResourceType")
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun initView() {
        logger.i("Navigate back")
        binding.backToHome.setOnClickListener {
            logger.i("Back button navigate to home fragment")
            callback.saveMessage("")
            parentFragmentManager.popBackStack()
        }

        logger.i("Handle dropdown menu ")
        val listCategoryRemote =
            resources.getStringArray(addViewModel.listCategoryRemote).toMutableList()
        val categoryAdapter =
            ArrayAdapter(requireContext(), R.layout.dropdown_item, listCategoryRemote)

        val listRemote = resources.getStringArray(addViewModel.listRemote)
        logger.i("Handle option of remote by typeBroadcast")
        binding.optionRemote.doAfterTextChanged {
            when (it.toString()) {
                listRemote[0] -> {
                    binding.local.root.visibility = View.VISIBLE
                    binding.http.root.visibility = View.GONE
                    binding.mqtt.root.visibility = View.GONE
                    typeBroadcast = ItemRemoteAdapter.Type.BLUETOOTH
                }

                listRemote[1] -> {
                    binding.local.root.visibility = View.GONE
                    binding.http.root.visibility = View.VISIBLE
                    binding.mqtt.root.visibility = View.GONE
                    typeBroadcast = ItemRemoteAdapter.Type.HTTP
                }

                listRemote[2] -> {
                    binding.local.root.visibility = View.GONE
                    binding.http.root.visibility = View.GONE
                    binding.mqtt.root.visibility = View.VISIBLE
                    typeBroadcast = ItemRemoteAdapter.Type.MQTT
                }
            }
        }
        binding.categoryRemoteText.setOnClickListener {
            showMenu(it, R.menu.category_menu, requireContext())
        }
        binding.optionRemote.setOnClickListener {
            showMenu(it, R.menu.broadcast_menu, requireContext())
        }
        binding.http.httpMethod.setOnClickListener {
            showMenu(it, R.menu.http_method_menu, requireContext())
        }

        val listPopupWindowButton = binding.categoryRemoteText
        val listPopupWindow = ListPopupWindow(requireContext(), null, androidx.constraintlayout.widget.R.attr.listPopupWindowStyle)

        listPopupWindow.anchorView = listPopupWindowButton

        val items = resources.getStringArray(R.array.remote_category).toMutableList()
        val adapter = ArrayAdapter(requireContext(), R.layout.dropdown_item, items)
        listPopupWindow.setAdapter(adapter)

        listPopupWindow.setOnItemClickListener { _: AdapterView<*>?, _: View?, position: Int, _: Long ->
            listPopupWindow.dismiss()
            if (position == 0){
                showCategoryDialog(this, items, categoryAdapter, binding.root)
            }else{
                listPopupWindowButton.text = items[position]
            }
        }

        listPopupWindowButton.setOnClickListener { listPopupWindow.show() }

        logger.i("default remote option")
        binding.local.root.visibility = View.VISIBLE
        binding.http.root.visibility = View.GONE
        binding.mqtt.root.visibility = View.GONE
        binding.optionRemote.text = listRemote[0]
        binding.http.httpMethod.text = GET_METHOD
        binding.local.deviceIdText.setText(callback.getDeviceName())

        logger.i("Receive data from Home")
        setFragmentResultListener(ID_REQUEST_KEY) { _, bundle ->
            val id = bundle.getInt(ID_ARG)
            logger.i("Set title when update")
            binding.addFragment.text = resources.getString(R.string.update_title)
            logger.i("Find remote by id")
            remoteUpdate = callback.findRemoteById(id)
            logger.i("Set value of remote item to update form")
            binding.remoteNameText.setText(remoteUpdate.name)
            binding.remoteDescriptionText.setText(remoteUpdate.describe)
            binding.categoryRemoteText.text = remoteUpdate.category
            val gson = Gson()
            val type: Type
            val config: Any
            when (remoteUpdate.type) {
                ItemRemoteAdapter.Type.BLUETOOTH -> {
                    binding.optionRemote.text = listRemote[0]
                    type = object : TypeToken<BluetoothConfig>() {}.type
                    config = gson.fromJson(remoteUpdate.config, type) as BluetoothConfig
                    binding.local.localContentText.setText(config.content)
                }

                ItemRemoteAdapter.Type.HTTP -> {
                    binding.optionRemote.text = listRemote[1]
                    type = object : TypeToken<HttpConfig>() {}.type
                    config = gson.fromJson(remoteUpdate.config, type) as HttpConfig
                    binding.http.httpMethod.text = config.method.toString()
                    binding.http.httpUrlText.setText(config.url)
                    binding.http.httpContentText.setText(config.content)
                }

                ItemRemoteAdapter.Type.MQTT -> {
                    binding.optionRemote.text = listRemote[2]
                    type = object : TypeToken<MqttConfig>() {}.type
                    config = gson.fromJson(remoteUpdate.config, type) as MqttConfig
                    binding.mqtt.domainText.setText(config.domain)
                    binding.mqtt.portText.setText(config.port)
                    binding.mqtt.channelText.setText((config.channel))
                    binding.mqtt.mqttContentText.setText(config.content)
                }
            }
        }


        logger.i("Navigate to home after save")
        binding.saveRemote.setOnClickListener {
            saveInputAsRemote(
                binding.remoteNameText.text.toString(),
                binding.remoteDescriptionText.text.toString(),
                binding.categoryRemoteText.text.toString(),
                typeBroadcast, callback
            )
        }
    }

    /* **********************************************************************
     * Class
     ********************************************************************** */

    interface Callback {
        fun addRemote(remote: Remote)
        fun findRemoteById(id: Int): Remote
        fun updateRemote(remotes: MutableList<Remote>)
        fun getAllRemote(): MutableList<Remote>
        fun getActionRemote(): String
        fun saveMessage(message: String)
        fun getDeviceName(): String
    }
}