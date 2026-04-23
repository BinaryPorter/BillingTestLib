package com.billing.test.runtime

import android.app.Activity
import android.app.Dialog

/**
 * Interface for creating a billing Dialog test page.
 * Implementations must have a no-arg constructor.
 */
interface BillingDialogFactory {
    fun createDialog(activity: Activity): Dialog
}
