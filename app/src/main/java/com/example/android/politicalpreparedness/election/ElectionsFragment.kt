package com.example.android.politicalpreparedness.election

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.android.politicalpreparedness.R
import com.example.android.politicalpreparedness.database.ElectionDatabase
import com.example.android.politicalpreparedness.databinding.FragmentElectionBinding
import com.example.android.politicalpreparedness.election.adapter.ElectionListAdapter
import com.example.android.politicalpreparedness.election.adapter.ElectionListener
import com.example.android.politicalpreparedness.repository.Repository

class ElectionsFragment : Fragment() {

    private lateinit var electionsViewModel: ElectionsViewModel
    private lateinit var upcomingElectionsListAdapter: ElectionListAdapter
    private lateinit var savedElectionsListAdapter: ElectionListAdapter

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View {

        val application = requireNotNull(this.activity).application
        val database = ElectionDatabase.getInstance(requireContext())
        val repository = Repository(database)
        val viewModelFactory = ElectionsViewModelFactory(repository, application)
        electionsViewModel = ViewModelProvider(this, viewModelFactory).get(ElectionsViewModel::class.java)

        val binding = FragmentElectionBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = this

        binding.electionViewModel = electionsViewModel

        upcomingElectionsListAdapter = ElectionListAdapter(ElectionListener { election ->
            electionsViewModel.onElectionClicked(election)
        })
        savedElectionsListAdapter = ElectionListAdapter(ElectionListener { election ->
            electionsViewModel.onElectionClicked(election)
        })

        binding.upcomingElectionsRecycler.adapter = upcomingElectionsListAdapter
        binding.savedElectionsRecycler.adapter = savedElectionsListAdapter

        electionsViewModel.navigateToVoterInfo.observe(viewLifecycleOwner, { election ->
            if (election != null) {
                this.findNavController().navigate(ElectionsFragmentDirections.actionElectionsFragmentToVoterInfoFragment(election, election.division))
                electionsViewModel.navigationToVoterInfoComplete()
            }
        })

        electionsViewModel.errorOnFetchingNetworkData.observe(viewLifecycleOwner, {
            if (it) {
                Toast.makeText(
                        activity,
                        R.string.network_error,
                        Toast.LENGTH_LONG
                ).show()
                binding.loadingUpcomingElectionsImage.visibility = View.GONE
                electionsViewModel.displayNetworkErrorCompleted()
            }
        })

        electionsViewModel.upcomingElections.observe(viewLifecycleOwner, { upcomingElections ->
            upcomingElections?.apply {
                upcomingElectionsListAdapter.submitList(upcomingElections)
                if (upcomingElections.isNotEmpty()) {
                    binding.loadingUpcomingElectionsImage.visibility = View.GONE
                    binding.connectionErrorImage.visibility = View.GONE
                } else {
                    binding.loadingUpcomingElectionsImage.visibility = View.VISIBLE
                }
            }
        })

        electionsViewModel.savedElections.observe(viewLifecycleOwner, { savedElections ->
            savedElections?.apply {
                savedElectionsListAdapter.submitList(savedElections)
                if(savedElections.isNotEmpty()) {
                    binding.noDataHint.visibility = View.GONE
                } else {
                    binding.noDataHint.visibility = View.VISIBLE
                }
            }
        })

        return binding.root
    }
}