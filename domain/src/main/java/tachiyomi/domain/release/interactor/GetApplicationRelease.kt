package tachiyomi.domain.release.interactor

import tachiyomi.core.common.preference.Preference
import tachiyomi.core.common.preference.PreferenceStore
import tachiyomi.domain.release.model.Release
import tachiyomi.domain.release.service.ReleaseService
import java.time.Instant
import java.time.temporal.ChronoUnit

class GetApplicationRelease(
    private val service: ReleaseService,
    private val preferenceStore: PreferenceStore,
) {

    private val lastChecked: Preference<Long> by lazy {
        preferenceStore.getLong(Preference.appStateKey("last_app_check"), 0)
    }

    suspend fun await(arguments: Arguments): Result {
        val now = Instant.now()

        // Limit checks to once every 3 days at most
        if (!arguments.forceCheck &&
            now.isBefore(
                Instant.ofEpochMilli(lastChecked.get()).plus(3, ChronoUnit.DAYS),
            )
        ) {
            return Result.NoNewUpdate
        }

        val release = service.latest(arguments) ?: return Result.NoNewUpdate

        lastChecked.set(now.toEpochMilli())

        // Check if latest version is different from current version
        val isNewVersion = isNewVersion(
            arguments.isPreview,
            arguments.commitCount,
            arguments.versionName,
            release.version,
        )
        return if (isNewVersion) {
            if (!arguments.forceCheck) {
                val skippedVersion = preferenceStore.getString(Preference.appStateKey("last_skipped_version"), "").get()
                if (release.version == skippedVersion) {
                    val skippedTime = preferenceStore.getLong(Preference.appStateKey("last_skipped_time"), 0L).get()
                    val fiveDaysAgo = now.minus(5, ChronoUnit.DAYS).toEpochMilli()
                    if (skippedTime > fiveDaysAgo) {
                        Result.NoNewUpdate
                    } else {
                        Result.NewUpdate(release)
                    }
                } else {
                    Result.NewUpdate(release)
                }
            } else {
                Result.NewUpdate(release)
            }
        } else {
            Result.NoNewUpdate
        }
    }

    private fun isNewVersion(
        isPreview: Boolean,
        commitCount: Int,
        versionName: String,
        versionTag: String,
    ): Boolean {
        // Removes prefixes like "r" or "v"
        val newVersion = versionTag.replace("[^\\d.]".toRegex(), "")
        return if (isPreview) {
            // Preview builds: based on releases in "tachiyomiorg/tachiyomi-preview" repo
            // tagged as something like "r1234"
            newVersion.toIntOrNull()?.let { it > commitCount } ?: false
        } else {
            // Release builds: based on releases in "tachiyomiorg/tachiyomi" repo
            // tagged as something like "v0.1.2"
            val oldVersion = versionName.replace("[^\\d.]".toRegex(), "")

            val newSemVer = newVersion.split(".").mapNotNull { it.toIntOrNull() }
            val oldSemVer = oldVersion.split(".").mapNotNull { it.toIntOrNull() }

            val maxLength = maxOf(newSemVer.size, oldSemVer.size)
            for (i in 0 until maxLength) {
                val newVal = newSemVer.getOrElse(i) { 0 }
                val oldVal = oldSemVer.getOrElse(i) { 0 }
                if (newVal > oldVal) return true
                if (newVal < oldVal) return false
            }

            false
        }
    }

    data class Arguments(
        val isPreview: Boolean,
        val commitCount: Int,
        val versionName: String,
        val repository: String,
        val forceCheck: Boolean = false,
    )

    sealed interface Result {
        data class NewUpdate(val release: Release) : Result
        data object NoNewUpdate : Result
        data object OsTooOld : Result
    }
}
