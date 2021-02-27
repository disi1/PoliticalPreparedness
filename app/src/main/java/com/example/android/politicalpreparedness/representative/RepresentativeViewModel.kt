package com.example.android.politicalpreparedness.representative

import android.app.Application
import android.util.Log
import androidx.lifecycle.*
import com.example.android.politicalpreparedness.network.models.Address
import com.example.android.politicalpreparedness.repository.Repository
import com.example.android.politicalpreparedness.utils.isNetworkAvailable
import kotlinx.coroutines.launch
import java.io.IOException

class RepresentativeViewModel(private val repository: Repository, application: Application) : AndroidViewModel(application) {

    val representatives = repository.representatives

    private val _address = MutableLiveData<Address>(Address("", "", "", "", ""))
    val address: LiveData<Address>
        get() = _address

    private val _errorOnFetchingNetworkData = MutableLiveData<Boolean>(false)
    val errorOnFetchingNetworkData: LiveData<Boolean>
        get() = _errorOnFetchingNetworkData

    private val _networkNotAvailable = MutableLiveData<Boolean>()
    val networkNotAvailable: LiveData<Boolean>
        get() = _networkNotAvailable

    private val _representativesFetched = MutableLiveData<Boolean>(true)
    val representativesFetched: LiveData<Boolean>
        get() = _representativesFetched

    init {
        _networkNotAvailable.value = !isNetworkAvailable(getApplication())
    }

    private fun getRepresentatives(address: String) {
        if (isNetworkAvailable(getApplication())) {
            viewModelScope.launch {
                try {
                    _representativesFetched.value = false
                    repository.refreshRepresentatives(address)
                    _errorOnFetchingNetworkData.value = false
                    _representativesFetched.value = true
                } catch (networkError: IOException) {
                    if (repository.representatives.value == null) {
                        _errorOnFetchingNetworkData.value = true
                    }
                }
            }
        } else {
            _networkNotAvailable.value = true
            _errorOnFetchingNetworkData.value = true
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
