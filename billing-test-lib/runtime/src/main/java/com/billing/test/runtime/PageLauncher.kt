package com.billing.test.runtime

import android.content.Context
import android.content.Intent
import android.widget.Toast
import com.billing.test.annotation.BillingTestPageEntry

object PageLauncher {

    fun launch(context: Context, entry: BillingTestPageEntry) {
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
}
