package com.rahullohra.lab

import android.content.Context
import android.util.DisplayMetrics

object Util {

    fun dpToPx(context: Context, dp: Int): Float {
        return dp * (context.resources.displayMetrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT).toFloat()
    }

    fun pxToDp(context: Context, px: Int): Float {
        return px / (context.resources.displayMetrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT).toFloat()
    }
}