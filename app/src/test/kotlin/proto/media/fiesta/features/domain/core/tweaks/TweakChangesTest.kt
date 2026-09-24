package proto.media.fiesta.features.domain.core.tweaks

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TweakChangesTest {

    @Test
    fun noChangesDoesNotReload() {
        val changes = TweakChanges()
        changes.recordInitial("pref_youtube_scroll_to_player", true)

        assertFalse(changes.shouldReload())
    }

    @Test
    fun singleChangeReloads() {
        val changes = TweakChanges()
        changes.recordInitial("pref_youtube_scroll_to_player", true)
        changes.update("pref_youtube_scroll_to_player", false)

        assertTrue(changes.shouldReload())
    }

    @Test
    fun togglingBackToInitialDoesNotReload() {
        val changes = TweakChanges()
        changes.recordInitial("pref_youtube_scroll_to_player", true)
        changes.update("pref_youtube_scroll_to_player", false)
        changes.update("pref_youtube_scroll_to_player", true)

        assertFalse(changes.shouldReload())
    }
}
