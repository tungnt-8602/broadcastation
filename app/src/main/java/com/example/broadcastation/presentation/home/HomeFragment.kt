package com.example.broadcastation.presentation.home

import android.annotation.SuppressLint
import android.bluetooth.BluetoothManager
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.annotation.RequiresApi
import androidx.appcompat.widget.PopupMenu
import androidx.core.os.bundleOf
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.broadcastation.R
import com.example.broadcastation.common.base.BaseFragment
import com.example.broadcastation.common.utility.DRAG_DIRS
import com.example.broadcastation.common.utility.ID_ARG
import com.example.broadcastation.common.utility.ID_REQUEST_KEY
import com.example.broadcastation.common.utility.TAG_ADD_FRAGMENT
import com.example.broadcastation.common.utility.TAG_UPDATE_FRAGMENT
import com.example.broadcastation.common.utility.screenNavigate
import com.example.broadcastation.databinding.HomeFragmentBinding
import com.example.broadcastation.entity.Remote
import com.example.broadcastation.entity.config.BluetoothConfig
import com.example.broadcastation.entity.config.Config
import com.example.broadcastation.entity.config.MqttConfig
import com.example.broadcastation.presentation.MainActivity
import com.example.broadcastation.presentation.MainViewModel
import com.example.broadcastation.presentation.add.AddFragment
import com.example.broadcastation.presentation.home.item.ItemMoveCustomCallback
import com.example.broadcastation.presentation.home.item.ItemRemoteCategoryAdapter
import com.example.broadcastation.presentation.home.item.ItemRemoteCustomAdapter
import com.example.broadcastation.presentation.home.item.ItemRemoteGridAdapter
import com.example.broadcastation.presentation.home.item.ItemRemoteNormalAdapter
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.lang.reflect.Type


