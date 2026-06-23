package eu.kanade.tachiyomi.data.updater

import android.content.Context
import android.os.Build
import java.util.Locale

fun Context.localizedReleaseInfo(body: String): String {
    val cleanBody = body.withoutChecksums()
    val languageCode = resources.configuration.run {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            locales[0]?.language
        } else {
            @Suppress("DEPRECATION")
            locale.language
        }
    }
    val targetLanguage = if (languageCode == "es") "es" else "en"

    return cleanBody.extractMarkdownLanguageSection(targetLanguage)
        ?: cleanBody.extractMarkdownLanguageSection("en")
        ?: cleanBody
}

private fun String.withoutChecksums(): String {
    return replace("""---(\R|.)*Checksums(\R|.)*""".toRegex(), "").trim()
}

private fun String.extractMarkdownLanguageSection(languageCode: String): String? {
    val sections = releaseLanguageSectionRegex.findAll(this).toList()
    if (sections.isEmpty()) return null

    return sections
        .mapIndexedNotNull { index, match ->
            val sectionLanguage = match.groupValues[1].lowercase(Locale.US)
            if (sectionLanguage != languageCode) return@mapIndexedNotNull null

            val sectionStart = match.range.last + 1
            val sectionEnd = sections.getOrNull(index + 1)?.range?.first ?: length
            substring(sectionStart, sectionEnd).trim()
        }
        .firstOrNull { it.isNotBlank() }
}

private val releaseLanguageSectionRegex = Regex(
    pattern = """(?m)^#{2,3}\s*(es|en)(?:[-_][A-Za-z]{2})?\s*$""",
    option = RegexOption.IGNORE_CASE,
)
