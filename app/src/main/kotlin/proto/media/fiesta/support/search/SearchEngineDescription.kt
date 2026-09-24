package proto.media.fiesta.support.search

data class SearchEngineDescription(
    val id: String,
    val version: Int,
    val shortName: String,
    val searchTemplate: String,
    val suggestionsTemplate: String?,
    val hosts: List<String>
)
