package com.example.broadcastation.presentation.home

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.commit
import androidx.fragment.app.setFragmentResult
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.broadcastation.R
import com.example.broadcastation.common.base.BaseFragment
import com.example.broadcastation.common.utility.DESC_ARG
import com.example.broadcastation.common.utility.DESC_REQUEST_KEY
import com.example.broadcastation.common.utility.EDIT_ARG
import com.example.broadcastation.common.utility.EDIT_REQUEST_KEY
import com.example.broadcastation.common.utility.EDIT_TITLE
import com.example.broadcastation.common.utility.ICON_ARG
import com.example.broadcastation.common.utility.ICON_REQUEST_KEY
import com.example.broadcastation.common.utility.NAME_ARG
import com.example.broadcastation.common.utility.NAME_REQUEST_KEY
import com.example.broadcastation.common.utility.TAG_ADD_FRAGMENT
import com.example.broadcastation.control.PermissionControl
import com.example.broadcastation.databinding.HomeFragmentBinding
import com.example.broadcastation.presentation.MainActivity
import com.example.broadcastation.presentation.MainViewModel
import com.example.broadcastation.presentation.add.AddFragment
import com.google.android.material.snackbar.Snackbar


class HomeFragment : BaseFragment<HomeFragmentBinding>(HomeFragmentBinding::inflate) {
    /* **********************************************************************
     * Variable
     ********************************************************************** */
    private var fragmentManager: FragmentManager? = null
    private val viewModel: MainViewModel by activityViewModels()

    companion object {
        fun newInstance(): HomeFragment {
            return HomeFragment()
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

        logger.i("Recycler view")
        binding.remoteList.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        val adapter = ItemRemoteAdapter(viewModel, binding.idLoadingPB, (activity as MainActivity))
        viewModel.remoteLiveList.observe(viewLifecycleOwner) { remotes ->
            logger.i("list: $remotes")
            adapter.setData(remotes)
            binding.remoteList.adapter = adapter
        }

        logger.i("Item navigate: Update remote")
        adapter.setOnItemTouchListener {
            setFragmentResult(EDIT_REQUEST_KEY, bundleOf(EDIT_ARG to EDIT_TITLE))
            setFragmentResult(NAME_REQUEST_KEY, bundleOf(NAME_ARG to it.name))
            setFragmentResult(DESC_REQUEST_KEY, bundleOf(DESC_ARG to it.describe))
            setFragmentResult(ICON_REQUEST_KEY, bundleOf(ICON_ARG to it.icon))
            fragmentManager?.commit {
                setCustomAnimations(
                    R.anim.slide_in,
                    R.anim.fade_out,
                    R.anim.fade_in,
                    R.anim.slide_out
                )
                replace(R.id.mainContainer, AddFragment(), null)
                addToBackStack(null)
                setReorderingAllowed(true)
            }
        }

        binding.add.setOnClickListener {
            logger.i("Add button navigate to add fragment")
            fragmentManager?.saveFragmentInstanceState(this)
            fragmentManager?.commit {
                setCustomAnimations(
                    R.anim.slide_in,
                    R.anim.fade_out,
                    R.anim.fade_in,
                    R.anim.slide_out
                )
                replace(R.id.mainContainer, AddFragment(), TAG_ADD_FRAGMENT)
                addToBackStack(null)
                setReorderingAllowed(true)
            }
        }

        logger.i("Message after action")
        viewModel.notice.observe(viewLifecycleOwner) { message ->
            logger.d(message)
            Snackbar.make(binding.root, message, Snackbar.LENGTH_SHORT).setAnchorView(binding.add)
                .show()
        }
    }

    /* **********************************************************************
     * Function
     ********************************************************************** */

    /* **********************************************************************
     * Class
     ********************************************************************** */
}