class HomeFragment(val callback: Callback) :
    BaseFragment<HomeFragmentBinding>(HomeFragmentBinding::inflate) {
    /* **********************************************************************
     * Variable
     ********************************************************************** */
    private var fragmentManager: FragmentManager? = null
    private val viewModel: MainViewModel by activityViewModels()
    private val homeViewModel: HomeViewModel by viewModels()
    lateinit var type: Type
    val gson = Gson()
    private lateinit var config: Config

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
        fragmentManager = parentFragmentManager

        logger.i("Empty/list display")
        val remoteList = callback.getAllRemote()
        if (remoteList.isEmpty()) {
            binding.empty.visibility = View.VISIBLE
            binding.remoteList.visibility = View.GONE
        } else {
            binding.empty.visibility = View.GONE
            binding.remoteList.visibility = View.VISIBLE
        }

        logger.i("Common item callback for all adapter")
        val itemCallBack = object : ItemRemoteCustomAdapter.Callback {
            override fun shareBluetooth(remote: Remote, callback: Callback) {
                val isTurnedOn =
                    activity?.getSystemService(BluetoothManager::class.java)?.adapter?.isEnabled
                try {
                    if (isTurnedOn == true) {
                        viewModel.shareBluetooth(remote, callback)
                        type = object : TypeToken<BluetoothConfig>() {}.type
                        config = gson.fromJson(remote.config, type)
                        callback.saveMessageBroadcast((config as BluetoothConfig).content)
                        homeViewModel.noticeBroadcast(callback.getMessageBroadcast())
                    } else {
                        callback.grantBluetoothPermission(remote, callback, binding.add)
                    }
                } catch (e: Exception) {
                    logger.w(e.message ?: "Share bluetooth")
                }
            }

            override fun postHttp(remote: Remote) {
                callback.postHttp(remote)
                homeViewModel.noticeBroadcast(callback.getMessageBroadcast())
            }

            override fun getHttp(remote: Remote) {
                callback.getHttp(remote)
                homeViewModel.noticeBroadcast(callback.getMessageBroadcast())
            }

            override fun publishMqtt(remote: Remote) {
                try {
                    type = object : TypeToken<MqttConfig>() {}.type
                    config = gson.fromJson(remote.config, type)
                    callback.publishMqtt(remote, callback, requireContext())
                    callback.saveMessageBroadcast((config as MqttConfig).content)
                    homeViewModel.noticeBroadcast(callback.getMessageBroadcast())
                } catch (e: Exception) {
                    logger.w(e.message ?: "Publish Mqtt")
                }
            }
        }

        val adapter = ItemRemoteNormalAdapter(callback = itemCallBack, callback)
        val gridAdapter = ItemRemoteGridAdapter(callback = itemCallBack, callback)
        val categoryAdapter = ItemRemoteCategoryAdapter(callback = itemCallBack, callback)
        val customAdapter = ItemRemoteCustomAdapter(callback = itemCallBack, callback)
        val itemCustomCallback: ItemTouchHelper.Callback = ItemMoveCustomCallback(customAdapter)
        val touchCustomHelper = ItemTouchHelper(itemCustomCallback)
        val touchHelper = ItemTouchHelper(object :
            ItemTouchHelper.SimpleCallback(DRAG_DIRS, ItemTouchHelper.END) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                val deletedRemote: Remote = remoteList[position]
                remoteList.removeAt(position)
                this@HomeFragment.callback.updateRemote(remoteList)
                adapter.setData(remoteList)

                // below line is to display our snackbar with action.
                Snackbar.make(
                    binding.root,
                    "${resources.getString(homeViewModel.deleteRemote)} ${deletedRemote.name}",
                    Snackbar.LENGTH_LONG
                )
                    .setAnchorView(binding.add)
                    .setAction(homeViewModel.undo) {
                        remoteList.add(position, deletedRemote)
                        this@HomeFragment.callback.updateRemote(remoteList)
                        adapter.setData(remoteList)
                    }.show()
            }
        })

        val data = mutableMapOf<MutableList<Any>, MutableList<Remote>>()
        for(remote in remoteList){
            val key : MutableList<Any> = mutableListOf(remote.category)
            if (!data.containsKey(key)) {
                data[key] = mutableListOf()
                data[key]?.add(remote)
            }else{
                data[key]?.add(remote)
            }
        }

        when(homeViewModel.getSortType()){
            HomeViewModel.SortType.Normal-> {
                adapter.setData(remoteList)
                binding.remoteList.adapter = adapter
                touchCustomHelper.attachToRecyclerView(null)
                homeViewModel.saveSortType(HomeViewModel.SortType.Normal)
                homeViewModel.noticeBroadcast(resources.getString(homeViewModel.noticeNormal))
            }
            HomeViewModel.SortType.Custom -> {
                customAdapter.setData(remoteList)
                binding.remoteList.adapter = customAdapter
                touchCustomHelper.attachToRecyclerView(binding.remoteList)
                homeViewModel.saveSortType(HomeViewModel.SortType.Custom)
                homeViewModel.noticeBroadcast(resources.getString(homeViewModel.noticeCustom))
            }
            HomeViewModel.SortType.Grid -> {
                gridAdapter.setData(remoteList)
                binding.remoteList.adapter = gridAdapter
                binding.remoteList.layoutManager = GridLayoutManager(requireContext(), 3)
                touchCustomHelper.attachToRecyclerView(null)
                homeViewModel.saveSortType(HomeViewModel.SortType.Grid)
                homeViewModel.noticeBroadcast(resources.getString(homeViewModel.noticeGrid))
            }
            HomeViewModel.SortType.Category -> {
                categoryAdapter.setData(data)
                logger.i("ttt $data")
                binding.remoteList.adapter = categoryAdapter
                touchCustomHelper.attachToRecyclerView(null)
                binding.remoteList.layoutManager = LinearLayoutManager(requireContext())
                homeViewModel.saveSortType(HomeViewModel.SortType.Category)
                homeViewModel.noticeBroadcast(resources.getString(homeViewModel.noticeCategory))
            }
        }

        binding.sortRemote.setOnClickListener {
            val popup = PopupMenu(requireContext(), it)
            popup.apply {
                menuInflater.inflate(homeViewModel.menuFilter, popup.menu)
                setOnMenuItemClickListener { menuItem: MenuItem ->
                    when (menuItem.title) {
                        resources.getString(R.string.normal_filter)-> {
                            adapter.setData(remoteList)
                            binding.remoteList.adapter = adapter
                            touchCustomHelper.attachToRecyclerView(null)
                            binding.remoteList.layoutManager = LinearLayoutManager(requireContext())
                            homeViewModel.saveSortType(HomeViewModel.SortType.Normal)
                            homeViewModel.noticeBroadcast(resources.getString(homeViewModel.noticeNormal))
                        }
                        resources.getString(R.string.custom_filter) -> {
                            customAdapter.setData(remoteList)
                            binding.remoteList.adapter = customAdapter
                            binding.remoteList.layoutManager = LinearLayoutManager(requireContext())
                            touchCustomHelper.attachToRecyclerView(binding.remoteList)
                            homeViewModel.saveSortType(HomeViewModel.SortType.Custom)
                            homeViewModel.noticeBroadcast(resources.getString(homeViewModel.noticeCustom))
                        }
                        resources.getString(R.string.grid_filter) -> {
                            gridAdapter.setData(remoteList)
                            binding.remoteList.adapter = gridAdapter
                            binding.remoteList.layoutManager = GridLayoutManager(requireContext(), 3)
                            touchCustomHelper.attachToRecyclerView(null)
                            homeViewModel.saveSortType(HomeViewModel.SortType.Grid)
                            homeViewModel.noticeBroadcast(resources.getString(homeViewModel.noticeNormal))
                        }
                        resources.getString(R.string.category_filter) -> {
                            categoryAdapter.setData(data)
                            binding.remoteList.adapter = categoryAdapter
                            touchCustomHelper.attachToRecyclerView(null)
                            binding.remoteList.layoutManager = LinearLayoutManager(requireContext())
                            homeViewModel.saveSortType(HomeViewModel.SortType.Category)
                            homeViewModel.noticeBroadcast(resources.getString(homeViewModel.noticeCategory))
                        }
                    }
                    true
                }
                show()
            }
        }

        touchHelper.attachToRecyclerView(binding.remoteList)

        adapter.setOnItemTouchListener {navigateUpdate(callback, it)}

        gridAdapter.setOnItemTouchListener {navigateUpdate(callback, it)}

        customAdapter.setOnItemTouchListener {navigateUpdate(callback, it)}

        categoryAdapter.setOnItemTouchListener {navigateUpdate(callback, it)}

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

    /* **********************************************************************
     * Function
     ********************************************************************** */
    private fun navigateUpdate(callback: Callback, remote: Remote){
        callback.saveMessageAction(TAG_UPDATE_FRAGMENT)
        setFragmentResult(ID_REQUEST_KEY, bundleOf(ID_ARG to remote.id))
        screenNavigate(
            fragmentManager,
            MainActivity.Navigate.UP,
            R.id.mainContainer,
            AddFragment(callback)
        )
    }

    /* **********************************************************************
     * Class
     ********************************************************************** */
    interface Callback : AddFragment.Callback {
        fun grantBluetoothPermission(remote: Remote, callback: Callback, view: View)
        fun shareBluetooth(remote: Remote, callback: Callback)
        fun postHttp(remote: Remote)
        fun getHttp(remote: Remote)
        fun publishMqtt(remote: Remote, callback: Callback, context: Context)

        fun updateNotice(): String
        fun saveMessageAction(message: String)
        fun getMessageAction(): String
        fun getMessageBroadcast(): String
        fun saveMessageBroadcast(message: String)
        fun startAdvertise(advertise: String, message: String)
        fun stopAdvertise()
    }
}