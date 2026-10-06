package eu.kanade.tachiyomi.ui.browse.manga.source.globalsearch

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.produceState
import cafe.adriel.voyager.core.model.StateScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import dev.icerock.moko.resources.StringResource
import eu.kanade.domain.entries.manga.model.toDomainManga
import eu.kanade.domain.source.service.SourcePreferences
import eu.kanade.presentation.util.ioCoroutineScope
import eu.kanade.tachiyomi.extension.manga.MangaExtensionManager
import eu.kanade.tachiyomi.source.CatalogueSource
import kotlinx.collections.immutable.PersistentMap
import kotlinx.collections.immutable.mutate
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.collections.immutable.toPersistentMap
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import tachiyomi.core.common.preference.toggle
import tachiyomi.domain.entries.manga.interactor.GetManga
import tachiyomi.domain.entries.manga.interactor.NetworkToLocalManga
import tachiyomi.domain.entries.manga.model.Manga
import tachiyomi.domain.source.manga.service.MangaSourceManager
import uy.kohesive.injekt.Injekt
import uy.kohesive.injekt.api.get

abstract class MangaSearchScreenModel(
    initialState: State = State(),
    sourcePreferences: SourcePreferences = Injekt.get(),
    private val sourceManager: MangaSourceManager = Injekt.get(),
    private val extensionManager: MangaExtensionManager = Injekt.get(),
    private val networkToLocalManga: NetworkToLocalManga = Injekt.get(),
    private val getManga: GetManga = Injekt.get(),
    private val preferences: SourcePreferences = Injekt.get(),
) : StateScreenModel<MangaSearchScreenModel.State>(initialState) {

    private val coroutineDispatcher = Dispatchers.IO.limitedParallelism(5)
    private var searchJob: Job? = null
    private val retryJobs = mutableMapOf<Long, Job>()

    private val enabledLanguages = sourcePreferences.enabledLanguages().get()
    private val disabledSources = sourcePreferences.disabledMangaSources().get()
    protected val pinnedSources = sourcePreferences.pinnedMangaSources().get()

    private var lastQuery: String? = null
    private var lastSourceFilter: MangaSourceFilter? = null

    protected var extensionFilter: String? = null

    private val sortComparator = { map: Map<CatalogueSource, MangaSearchItemResult> ->
        compareBy<CatalogueSource>(
            { (map[it] as? MangaSearchItemResult.Success)?.isEmpty ?: true },
            { "${it.id}" !in pinnedSources },
            { "${it.name.lowercase()} (${it.lang})" },
        )
    }

    init {
        screenModelScope.launch {
            preferences.globalSearchFilterState().changes().collectLatest { state ->
                mutableState.update { it.copy(onlyShowHasResults = state) }
            }
        }
    }

    @Composable
    fun getManga(initialManga: Manga): androidx.compose.runtime.State<Manga> {
        return produceState(initialValue = initialManga) {
            getManga.subscribe(initialManga.url, initialManga.source)
                .filterNotNull()
                .collectLatest { manga ->
                    value = manga
                }
        }
    }

    open fun getEnabledSources(): List<CatalogueSource> {
        return sourceManager.getCatalogueSources()
            .filter { it.lang in enabledLanguages && "${it.id}" !in disabledSources }
            .sortedWith(
                compareBy(
                    { "${it.id}" !in pinnedSources },
                    { "${it.name.lowercase()} (${it.lang})" },
                ),
            )
    }

    private fun getSelectedSources(): List<CatalogueSource> {
        val enabledSources = getEnabledSources()

        val filter = extensionFilter
        if (filter.isNullOrEmpty()) {
            return enabledSources
        }

        return extensionManager.installedExtensionsFlow.value
            .filter { it.pkgName == filter }
            .flatMap { it.sources }
            .filterIsInstance<CatalogueSource>()
            .filter { it in enabledSources }
    }

    fun updateSearchQuery(query: String?) {
        mutableState.update { it.copy(searchQuery = query) }
    }

    fun setSourceFilter(filter: MangaSourceFilter) {
        mutableState.update { it.copy(sourceFilter = filter) }
        search()
    }

    fun toggleFilterResults() {
        preferences.globalSearchFilterState().toggle()
    }

    fun dismissEasterEgg() {
        mutableState.update { it.copy(activeEasterEgg = null) }
    }

    fun confirmEasterEgg() {
        val query = state.value.searchQuery ?: return
        mutableState.update { it.copy(activeEasterEgg = null) }
        search(force = true)
    }

    fun search(force: Boolean = false) {
        val query = state.value.searchQuery
        val sourceFilter = state.value.sourceFilter

        if (query.isNullOrBlank()) return

        if (!force) {
            val message = eu.kanade.tachiyomi.ui.browse.EasterEggHelper.getEasterEggMessage(query)
            if (message != null) {
                mutableState.update { it.copy(activeEasterEgg = message) }
                return
            }
        }

        val sameQuery = this.lastQuery == query
        if (sameQuery && this.lastSourceFilter == sourceFilter) return

        this.lastQuery = query
        this.lastSourceFilter = sourceFilter

        searchJob?.cancel()
        retryJobs.values.forEach { it.cancel() }
        retryJobs.clear()
        val sources = getSelectedSources()

        // Reuse previous results if possible
        if (sameQuery) {
            val existingResults = state.value.items
            updateItems(
                sources
                    .associateWith { existingResults[it] ?: MangaSearchItemResult.Loading }
                    .toPersistentMap(),
            )
        } else {
            updateItems(
                sources
                    .associateWith { MangaSearchItemResult.Loading }
                    .toPersistentMap(),
            )
        }
        searchJob = ioCoroutineScope.launch {
            sources.map { source ->
                async {
                    if (state.value.items[source] !is MangaSearchItemResult.Loading) {
                        return@async
                    }
                    val result = fetchSource(source, query)
                    if (isActive) updateItem(source, result)
                }
            }
                .awaitAll()
        }
    }

    fun retrySource(source: CatalogueSource) {
        val query = lastQuery ?: return
        if (state.value.items[source] !is MangaSearchItemResult.Error || retryJobs[source.id]?.isActive == true) return
        updateItem(source, MangaSearchItemResult.Loading)
        retryJobs[source.id] = ioCoroutineScope.launch {
            val result = fetchSource(source, query)
            if (isActive) updateItem(source, result)
        }
    }

    private suspend fun fetchSource(source: CatalogueSource, query: String): MangaSearchItemResult {
        return try {
            val page = withContext(coroutineDispatcher) {
                source.getSearchManga(1, query, source.getFilterList())
            }
            val titles = page.mangas.map {
                networkToLocalManga.await(it.toDomainManga(source.id))
            }
            MangaSearchItemResult.Success(titles)
        } catch (e: CancellationException) {
            throw e
        } catch (e: LinkageError) {
            MangaSearchItemResult.Error(e)
        } catch (e: Exception) {
            MangaSearchItemResult.Error(e)
        }
    }

    private fun updateItems(items: PersistentMap<CatalogueSource, MangaSearchItemResult>) {
        mutableState.update {
            it.copy(
                items = items
                    .toSortedMap(sortComparator(items))
                    .toPersistentMap(),
            )
        }
    }

    private fun updateItem(source: CatalogueSource, result: MangaSearchItemResult) {
        mutableState.update { current ->
            val newItems = current.items.mutate { it[source] = result }
            current.copy(items = newItems.toSortedMap(sortComparator(newItems)).toPersistentMap())
        }
    }

    @Immutable
    data class State(
        val fromSourceId: Long? = null,
        val searchQuery: String? = null,
        val sourceFilter: MangaSourceFilter = MangaSourceFilter.PinnedOnly,
        val onlyShowHasResults: Boolean = false,
        val items: PersistentMap<CatalogueSource, MangaSearchItemResult> = persistentMapOf(),
        val activeEasterEgg: StringResource? = null,
    ) {
        val progress: Int = items.count { it.value !is MangaSearchItemResult.Loading }
        val total: Int = items.size
        val filteredItems = items.filter { (_, result) -> result.isVisible(onlyShowHasResults) }
    }
}

enum class MangaSourceFilter {
    All,
    PinnedOnly,
}

sealed interface MangaSearchItemResult {
    data object Loading : MangaSearchItemResult

    data class Error(
        val throwable: Throwable,
    ) : MangaSearchItemResult

    data class Success(
        val result: List<Manga>,
    ) : MangaSearchItemResult {
        val isEmpty: Boolean
            get() = result.isEmpty()
    }

    fun isVisible(onlyShowHasResults: Boolean): Boolean {
        return !onlyShowHasResults || (this is Success && !this.isEmpty)
    }
}
