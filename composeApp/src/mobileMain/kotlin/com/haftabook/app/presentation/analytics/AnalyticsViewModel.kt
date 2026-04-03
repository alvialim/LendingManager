package com.haftabook.app.presentation.analytics

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.haftabook.app.domain.usecase.AnalyticsBucket
import com.haftabook.app.domain.usecase.AnalyticsGranularity
import com.haftabook.app.domain.usecase.GetAnalyticsUseCase
import kotlinx.coroutines.launch

class AnalyticsViewModel(
    private val getAnalyticsUseCase: GetAnalyticsUseCase,
) : ViewModel() {

    var granularity by mutableStateOf(AnalyticsGranularity.Yearly)
        private set

    var buckets by mutableStateOf<List<AnalyticsBucket>>(emptyList())
        private set

    var isLoading by mutableStateOf(false)
        private set

    init {
        load()
    }

    fun onGranularityChange(value: AnalyticsGranularity) {
        if (value == granularity) return
        granularity = value
        load()
    }

    private fun load() {
        viewModelScope.launch {
            isLoading = true
            try {
                buckets = getAnalyticsUseCase.execute(granularity)
            } finally {
                isLoading = false
            }
        }
    }
}
