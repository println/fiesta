package proto.media.fiesta.features.layout

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import proto.media.fiesta.R
import proto.media.fiesta.features.domain.phone.update.UpdateNotice

class MainPhoneActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_phone_main)
        if (savedInstanceState == null) UpdateNotice(this).checkInBackground()
    }
}
