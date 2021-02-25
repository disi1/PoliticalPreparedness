package com.example.android.politicalpreparedness.representative

import android.content.Context
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.Adapter
import android.widget.AdapterView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import com.example.android.politicalpreparedness.R
import com.example.android.politicalpreparedness.database.ElectionDatabase
import com.example.android.politicalpreparedness.databinding.FragmentRepresentativeBinding
import com.example.android.politicalpreparedness.election.VoterInfoViewModel
import com.example.android.politicalpreparedness.election.VoterInfoViewModelFactory
import com.example.android.politicalpreparedness.network.models.Address
import com.example.android.politicalpreparedness.repository.Repository
import com.example.android.politicalpreparedness.representative.adapter.RepresentativeListAdapter
import java.util.Locale

class DetailFragment : Fragment() {

    private lateinit var binding: FragmentRepresentativeBinding

    companion object {
        //TODO: Add Constant for Location request
    }

    private lateinit var representativeViewModel: RepresentativeViewModel

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        val database = ElectionDatabase.getInstance(requireContext())
        val repository = Repository(database)

        val representativeViewModelFactory = RepresentativeViewModelFactory(repository)
        representativeViewModel = ViewModelProvider(this, representativeViewModelFactory).get(RepresentativeViewModel::class.java)

        binding = FragmentRepresentativeBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = this
        binding.viewModel = representativeViewModel

        val representativeListAdapter = RepresentativeListAdapter()
        binding.representativesRecycler.adapter = representativeListAdapter

        representativeViewModel.representatives.observe(viewLifecycleOwner, { representativesList ->
            representativesList?.let {
                representativeListAdapter.submitList(representativesList)
            }
        })

        binding.state.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                representativeViewModel.address.value?.state = binding.state.selectedItem as String
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                representativeViewModel.address.value?.state = binding.state.selectedItem as String
            }

        }

        representativeViewModel.errorOnFetchingNetworkData.observe(viewLifecycleOwner, {
            if(it) {
                Toast.makeText(
                        activity,
                        R.string.network_error,
                        Toast.LENGTH_LONG
                ).show()
                representativeViewModel.displayNetworkErrorComplete()
            }
        })

        binding.buttonSearch.setOnClickListener {
            findMyRepresentatives()
        }

        return binding.root
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        //TODO: Handle location permission result to get location on permission granted
    }

    private fun findMyRepresentatives() {
        hideKeyboard()

        representativeViewModel.onFindMyRepresentativesClicked()
    }

    private fun checkLocationPermissions(): Boolean {
        return if (isPermissionGranted()) {
            true
        } else {
            //TODO: Request Location permissions
            false
        }
    }

    private fun isPermissionGranted() : Boolean {
        //TODO: Check if permission is already granted and return (true = granted, false = denied/other)

        // just to make it compile
        return false
    }

    private fun getLocation() {
        //TODO: Get location from LocationServices
        //TODO: The geoCodeLocation method is a helper function to change the lat/long location to a human readable street address
    }

    private fun geoCodeLocation(location: Location): Address {
        val geocoder = Geocoder(context, Locale.getDefault())
        return geocoder.getFromLocation(location.latitude, location.longitude, 1)
                .map { address ->
                    Address(address.thoroughfare, address.subThoroughfare, address.locality, address.adminArea, address.postalCode)
                }
                .first()
    }

    private fun hideKeyboard() {
        val imm = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(requireView().windowToken, 0)
    }
}