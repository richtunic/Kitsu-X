package eu.kanade.presentation.more.settings.screen

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.collectAsState
import tachiyomi.presentation.core.util.collectAsState as collectPreferencesAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import eu.kanade.domain.ui.UiPreferences
import eu.kanade.presentation.more.settings.Preference
import eu.kanade.presentation.more.settings.screen.browse.AnimeExtensionReposScreen
import eu.kanade.presentation.more.settings.screen.browse.MangaExtensionReposScreen
import kotlinx.collections.immutable.toPersistentList
import mihon.domain.extensionrepo.anime.interactor.GetAnimeExtensionRepoCount
import mihon.domain.extensionrepo.manga.interactor.GetMangaExtensionRepoCount
import tachiyomi.i18n.MR
import tachiyomi.i18n.aniyomi.AYMR
import tachiyomi.presentation.core.i18n.pluralStringResource
import tachiyomi.presentation.core.i18n.stringResource
import uy.kohesive.injekt.Injekt
import uy.kohesive.injekt.api.get

object SettingsExperienceScreen : SearchableSettings {

    @ReadOnlyComposable
    @Composable
    override fun getTitleRes() = MR.strings.pref_category_experience

    @Composable
    override fun getPreferences(): List<Preference> {
        val navigator = LocalNavigator.currentOrThrow

        val uiPreferences = remember { Injekt.get<UiPreferences>() }
        val getMangaExtensionRepoCount = remember { Injekt.get<GetMangaExtensionRepoCount>() }
        val getAnimeExtensionRepoCount = remember { Injekt.get<GetAnimeExtensionRepoCount>() }

        val mangaReposCount by getMangaExtensionRepoCount.subscribe().collectAsState(0)
        val animeReposCount by getAnimeExtensionRepoCount.subscribe().collectAsState(0)

        val showAnimePref = uiPreferences.showAnime()
        val showMangaPref = uiPreferences.showManga()

        val showAnime by showAnimePref.collectPreferencesAsState()
        val showManga by showMangaPref.collectPreferencesAsState()

        return listOf(
            Preference.PreferenceGroup(
                title = stringResource(MR.strings.pref_category_experience),
                preferenceItems = buildList {
                    add(
                        Preference.PreferenceItem.SwitchPreference(
                            preference = showAnimePref,
                            title = stringResource(MR.strings.pref_show_anime),
                            subtitle = stringResource(MR.strings.pref_show_anime_summary),
                        )
                    )
                    add(
                        Preference.PreferenceItem.SwitchPreference(
                            preference = showMangaPref,
                            title = stringResource(MR.strings.pref_show_manga),
                            subtitle = stringResource(MR.strings.pref_show_manga_summary),
                        )
                    )
                add(
                    Preference.PreferenceItem.SwitchPreference(
                        preference = uiPreferences.showRecommendations(),
                        title = stringResource(MR.strings.pref_show_recommendations),
                        subtitle = stringResource(MR.strings.pref_show_recommendations_summary),
                    )
                )
                add(
                    Preference.PreferenceItem.SwitchPreference(
                        preference = uiPreferences.showHeroBanner(),
                        title = stringResource(MR.strings.kitsux_pref_show_hero_banner),
                        subtitle = stringResource(MR.strings.kitsux_pref_show_hero_banner_summary),
                    )
                )
                if (showAnime) {
                        add(
                            Preference.PreferenceItem.TextPreference(
                                title = stringResource(AYMR.strings.label_anime_extension_repos),
                                subtitle = pluralStringResource(
                                    MR.plurals.num_repos,
                                    animeReposCount,
                                    animeReposCount,
                                ),
                                onClick = {
                                    navigator.push(AnimeExtensionReposScreen())
                                },
                            )
                        )
                    }
                    if (showManga) {
                        add(
                            Preference.PreferenceItem.TextPreference(
                                title = stringResource(AYMR.strings.label_manga_extension_repos),
                                subtitle = pluralStringResource(
                                    MR.plurals.num_repos,
                                    mangaReposCount,
                                    mangaReposCount,
                                ),
                                onClick = {
                                    navigator.push(MangaExtensionReposScreen())
                                },
                            )
                        )
                    }
                }.toPersistentList()
            )
        )
    }
}
