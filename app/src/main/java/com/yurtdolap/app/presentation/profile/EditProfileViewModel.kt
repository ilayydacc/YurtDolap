package com.yurtdolap.app.presentation.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yurtdolap.app.domain.repository.LocationRepository
import com.yurtdolap.app.domain.repository.UserRepository
import com.yurtdolap.app.domain.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.Job
import javax.inject.Inject

data class EditProfileState(
    val name: String = "",
    val city: String = "",
    val cities: List<String> = emptyList(),
    val dormitory: String = "",
    val dormitories: List<String> = emptyList(),
    val isLoading: Boolean = true,
    val isDormitoriesLoading: Boolean = false,
    val isSaving: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class EditProfileViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val locationRepository: LocationRepository
) : ViewModel() {

    private val _state = MutableStateFlow(EditProfileState())
    val state: StateFlow<EditProfileState> = _state.asStateFlow()

    private val _saveSuccess = MutableSharedFlow<Unit>()
    val saveSuccess: SharedFlow<Unit> = _saveSuccess.asSharedFlow()
    private var dormitoriesJob: Job? = null

    init {
        loadProfile()
        loadCities()
    }

    private fun loadCities() {
        viewModelScope.launch {
            locationRepository.getCities().collect { resource ->
                when (resource) {
                    is Resource.Success -> {
                        _state.value = _state.value.copy(
                            cities = resource.data?.map { it.name }.orEmpty()
                        )
                    }

                    is Resource.Error -> {
                        _state.value = _state.value.copy(
                            errorMessage = resource.message ?: "Sehirler yuklenemedi"
                        )
                    }

                    is Resource.Loading -> Unit
                }
            }
        }
    }

    fun loadProfile() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, errorMessage = null)
            when (val result = userRepository.getUserProfile()) {
                is Resource.Success -> {
                    val profile = result.data
                    _state.value = _state.value.copy(
                        name = profile?.name.orEmpty(),
                        city = profile?.city.orEmpty(),
                        dormitory = profile?.dormitory.orEmpty(),
                        isLoading = false,
                        errorMessage = null
                    )
                    profile?.city?.takeIf { it.isNotBlank() }?.let { loadDormitories(it) }
                }
                is Resource.Error -> {
                    _state.value = _state.value.copy(
                        isLoading = false,
                        errorMessage = result.message ?: "Profil bilgileri yuklenemedi"
                    )
                }
                is Resource.Loading -> {
                    _state.value = _state.value.copy(isLoading = true)
                }
            }
        }
    }

    fun onNameChange(value: String) {
        _state.value = _state.value.copy(name = value, errorMessage = null)
    }

    fun onDormitoryChange(value: String) {
        _state.value = _state.value.copy(dormitory = value, errorMessage = null)
    }

    fun onCityChange(value: String) {
        _state.value = _state.value.copy(
            city = value,
            dormitory = "",
            dormitories = emptyList(),
            errorMessage = null
        )
        if (value.isNotBlank()) {
            loadDormitories(value)
        }
    }

    private fun loadDormitories(cityName: String) {
        dormitoriesJob?.cancel()
        dormitoriesJob = viewModelScope.launch {
            _state.value = _state.value.copy(isDormitoriesLoading = true)
            locationRepository.getDormitoriesByCity(cityName).collect { resource ->
                when (resource) {
                    is Resource.Success -> {
                        _state.value = _state.value.copy(
                            dormitories = resource.data?.map { it.name }.orEmpty(),
                            isDormitoriesLoading = false
                        )
                    }

                    is Resource.Error -> {
                        _state.value = _state.value.copy(
                            isDormitoriesLoading = false,
                            errorMessage = resource.message ?: "Yurtlar yuklenemedi"
                        )
                    }

                    is Resource.Loading -> {
                        _state.value = _state.value.copy(isDormitoriesLoading = true)
                    }
                }
            }
        }
    }

    fun saveProfile() {
        val current = _state.value
        val trimmedName = current.name.trim()
        val trimmedCity = current.city.trim()
        val trimmedDormitory = current.dormitory.trim()

        if (trimmedName.isBlank() || trimmedCity.isBlank() || trimmedDormitory.isBlank()) {
            _state.value = current.copy(errorMessage = "Isim, sehir ve yurt bilgisi zorunlu")
            return
        }

        if (current.dormitories.isNotEmpty() && !current.dormitories.contains(trimmedDormitory)) {
            _state.value = current.copy(errorMessage = "Lutfen sehirdeki yurt listesinden bir yurt sec")
            return
        }

        viewModelScope.launch {
            _state.value = current.copy(isSaving = true, errorMessage = null)
            when (val result = userRepository.updateUserProfile(trimmedName, trimmedCity, trimmedDormitory)) {
                is Resource.Success -> {
                    _state.value = _state.value.copy(isSaving = false, errorMessage = null)
                    _saveSuccess.emit(Unit)
                }
                is Resource.Error -> {
                    _state.value = _state.value.copy(
                        isSaving = false,
                        errorMessage = result.message ?: "Profil guncellenemedi"
                    )
                }
                is Resource.Loading -> {
                    _state.value = _state.value.copy(isSaving = true)
                }
            }
        }
    }
}
