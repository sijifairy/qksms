package com.moez.QKSMS.common.util

import android.content.Context
import android.graphics.*
import android.os.Build
import android.util.DisplayMetrics
import android.util.Log
import android.view.View
import android.view.Window
import android.view.WindowManager
import java.io.File

val ATLEAST_O = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
val ATLEAST_LOLLIPOP = Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP
val ATLEAST_JB = Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN
val ATLEAST_N = Build.VERSION.SDK_INT >= Build.VERSION_CODES.N
val ATLEAST_MARSHMALLOW = Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
val ATLEAST_KITKAT = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT
val ATLEAST_JB_MR1 = Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1

fun transparentStatusbarNavigationbar(window: Window) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS or WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION)
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.statusBarColor = Color.TRANSPARENT
        window.navigationBarColor = Color.TRANSPARENT
    } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
        // Make status bar and navigation bar transparent
        window.addFlags(
            WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS or WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION
        )
    }
}

fun immersive(window: Window) {
    val decorView = window.decorView
    val option = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
    decorView.systemUiVisibility = option
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.statusBarColor = Color.TRANSPARENT
    }
}

fun setSystemButtonsTheme(window: Window, isLight: Boolean) {
    if (ATLEAST_MARSHMALLOW) {
        val rootView = window.decorView.rootView
        var systemUiVisibility = rootView.systemUiVisibility
        if (isLight) {
            systemUiVisibility = systemUiVisibility or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            if (ATLEAST_O) {
                systemUiVisibility = systemUiVisibility or View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
            }
        } else {
            systemUiVisibility = systemUiVisibility and View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR.inv()
            if (ATLEAST_O) {
                systemUiVisibility =
                    systemUiVisibility and View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR.inv()
            }
        }
        rootView.systemUiVisibility = systemUiVisibility
    }
}

fun setStatusBarTheme(window: Window, isLight: Boolean) {
    if (ATLEAST_MARSHMALLOW) {
        val rootView = window.decorView.rootView
        var systemUiVisibility = rootView.systemUiVisibility
        if (isLight) {
            systemUiVisibility = systemUiVisibility or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        } else {
            systemUiVisibility = systemUiVisibility and View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR.inv()
        }
        rootView.systemUiVisibility = systemUiVisibility
    }
}

fun getPhoneWidth(context: Context): Int {
    val dm = DisplayMetrics()
    val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    val display = wm.defaultDisplay
    display.getMetrics(dm)
    return dm.widthPixels
}

/**
 * 返回手机屏幕高度
 */
fun getPhoneHeight(context: Context): Int {
    var height = context.resources.displayMetrics.heightPixels
    val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager

    if (windowManager != null) {
        val localPoint = Point()
        windowManager.defaultDisplay.getRealSize(localPoint)
        if (localPoint.y > height) {
            height = localPoint.y
        }
    }
    return height
}

fun rotate(file: File, angle: Int = 90): Bitmap {
    return rotatingImage(angle, BitmapFactory.decodeFile(file.absolutePath))
}

/**
 * 旋转图片
 * rotate the image with specified angle
 *
 * @param angle  the angle will be rotating 旋转的角度
 * @param bitmap target image               目标图片
 */
fun rotatingImage(angle: Int, bitmap: Bitmap): Bitmap {
    //rotate image
    val matrix = Matrix()
    matrix.postRotate(angle.toFloat())

    //create a new image
    return Bitmap.createBitmap(
        bitmap, 0, 0, bitmap.width, bitmap.height, matrix,
        true
    )
}
