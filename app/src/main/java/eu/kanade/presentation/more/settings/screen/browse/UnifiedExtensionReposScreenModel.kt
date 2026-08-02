package eu.kanade.presentation.more.settings.screen.browse

import cafe.adriel.voyager.core.model.StateScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import eu.kanade.tachiyomi.extension.anime.AnimeExtensionManager
import eu.kanade.tachiyomi.extension.manga.MangaExtensionManager
import kotlinx.collections.immutable.toImmutableSet
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import mihon.domain.extensionrepo.anime.interactor.CreateAnimeExtensionRepo
import mihon.domain.extensionrepo.anime.interactor.DeleteAnimeExtensionRepo
import mihon.domain.extensionrepo.anime.interactor.GetAnimeExtensionRepo
import mihon.domain.extensionrepo.anime.interactor.ReplaceAnimeExtensionRepo
import mihon.domain.extensionrepo.anime.interactor.UpdateAnimeExtensionRepo
import mihon.domain.extensionrepo.manga.interactor.CreateMangaExtensionRepo
import mihon.domain.extensionrepo.manga.interactor.DeleteMangaExtensionRepo
import mihon.domain.extensionrepo.manga.interactor.GetMangaExtensionRepo
import mihon.domain.extensionrepo.manga.interactor.ReplaceMangaExtensionRepo
import mihon.domain.extensionrepo.manga.interactor.UpdateMangaExtensionRepo
import mihon.domain.extensionrepo.model.ExtensionRepo
import tachiyomi.core.common.util.lang.launchIO
import uy.kohesive.injekt.Injekt
import uy.kohesive.injekt.api.get

class UnifiedExtensionReposScreenModel(
    private val getAnimeRepo: GetAnimeExtensionRepo = Injekt.get(),
    private val getMangaRepo: GetMangaExtensionRepo = Injekt.get(),
    private val createAnimeRepo: CreateAnimeExtensionRepo = Injekt.get(),
    private val createMangaRepo: CreateMangaExtensionRepo = Injekt.get(),
    private val deleteAnimeRepo: DeleteAnimeExtensionRepo = Injekt.get(),
    private val deleteMangaRepo: DeleteMangaExtensionRepo = Injekt.get(),
    private val replaceAnimeRepo: ReplaceAnimeExtensionRepo = Injekt.get(),
    private val replaceMangaRepo: ReplaceMangaExtensionRepo = Injekt.get(),
    private val updateAnimeRepo: UpdateAnimeExtensionRepo = Injekt.get(),
    private val updateMangaRepo: UpdateMangaExtensionRepo = Injekt.get(),
    private val animeExtensionManager: AnimeExtensionManager = Injekt.get(),
    private val mangaExtensionManager: MangaExtensionManager = Injekt.get(),
) : StateScreenModel<RepoScreenState>(RepoScreenState.Loading) {

    private val _events = Channel<RepoEvent>(Channel.UNLIMITED)
    val events = _events.receiveAsFlow()
    private var existingReposSynchronized = false

    init {
        screenModelScope.launchIO {
            combine(getAnimeRepo.subscribeAll(), getMangaRepo.subscribeAll()) { anime, manga -> anime to manga }
                .collectLatest { (animeRepos, mangaRepos) ->
                    val repos = (animeRepos + mangaRepos).distinctBy(ExtensionRepo::baseUrl)
                    mutableState.update { RepoScreenState.Success(repos.toImmutableSet()) }

                    if (!existingReposSynchronized) {
                        existingReposSynchronized = true
                        val animeUrls = animeRepos.mapTo(mutableSetOf(), ExtensionRepo::baseUrl)
                        val mangaUrls = mangaRepos.mapTo(mutableSetOf(), ExtensionRepo::baseUrl)
                        animeRepos.filterNot { it.baseUrl in mangaUrls }.forEach { createMangaRepo.await(it.baseUrl) }
                        mangaRepos.filterNot { it.baseUrl in animeUrls }.forEach { createAnimeRepo.await(it.baseUrl) }
                        refreshExtensions()
                    }
                }
        }
    }

    fun createRepo(baseUrl: String) {
        screenModelScope.launchIO {
            val animeResult = createAnimeRepo.await(baseUrl)
            val mangaResult = createMangaRepo.await(baseUrl)
            val conflict = when {
                animeResult is CreateAnimeExtensionRepo.Result.DuplicateFingerprint ->
                    RepoDialog.Conflict(animeResult.oldRepo, animeResult.newRepo)
                mangaResult is CreateMangaExtensionRepo.Result.DuplicateFingerprint ->
                    RepoDialog.Conflict(mangaResult.oldRepo, mangaResult.newRepo)
                else -> null
            }

            when {
                conflict != null -> showDialog(conflict)
                animeResult is CreateAnimeExtensionRepo.Result.InvalidUrl &&
                    mangaResult is CreateMangaExtensionRepo.Result.InvalidUrl -> _events.send(RepoEvent.InvalidUrl)
                animeResult is CreateAnimeExtensionRepo.Result.RepoAlreadyExists &&
                    mangaResult is CreateMangaExtensionRepo.Result.RepoAlreadyExists ->
                    _events.send(RepoEvent.RepoAlreadyExists)
                else -> {
                    dismissDialog()
                    refreshExtensions()
                }
            }
        }
    }

    fun replaceRepo(newRepo: ExtensionRepo) {
        screenModelScope.launchIO {
            replaceAnimeRepo.await(newRepo)
            replaceMangaRepo.await(newRepo)
            dismissDialog()
            refreshExtensions()
        }
    }

    fun refreshRepos() {
        screenModelScope.launchIO {
            updateAnimeRepo.awaitAll()
            updateMangaRepo.awaitAll()
            refreshExtensions()
        }
    }

    fun deleteRepo(baseUrl: String) {
        screenModelScope.launchIO {
            deleteAnimeRepo.await(baseUrl)
            deleteMangaRepo.await(baseUrl)
        }
    }

    fun showDialog(dialog: RepoDialog) {
        mutableState.update { state ->
            if (state is RepoScreenState.Success) state.copy(dialog = dialog) else state
        }
    }

    fun dismissDialog() {
        mutableState.update { state ->
            if (state is RepoScreenState.Success) state.copy(dialog = null) else state
        }
    }

    private suspend fun refreshExtensions() {
        runCatching { animeExtensionManager.findAvailableExtensions() }
        runCatching { mangaExtensionManager.findAvailableExtensions() }
    }
}
