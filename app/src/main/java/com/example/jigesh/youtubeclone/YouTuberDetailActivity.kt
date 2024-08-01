package com.example.jigesh.youtubeclone

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

class YouTuberDetailActivity : AppCompatActivity() {

    private lateinit var subscribeButton: Button
    private lateinit var firestore: FirebaseFirestore
    private var isSubscribed: Boolean = false

    private lateinit var viewPager: ViewPager2
    private lateinit var textView4: TextView
    private lateinit var textView5: TextView
    private lateinit var textView6: TextView
    private lateinit var textView7: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_youtuberdetail)

        firestore = FirebaseFirestore.getInstance()

        val channelName = intent.getStringExtra("CHANNEL_NAME") ?: return

        val channelNameTextView = findViewById<TextView>(R.id.youtuber_channelname)
        channelNameTextView.text = channelName

        viewPager = findViewById(R.id.viewPager)
        textView4 = findViewById(R.id.textView4)
        textView5 = findViewById(R.id.textView5)
        textView6 = findViewById(R.id.textView6)
        textView7 = findViewById(R.id.textView7)

        val adapter = ViewPagerAdapter(this)
        adapter.addFragment(UserhomeFragment().apply {
            arguments = Bundle().apply {
                putString("CHANNEL_ID", channelName)
            }
        }, "Home")
        adapter.addFragment(UserShortFragment().apply {
            arguments = Bundle().apply {
                putString("CHANNEL_ID", channelName)
            }
        }, "Shorts")
        adapter.addFragment(UserPlaylistFragment(), "Playlists")
        adapter.addFragment(UserCommunityFragment(), "Community")
        // Add more fragments as needed

        viewPager.adapter = adapter

        textView4.setOnClickListener { viewPager.currentItem = 0 }
        textView5.setOnClickListener { viewPager.currentItem = 1 }
        textView6.setOnClickListener { viewPager.currentItem = 2 }
        textView7.setOnClickListener { viewPager.currentItem = 3 }

        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                updateTabSelection(position)
            }
        })

        subscribeButton = findViewById(R.id.subscribebtn)
        subscribeButton.setOnClickListener {
            handleSubscribe(channelName)
        }

        // Fetch YouTuber details
        fetchYoutuberDetails(channelName)

        // Check subscription status
        checkSubscriptionStatus(channelName)
    }

    private fun updateTabSelection(position: Int) {
        val selectedColor = ContextCompat.getColor(this, R.color.white) // Define in colors.xml
        val defaultColor = ContextCompat.getColor(this, R.color.gray) // Define in colors.xml

        textView4.setTextColor(if (position == 0) selectedColor else defaultColor)
        textView5.setTextColor(if (position == 1) selectedColor else defaultColor)
        textView6.setTextColor(if (position == 2) selectedColor else defaultColor)
        textView7.setTextColor(if (position == 3) selectedColor else defaultColor)
    }

    private fun fetchYoutuberDetails(channelName: String) {
        firestore.collection("users").whereEqualTo("channelname", channelName).get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    findViewById<TextView>(R.id.youtuber_email).text = document.getString("email")
                    val profilePicUrl = document.getString("profilePic")
                    val profilePicImageView = findViewById<ImageView>(R.id.youtuber_profilepic)
                    Glide.with(this)
                        .load(profilePicUrl)
                        .apply(RequestOptions.circleCropTransform())
                        .into(profilePicImageView)
                    findViewById<TextView>(R.id.youtuber_subscribers).text =
                        "${document.getLong("subscriberCount")} subscribers"
                    findViewById<TextView>(R.id.youtuber_totalvideos).text =
                        "${document.getLong("videocount")} videos"
                }
            }
            .addOnFailureListener {
                // Handle any errors
            }
    }

    private fun checkSubscriptionStatus(channelId: String) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        firestore.collection("users").document(userId)
            .collection("subscriptions").document(channelId)
            .get()
            .addOnSuccessListener { document ->
                isSubscribed = document.exists()
                updateSubscriptionButton(isSubscribed)
            }
    }

    private fun handleSubscribe(channelId: String) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        val userSubscriptionsRef = firestore.collection("users").document(userId)
            .collection("subscriptions").document(channelId)

        if (isSubscribed) {
            userSubscriptionsRef.delete().addOnSuccessListener {
                updateSubscriptionButton(false)
                decrementSubscriberCount(channelId)
            }
        } else {
            userSubscriptionsRef.set(mapOf("subscribed" to true)).addOnSuccessListener {
                updateSubscriptionButton(true)
                incrementSubscriberCount(channelId)
            }
        }
    }

    private fun updateSubscriptionButton(isSubscribed: Boolean) {
        this.isSubscribed = isSubscribed
        subscribeButton.text = if (isSubscribed) "Subscribed" else "Subscribe"
        subscribeButton.setTextColor(
            if (isSubscribed) ContextCompat.getColor(this, R.color.red)
            else ContextCompat.getColor(this, R.color.black)
        )
    }

    private fun incrementSubscriberCount(channelId: String) {
        firestore.collection("users").whereEqualTo("channelName", channelId).get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    firestore.collection("users").document(document.id)
                        .update("subscribers", FieldValue.increment(1))
                }
            }
    }

    private fun decrementSubscriberCount(channelId: String) {
        firestore.collection("users").whereEqualTo("channelName", channelId).get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    firestore.collection("users").document(document.id)
                        .update("subscribers", FieldValue.increment(-1))
                }
            }
    }
}
