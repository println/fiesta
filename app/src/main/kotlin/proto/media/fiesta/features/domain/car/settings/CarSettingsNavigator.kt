package proto.media.fiesta.features.domain.car.settings

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import proto.media.fiesta.R

class CarSettingsNavigator(
    private val panelView: View,
    private val panelOutsideView: View,
    private val screenView: View,
    private val screenBody: FrameLayout,
    private val screenTitle: TextView,
    private val screenHeaderActions: ViewGroup,
    private val inflater: LayoutInflater
) {
    private var currentScreen: CarSettingsScreen? = null

    init {
        panelOutsideView.setOnClickListener { closeAll() }
    }

    fun isPanelOpen(): Boolean = panelView.visibility == View.VISIBLE

    fun isScreenOpen(): Boolean = screenView.visibility == View.VISIBLE

    fun isAnyOpen(): Boolean = isPanelOpen() || isScreenOpen()

    fun togglePanel() {
        if (isPanelOpen()) closeAll() else openPanel()
    }

    fun openPanel() {
        panelView.findViewById<View>(R.id.settings_panel_scroll).scrollTo(0, 0)
        screenView.visibility = View.GONE
        panelView.visibility = View.VISIBLE
        panelOutsideView.visibility = View.VISIBLE
    }

    fun open(screen: CarSettingsScreen) {
        closeCurrentScreen()
        screenBody.addView(screen.createView(inflater, screenBody))
        screenTitle.setText(screen.titleRes)
        screen.createHeaderActions(inflater, screenHeaderActions)
        currentScreen = screen
        panelView.visibility = View.GONE
        screenView.visibility = View.VISIBLE
        panelOutsideView.visibility = View.GONE
        screen.onShown()
    }

    fun setTitle(titleRes: Int) {
        screenTitle.setText(titleRes)
    }

    fun back(): Boolean {
        if (!isScreenOpen()) {
            return false
        }
        if (currentScreen?.handleBack() == true) {
            return true
        }
        closeCurrentScreen()
        screenView.visibility = View.GONE
        panelView.visibility = View.VISIBLE
        panelOutsideView.visibility = View.VISIBLE
        return true
    }

    fun closeAll() {
        closeCurrentScreen()
        screenView.visibility = View.GONE
        panelView.visibility = View.GONE
        panelOutsideView.visibility = View.GONE
    }

    private fun closeCurrentScreen() {
        currentScreen?.onHidden()
        currentScreen = null
        screenBody.removeAllViews()
        screenHeaderActions.removeAllViews()
    }
}
