package com.billing.test.annotation

/**
 * Runtime representation of a billing test page entry.
 * Populated from generated BillingTestPageRegistry.
 */
data class BillingTestPageEntry(
    val name: String,
    val category: String,
    val description: String,
    val type: BillingPageType = BillingPageType.ACTIVITY,
    val activityClassName: String = "",
    val intentFactoryClassName: String? = null
)
