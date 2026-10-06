package eu.kanade.tachiyomi.ui.browse.manga.extension

import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import eu.kanade.presentation.browse.manga.MangaExtensionScreen
import eu.kanade.presentation.components.AppBar
import eu.kanade.presentation.components.TabContent
import eu.kanade.presentation.more.settings.screen.browse.UnifiedExtensionReposScreen
import eu.kanade.tachiyomi.extension.manga.model.MangaExtension
import eu.kanade.tachiyomi.ui.browse.manga.extension.details.MangaExtensionDetailsScreen
import eu.kanade.tachiyomi.ui.webview.WebViewScreen
import eu.kanade.tachiyomi.util.system.isPackageInstalled
import kotlinx.collections.immutable.persistentListOf
import mihon.domain.extensionrepo.anime.interactor.GetAnimeExtensionRepoCount
import mihon.domain.extensionrepo.manga.interactor.GetMangaExtensionRepoCount
import tachiyomi.i18n.MR
import tachiyomi.i18n.aniyomi.AYMR
import tachiyomi.presentation.core.i18n.stringResource
import uy.kohesive.injekt.Injekt
import uy.kohesive.injekt.api.get

@Composable
fun mangaExtensionsTab(
    extensionsScreenModel: MangaExtensionsScreenModel,
    listContent: (@Composable (LazyListScope.() -> Unit) -> Unit)? = null,
): TabContent {
    val navigator = LocalNavigator.currentOrThrow
    val context = LocalContext.current

    val state by extensionsScreenModel.state.collectAsState()
    val animeRepoCount by remember { Injekt.get<GetAnimeExtensionRepoCount>().subscribe() }
        .collectAsState(initial = 0)
    val mangaRepoCount by remember { Injekt.get<GetMangaExtensionRepoCount>().subscribe() }
        .collectAsState(initial = 0)
    val hasExtensionRepos = animeRepoCount > 0 || mangaRepoCount > 0
    var privateExtensionToUninstall by remember { mutableStateOf<MangaExtension?>(null) }
    var showUpdateAllConfirmation by remember { mutableStateOf(false) }

    return TabContent(
        titleRes = AYMR.strings.label_manga_extensions,
        badgeNumber = state.updates.takeIf { it > 0 },
        searchEnabled = true,
        actions = persistentListOf(
            AppBar.OverflowAction(
                title = stringResource(MR.strings.label_extension_repos),
                onClick = { navigator.push(UnifiedExtensionReposScreen()) },
            ),
            AppBar.OverflowAction(
                title = stringResource(MR.strings.action_filter),
                onClick = { navigator.push(MangaExtensionFilterScreen()) },
            ),
        ),
        content = { contentPadding, _ ->
            MangaExtensionScreen(
                state = state,
                listContent = listContent,
                hasExtensionRepos = hasExtensionRepos,
                contentPadding = contentPadding,
                searchQuery = state.searchQuery,
                onLongClickItem = { extension ->
                    when (extension) {
                        is MangaExtension.Available -> extensionsScreenModel.installExtension(
                            extension,
                        )
                        else -> {
                            if (context.isPackageInstalled(extension.pkgName)) {
                                extensionsScreenModel.uninstallExtension(extension)
                            } else {
                                privateExtensionToUninstall = extension
                            }
                        }
                    }
                },
                onClickItemCancel = extensionsScreenModel::cancelInstallUpdateExtension,
                onClickUpdateAll = { showUpdateAllConfirmation = true },
                onOpenWebView = { extension ->
                    extension.sources.getOrNull(0)?.let {
                        navigator.push(
                            WebViewScreen(
                                url = it.baseUrl,
                                initialTitle = it.name,
                                sourceId = it.id,
                            ),
                        )
                    }
                },
                onInstallExtension = extensionsScreenModel::installExtension,
                onOpenExtension = { navigator.push(MangaExtensionDetailsScreen(it.pkgName)) },
                onTrustExtension = { extensionsScreenModel.trustExtension(it) },
                onUninstallExtension = { extensionsScreenModel.uninstallExtension(it) },
                onUpdateExtension = extensionsScreenModel::updateExtension,
                onRefresh = extensionsScreenModel::findAvailableExtensions,
            )

            privateExtensionToUninstall?.let { extension ->
                MangaExtensionUninstallConfirmation(
                    extensionName = extension.name,
                    onClickConfirm = {
                        extensionsScreenModel.uninstallExtension(extension)
                    },
                    onDismissRequest = {
                        privateExtensionToUninstall = null
                    },
                )
            }

            if (showUpdateAllConfirmation) {
                ExtensionUpdateAllConfirmation(
                    updateCount = state.updates,
                    onClickConfirm = extensionsScreenModel::updateAllExtensions,
                    onDismissRequest = { showUpdateAllConfirmation = false },
                )
            }
        },
    )
}

@Composable
private fun ExtensionUpdateAllConfirmation(
    updateCount: Int,
    onClickConfirm: () -> Unit,
    onDismissRequest: () -> Unit,
) {
    AlertDialog(
        title = { Text(text = stringResource(MR.strings.ext_update_all_confirm_title)) },
        text = {
            Text(
                text = stringResource(
                    MR.strings.ext_update_all_confirm_message,
                    updateCount,
                ),
            )
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onDismissRequest()
                    onClickConfirm()
                },
            ) {
                Text(text = stringResource(MR.strings.ext_update_all))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text(text = stringResource(MR.strings.action_cancel))
            }
        },
        onDismissRequest = onDismissRequest,
    )
}

@Composable
private fun MangaExtensionUninstallConfirmation(
    extensionName: String,
    onClickConfirm: () -> Unit,
    onDismissRequest: () -> Unit,
) {
    AlertDialog(
        title = {
            Text(text = stringResource(MR.strings.ext_confirm_remove))
        },
        text = {
            Text(text = stringResource(MR.strings.remove_private_extension_message, extensionName))
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onClickConfirm()
                    onDismissRequest()
                },
            ) {
                Text(text = stringResource(MR.strings.ext_remove))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text(text = stringResource(MR.strings.action_cancel))
            }
        },
        onDismissRequest = onDismissRequest,
    )
}
