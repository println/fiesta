package proto.media.fiesta.features.domain.car.browser

import android.content.Context
import android.support.car.input.CarRestrictedEditText
import android.util.AttributeSet
import com.google.android.gms.car.input.CarEditable
import com.google.android.gms.car.input.CarEditableListener

class CarEditText(context: Context, attributeSet: AttributeSet?) :
    CarRestrictedEditText(context, attributeSet), CarEditable {

    override fun setCarEditableListener(carEditableListener: CarEditableListener?) {
        super.setCarEditableListener(android.support.car.input.CarEditableListener { i, i1, i2, i3 ->
            carEditableListener!!.onUpdateSelection(i, i1, i2, i3)
        })
    }
}
