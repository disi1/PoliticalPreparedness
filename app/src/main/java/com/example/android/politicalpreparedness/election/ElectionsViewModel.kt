package com.example.android.politicalpreparedness.election

import android.app.Application
import androidx.lifecycle.*
import com.example.android.politicalpreparedness.network.models.Election
import com.example.android.politicalpreparedness.repository.Repository
import com.example.android.politicalpreparedness.utils.isNetworkAvailable
import kotlinx.coroutines.launch
import java.io.IOException

class ElectionsViewModel(private val repository: Repository, application: Application) : AndroidViewModel(application) {

    val upcomingElections = repository.upcomingElections
    val savedElections = repository.savedElections

    private val _errorOnFetchingNetworkData = MutableLiveData<Boolean>(false)
    val errorOnFetchingNetworkData: LiveData<Boolean>
        get() = _errorOnFetchingNetworkData

    private val _networkNotAvailable = MutableLiveData<Boolean>()
    val networkNotAvailable: LiveData<Boolean>
        get() = _networkNotAvailable

    private val _navigateToVoterInfo = MutableLiveData<Election>()
    val navigateToVoterInfo: LiveData<Election>
        get() = _navigateToVoterInfo

    fun displayNetworkErrorCompleted() {
        _errorOnFetchingNetworkData.value = false
    }

    fun onElectionClicked(election: Election) {
        _navigateToVoterInfo.value = election
    }

    fun navigationToVoterInfoComplete() {
        _navigateToVoterInfo.value = null
    }

    init {
        if(isNetworkAvailable(application)) {
            _networkNotAvailable.value = false
            refreshUpcomingElections()
        } else {
            _networkNotAvailable.value = true
            _errorOnFetchingNetworkData.value = true
        }
    }

    private fun refreshUpcomingElections() {
        viewModelScope.launch {
            try {
                repository.refreshUpcomingElections()
                _errorOnFetchingNetworkData.value = false
            } catch (networkError: IOException) {
                if (upcomingElections.value.isNullOrEmpty()) {
                    _errorOnFetchingNetworkData.value = true
                }
            }
        }
    }
}