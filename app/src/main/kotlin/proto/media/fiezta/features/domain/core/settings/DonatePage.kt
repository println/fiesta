package proto.media.fiezta.features.domain.core.settings

object DonatePage {
    private const val ENGLISH_URL = "https://println.github.io/fiezta/donate.html"
    private const val PORTUGUESE_URL = "https://println.github.io/fiezta/pt-br/donate.html"

    fun urlFor(language: String): String =
        if (language.equals("pt", ignoreCase = true)) PORTUGUESE_URL else ENGLISH_URL
}
