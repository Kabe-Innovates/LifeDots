package com.example.lifedots.github

/**
 * Represents a single day's contribution data from GitHub.
 * @param date Date in "YYYY-MM-DD" format
 * @param level Contribution intensity level (0-4), matching GitHub's color scale
 */
data class ContributionDay(
    val date: String,
    val level: Int
)

/**
 * Cached GitHub contribution data for a user.
 */
data class GitHubContributionData(
    val username: String,
    val totalContributions: Int,
    val days: List<ContributionDay>,
    val lastFetched: Long // System.currentTimeMillis()
)
