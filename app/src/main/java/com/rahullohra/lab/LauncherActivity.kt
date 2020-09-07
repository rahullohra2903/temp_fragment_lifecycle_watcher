package com.rahullohra.lab

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.rahullohra.lab.gratification.GratificationHomeActivity

class LauncherActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        launch()
        finish()
    }

    fun launch() {
        startActivity(Intent(this, GratificationHomeActivity::class.java))
    }
}