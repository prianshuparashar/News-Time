package com.prianshuparashar.newstime.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.prianshuparashar.newstime.common.constant.Const
import com.prianshuparashar.newstime.data.model.ApiSource
import com.prianshuparashar.newstime.data.repository.NewsRepository
import com.prianshuparashar.newstime.ui.base.UIEvent
import com.prianshuparashar.newstime.ui.base.UIState
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

class SourceViewModel(
    private val newsRepository: NewsRepository
) : ViewModel() {

    private val _sourceState = MutableStateFlow<UIState<List<ApiSource>>>(UIState.Idle)
    val sourceState: StateFlow<UIState<List<ApiSource>>> = _sourceState.asStateFlow()

    private val _events = MutableSharedFlow<UIEvent>()
    val events = _events.asSharedFlow()

    fun fetchSources() {
        _sourceState.value = UIState.Loading

        viewModelScope.launch {
            newsRepository.getSources()
                .catch { exception ->
                    _sourceState.value = UIState.Error(exception.message ?: Const.ERROR_UNKNOWN)
                }
                .collect { sources ->
                    _sourceState.value = UIState.Success(sources.sources)
                }
        }
    }

    fun onSourceSelected(source: ApiSource) {
        val id = source.id ?: return
        viewModelScope.launch {
            _events.emit(UIEvent.NavigateToArticles(id, source.name))
        }
    }
}