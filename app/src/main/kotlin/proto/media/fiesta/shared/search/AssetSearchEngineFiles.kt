package proto.media.fiesta.shared.search

import android.content.Context
import org.json.JSONObject
import proto.media.fiesta.support.search.SearchEngineDefaults
import proto.media.fiesta.support.search.SearchEngineFiles
import java.io.InputStream

class AssetSearchEngineFiles(context: Context) : SearchEngineFiles {

    private val context: Context = context.applicationContext

    override fun readDefaults(): SearchEngineDefaults {
        val json = JSONObject(context.assets.open("$DIR/defaults.json").bufferedReader(Charsets.UTF_8).use { it.readText() })
        val order = json.getJSONArray("order")
        return SearchEngineDefaults(
            order = (0 until order.length()).map { order.getString(it) },
            fallback = json.getString("fallback")
        )
    }

    override fun openDescription(engineId: String): InputStream = context.assets.open("$DIR/$engineId.xml")

    private companion object {
        const val DIR = "search"
    }
}
