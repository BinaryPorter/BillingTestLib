package com.example.billing

import android.app.Activity
import android.app.Dialog
import android.view.Gravity
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import com.billing.test.annotation.BillingTestPage
import com.billing.test.runtime.BillingDialogFactory
import com.google.android.material.bottomsheet.BottomSheetDialog

@BillingTestPage(
    name = "Subscribe Dialog",
    type = com.billing.test.annotation.BillingPageType.DIALOG,
    category = "Dialog",
    description = "Subscribe bottom sheet dialog example."
)
class SubscribeDialogFactory : BillingDialogFactory {
    override fun createDialog(activity: Activity): Dialog {
        return BottomSheetDialog(activity).apply {
            val layout = LinearLayout(activity).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(48, 48, 48, 48)
                val title = TextView(activity).apply {
                    text = "Subscribe Premium"
                    textSize = 22f
                    gravity = Gravity.CENTER
                    setPadding(0, 0, 0, 24)
                }
                val desc = TextView(activity).apply {
                    text = "Unlock all premium features with a subscription.\n\nMonthly: $9.99\nYearly: $79.99"
                    textSize = 16f
                    setPadding(0, 0, 0, 32)
                }
                val btnSubscribe = Button(activity).apply {
                    text = "Subscribe Now"
                    setOnClickListener { dismiss() }
                }
                addView(title)
                addView(desc)
                addView(btnSubscribe)
            }
            setContentView(ScrollView(activity).apply { addView(layout) })
        }
    }
}
