package com.example.jigesh.youtubeclone

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.example.jigesh.youtubeclone.databinding.FragmentProfileBinding
import com.example.jigesh.youtubeclone.model.UserModel
import com.example.jigesh.youtubeclone.model.VideoModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.util.*

class ProfileFragment : Fragment() {

    private lateinit var binding: FragmentProfileBinding
    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var storage: FirebaseStorage
    private lateinit var storageReference: StorageReference
    private val PICK_IMAGE_REQUEST = 1


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentProfileBinding.inflate(inflater, container, false)

        // Initialize Firebase instances
        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()
        storage = FirebaseStorage.getInstance()
        storageReference = storage.reference

        // Check if the user is logged in
        val currentUser = auth.currentUser
        if (currentUser != null) {
            // Retrieve user data from Firestore
            firestore.collection("users").document(currentUser.uid)
                .get()
                .addOnSuccessListener { document ->
                    if (document != null) {
                        // Retrieve username and email from Firestore document
                        val username = document.getString("username")
                        val email = document.getString("email")
                        val profilePicUrl = document.getString("profilePic")

                        // Extract username from email
                        val atIndex = email?.indexOf('@') ?: -1
                        val usernameWithoutDomain = if (atIndex != -1) email?.substring(0, atIndex) else email

                        // Construct username with "@" symbol
                        val displayUsername = "@$usernameWithoutDomain"

                        // Set username and email to TextViews
                        binding.username.text = username
                        binding.usernameGmail.text = displayUsername

                        // Load profile picture if available
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
            firestore.collection("videos").document(currentUser.uid)
                .get()
                .addOnSuccessListener { document ->
                    if (document != null) {

                        val profilePicUrl = document.getString("profilePic")

                        // Load profile picture if available
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
        }

        // Set click listener for "Your videos" section
        binding.yourvideos.setOnClickListener {
            startActivity(Intent(requireContext(), YourVideosActivity::class.java))
        }

        // Set click listener for profile picture
        binding.profilePic.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(intent, PICK_IMAGE_REQUEST)
        }

        binding.viewchannel.setOnClickListener {
            switchToViewChannelFragment()
        }

        return binding.root
    }

    private fun switchToViewChannelFragment() {
        val viewChannelFragment = ViewChannelFragment()
        val transaction = parentFragmentManager.beginTransaction()
        transaction.replace(R.id.framelayout, viewChannelFragment)
        transaction.addToBackStack(null)  // Optional: if you want to add the transaction to the back stack
        transaction.commit()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.data != null) {
            val imageUri: Uri = data.data!!

            // Display the selected image as a circular cropped image using Glide
            Glide.with(this)
                .load(imageUri)
                .circleCrop()
                .into(binding.profilePic)

            // Upload the image to Firebase Storage
            val ref = storageReference.child("profile_pics/${UUID.randomUUID()}")
            ref.putFile(imageUri)
                .addOnSuccessListener {
                    ref.downloadUrl.addOnSuccessListener { uri ->
                        val profilePicUrl = uri.toString()

                        // Update Firestore with the new profilePic URL
                        val currentUser = auth.currentUser
                        if (currentUser != null) {
                            firestore.collection("users").document(currentUser.uid)
                                .update("profilePic", profilePicUrl)
                                .addOnSuccessListener {
                                    // Optionally, update the UserModel if you use it locally
                                    val userModel = UserModel(
                                        id = currentUser.uid,
                                        email = currentUser.email ?: "",
                                        username = binding.username.text.toString(),
                                        profilePic = profilePicUrl
                                        // Initialize other fields as needed
                                    )
                                    // Save the userModel instance as needed

                                    // Update profilePic in VideoModel for the current user
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
        // Query Firestore for videos uploaded by the current user
        firestore.collection("videos")
            .whereEqualTo("uploaderId", userId)
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    // Update the profilePic field in each video document
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
}
