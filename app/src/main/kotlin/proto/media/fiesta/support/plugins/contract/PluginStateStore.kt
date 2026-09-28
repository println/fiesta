package proto.media.fiesta.support.plugins.contract

import proto.media.fiesta.support.plugins.model.ActivationState

interface PluginStateStore {

    fun read(): ActivationState

    fun write(state: ActivationState)
}
