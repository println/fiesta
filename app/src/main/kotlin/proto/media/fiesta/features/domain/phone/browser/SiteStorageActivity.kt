package proto.media.fiesta.features.domain.phone.browser
import android.app.AlertDialog
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.webkit.CookieManager
import android.webkit.WebStorage
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doAfterTextChanged
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import proto.media.fiesta.R
import proto.media.fiesta.features.domain.core.browser.CookieUtils
import java.net.URI
import java.net.URISyntaxException
import proto.media.fiesta.features.domain.core.browser.BrowserStorageUtils

class SiteStorageActivity : AppCompatActivity() {

    private lateinit var adapter: SiteStorageAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var emptyView: TextView
    private var originsByHost: Map<String, List<String>> = emptyMap()
    private var entries: List<SiteStorageAdapter.SiteEntry> = emptyList()
    private var query = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_site_storage)

        // The window decor already supplies the action bar; adding a Toolbar here throws.
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        recyclerView = findViewById(R.id.site_storage_list)
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = SiteStorageAdapter()
        adapter.setDeleteCallback { entry -> confirmDeleteSite(entry) }
        recyclerView.adapter = adapter

        emptyView = findViewById(R.id.site_storage_empty)

        findViewById<EditText>(R.id.site_storage_search).doAfterTextChanged {
            query = it?.toString().orEmpty().trim()
            showEntries()
        }
    }

    override fun onResume() {
        super.onResume()
        loadSites()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menu.add(Menu.NONE, MENU_DELETE_ALL, Menu.NONE, R.string.storage_delete_all)
            .setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                true
            }
            MENU_DELETE_ALL -> {
                confirmDeleteAll()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun loadSites() {
        val recordedHosts = BrowserStorageUtils.getRecordedHosts(this)
        WebStorage.getInstance().getOrigins { originsMap ->
            if (isFinishing) {
                return@getOrigins
            }
            val byHost = HashMap<String, MutableList<String>>()
            val hosts = recordedHosts.toMutableSet()
            if (originsMap != null) {
                for (key in originsMap.keys) {
                    val origin = key.toString()
                    val host = hostFromOrigin(origin)
                    if (host.isNullOrEmpty()) {
                        continue
                    }
                    hosts.add(host)
                    byHost.getOrPut(host) { mutableListOf() }.add(origin)
                }
            }
            originsByHost = byHost

            val entries = mutableListOf<SiteStorageAdapter.SiteEntry>()
            for (host in hosts) {
                val cookieCount = BrowserStorageUtils.countCookies(host)
                val hasWebStorage = originsByHost.containsKey(host)
                entries.add(SiteStorageAdapter.SiteEntry(host, cookieCount, hasWebStorage))
            }
            entries.sortWith(Comparator { a, b ->
                if (a.cookieCount != b.cookieCount) {
                    b.cookieCount - a.cookieCount
                } else {
                    a.host.compareTo(b.host, ignoreCase = true)
                }
            })

            this.entries = entries
            showEntries()
        }
    }

    private fun showEntries() {
        val matching = entries.filter { it.host.contains(query, ignoreCase = true) }
        adapter.setEntries(matching)
        val empty = matching.isEmpty()
        recyclerView.visibility = if (empty) View.GONE else View.VISIBLE
        emptyView.visibility = if (empty) View.VISIBLE else View.GONE
    }

    private fun hostFromOrigin(origin: String): String? {
        return try {
            val host = URI(origin).host ?: return null
            if (host.startsWith("www.")) host.substring(4) else host
        } catch (e: URISyntaxException) {
            null
        }
    }

    private fun confirmDeleteSite(entry: SiteStorageAdapter.SiteEntry) {
        AlertDialog.Builder(this)
            .setTitle(R.string.storage_delete_title)
            .setMessage(getString(R.string.storage_delete_message, entry.host))
            .setPositiveButton(R.string.storage_delete_confirm) { _, _ -> deleteSite(entry) }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }

    private fun deleteSite(entry: SiteStorageAdapter.SiteEntry) {
        originsByHost[entry.host]?.forEach { origin ->
            WebStorage.getInstance().deleteOrigin(origin)
        }
        BrowserStorageUtils.forgetHost(this, entry.host)
        BrowserStorageUtils.clearCookiesForHost(entry.host, Runnable {
            if (!isFinishing) {
                loadSites()
            }
        })
    }

    private fun confirmDeleteAll() {
        AlertDialog.Builder(this)
            .setTitle(R.string.storage_delete_title)
            .setMessage(R.string.storage_delete_all)
            .setPositiveButton(R.string.storage_delete_confirm) { _, _ -> deleteAll() }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }

    @Suppress("DEPRECATION")
    private fun deleteAll() {
        WebStorage.getInstance().deleteAllData()
        BrowserStorageUtils.forgetAllHosts(this)
        val cm = CookieManager.getInstance()
        cm.removeAllCookies {
            CookieUtils.flush()
            if (!isFinishing) {
                loadSites()
            }
        }
    }

    companion object {
        private const val MENU_DELETE_ALL = 1
    }
}
