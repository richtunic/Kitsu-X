package tachiyomi.data.release

import android.os.Build
import eu.kanade.tachiyomi.network.GET
import eu.kanade.tachiyomi.network.NetworkHelper
import eu.kanade.tachiyomi.network.awaitSuccess
import eu.kanade.tachiyomi.network.parseAs
import kotlinx.serialization.json.Json
import tachiyomi.domain.release.interactor.GetApplicationRelease
import tachiyomi.domain.release.model.Release
import tachiyomi.domain.release.service.ReleaseService

class ReleaseServiceImpl(
    private val networkService: NetworkHelper,
    private val json: Json,
) : ReleaseService {

    override suspend fun latest(arguments: GetApplicationRelease.Arguments): Release? {
        val release = with(json) {
            networkService.client
                .newCall(GET("https://api.github.com/repos/${arguments.repository}/releases/latest"))
                .awaitSuccess()
                .parseAs<GithubRelease>()
        }
        val downloadLink = getDownloadLink(release = release, installedAbi = arguments.installedAbi) ?: return null

        return Release(
            version = release.versionName,
            info = release.info.replace(gitHubUsernameMentionRegex) { mention ->
                "[${mention.value}](https://github.com/${mention.value.substring(1)})"
            },
            releaseLink = release.releaseLink,
            downloadLink = downloadLink,
        )
    }

    private fun getDownloadLink(release: GithubRelease, installedAbi: String?): String? {
        val apkAssets = release.assets.filter { it.name.endsWith(".apk", ignoreCase = true) }

        return preferredApkAssetNames(installedAbi).firstNotNullOfOrNull { preferredName ->
            apkAssets.firstOrNull { asset ->
                asset.name.matchesApkVariant(preferredName)
            }?.downloadLink
        }
    }

    companion object {
        internal fun preferredApkAssetNames(installedAbi: String?): List<String> {
            return listOfNotNull(installedAbi?.takeIf { it in supportedReleaseAbis })
                .plus(
                    Build.SUPPORTED_ABIS
                        .filter { it in supportedReleaseAbis },
                )
                .plus("universal")
                .distinct()
        }

        private fun String.matchesApkVariant(variant: String): Boolean {
            val variantRegex = "(^|[-_])${Regex.escape(variant)}($|[-.])"
                .toRegex(RegexOption.IGNORE_CASE)
            return removeSuffix(".apk").matches(variantRegex) ||
                variantRegex.containsMatchIn(this)
        }

        internal fun preferredApkAssetNames(): List<String> {
            return preferredApkAssetNames(installedAbi = null)
        }

        private val supportedReleaseAbis = setOf("armeabi-v7a", "arm64-v8a", "x86", "x86_64")

        private val GithubRelease.versionName: String
            get() = name
                ?.takeIf { it.isNotBlank() }
                ?: version.removePrefix("v")

        /**
         * Regular expression that matches a mention to a valid GitHub username, like it's
         * done in GitHub Flavored Markdown. It follows these constraints:
         *
         * - Alphanumeric with single hyphens (no consecutive hyphens)
         * - Cannot begin or end with a hyphen
         * - Max length of 39 characters
         *
         * Reference: https://stackoverflow.com/a/30281147
         */
        private val gitHubUsernameMentionRegex = """\B@([a-z0-9](?:-(?=[a-z0-9])|[a-z0-9]){0,38}(?<=[a-z0-9]))"""
            .toRegex(RegexOption.IGNORE_CASE)
    }
}
