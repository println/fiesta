package proto.media.fiezta.support.search

import java.net.URLEncoder

internal fun encodeQuery(query: String): String = URLEncoder.encode(query, "UTF-8")
