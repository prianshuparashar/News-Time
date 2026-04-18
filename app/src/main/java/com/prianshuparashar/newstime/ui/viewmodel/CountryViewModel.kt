package com.prianshuparashar.newstime.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.prianshuparashar.newstime.common.constant.CountryData
import com.prianshuparashar.newstime.data.model.Country
import com.prianshuparashar.newstime.ui.base.UIEvent
import com.prianshuparashar.newstime.ui.base.UIState
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

class CountryViewModel : ViewModel() {
    private val _countryState = MutableStateFlow<UIState<List<Country>>>(UIState.Idle)
    val countryState: StateFlow<UIState<List<Country>>> = _countryState

    private val _events = MutableSharedFlow<UIEvent>()
    val events = _events.asSharedFlow()

    fun loadCountries() {
        _countryState.value = UIState.Success(CountryData.countries)
    }

    fun onCountrySelected(country: Country) {
        viewModelScope.launch {
            _events.emit(UIEvent.NavigateToArticles(country = country.code, title = country.name))
        }
    }
}