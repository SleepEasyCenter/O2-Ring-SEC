package com.example.lpdemo

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment

class SecondFragment:Fragment(R.layout.activity_main) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Toast.makeText(context, "Second Fragment Launched", Toast.LENGTH_SHORT).show()
    }

    override fun onResume() {
        super.onResume()
        // Call `initView()` safely only if the activity is an instance of `MainActivity`
        (activity as? MainActivity)?.initView()
    }
}