package com.example.safetynettest.ui.result

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.navArgs
import com.example.safetynettest.databinding.FragmentResultBinding
import com.example.safetynettest.model.SafetynetResultModel


class ResultFragment : Fragment() {

    private lateinit var binding: FragmentResultBinding
    private val args: ResultFragmentArgs by navArgs() // Declared to get args passed between navgraph
    private lateinit var data: SafetynetResultModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentResultBinding.inflate(inflater)
        data = args.data
        displayData(data)

        return binding.root
    }


    // Function to display data into screen
    private fun displayData(data: SafetynetResultModel) {
        binding.profileMatchTxt.text = data.profileMatch
        binding.evaluationTxt.text = data.evaluationType
        binding.basicIntegrityTxt.text = data.basicIntegrity
    }
}