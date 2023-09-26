package com.example.broadcastation.presentation.add

import android.app.AlertDialog
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.commit
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.broadcastation.R
import com.example.broadcastation.common.base.BaseFragment
import com.example.broadcastation.common.utility.DESC_ARG
import com.example.broadcastation.common.utility.DESC_REQUEST_KEY
import com.example.broadcastation.common.utility.ICON_ARG
import com.example.broadcastation.common.utility.ICON_REQUEST_KEY
import com.example.broadcastation.common.utility.ID_ARG
import com.example.broadcastation.common.utility.ID_REQUEST_KEY
import com.example.broadcastation.common.utility.MES_ADD_SUCCESS
import com.example.broadcastation.common.utility.MES_UPDATE_SUCCESS
import com.example.broadcastation.common.utility.NAME_ARG
import com.example.broadcastation.common.utility.NAME_REQUEST_KEY
import com.example.broadcastation.common.utility.TAG_ADD_FRAGMENT
import com.example.broadcastation.common.utility.TAG_HOME_FRAGMENT
import com.example.broadcastation.common.utility.TAG_UPDATE_FRAGMENT
import com.example.broadcastation.common.utility.screenNavigate
import com.example.broadcastation.databinding.AddFragmentBinding
import com.example.broadcastation.entity.Remote
import com.example.broadcastation.presentation.MainViewModel
import com.example.broadcastation.presentation.add.http.HttpFragment
import com.example.broadcastation.presentation.add.local.LocalFragment
import com.example.broadcastation.presentation.add.mqtt.MqttFragment
import com.example.broadcastation.presentation.home.HomeFragment
import com.example.broadcastation.presentation.home.ItemRemoteAdapter
import com.google.android.material.textfield.TextInputEditText


class AddFragment(private val callback: HomeFragment.Callback) :
    BaseFragment<AddFragmentBinding>(AddFragmentBinding::inflate) {/* **********************************************************************
     * Variable
     ********************************************************************** */

    private var fragmentManager: FragmentManager? = null
    private val addViewModel: AddViewModel by viewModels()
    private val viewModel: MainViewModel by activityViewModels()
    private var typeBroadcast: ItemRemoteAdapter.Type = ItemRemoteAdapter.Type.BLUETOOTH
    private lateinit var remoteUpdate : Remote

    /* **********************************************************************
     * Life Cycle
     ********************************************************************** */

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (!isAdded) {
            return
        }
        fragmentManager = activity?.supportFragmentManager

        binding.backToHome.setOnClickListener {
            logger.i("Back button navigate to home fragment")
            screenNavigate(
                fragmentManager, R.id.mainContainer, fragmentManager?.findFragmentByTag(
                    TAG_HOME_FRAGMENT
                ) ?: HomeFragment(callback)
            )
        }

        logger.i("Handle dropdown menu ")
        val listRemote = resources.getStringArray(R.array.remote_menu)
        val listCategoryRemote = resources.getStringArray(R.array.remote_category).toMutableList()
        val categoryAdapter =
            ArrayAdapter(requireContext(), R.layout.dropdown_item, listCategoryRemote)
        val optionAdapter = ArrayAdapter(requireContext(), R.layout.dropdown_item, listRemote)

        binding.categoryNameText.setAdapter(categoryAdapter)
        binding.categoryNameText.setOnItemClickListener { adapterView, view, i, l ->
            if (i == 0) {
                val builder = AlertDialog.Builder(requireContext())
                val inflater = layoutInflater
                builder.setTitle("Thêm danh mục")
                val dialogLayout = inflater.inflate(R.layout.layout_add_category, null)
                val newCategory =
                    dialogLayout.findViewById<TextInputEditText>(R.id.add_category_text)
                builder.setView(dialogLayout)
                builder.setPositiveButton("OK") { dialogInterface, i ->
                    if (newCategory.text.isNullOrEmpty()) {
                        Toast.makeText(
                            requireContext(), "Danh mục không được để trống", Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        listCategoryRemote.add(newCategory.text.toString())
                        categoryAdapter.notifyDataSetChanged()
                        Toast.makeText(
                            requireContext(),
                            newCategory.text.toString() + " đã được thêm vào danh mục",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
                builder.show()
            }
        }

        val tabs = addViewModel.getTabs() ?: return

        logger.i("Handle option of remote by typeBroadcast")
        binding.optionNameText.doAfterTextChanged {
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
        binding.optionNameText.setAdapter(optionAdapter)

        logger.i("Receive data from Home")
        setFragmentResultListener(ID_REQUEST_KEY) { key, bundle ->
            val id = bundle.getInt(ID_ARG)
            remoteUpdate = viewModel.getAllRemote().toMutableList().find { it.id == id }!!
            binding.remoteNameText.setText(remoteUpdate.name)
            binding.remoteDescriptionText.setText(remoteUpdate.describe)
            when (remoteUpdate.type) {
                ItemRemoteAdapter.Type.BLUETOOTH -> {
                    binding.optionNameText.setText(listRemote[0])
                }

                ItemRemoteAdapter.Type.HTTP -> {
                    binding.optionNameText.setText(listRemote[1])
                }

                ItemRemoteAdapter.Type.MQTT -> {
                    binding.optionNameText.setText(listRemote[2])
                }

                else -> {
                    binding.optionNameText.setText(listRemote[0])
                }
            }
        }

        logger.i("Navigate to home")
        binding.saveRemote.setOnClickListener {
            saveInputAsRemote(
                binding.remoteNameText.text.toString(),
                binding.remoteDescriptionText.text.toString(),
                typeBroadcast
            )
            screenNavigate(
                fragmentManager,
                R.id.mainContainer,
                fragmentManager?.findFragmentByTag(TAG_HOME_FRAGMENT) ?: HomeFragment(callback)
            )

        }
    }

    /* ***********************************************************************
     * Function
     ********************************************************************** */
    private fun saveInputAsRemote(name: String, describe: String, type: ItemRemoteAdapter.Type) {
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
        if(viewModel.getEditRemote() == TAG_ADD_FRAGMENT) {
            viewModel.notice.value = MES_ADD_SUCCESS
            var lastId = 1
            val remoteArray = callback.getAllRemote()
            if (remoteArray.isNotEmpty()) {
                lastId += remoteArray.last().id
            }
            callback.addRemote(Remote(lastId, name, describe, type, icon))
        }
        else if (viewModel.getEditRemote() == TAG_UPDATE_FRAGMENT) {
            viewModel.notice.value = MES_UPDATE_SUCCESS

        }
    }

    /* **********************************************************************
     * Class
     ********************************************************************** */
    private class ViewPagerAdapter(val tabs: List<AddViewModel.Tab>, activity: FragmentActivity) :
        FragmentStateAdapter(activity) {
        override fun getItemCount(): Int {
            return tabs.size
        }

        override fun createFragment(position: Int): Fragment {
            val item = tabs[position]
            var fragment = item.fragment
            if (fragment == null) {
                fragment = when (item.type) {
                    AddViewModel.Type.LOCAL -> LocalFragment()
                    AddViewModel.Type.HTTP -> HttpFragment()
                    AddViewModel.Type.MQTT -> MqttFragment()
                }
                tabs[position].fragment = fragment
            }
            return fragment
        }
    }

    interface Callback {
        fun addRemote(remote: Remote)
    }
}