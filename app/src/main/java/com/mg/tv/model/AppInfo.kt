package com.mg.tv.model

import android.graphics.drawable.Drawable

data class AppInfo(
    val packageName: String,
    val activityName: String,
    val label: String,
    val icon: Drawable?,
    val banner: Drawable? = null,
    val isLeanback: Boolean = true,
    var isFavorite: Boolean = false
)
