package com.yurtdolap.app.presentation.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yurtdolap.app.domain.repository.AuthRepository
import com.yurtdolap.app.domain.util.Resource
import com.yurtdolap.app.presentation.designsystem.components.UIState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.yurtdolap.app.domain.repository.LocationRepository

data class AuthFormState(
    val email: String = "",
    val password: String = "",
    val name: String = "",
    val city: String = "",
    val dormitory: String = ""
)

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val locationRepository: LocationRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<UIState<Unit>>(UIState.Idle)
    val uiState: StateFlow<UIState<Unit>> = _uiState.asStateFlow()

    private val _formState = MutableStateFlow(AuthFormState())
    val formState: StateFlow<AuthFormState> = _formState.asStateFlow()

    private val _cities = MutableStateFlow<List<String>>(emptyList())
    val cities: StateFlow<List<String>> = _cities.asStateFlow()

    private val _dormitories = MutableStateFlow<List<String>>(emptyList())
    val dormitories: StateFlow<List<String>> = _dormitories.asStateFlow()
    private var dormitoriesJob: Job? = null

    init {
        fetchCities()
    }

    private fun fetchCities() {
        viewModelScope.launch {
            locationRepository.getCities().collect { resource ->
                if (resource is Resource.Success) {
                    _cities.value = resource.data?.map { it.name } ?: emptyList()
                }
            }
        }
    }

    private fun fetchDormitories(cityName: String) {
        dormitoriesJob?.cancel()
        dormitoriesJob = viewModelScope.launch {
            locationRepository.getDormitoriesByCity(cityName).collect { resource ->
                if (resource is Resource.Success) {
                    _dormitories.value = resource.data?.map { it.name } ?: emptyList()
                }
            }
        }
    }

    fun onEmailChange(email: String) {
        _formState.value = _formState.value.copy(email = email)
    }

    fun onPasswordChange(password: String) {
        _formState.value = _formState.value.copy(password = password)
    }

    fun onNameChange(name: String) {
        _formState.value = _formState.value.copy(name = name)
    }

    fun onCityChange(city: String) {
        _formState.value = _formState.value.copy(city = city, dormitory = "")
        fetchDormitories(city)
    }

    fun onDormitoryChange(dorm: String) {
        _formState.value = _formState.value.copy(dormitory = dorm)
    }

    fun login() {
        val email = _formState.value.email
        val password = _formState.value.password

        if (email.isBlank() || password.isBlank()) {
            _uiState.value = UIState.Error("E-posta ve şifre boş olamaz.")
            return
        }

        _uiState.value = UIState.Loading
        viewModelScope.launch {
            val result = authRepository.signInWithEmailAndPassword(email, password)
            if (result is Resource.Success) {
                _uiState.value = UIState.Success(Unit)
            } else {
                _uiState.value = UIState.Error(result.message ?: "Giriş başarısız oldu.")
            }
        }
    }

    fun register() {
        val state = _formState.value
        
        if (state.email.isBlank() || state.password.isBlank() || state.name.isBlank() || state.city.isBlank() || state.dormitory.isBlank()) {
            _uiState.value = UIState.Error("Tüm alanları doldurunuz.")
            return
        }
        
        val emailLower = state.email.trim().lowercase()
        if (!(emailLower.endsWith(".edu.tr") || emailLower.endsWith(".edu"))) {
            _uiState.value = UIState.Error("Sadece .edu.tr veya .edu uzantılı öğrenci e-posta adresleriyle kayıt olabilirsiniz.")
            return
        }
        
        if (state.password.length < 6) {
             _uiState.value = UIState.Error("Şifre en az 6 karakter olmalıdır.")
             return
        }

        _uiState.value = UIState.Loading
        viewModelScope.launch {
            val result = authRepository.createUserWithEmailAndPassword(
                email = state.email.trim(),
                password = state.password,
                name = state.name,
                city = state.city,
                dormitory = state.dormitory
            )
            if (result is Resource.Success) {
                _uiState.value = UIState.Success(Unit)
            } else {
                _uiState.value = UIState.Error(result.message ?: "Kayıt işlemi başarısız oldu.")
            }
        }
    }

    fun resetState() {
        _uiState.value = UIState.Idle
    }
}
