package proto.media.fiesta.features.domain.phone.settings
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.annotation.StringRes
import androidx.annotation.XmlRes
import androidx.appcompat.app.AppCompatActivity
import proto.media.fiesta.R

class SettingsPhoneActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_phone_settings)

        val screenXmlRes = intent.getIntExtra(EXTRA_SCREEN_XML, R.xml.settings)
        val titleRes = intent.getIntExtra(EXTRA_TITLE_RES, R.string.settings)
        setTitle(titleRes)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            elevation = 0f
        }

        fragmentManager.beginTransaction()
            .replace(R.id.settings_container, SettingsPhoneFragment.newInstance(screenXmlRes))
            .commit()
    }

    override fun onStart() {
        super.onStart()
        overridePendingTransition(0, 0)
    }

    override fun finish() {
        super.finish()
        overridePendingTransition(0, 0)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean =
        if (item.itemId == android.R.id.home) {
            finish()
            true
        } else {
            super.onOptionsItemSelected(item)
        }

    companion object {
        private const val EXTRA_SCREEN_XML = "extra_screen_xml"
        private const val EXTRA_TITLE_RES = "extra_title_res"

        @JvmStatic
        fun newIntent(context: Context, @XmlRes screenXmlRes: Int, @StringRes titleRes: Int): Intent =
            Intent(context, SettingsPhoneActivity::class.java)
                .putExtra(EXTRA_SCREEN_XML, screenXmlRes)
                .putExtra(EXTRA_TITLE_RES, titleRes)
    }
}
