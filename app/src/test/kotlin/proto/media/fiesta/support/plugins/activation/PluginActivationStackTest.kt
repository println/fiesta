package proto.media.fiesta.support.plugins.activation

import proto.media.fiesta.support.plugins.model.ActivationState
import proto.media.fiesta.support.plugins.model.LoadedManifest
import proto.media.fiesta.support.plugins.model.PluginManifest
import proto.media.fiesta.support.plugins.model.PluginOrigin
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PluginActivationStackTest {

    private val empty = ActivationState(emptyList(), emptySet(), emptySet())

    @Test
    fun firstLoadActivatesDefaultsInFirstContactOrder() {
        val state = PluginActivationStack.withNewDefaults(empty, listOf("youtube", "generic"))
        assertEquals(listOf("youtube", "generic"), state.order)
    }

    @Test
    fun newDefaultOnUpdateGoesToTheEnd() {
        val afterFirstLoad = PluginActivationStack.withNewDefaults(empty, listOf("youtube"))
        val afterUpdate = PluginActivationStack.withNewDefaults(afterFirstLoad, listOf("youtube", "generic"))
        assertEquals(listOf("youtube", "generic"), afterUpdate.order)
    }

    @Test
    fun seenDefaultDoesNotComeBackAfterDeactivation() {
        val loaded = PluginActivationStack.withNewDefaults(empty, listOf("youtube", "generic"))
        val deactivated = PluginActivationStack.deactivate(loaded, "generic")
        val reloaded = PluginActivationStack.withNewDefaults(deactivated, listOf("youtube", "generic"))
        assertEquals(listOf("youtube"), reloaded.order)
    }

    @Test
    fun activatePutsAtTheEnd() {
        val state = ActivationState(listOf("youtube"), setOf("youtube"), emptySet())
        val activated = PluginActivationStack.activate(state, "generic")
        assertEquals(listOf("youtube", "generic"), activated.order)
    }

    @Test
    fun deactivateRemovesFromOrder() {
        val state = ActivationState(listOf("youtube", "generic"), setOf("youtube", "generic"), emptySet())
        val deactivated = PluginActivationStack.deactivate(state, "youtube")
        assertEquals(listOf("generic"), deactivated.order)
    }

    @Test
    fun removeDefaultRemovesFromOrderAndMarksRemoved() {
        val state = ActivationState(listOf("youtube", "generic"), setOf("youtube", "generic"), emptySet())
        val removed = PluginActivationStack.removeDefault(state, "generic")
        assertEquals(listOf("youtube"), removed.order)
        assertTrue("generic" in removed.removedDefaults)
    }

    @Test
    fun removedDefaultDoesNotComeBackWithNewDefaults() {
        val state = ActivationState(listOf("youtube"), setOf("youtube", "generic"), setOf("generic"))
        val result = PluginActivationStack.withNewDefaults(state, listOf("youtube", "generic"))
        assertEquals(listOf("youtube"), result.order)
    }

    @Test
    fun restoreDefaultsBringsThemBackAtTheEndInGivenOrder() {
        val state = ActivationState(listOf("installed", "youtube"), setOf("youtube", "generic"), setOf("generic"))
        val restored = PluginActivationStack.restoreDefaults(state, listOf("youtube", "generic"))
        assertEquals(listOf("installed", "youtube", "generic"), restored.order)
        assertTrue(restored.removedDefaults.isEmpty())
    }

    @Test
    fun stackIgnoresIdWithoutManifest() {
        val state = ActivationState(listOf("youtube", "missing"), setOf("youtube"), emptySet())
        val youtube = LoadedManifest(PluginManifest("youtube", "YouTube", 1, emptyList()), PluginOrigin.DEFAULT)
        val stack = PluginActivationStack.stack(state, listOf(youtube))
        assertEquals(listOf("youtube"), stack.map { it.id })
    }

    @Test
    fun higherVersionWinsBetweenDefaultAndInstalled() {
        val state = ActivationState(listOf("youtube"), setOf("youtube"), emptySet())
        val default = LoadedManifest(PluginManifest("youtube", "YouTube", 1, emptyList()), PluginOrigin.DEFAULT)
        val installed = LoadedManifest(PluginManifest("youtube", "YouTube", 2, emptyList()), PluginOrigin.INSTALLED)
        val stack = PluginActivationStack.stack(state, listOf(default, installed))
        assertEquals(2, stack.single().version)
    }

    @Test
    fun installedWinsOnVersionTieAndKeepsIdPosition() {
        val state = ActivationState(listOf("generic", "youtube"), setOf("youtube", "generic"), emptySet())
        val default = LoadedManifest(PluginManifest("youtube", "YouTube", 1, emptyList()), PluginOrigin.DEFAULT)
        val installed = LoadedManifest(PluginManifest("youtube", "YouTube Installed", 1, emptyList()), PluginOrigin.INSTALLED)
        val generic = LoadedManifest(PluginManifest("generic", "Generic", 1, emptyList()), PluginOrigin.DEFAULT)
        val stack = PluginActivationStack.stack(state, listOf(default, installed, generic))
        assertEquals(listOf("generic", "youtube"), stack.map { it.id })
        assertEquals("YouTube Installed", stack[1].name)
    }

    @Test
    fun fallbacksAreMovedToTheEndOfTheOrder() {
        val state = ActivationState(order = listOf("generic", "youtube"), seenDefaults = setOf("generic", "youtube"), removedDefaults = emptySet())
        assertEquals(listOf("youtube", "generic"), PluginActivationStack.withFallbacksLast(state, listOf("generic")).order)
    }

    @Test
    fun fallbacksComeBackWhenMissingFromTheOrder() {
        val state = ActivationState(order = listOf("youtube"), seenDefaults = setOf("generic", "youtube"), removedDefaults = setOf("generic"))
        val result = PluginActivationStack.withFallbacksLast(state, listOf("generic"))
        assertEquals(listOf("youtube", "generic"), result.order)
        assertEquals(emptySet<String>(), result.removedDefaults)
    }
}
