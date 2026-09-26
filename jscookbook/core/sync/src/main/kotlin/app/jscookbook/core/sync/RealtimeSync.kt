package app.jscookbook.core.sync

import io.github.jan.supabase.realtime.PostgresAction
import io.github.jan.supabase.realtime.channel
import io.github.jan.supabase.realtime.postgresChangeFlow
import io.github.jan.supabase.realtime.realtime
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Listens for changes to the shared cook book while the app is running and calls [onChange], which
 * triggers a pull. Realtime applies the same RLS policies, so only the household's rows arrive.
 * Rows are never taken from the event itself: the pull reads them through the normal path.
 */
@Singleton
class RealtimeSync @Inject constructor(private val backend: Backend) {

    suspend fun listen(cookbookId: String, onChange: () -> Unit) {
        val client = backend.client ?: return
        val channel = client.channel("cookbook-$cookbookId")
        val changes = channel.postgresChangeFlow<PostgresAction>(schema = "public")
        try {
            coroutineScope {
                launch { changes.collect { onChange() } }
                channel.subscribe()
            }
        } finally {
            withContext(NonCancellable) { client.realtime.removeChannel(channel) }
        }
    }
}
