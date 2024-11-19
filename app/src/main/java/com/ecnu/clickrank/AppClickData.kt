package com.ecnu.clickrank

import android.graphics.drawable.Drawable

data class AppClickData(
    val appName: String,
    var clickCount: Int = 0, // 点击次数
    var appIcon: Drawable? = null // 应用图标
)
