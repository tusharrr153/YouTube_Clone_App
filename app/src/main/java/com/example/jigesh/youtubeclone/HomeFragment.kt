package com.example.jigesh.youtubeclone


import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.jigesh.youtubeclone.R
import com.example.jigesh.youtubeclone.adapter.VideoAdapter
import com.example.jigesh.youtubeclone.model.VideoModel
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.toObject

class HomeFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var videoAdapter: VideoAdapter
    private lateinit var videoList: MutableList<VideoModel>
    private val db = FirebaseFirestore.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        recyclerView = view.findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(context)
        videoList = mutableListOf()
        videoAdapter = VideoAdapter(videoList)
        recyclerView.adapter = videoAdapter

        fetchVideos()

        return view
    }

    private fun fetchVideos() {
        db.collection("videos")
            .orderBy("createdTime", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    val video = document.toObject<VideoModel>()
                    videoList.add(video)
                }
                videoAdapter.notifyDataSetChanged()
            }
            .addOnFailureListener { exception ->
                Log.e("HomeFragment", "Error fetching videos: ", exception)
            }
    }
}
