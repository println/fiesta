package proto.media.fiesta.support.plugins.manifest

import proto.media.fiesta.support.plugins.model.PageStage
import proto.media.fiesta.support.plugins.model.PluginEnv
import proto.media.fiesta.support.plugins.model.PluginManifest
import proto.media.fiesta.support.plugins.model.PluginRule
import proto.media.fiesta.support.plugins.model.Rerun
import proto.media.fiesta.support.plugins.model.TweakSpec
import org.junit.Assert.assertEquals
import org.junit.Test

class PluginPlatformsTest {

    private fun tweak(env: PluginEnv) = TweakSpec("a.tweak.js", PageStage.COMMITTED, Rerun.LOAD, null, env, null)

    private fun manifest(vararg rules: PluginRule) = PluginManifest("id", "Name", 1, rules.toList())

    @Test
    fun eventsRunOnlyInCar() {
        val platforms = PluginPlatforms.of(manifest(PluginRule(listOf("*"), "a.events.js", emptyList())))
        assertEquals(PluginPlatforms(car = true, phone = false), platforms)
    }

    @Test
    fun phoneTweakRunsOnlyOnPhone() {
        val platforms = PluginPlatforms.of(manifest(PluginRule(listOf("*"), null, listOf(tweak(PluginEnv.PHONE)))))
        assertEquals(PluginPlatforms(car = false, phone = true), platforms)
    }

    @Test
    fun bothTweakRunsOnBoth() {
        val platforms = PluginPlatforms.of(manifest(PluginRule(listOf("*"), null, listOf(tweak(PluginEnv.BOTH)))))
        assertEquals(PluginPlatforms(car = true, phone = true), platforms)
    }

    @Test
    fun rulesAreCombined() {
        val platforms = PluginPlatforms.of(
            manifest(
                PluginRule(listOf("*"), "a.events.js", emptyList()),
                PluginRule(listOf("*"), null, listOf(tweak(PluginEnv.PHONE)))
            )
        )
        assertEquals(PluginPlatforms(car = true, phone = true), platforms)
    }

    @Test
    fun tweakEnvMapsToItsPlatforms() {
        assertEquals(PluginPlatforms(car = true, phone = false), PluginPlatforms.of(PluginEnv.CAR))
        assertEquals(PluginPlatforms(car = false, phone = true), PluginPlatforms.of(PluginEnv.PHONE))
        assertEquals(PluginPlatforms(car = true, phone = true), PluginPlatforms.of(PluginEnv.BOTH))
    }

    @Test
    fun emptyManifestRunsNowhere() {
        assertEquals(PluginPlatforms(car = false, phone = false), PluginPlatforms.of(manifest()))
    }
}
