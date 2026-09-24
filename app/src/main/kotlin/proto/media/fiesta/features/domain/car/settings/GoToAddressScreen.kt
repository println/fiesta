package proto.media.fiesta.features.domain.car.settings

import android.content.Context
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.text.Editable
import android.text.TextWatcher
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.ImageView
import android.widget.TextView
import proto.media.fiesta.R
import proto.media.fiesta.features.domain.car.browser.CarEditText
import proto.media.fiesta.features.domain.core.bookmarks.BookmarkUtils
import proto.media.fiesta.features.domain.core.browser.AutocompleteUtils
import proto.media.fiesta.features.domain.core.browser.BrowserStorageUtils
import proto.media.fiesta.features.domain.core.browser.SearchSuggestClient
import proto.media.fiesta.features.domain.core.browser.SuggestionMerger
import proto.media.fiesta.support.search.SearchEngine
import proto.media.fiesta.shared.search.texts
import proto.media.fiesta.features.domain.core.settings.SettingsUtils
import io.realm.Realm
import java.net.URI

class GoToAddressScreen(
    private val context: Context,
    private var engine: SearchEngine,
    private val engines: List<SearchEngine>,
    private val onEngineChanged: (SearchEngine) -> Unit,
    private val startWithVoice: Boolean,
    private val bindKeyboard: (CarEditText) -> Unit,
    private val unbindKeyboard: () -> Unit,
    private val startVoiceInput: (onResult: (String) -> Unit, onInterrupted: () -> Unit) -> Unit,
    private val stopVoiceInput: () -> Unit,
    private val onClosedWithoutNavigation: () -> Unit,
    private val onAddressConfirmed: (input: String, search: (String) -> String) -> Unit
) : CarSettingsScreen {

    override val titleRes: Int
        get() = engine.texts.titleRes

    private val searchSuggestClient = SearchSuggestClient("go-to-address-suggest", SEARCH_DEBOUNCE_MILLIS)
    private lateinit var editText: CarEditText
    private lateinit var voiceButton: View
    private var engineButtons: Map<SearchEngine, View> = emptyMap()
    private lateinit var suggestionRows: List<View>
    private lateinit var suggestionIcons: List<ImageView>
    private lateinit var suggestionTexts: List<TextView>
    private var listening = false
    private var navigated = false

    override fun createView(inflater: LayoutInflater, parent: ViewGroup): View {
        val view = inflater.inflate(R.layout.car_settings_go_to_address, parent, false)
        editText = view.findViewById(R.id.go_to_address_edittext)
        showEngine()

        val suggestionsContainer = view.findViewById<ViewGroup>(R.id.go_to_address_suggestions)
        val rows = (0 until MAX_ROWS).map {
            inflater.inflate(R.layout.car_settings_address_suggestion_row, suggestionsContainer, false)
        }
        rows.forEach { suggestionsContainer.addView(it) }
        suggestionRows = rows
        suggestionIcons = rows.map { it.findViewById(R.id.suggestion_icon) }
        suggestionTexts = rows.map { it.findViewById(R.id.suggestion_text) }

        editText.addTextChangedListener(AddressTextWatcher())
        editText.setOnClickListener { enterTextMode() }
        editText.setOnEditorActionListener { _, actionId, event -> onEditorAction(actionId, event) }

        voiceButton = view.findViewById(R.id.go_to_address_voice_button)
        voiceButton.setOnClickListener {
            if (listening) enterTextMode() else enterVoiceMode()
        }

        renderRows(emptyList())
        return view
    }

    override fun createHeaderActions(inflater: LayoutInflater, container: ViewGroup) {
        engineButtons = engines.associateWith { buttonEngine ->
            val button = inflater.inflate(R.layout.car_search_engine_button, container, false)
            button.findViewById<ImageView>(R.id.engine_button_icon).setImageResource(buttonEngine.texts.iconRes)
            button.contentDescription = context.getString(buttonEngine.texts.titleRes)
            button.setOnClickListener { selectEngine(buttonEngine) }
            container.addView(button)
            button
        }
        showEngine()
    }

    private fun selectEngine(selected: SearchEngine) {
        if (selected == engine) return
        engine = selected
        showEngine()
        onEngineChanged(engine)
        updateSuggestions(editText.text.toString())
        if (!listening) enterTextMode()
    }

    private fun showEngine() {
        editText.hint = context.getString(engine.texts.hintRes)
        engineButtons.forEach { (buttonEngine, button) ->
            val isSelected = buttonEngine == engine
            button.isSelected = isSelected
            button.findViewById<ImageView>(R.id.engine_button_icon).apply {
                colorFilter = if (isSelected) null else GRAYSCALE
                alpha = if (isSelected) 1f else UNSELECTED_ENGINE_ALPHA
            }
        }
    }

    override fun onShown() {
        if (startWithVoice) enterVoiceMode() else enterTextMode()
    }

    override fun onHidden() {
        if (listening) {
            stopVoiceInput()
            listening = false
        }
        unbindKeyboard()
        searchSuggestClient.shutdown()
        if (!navigated) onClosedWithoutNavigation()
    }

    private fun enterVoiceMode() {
        unbindKeyboard()
        listening = true
        startVoiceInput(
            { text ->
                listening = false
                editText.setText(text)
                editText.setSelection(text.length)
                confirmText(text)
            },
            { enterTextMode() }
        )
    }

    private fun enterTextMode() {
        if (listening) {
            stopVoiceInput()
            listening = false
        }
        bindKeyboard(editText)
        editText.requestFocus()
    }

    private fun onEditorAction(actionId: Int, event: KeyEvent?): Boolean {
        val isConfirmAction = actionId == EditorInfo.IME_ACTION_GO ||
            actionId == EditorInfo.IME_ACTION_DONE ||
            actionId == EditorInfo.IME_ACTION_SEARCH ||
            (event != null && event.keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_DOWN)
        if (isConfirmAction) {
            confirmText(editText.text.toString())
            return true
        }
        return false
    }

    private fun confirmText(text: String) {
        if (text.isBlank()) {
            return
        }
        navigateTo(text)
    }

    private fun navigateTo(input: String) {
        navigated = true
        onAddressConfirmed(input) { engine.searchUrl(it) }
    }

    private fun updateSuggestions(typed: String) {
        val trimmed = typed.trim()
        if (trimmed.isEmpty()) {
            renderRows(emptyList())
            return
        }

        val inline = AutocompleteUtils.findMatch(context, trimmed.lowercase())?.let { match ->
            SuggestionMerger.Suggestion(SuggestionMerger.Type.INLINE_COMPLETION, match, "https://$match", match)
        }

        val history = BrowserStorageUtils.getRecordedHosts(context)
            .filter { it.contains(trimmed, ignoreCase = true) }
            .map { SuggestionMerger.Suggestion(SuggestionMerger.Type.HISTORY, it, "https://$it", it) }

        val bookmarks = queryBookmarks(trimmed)

        if (SettingsUtils.isSearchSuggestionsEnabled(context)) {
            searchSuggestClient.fetch(engine.suggestionsUrl(trimmed)) { results ->
                val search = results.map {
                    SuggestionMerger.Suggestion(SuggestionMerger.Type.SEARCH, it, engine.searchUrl(it), null)
                }
                renderRows(SuggestionMerger.merge(inline, history, bookmarks, search, MAX_ROWS))
            }
        } else {
            renderRows(SuggestionMerger.merge(inline, history, bookmarks, emptyList(), MAX_ROWS))
        }
    }

    private fun queryBookmarks(trimmed: String): List<SuggestionMerger.Suggestion> {
        val realm = Realm.getDefaultInstance()
        return try {
            BookmarkUtils.getBookmarks(realm)
                .filter { bookmark ->
                    bookmark.title?.contains(trimmed, ignoreCase = true) == true ||
                        bookmark.url?.contains(trimmed, ignoreCase = true) == true
                }
                .mapNotNull { bookmark ->
                    val url = bookmark.url ?: return@mapNotNull null
                    SuggestionMerger.Suggestion(SuggestionMerger.Type.BOOKMARK, bookmark.title ?: url, url, hostOf(url))
                }
        } finally {
            realm.close()
        }
    }

    private fun renderRows(suggestions: List<SuggestionMerger.Suggestion>) {
        for (i in suggestionRows.indices) {
            val suggestion = suggestions.getOrNull(i)
            if (suggestion == null) {
                suggestionRows[i].visibility = View.GONE
                continue
            }
            suggestionRows[i].visibility = View.VISIBLE
            suggestionTexts[i].text = suggestion.text
            suggestionIcons[i].setImageResource(iconFor(suggestion.type))
            suggestionRows[i].setOnClickListener { navigateTo(suggestion.url) }
        }
    }

    private fun iconFor(type: SuggestionMerger.Type): Int = when (type) {
        SuggestionMerger.Type.INLINE_COMPLETION -> R.drawable.mozac_ic_globe
        SuggestionMerger.Type.HISTORY -> R.drawable.ic_car_history
        SuggestionMerger.Type.BOOKMARK -> R.drawable.ic_star
        SuggestionMerger.Type.SEARCH -> R.drawable.mozac_ic_search
    }

    private fun hostOf(url: String): String? = try {
        URI(url).host
    } catch (e: Exception) {
        null
    }

    private inner class AddressTextWatcher : TextWatcher {
        private var lengthBeforeChange = 0
        private var autocompleting = false

        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
            lengthBeforeChange = s.length
        }

        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}

        override fun afterTextChanged(s: Editable) {
            if (autocompleting) {
                autocompleting = false
                return
            }
            val growing = s.length > lengthBeforeChange
            val cursorAtEnd = editText.selectionStart == s.length && editText.selectionEnd == s.length
            val typed = s.toString()
            if (growing && cursorAtEnd) {
                val match = AutocompleteUtils.findMatch(context, typed.lowercase())
                if (match != null && match.length > typed.length) {
                    autocompleting = true
                    s.append(match.substring(typed.length))
                    editText.setSelection(typed.length, s.length)
                }
            }
            updateSuggestions(typed)
        }
    }

    companion object {
        private const val MAX_ROWS = 6
        private const val UNSELECTED_ENGINE_ALPHA = 0.6f
        private val GRAYSCALE = ColorMatrixColorFilter(ColorMatrix().apply { setSaturation(0f) })
        private const val SEARCH_DEBOUNCE_MILLIS = 300L
    }
}
