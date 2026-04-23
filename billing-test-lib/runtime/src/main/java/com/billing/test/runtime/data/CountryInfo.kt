package com.billing.test.runtime.data

data class CountryInfo(
    val code: String,
    val name: String,
    val flagEmoji: String,
    val currencyCode: String
)

object Countries {
    val list: List<CountryInfo> = listOf(
        CountryInfo("us", "United States", "\uD83C\uDDFA\uD83C\uDDF8", "USD"),
        CountryInfo("kr", "South Korea", "\uD83C\uDDF0\uD83C\uDDF7", "KRW"),
        CountryInfo("de", "Germany", "\uD83C\uDDE9\uD83C\uDDEA", "EUR"),
        CountryInfo("fr", "France", "\uD83C\uDDEB\uD83C\uDDF7", "EUR"),
        CountryInfo("gb", "United Kingdom", "\uD83C\uDDEC\uD83C\uDDE7", "GBP"),
        CountryInfo("ca", "Canada", "\uD83C\uDDE8\uD83C\uDDE6", "CAD"),
        CountryInfo("it", "Italy", "\uD83C\uDDEE\uD83C\uDDF9", "EUR"),
        CountryInfo("br", "Brazil", "\uD83C\uDDE7\uD83C\uDDF7", "BRL"),
        CountryInfo("es", "Spain", "\uD83C\uDDEA\uD83C\uDDF8", "EUR"),
        CountryInfo("id", "Indonesia", "\uD83C\uDDEE\uD83C\uDDE9", "IDR"),
        CountryInfo("in", "India", "\uD83C\uDDEE\uD83C\uDDF3", "INR"),
        CountryInfo("th", "Thailand", "\uD83C\uDDF9\uD83C\uDDED", "THB"),
        CountryInfo("ph", "Philippines", "\uD83C\uDDF5\uD83C\uDDED", "PHP"),
        CountryInfo("ro", "Romania", "\uD83C\uDDF7\uD83C\uDDF4", "RON"),
        CountryInfo("jp", "Japan", "\uD83C\uDDEF\uD83C\uDDF5", "JPY")
    )

    fun findByCode(code: String): CountryInfo? {
        return list.find { it.code == code.lowercase() }
    }
}
