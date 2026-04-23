package com.go.fasting.billing.init

import android.content.ContentProvider
import android.content.ContentValues
import android.database.Cursor
import android.net.Uri
import com.example.billing.BillingTestInit

/**
 * Auto-initializes BillingTest in debug builds via ContentProvider.
 * No changes needed in App.kt — the manifest entry in src/debug handles registration.
 */
class BillingTestInitProvider : ContentProvider() {

    override fun onCreate(): Boolean {
        BillingTestInit.initIfNeeded(context!!.applicationContext as android.app.Application)
        return true
    }

    override fun query(uri: Uri, projection: Array<out String>?, selection: String?,
                      selectionArgs: Array<out String>?, sortOrder: String?): Cursor? = null

    override fun getType(uri: Uri): String? = null

    override fun insert(uri: Uri, values: ContentValues?): Uri? = null

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<out String>?): Int = 0

    override fun update(uri: Uri, values: ContentValues?, selection: String?,
                       selectionArgs: Array<out String>?): Int = 0
}
