package com.billing.test.runtime

import android.content.Context
import android.content.Intent
import android.widget.Toast
import com.billing.test.annotation.BillingPageType
import com.billing.test.annotation.BillingTestPageEntry
import com.billing.test.runtime.ui.DialogHostActivity

object PageLauncher {

    fun launch(context: Context, entry: BillingTestPageEntry) {
        when (entry.type) {
            BillingPageType.ACTIVITY -> launchActivity(context, entry)
            BillingPageType.DIALOG -> launchDialog(context, entry)
        }
    }

    private fun launchActivity(context: Context, entry: BillingTestPageEntry) {
        try {
            val intent = if (entry.intentFactoryClassName != null) {
                val factoryClass = Class.forName(entry.intentFactoryClassName)
                val factory = factoryClass.getDeclaredConstructor().newInstance() as BillingIntentFactory
                factory.createIntent(context)
            } else {
                val activityClass = Class.forName(entry.activityClassName)
                Intent(context, activityClass)
            }
            context.startActivity(intent)
        } catch (e: ClassNotFoundException) {
            Toast.makeText(context, "Class not found: ${e.message}", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(context, "Failed to launch: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun launchDialog(context: Context, entry: BillingTestPageEntry) {
        val factoryClassName = entry.activityClassName.ifEmpty {
            Toast.makeText(context, "Missing dialog factory class name", Toast.LENGTH_SHORT).show()
            return
        }
        try {
            context.startActivity(DialogHostActivity.createIntent(context, factoryClassName))
        } catch (e: Exception) {
            Toast.makeText(context, "Failed to launch dialog: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
}
