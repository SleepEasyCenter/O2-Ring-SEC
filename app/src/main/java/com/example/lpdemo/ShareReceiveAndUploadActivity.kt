package com.example.lpdemo

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Parcelable
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toFile
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.lpdemo.databinding.ActivityShareReceiveAndUploadBinding
import java.io.File


class ShareReceiveAndUploadActivity : AppCompatActivity() {

    lateinit var binding: ActivityShareReceiveAndUploadBinding;

    val TAG: String = "ShareReceiveAndUploadActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityShareReceiveAndUploadBinding.inflate(layoutInflater);
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar);
        actionBar?.setDisplayHomeAsUpEnabled(true)
        when {
            intent?.action == Intent.ACTION_SEND -> {
                if ("text/csv" == intent.type) {
                    handleSendCSV(intent)
                } else if (intent.type == "application/pdf") {
                    handleSendPdf(intent)
                }
            }

            else -> {

            }
        }

    }

    fun handleSendCSV(intent: Intent) {
        intent.getStringExtra(Intent.EXTRA_TEXT)?.let {
            // Update UI to reflect text being shared
        }
    }

    fun handleSendPdf(intent: Intent) {
        (intent.getParcelableExtra<Parcelable>(Intent.EXTRA_STREAM) as? Uri)?.let { data ->
            Log.d(TAG, "Received pdf:" + data.toString())
            val file = data.toFile()
            // todo upload to database -> need to figure out authentication
            // file.readBytes()
        }

    }

    override fun onSupportNavigateUp(): Boolean {
        finish();
        return false;
    }
}