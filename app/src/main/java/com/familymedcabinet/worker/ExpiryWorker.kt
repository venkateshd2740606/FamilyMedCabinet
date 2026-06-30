package com.familymedcabinet.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.familymedcabinet.data.local.PreferencesDataStore
import com.familymedcabinet.domain.repository.MedicineRepository
import com.familymedcabinet.engine.CabinetEngine
import com.familymedcabinet.notification.NotificationHelper
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first
import java.util.concurrent.TimeUnit

@HiltWorker
class ExpiryWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val medicineRepository: MedicineRepository,
    private val engine: CabinetEngine,
    private val notificationHelper: NotificationHelper,
    private val preferencesDataStore: PreferencesDataStore
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val prefs = preferencesDataStore.preferencesFlow.first()
        if (!prefs.expiryNotificationsEnabled) return Result.success()

        val expiring = medicineRepository
            .observeExpiringBefore(engine.expiryMillisInDays(30))
            .first()
        if (expiring.isNotEmpty()) {
            val names = expiring.take(3).joinToString(", ") { it.name }
            val suffix = if (expiring.size > 3) " and ${expiring.size - 3} more" else ""
            notificationHelper.showReminder(
                "Medicines Expiring Soon",
                "$names$suffix expire within 30 days. Check your cabinet."
            )
        }
        return Result.success()
    }

    companion object {
        private const val WORK_NAME = "cabinet_expiry_check"

        fun schedule(context: Context) {
            val request = PeriodicWorkRequestBuilder<ExpiryWorker>(1, TimeUnit.DAYS).build()
            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.UPDATE,
                request
            )
        }

        fun cancel(context: Context) {
            WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
        }
    }
}
