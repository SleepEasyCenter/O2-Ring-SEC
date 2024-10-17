package com.sleepeasycenter.o2ring_app

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.sleepeasycenter.o2ring_app.databinding.ActivityMainBinding


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