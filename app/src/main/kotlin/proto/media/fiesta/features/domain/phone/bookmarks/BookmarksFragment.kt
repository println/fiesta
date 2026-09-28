package proto.media.fiesta.features.domain.phone.bookmarks

import android.content.Context
import android.graphics.BitmapFactory
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.os.Bundle
import android.text.InputType
import android.util.TypedValue
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.PopupMenu
import androidx.appcompat.widget.Toolbar
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import proto.media.fiesta.R
import proto.media.fiesta.features.domain.core.bookmarks.Bookmark
import proto.media.fiesta.features.domain.core.bookmarks.BookmarkEditSession
import proto.media.fiesta.features.domain.core.bookmarks.BookmarkEntry
import proto.media.fiesta.features.domain.core.bookmarks.BookmarkMonogram
import proto.media.fiesta.features.domain.core.bookmarks.BookmarkSearch
import proto.media.fiesta.features.domain.core.bookmarks.BookmarkUtils
import proto.media.fiesta.features.domain.core.bookmarks.BookmarksClickCallback
import io.realm.Realm

class BookmarksFragment : Fragment() {

    private lateinit var listener: BookmarksClickCallback
    private lateinit var emptyView: TextView
    private lateinit var session: BookmarkEditSession
    private val adapter = BookmarksAdapter()
    private var realm: Realm? = null
    private var storedBookmarks: List<Bookmark> = emptyList()
    private var query = ""

