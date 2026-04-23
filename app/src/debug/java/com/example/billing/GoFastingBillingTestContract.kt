package com.example.billing

import android.app.Activity
import com.billing.test.runtime.BillingTestContract
import com.billing.test.runtime.SkuInfo
import com.example.billingtestlib.BillingManager

class GoFastingBillingTestContract : BillingTestContract {
    override fun showCountryPart(): Boolean {
        return true
    }

    override fun setCountryCode(code: String) {
        BillingManager.countryCode = code
    }

    override fun getCountryCode(): String {
        return BillingManager.countryCode
    }

    override fun initSkuID() {
        BillingManager.initSkuID()
    }

    override fun refreshSkuPrice(activity: Activity) {
        BillingManager.refresh()
    }

    override fun getSkuList(): List<SkuInfo> {
       val types = listOf(
            BillingManager.TYPE_MONTH to "TYPE_MONTH",
            BillingManager.TYPE_YEAR_20 to "TYPE_YEAR_20",
        )

        return types.map { (type, name) ->
            SkuInfo(
                typeName = name,
                skuId = BillingManager.getProductIdByType(type),
                price = BillingManager.getSkuPrice(type),
                currencySymbol = BillingManager.getPriceSymbol(type)
            )
        }
    }
}
