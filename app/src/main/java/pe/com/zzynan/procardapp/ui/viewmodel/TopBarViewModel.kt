package pe.com.zzynan.procardapp.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pe.com.zzynan.procardapp.core.di.ServiceLocator
import pe.com.zzynan.procardapp.domain.model.UserProfile
import pe.com.zzynan.procardapp.domain.usecase.ObserveUserProfileUseCase
import pe.com.zzynan.procardapp.domain.usecase.SaveUserProfileUseCase
import pe.com.zzynan.procardapp.ui.state.TopBarUiState

/**
 * ViewModel que mantiene el nombre del usuario y lo persiste sin bloquear la UI.
 */
class TopBarViewModel(
    private val observeUserProfileUseCase: ObserveUserProfileUseCase,
    private val saveUserProfileUseCase: SaveUserProfileUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(TopBarUiState())
    val uiState: StateFlow<TopBarUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            observeUserProfileUseCase().collect { profile ->
                _uiState.update { it.copy(displayName = profile.displayName) }
            }
        }
    }

    fun onUserNameChange(newName: String) {
        val sanitized = newName.ifBlank { UserProfile.DEFAULT_DISPLAY_NAME }
        _uiState.update { it.copy(displayName = sanitized) }
        viewModelScope.launch {
            saveUserProfileUseCase(newName)
        }
    }

    companion object {
        fun provideFactory(context: Context): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
                val appContext = context.applicationContext
                val userProfileRepository = ServiceLocator.provideUserProfileRepository(appContext)
                val observeUseCase = ObserveUserProfileUseCase(userProfileRepository)
                val saveUseCase = SaveUserProfileUseCase(userProfileRepository)
                @Suppress("UNCHECKED_CAST")
                return TopBarViewModel(observeUseCase, saveUseCase) as T
            }
        }
    }
}
