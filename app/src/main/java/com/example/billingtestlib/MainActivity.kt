package com.example.billingtestlib

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        try {
            val entryClass = Class.forName("com.example.billing.BillingTestCenterEntry")
            val instance = entryClass.getDeclaredField("INSTANCE").get(null)
            val setupMethod = entryClass.getDeclaredMethod("setup", View::class.java)
            setupMethod.invoke(instance, findViewById(R.id.main))
        } catch (e: Exception) {
            Log.d("BillingTest", "initView: "+e.message)
        }
    }
}