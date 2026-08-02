package eu.kanade.presentation.more.settings.screen

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import eu.kanade.domain.ui.UiPreferences
import eu.kanade.presentation.more.settings.Preference
import eu.kanade.presentation.more.settings.screen.browse.UnifiedExtensionReposScreen
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.flow.combine
import mihon.domain.extensionrepo.anime.interactor.GetAnimeExtensionRepo
import mihon.domain.extensionrepo.manga.interactor.GetMangaExtensionRepo
import tachiyomi.i18n.MR
import tachiyomi.presentation.core.i18n.pluralStringResource
import tachiyomi.presentation.core.i18n.stringResource
import uy.kohesive.injekt.Injekt
import uy.kohesive.injekt.api.get
import tachiyomi.presentation.core.util.collectAsState as collectPreferencesAsState

object SettingsExperienceScreen : SearchableSettings {

    @ReadOnlyComposable
    @Composable
    override fun getTitleRes() = MR.strings.pref_category_experience

    @Composable
    override fun getPreferences(): List<Preference> {
        val navigator = LocalNavigator.currentOrThrow

        val uiPreferences = remember { Injekt.get<UiPreferences>() }
        val getMangaExtensionRepo = remember { Injekt.get<GetMangaExtensionRepo>() }
        val getAnimeExtensionRepo = remember { Injekt.get<GetAnimeExtensionRepo>() }
        val reposCount by remember {
            combine(getAnimeExtensionRepo.subscribeAll(), getMangaExtensionRepo.subscribeAll()) { anime, manga ->
                (anime + manga).distinctBy { it.baseUrl }.size
            }
        }.collectAsState(0)

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
                        ),
                    )
                    add(
                        Preference.PreferenceItem.SwitchPreference(
                            preference = showMangaPref,
                            title = stringResource(MR.strings.pref_show_manga),
                            subtitle = stringResource(MR.strings.pref_show_manga_summary),
                        ),
                    )
                    add(
                        Preference.PreferenceItem.SwitchPreference(
                            preference = uiPreferences.showRecommendations(),
                            title = stringResource(MR.strings.pref_show_recommendations),
                            subtitle = stringResource(MR.strings.pref_show_recommendations_summary),
                        ),
                    )
                    add(
                        Preference.PreferenceItem.SwitchPreference(
                            preference = uiPreferences.showHeroBanner(),
                            title = stringResource(MR.strings.kitsux_pref_show_hero_banner),
                            subtitle = stringResource(MR.strings.kitsux_pref_show_hero_banner_summary),
                        ),
                    )
                    if (showAnime || showManga) {
                        add(
                            Preference.PreferenceItem.TextPreference(
                                title = stringResource(MR.strings.label_extension_repos),
                                subtitle = pluralStringResource(
                                    MR.plurals.num_repos,
                                    reposCount,
                                    reposCount,
                                ),
                                onClick = {
                                    navigator.push(UnifiedExtensionReposScreen())
                                },
                            ),
                        )
                    }
                }.toPersistentList(),
            ),
        )
    }
}
