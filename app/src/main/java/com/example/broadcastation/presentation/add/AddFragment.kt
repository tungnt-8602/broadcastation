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
import com.google.android.material.snackbar.Snackbar


@Suppress("DEPRECATION")
class AddFragment :
    BaseFragment<AddFragmentBinding>(AddFragmentBinding::inflate) {
    /* **********************************************************************
     * Variable
     ********************************************************************** */

    private var fragmentManager: FragmentManager? = null
    private var transaction: FragmentTransaction? = null
    private val viewModel: AddViewModel by viewModels()
    private lateinit var categoryAdapter: ArrayAdapter<String>
    private lateinit var optionAdapter: ArrayAdapter<String>
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

        binding.saveRemote.setOnClickListener {
            shareViewModel.addRemote(Remote("Data", "", 1, R.drawable.ic_local_fill))
            logger.i("Add remote to live data : ${shareViewModel.remoteLiveList.value}")
            logger.i("Add remote to list: ${shareViewModel.remoteList}")
            transaction?.replace(R.id.mainContainer, HomeFragment.newInstance(), null)?.addToBackStack(null)?.commit()
        }

        binding.optionNameText.setOnItemClickListener { adapterView, view, i, l ->
            when (i) {
                0 -> {
                    binding.local.root.visibility = View.VISIBLE
                    binding.http.root.visibility = View.GONE
                    binding.mqtt.root.visibility = View.GONE
                }
                1 -> {
                    binding.local.root.visibility = View.GONE
                    binding.http.root.visibility = View.VISIBLE
                    binding.mqtt.root.visibility = View.GONE
                }
                else -> {
                    binding.local.root.visibility = View.GONE
                    binding.http.root.visibility = View.GONE
                    binding.mqtt.root.visibility = View.VISIBLE
                }
            }
        }
        binding.optionNameText.inputType = android.text.InputType.TYPE_CLASS_TEXT
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