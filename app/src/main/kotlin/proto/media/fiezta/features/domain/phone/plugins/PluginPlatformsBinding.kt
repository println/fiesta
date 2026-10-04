package proto.media.fiezta.features.domain.phone.plugins

import android.view.View
import proto.media.fiezta.R
import proto.media.fiezta.support.plugins.manifest.PluginPlatforms

fun View.bindPlatforms(platforms: PluginPlatforms) {
    findViewById<View>(R.id.plugin_runs_in_car).visibility = if (platforms.car) View.VISIBLE else View.GONE
    findViewById<View>(R.id.plugin_runs_on_phone).visibility = if (platforms.phone) View.VISIBLE else View.GONE
}
