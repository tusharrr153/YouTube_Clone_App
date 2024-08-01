package com.example.jigesh.youtubeclone

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.jigesh.youtubeclone.adapter.VideoAdapter
import com.example.jigesh.youtubeclone.adapter.VideoListAdapter
import com.example.jigesh.youtubeclone.model.VideoModel
import com.google.firebase.firestore.FirebaseFirestore

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class UserhomeFragment : Fragment() {

    private var param1: String? = null
    private var param2: String? = null

    private lateinit var recyclerView: RecyclerView
    private lateinit var videoAdapter: VideoListAdapter
    private lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_userhome, container, false)

        recyclerView = view.findViewById(R.id.youtuber_view)
        recyclerView.layoutManager = LinearLayoutManager(context)
        videoAdapter = VideoListAdapter(listOf())
        recyclerView.adapter = videoAdapter

        firestore = FirebaseFirestore.getInstance()

        fetchVideos()

        return view
    }

    private fun fetchVideos() {
        val channelId = arguments?.getString("CHANNEL_ID") ?: return
        Log.d("UserhomeFragment", "Fetching videos for channel ID: $channelId")
        firestore.collection("videos")
            .whereEqualTo("channelname", channelId)
            .get()
            .addOnSuccessListener { documents ->
                val videoList = documents.map { it.toObject(VideoModel::class.java) }
                videoAdapter.updateVideos(videoList)
                Log.d("UserhomeFragment", "Videos fetched: ${videoList.size}")
            }
            .addOnFailureListener { e ->
                Log.e("UserhomeFragment", "Error fetching videos", e)
            }
    }

    companion object {

        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            UserhomeFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}