package eu.kanade.presentation.more.onboarding

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import eu.kanade.tachiyomi.extension.anime.AnimeExtensionManager
import eu.kanade.tachiyomi.extension.manga.MangaExtensionManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import mihon.domain.extensionrepo.anime.interactor.CreateAnimeExtensionRepo
import mihon.domain.extensionrepo.manga.interactor.CreateMangaExtensionRepo
import tachiyomi.i18n.MR
import tachiyomi.presentation.core.i18n.stringResource
import uy.kohesive.injekt.Injekt
import uy.kohesive.injekt.api.get

internal class ExtensionsRepoStep(
    private val onSuccess: () -> Unit,
) : OnboardingStep {

    private val createMangaExtensionRepo: CreateMangaExtensionRepo = Injekt.get()
    private val createAnimeExtensionRepo: CreateAnimeExtensionRepo = Injekt.get()

    override val title = MR.strings.kitsux_onboarding_extension_repo_title
    override val description = MR.strings.kitsux_onboarding_extension_repo_description

    override val isComplete: Boolean = true // Since this step can be skipped/omitted, isComplete is true

    @Composable
    override fun Content() {
        val scope = rememberCoroutineScope()
        var url by remember { mutableStateOf("") }
        var isAdding by remember { mutableStateOf(false) }
        var errorText by remember { mutableStateOf<String?>(null) }
        var successText by remember { mutableStateOf<String?>(null) }
        val repoAddedText = stringResource(MR.strings.kitsux_extension_repo_added)
        val repoInvalidText = stringResource(MR.strings.kitsux_extension_repo_invalid)
        val repoEmptyText = stringResource(MR.strings.kitsux_extension_repo_empty)

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            OutlinedTextField(
                value = url,
                onValueChange = {
                    url = it
                    errorText = null
                    successText = null
                },
                label = { Text(stringResource(MR.strings.kitsux_extension_repo_url)) },
                placeholder = { Text("https://...") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                enabled = !isAdding,
                isError = errorText != null,
            )

            if (errorText != null) {
                Text(
                    text = errorText!!,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.align(Alignment.Start),
                )
            }

            if (successText != null) {
                Text(
                    text = successText!!,
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.align(Alignment.Start),
                )
            }

            Button(
                onClick = {
                    if (url.isNotBlank()) {
                        isAdding = true
                        errorText = null
                        successText = null
                        scope.launch {
                            val resManga = withContext(Dispatchers.IO) { createMangaExtensionRepo.await(url) }
                            val resAnime = withContext(Dispatchers.IO) { createAnimeExtensionRepo.await(url) }
                            isAdding = false

                            val isMangaOk = resManga is CreateMangaExtensionRepo.Result.Success ||
                                resManga is CreateMangaExtensionRepo.Result.RepoAlreadyExists ||
                                resManga is CreateMangaExtensionRepo.Result.DuplicateFingerprint
                            val isAnimeOk = resAnime is CreateAnimeExtensionRepo.Result.Success ||
                                resAnime is CreateAnimeExtensionRepo.Result.RepoAlreadyExists ||
                                resAnime is CreateAnimeExtensionRepo.Result.DuplicateFingerprint

                            if (isMangaOk || isAnimeOk) {
                                withContext(Dispatchers.IO) {
                                    try {
                                        Injekt.get<MangaExtensionManager>().findAvailableExtensions()
                                        Injekt.get<AnimeExtensionManager>().findAvailableExtensions()
                                    } catch (e: Exception) {
                                        // Ignore
                                    }
                                }
                                successText = repoAddedText
                                scope.launch {
                                    kotlinx.coroutines.delay(1000)
                                    onSuccess()
                                }
                            } else {
                                errorText = repoInvalidText
                            }
                        }
                    } else {
                        errorText = repoEmptyText
                    }
                },
                enabled = !isAdding && url.isNotBlank(),
                modifier = Modifier.fillMaxWidth(),
            ) {
                if (isAdding) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp,
                    )
                } else {
                    Text(stringResource(MR.strings.kitsux_extension_repo_add))
                }
            }
        }
    }
}
