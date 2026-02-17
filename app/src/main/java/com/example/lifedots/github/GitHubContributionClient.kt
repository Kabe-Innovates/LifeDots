package com.example.lifedots.github

import android.content.Context
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.jsoup.Jsoup
import java.util.concurrent.TimeUnit

/**
 * Client for fetching GitHub contribution data by scraping the public
 * contributions page: https://github.com/users/{username}/contributions
 *
 * Results are cached to SharedPreferences so the wallpaper can render
 * without network access.
 */
class GitHubContributionClient(private val context: Context) {

    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    private val gson = Gson()

    private val prefs by lazy {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    /**
     * Fetch contribution data from GitHub and cache it.
     * Must be called from a coroutine (runs on IO dispatcher).
     *
     * @return Result with the contribution data, or failure with exception
     */
    suspend fun fetchContributions(username: String): Result<GitHubContributionData> =
        withContext(Dispatchers.IO) {
            try {
                val url = "https://github.com/users/$username/contributions"
                val request = Request.Builder()
                    .url(url)
                    .header("User-Agent", "LifeDots-Android/1.0")
                    .build()

                val response = client.newCall(request).execute()

                if (!response.isSuccessful) {
                    return@withContext Result.failure(
                        Exception("GitHub returned ${response.code}: ${response.message}")
                    )
                }

                val html = response.body?.string()
                    ?: return@withContext Result.failure(Exception("Empty response from GitHub"))

                val data = parseContributionsHtml(username, html)
                cacheData(data)
                Result.success(data)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    /**
     * Parse the GitHub contributions HTML page to extract daily contribution levels.
     */
    private fun parseContributionsHtml(username: String, html: String): GitHubContributionData {
        val doc = Jsoup.parse(html)
        val days = mutableListOf<ContributionDay>()

        // GitHub renders contribution cells as <td> elements with data-date and data-level
        val cells = doc.select("td[data-date][data-level]")

        for (cell in cells) {
            val date = cell.attr("data-date")
            val level = cell.attr("data-level").toIntOrNull() ?: 0
            if (date.isNotEmpty()) {
                days.add(ContributionDay(date = date, level = level.coerceIn(0, 4)))
            }
        }

        // Calculate total contributions from the heading text or sum of levels
        val totalText = doc.select("h2.f4.text-normal.mb-2")
            .text()
            .replace(",", "")
            .replace(Regex("[^0-9]"), "")
        val total = totalText.toIntOrNull() ?: days.count { it.level > 0 }

        return GitHubContributionData(
            username = username,
            totalContributions = total,
            days = days,
            lastFetched = System.currentTimeMillis()
        )
    }

    /**
     * Cache contribution data to SharedPreferences as JSON.
     */
    private fun cacheData(data: GitHubContributionData) {
        prefs.edit()
            .putString(KEY_CACHED_DATA, gson.toJson(data))
            .apply()
    }

    /**
     * Load cached contribution data (for use by the wallpaper renderer).
     * Returns null if no data is cached.
     */
    fun getCachedData(): GitHubContributionData? {
        val json = prefs.getString(KEY_CACHED_DATA, null) ?: return null
        return try {
            gson.fromJson(json, GitHubContributionData::class.java)
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Check if cached data is stale (older than the given interval).
     */
    fun isCacheStale(refreshIntervalHours: Int): Boolean {
        val cached = getCachedData() ?: return true
        val ageMs = System.currentTimeMillis() - cached.lastFetched
        val intervalMs = refreshIntervalHours * 60 * 60 * 1000L
        return ageMs > intervalMs
    }

    /**
     * Clear cached data.
     */
    fun clearCache() {
        prefs.edit().remove(KEY_CACHED_DATA).apply()
    }

    companion object {
        private const val PREFS_NAME = "github_contributions_cache"
        private const val KEY_CACHED_DATA = "cached_contribution_data"
    }
}
