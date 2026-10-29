package com.mg.tv.data

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import com.mg.tv.model.AppInfo

class AppRepository(
    private val context: Context,
    private val favoritesManager: FavoritesManager
) {

    fun getInstalledApps(): List<AppInfo> {
        val pm = context.packageManager
        val selfPackage = context.packageName

        // 1. Query Leanback Launcher apps
        val leanbackIntent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_LEANBACK_LAUNCHER)
        }
        val leanbackApps: List<ResolveInfo> = pm.queryIntentActivities(leanbackIntent, 0)

        // 2. Query Standard Launcher apps
        val standardIntent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }
        val standardApps: List<ResolveInfo> = pm.queryIntentActivities(standardIntent, 0)

        val appMap = LinkedHashMap<String, AppInfo>()

        // Add Leanback apps first
        for (ri in leanbackApps) {
            val pkg = ri.activityInfo.packageName
            if (pkg == selfPackage) continue

            val label = ri.loadLabel(pm)?.toString() ?: ri.activityInfo.name

            val banner = try {
                ri.activityInfo.loadBanner(pm)
                    ?: ri.activityInfo.applicationInfo.loadBanner(pm)
                    ?: pm.getActivityBanner(android.content.ComponentName(pkg, ri.activityInfo.name))
                    ?: pm.getApplicationBanner(pkg)
            } catch (e: Exception) {
                null
            }

            val icon = try {
                ri.activityInfo.loadIcon(pm) ?: ri.loadIcon(pm)
            } catch (e: Exception) {
                null
            }

            appMap[pkg] = AppInfo(
                packageName = pkg,
                activityName = ri.activityInfo.name,
                label = label.trim(),
                icon = icon,
                banner = banner,
                isLeanback = true,
                isFavorite = false
            )
        }

        // Add Standard apps if not already present
        for (ri in standardApps) {
            val pkg = ri.activityInfo.packageName
            if (pkg == selfPackage) continue
            if (appMap.containsKey(pkg)) continue

            val label = ri.loadLabel(pm)?.toString() ?: ri.activityInfo.name

            val banner = try {
                ri.activityInfo.loadBanner(pm)
                    ?: ri.activityInfo.applicationInfo.loadBanner(pm)
                    ?: pm.getActivityBanner(android.content.ComponentName(pkg, ri.activityInfo.name))
                    ?: pm.getApplicationBanner(pkg)
            } catch (e: Exception) {
                null
            }

            val icon = try {
                ri.loadIcon(pm)
            } catch (e: Exception) {
                null
            }

            appMap[pkg] = AppInfo(
                packageName = pkg,
                activityName = ri.activityInfo.name,
                label = label.trim(),
                icon = icon,
                banner = banner,
                isLeanback = false,
                isFavorite = false
            )
        }

        // Initialize defaults if first run
        if (!favoritesManager.isInitialized()) {
            favoritesManager.initializeDefaults(appMap.keys.toList())
        }

        val favoritePackages = favoritesManager.getFavorites()

        // Populate isFavorite flag
        val allApps = appMap.values.map { app ->
            app.copy(isFavorite = favoritePackages.contains(app.packageName))
        }

        // Split into favourites (ordered by priority) and rest (alphabetical)
        val favPriority = FavoritesManager.DEFAULT_FAVORITES_PRIORITY
        val favs = allApps.filter { it.isFavorite }.sortedWith(compareBy { app ->
            val idx = favPriority.indexOf(app.packageName)
            if (idx >= 0) idx else 999
        })

        val nonFavs = allApps.filter { !it.isFavorite }.sortedWith(
            compareBy(String.CASE_INSENSITIVE_ORDER) { it.label }
        )

        // Return combined list with favorites first
        return favs + nonFavs
    }
}

