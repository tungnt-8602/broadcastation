package com.example.broadcastation.presentation.home

import android.annotation.SuppressLint
import android.bluetooth.BluetoothManager
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.annotation.RequiresApi
import androidx.core.os.bundleOf
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.broadcastation.R
import com.example.broadcastation.common.base.BaseFragment
import com.example.broadcastation.common.utility.ID_ARG
import com.example.broadcastation.common.utility.ID_REQUEST_KEY
import com.example.broadcastation.common.utility.TAG_ADD_FRAGMENT
import com.example.broadcastation.common.utility.TAG_UPDATE_FRAGMENT
import com.example.broadcastation.common.utility.screenNavigate
import com.example.broadcastation.databinding.HomeFragmentBinding
import com.example.broadcastation.entity.MqttConfig
import com.example.broadcastation.entity.Remote
import com.example.broadcastation.presentation.MainActivity
import com.example.broadcastation.presentation.MainViewModel
import com.example.broadcastation.presentation.add.AddFragment
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken


class HomeFragment(val callback: Callback) :
    BaseFragment<HomeFragmentBinding>(HomeFragmentBinding::inflate) {
    /* **********************************************************************
     * Variable
     ********************************************************************** */
    private var fragmentManager: FragmentManager? = null
    private val viewModel: MainViewModel by activityViewModels()
    private val homeViewModel: HomeViewModel by viewModels()

    /* **********************************************************************
     * Life Cycle
     ********************************************************************** */

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    @SuppressLint("SuspiciousIndentation")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (!isAdded) {
            return
        }
        fragmentManager = activity?.supportFragmentManager

        logger.i("Empty/list display")
        val remoteList = callback.getAllRemote()
        if (remoteList.isEmpty()) {
            binding.empty.visibility = View.VISIBLE
            binding.remoteList.visibility = View.GONE
        } else {
            binding.empty.visibility = View.GONE
            binding.remoteList.visibility = View.VISIBLE
        }

        logger.i("Recycler view")
        binding.remoteList.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        val adapter = callback.let {
            ItemRemoteAdapter(callback = object : ItemRemoteAdapter.Callback {
                override fun shareBluetooth(remote: Remote, callback: Callback) {
                    var isTurnedOn =
                        activity?.getSystemService(BluetoothManager::class.java)?.adapter?.isEnabled
                    if (isTurnedOn == true) {
                        viewModel.shareBluetooth(remote, callback)

                    } else {
                        callback.grantBluetoothPermission(remote, callback)
                    }
                }

                override fun postHttp(remote: Remote) {
                    callback.postHttp(remote)
                    homeViewModel.noticeBroadcast(resources.getString(R.string.post_http_notice))
                }

                override fun getHttp(remote: Remote) {
                    callback.getHttp(remote)
                    homeViewModel.noticeBroadcast(resources.getString(R.string.get_http_notice))
                }

                override fun publishMqtt(remote: Remote) {
                    val type = object : TypeToken<MqttConfig>() {}.type
                    val gson = Gson()
                    val config = gson.fromJson(remote.config, type) as MqttConfig
                    callback.publishMqtt(remote, callback)
                    callback.saveMessageBroadcast(config.content)
                    callback.getMessageBroadcast().let {
                        Snackbar.make(binding.root, it, Snackbar.LENGTH_SHORT)
                            .setAnchorView(binding.add).show()
                    }
                }
            }, callback) }
            adapter.setData(remoteList)
            binding.remoteList.adapter = adapter

            logger.i("Item navigate: Update remote")
            adapter.setOnItemTouchListener {
                callback.saveMessageAction(TAG_UPDATE_FRAGMENT)
                setFragmentResult(ID_REQUEST_KEY, bundleOf(ID_ARG to it.id))
                screenNavigate(
                    fragmentManager,
                    MainActivity.Navigate.UP,
                    R.id.mainContainer,
                    AddFragment(callback)
                )
            }

            binding.add.setOnClickListener {
                logger.i("Add button navigate to add fragment")
                callback.saveMessageAction(TAG_ADD_FRAGMENT)
                screenNavigate(
                    fragmentManager,
                    MainActivity.Navigate.UP,
                    R.id.mainContainer,
                    AddFragment(callback),
                    TAG_ADD_FRAGMENT
                )
            }

            logger.i("Message after type")
            val message = callback.updateNotice()
            if (message != "") {
                Snackbar.make(binding.root, message, Snackbar.LENGTH_SHORT)
                    .setAnchorView(binding.add)
                    .show()
            }
            homeViewModel.notice.observe(viewLifecycleOwner) {
                Snackbar.make(binding.root, it, Snackbar.LENGTH_SHORT)
                    .setAnchorView(binding.add).show()
            }
        }

        override fun onDestroyView() {
            super.onDestroyView()
            logger.e("onDestroyView")
        }

        override fun onDestroy() {
            super.onDestroy()
            logger.e("onDestroy")
        }

        /* **********************************************************************
     * Function
     ********************************************************************** */

        /* **********************************************************************
     * Class
     ********************************************************************** */
        interface Callback : AddFragment.Callback {
            fun grantBluetoothPermission(remote: Remote, callback: Callback)
            fun shareBluetooth(remote: Remote, callback: Callback)
            fun postHttp(remote: Remote)
            fun getHttp(remote: Remote)
            fun publishMqtt(remote: Remote, callback: Callback)

            fun updateNotice(): String
            fun saveMessageAction(message: String)
            fun getMessageAction(): String
            fun getMessageBroadcast(): String
            fun saveMessageBroadcast(message: String)
            fun startAdvertise(advertise: String, message: String)
            fun stopAdvertise()
    }
    }