    override fun onAttach(context: Context) {
        super.onAttach(context)
        listener = parentFragment as? BookmarksClickCallback
            ?: context as? BookmarksClickCallback
            ?: throw IllegalStateException("$context should implement ${BookmarksClickCallback::class.java.simpleName}")
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View =
        inflater.inflate(R.layout.bookmarks_fragment, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.findViewById<Toolbar>(R.id.bookmarks_toolbar).setNavigationOnClickListener { listener.onBookmarkFragmentClose() }
        emptyView = view.findViewById(R.id.bookmarks_empty)

        val list = view.findViewById<RecyclerView>(R.id.bookmarks_list)
        list.layoutManager = LinearLayoutManager(context)
        list.adapter = adapter

        view.findViewById<EditText>(R.id.bookmarks_search).doAfterTextChanged {
            query = it?.toString().orEmpty()
            refresh()
        }
        view.isFocusableInTouchMode = true
        view.requestFocus()
        view.setOnKeyListener { _, keyCode, event ->
            val isBackRelease = event.action == KeyEvent.ACTION_UP && keyCode == KeyEvent.KEYCODE_BACK
            if (isBackRelease) listener.onBookmarkFragmentClose()
            isBackRelease
        }

        val realm = Realm.getDefaultInstance()
        this.realm = realm
        BookmarkUtils.seedDefaultFavoritesOnce(requireContext(), realm)
        storedBookmarks = BookmarkUtils.getBookmarks(realm).toList()
        session = BookmarkEditSession(
            storedBookmarks.mapIndexed { index, bookmark ->
                BookmarkEntry(index, bookmark.title, bookmark.url, bookmark.isPreventDelete)
            }
        )
        refresh()
    }

    override fun onDestroyView() {
        commitChanges()
        realm?.close()
        realm = null
        super.onDestroyView()
    }

    private fun refresh() {
        val visible = session.entries().filter { BookmarkSearch.matches(it.title, it.url, query) }
        adapter.submit(visible)
        emptyView.visibility = if (visible.isEmpty()) View.VISIBLE else View.GONE
    }

    private fun commitChanges() {
        if (!session.hasChanges()) return
        val removedKeys = session.removedKeys()
        realm?.executeTransaction {
            removedKeys.forEach { storedBookmarks[it].deleteFromRealm() }
            session.entries().forEachIndexed { position, entry ->
                storedBookmarks[entry.key].apply {
                    title = entry.title
                    url = entry.url
                    sortOrder = position
                }
            }
        }
        if (removedKeys.isNotEmpty()) {
            val message = if (removedKeys.size == 1) {
                getString(R.string.car_settings_bookmark_removed_one)
            } else {
                getString(R.string.car_settings_bookmarks_removed_many, removedKeys.size)
            }
            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
        }
    }

    private fun showOptions(anchor: View, entry: BookmarkEntry) {
        val popup = PopupMenu(requireContext(), anchor)
        popup.menu.add(0, MENU_EDIT, 0, R.string.bookmark_edit)
        if (session.canMoveUp(entry.key)) popup.menu.add(0, MENU_MOVE_UP, 1, R.string.bookmark_move_up)
        if (session.canMoveDown(entry.key)) popup.menu.add(0, MENU_MOVE_DOWN, 2, R.string.bookmark_move_down)
        popup.menu.add(0, MENU_DELETE, 3, R.string.bookmark_delete)
        popup.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                MENU_EDIT -> showEditDialog(entry)
                MENU_MOVE_UP -> session.moveUp(entry.key)
                MENU_MOVE_DOWN -> session.moveDown(entry.key)
                MENU_DELETE -> delete(entry)
            }
            refresh()
            true
        }
        popup.show()
    }

    private fun delete(entry: BookmarkEntry) {
        if (!session.remove(entry.key)) {
            Toast.makeText(requireContext(), R.string.car_settings_bookmark_locked, Toast.LENGTH_SHORT).show()
        }
    }

    private fun showEditDialog(entry: BookmarkEntry) {
        val context = requireContext()
        val nameField = EditText(context).apply {
            hint = getString(R.string.bookmark_edit_name)
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_CAP_SENTENCES
            setText(entry.title)
        }
        val urlField = EditText(context).apply {
            hint = getString(R.string.bookmark_edit_url)
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_URI
            setText(entry.url)
        }
        val form = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            val pad = dp(20)
            setPadding(pad, dp(8), pad, 0)
            addView(nameField)
            addView(urlField)
        }
        AlertDialog.Builder(context)
            .setTitle(R.string.bookmark_edit_title)
            .setView(form)
            .setPositiveButton(android.R.string.ok) { _, _ ->
                val url = urlField.text.toString().trim()
                if (url.isEmpty()) return@setPositiveButton
                session.edit(entry.key, nameField.text.toString().trim(), url)
                refresh()
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }

    private fun open(entry: BookmarkEntry) {
        val url = entry.url ?: return
        listener.onBookmarkSelected(Bookmark().apply {
            title = entry.title
            this.url = url
        })
        listener.onBookmarkFragmentClose()
    }

    private fun dp(value: Int): Int =
        TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, value.toFloat(), resources.displayMetrics).toInt()

    private inner class BookmarksAdapter : RecyclerView.Adapter<BookmarkViewHolder>() {

        private var items: List<BookmarkEntry> = emptyList()

        fun submit(newItems: List<BookmarkEntry>) {
            items = newItems
            notifyDataSetChanged()
        }

        override fun getItemCount(): Int = items.size

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookmarkViewHolder =
            BookmarkViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_bookmark_phone, parent, false))

        override fun onBindViewHolder(holder: BookmarkViewHolder, position: Int) {
            holder.bind(items[position])
        }
    }

    private inner class BookmarkViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val monogram: TextView = itemView.findViewById(R.id.bookmark_monogram)
        private val thumbnail: ImageView = itemView.findViewById(R.id.bookmark_thumbnail)
        private val title: TextView = itemView.findViewById(R.id.bookmark_title)
        private val host: TextView = itemView.findViewById(R.id.bookmark_host)
        private val menu: View = itemView.findViewById(R.id.bookmark_menu)

        init {
            itemView.findViewById<View>(R.id.bookmark_tile).clipToOutline = true
        }

        fun bind(entry: BookmarkEntry) {
            title.text = entry.title?.takeIf { it.isNotBlank() } ?: entry.url
            host.text = entry.url?.let { Uri.parse(it).host } ?: entry.url
            bindTile(entry)
            menu.setOnClickListener { showOptions(menu, entry) }
            itemView.setOnClickListener { open(entry) }
        }

        private fun bindTile(entry: BookmarkEntry) {
            val stored = storedBookmarks[entry.key]
            val thumbnailBytes = stored.thumbnail
            when {
                thumbnailBytes != null -> showThumbnail { setImageBitmap(BitmapFactory.decodeByteArray(thumbnailBytes, 0, thumbnailBytes.size)) }
                stored.thumbnailResource != 0 -> showThumbnail { setImageResource(stored.thumbnailResource) }
                else -> showMonogram(entry)
            }
        }

        private fun showThumbnail(load: ImageView.() -> Unit) {
            thumbnail.visibility = View.VISIBLE
            thumbnail.load()
            monogram.visibility = View.GONE
        }

        private fun showMonogram(entry: BookmarkEntry) {
            thumbnail.visibility = View.GONE
            thumbnail.setImageDrawable(null)
            val initials = BookmarkMonogram.of(entry.title, entry.url)
            val palette = resources.getIntArray(R.array.car_favorite_monogram_palette)
            monogram.visibility = View.VISIBLE
            monogram.text = initials
            monogram.background = GradientDrawable().apply {
                setColor(palette[BookmarkMonogram.paletteIndex(initials, palette.size)])
                cornerRadius = dp(10).toFloat()
            }
        }
    }

    companion object {
        private const val MENU_EDIT = 1
        private const val MENU_MOVE_UP = 2
        private const val MENU_MOVE_DOWN = 3
        private const val MENU_DELETE = 4
    }
}
