package com.example.jigesh.youtubeclone

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.jigesh.youtubeclone.adapter.UploadVideoAdapter
import com.example.jigesh.youtubeclone.adapter.VideoAdapter
import com.example.jigesh.youtubeclone.databinding.FragmentViewChannelBinding
import com.example.jigesh.youtubeclone.model.UserModel
import com.example.jigesh.youtubeclone.model.VideoModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.util.UUID

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class ViewChannelFragment : Fragment() {
    private var param1: String? = null
    private var param2: String? = null

    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var storage: FirebaseStorage
    private lateinit var storageReference: StorageReference
    private val PICK_IMAGE_REQUEST = 1
    private lateinit var binding: FragmentViewChannelBinding

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
        binding = FragmentViewChannelBinding.inflate(inflater, container, false)
        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()
        storage = FirebaseStorage.getInstance()
        storageReference = storage.reference

        val currentUser = auth.currentUser
        if (currentUser != null) {
            firestore.collection("users").document(currentUser.uid)
                .get()
                .addOnSuccessListener { document ->
                    if (document != null) {
                        val username = document.getString("username")
                        val email = document.getString("email")
                        val profilePicUrl = document.getString("profilePic")

                        val atIndex = email?.indexOf('@') ?: -1
                        val usernameWithoutDomain = if (atIndex != -1) email?.substring(0, atIndex) else email
                        val displayUsername = "@$usernameWithoutDomain"

                        binding.username.text = username
                        binding.usernameGmail.text = displayUsername

                        if (!profilePicUrl.isNullOrEmpty()) {
                            Glide.with(this)
                                .load(profilePicUrl)
                                .circleCrop()
                                .into(binding.profilePic)
                        }
                    }
                }
                .addOnFailureListener { exception ->
                    // Handle failure
                }

            firestore.collection("videos")
                .whereEqualTo("uploaderId", currentUser.uid)
                .get()
                .addOnSuccessListener { documents ->
                    if (!documents.isEmpty) {
                        // User has uploaded videos
                        val videoList = documents.toObjects(VideoModel::class.java)
                        binding.recyclerView.visibility = View.VISIBLE
                        binding.imageView2.visibility = View.GONE
                        binding.textView3.visibility = View.GONE
                        binding.textv.visibility = View.GONE
                        binding.create.visibility = View.GONE

                        setupRecyclerView(videoList)
                    } else {
                        // User has not uploaded any videos
                        binding.recyclerView.visibility = View.GONE
                        binding.imageView2.visibility = View.VISIBLE
                        binding.textView3.visibility = View.VISIBLE
                        binding.textv.visibility = View.VISIBLE
                        binding.create.visibility = View.VISIBLE
                    }
                }
                .addOnFailureListener { exception ->
                    // Handle failure
                }
        }

        binding.profilePic.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(intent, PICK_IMAGE_REQUEST)
        }

        return binding.root
    }

    private fun setupRecyclerView(videoList: List<VideoModel>) {
        val adapter = UploadVideoAdapter(videoList)
        binding.recyclerView.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        binding.recyclerView.adapter = adapter
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.data != null) {
            val imageUri: Uri = data.data!!

            Glide.with(this)
                .load(imageUri)
                .circleCrop()
                .into(binding.profilePic)

            val ref = storageReference.child("profile_pics/${UUID.randomUUID()}")
            ref.putFile(imageUri)
                .addOnSuccessListener {
                    ref.downloadUrl.addOnSuccessListener { uri ->
                        val profilePicUrl = uri.toString()

                        val currentUser = auth.currentUser
                        if (currentUser != null) {
                            firestore.collection("users").document(currentUser.uid)
                                .update("profilePic", profilePicUrl)
                                .addOnSuccessListener {
                                    val userModel = UserModel(
                                        id = currentUser.uid,
                                        email = currentUser.email ?: "",
                                        username = binding.username.text.toString(),
                                        profilePic = profilePicUrl
                                    )
                                    updateProfilePicInVideos(currentUser.uid, profilePicUrl)
                                }
                                .addOnFailureListener { exception ->
                                    // Handle failure
                                }
                        }
                    }
                }
                .addOnFailureListener {
                    // Handle failure
                }
        }
    }

    private fun updateProfilePicInVideos(userId: String, profilePicUrl: String) {
        firestore.collection("videos")
            .whereEqualTo("uploaderId", userId)
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    firestore.collection("videos").document(document.id)
                        .update("profilePic", profilePicUrl)
                        .addOnFailureListener { exception ->
                            // Handle failure
                        }
                }
            }
            .addOnFailureListener { exception ->
                // Handle failure
            }
    }

    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            ViewChannelFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}
