package eu.kanade.tachiyomi.ui.browse.manga.source.globalsearch

import eu.kanade.domain.source.service.SourcePreferences
import eu.kanade.tachiyomi.source.CatalogueSource
import uy.kohesive.injekt.Injekt
import uy.kohesive.injekt.api.get

class GlobalMangaSearchScreenModel(
    initialQuery: String = "",
    initialExtensionFilter: String? = null,
    sourcePreferences: SourcePreferences = Injekt.get(),
) : MangaSearchScreenModel(
    State(
        searchQuery = initialQuery,
        sourceFilter = if (sourcePreferences.searchPinnedMangaSourcesOnly().get()) {
            MangaSourceFilter.PinnedOnly
        } else {
            MangaSourceFilter.All
        },
    ),
    sourcePreferences = sourcePreferences,
) {

    init {
        extensionFilter = initialExtensionFilter
        if (initialQuery.isNotBlank() || !initialExtensionFilter.isNullOrBlank()) {
            if (extensionFilter != null) {
                // we're going to use custom extension filter instead
                setSourceFilter(MangaSourceFilter.All)
            }
            search()
        }
    }

    override fun getEnabledSources(): List<CatalogueSource> {
        return super.getEnabledSources()
            .filter { state.value.sourceFilter != MangaSourceFilter.PinnedOnly || "${it.id}" in pinnedSources }
    }
}
