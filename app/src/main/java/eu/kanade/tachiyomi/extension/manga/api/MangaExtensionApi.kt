package eu.kanade.tachiyomi.extension.manga.api

import android.content.Context
import eu.kanade.tachiyomi.extension.ExtensionUpdateNotifier
import eu.kanade.tachiyomi.extension.api.FlexibleLongSerializer
import eu.kanade.tachiyomi.extension.manga.MangaExtensionManager
import eu.kanade.tachiyomi.extension.manga.model.MangaExtension
import eu.kanade.tachiyomi.extension.manga.model.MangaLoadResult
import eu.kanade.tachiyomi.extension.manga.util.MangaExtensionLoader
import eu.kanade.tachiyomi.network.GET
import eu.kanade.tachiyomi.network.NetworkHelper
import eu.kanade.tachiyomi.network.awaitSuccess
import eu.kanade.tachiyomi.network.parseAs
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.protobuf.ProtoBuf
import kotlinx.serialization.protobuf.ProtoNumber
import logcat.LogPriority
import mihon.domain.extensionrepo.manga.interactor.GetMangaExtensionRepo
import mihon.domain.extensionrepo.manga.interactor.UpdateMangaExtensionRepo
import mihon.domain.extensionrepo.model.ExtensionRepo
import tachiyomi.core.common.preference.Preference
import tachiyomi.core.common.preference.PreferenceStore
import tachiyomi.core.common.util.lang.withIOContext
import tachiyomi.core.common.util.system.logcat
import uy.kohesive.injekt.injectLazy
import java.io.ByteArrayInputStream
import java.time.Instant
import java.util.zip.GZIPInputStream
import kotlin.time.Duration.Companion.days

internal class MangaExtensionApi {

    private val networkService: NetworkHelper by injectLazy()
    private val preferenceStore: PreferenceStore by injectLazy()
    private val getExtensionRepo: GetMangaExtensionRepo by injectLazy()
    private val updateExtensionRepo: UpdateMangaExtensionRepo by injectLazy()
    private val extensionManager: MangaExtensionManager by injectLazy()
    private val json: Json by injectLazy()

    private val lastExtCheck: Preference<Long> by lazy {
        preferenceStore.getLong("last_ext_check", 0)
    }

    suspend fun findExtensions(): List<MangaExtension.Available> {
        return withIOContext {
            getExtensionRepo.getAll()
                .map { async { getExtensions(it) } }
                .awaitAll()
                .flatten()
        }
    }

    private suspend fun getExtensions(extRepo: ExtensionRepo): List<MangaExtension.Available> {
        val repoBaseUrl = extRepo.baseUrl
        return try {
            val modernIndexUrl = runCatching {
                with(json) {
                    networkService.client
                        .newCall(GET("$repoBaseUrl/repo.json"))
                        .awaitSuccess()
                        .parseAs<ExtensionRepoManifest>()
                        .indexV2
                }
            }.getOrNull()

            if (modernIndexUrl != null) {
                val response = networkService.client.newCall(GET(modernIndexUrl)).awaitSuccess()
                return ProtoBuf.decodeFromByteArray(
                    ModernExtensionStore.serializer(),
                    response.body.bytes().decompressGzipIfNeeded(),
                )
                    .extensionList
                    ?.toExtensions(repoBaseUrl)
                    .orEmpty()
            }

            val response = networkService.client
                .newCall(GET("$repoBaseUrl/index.min.json"))
                .awaitSuccess()

            with(json) {
                response
                    .parseAs<List<ExtensionJsonObject>>()
                    .toExtensions(repoBaseUrl)
            }
        } catch (e: Throwable) {
            logcat(LogPriority.ERROR, e) { "Failed to get extensions from $repoBaseUrl" }
            emptyList()
        }
    }

    suspend fun checkForUpdates(
        context: Context,
        fromAvailableExtensionList: Boolean = false,
    ): List<MangaExtension.Installed>? {
        // Limit checks to once a day at most
        if (fromAvailableExtensionList &&
            Instant.now().toEpochMilli() < lastExtCheck.get() + 1.days.inWholeMilliseconds
        ) {
            return null
        }

        // Update extension repo details
        updateExtensionRepo.awaitAll()

        val extensions = if (fromAvailableExtensionList) {
            extensionManager.availableExtensionsFlow.value
        } else {
            findExtensions().also { lastExtCheck.set(Instant.now().toEpochMilli()) }
        }

        val installedExtensions = MangaExtensionLoader.loadMangaExtensions(context)
            .filterIsInstance<MangaLoadResult.Success>()
            .map { it.extension }

        val extensionsWithUpdate = mutableListOf<MangaExtension.Installed>()
        for (installedExt in installedExtensions) {
            val pkgName = installedExt.pkgName
            val availableExt = extensions.find { it.pkgName == pkgName } ?: continue
            val hasUpdatedVer = availableExt.versionCode > installedExt.versionCode
            val hasUpdatedLib = availableExt.libVersion > installedExt.libVersion
            val hasUpdate = hasUpdatedVer || hasUpdatedLib
            if (hasUpdate) {
                extensionsWithUpdate.add(installedExt)
            }
        }

        if (extensionsWithUpdate.isNotEmpty()) {
            ExtensionUpdateNotifier(context).promptUpdates(extensionsWithUpdate.map { it.name })
        }

        return extensionsWithUpdate
    }

