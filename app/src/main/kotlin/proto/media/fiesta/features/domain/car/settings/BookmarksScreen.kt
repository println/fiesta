package proto.media.fiesta.features.domain.car.settings

import android.content.Context
import android.graphics.BitmapFactory
import android.graphics.Color
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.GridLayout
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import proto.media.fiesta.R
import proto.media.fiesta.features.domain.core.bookmarks.AlphabetIndex
import proto.media.fiesta.features.domain.core.bookmarks.Bookmark
import proto.media.fiesta.features.domain.core.bookmarks.BookmarkUrlMatch
import proto.media.fiesta.features.domain.core.bookmarks.BookmarkUtils
import proto.media.fiesta.features.domain.core.bookmarks.PendingRemovals
import proto.media.fiesta.features.domain.core.settings.SettingsUtils
import io.realm.OrderedRealmCollectionChangeListener
import io.realm.Realm
import io.realm.RealmResults

class BookmarksScreen(
    private val context: Context,
    private val currentUrl: () -> String?,
    private val onAddCurrentSite: () -> Unit,
    private val onBookmarkSelected: (url: String) -> Unit
) : CarSettingsScreen {

    override val titleRes: Int = R.string.bookmarks

    private val pendingRemovals = PendingRemovals()
    private val adapter = BookmarksAdapter()

    private var realm: Realm? = null
    private var bookmarks: RealmResults<Bookmark>? = null
    private var itemKeysInOrder: List<Char> = emptyList()

    private lateinit var currentSiteRow: TextView
    private lateinit var recyclerView: RecyclerView
    private lateinit var jumpGrid: GridLayout

    override fun createView(inflater: LayoutInflater, parent: ViewGroup): View {
        val view = inflater.inflate(R.layout.car_settings_bookmarks, parent, false)

        currentSiteRow = view.findViewById(R.id.bookmarks_add_current_site)
        currentSiteRow.setOnClickListener { toggleCurrentSite() }
        view.findViewById<View>(R.id.bookmarks_jump_button).setOnClickListener { showJumpGrid() }

        recyclerView = view.findViewById(R.id.bookmarks_recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = adapter

        jumpGrid = view.findViewById(R.id.bookmarks_jump_grid)
        buildJumpGrid()

        val realm = Realm.getDefaultInstance()
        this.realm = realm
        BookmarkUtils.seedDefaultFavoritesOnce(context, realm)
        val bookmarks = BookmarkUtils.getBookmarks(realm)
        this.bookmarks = bookmarks
        bookmarks.addChangeListener(OrderedRealmCollectionChangeListener<RealmResults<Bookmark>> { _, _ -> rebuildList() })
        rebuildList()

        return view
    }

    override fun onHidden() {
        commitPendingRemovals()
        bookmarks?.removeAllChangeListeners()
        bookmarks = null
        realm?.close()
        realm = null
    }

    override fun handleBack(): Boolean {
        if (jumpGrid.visibility != View.VISIBLE) {
            return false
        }
        jumpGrid.visibility = View.GONE
        return true
    }

    private fun commitPendingRemovals() {
        val idsToRemove = pendingRemovals.toCommit()
        if (idsToRemove.isEmpty()) {
            return
        }
        val realm = Realm.getDefaultInstance()
        try {
            realm.executeTransaction { transactionRealm ->
                BookmarkUtils.getBookmarks(transactionRealm)
                    .filter { idOf(it) in idsToRemove }
                    .forEach { it.deleteFromRealm() }
            }
        } finally {
            realm.close()
        }
        pendingRemovals.clear()
        val message = if (idsToRemove.size == 1) {
            context.getString(R.string.car_settings_bookmark_removed_one)
        } else {
            context.getString(R.string.car_settings_bookmarks_removed_many, idsToRemove.size)
        }
        showCarToast(recyclerView, message)
    }

    private fun rebuildList() {
        val all = bookmarks ?: return
        val sorted = all.sortedWith { a, b -> AlphabetIndex.compare(a.title, a.url, b.title, b.url) }

        val items = mutableListOf<ListItem>()
        val itemKeys = mutableListOf<Char>()
        var lastKey: Char? = null
        for (bookmark in sorted) {
            val key = AlphabetIndex.sectionKeyOf(bookmark.title, bookmark.url)
            if (key != lastKey) {
                items.add(ListItem.Header(key))
                itemKeys.add(key)
                lastKey = key
            }
            items.add(ListItem.Row(bookmark))
            itemKeys.add(key)
        }
        itemKeysInOrder = itemKeys
        adapter.submit(items)
        updateJumpGridAvailability()
        refreshCurrentSiteRow()
    }

    private fun findCurrentSiteBookmark(): Bookmark? =
        bookmarks?.firstOrNull { BookmarkUrlMatch.sameUrl(it.url, currentUrl()) }

    private fun refreshCurrentSiteRow() {
        val isBookmarked = findCurrentSiteBookmark() != null
        currentSiteRow.setText(if (isBookmarked) R.string.remove_bookmark else R.string.add_bookmark)
        val icon = if (isBookmarked) R.drawable.ic_car_favorite_filled else R.drawable.ic_car_favorite
        currentSiteRow.setCompoundDrawablesWithIntrinsicBounds(icon, 0, 0, 0)
    }

    private fun toggleCurrentSite() {
        val existing = findCurrentSiteBookmark()
        when {
            existing == null -> {
                onAddCurrentSite()
                showCarToast(currentSiteRow, context.getString(R.string.car_settings_bookmark_added))
            }
            existing.isPreventDelete ->
                showCarToast(currentSiteRow, context.getString(R.string.car_settings_bookmark_locked))
            else -> {
                BookmarkUtils.removeByUrl(existing.url)
                showCarToast(currentSiteRow, context.getString(R.string.car_settings_bookmark_removed))
            }
        }
    }

    private fun buildJumpGrid() {
        jumpGrid.removeAllViews()
        jumpGrid.rowCount = (JUMP_LETTERS.size + jumpGrid.columnCount - 1) / jumpGrid.columnCount
        for (letter in JUMP_LETTERS) {
            val key = TextView(context)
            key.text = letter.toString()
            key.tag = letter
            key.gravity = Gravity.CENTER
            key.textSize = JUMP_KEY_TEXT_SIZE_SP
            key.setTextColor(Color.WHITE)
            key.setBackgroundResource(R.drawable.car_settings_tonal_background)
            key.layoutParams = GridLayout.LayoutParams(
                GridLayout.spec(GridLayout.UNDEFINED, 1f),
                GridLayout.spec(GridLayout.UNDEFINED, 1f)
            ).apply {
                width = 0
                height = 0
                val margin = context.resources.getDimensionPixelSize(R.dimen.car_settings_jump_key_margin)
                setMargins(margin, margin, margin, margin)
            }
            key.setOnClickListener { jumpTo(letter) }
            jumpGrid.addView(key)
        }
    }

    private fun updateJumpGridAvailability() {
        val available = AlphabetIndex.availableLetters(itemKeysInOrder)
        for (i in 0 until jumpGrid.childCount) {
            val key = jumpGrid.getChildAt(i)
            val hasBookmarks = (key.tag as Char) in available
            key.isEnabled = hasBookmarks
            key.alpha = if (hasBookmarks) 1f else DISABLED_KEY_ALPHA
        }
    }

    private fun showJumpGrid() {
        jumpGrid.visibility = View.VISIBLE
    }

    private fun jumpTo(letter: Char) {
        jumpGrid.visibility = View.GONE
        val position = AlphabetIndex.positionOf(letter, itemKeysInOrder) ?: return
        (recyclerView.layoutManager as LinearLayoutManager).scrollToPositionWithOffset(position, 0)
    }

    private fun idOf(bookmark: Bookmark): String = bookmark.url ?: bookmark.title.orEmpty()

    private sealed class ListItem {
        data class Header(val letter: Char) : ListItem()
        data class Row(val bookmark: Bookmark) : ListItem()
    }

    private inner class BookmarksAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
        private var items: List<ListItem> = emptyList()

        fun submit(newItems: List<ListItem>) {
            items = newItems
            notifyDataSetChanged()
        }

        override fun getItemViewType(position: Int): Int = when (items[position]) {
            is ListItem.Header -> VIEW_TYPE_HEADER
            is ListItem.Row -> VIEW_TYPE_ROW
        }

        override fun getItemCount(): Int = items.size

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            val inflater = LayoutInflater.from(parent.context)
            return if (viewType == VIEW_TYPE_HEADER) {
                HeaderViewHolder(inflater.inflate(R.layout.car_settings_bookmark_header_row, parent, false))
            } else {
                RowViewHolder(inflater.inflate(R.layout.car_settings_bookmark_row, parent, false))
            }
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            when (val item = items[position]) {
                is ListItem.Header -> (holder as HeaderViewHolder).bind(item.letter)
                is ListItem.Row -> (holder as RowViewHolder).bind(item.bookmark)
            }
        }

        private inner class HeaderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            private val letterView: TextView = itemView.findViewById(R.id.bookmark_header_letter)

            fun bind(letter: Char) {
                letterView.text = letter.toString()
                letterView.setOnClickListener { showJumpGrid() }
            }
        }

        private inner class RowViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            private val title: TextView = itemView.findViewById(R.id.bookmark_row_title)
            private val host: TextView = itemView.findViewById(R.id.bookmark_row_host)
            private val thumbnail: ImageView = itemView.findViewById(R.id.bookmark_row_thumbnail)
            private val heart: ImageButton = itemView.findViewById(R.id.bookmark_row_heart)
            private val home: ImageButton = itemView.findViewById(R.id.bookmark_row_home)

            fun bind(bookmark: Bookmark) {
                val url = bookmark.url
                home.isActivated = url != null && BookmarkUrlMatch.sameUrl(url, SettingsUtils.getHomeUrl(context))
                home.imageAlpha = if (home.isActivated) ACTIVE_ICON_ALPHA else INACTIVE_ICON_ALPHA
                home.setOnClickListener {
                    if (url == null || home.isActivated) {
                        return@setOnClickListener
                    }
                    SettingsUtils.setHomeUrl(context, url)
                    notifyDataSetChanged()
                    showCarToast(home, context.getString(R.string.car_settings_home_set, bookmark.title ?: url))
                }

                title.text = bookmark.title ?: bookmark.url
                host.text = bookmark.url
                val thumbnailBytes = bookmark.thumbnail
                if (thumbnailBytes != null) {
                    thumbnail.setImageBitmap(BitmapFactory.decodeByteArray(thumbnailBytes, 0, thumbnailBytes.size))
                } else if (bookmark.thumbnailResource != 0) {
                    thumbnail.setImageResource(bookmark.thumbnailResource)
                } else {
                    thumbnail.setImageDrawable(null)
                }

                val id = idOf(bookmark)
                val pendingRemoval = pendingRemovals.isPending(id)
                heart.setImageResource(
                    if (bookmark.isPreventDelete || !pendingRemoval) {
                        R.drawable.ic_car_favorite_filled
                    } else {
                        R.drawable.ic_car_favorite
                    }
                )
                heart.setOnClickListener {
                    if (bookmark.isPreventDelete) {
                        showCarToast(heart, context.getString(R.string.car_settings_bookmark_locked))
                        return@setOnClickListener
                    }
                    pendingRemovals.toggle(id)
                    notifyItemChanged(adapterPosition)
                }
                itemView.setOnClickListener {
                    bookmark.url?.let(onBookmarkSelected)
                }
            }
        }
    }

    companion object {
        private const val VIEW_TYPE_HEADER = 0
        private const val VIEW_TYPE_ROW = 1
        private const val JUMP_KEY_TEXT_SIZE_SP = 24f
        private const val ACTIVE_ICON_ALPHA = 255
        private const val INACTIVE_ICON_ALPHA = 90
        private const val DISABLED_KEY_ALPHA = 0.3f
        private val JUMP_LETTERS = listOf(AlphabetIndex.OTHER_SECTION) + ('A'..'Z')
    }
}
