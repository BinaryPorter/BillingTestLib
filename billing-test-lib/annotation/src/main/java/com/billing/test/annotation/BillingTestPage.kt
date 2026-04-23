package com.billing.test.annotation

/**
 * Marks an Activity or BillingDialogFactory as a testable billing page.
 * The ksp processor will collect all annotated classes and generate a BillingTestPageRegistry.
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.BINARY)
annotation class BillingTestPage(
    /** Display name shown in the test center list. */
    val name: String,
    /** Page type: Activity or Dialog. */
    val type: BillingPageType = BillingPageType.ACTIVITY,
    /** Custom category name for grouping. Each project defines its own categories. */
    val category: String = "OTHER",
    /** Optional description. */
    val description: String = "",
    /** Fully qualified class name of a custom Intent factory for Activity pages that need extras. */
    val intentFactory: String = ""
)

enum class BillingPageType {
    ACTIVITY,
    DIALOG
}
