package app.jscookbook.core.sync

import app.jscookbook.core.data.sync.LocalSync
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Decides when to sync, for as long as the app process lives: whenever someone is signed in and
 * this phone is in a household, it syncs on start, shortly after every local change, whenever
 * Realtime reports a change from the other phone, and hourly in the background.
 */
@Singleton
class SyncCoordinator @Inject constructor(
    private val accounts: AccountRepository,
    private val household: HouseholdRepository,
    private val local: LocalSync,
    private val realtime: RealtimeSync,
    private val scheduler: SyncScheduler,
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private var started = false

    @OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
    fun start() {
        if (started) return
        started = true
        scope.launch {
            combine(accounts.account, local.observeLinkedCookbook()) { account, linked -> account to linked }
                .collectLatest { (account, linked) ->
                    when {
                        account !is Account.SignedIn -> {
                            if (account != Account.Loading) scheduler.cancel()
                        }
                        linked == null -> runCatching { household.relinkIfMember(account.userId) }
                        else -> {
                            scheduler.schedulePeriodic()
                            scheduler.syncNow()
                            val remoteChanges = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
                            coroutineScope {
                                launch {
                                    local.observePendingCount().drop(1).filter { it > 0 }.debounce(LocalChangeDelayMs)
                                        .collect { scheduler.syncNow() }
                                }
                                launch {
                                    remoteChanges.debounce(RemoteChangeDelayMs).collect { scheduler.syncNow() }
                                }
                                launch {
                                    runCatching { realtime.listen(linked) { remoteChanges.tryEmit(Unit) } }
                                }
                            }
                        }
                    }
                }
        }
    }

    private companion object {
        const val LocalChangeDelayMs = 1_500L
        const val RemoteChangeDelayMs = 500L
    }
}
