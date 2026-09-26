package app.jscookbook.core.sync

/**
 * Where the backend is. The app fills this from BuildConfig, which reads `local.properties`
 * (SUPABASE_URL, SUPABASE_ANON_KEY, GOOGLE_WEB_CLIENT_ID). When they're blank the app runs
 * local-only and Settings says sync isn't set up.
 */
data class SupabaseConfig(
    val url: String,
    val anonKey: String,
    val googleWebClientId: String,
) {
    val isConfigured: Boolean get() = url.isNotBlank() && anonKey.isNotBlank()
    val hasGoogleSignIn: Boolean get() = isConfigured && googleWebClientId.isNotBlank()
}
