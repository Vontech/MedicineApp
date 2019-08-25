package org.vontech.medicine.views

import android.view.View

fun makeHeightEqualWidth(view: View) {

    val params = view.layoutParams
    params.height =  view.measuredWidth
    view.layoutParams = params
    view.invalidate()

}