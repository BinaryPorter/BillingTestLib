package com.billing.test.runtime

import android.content.Context
import android.content.Intent

/**
 * Interface for creating custom Intents when launching a billing test page.
 * Used by pages that require specific extras (e.g., discount percentage).
 *
 * Implementations must have a no-arg constructor.
 */
interface BillingIntentFactory {
    fun createIntent(context: Context): Intent
}
