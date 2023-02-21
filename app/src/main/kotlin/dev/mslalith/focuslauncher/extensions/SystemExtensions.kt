package dev.mslalith.focuslauncher.extensions

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import dev.mslalith.focuslauncher.core.common.extensions.isSystemApp
import dev.mslalith.focuslauncher.core.model.App

val Context.appDrawerApps: List<App>
    get() {
        val intent = Intent(Intent.ACTION_MAIN)
        intent.addCategory(Intent.CATEGORY_LAUNCHER)

        val list = packageManager.queryIntentActivities(intent, PackageManager.GET_META_DATA)
        return list.map { resolveInfo ->
            with(resolveInfo.activityInfo) {
                val appName = loadLabel(packageManager).toString()
                val appPackageName = packageName
                val isSystem = isSystemApp(appPackageName)
                return@map App(
                    name = appName,
                    displayName = appName,
                    packageName = appPackageName,
                    isSystem = isSystem
                )
            }
        }.sortedBy { it.name }
    }

fun Context.canLaunch(packageName: String): Boolean {
    val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)

    val list = packageManager.queryIntentActivities(intent, PackageManager.GET_META_DATA)
    return list.any { resolveInfo ->
        packageName == resolveInfo?.activityInfo?.packageName
    }
}
