package com.example.lpdemo

import android.util.Log
import androidx.fragment.app.Fragment

class SecondFragment:Fragment(R.layout.activity_main) {
    override fun onResume() {
        super.onResume()
        Log.d("Test Run", "Test Run")
        // Call initView() from MainActivity when the fragment is resumed
        (activity as? MainActivity)?.initView()
    }
}