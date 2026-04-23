package com.billing.test.runtime.ui

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.billing.test.runtime.BillingDialogFactory

class DialogHostActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val factoryClassName = intent.getStringExtra(EXTRA_FACTORY_CLASS)
        if (factoryClassName == null) {
            finish()
            return
        }

        try {
            val factoryClass = Class.forName(factoryClassName)
            val factory = factoryClass.getDeclaredConstructor().newInstance() as BillingDialogFactory
            val dialog = factory.createDialog(this)
            dialog.setOnDismissListener { finish() }
            dialog.show()
        } catch (e: ClassNotFoundException) {
            Toast.makeText(this, "Class not found: ${e.message}", Toast.LENGTH_SHORT).show()
            finish()
        } catch (e: Exception) {
            Toast.makeText(this, "Failed to create dialog: ${e.message}", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    companion object {
        const val EXTRA_FACTORY_CLASS = "billing_dialog_factory_class"

        fun createIntent(context: Context, factoryClassName: String): Intent {
            return Intent(context, DialogHostActivity::class.java).apply {
                putExtra(EXTRA_FACTORY_CLASS, factoryClassName)
            }
        }
    }
}
