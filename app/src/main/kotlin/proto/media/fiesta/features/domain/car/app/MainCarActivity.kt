package proto.media.fiesta.features.domain.car.app

import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.view.Window
import com.google.android.apps.auto.sdk.CarActivity
import proto.media.fiesta.R
import proto.media.fiesta.features.domain.car.browser.WebViewCarFragment

class MainCarActivity : CarActivity() {
    private var currentFragmentTag: String? = null
    private val fragmentLifecycleCallbacks = object : FragmentManager.FragmentLifecycleCallbacks() {
        override fun onFragmentStarted(fm: FragmentManager, f: Fragment) {
            updateStatusBarTitle()
        }
    }
    private val activityCallbacks = mutableSetOf<ActivityCallbacks>()

    override fun onWindowFocusChanged(hasFocus: Boolean, b1: Boolean) {
        super.onWindowFocusChanged(hasFocus, b1)
        for (next in activityCallbacks) {
            next.onWindowFocusChanged(hasFocus)
        }
    }

    override fun onCreate(bundle: Bundle?) {
        super.onCreate(bundle)
        setContentView(R.layout.activity_car_main)

        val carUiController = carUiController
        carUiController.statusBarController.hideAppHeader()
        carUiController.statusBarController.hideConnectivityLevel()

        val fragmentManager = supportFragmentManager
        val webViewCarFragment = WebViewCarFragment()

        fragmentManager.beginTransaction()
            .add(R.id.fragment_container, webViewCarFragment, FRAGMENT_MAIN)
            .detach(webViewCarFragment)
            .commitNow()

        var initialFragmentTag: String? = FRAGMENT_MAIN
        if (bundle != null && bundle.containsKey(CURRENT_FRAGMENT_KEY)) {
            initialFragmentTag = bundle.getString(CURRENT_FRAGMENT_KEY)
        }
        switchToFragment(initialFragmentTag)
        supportFragmentManager.registerFragmentLifecycleCallbacks(fragmentLifecycleCallbacks, false)
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
    }

    override fun onSaveInstanceState(bundle: Bundle) {
        bundle.putString(CURRENT_FRAGMENT_KEY, currentFragmentTag)
        super.onSaveInstanceState(bundle)
    }

    override fun onStart() {
        super.onStart()
        switchToFragment(currentFragmentTag)
    }

    private fun switchToFragment(tag: String?) {
        if (tag!! == currentFragmentTag) {
            return
        }
        val manager = supportFragmentManager
        val currentFragment = if (currentFragmentTag == null) null else manager.findFragmentByTag(currentFragmentTag)
        val newFragment = manager.findFragmentByTag(tag)
        val transaction = supportFragmentManager.beginTransaction()
        if (currentFragment != null) {
            transaction.detach(currentFragment)
        }
        transaction.attach(newFragment)
        transaction.commit()
        currentFragmentTag = tag
    }

    private fun updateStatusBarTitle() {
        val fragment = supportFragmentManager.findFragmentByTag(currentFragmentTag) as CarFragment
        carUiController.statusBarController.setTitle(fragment.title)
    }

    fun getWindow(): Window {
        return super.c()
    }

    override fun onConfigurationChanged(configuration: Configuration) {
        super.onConfigurationChanged(configuration)
        for (next in activityCallbacks) {
            next.onConfigChanged()
        }
    }

    fun addActivityCallback(listener: ActivityCallbacks) {
        activityCallbacks.add(listener)
    }

    fun removeActivityCallback(listener: ActivityCallbacks) {
        activityCallbacks.remove(listener)
    }

    interface ActivityCallbacks {
        fun onConfigChanged()
        fun onWindowFocusChanged(hasFocus: Boolean)
    }

    companion object {
        private const val FRAGMENT_MAIN = "main"
        private const val CURRENT_FRAGMENT_KEY = "app_current_fragment"
    }
}
