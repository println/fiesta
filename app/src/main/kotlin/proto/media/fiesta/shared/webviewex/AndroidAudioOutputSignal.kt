package proto.media.fiesta.shared.webviewex

import android.content.Context
import android.media.AudioManager
import android.media.AudioPlaybackConfiguration
import android.os.Build
import android.os.Handler
import android.os.Looper
import proto.media.fiesta.support.webviewex.gearhead.AudioOutputSignal
import proto.media.fiesta.support.webviewex.gearhead.AudioOutputState

class AndroidAudioOutputSignal(context: Context) : AudioOutputSignal {

    private val audioManager = context.getSystemService(AudioManager::class.java)

    override fun observe(listener: (AudioOutputState) -> Unit): AutoCloseable {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return AutoCloseable { }
        val callback = object : AudioManager.AudioPlaybackCallback() {
            override fun onPlaybackConfigChanged(configs: MutableList<AudioPlaybackConfiguration>) {
                listener(if (configs.isEmpty()) AudioOutputState.IDLE else AudioOutputState.ACTIVE)
            }
        }
        audioManager.registerAudioPlaybackCallback(callback, Handler(Looper.getMainLooper()))
        return AutoCloseable { audioManager.unregisterAudioPlaybackCallback(callback) }
    }
}