    private fun List<ExtensionJsonObject>.toExtensions(repoUrl: String): List<MangaExtension.Available> {
        return this
            .filter {
                val libVersion = it.extractLibVersion()
                libVersion >= MangaExtensionLoader.LIB_VERSION_MIN && libVersion <= MangaExtensionLoader.LIB_VERSION_MAX
            }
            .map {
                MangaExtension.Available(
                    name = it.name.substringAfter("Tachiyomi: "),
                    pkgName = it.pkg,
                    versionName = it.version,
                    versionCode = it.code,
                    libVersion = it.extractLibVersion(),
                    lang = it.lang,
                    isNsfw = it.nsfw == 1,
                    sources = it.sources?.map(extensionSourceMapper).orEmpty(),
                    apkName = it.apk,
                    iconUrl = "$repoUrl/icon/${it.pkg}.png",
                    repoUrl = repoUrl,
                )
            }
    }

    fun getApkUrl(extension: MangaExtension.Available): String {
        return extension.apkName.takeIf { it.startsWith("https://") }
            ?: "${extension.repoUrl}/apk/${extension.apkName}"
    }

    private fun ExtensionJsonObject.extractLibVersion(): Double {
        return version.substringBeforeLast('.').toDouble()
    }
}

private fun ByteArray.decompressGzipIfNeeded(): ByteArray {
    val isGzip = size >= 2 && this[0] == 0x1f.toByte() && this[1] == 0x8b.toByte()
    return if (isGzip) {
        GZIPInputStream(ByteArrayInputStream(this)).use { it.readBytes() }
    } else {
        this
    }
}

@Serializable
private data class ExtensionRepoManifest(
    @kotlinx.serialization.SerialName("index_v2") val indexV2: String? = null,
)

@Serializable
private data class ModernExtensionStore(
    @ProtoNumber(101) val extensionList: ModernExtensionList? = null,
)

@Serializable
private data class ModernExtensionList(
    @ProtoNumber(1) val extensions: List<ModernExtension> = emptyList(),
) {
    fun toExtensions(repoUrl: String): List<MangaExtension.Available> {
        return extensions
            .filter {
                val libVersion = it.extensionLib.toDoubleOrNull()
                libVersion != null &&
                    libVersion >= MangaExtensionLoader.LIB_VERSION_MIN &&
                    libVersion <= MangaExtensionLoader.LIB_VERSION_MAX
            }
            .map { extension ->
                val languages = extension.sources.map { it.language }.distinct()
                MangaExtension.Available(
                    name = extension.name,
                    pkgName = extension.packageName,
                    versionName = extension.versionName,
                    versionCode = extension.versionCode,
                    libVersion = extension.extensionLib.toDouble(),
                    lang = languages.singleOrNull() ?: "all",
                    isNsfw = extension.contentWarning >= ModernContentWarning.MIXED,
                    sources = extension.sources.map { source ->
                        MangaExtension.Available.MangaSource(
                            id = source.id,
                            lang = source.language,
                            name = source.name,
                            baseUrl = source.homeUrl,
                        )
                    },
                    apkName = extension.resources.apkUrl,
                    iconUrl = extension.resources.iconUrl,
                    repoUrl = repoUrl,
                )
            }
    }
}

@Serializable
private data class ModernExtension(
    @ProtoNumber(1) val name: String,
    @ProtoNumber(2) val packageName: String,
    @ProtoNumber(3) val resources: ModernExtensionResources,
    @ProtoNumber(4) val extensionLib: String,
    @ProtoNumber(5) val versionCode: Long,
    @ProtoNumber(6) val versionName: String,
    @ProtoNumber(7) val contentWarning: ModernContentWarning,
    @ProtoNumber(8) val sources: List<ModernExtensionSource> = emptyList(),
)

@Serializable
private data class ModernExtensionResources(
    @ProtoNumber(1) val apkUrl: String,
    @ProtoNumber(2) val iconUrl: String,
)

@Serializable
private data class ModernExtensionSource(
    @ProtoNumber(1) val id: Long,
    @ProtoNumber(2) val name: String,
    @ProtoNumber(3) val language: String,
    @ProtoNumber(4) val homeUrl: String = "",
)

@Serializable
private enum class ModernContentWarning {
    @ProtoNumber(0)
    UNSPECIFIED,

    @ProtoNumber(1)
    SAFE,

    @ProtoNumber(2)
    MIXED,

    @ProtoNumber(3)
    NSFW,
}

@Serializable
private data class ExtensionJsonObject(
    val name: String,
    val pkg: String,
    val apk: String,
    val lang: String,
    val code: Long,
    val version: String,
    val nsfw: Int,
    val sources: List<ExtensionSourceJsonObject>? = null,
)

@Serializable
private data class ExtensionSourceJsonObject(
    @Serializable(with = FlexibleLongSerializer::class)
    val id: Long,
    val lang: String,
    val name: String,
    val baseUrl: String,
)

private val extensionSourceMapper: (ExtensionSourceJsonObject) -> MangaExtension.Available.MangaSource = {
    MangaExtension.Available.MangaSource(
        id = it.id,
        lang = it.lang,
        name = it.name,
        baseUrl = it.baseUrl,
    )
}
