package app.jscookbook.core.sync

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class SyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val engine: SyncEngine,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result = when (engine.sync()) {
        SyncResult.Success, SyncResult.Skipped -> Result.success()
        is SyncResult.Failed -> if (runAttemptCount < MaxAttempts) Result.retry() else Result.failure()
    }

    private companion object {
        const val MaxAttempts = 8
    }
}
