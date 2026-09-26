package proto.media.fiesta.features.domain.phone.update

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.widget.CheckBox
import android.widget.FrameLayout
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.json.JSONException
import org.json.JSONObject
import proto.media.fiesta.BuildConfig
import proto.media.fiesta.R
import proto.media.fiesta.features.domain.core.settings.SettingsUtils
import proto.media.fiesta.features.domain.core.update.AppVersion
import proto.media.fiesta.features.domain.core.update.UpdateCheck
import java.io.IOException

class UpdateNotice(private val activity: Activity) {

    private class LatestRelease(val tag: String, val apkUrl: String)

    fun checkInBackground() {
        if (!SettingsUtils.isUpdateNoticeEnabled(activity)) return
        val request = Request.Builder()
            .url(LATEST_RELEASE_URL)
            .header("Accept", "application/vnd.github+json")
            .build()
        OkHttpClient().newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.w(TAG, "update check failed", e)
            }

            override fun onResponse(call: Call, response: Response) {
                val release = response.use { if (it.isSuccessful) parse(it.body?.string()) else null } ?: return
                if (!UpdateCheck.shouldNotify(BuildConfig.VERSION_NAME, release.tag)) return
                activity.runOnUiThread { show(release) }
            }
        })
    }

    private fun parse(json: String?): LatestRelease? = try {
        val release = JSONObject(json.orEmpty())
        findApkUrl(release)?.let { LatestRelease(release.getString("tag_name"), it) }
    } catch (e: JSONException) {
        Log.w(TAG, "unreadable release", e)
        null
    }

    private fun findApkUrl(release: JSONObject): String? {
        val assets = release.getJSONArray("assets")
        for (i in 0 until assets.length()) {
            val url = assets.getJSONObject(i).getString("browser_download_url")
            if (url.startsWith(DOWNLOAD_PREFIX) && url.endsWith(".apk")) return url
        }
        return null
    }

    private fun show(release: LatestRelease) {
        if (activity.isFinishing || activity.isDestroyed) return
        val dontShowAgain = CheckBox(activity).apply { setText(R.string.update_dont_show_again) }
        AlertDialog.Builder(activity)
            .setTitle(R.string.update_available_title)
            .setMessage(activity.getString(R.string.update_available_message, AppVersion.parse(release.tag).toString()))
            .setView(padded(dontShowAgain))
            .setPositiveButton(R.string.update_available_download) { _, _ -> download(release.apkUrl) }
            .setNegativeButton(android.R.string.cancel, null)
            .setOnDismissListener { if (dontShowAgain.isChecked) SettingsUtils.disableUpdateNotice(activity) }
            .show()
    }

    private fun padded(checkBox: CheckBox): FrameLayout {
        val horizontal = (DIALOG_PADDING_DP * activity.resources.displayMetrics.density).toInt()
        return FrameLayout(activity).apply {
            setPadding(horizontal, 0, horizontal, 0)
            addView(checkBox)
        }
    }

    private fun download(url: String) {
        try {
            activity.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(activity, R.string.update_no_browser, Toast.LENGTH_SHORT).show()
        }
    }

    companion object {
        private const val TAG = "UpdateNotice"
        private const val LATEST_RELEASE_URL = "https://api.github.com/repos/println/fiesta/releases/latest"
        private const val DOWNLOAD_PREFIX = "https://github.com/println/fiesta/releases/download/"
        private const val DIALOG_PADDING_DP = 20
    }
}
