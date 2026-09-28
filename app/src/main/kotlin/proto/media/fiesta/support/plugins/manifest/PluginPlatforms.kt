package proto.media.fiesta.support.plugins.manifest

import proto.media.fiesta.support.plugins.model.PluginEnv
import proto.media.fiesta.support.plugins.model.PluginManifest

data class PluginPlatforms(val car: Boolean, val phone: Boolean) {

    companion object {
        fun of(manifest: PluginManifest): PluginPlatforms {
            val tweakPlatforms = manifest.rules.flatMap { rule -> rule.tweaks.map { of(it.env) } }
            val hasEvents = manifest.rules.any { it.events != null }
            return PluginPlatforms(
                car = hasEvents || tweakPlatforms.any { it.car },
                phone = tweakPlatforms.any { it.phone }
            )
        }

        fun of(env: PluginEnv): PluginPlatforms = PluginPlatforms(
            car = env == PluginEnv.CAR || env == PluginEnv.BOTH,
            phone = env == PluginEnv.PHONE || env == PluginEnv.BOTH
        )
    }
}
