package com.example.broadcastation.presentation.add

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doAfterTextChanged
import androidx.core.widget.doOnTextChanged
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
import com.example.broadcastation.common.utility.MES_UPDATE_SUCCESS
import com.example.broadcastation.common.utility.NAME_ARG
import com.example.broadcastation.common.utility.NAME_REQUEST_KEY
import com.example.broadcastation.common.utility.TAG_HOME_FRAGMENT
import com.example.broadcastation.databinding.AddFragmentBinding
import com.example.broadcastation.entity.Remote
import com.example.broadcastation.presentation.MainViewModel
import com.example.broadcastation.presentation.add.http.HttpFragment
import com.example.broadcastation.presentation.add.local.LocalFragment
import com.example.broadcastation.presentation.add.mqtt.MqttFragment
import com.example.broadcastation.presentation.home.HomeFragment
import com.google.android.material.snackbar.Snackbar
import kotlin.math.log


class AddFragment :
    BaseFragment<AddFragmentBinding>(AddFragmentBinding::inflate) {
    /* **********************************************************************
     * Variable
     ********************************************************************** */

    private var fragmentManager: FragmentManager? = null
    private val addViewModel: AddViewModel by viewModels()
    private val viewModel: MainViewModel by activityViewModels()
    private lateinit var categoryAdapter: ArrayAdapter<String>
    private lateinit var optionAdapter: ArrayAdapter<String>
    private lateinit var pagerAdapter: ViewPagerAdapter
    private var method: Int = 1

    companion object {
        fun newInstance(): AddFragment {
            return AddFragment()
        }
    }

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
            fragmentManager?.commit {
                setCustomAnimations(
                    R.anim.fade_in,
                    R.anim.slide_out,
                    R.anim.slide_in,
                    R.anim.fade_out
                )
                replace(
                    R.id.mainContainer,
                    fragmentManager?.findFragmentByTag("home") ?: HomeFragment.newInstance(),
                    null
                )
                addToBackStack(null)
                setReorderingAllowed(true)
            }
        }

        logger.i("Handle dropdown menu ")
        val tabs = addViewModel.getTabs() ?: return
        val listRemote = resources.getStringArray(R.array.remote_menu)
        val listCategoryRemote = resources.getStringArray(R.array.remote_category)
        categoryAdapter = ArrayAdapter(
            requireContext(),
            R.layout.dropdown_item,
            listCategoryRemote
        )
        optionAdapter = ArrayAdapter(
            requireContext(),
            R.layout.dropdown_item,
            listRemote
        )
        pagerAdapter = ViewPagerAdapter(tabs, requireActivity())
        binding.categoryNameText.setAdapter(categoryAdapter)
        binding.optionNameText.setAdapter(optionAdapter)
        binding.optionNameText.setText(listRemote[0])
        binding.local.root.visibility = View.VISIBLE
        binding.http.root.visibility = View.GONE
        binding.mqtt.root.visibility = View.GONE

        logger.i("Receive data from Home")
        setFragmentResultListener(NAME_REQUEST_KEY) { key, bundle ->
            val name = bundle.getString(NAME_ARG)
            binding.remoteNameText.setText(name)
        }
        setFragmentResultListener(DESC_REQUEST_KEY) { key, bundle ->
            val des = bundle.getString(DESC_ARG)
            binding.remoteDescriptionText.setText(des)
        }
        setFragmentResultListener(ICON_REQUEST_KEY) { key, bundle ->
            when (bundle.getInt(ICON_ARG)) {
                R.drawable.ic_local -> {
                    binding.optionNameText.setText(listRemote[0])
                }

                R.drawable.ic_http -> {
                    binding.optionNameText.setText(listRemote[1])
                }

                else -> {
                    binding.optionNameText.setText(listRemote[2])
                }
            }
        }

        logger.i("Navigate to home")
        binding.saveRemote.setOnClickListener {
            viewModel.notice.value = MES_UPDATE_SUCCESS
            addRemote(
                binding.remoteNameText.text.toString(),
                binding.remoteDescriptionText.text.toString(),
                method
            )
            fragmentManager?.commit {
                setCustomAnimations(
                    R.anim.fade_in,
                    R.anim.slide_out,
                    R.anim.slide_in,
                    R.anim.fade_out
                )
                replace(
                    R.id.mainContainer,
                    fragmentManager?.findFragmentByTag(TAG_HOME_FRAGMENT)
                        ?: HomeFragment.newInstance(),
                    null
                )
                addToBackStack(null)
                setReorderingAllowed(true)
            }
        }

        logger.i("Handle option of remote by method")
        binding.optionNameText.doAfterTextChanged {
            when (it.toString()) {
                listRemote[0] -> {
                    binding.local.root.visibility = View.VISIBLE
                    binding.http.root.visibility = View.GONE
                    binding.mqtt.root.visibility = View.GONE
                    method = 1
                }

                listRemote[1] -> {
                    binding.local.root.visibility = View.GONE
                    binding.http.root.visibility = View.VISIBLE
                    binding.mqtt.root.visibility = View.GONE
                    method = 2
                }

                listRemote[2] -> {
                    binding.local.root.visibility = View.GONE
                    binding.http.root.visibility = View.GONE
                    binding.mqtt.root.visibility = View.VISIBLE
                    method = 3
                }
            }
        }
        binding.optionNameText.setOnItemClickListener { adapterView, view, i, l ->
            when (i) {
                0 -> {
                    binding.local.root.visibility = View.VISIBLE
                    binding.http.root.visibility = View.GONE
                    binding.mqtt.root.visibility = View.GONE
                    method = 1
                }

                1 -> {
                    binding.local.root.visibility = View.GONE
                    binding.http.root.visibility = View.VISIBLE
                    binding.mqtt.root.visibility = View.GONE
                    method = 2
                }

                else -> {
                    binding.local.root.visibility = View.GONE
                    binding.http.root.visibility = View.GONE
                    binding.mqtt.root.visibility = View.VISIBLE
                    method = 3
                }
            }
        }

    }

    /* **********************************************************************
     * Function
     ********************************************************************** */
    private fun addRemote(name: String, describe: String, type: Int) {
        val icon = when (type) {
            1 -> {
                R.drawable.ic_local
            }

            2 -> {
                R.drawable.ic_http
            }

            else -> {
                R.drawable.ic_mqtt
            }
        }
        viewModel.addRemote(Remote(name, describe, type, icon))
    }

    private fun deleteRemote(name: String, describe: String, type: Int) {
        val icon = when (type) {
            1 -> {
                R.drawable.ic_local
            }

            2 -> {
                R.drawable.ic_http
            }

            else -> {
                R.drawable.ic_mqtt
            }
        }
        viewModel.deleteRemote(Remote(name, describe, type, icon))
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
}