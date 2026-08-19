package com.example.matchmate.ui.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.switchMap
import androidx.lifecycle.viewModelScope
import com.example.matchmate.data.local.MatchStatus
import com.example.matchmate.data.local.UserEntity
import com.example.matchmate.data.repository.UserRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainViewModel(private val repository: UserRepository) : ViewModel() {

    val allUsers: LiveData<List<UserEntity>> = repository.users

    private val _filter = MutableLiveData(MatchFilter.ALL)
    val filter: LiveData<MatchFilter> = _filter

    val filteredUsers: LiveData<List<UserEntity>> = _filter.switchMap { filter ->
        val status = filter.toMatchStatus()
        if (status == null) repository.users else repository.getUsersByStatus(status)
    }

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _hasError = MutableLiveData(false)
    val hasError: LiveData<Boolean> = _hasError

    private val _showError = MutableLiveData(false)
    val showError: LiveData<Boolean> = _showError

    private val _showEmptyCache = MutableLiveData(false)
    val showEmptyCache: LiveData<Boolean> = _showEmptyCache

    private val _showEmptyFilter = MutableLiveData(false)
    val showEmptyFilter: LiveData<Boolean> = _showEmptyFilter

    private val _showContent = MutableLiveData(false)
    val showContent: LiveData<Boolean> = _showContent

    private val _showLoading = MediatorLiveData<Boolean>().apply {
        addSource(isLoading) { recomputeScreenState() }
        addSource(hasError) { recomputeScreenState() }
        addSource(allUsers) { recomputeScreenState() }
        addSource(filteredUsers) { recomputeScreenState() }
    }
    val showLoading: LiveData<Boolean> = _showLoading

    private fun recomputeScreenState() {
        val loading = isLoading.value == true
        val error = hasError.value == true
        val cacheEmpty = allUsers.value.isNullOrEmpty()
        val filteredEmpty = filteredUsers.value.isNullOrEmpty()

        _showLoading.value = loading && cacheEmpty
        _showError.value = !loading && error && cacheEmpty
        _showEmptyCache.value = !loading && !error && cacheEmpty
        _showEmptyFilter.value = !loading && !error && !cacheEmpty && filteredEmpty
        _showContent.value = !filteredEmpty
    }

    init {
        fetchUsers()
    }

    fun onFilterSelected(filter: MatchFilter) {
        _filter.value = filter
    }

    private fun fetchUsers() {
        viewModelScope.launch(Dispatchers.IO) {
            _isLoading.postValue(true)
            val result = repository.fetchUsersIfNeeded()
            _hasError.postValue(result.isFailure)
            _isLoading.postValue(false)
        }
    }

    fun refresh() {
        viewModelScope.launch(Dispatchers.IO) {
            _isLoading.postValue(true)
            val result = repository.retryFetch()
            _hasError.postValue(result.isFailure && allUsers.value.isNullOrEmpty())
            _isLoading.postValue(false)
        }
    }

    fun onAcceptClicked(email: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateMatchStatus(email, MatchStatus.ACCEPTED)
        }
    }

    fun onDeclineClicked(email: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateMatchStatus(email, MatchStatus.DECLINED)
        }
    }

    fun onNetworkRestored() {
        if (allUsers.value.isNullOrEmpty()) refresh()
    }

    private val _isLoadingNextPage = MutableLiveData(false)
    val isLoadingNextPage: LiveData<Boolean> = _isLoadingNextPage


    fun loadNextPage() {
        if (_isLoadingNextPage.value == true) return
        viewModelScope.launch(Dispatchers.IO) {
            _isLoadingNextPage.postValue(true)
            repository.loadNextPage()
            _isLoadingNextPage.postValue(false)
        }
    }
}
