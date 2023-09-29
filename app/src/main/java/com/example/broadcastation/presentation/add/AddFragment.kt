package com.example.broadcastation.presentation.add

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import androidx.annotation.RequiresApi
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.setFragmentResultListener
import com.example.broadcastation.R
import com.example.broadcastation.common.base.BaseFragment
import com.example.broadcastation.common.utility.GET_METHOD
import com.example.broadcastation.common.utility.ID_ARG
import com.example.broadcastation.common.utility.ID_REQUEST_KEY
import com.example.broadcastation.common.utility.TAG_ADD_FRAGMENT
import com.example.broadcastation.common.utility.TAG_HOME_FRAGMENT
import com.example.broadcastation.common.utility.TAG_UPDATE_FRAGMENT
import com.example.broadcastation.common.utility.screenNavigate
import com.example.broadcastation.common.utility.showMenu
import com.example.broadcastation.databinding.AddFragmentBinding
import com.example.broadcastation.entity.Remote
import com.example.broadcastation.presentation.MainActivity
import com.example.broadcastation.presentation.home.HomeFragment
import com.example.broadcastation.presentation.home.HomeFragment.Callback
import com.example.broadcastation.presentation.home.ItemRemoteAdapter
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText

class AddFragment :
    BaseFragment<AddFragmentBinding>(AddFragmentBinding::inflate) {
    /* **********************************************************************
     * Variable
     ********************************************************************** */

    private var fragmentManager: FragmentManager? = null
    private var typeBroadcast: ItemRemoteAdapter.Type = ItemRemoteAdapter.Type.BLUETOOTH
    private lateinit var remoteUpdate: Remote

    companion object {
        fun instance(callback: HomeFragment.Callback): AddFragment {
            val addFragment = AddFragment()
            val args = Bundle()
            args.putSerializable("callback", callback)
            addFragment.arguments = args
            return addFragment
        }
    }

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
    }


    /* ***********************************************************************
     * Function
     ********************************************************************** */
    private fun saveInputAsRemote(
        name: String,
        describe: String,
        type: ItemRemoteAdapter.Type,
        callback: HomeFragment.Callback
    ) {
        val icon = when (type) {
            ItemRemoteAdapter.Type.HTTP -> {
                R.drawable.ic_http
            }

            ItemRemoteAdapter.Type.BLUETOOTH -> {
                R.drawable.ic_local
            }

            ItemRemoteAdapter.Type.MQTT -> {
                R.drawable.ic_mqtt
            }
        }
        if (callback.updateNotice() == TAG_ADD_FRAGMENT) {
            callback.saveMessage(resources.getString(R.string.mes_add_success))
            var lastId = 1
            val remoteArray = callback.getAllRemote()
            if (remoteArray.isNotEmpty()) {
                lastId += remoteArray.last().id
            }
            callback.addRemote(Remote(lastId, name, describe, type, icon))
        } else if (callback.updateNotice() == TAG_UPDATE_FRAGMENT) {
            callback.saveMessage(resources.getString(R.string.mes_update_success))
            val oldRemoteList = callback.getAllRemote()
            oldRemoteList.forEach {
                if (it.id == remoteUpdate.id) {
                    it.name = name
                    it.describe = describe
                    it.type = type
                    it.icon = icon
                }
            }
            callback.updateRemote(oldRemoteList)
        }
    }

    @SuppressLint("ResourceType")
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun initView(){
        val callback = arguments?.getSerializable("callback", HomeFragment.Callback::class.java)
        fragmentManager = activity?.supportFragmentManager

        binding.backToHome.setOnClickListener {
            logger.i("Back button navigate to home fragment")
            callback?.saveMessage("")
            (fragmentManager?.findFragmentByTag(
                TAG_HOME_FRAGMENT
            ) ?: callback?.let { callback -> HomeFragment.instance(callback) })?.let { fragment ->
                screenNavigate(
                    fragmentManager,
                    MainActivity.Navigate.DOWN,
                    R.id.mainContainer,
                    fragment
                )
            }
        }

        logger.i("Handle dropdown menu ")
        val listRemote = resources.getStringArray(R.array.remote_menu)
        val listCategoryRemote = resources.getStringArray(R.array.remote_category).toMutableList()
        val categoryAdapter =
            ArrayAdapter(requireContext(), R.layout.dropdown_item, listCategoryRemote)

        binding.categoryNameText.setAdapter(categoryAdapter)
        binding.categoryNameText.setOnItemClickListener { _, _, i, _ ->
            if (i == 0) {
                val builder = AlertDialog.Builder(requireContext())
                val inflater = layoutInflater
                builder.setTitle(resources.getString(R.string.add_category_title))
                val dialogLayout = inflater.inflate(R.layout.layout_add_category, null)
                val newCategory =
                    dialogLayout.findViewById<TextInputEditText>(R.id.add_category_text)
                builder.setView(dialogLayout)
                builder.setPositiveButton("OK") { _, _ ->
                    if (newCategory.text.isNullOrEmpty()) {
                        Snackbar.make(
                            binding.root,
                            resources.getString(R.string.add_category_fail),
                            Snackbar.LENGTH_SHORT
                        ).show()
                    } else {
                        listCategoryRemote.add(newCategory.text.toString())
                        categoryAdapter.notifyDataSetChanged()
                        Snackbar.make(
                            binding.root,
                            "${newCategory.text}: ${resources.getString(R.string.add_category_success)}",
                            Snackbar.LENGTH_SHORT
                        ).show()
                    }
                }
                builder.show()
            }
        }

        logger.i("default remote option")
        binding.local.root.visibility = View.VISIBLE
        binding.http.root.visibility = View.GONE
        binding.mqtt.root.visibility = View.GONE
        binding.optionRemote.text = listRemote[0]

        binding.http.httpMethod.setOnClickListener {
            showMenu(it, R.menu.http_method_menu, requireContext())
        }
        binding.http.httpMethod.text = GET_METHOD

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
        binding.optionRemote.setOnClickListener {
            showMenu(it, R.menu.broadcast_menu, requireContext())
        }

        logger.i("Receive data from Home")
        setFragmentResultListener(ID_REQUEST_KEY) { _, bundle ->
            val id = bundle.getInt(ID_ARG)
            logger.i("Set title when update")
            binding.addFragment.text = resources.getString(R.string.update_title)
            logger.i("Find remote by id")
            remoteUpdate = callback?.findRemoteById(id)!!
            logger.i("Set value of remote item to update form")
            binding.remoteNameText.setText(remoteUpdate.name)
            binding.remoteDescriptionText.setText(remoteUpdate.describe)
            when (remoteUpdate.type) {
                ItemRemoteAdapter.Type.BLUETOOTH -> {
                    binding.optionRemote.text = listRemote[0]
                }

                ItemRemoteAdapter.Type.HTTP -> {
                    binding.optionRemote.text = listRemote[1]
                }

                ItemRemoteAdapter.Type.MQTT -> {
                    binding.optionRemote.text = listRemote[2]
                }
            }
        }

        binding.local.deviceIdText.setText(callback?.getDeviceName())

        logger.i("Navigate to home")
        binding.saveRemote.setOnClickListener {
            if (callback != null) {
                saveInputAsRemote(
                    binding.remoteNameText.text.toString(),
                    binding.remoteDescriptionText.text.toString(),
                    typeBroadcast, callback
                )
            }
            (fragmentManager?.findFragmentByTag(TAG_HOME_FRAGMENT) ?: callback?.let { callback ->
                HomeFragment.instance(callback)
            })?.let { fragment ->
                screenNavigate(
                    fragmentManager,
                    MainActivity.Navigate.DOWN,
                    R.id.mainContainer,
                    fragment
                )
            }

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