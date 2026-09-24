package proto.media.fiesta.features.domain.core.bookmarks
import android.content.Context
import proto.media.fiesta.features.domain.core.settings.SettingsUtils
import proto.media.fiesta.support.webviewex.WebViewEx
import io.realm.Realm
import io.realm.RealmResults

object BookmarkUtils {
    private const val BOOKMARK_VIEW_SIZE_IN_DP = 180f

    @JvmStatic
    fun addBookmark(browser: WebViewEx<*>, onSaved: () -> Unit = {}) {
        val pageTitle = browser.title
        val pageUrl = browser.url
        browser.capturePage(BOOKMARK_VIEW_SIZE_IN_DP) { thumbnail ->
            val bookmark = Bookmark()
            bookmark.title = pageTitle
            bookmark.url = pageUrl
            bookmark.thumbnail = thumbnail
            bookmark.createdAt = System.currentTimeMillis()
            val realm = Realm.getDefaultInstance()
            try {
                realm.executeTransaction { realm1 ->
                    val max = realm1.where(Bookmark::class.java).max("sortOrder")
                    bookmark.sortOrder = if (max == null) 0 else max.toInt() + 1
                    realm1.insert(bookmark)
                }
            } finally {
                realm.close()
            }
            onSaved()
        }
    }

    @JvmStatic
    fun seedDefaultFavoritesOnce(context: Context, realm: Realm) {
        if (SettingsUtils.areDefaultFavoritesSeeded(context)) {
            return
        }
        if (realm.where(Bookmark::class.java).count() == 0L) {
            realm.executeTransaction { realm1 ->
                addDefaultFavorite(realm1, "Google", "https://www.google.com", 0)
                addDefaultFavorite(realm1, "YouTube", "https://www.youtube.com", 1)
            }
        }
        SettingsUtils.markDefaultFavoritesSeeded(context)
    }

    private fun addDefaultFavorite(realm: Realm, title: String, url: String, sortOrder: Int) {
        val bookmark = Bookmark()
        bookmark.title = title
        bookmark.url = url
        bookmark.sortOrder = sortOrder
        bookmark.createdAt = System.currentTimeMillis()
        realm.insert(bookmark)
    }

    @JvmStatic
    fun getBookmarks(realm: Realm): RealmResults<Bookmark> {
        return realm.where(Bookmark::class.java).sort("sortOrder").findAll()
    }

    @JvmStatic
    fun findByUrl(realm: Realm, url: String?): Bookmark? {
        if (url.isNullOrEmpty()) {
            return null
        }
        return realm.where(Bookmark::class.java).findAll().firstOrNull { BookmarkUrlMatch.sameUrl(it.url, url) }
    }

    @JvmStatic
    fun removeByUrl(url: String?) {
        val realm = Realm.getDefaultInstance()
        try {
            realm.executeTransaction { transactionRealm ->
                findByUrl(transactionRealm, url)?.deleteFromRealm()
            }
        } finally {
            realm.close()
        }
    }
}
