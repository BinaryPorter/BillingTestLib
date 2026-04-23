package com.example.billing

import android.content.Context
import android.content.Intent
import com.billing.test.runtime.BillingIntentFactory
import com.example.billingtestlib.SkuActivity1
import com.example.billingtestlib.SkuActivity2

class SkuActivity2Factory : BillingIntentFactory {
    override fun createIntent(context: Context): Intent {
        return Intent(context, SkuActivity2::class.java).apply {
            putExtra("vipDiscount", "60")
        }
    }
}
