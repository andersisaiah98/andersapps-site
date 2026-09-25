package app.jscookbook

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.jscookbook.data.ThemeMode
import app.jscookbook.data.UserPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

data class MainUiState(val loading: Boolean, val themeMode: ThemeMode)

@HiltViewModel
class MainViewModel @Inject constructor(preferences: UserPreferencesRepository) : ViewModel() {
    val uiState: StateFlow<MainUiState> = preferences.themeMode
        .map { MainUiState(loading = false, themeMode = it) }
        .stateIn(
            scope = viewModelScope,
            // Eager so the splash screen can wait for the saved theme before the first frame.
            started = SharingStarted.Eagerly,
            initialValue = MainUiState(loading = true, themeMode = ThemeMode.System),
        )
}
