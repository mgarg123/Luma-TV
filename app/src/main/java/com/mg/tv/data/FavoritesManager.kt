package com.mg.tv.data

import android.content.Context
import android.content.SharedPreferences

class FavoritesManager(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    companion object {
        private const val PREFS_NAME = "luma_tv_favorites"
        private const val KEY_FAVORITES = "user_favorites"
        private const val KEY_INITIALIZED = "favorites_initialized_v2"

        // Default top 4 matches the reference image: Netflix, Prime Video, YouTube, Hotstar
        val DEFAULT_FAVORITES_PRIORITY = listOf(
            "com.netflix.ninja",
            "com.amazon.amazonvideo.livingroom",
            "com.google.android.youtube.tv",
            "in.startv.hotstar",
            "com.sonyliv",
            "com.mitv.gallery",
            "com.android.vending",
            "org.videolan.vlc"
        )
    }

    fun isInitialized(): Boolean = prefs.getBoolean(KEY_INITIALIZED, false)

    fun initializeDefaults(installedPackages: List<String>) {
        if (isInitialized()) return

        val initialFavs = LinkedHashSet<String>()
        for (pkg in DEFAULT_FAVORITES_PRIORITY) {
            if (installedPackages.contains(pkg)) {
                initialFavs.add(pkg)
                if (initialFavs.size >= 4) break
            }
        }

        // If fewer than 4 matched priority list, fill with first available installed packages
        if (initialFavs.size < 4) {
            for (pkg in installedPackages) {
                initialFavs.add(pkg)
                if (initialFavs.size >= 4) break
            }
        }

        prefs.edit()
            .putStringSet(KEY_FAVORITES, initialFavs)
            .putBoolean(KEY_INITIALIZED, true)
            .apply()
    }

    fun getFavorites(): Set<String> {
        return prefs.getStringSet(KEY_FAVORITES, emptySet()) ?: emptySet()
    }

    fun isFavorite(packageName: String): Boolean {
        return getFavorites().contains(packageName)
    }

    fun toggleFavorite(packageName: String): Boolean {
        val current = HashSet(getFavorites())
        val newState = if (current.contains(packageName)) {
            current.remove(packageName)
            false
        } else {
            current.add(packageName)
            true
        }
        prefs.edit().putStringSet(KEY_FAVORITES, current).apply()
        return newState
    }

    fun setFavorite(packageName: String, favorite: Boolean) {
        val current = HashSet(getFavorites())
        if (favorite) {
            current.add(packageName)
        } else {
            current.remove(packageName)
        }
        prefs.edit().putStringSet(KEY_FAVORITES, current).apply()
    }
}
