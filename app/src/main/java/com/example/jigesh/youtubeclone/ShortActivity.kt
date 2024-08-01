package com.example.jigesh.youtubeclone

import android.net.Uri
import android.os.Bundle
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.VideoView
import androidx.appcompat.app.AppCompatActivity
import com.example.jigesh.youtubeclone.databinding.ActivityShortBinding

class ShortActivity : AppCompatActivity() {
    private lateinit var binding: ActivityShortBinding
    private lateinit var videoView: VideoView
    private lateinit var progressBar: ProgressBar
    private lateinit var pauseIcon: ImageView
    private lateinit var usernameView: TextView
    private lateinit var captionView: TextView
    private lateinit var likeButton: ImageView
    private lateinit var dislikeButton: ImageView
    private lateinit var likeCount: TextView
    private lateinit var dislikeCount: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityShortBinding.inflate(layoutInflater)
        setContentView(binding.root)

        videoView = findViewById(R.id.video_view)
        progressBar = findViewById(R.id.progress_bar)
        pauseIcon = findViewById(R.id.pause_icon)
        usernameView = findViewById(R.id.username_view)
        captionView = findViewById(R.id.caption_view)
        likeButton = findViewById(R.id.like_button)
        dislikeButton = findViewById(R.id.dislike_button)
        likeCount = findViewById(R.id.like_count)
        dislikeCount = findViewById(R.id.dislike_count)

        setupVideoPlayer()
    }

    private fun setupVideoPlayer() {
        val videoUrl = intent.getStringExtra("VIDEO_URL")
        val username = intent.getStringExtra("CHANNEL_NAME")
        val caption = intent.getStringExtra("CAPTION")

        if (videoUrl != null) {
            progressBar.visibility = ProgressBar.VISIBLE
            videoView.setVideoURI(Uri.parse(videoUrl))

            videoView.setOnPreparedListener {
                progressBar.visibility = ProgressBar.GONE
                it.start()
            }

            videoView.setOnCompletionListener {
                // Handle video completion
            }
        }

        usernameView.text = username
        captionView.text = caption
    }

    override fun onDestroy() {
        super.onDestroy()
        videoView.stopPlayback()
    }
}
