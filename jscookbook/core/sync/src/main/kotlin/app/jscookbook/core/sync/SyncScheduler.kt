package app.jscookbook.core.sync

import android.content.Context
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Sync runs in WorkManager so it survives the app closing and waits for a connection: a change made
 * offline goes up as soon as the phone is back online.
 */
@Singleton
class SyncScheduler @Inject constructor(@ApplicationContext private val context: Context) {

    private val online = Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()

    /** Sync as soon as there's a connection. A pass already queued or running covers it. */
    fun syncNow() {
        WorkManager.getInstance(context).enqueueUniqueWork(
            NowWork,
            ExistingWorkPolicy.KEEP,
            OneTimeWorkRequestBuilder<SyncWorker>()
                .setConstraints(online)
                .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 15, TimeUnit.SECONDS)
                .build(),
        )
    }

    /** A background pass every hour, for changes that arrive while the app isn't open. */
    fun schedulePeriodic() {
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            PeriodicWork,
            ExistingPeriodicWorkPolicy.KEEP,
            PeriodicWorkRequestBuilder<SyncWorker>(1, TimeUnit.HOURS).setConstraints(online).build(),
        )
    }

    fun cancel() {
        WorkManager.getInstance(context).apply {
            cancelUniqueWork(NowWork)
            cancelUniqueWork(PeriodicWork)
        }
    }

    private companion object {
        const val NowWork = "sync-now"
        const val PeriodicWork = "sync-periodic"
    }
}
