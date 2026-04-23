package com.example.billingtestlib

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.billing.test.annotation.BillingTestPage

@BillingTestPage(
    name = "SkuActivity2",
    category = "Type2",
    intentFactory = "com.example.billing.SkuActivity2Factory"
)
class SkuActivity2 : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_sku2)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        intent.getStringExtra("vipDiscount")?.let { vipDiscount ->
            findViewById<android.widget.TextView>(R.id.tv_vip_discount).text = "VIP Discount: $vipDiscount%"
        }
    }
}