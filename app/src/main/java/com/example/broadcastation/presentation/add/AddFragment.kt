package com.example.broadcastation.presentation.add

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import androidx.fragment.app.viewModels
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.broadcastation.R
import com.example.broadcastation.common.base.BaseFragment
import com.example.broadcastation.databinding.AddFragmentBinding
import com.example.broadcastation.presentation.add.http.HttpFragment
import com.example.broadcastation.presentation.add.local.LocalFragment
import com.example.broadcastation.presentation.add.mqtt.MqttFragment
import com.example.broadcastation.presentation.home.HomeFragment
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator


@Suppress("DEPRECATION")
class AddFragment :
    BaseFragment<AddFragmentBinding>(AddFragmentBinding::inflate) {
    /* **********************************************************************
     * Variable
     ********************************************************************** */

    private var fragmentManager: FragmentManager? = null
    private var transaction: FragmentTransaction? = null
    private val viewModel: AddViewModel by viewModels()
    var bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()


    /* **********************************************************************
     * Life Cycle
     ********************************************************************** */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (!isAdded) {
            return
        }
        fragmentManager = activity?.supportFragmentManager
        transaction = fragmentManager?.beginTransaction()?.setCustomAnimations(
            R.anim.fade_in,  // enter
            R.anim.slide_out,  // exit
            R.anim.slide_in,   // popEnter
            R.anim.fade_out  // popExit
        )
        binding.backToHome.setOnClickListener {
            logger.i("Back button navigate to home fragment")
            transaction?.replace(R.id.mainContainer, HomeFragment(), null)?.commit()
        }

        binding.addedRemote.setOnClickListener {
            if (ActivityCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.BLUETOOTH_CONNECT
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                bluetoothAdapter.enable()
                Snackbar.make(requireContext(), binding.root, "hello", 1).show()
            }
        }

        val tabs = viewModel.getTabs() ?: return
        val adapter = ViewPagerAdapter(tabs, requireActivity())
        binding.viewpager.adapter = adapter
        TabLayoutMediator(binding.tabBroadcast, binding.viewpager) { tab, position ->
//            tab.text = resources.getString(tabs[position].title)

            // Set the icon property
            tab.icon = resources.getDrawable(tabs[position].icon)
        }.attach()
        binding.viewpager.isUserInputEnabled = false
        binding.tabBroadcast.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                val position: Int? = tab?.position
                if (position != null) {
                    binding.viewpager.currentItem = position
                    binding.tabBroadcast.selectTab(binding.tabBroadcast.getTabAt(position))
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }

    override fun onStart() {
        super.onStart()
    }

    override fun onResume() {
        super.onResume()
    }

    override fun onPause() {
        super.onPause()
    }

    override fun onStop() {
        super.onStop()
    }


    override fun onDestroyView() {
        super.onDestroyView()
    }

    override fun onDestroy() {
        super.onDestroy()
    }
    /* **********************************************************************
     * Function
     ********************************************************************** */

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