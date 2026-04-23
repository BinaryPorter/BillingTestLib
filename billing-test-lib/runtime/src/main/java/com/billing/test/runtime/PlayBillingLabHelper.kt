package com.billing.test.runtime

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.widget.Toast

object PlayBillingLabHelper {

    private const val PACKAGE_NAME = "com.google.android.apps.play.billingtestcompanion"
    private const val PLAY_STORE_URI = "market://details?id=$PACKAGE_NAME"

    fun isInstalled(context: Context): Boolean {
        return try {
            context.packageManager.getPackageInfo(PACKAGE_NAME, 0)
            true
        } catch (e: PackageManager.NameNotFoundException) {
            false
        }
    }

    fun launch(context: Context) {
        if (!isInstalled(context)) {
            Toast.makeText(context, "Please install Play Billing Lab first", Toast.LENGTH_LONG).show()
            try {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(PLAY_STORE_URI))
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
            } catch (e: Exception) {
                Toast.makeText(context, "Cannot open Play Store: ${e.message}", Toast.LENGTH_LONG).show()
            }
            return
        }

        // Try launch intent first (standard launcher activity)
        val launchIntent = context.packageManager.getLaunchIntentForPackage(PACKAGE_NAME)
        if (launchIntent != null) {
            launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            launchIntent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
            context.startActivity(launchIntent)
            return
        }

        // Fallback: query for any exportable activity in the package
        try {
            val packageInfo = context.packageManager.getPackageInfo(PACKAGE_NAME,
                PackageManager.GET_ACTIVITIES)
            val activities = packageInfo.activities
            if (!activities.isNullOrEmpty()) {
                val intent = Intent(Intent.ACTION_MAIN).apply {
                    setClassName(PACKAGE_NAME, activities[0].name)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
                }
                context.startActivity(intent)
                return
            }
        } catch (_: Exception) {
        }

        Toast.makeText(context, "Cannot launch Play Billing Lab", Toast.LENGTH_LONG).show()
    }
}
