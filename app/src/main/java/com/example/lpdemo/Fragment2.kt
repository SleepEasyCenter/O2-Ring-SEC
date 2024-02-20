package com.example.lpdemo

import androidx.fragment.app.Fragment

class SecondFragment:Fragment(R.layout.activity_main) {
    override fun onResume() {
        super.onResume()
        // Call initView() from MainActivity when the fragment is resumed
        (activity as? MainActivity)?.initView()
    }
}