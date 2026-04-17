package com.prianshuparashar.newstime.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.prianshuparashar.newstime.common.constant.LanguageData
import com.prianshuparashar.newstime.data.model.Language
import com.prianshuparashar.newstime.ui.base.UIEvent
import com.prianshuparashar.newstime.ui.base.UIState
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class LanguageViewModel : ViewModel() {
    private val _languagesState = MutableStateFlow<UIState<List<Language>>>(UIState.Idle)
    val languagesState: StateFlow<UIState<List<Language>>> = _languagesState.asStateFlow()

    private val _events = MutableSharedFlow<UIEvent>()
    val events = _events.asSharedFlow()

    fun loadLanguages() {
        _languagesState.value = UIState.Success(LanguageData.languages)
    }

    fun onLanguageSelected(language: Language) {
        viewModelScope.launch {
            _events.emit(UIEvent.NavigateToArticles(language = language.code, title = language.name))
        }
    }
}