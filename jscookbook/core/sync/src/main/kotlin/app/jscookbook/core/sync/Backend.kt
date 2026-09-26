package app.jscookbook.core.sync

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.FlowType
import io.github.jan.supabase.compose.auth.ComposeAuth
import io.github.jan.supabase.compose.auth.googleNativeLogin
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.realtime.Realtime
import io.github.jan.supabase.storage.Storage
import javax.inject.Inject
import javax.inject.Singleton

/**
 * The one Supabase client, or null when the app isn't configured for sync. It only ever holds the
 * public anon key; everything the signed-in user can reach is decided by Row Level Security.
 */
@Singleton
class Backend @Inject constructor(val config: SupabaseConfig) {

    val client: SupabaseClient? = if (config.isConfigured) {
        createSupabaseClient(supabaseUrl = config.url, supabaseKey = config.anonKey) {
            install(Auth) {
                // Magic links open app.jscookbook://login, which MainActivity hands back here.
                scheme = DeepLinkScheme
                host = DeepLinkHost
                flowType = FlowType.PKCE
            }
            install(Postgrest)
            install(Storage)
            install(Realtime)
            install(ComposeAuth) {
                if (config.hasGoogleSignIn) googleNativeLogin(serverClientId = config.googleWebClientId)
            }
        }
    } else {
        null
    }

    companion object {
        const val DeepLinkScheme = "app.jscookbook"
        const val DeepLinkHost = "login"
        const val PhotoBucket = "photos"
    }
}
