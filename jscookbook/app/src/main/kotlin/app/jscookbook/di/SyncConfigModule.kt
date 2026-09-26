package app.jscookbook.di

import app.jscookbook.BuildConfig
import app.jscookbook.core.sync.SupabaseConfig
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object SyncConfigModule {
    /** Filled at build time from local.properties; see the README for the key names. */
    @Provides
    fun provideSupabaseConfig(): SupabaseConfig = SupabaseConfig(
        url = BuildConfig.SUPABASE_URL,
        anonKey = BuildConfig.SUPABASE_ANON_KEY,
        googleWebClientId = BuildConfig.GOOGLE_WEB_CLIENT_ID,
    )
}
