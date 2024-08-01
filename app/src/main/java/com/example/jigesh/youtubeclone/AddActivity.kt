package com.example.jigesh.youtubeclone

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.viewpager2.widget.ViewPager2

class AddActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_add)
        // Find TextViews by ID
        val textVideo: TextView = findViewById(R.id.text_video)
        val textShorts: TextView = findViewById(R.id.text_shorts)


        // Set onClickListeners
        textVideo.setOnClickListener {
           // textVideo.background = R.drawable.bghome
            val intent = Intent(this, CreateShortActivity::class.java)
            startActivity(intent)
        }

        textShorts.setOnClickListener {
            val intent = Intent(this, CreateVideoActivity::class.java)
            startActivity(intent)
        }

    }

}