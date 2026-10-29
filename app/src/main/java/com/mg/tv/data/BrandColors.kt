package com.mg.tv.data

import android.graphics.Color
import android.graphics.drawable.GradientDrawable

/**
 * Brand ambient lighting colors for streaming/TV apps.
 * Creates an Apple TV-style luminous ambient glow centered behind the header/title.
 */
object BrandColors {

    data class AmbientTheme(
        val centerColor: Int,
        val midColor: Int,
        val outerColor: Int = Color.parseColor("#08090C")
    )

    private val brandThemes = mapOf(
        // Netflix: rich glowing crimson spotlight fading to deep wine
        "com.netflix.ninja" to AmbientTheme(
            centerColor = Color.parseColor("#B80A15"),
            midColor = Color.parseColor("#420408")
        ),

        // Prime Video: vibrant electric cyan/blue
        "com.amazon.amazonvideo.livingroom" to AmbientTheme(
            centerColor = Color.parseColor("#0084C9"),
            midColor = Color.parseColor("#00213D")
        ),

        // YouTube: pure vibrant red glow
        "com.google.android.youtube.tv" to AmbientTheme(
            centerColor = Color.parseColor("#D60000"),
            midColor = Color.parseColor("#400000")
        ),
        "com.google.android.youtube.tvmusic" to AmbientTheme(
            centerColor = Color.parseColor("#D60000"),
            midColor = Color.parseColor("#400000")
        ),

        // JioHotstar: royal violet-magenta ambient spotlight
        "in.startv.hotstar" to AmbientTheme(
            centerColor = Color.parseColor("#5A228B"),
            midColor = Color.parseColor("#1C0A2E")
        ),

        // SonyLIV: electric cobalt blue
        "com.sonyliv" to AmbientTheme(
            centerColor = Color.parseColor("#1D4ED8"),
            midColor = Color.parseColor("#0B1B48")
        ),

        // Google Play & Videos
        "com.google.android.videos" to AmbientTheme(
            centerColor = Color.parseColor("#2563EB"),
            midColor = Color.parseColor("#0C1C44")
        ),
        "com.android.vending" to AmbientTheme(
            centerColor = Color.parseColor("#059669"),
            midColor = Color.parseColor("#022E21")
        ),

        // Mi apps
        "com.mitv.gallery" to AmbientTheme(
            centerColor = Color.parseColor("#D97706"),
            midColor = Color.parseColor("#3D2102")
        ),
        "com.mitv.tvhome.mitvplus" to AmbientTheme(
            centerColor = Color.parseColor("#EA580C"),
            midColor = Color.parseColor("#3B1602")
        )
    )

    private val defaultTheme = AmbientTheme(
        centerColor = Color.parseColor("#28303E"),
        midColor = Color.parseColor("#141820")
    )

    fun getTheme(packageName: String): AmbientTheme {
        return brandThemes[packageName] ?: defaultTheme
    }

    /**
     * Builds a smooth radial gradient centered at the top-middle (50% X, 26% Y).
     */
    fun createRadialDrawable(packageName: String, screenWidthPx: Float): GradientDrawable {
        val theme = getTheme(packageName)
        return GradientDrawable().apply {
            gradientType = GradientDrawable.RADIAL_GRADIENT
            setGradientCenter(0.50f, 0.26f)
            // Soft diffuse glow spanning across the top portion of the TV
            gradientRadius = screenWidthPx * 0.60f
            colors = intArrayOf(
                theme.centerColor,
                theme.midColor,
                theme.outerColor
            )
        }
    }
}
