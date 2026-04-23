package com.billing.test.runtime

import android.app.Activity

/**
 * Contract for project-specific billing test integration.
 * Each project implements this to bridge the test center with its own
 * country code override and SKU initialization logic.
 */
interface BillingTestContract {
    /**
     * 是否显示国家部分设置，如果不需要价格分组，设置为false隐藏国家设置部分
     */
    fun showCountryPart(): Boolean

    /** 价格分组：设置国家 */
    fun setCountryCode(code: String)

    /** 价格分组：获取国家. */
    fun getCountryCode(): String

    /** 价格分组：设置国家后刷新sku配置. */
    fun initSkuID()

    /** 重新获取价格信息 */
    fun refreshSkuPrice(activity:Activity)

    /**
     * 获取当前国家/分组的SKU列表，用于在测试中心预览显示。
     * 可选 - 返回空列表以隐藏SKU预览部分。
     */
    fun getSkuList(): List<SkuInfo> = emptyList()
}

/**
 * Represents a single SKU with display information.
 */
data class SkuInfo(
    val typeName: String,
    val skuId: String,
    val price: String,
    val currencySymbol: String
)
