package com.billing.test.runtime

import android.util.Log
import com.billing.test.annotation.BillingTestPageEntry
import com.billing.test.annotation.BillingTestPageRegistry
import java.util.ServiceLoader

object BillingTestPageRegistryProvider {

    private const val TAG = "BillingTest"

    private var cachedPages: List<BillingTestPageEntry>? = null

    fun getPages(): List<BillingTestPageEntry> {
        cachedPages?.let { return it }
        val pages = try {
            ServiceLoader.load(BillingTestPageRegistry::class.java)
                .iterator()
                .asSequence()
                .flatMap { it.getPages() }
                .toList()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to load BillingTestPageRegistry via ServiceLoader", e)
            emptyList()
        }
        if (pages.isEmpty()) {
            Log.w(TAG, "No BillingTestPageRegistry implementations found via ServiceLoader. "
                + "Ensure KSP ran and generated the registry + META-INF/services file.")
        } else {
            Log.d(TAG, "Loaded ${pages.size} billing test pages via ServiceLoader")
        }
        cachedPages = pages
        return pages
    }
}
