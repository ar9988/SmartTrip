package com.neungi.moyeo.views

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.neungi.domain.model.ApiStatus
import com.neungi.domain.model.Festival
import com.neungi.domain.model.LoginInfo
import com.neungi.domain.model.Place
import com.neungi.domain.usecase.GetSearchPlaceUseCase
import com.neungi.domain.usecase.GetUserInfoUseCase
import com.neungi.moyeo.util.EmptyState
import com.neungi.moyeo.views.aiplanning.viewmodel.AiPlanningUiEvent
import com.neungi.moyeo.views.aiplanning.viewmodel.SearchUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val getSearchPlaceUseCase: GetSearchPlaceUseCase,
    private val getUserInfoUseCase: GetUserInfoUseCase
) : ViewModel() {

    init {
        viewModelScope.launch {
            getUserInfoUseCase.getLoginInfo().collect { loginInfo ->
                _userLoginInfo.value = loginInfo
                Timber.d("Login Info loaded: $loginInfo")
            }
        }
    }

    private val _bnvState = MutableStateFlow<Boolean>(true)
    val bnvState = _bnvState.asStateFlow()

    fun setBnvState(isVisible: Boolean) {
        _bnvState.value = isVisible
    }

    //관광지 검색 결과
    private val _placeSearchResult = MutableStateFlow<List<Place>>(emptyList())
    val placeSearchResult = _placeSearchResult.asStateFlow()

    private val _searchUiState = MutableStateFlow<SearchUiState>(SearchUiState())
    val searchUiState = _searchUiState.asStateFlow()

    private val _userLoginInfo = MutableStateFlow<LoginInfo?>(null)
    val userLoginInfo = _userLoginInfo.asStateFlow()





    /*
    관광지 검색시
    검색창 텍스트 변경시
     */
    fun onSearchTextChanged(keyword:String?){
        Timber.d(keyword.toString())
        if(keyword.isNullOrBlank()){
            _searchUiState.update { it.copy( searchTextState = EmptyState.EMPTY) }
        }else{
            viewModelScope.launch {
                val result = getSearchPlaceUseCase.getSearchPlace(keyword)
                Timber.d("${result}")
                when (result.status) {
                    ApiStatus.SUCCESS -> {
                        result.data?.let { festivals ->
                            _placeSearchResult.value = festivals.toList()
                        }
                    }
                    ApiStatus.ERROR -> {
                        Timber.e(result.message)
                        _placeSearchResult.value = emptyList()
                    }
                    ApiStatus.FAIL -> {
                        _placeSearchResult.value = emptyList()
                    }
                    ApiStatus.LOADING -> {
                        // 로딩 상태 처리가 필요한 경우
                    }
                }
                _searchUiState.update { it.copy( searchTextState = EmptyState.NONE) }
            }
        }
    }

    fun clearSearchResult(){
        _searchUiState.update { it.copy( searchTextState = EmptyState.EMPTY) }
        _placeSearchResult.value = emptyList()
    }


}