package proto.media.fiesta.features.domain.car.app

import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import proto.media.fiesta.BuildConfig
import proto.media.fiesta.R

class SafetyWarningFragment : Fragment() {
    private var listener: FragmentInteractionListener? = null
    private lateinit var continueButton: View
    private lateinit var appInfo: TextView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.safety_warning, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        appInfo = view.findViewById(R.id.app_info_view)
        appInfo.text = "${getString(R.string.app_name)} v${BuildConfig.VERSION_NAME}"
        continueButton = view.findViewById(R.id.continue_button)
        continueButton.setOnClickListener {
            listener!!.onReadyToExitSafetyInstructions(this@SafetyWarningFragment)
        }
        continueButton.isFocusable = true
        continueButton.isFocusableInTouchMode = true
        continueButton.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                continueButton.requestFocus()
            }
        }
        continueButton.setOnKeyListener { _, keyCode, _ ->
            if (keyCode == KeyEvent.KEYCODE_BACK) {
                listener!!.onReadyToExitSafetyInstructions(this@SafetyWarningFragment)
                true
            } else keyCode == KeyEvent.KEYCODE_DPAD_DOWN || keyCode == KeyEvent.KEYCODE_DPAD_UP ||
                keyCode == KeyEvent.KEYCODE_DPAD_LEFT || keyCode == KeyEvent.KEYCODE_DPAD_RIGHT
        }
    }

    override fun onResume() {
        super.onResume()
        continueButton.requestFocus()
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        (parentFragment as? FragmentInteractionListener)?.let { listener = it }
    }

    interface FragmentInteractionListener {
        fun onReadyToExitSafetyInstructions(warningFragment: SafetyWarningFragment?)
    }
}
