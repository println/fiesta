package proto.media.fiesta.features.domain.phone.browser
import android.content.Context
import android.util.AttributeSet
import android.view.KeyEvent
import androidx.appcompat.widget.AppCompatEditText

class UrlEditText @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = androidx.appcompat.R.attr.editTextStyle
) : AppCompatEditText(context, attrs, defStyleAttr) {

    fun interface BackListener {
        fun onBackWhileFocused(): Boolean
    }

    private var backListener: BackListener? = null

    fun setBackListener(backListener: BackListener?) {
        this.backListener = backListener
    }

    override fun dispatchKeyEventPreIme(event: KeyEvent): Boolean {
        if (event.keyCode == KeyEvent.KEYCODE_BACK
            && event.action == KeyEvent.ACTION_UP
            && hasFocus()
            && backListener?.onBackWhileFocused() == true
        ) {
            return true
        }
        return super.dispatchKeyEventPreIme(event)
    }
}
