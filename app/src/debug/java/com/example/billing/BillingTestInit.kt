package com.example.billing

import android.app.Application
import com.billing.test.runtime.BillingTest

/**
 * Debug-only initializer for BillingTest.
 * Register this in App.kt debug via ContentProvider or call init() manually.
 */
object BillingTestInit {

    private var initialized = false

    fun initIfNeeded(app: Application) {
        if (initialized) return
        initialized = true
        BillingTest.init(GoFastingBillingTestContract())
    }
}
