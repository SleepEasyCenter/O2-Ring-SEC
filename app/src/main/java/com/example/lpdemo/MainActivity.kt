package com.example.lpdemo

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.lpdemo.databinding.ActivityMainBinding


class MainActivity : AppCompatActivity() {
    private val TAG: String = "MainActivity"
    private lateinit var binding: ActivityMainBinding
    lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navHostFragment = supportFragmentManager.findFragmentById(R.id.navhost) as NavHostFragment;
         navController = navHostFragment.navController;
        binding.bottomNavigationView.setupWithNavController(navController);

//        Log.d(TAG, "API USERNAME: " + BuildConfig.API_USERNAME)
//        Log.d(TAG, "API KEY: " +  BuildConfig.API_PASSWORD)
    }

    public fun startScanActivity(){
        val intent: Intent = Intent(this@MainActivity, DeviceScanActivity::class.java);
        startActivity(intent)
    }
    public fun startPatientEditActivity(){
        val intent: Intent = Intent(this@MainActivity, ConfigurePatientActivity::class.java);
        startActivity(intent)
    }
}