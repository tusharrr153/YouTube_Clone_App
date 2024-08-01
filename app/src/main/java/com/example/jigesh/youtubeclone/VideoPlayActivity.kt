package com.example.jigesh.youtubeclone

import android.animation.ObjectAnimator
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.drawable.StateListDrawable
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import java.util.Date
import java.util.concurrent.TimeUnit

class VideoPlayActivity : AppCompatActivity() {

    private lateinit var player: ExoPlayer
    private lateinit var playerView: com.google.android.exoplayer2.ui.PlayerView
    private lateinit var likeButton: TextView
    private lateinit var dislikeButton: TextView
    private lateinit var subscribeButton: Button
    private lateinit var subscribeCount: TextView
    private lateinit var firestore: FirebaseFirestore
    private val currentUser = FirebaseAuth.getInstance().currentUser

    private lateinit var tvYoutuberName: TextView
    private lateinit var tvCaption: TextView
    private lateinit var tvSubscriberCount: TextView
    private lateinit var tvUploadTime: TextView
    private lateinit var totalView: TextView
    private lateinit var ivProfilePicture: ImageView

    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_video_play)

        sharedPreferences = getSharedPreferences("video_preferences", Context.MODE_PRIVATE)

        playerView = findViewById(R.id.playerView)
        likeButton = findViewById(R.id.likeButton)
        dislikeButton = findViewById(R.id.dislikeButton)
        subscribeButton = findViewById(R.id.btnSubscribe)

        tvYoutuberName = findViewById(R.id.tvYoutuberName)
        tvCaption = findViewById(R.id.tvCaption)
        tvSubscriberCount = findViewById(R.id.tvSubscriberCount)
        tvUploadTime = findViewById(R.id.tvUploadTime)
        totalView = findViewById(R.id.totalview)
        subscribeCount = findViewById(R.id.tvSubscriberCount)
        ivProfilePicture = findViewById(R.id.ivProfilePicture)

        firestore = FirebaseFirestore.getInstance()

        val videoUrl = intent.getStringExtra("VIDEO_URL")
        val videoId = intent.getStringExtra("VIDEO_ID")
        val videoCaption = intent.getStringExtra("VIDEO_CAPTION")
        val uploadTimeMillis = intent.getLongExtra("UPLOAD_TIME", 0)
        val uploadTime = Date(uploadTimeMillis)
        val totalViews = intent.getLongExtra("TOTAL_VIEWS", 0)

        setupPlayer(videoUrl)

        if (videoId != null) {
            fetchUploaderData(videoId)
            checkSubscriptionStatus()
            incrementViewCount(videoId)
            restoreLikeState(videoId)
        }

        likeButton.setOnClickListener {
            if (videoId != null) {
                updateLikeDislikeCount(videoId, "like")
            }
        }

        dislikeButton.setOnClickListener {
            if (videoId != null) {
                updateLikeDislikeCount(videoId, "dislike")
            }
        }

        subscribeButton.setOnClickListener {
            val channelName = tvYoutuberName.text.toString()
            if (channelName.isNotEmpty() && videoId != null) {
                handleSubscribe(channelName, videoId)
            }
        }

        updateLikeButtonDrawable()

        val youtuberFrame = findViewById<LinearLayout>(R.id.youtuberdata)
        youtuberFrame.setOnClickListener {
            val channelname = findViewById<TextView>(R.id.tvYoutuberName).text.toString()
            val intent = Intent(this, YouTuberDetailActivity::class.java)
            intent.putExtra("CHANNEL_NAME", channelname)
            startActivity(intent)
        }

    }

    private fun fetchUploaderData(videoId: String) {
        firestore.collection("videos").document(videoId).get()
            .addOnSuccessListener { document ->
                val uploaderId = document.getString("uploaderId")
                val caption = document.getString("caption")
                tvCaption.text = caption ?: "No caption available"
                if (uploaderId != null) {
                    firestore.collection("users").document(uploaderId).get()
                        .addOnSuccessListener { userDocument ->
                            val channelName = userDocument.getString("channelname")
                            val profilePic = userDocument.getString("profilePic")
                            val subscriberCount = userDocument.getLong("subscriberCount")
                            bindData(channelName, profilePic, subscriberCount)
                        }
                }
            }
    }

    private fun updateLikeButtonDrawable() {
        val likeDrawable = ContextCompat.getDrawable(this, R.drawable.like2)
        val likeFilledDrawable = ContextCompat.getDrawable(this, R.drawable.like2)

        val stateListDrawable = StateListDrawable()
        stateListDrawable.addState(intArrayOf(android.R.attr.state_selected), likeFilledDrawable)
        stateListDrawable.addState(intArrayOf(), likeDrawable)

        likeButton.setCompoundDrawablesWithIntrinsicBounds(stateListDrawable, null, null, null)
    }

    private fun setupPlayer(videoUrl: String?) {
        player = ExoPlayer.Builder(this).build()
        playerView.player = player

        videoUrl?.let {
            val mediaItem = MediaItem.fromUri(Uri.parse(it))
            player.setMediaItem(mediaItem)
            player.prepare()
            player.playWhenReady = true
        }
    }

    private fun bindData(channelName: String?, profilePic: String?, subscriberCount: Long?) {
        tvYoutuberName.text = channelName ?: "Unknown channel"
        tvSubscriberCount.text = "$subscriberCount subscribers"

        profilePic?.let {
            Glide.with(this)
                .load(it)
                .placeholder(R.drawable.profile)
                .error(R.drawable.profile)
                .apply(RequestOptions.circleCropTransform())
                .into(ivProfilePicture)
        }

        firestore.collection("videos").document(intent.getStringExtra("VIDEO_ID")!!).get()
            .addOnSuccessListener { document ->
                val likeCount = document.getLong("likeCount") ?: 0L
                likeButton.text = " $likeCount Likes"
                checkSubscriptionStatus()
            }
    }

    private fun incrementViewCount(videoId: String) {
        val userId = currentUser?.uid ?: return

        val viewsCollection = firestore.collection("videos").document(videoId).collection("views")

        viewsCollection.document(userId).get().addOnSuccessListener { document ->
            if (!document.exists()) {
                viewsCollection.document(userId).set(mapOf("viewed" to true)).addOnSuccessListener {
                    firestore.collection("videos").document(videoId)
                        .update("view", FieldValue.increment(1))
                        .addOnSuccessListener {
                            firestore.collection("videos").document(videoId).get()
                                .addOnSuccessListener { videoDocument ->
                                    val updatedViews = videoDocument.getLong("view") ?: 0L
                                    totalView.text = "$updatedViews views"
                                }
                        }
                }
            } else {
                firestore.collection("videos").document(videoId).get()
                    .addOnSuccessListener { videoDocument ->
                        val currentViews = videoDocument.getLong("view") ?: 0L
                        totalView.text = "$currentViews views"
                    }
            }
        }
    }

    private fun updateLikeDislikeCount(videoId: String, action: String) {
        val userId = currentUser?.uid ?: return

        val videoRef = firestore.collection("videos").document(videoId)
        val userLikesRef = videoRef.collection("likes").document(userId)

        userLikesRef.get().addOnSuccessListener { document ->
            if (!document.exists()) {
                when (action) {
                    "like" -> {
                        animateLikeButton()
                        incrementLikeCount(videoRef)
                        userLikesRef.set(mapOf("action" to "like"))
                        likeButton.isSelected = true
                        likeButton.setTextColor(ContextCompat.getColor(this, R.color.blue))
                        saveLikeState(videoId, true)
                    }

                    "dislike" -> {
                        animateDislikeButton()
                        incrementDislikeCount(videoRef)
                        userLikesRef.set(mapOf("action" to "dislike"))
                        likeButton.isSelected = false
                        likeButton.setTextColor(ContextCompat.getColor(this, R.color.white))
                        saveLikeState(videoId, false)
                    }
                }
            } else {
                val userAction = document.getString("action")
                when {
                    userAction == "like" && action == "like" -> {
                        decrementLikeCount(videoRef)
                        userLikesRef.delete()
                        likeButton.isSelected = false
                        likeButton.setTextColor(ContextCompat.getColor(this, R.color.white))
                        saveLikeState(videoId, false)
                    }

                    userAction == "dislike" && action == "dislike" -> {
                        decrementDislikeCount(videoRef)
                        userLikesRef.delete()
                        likeButton.isSelected = false
                        likeButton.setTextColor(ContextCompat.getColor(this, R.color.white))
                        saveLikeState(videoId, false)
                    }

                    userAction == "like" && action == "dislike" -> {
                        decrementLikeCount(videoRef)
                        incrementDislikeCount(videoRef)
                        userLikesRef.set(mapOf("action" to "dislike"))
                        likeButton.isSelected = false
                        likeButton.setTextColor(ContextCompat.getColor(this, R.color.white))
                        saveLikeState(videoId, false)
                    }

                    userAction == "dislike" && action == "like" -> {
                        decrementDislikeCount(videoRef)
                        incrementLikeCount(videoRef)
                        userLikesRef.set(mapOf("action" to "like"))
                        likeButton.isSelected = true
                        likeButton.setTextColor(ContextCompat.getColor(this, R.color.blue))
                        saveLikeState(videoId, true)
                    }
                }
            }
        }
    }

    private fun animateLikeButton() {
        ObjectAnimator.ofFloat(likeButton, "scaleX", 1.2f).apply {
            duration = 300
            repeatCount = 1
            repeatMode = ObjectAnimator.REVERSE
            start()
        }
        ObjectAnimator.ofFloat(likeButton, "scaleY", 1.2f).apply {
            duration = 300
            repeatCount = 1
            repeatMode = ObjectAnimator.REVERSE
            start()
        }
    }

    private fun animateDislikeButton() {
        ObjectAnimator.ofFloat(dislikeButton, "scaleX", 1.2f).apply {
            duration = 300
            repeatCount = 1
            repeatMode = ObjectAnimator.REVERSE
            start()
        }
        ObjectAnimator.ofFloat(dislikeButton, "scaleY", 1.2f).apply {
            duration = 300
            repeatCount = 1
            repeatMode = ObjectAnimator.REVERSE
            start()
        }
    }

    private fun incrementLikeCount(videoRef: DocumentReference) {
        videoRef.update("likeCount", FieldValue.increment(1)).addOnSuccessListener {
            videoRef.get().addOnSuccessListener { document ->
                val likeCount = document.getLong("likeCount") ?: 0L
                likeButton.text = " $likeCount Likes"
            }
        }
    }

    private fun decrementLikeCount(videoRef: DocumentReference) {
        videoRef.update("likeCount", FieldValue.increment(-1)).addOnSuccessListener {
            videoRef.get().addOnSuccessListener { document ->
                val likeCount = document.getLong("likeCount") ?: 0L
                likeButton.text = " $likeCount Likes"
            }
        }
    }

    private fun incrementDislikeCount(videoRef: DocumentReference) {
        videoRef.update("dislikeCount", FieldValue.increment(1))
    }

    private fun decrementDislikeCount(videoRef: DocumentReference) {
        videoRef.update("dislikeCount", FieldValue.increment(-1))
    }

    private fun handleSubscribe(channelName: String, videoId: String) {
        val userId = currentUser?.uid ?: return

        val userSubscriptionsRef = firestore.collection("users").document(userId)
            .collection("subscriptions").document(channelName)

        userSubscriptionsRef.get().addOnSuccessListener { snapshot ->
            if (snapshot.exists()) {
                userSubscriptionsRef.delete().addOnSuccessListener {
                    decrementSubscriberCount(channelName, videoId)
                    subscribeButton.setTextColor(ContextCompat.getColor(this, R.color.black))
                    subscribeButton.text = "Subscribe"
                    checkSubscriptionStatus()
                }
            } else {
                firestore.collection("videos").document(videoId).get()
                    .addOnSuccessListener { document ->
                        val uploaderId = document.getString("uploaderId") ?: return@addOnSuccessListener
                        val uploaderRef = firestore.collection("users").document(uploaderId)

                        uploaderRef.get().addOnSuccessListener { uploaderDocument ->
                            val uploaderName = uploaderDocument.getString("username")
                            val uploaderProfilePic = uploaderDocument.getString("profilePic")

                            val subscriptionData = hashMapOf(
                                "channelName" to channelName,
                                "profilePic" to uploaderProfilePic,
                                "uploaderName" to uploaderName,
                                "uploaderId" to uploaderId
                            )

                            userSubscriptionsRef.set(subscriptionData).addOnSuccessListener {
                                incrementSubscriberCount(channelName, videoId)
                                subscribeButton.setTextColor(ContextCompat.getColor(this, R.color.red))
                                subscribeButton.text = "Subscribed"
                                checkSubscriptionStatus()
                            }
                        }
                    }
            }
        }
    }

    private fun incrementSubscriberCount(channelName: String, videoId: String) {
        val userId = currentUser?.uid ?: return

        firestore.collection("videos").document(videoId).get().addOnSuccessListener { document ->
            val uploaderId = document.getString("uploaderId") ?: return@addOnSuccessListener
            firestore.collection("users").document(uploaderId)
                .update("subscriberCount", FieldValue.increment(1))
                .addOnSuccessListener {
                    firestore.collection("users").document(uploaderId).get()
                        .addOnSuccessListener { userDocument ->
                            val updatedSubscriberCount = userDocument.getLong("subscriberCount") ?: 0L
                            subscribeCount.text = "$updatedSubscriberCount subscribers"
                        }
                }
        }
    }

    private fun decrementSubscriberCount(channelName: String, videoId: String) {
        val userId = currentUser?.uid ?: return

        firestore.collection("videos").document(videoId).get().addOnSuccessListener { document ->
            val uploaderId = document.getString("uploaderId") ?: return@addOnSuccessListener
            firestore.collection("users").document(uploaderId)
                .update("subscriberCount", FieldValue.increment(-1))
                .addOnSuccessListener {
                    firestore.collection("users").document(uploaderId).get()
                        .addOnSuccessListener { userDocument ->
                            val updatedSubscriberCount = userDocument.getLong("subscriberCount") ?: 0L
                            subscribeCount.text = "$updatedSubscriberCount subscribers"
                        }
                }
        }
    }

    private fun checkSubscriptionStatus() {
        val channelName = tvYoutuberName.text.toString()
        val userId = currentUser?.uid ?: return

        Log.d(TAG, "Checking subscription status for channel: $channelName")

        val userSubscriptionsRef = firestore.collection("users").document(userId)
            .collection("subscriptions").document(channelName)

        userSubscriptionsRef.get().addOnSuccessListener { snapshot ->
            if (snapshot.exists()) {
                subscribeButton.setTextColor(ContextCompat.getColor(this, R.color.red))
                subscribeButton.text = "Subscribed"
                Log.d(TAG, "User is subscribed to $channelName")
            } else {
                subscribeButton.setTextColor(ContextCompat.getColor(this, R.color.black))
                subscribeButton.text = "Subscribe"
                Log.d(TAG, "User is NOT subscribed to $channelName")
            }
        }.addOnFailureListener { e ->
            Log.e(TAG, "Failed to check subscription status: ${e.message}", e)
        }
    }

    private var subscriptionListener: ListenerRegistration? = null

    private fun setupSubscriptionListener() {
        val userId = currentUser?.uid ?: return
        val channelName = tvYoutuberName.text.toString()

        val userSubscriptionsRef = firestore.collection("users").document(userId)
            .collection("subscriptions").document(channelName)

        subscriptionListener = userSubscriptionsRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.e(TAG, "Error listening to subscription status: ${error.message}", error)
                return@addSnapshotListener
            }

            if (snapshot != null && snapshot.exists()) {
                subscribeButton.setTextColor(ContextCompat.getColor(this, R.color.red))
                subscribeButton.text = "Subscribed"
                Log.d(TAG, "User is subscribed to $channelName")
            } else {
                subscribeButton.setTextColor(ContextCompat.getColor(this, R.color.black))
                subscribeButton.text = "Subscribe"
                Log.d(TAG, "User is NOT subscribed to $channelName")
            }
        }
    }

    private fun saveLikeState(videoId: String, isLiked: Boolean) {
        sharedPreferences.edit().putBoolean("like_$videoId", isLiked).apply()
    }

    private fun restoreLikeState(videoId: String) {
        val isLiked = sharedPreferences.getBoolean("like_$videoId", false)
        likeButton.isSelected = isLiked
        likeButton.setTextColor(
            if (isLiked) ContextCompat.getColor(this, R.color.blue)
            else ContextCompat.getColor(this, R.color.white)
        )
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "onResume called")
        val videoId = intent.getStringExtra("VIDEO_ID")
        if (videoId != null) {
            restoreLikeState(videoId)
        }
        checkSubscriptionStatus()
    }

    override fun onStart() {
        super.onStart()
        Log.d(TAG, "onStart called")
        checkSubscriptionStatus()
        setupSubscriptionListener()
    }

    override fun onPause() {
        super.onPause()
        Log.d(TAG, "onPause called")
        player.pause()
    }

    override fun onStop() {
        super.onStop()
        Log.d(TAG, "onStop called")
        subscriptionListener?.remove()
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "onDestroy called")
        player.release()
    }
}
