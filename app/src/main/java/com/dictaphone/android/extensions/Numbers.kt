package com.dictaphone.android.extensions

import android.content.Context
import android.util.TypedValue

/**
 * Extensions for numbers copmutation
 */
fun Context.dp(num: Int): Int {
    val resources = resources
    return TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP,
        num.toFloat(),
        resources.displayMetrics
    ).toInt()
}
