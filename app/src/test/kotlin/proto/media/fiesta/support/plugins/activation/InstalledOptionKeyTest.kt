package proto.media.fiesta.support.plugins.activation

import org.junit.Assert.assertEquals
import org.junit.Test

class InstalledOptionKeyTest {

    @Test
    fun buildsKeyFromPluginIdAndScriptName() {
        assertEquals(
            "pref_plugin_youtube_player_scroll_tweak",
            InstalledOptionKey.of("youtube", "player-scroll.tweak.js")
        )
    }
}
