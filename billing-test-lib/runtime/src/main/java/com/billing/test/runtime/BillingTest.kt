package com.billing.test.runtime

/**
 * Entry point for the billing test library.
 * Call [init] in Application.onCreate() with a project-specific [BillingTestContract].
 */
object BillingTest {

    private var contract: BillingTestContract? = null

    /**
     * Initialize the billing test library.
     * Must be called before using any billing test features.
     */
    @JvmStatic
    fun init(contract: BillingTestContract) {
        this.contract = contract
    }

    internal fun getContract(): BillingTestContract {
        return contract
            ?: throw IllegalStateException("BillingTest.init() not called. Call it in Application.onCreate().")
    }
}
