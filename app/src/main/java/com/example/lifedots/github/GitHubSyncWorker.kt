package com.example.lifedots.github

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.example.lifedots.preferences.LifeDotsPreferences
import java.util.concurrent.TimeUnit

/**
 * Background worker that periodically fetches GitHub contribution data
 * and caches it for the wallpaper renderer.
 */
class GitHubSyncWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val preferences = LifeDotsPreferences.getInstance(applicationContext)
        val settings = preferences.settings

        // Only fetch if GitHub is enabled and username is set
        if (!settings.gitHubSettings.enabled || settings.gitHubSettings.username.isBlank()) {
            return Result.success()
        }

        val client = GitHubContributionClient(applicationContext)
        val result = client.fetchContributions(settings.gitHubSettings.username)

        return if (result.isSuccess) {
            // Trigger wallpaper redraw
            preferences.notifyWallpaperUpdate()
            Result.success()
        } else {
            Result.retry()
        }
    }

    companion object {
        private const val WORK_NAME = "github_sync"

        /**
         * Schedule periodic sync. Call when GitHub is enabled or interval changes.
         */
        fun schedule(context: Context, intervalHours: Int) {
            val request = PeriodicWorkRequestBuilder<GitHubSyncWorker>(
                intervalHours.toLong(), TimeUnit.HOURS
            ).build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.UPDATE,
                request
            )
        }

        /**
         * Cancel periodic sync. Call when GitHub is disabled.
         */
        fun cancel(context: Context) {
            WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
        }
    }
}
