package proto.media.fiesta.support.webviewex.external

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.util.Log
import java.net.URISyntaxException

internal class ExternalSchemeHandler(
    private val context: Context,
    private val allowsLaunch: Boolean
) {

    sealed class Outcome {
        object Launched : Outcome()
        object Denied : Outcome()
        data class LoadFallback(val url: String) : Outcome()
        object NotInstalled : Outcome()
    }

    fun handle(url: String): Outcome {
        if (!allowsLaunch) return Outcome.Denied
        val intent = parse(url) ?: return Outcome.NotInstalled
        val fallbackUrl = intent.getStringExtra(FALLBACK_EXTRA)
        val declaredPackage = intent.`package`
        sanitize(intent)
        val resolution = ExternalSchemePolicy.resolve(resolvesToOtherApp(intent), fallbackUrl, declaredPackage)
        return when (resolution) {
            ExternalResolution.Launch -> if (start(intent)) Outcome.Launched else Outcome.NotInstalled
            is ExternalResolution.LoadFallback -> Outcome.LoadFallback(resolution.url)
            is ExternalResolution.OpenMarket -> if (openMarket(resolution.packageName)) Outcome.Launched else Outcome.NotInstalled
            ExternalResolution.NotInstalled -> Outcome.NotInstalled
        }
    }

    fun openInSystemBrowser(url: String): Boolean {
        if (!ExternalSchemePolicy.isWebUrl(url)) return false
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).addCategory(Intent.CATEGORY_BROWSABLE)
        sanitize(intent)
        return start(intent)
    }

    private fun parse(url: String): Intent? = try {
        when (ExternalSchemePolicy.classify(url)) {
            UrlKind.INTENT_URI -> Intent.parseUri(url, Intent.URI_INTENT_SCHEME)
            UrlKind.ANDROID_APP -> Intent.parseUri(url, Intent.URI_ANDROID_APP_SCHEME)
            UrlKind.WEB -> null
            else -> Intent(Intent.ACTION_VIEW, Uri.parse(url))
        }
    } catch (e: URISyntaxException) {
        Log.w(TAG, "Unparseable external link", e)
        null
    }

    private fun sanitize(intent: Intent) {
        intent.selector = null
        intent.component = null
        intent.flags = ExternalSchemePolicy.sanitizeFlags(intent.flags)
        intent.addCategory(Intent.CATEGORY_BROWSABLE)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }

    private fun resolvesToOtherApp(intent: Intent): Boolean {
        val info = context.packageManager.resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY) ?: return false
        return info.activityInfo?.packageName != context.packageName
    }

    private fun openMarket(packageName: String): Boolean {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$packageName"))
            .addCategory(Intent.CATEGORY_BROWSABLE)
        sanitize(intent)
        return resolvesToOtherApp(intent) && start(intent)
    }

    private fun start(intent: Intent): Boolean = try {
        context.startActivity(intent)
        true
    } catch (e: Exception) {
        Log.w(TAG, "Could not start external activity", e)
        false
    }

    private companion object {
        const val TAG = "WebViewExExternal"
        const val FALLBACK_EXTRA = "browser_fallback_url"
    }
}
