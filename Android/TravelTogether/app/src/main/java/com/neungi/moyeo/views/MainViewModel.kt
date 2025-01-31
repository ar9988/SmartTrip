package com.neungi.moyeo.views

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(

) : ViewModel() {

    private val _bnvState = MutableStateFlow<Boolean>(true)
    val bnvState = _bnvState.asStateFlow()

    fun setBnvState(isVisible: Boolean) {
        _bnvState.value = isVisible
    }
}