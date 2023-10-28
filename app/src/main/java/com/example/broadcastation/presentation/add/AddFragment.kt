package com.example.broadcastation.presentation.add

import android.annotation.SuppressLint
import android.app.Activity
import android.os.Build
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.webkit.URLUtil
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
import com.example.broadcastation.common.utility.screenNavigate
import com.example.broadcastation.common.utility.showCategoryDialog
import com.example.broadcastation.common.utility.showMenu
import com.example.broadcastation.databinding.AddFragmentBinding
import com.example.broadcastation.entity.Remote
import com.example.broadcastation.entity.config.BluetoothConfig
import com.example.broadcastation.entity.config.Config
import com.example.broadcastation.entity.config.HttpConfig
import com.example.broadcastation.entity.config.MqttConfig
import com.example.broadcastation.presentation.MainActivity
import com.example.broadcastation.presentation.home.HomeFragment
import com.example.broadcastation.presentation.home.item.ItemRemoteCustomAdapter
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.lang.reflect.Type


@Suppress("DEPRECATION")
@SuppressLint("ClickableViewAccessibility")
class AddFragment(private val callback: HomeFragment.Callback) :
    BaseFragment<AddFragmentBinding>(AddFragmentBinding::inflate) {
    /* **********************************************************************
     * Variable
     ********************************************************************** */
    private var typeBroadcast: ItemRemoteCustomAdapter.Type = ItemRemoteCustomAdapter.Type.BLUETOOTH
    private lateinit var remoteUpdate: Remote
    private val addViewModel: AddViewModel by viewModels()
    val gson = Gson()
    lateinit var type: Type
    private lateinit var config: Config
    private lateinit var categoryAdapter : ArrayAdapter<String>
    private lateinit var listCategoryRemote : MutableList<String>
    private lateinit var listBroadcastType : Array<String>
    private lateinit var categoryPopup : ListPopupWindow

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
        actionView()
    }

    /* ***********************************************************************
     * Function
     ********************************************************************** */
    @SuppressLint("ResourceType", "ClickableViewAccessibility")
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun initView() {
        logger.i("Handle dropdown menu ")
        listCategoryRemote = addViewModel.getCategoryList()
        if(listCategoryRemote.isEmpty()){
            listCategoryRemote = resources.getStringArray(addViewModel.listCategoryRemote).toMutableList()
        }
        categoryAdapter =
            ArrayAdapter(requireContext(), addViewModel.dropdownItem, listCategoryRemote)
        addViewModel.saveCategoryList(listCategoryRemote)

        listBroadcastType = resources.getStringArray(addViewModel.listBroadcastType)

        categoryPopup = ListPopupWindow(
            requireContext(),
            null,
            androidx.constraintlayout.widget.R.attr.listPopupWindowStyle
        )
        categoryPopup.anchorView = binding.categoryRemoteText
        categoryPopup.setAdapter(categoryAdapter)

        logger.i("default remote option")
        binding.local.root.visibility = View.VISIBLE
        binding.http.root.visibility = View.GONE
        binding.mqtt.root.visibility = View.GONE
        binding.optionRemote.text = listBroadcastType[0]
        binding.http.httpMethod.text = GET_METHOD
        binding.local.deviceIdText.setText(callback.getDeviceName())

        addViewModel.notice.observe(viewLifecycleOwner) {
            Snackbar.make(binding.root, it, Snackbar.LENGTH_SHORT).setAnchorView(binding.saveRemote)
                .show()
        }
    }

    private fun actionView(){
        logger.i("Navigate back")
        binding.backToHome.setOnClickListener {
            logger.i("Back button navigate to home fragment")
            callback.saveMessage("")
            callback.saveMessageBroadcast("")
            parentFragmentManager.popBackStack()
        }

        categoryPopup.setOnItemClickListener { _: AdapterView<*>?, _: View?, position: Int, _: Long ->
            categoryPopup.dismiss()
            when(position){
                0-> {
                    showCategoryDialog(this, listCategoryRemote, categoryAdapter, binding.saveRemote, addViewModel)
                }
                1-> {
                    listCategoryRemote.clear()
                    listCategoryRemote.addAll(resources.getStringArray(addViewModel.listCategoryRemote).toMutableList())
                    addViewModel.saveCategoryList(listCategoryRemote)
                    categoryAdapter.notifyDataSetChanged()
                    Snackbar.make(
                        binding.root,
                        resources.getString(R.string.def_return),
                        Snackbar.LENGTH_SHORT
                    )
                        .setAnchorView(binding.saveRemote)
                        .show()
                }
                else -> binding.categoryRemoteText.text = listCategoryRemote[position]
            }
        }

        binding.categoryRemoteText.setOnClickListener { categoryPopup.show() }

        binding.optionRemote.setOnClickListener {
            showMenu(it, addViewModel.menuBroadcast, requireContext())
        }
        logger.i("Handle option of remote by typeBroadcast")
        binding.optionRemote.doAfterTextChanged {
            when (it.toString()) {
                listBroadcastType[0] -> {
                    binding.local.root.visibility = View.VISIBLE
                    binding.http.root.visibility = View.GONE
                    binding.mqtt.root.visibility = View.GONE
                    typeBroadcast = ItemRemoteCustomAdapter.Type.BLUETOOTH
                }

                listBroadcastType[1] -> {
                    binding.local.root.visibility = View.GONE
                    binding.http.root.visibility = View.VISIBLE
                    binding.mqtt.root.visibility = View.GONE
                    typeBroadcast = ItemRemoteCustomAdapter.Type.HTTP
                }

                listBroadcastType[2] -> {
                    binding.local.root.visibility = View.GONE
                    binding.http.root.visibility = View.GONE
                    binding.mqtt.root.visibility = View.VISIBLE
                    typeBroadcast = ItemRemoteCustomAdapter.Type.MQTT
                }
            }
        }

        binding.http.httpMethod.setOnClickListener {
            showMenu(it, addViewModel.menuHttpMethod, requireContext())
        }

        logger.i("Receive data from Home")
        setFragmentResultListener(ID_REQUEST_KEY) { _, bundle ->
            val id = bundle.getInt(ID_ARG)
            logger.i("Set title when update")
            binding.title.text = resources.getString(addViewModel.updateTitle)
            logger.i("Find remote by id")
            remoteUpdate = callback.findRemoteById(id)
            logger.i("Set value of remote item to update form")
            binding.remoteNameText.setText(remoteUpdate.name)
            binding.remoteDescriptionText.setText(remoteUpdate.describe)
            binding.categoryRemoteText.text = remoteUpdate.category

            try {
                when (remoteUpdate.type) {
                    ItemRemoteCustomAdapter.Type.BLUETOOTH -> {
                        binding.optionRemote.text = listBroadcastType[0]
                        type = object : TypeToken<BluetoothConfig>() {}.type
                        config = gson.fromJson(remoteUpdate.config, type)
                        binding.local.localContentText.setText((config as BluetoothConfig).content)
                    }

                    ItemRemoteCustomAdapter.Type.HTTP -> {
                        binding.optionRemote.text = listBroadcastType[1]
                        type = object : TypeToken<HttpConfig>() {}.type
                        config = gson.fromJson(remoteUpdate.config, type)
                        binding.http.httpMethod.text = (config as HttpConfig).method.toString()
                        binding.http.httpUrlText.setText((config as HttpConfig).url)
                        binding.http.httpContentText.setText((config as HttpConfig).content)
                    }

                    ItemRemoteCustomAdapter.Type.MQTT -> {
                        binding.optionRemote.text = listBroadcastType[2]
                        type = object : TypeToken<MqttConfig>() {}.type
                        config = gson.fromJson(remoteUpdate.config, type)
                        binding.mqtt.domainText.setText((config as MqttConfig).domain)
                        binding.mqtt.portText.setText((config as MqttConfig).port)
                        binding.mqtt.channelText.setText(((config as MqttConfig).channel))
                        binding.mqtt.mqttContentText.setText((config as MqttConfig).content)
                    }
                }
            } catch (e: Exception) {
                logger.w(e.message ?: "Broadcasting")
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

        binding.root.setOnTouchListener { view, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    hideKeyboardAndFocus(view)
                    view.clearFocus()
                }
            }
            false
        }
    }

    private fun saveInputAsRemote(
        name: String,
        describe: String,
        category: String,
        type: ItemRemoteCustomAdapter.Type,
        callback: HomeFragment.Callback
    ) {
        val icon: Int
        val gson = Gson()
        val config: String
        logger.i("Handle icon, type and config")
        when (type) {
            ItemRemoteCustomAdapter.Type.HTTP -> {
                icon = R.drawable.ic_http
                config = gson.toJson(
                    HttpConfig(
                        binding.http.httpUrlText.text.toString(),
                        when (binding.http.httpMethod.text.toString().uppercase()) {
                            POST_METHOD -> ItemRemoteCustomAdapter.HttpMethod.POST
                            else -> ItemRemoteCustomAdapter.HttpMethod.GET
                        },
                        binding.http.httpContentText.text.toString()
                    )
                )
            }

            ItemRemoteCustomAdapter.Type.BLUETOOTH -> {
                icon = R.drawable.ic_local
                config = gson.toJson(
                    BluetoothConfig(
                        binding.local.deviceIdText.text.toString(),
                        binding.local.localContentText.text.toString()
                    )
                )
            }

            ItemRemoteCustomAdapter.Type.MQTT -> {
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
        } else if ((binding.http.httpUrlText.text.isNullOrEmpty() || !URLUtil.isValidUrl(binding.http.httpUrlText.text.toString())) && type == ItemRemoteCustomAdapter.Type.HTTP ) {
            addViewModel.noticeVerify(resources.getString(R.string.empty_http_url))
        } else if ((binding.mqtt.domainText.text.isNullOrEmpty() || binding.mqtt.channelText.text.isNullOrEmpty()) && type == ItemRemoteCustomAdapter.Type.MQTT) {
            addViewModel.noticeVerify(resources.getString(R.string.empty_mqtt_domain_channel))
        } else {
            logger.i("Handle add or update")
            if (callback.updateNotice() == TAG_ADD_FRAGMENT) {
                callback.saveMessage(resources.getString(R.string.mes_add_success))
                var lastId = 1
                var highestId = 0
                val remoteArray = callback.getAllRemote()
                if (remoteArray.isNotEmpty()) {
                    for(r in remoteArray){
                        if(r.id > highestId){
                            highestId = r.id
                        }
                    }
                    lastId += highestId
                }
                callback.addRemote(Remote(lastId, name, describe, category, type, icon, config))
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
            screenNavigate(
                fragmentManager,
                MainActivity.Navigate.DOWN,
                R.id.mainContainer,
                HomeFragment(callback)
            )
        }
    }

    private fun hideKeyboardAndFocus(view: View) {
        val inputMethodManger =
            activity?.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager?
        inputMethodManger?.hideSoftInputFromWindow(view.windowToken, 0)
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