package proto.media.fiesta.features.domain.car.settings

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

interface CarSettingsScreen {
    val titleRes: Int

    fun createView(inflater: LayoutInflater, parent: ViewGroup): View

    fun createHeaderActions(inflater: LayoutInflater, container: ViewGroup) {}

    fun onShown() {}

    fun onHidden() {}

    fun handleBack(): Boolean = false
}
