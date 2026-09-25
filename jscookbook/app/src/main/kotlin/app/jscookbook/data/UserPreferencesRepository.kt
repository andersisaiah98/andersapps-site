package app.jscookbook.data

import kotlinx.coroutines.flow.Flow

/** Small on-device settings. Recipes and the rest of the cook book live in Room from Phase 1. */
interface UserPreferencesRepository {
    val themeMode: Flow<ThemeMode>

    suspend fun setThemeMode(mode: ThemeMode)
}
