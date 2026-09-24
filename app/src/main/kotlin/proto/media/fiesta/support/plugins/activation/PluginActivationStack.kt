package proto.media.fiesta.support.plugins.activation

import proto.media.fiesta.support.plugins.model.ActivationState
import proto.media.fiesta.support.plugins.model.LoadedManifest
import proto.media.fiesta.support.plugins.model.PluginManifest
import proto.media.fiesta.support.plugins.model.PluginOrigin

object PluginActivationStack {

    fun withNewDefaults(state: ActivationState, defaultsInFirstContactOrder: List<String>): ActivationState {
        val newlySeen = defaultsInFirstContactOrder.filter { it !in state.seenDefaults }
        if (newlySeen.isEmpty()) return state
        val toActivate = newlySeen.filter { it !in state.removedDefaults }
        return state.copy(
            order = state.order + toActivate,
            seenDefaults = state.seenDefaults + newlySeen
        )
    }

    fun withFallbacksLast(state: ActivationState, fallbackIds: List<String>): ActivationState =
        state.copy(
            order = state.order.filterNot { it in fallbackIds } + fallbackIds,
            seenDefaults = state.seenDefaults + fallbackIds,
            removedDefaults = state.removedDefaults - fallbackIds.toSet()
        )

    fun activate(state: ActivationState, id: String): ActivationState =
        state.copy(order = state.order.filterNot { it == id } + id)

    fun deactivate(state: ActivationState, id: String): ActivationState =
        state.copy(order = state.order.filterNot { it == id })

    fun removeDefault(state: ActivationState, id: String): ActivationState =
        state.copy(
            order = state.order.filterNot { it == id },
            removedDefaults = state.removedDefaults + id
        )

    fun restoreDefaults(state: ActivationState, defaultsInFirstContactOrder: List<String>): ActivationState =
        state.copy(
            order = state.order.filterNot { it in defaultsInFirstContactOrder } + defaultsInFirstContactOrder,
            seenDefaults = defaultsInFirstContactOrder.toSet(),
            removedDefaults = emptySet()
        )

    fun stack(state: ActivationState, available: List<LoadedManifest>): List<PluginManifest> {
        val byId = available.groupBy { it.manifest.id }
        return state.order.mapNotNull { id ->
            val candidates = byId[id] ?: return@mapNotNull null
            candidates.reduce { best, candidate ->
                when {
                    candidate.manifest.version > best.manifest.version -> candidate
                    candidate.manifest.version < best.manifest.version -> best
                    candidate.origin == PluginOrigin.INSTALLED -> candidate
                    else -> best
                }
            }.manifest
        }
    }
}
