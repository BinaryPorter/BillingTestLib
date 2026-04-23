package com.billing.test.runtime.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.billing.test.annotation.BillingPageType
import com.billing.test.annotation.BillingTestPageEntry
import com.billing.test.runtime.BillingTest
import com.billing.test.runtime.BillingTestPageRegistryProvider
import com.billing.test.runtime.PageLauncher
import com.billing.test.runtime.PlayBillingLabHelper
import com.billing.test.runtime.R
import com.billing.test.runtime.SkuInfo
import com.billing.test.runtime.data.Countries

class BillingTestCenterActivity : AppCompatActivity() {

    private lateinit var spinnerCountry: AdapterView<*> // actually Spinner but AdapterView for type compat

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_billing_test_center)
        setupCountrySpinner()
        setupQuickCountries()
        setupPlayBillingLab()
        setupSkuPreview()
        setupPageList()
        setupBackButton()
        setupResetButton()
    }

    private fun setupBackButton() {
        findViewById<View>(R.id.btn_back).setOnClickListener { finish() }
    }

    private fun setupResetButton() {
        findViewById<View>(R.id.btn_reset).setOnClickListener {
            val contract = BillingTest.getContract()
            contract.initSkuID()
            contract.refreshSkuPrice(this)
            it.postDelayed({ refreshSkuList() }, 3000)
        }
    }

    private fun setupCountrySpinner() {
        if (!BillingTest.getContract().showCountryPart()) {
            findViewById<View>(R.id.country).visibility = View.GONE
            return
        }

        val spinner = findViewById<android.widget.Spinner>(R.id.spinner_country)
        spinnerCountry = spinner

        val contract = BillingTest.getContract()
        val currentCode = contract.getCountryCode()

        val countries = Countries.list
        val labels = countries.map { "${it.flagEmoji} ${it.code.uppercase()} (${it.currencyCode})" }

        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, labels)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter

        // Select current country
        val currentIndex = countries.indexOfFirst { it.code == currentCode.lowercase() }
        if (currentIndex >= 0) {
            spinner.setSelection(currentIndex)
        }

        findViewById<View>(R.id.btn_apply_country).setOnClickListener {
            val selectedPosition = spinner.selectedItemPosition
            if (selectedPosition in countries.indices) {
                val selected = countries[selectedPosition]
                contract.setCountryCode(selected.code)
                contract.initSkuID()
                refreshSkuList()
            }
        }
    }

    private fun setupQuickCountries() {
        val recycler = findViewById<RecyclerView>(R.id.recycler_quick_countries)
        recycler.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        recycler.adapter = QuickCountryAdapter(Countries.list) { country ->
            val contract = BillingTest.getContract()
            contract.setCountryCode(country.code)
            contract.initSkuID()

            // Update spinner selection
            val spinner = findViewById<android.widget.Spinner>(R.id.spinner_country)
            val index = Countries.list.indexOfFirst { it.code == country.code }
            if (index >= 0) spinner.setSelection(index)

            refreshSkuList()
        }
    }

    private fun setupPlayBillingLab() {
        findViewById<View>(R.id.btn_play_billing_lab).setOnClickListener {
            PlayBillingLabHelper.launch(this)
        }
    }

    private fun setupSkuPreview() {
        refreshSkuList()
    }

    private fun refreshSkuList() {
        val contract = BillingTest.getContract()
        val skuList = contract.getSkuList()

        val layoutPreview = findViewById<View>(R.id.layout_sku_preview)
        val divider = findViewById<View>(R.id.divider_sku)
        val recycler = findViewById<RecyclerView>(R.id.recycler_sku)

        if (skuList.isEmpty()) {
            layoutPreview.visibility = View.GONE
            divider.visibility = View.GONE
        } else {
            layoutPreview.visibility = View.VISIBLE
            divider.visibility = View.VISIBLE
            recycler.layoutManager = LinearLayoutManager(this)
            recycler.adapter = SkuAdapter(skuList)
        }
    }

    private fun setupPageList() {
        val pages = BillingTestPageRegistryProvider.getPages()
        val recycler = findViewById<RecyclerView>(R.id.recycler_pages)

        // Group by category
        val grouped = pages.groupBy { it.category }
        val items = mutableListOf<Any>()
        for ((category, entries) in grouped) {
            items.add(CategoryHeader("$category (${entries.size})"))
            items.addAll(entries)
        }

        recycler.layoutManager = LinearLayoutManager(this)
        recycler.adapter = PageListAdapter(items) { entry ->
            PageLauncher.launch(this, entry)
        }
    }

    // --- Adapters ---

    private class QuickCountryAdapter(
        private val countries: List<com.billing.test.runtime.data.CountryInfo>,
        private val onSelect: (com.billing.test.runtime.data.CountryInfo) -> Unit
    ) : RecyclerView.Adapter<QuickCountryAdapter.ViewHolder>() {

        class ViewHolder(val view: View) : RecyclerView.ViewHolder(view)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_quick_country, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val country = countries[position]
            val chip = holder.view as com.google.android.material.chip.Chip
            chip.text = "${country.flagEmoji} ${country.code.uppercase()}"
            chip.setOnClickListener { onSelect(country) }
        }

        override fun getItemCount() = countries.size
    }

    private class SkuAdapter(
        private val items: List<SkuInfo>
    ) : RecyclerView.Adapter<SkuAdapter.ViewHolder>() {

        class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val tvTypeName: TextView = view.findViewById(R.id.tv_type_name)
            val tvPrice: TextView = view.findViewById(R.id.tv_price)
            val tvSkuId: TextView = view.findViewById(R.id.tv_sku_id)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_sku_info, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val item = items[position]
            holder.tvTypeName.text = item.typeName
            holder.tvPrice.text = "${item.currencySymbol}${item.price}"
            holder.tvSkuId.text = item.skuId
        }

        override fun getItemCount() = items.size
    }

    private class CategoryHeader(val title: String)

    private class PageListAdapter(
        private val items: List<Any>,
        private val onPageClick: (BillingTestPageEntry) -> Unit
    ) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

        companion object {
            private const val TYPE_HEADER = 0
            private const val TYPE_PAGE = 1
        }

        override fun getItemViewType(position: Int): Int {
            return if (items[position] is CategoryHeader) TYPE_HEADER else TYPE_PAGE
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            return if (viewType == TYPE_HEADER) {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_page_category_header, parent, false)
                HeaderViewHolder(view)
            } else {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_billing_page, parent, false)
                PageViewHolder(view)
            }
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            val item = items[position]
            if (item is CategoryHeader) {
                (holder as HeaderViewHolder).tvCategory.text = item.title
            } else if (item is BillingTestPageEntry) {
                val vh = holder as PageViewHolder
                vh.tvName.text = item.name
                if (item.type == BillingPageType.DIALOG) {
                    vh.tvType.visibility = View.VISIBLE
                    vh.tvType.text = "Dialog"
                } else {
                    vh.tvType.visibility = View.GONE
                }
                if (item.activityClassName.isNotEmpty()) {
                    vh.tvClass.visibility = View.VISIBLE
                    vh.tvClass.text = item.activityClassName.substringAfterLast(".")
                } else {
                    vh.tvClass.visibility = View.GONE
                }
                vh.itemView.setOnClickListener { onPageClick(item) }
            }
        }

        override fun getItemCount() = items.size

        class HeaderViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val tvCategory: TextView = view.findViewById(R.id.tv_category)
        }

        class PageViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val tvName: TextView = view.findViewById(R.id.tv_page_name)
            val tvType: TextView = view.findViewById(R.id.tv_page_type)
            val tvClass: TextView = view.findViewById(R.id.tv_page_class)
        }
    }
}
