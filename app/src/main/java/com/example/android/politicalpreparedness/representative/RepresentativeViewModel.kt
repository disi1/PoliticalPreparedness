package com.example.android.politicalpreparedness.representative

import androidx.lifecycle.*
import com.example.android.politicalpreparedness.network.models.Address
import com.example.android.politicalpreparedness.repository.Repository
import kotlinx.coroutines.launch
import java.io.IOException

class RepresentativeViewModel(private val repository: Repository): ViewModel() {

    val representatives = repository.representatives

    private val _address = MutableLiveData<Address>(Address("","","","",""))
    val address: LiveData<Address>
        get() = _address

    private val _errorOnFetchingNetworkData = MutableLiveData<Boolean>(false)
    val errorOnFetchingNetworkData: LiveData<Boolean>
        get() = _errorOnFetchingNetworkData

    private fun getRepresentatives(address: String) {
        viewModelScope.launch {
            try {
                repository.refreshRepresentatives(address)
                _errorOnFetchingNetworkData.value = false
            } catch (networkError: IOException) {
                if(repository.representatives.value == null) {
                    _errorOnFetchingNetworkData.value = true
                }
            }
        }
    }

    fun displayNetworkErrorComplete() {
        _errorOnFetchingNetworkData.value = false
    }

    fun onFindMyRepresentativesClicked() {
        _address.value?.let { getRepresentatives(it.toFormattedString()) }
    }

    fun onUseMyLocationClicked(address: Address) {
        _address.value = address
        getRepresentatives(address.toFormattedString())
    }
}
