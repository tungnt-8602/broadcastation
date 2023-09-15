package com.example.broadcastation.presentation.add.local

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.example.broadcastation.R
import com.example.broadcastation.common.base.BaseFragment
import com.example.broadcastation.databinding.FragmentLocalBinding
import com.example.broadcastation.presentation.add.AddViewModel

class LocalFragment : BaseFragment<FragmentLocalBinding>(FragmentLocalBinding::inflate) {

    private val viewModel: AddViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        logger.i("Paste uuid in edittext")
        viewModel.uuid.observe(viewLifecycleOwner){
            binding.deviceIdText.setText(it)
        }
        logger.i("Get data uuid")
        viewModel.bind()
    }
}