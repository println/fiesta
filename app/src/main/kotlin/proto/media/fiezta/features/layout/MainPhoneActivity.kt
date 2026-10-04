package proto.media.fiezta.features.layout

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import proto.media.fiezta.R
import proto.media.fiezta.features.domain.phone.update.UpdateNotice

class MainPhoneActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_phone_main)
        if (savedInstanceState == null) UpdateNotice(this).checkInBackground()
    }
}
