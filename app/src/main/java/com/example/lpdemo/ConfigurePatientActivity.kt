package com.example.lpdemo

import android.app.AlertDialog
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.lpdemo.databinding.ActivityConfigurePatientBinding
import com.example.lpdemo.dialogs.DialogUnsavedChangesWarning

class ConfigurePatientActivity : AppCompatActivity() {
    public lateinit var binding: ActivityConfigurePatientBinding;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityConfigurePatientBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar2);
        supportActionBar?.setTitle("Configure Patient")
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.show()

    }

    override fun onSupportNavigateUp(): Boolean {

        DialogUnsavedChangesWarning({ x -> if (x){finish()} }).show(supportFragmentManager,"UNSAVED_CHANGES_DIALOG")

        return false
    }
}