package com.example.jigesh.youtubeclone

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.jigesh.youtubeclone.adapter.SubscriptionAdapter
import com.example.jigesh.youtubeclone.adapter.VideoAdapter
import com.example.jigesh.youtubeclone.model.Subscription
import com.example.jigesh.youtubeclone.model.VideoModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class SubscriptionFragment : Fragment(), SubscriptionAdapter.OnItemClickListener {
    private lateinit var subscriptionRecyclerView: RecyclerView
    private lateinit var subscriptionAdapter: SubscriptionAdapter
    private lateinit var subscriptions: MutableList<Subscription>

    private lateinit var videoRecyclerView: RecyclerView
    private lateinit var videoAdapter: VideoAdapter
    private lateinit var videos: MutableList<VideoModel>

    private lateinit var firestore: FirebaseFirestore
    private val currentUser = FirebaseAuth.getInstance().currentUser

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_subscription, container, false)
        subscriptionRecyclerView = view.findViewById(R.id.subscription_recycler)
        subscriptionRecyclerView.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        subscriptions = mutableListOf()
        subscriptionAdapter = SubscriptionAdapter(subscriptions, this)
        subscriptionRecyclerView.adapter = subscriptionAdapter

        videoRecyclerView = view.findViewById(R.id.youtuber_video)
        videoRecyclerView.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        videos = mutableListOf()
        videoAdapter = VideoAdapter(videos)
        videoRecyclerView.adapter = videoAdapter

        firestore = FirebaseFirestore.getInstance()
        loadSubscriptions()

        return view
    }

    private fun loadSubscriptions() {
        val userId = currentUser?.uid ?: return
        firestore.collection("users").document(userId).collection("subscriptions")
            .get()
            .addOnSuccessListener { snapshot ->
                subscriptions.clear()
                val uploaderIds = mutableListOf<String>()
                for (document in snapshot) {
                    val channelName = document.getString("channelName") ?: "Unknown"
                    val profilePic = document.getString("profilePic") ?: ""
                    subscriptions.add(Subscription(channelName, profilePic))

                    val uploaderId = document.getString("uploaderId")
                    if (uploaderId != null) {
                        uploaderIds.add(uploaderId)
                    }
                }
                subscriptionAdapter.notifyDataSetChanged()
                Log.d("SubscriptionFragment", "Subscriptions loaded: ${subscriptions.size}")
                Log.d("SubscriptionFragment", "Uploader IDs: $uploaderIds")
                loadVideosFromChannels(uploaderIds)
            }
            .addOnFailureListener { e ->
                Log.e("SubscriptionFragment", "Failed to load subscriptions: ${e.message}", e)
            }
    }

    private fun loadVideosFromChannels(uploaderIds: List<String>) {
        if (uploaderIds.isEmpty()) return

        firestore.collection("videos")
            .whereIn("uploaderId", uploaderIds)
            .get()
            .addOnSuccessListener { snapshot ->
                videos.clear()
                for (document in snapshot.documents) {
                    val video = document.toObject(VideoModel::class.java)
                    if (video != null) {
                        videos.add(video)
                        Log.d("SubscriptionFragment", "Video added: ${video.caption}")
                    }
                }
                videoAdapter.notifyDataSetChanged()
                Log.d("SubscriptionFragment", "Videos loaded: ${videos.size}")
            }
            .addOnFailureListener { e ->
                Log.e("SubscriptionFragment", "Failed to load videos: ${e.message}", e)
            }
    }

    private fun loadVideosForChannel(channelName: String) {
        Log.d("SubscriptionFragment", "Loading videos for channel: $channelName")
        firestore.collection("videos")
            .whereEqualTo("channelname", channelName)
            .get()
            .addOnSuccessListener { snapshot ->
                videos.clear()
                for (document in snapshot.documents) {
                    val video = document.toObject(VideoModel::class.java)
                    if (video != null) {
                        videos.add(video)
                        Log.d("SubscriptionFragment", "Video added: ${video.caption}")
                    }
                }
                videoAdapter.notifyDataSetChanged()
                Log.d("SubscriptionFragment", "Videos loaded for channel: ${videos.size}")
            }
            .addOnFailureListener { e ->
                Log.e("SubscriptionFragment", "Failed to load videos: ${e.message}", e)
            }
    }

    override fun onItemClick(subscription: Subscription) {
        Log.d("SubscriptionFragment", "Subscription clicked: ${subscription.channelName}")
        loadVideosForChannel(subscription.channelName)
    }
}
