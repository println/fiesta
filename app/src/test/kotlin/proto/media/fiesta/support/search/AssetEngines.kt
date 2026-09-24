package proto.media.fiesta.support.search

import java.io.File

fun assetDescription(id: String): SearchEngineDescription =
    File("src/main/assets/search/$id.xml").inputStream().use { OpenSearchParser.parse(it) }

fun testEngine(id: String): DescribedSearchEngine =
    DescribedSearchEngine(assetDescription(id))
