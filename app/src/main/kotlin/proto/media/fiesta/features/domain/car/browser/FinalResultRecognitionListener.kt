package proto.media.fiesta.features.domain.car.browser

import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.SpeechRecognizer

class FinalResultRecognitionListener(private val callbacks: Callbacks) : RecognitionListener {

    override fun onReadyForSpeech(params: Bundle?) {}

    override fun onBeginningOfSpeech() {}

    override fun onRmsChanged(rmsdB: Float) {}

    override fun onBufferReceived(buffer: ByteArray?) {}

    override fun onEndOfSpeech() {}

    override fun onError(error: Int) {
        callbacks.onError(error)
    }

    override fun onResults(results: Bundle) {
        val bestMatch = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)?.firstOrNull()
        if (bestMatch != null) {
            callbacks.onFinalResult(bestMatch)
        } else {
            callbacks.onError(NO_CANDIDATE)
        }
    }

    override fun onPartialResults(partialResults: Bundle?) {}

    override fun onEvent(eventType: Int, params: Bundle?) {}

    interface Callbacks {
        fun onFinalResult(text: String)

        fun onError(error: Int)
    }

    private companion object {
        const val NO_CANDIDATE = 0
    }
}
