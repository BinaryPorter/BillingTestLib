package com.example.billingtestlib


object BillingManager {
    const val TYPE_MONTH: Int = 1
    const val TYPE_YEAR_20: Int = 2

    const val VIP_MONTHLY = "monthly"
    const val VIP_YEARLY_20 = "yearly_20"

    var countryCode: String = "US"

    fun getProductIdByType(type: Int): String {
        var productId = ""
        if (type == TYPE_MONTH) {
            productId = VIP_MONTHLY
        } else if (type == TYPE_YEAR_20) {
            productId = VIP_YEARLY_20
        }
        return productId
    }

    fun getSkuPrice(type: Int): String {
        return when (type) {
            TYPE_MONTH -> {
                "20"
            }
            TYPE_YEAR_20 -> {
                "200"
            }
            else -> {
                ""
            }
        }
    }
    fun getPriceSymbol(type: Int): String {
        return "$"
    }

    fun refresh() {
        //重新获取价格等信息
    }

    fun initSkuID() {
        //初始化sku id
        if (countryCode == "US") {
            //do something
        } else if (countryCode == "BR") {
            //do something
        }
    }
}