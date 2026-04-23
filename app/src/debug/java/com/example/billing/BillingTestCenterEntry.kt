package com.example.billing

import android.content.Intent
import android.view.View
import com.billing.test.runtime.ui.BillingTestCenterActivity
import com.example.billingtestlib.R

/**
 * Debug-only helper to set up the billing test center entry in DebugActivity.
 * Called from DebugActivity.initView() via try/catch reflection to avoid
 * compile-time dependency in release builds.
 */
object BillingTestCenterEntry {

    fun setup(view: View) {
        val billingTestCenter = view.findViewById<View>(R.id.billing_test_center)
        billingTestCenter?.setOnClickListener {
            val context = view.context
            context.startActivity(Intent(context, BillingTestCenterActivity::class.java))
        }
    }
}
