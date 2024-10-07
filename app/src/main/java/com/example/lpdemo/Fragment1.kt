package com.example.lpdemo

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment

class FirstFragment:Fragment(R.layout.activity_oxy) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Toast.makeText(context, "First Fragment Launched", Toast.LENGTH_SHORT).show()
    }
}