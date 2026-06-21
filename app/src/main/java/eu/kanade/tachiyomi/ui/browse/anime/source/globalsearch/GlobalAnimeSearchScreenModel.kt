package eu.kanade.tachiyomi.ui.browse.anime.source.globalsearch

import eu.kanade.domain.source.service.SourcePreferences
import eu.kanade.tachiyomi.animesource.AnimeCatalogueSource
import uy.kohesive.injekt.Injekt
import uy.kohesive.injekt.api.get

class GlobalAnimeSearchScreenModel(
    initialQuery: String = "",
    initialExtensionFilter: String? = null,
    sourcePreferences: SourcePreferences = Injekt.get(),
) : AnimeSearchScreenModel(
    State(
        searchQuery = initialQuery,
        sourceFilter = if (sourcePreferences.searchPinnedAnimeSourcesOnly().get()) {
            AnimeSourceFilter.PinnedOnly
        } else {
            AnimeSourceFilter.All
        },
    ),
    sourcePreferences = sourcePreferences,
) {

    init {
        extensionFilter = initialExtensionFilter
        if (initialQuery.isNotBlank() || !initialExtensionFilter.isNullOrBlank()) {
            if (extensionFilter != null) {
                // we're going to use custom extension filter instead
                setSourceFilter(AnimeSourceFilter.All)
            }
            search()
        }
    }

    override fun getEnabledSources(): List<AnimeCatalogueSource> {
        return super.getEnabledSources()
            .filter { state.value.sourceFilter != AnimeSourceFilter.PinnedOnly || "${it.id}" in pinnedSources }
    }
}
