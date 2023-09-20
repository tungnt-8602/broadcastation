package com.example.broadcastation.presentation.add

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.SpinnerAdapter
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.broadcastation.R
import com.example.broadcastation.common.base.BaseFragment
import com.example.broadcastation.common.logger.Logger
import com.example.broadcastation.databinding.AddFragmentBinding
import com.example.broadcastation.entity.Remote
import com.example.broadcastation.presentation.add.http.HttpFragment
import com.example.broadcastation.presentation.add.local.LocalFragment
import com.example.broadcastation.presentation.add.mqtt.MqttFragment
import com.example.broadcastation.presentation.home.HomeFragment
import com.example.broadcastation.presentation.home.HomeViewModel
import com.google.android.material.floatingactionbutton.FloatingActionButton


@Suppress("DEPRECATION")
class AddFragment :
    BaseFragment<AddFragmentBinding>(AddFragmentBinding::inflate) {
    /* **********************************************************************
     * Variable
     ********************************************************************** */

    private var fragmentManager: FragmentManager? = null
    private var transaction: FragmentTransaction? = null
    private val viewModel: AddViewModel by viewModels()
    private lateinit var stringAdapter: ArrayAdapter<String>
    private lateinit var pagerAdapter: ViewPagerAdapter

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
        transaction = fragmentManager?.beginTransaction()?.setCustomAnimations(
            R.anim.fade_in,  // enter
            R.anim.slide_out,  // exit
            R.anim.slide_in,   // popEnter
            R.anim.fade_out  // popExit
        )
        binding.backToHome.setOnClickListener {
            logger.i("Back button navigate to home fragment")
            transaction?.replace(R.id.mainContainer, HomeFragment.newInstance(), null)?.addToBackStack(null)?.commit()
        }

        val tabs = viewModel.getTabs() ?: return
        val listRemote = tabs.map { resources.getString(it.title) }
        stringAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_dropdown_item,
            listRemote
        )

        pagerAdapter = ViewPagerAdapter(tabs, requireActivity())
        binding.remoteOption.adapter = stringAdapter
        binding.viewpager.adapter = pagerAdapter
        binding.viewpager.isUserInputEnabled = false
        binding.remoteOption.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                binding.viewpager.currentItem = position
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                binding.viewpager.currentItem = 0
            }
        }
        binding.saveRemote.setOnClickListener {
            shareViewModel.addRemote(Remote("Data", "", 1, R.drawable.ic_local_fill))
            logger.i("Add remote to live data : ${shareViewModel.remoteLiveList.value}")
            logger.i("Add remote to list: ${shareViewModel.remoteList}")
            transaction?.replace(R.id.mainContainer, HomeFragment.newInstance(), null)?.addToBackStack(null)?.commit()

        }
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