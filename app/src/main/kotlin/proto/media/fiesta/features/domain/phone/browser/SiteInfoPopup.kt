package proto.media.fiesta.features.domain.phone.browser
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.content.res.AppCompatResources
import androidx.appcompat.widget.SwitchCompat
import com.google.android.material.bottomsheet.BottomSheetDialog
import proto.media.fiesta.R
import proto.media.fiesta.features.domain.core.settings.SettingsStorage
import proto.media.fiesta.support.webviewex.navigation.HostName
import kotlin.math.abs

object SiteInfoPopup {

    @JvmStatic
    fun show(context: Context, url: String?, favicon: Bitmap?, blockedCount: Int, onAdBlockChanged: Runnable?) {
        val dialog = BottomSheetDialog(context)
        val content = LayoutInflater.from(context).inflate(R.layout.popup_site_info, null)
        dialog.setContentView(content)

        val uri = if (url != null) Uri.parse(url) else null
        val host = uri?.host ?: url
        val secure = uri != null && "https".equals(uri.scheme, ignoreCase = true)
        val storageHost = HostName.of(url) ?: host

        content.findViewById<TextView>(R.id.site_info_host).text = host

        val faviconView = content.findViewById<ImageView>(R.id.site_info_favicon)
        if (favicon != null) {
            faviconView.setImageBitmap(favicon)
        } else {
            faviconView.setImageDrawable(letterTile(host))
        }

        val connectionText = content.findViewById<TextView>(R.id.site_info_connection)
        connectionText.setText(
            if (secure) R.string.browser_connection_secure else R.string.browser_connection_insecure
        )
        content.findViewById<ImageView>(R.id.site_info_connection_icon).setImageDrawable(
            AppCompatResources.getDrawable(
                context,
                if (secure) R.drawable.mozac_ic_shield else R.drawable.mozac_ic_shield_disabled
            )
        )

        content.findViewById<View>(R.id.site_info_connection_row).setOnClickListener { v ->
            AlertDialog.Builder(v.context, R.style.DialogStyle)
                .setTitle(connectionText.text)
                .setMessage(secureDetails(v.context, secure))
                .setPositiveButton(android.R.string.ok, null)
                .show()
        }

        val adBlockSwitch = content.findViewById<SwitchCompat>(R.id.site_info_adblock_switch)
        val adBlockSummary = content.findViewById<TextView>(R.id.site_info_adblock_summary)
        val adBlockIcon = content.findViewById<ImageView>(R.id.site_info_adblock_icon)
        val adBlockOn = SettingsStorage.isAdBlockEnabledForHost(context, storageHost)
        adBlockSwitch.isChecked = adBlockOn
        adBlockSummary.setText(if (adBlockOn) R.string.browser_adblock_on else R.string.browser_adblock_off)
        adBlockIcon.setImageDrawable(
            AppCompatResources.getDrawable(
                context,
                if (adBlockOn) R.drawable.mozac_ic_shield_checkmark_24 else R.drawable.mozac_ic_shield_cross_24
            )
        )
        adBlockSwitch.setOnCheckedChangeListener { buttonView, isChecked ->
            SettingsStorage.setAdBlockOverride(buttonView.context, storageHost, isChecked)
            adBlockSummary.setText(if (isChecked) R.string.browser_adblock_on else R.string.browser_adblock_off)
            adBlockIcon.setImageDrawable(
                AppCompatResources.getDrawable(
                    buttonView.context,
                    if (isChecked) R.drawable.mozac_ic_shield_checkmark_24 else R.drawable.mozac_ic_shield_cross_24
                )
            )
            onAdBlockChanged?.run()
        }

        val blockedCountView = content.findViewById<TextView>(R.id.site_info_blocked_count)
        val blockedLabelView = content.findViewById<TextView>(R.id.site_info_blocked_label)
        blockedCountView.text = blockedCount.toString()
        blockedLabelView.text = if (blockedCount == 0)
            context.getString(R.string.browser_blocked_count_label_zero)
        else
            context.resources.getQuantityString(R.plurals.browser_blocked_count_label, blockedCount)

        content.findViewById<View>(R.id.site_info_storage).setOnClickListener {
            dialog.dismiss()
            context.startActivity(Intent(context, SiteStorageActivity::class.java))
        }

        dialog.show()
    }

    private fun letterTile(host: String?): Drawable {
        val letter = if (!host.isNullOrEmpty()) host.substring(0, 1).uppercase() else "?"
        val palette = intArrayOf(
            0xFFE53935.toInt(), 0xFF8E24AA.toInt(), 0xFF3949AB.toInt(), 0xFF1E88E5.toInt(),
            0xFF00897B.toInt(), 0xFF43A047.toInt(), 0xFFF4511E.toInt(), 0xFF6D4C41.toInt(),
        )
        val color = palette[abs(host?.hashCode() ?: 0) % palette.size]

        val size = 96
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val background = Paint(Paint.ANTI_ALIAS_FLAG).apply { this.color = color }
        canvas.drawRect(0f, 0f, size.toFloat(), size.toFloat(), background)

        val text = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            this.color = Color.WHITE
            textSize = size * 0.55f
            textAlign = Paint.Align.CENTER
        }
        val metrics = text.fontMetrics
        val y = size / 2f - (metrics.ascent + metrics.descent) / 2f
        canvas.drawText(letter, size / 2f, y, text)

        return BitmapDrawable(null, bitmap)
    }

    private fun secureDetails(context: Context, secure: Boolean): String = context.getString(
        if (secure) R.string.browser_connection_secure_details else R.string.browser_connection_insecure_details
    )
}
