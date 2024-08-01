package com.example.jigesh.youtubeclone

import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.ArrayAdapter
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.example.jigesh.youtubeclone.databinding.ActivityCreateVideoBinding
import com.example.jigesh.youtubeclone.model.UserModel
import com.example.jigesh.youtubeclone.model.VideoModel
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.*

class CreateVideoActivity : AppCompatActivity() {
    lateinit var binding: ActivityCreateVideoBinding
    private var selectedVideoUri: Uri? = null
    private var selectedImageUri: Uri? = null
    lateinit var videoLauncher: ActivityResultLauncher<Intent>
    lateinit var imageLauncher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreateVideoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupCategorySpinner()

        videoLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                selectedVideoUri = result.data?.data
                showPostView()
            }
        }

        imageLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                selectedImageUri = result.data?.data
                Glide.with(this).load(selectedImageUri).into(binding.postThumbnailView)
            }
        }

        binding.addIcon.setOnClickListener {
            checkPermissionAndOpenVideoPicker()
        }

        binding.postThumbnailView.setOnClickListener {
            openImagePicker()
        }

        binding.submitPostBtn.setOnClickListener {
            postVideo()
        }

        binding.cancelPostBtn.setOnClickListener {
            finish()
        }
    }

    private fun setupCategorySpinner() {
        val categories = listOf("Gaming", "News", "Music", "Sports", "Education","Exercise","Comedy","tourism","VLog","Unboxing","Technology","Entertainment")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, categories)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.postCategorySpinner.adapter = adapter
    }

    private fun postVideo() {
        if (binding.postCaptionInput.text.toString().isEmpty()) {
            binding.postCaptionInput.error = "Write something"
            return
        }
        setInProgress(true)
        selectedVideoUri?.let { videoUri ->
            val videoRef = FirebaseStorage.getInstance()
                .reference
                .child("videos/${videoUri.lastPathSegment}")
            videoRef.putFile(videoUri)
                .addOnSuccessListener {
                    videoRef.downloadUrl.addOnSuccessListener { videoUrl ->
                        selectedImageUri?.let { imageUri ->
                            val imageRef = FirebaseStorage.getInstance()
                                .reference
                                .child("video_thumbnails/${imageUri.lastPathSegment}")
                            imageRef.putFile(imageUri)
                                .addOnSuccessListener {
                                    imageRef.downloadUrl.addOnSuccessListener { imageUrl ->
                                        CoroutineScope(Dispatchers.Main).launch {
                                            val duration = withContext(Dispatchers.IO) { getVideoDuration(videoUri) }
                                            postToFirestore(videoUrl.toString(), imageUrl.toString(), duration)
                                        }
                                    }
                                }
                        } ?: run {
                            CoroutineScope(Dispatchers.Main).launch {
                                val duration = withContext(Dispatchers.IO) { getVideoDuration(videoUri) }
                                postToFirestore(videoUrl.toString(), "", duration)
                            }
                        }
                    }
                }
        }
    }

    private fun getVideoDuration(videoUri: Uri): String {
        val retriever = MediaMetadataRetriever()
        retriever.setDataSource(this, videoUri)
        val durationMs = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLong() ?: 0L
        retriever.release()
        val minutes = durationMs / 1000 / 60
        val seconds = (durationMs / 1000 % 60).toInt()
        return String.format("%02d:%02d", minutes, seconds)
    }
/*
    private fun postToFirestore(videoUrl: String, imageUrl: String, duration: String) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId != null) {
            Firebase.firestore.collection("users").document(userId).get()
                .addOnSuccessListener { document ->
                    if (document != null) {
                        val user = document.toObject(UserModel::class.java)
                        val channelname = user?.channelname ?: ""
                        val uploaderName = user?.username ?: "Unknown User"
                        val category = binding.postCategorySpinner.selectedItem.toString()

                        val videoModel = VideoModel(
                            uploadername = uploaderName,
                            channelname = channelname,
                            videoId = "${userId}_${Timestamp.now().toDate().time}",
                            caption = binding.postCaptionInput.text.toString(),
                            url = videoUrl,
                            duration = duration,  // Store duration as String
                            videoimage = imageUrl,
                            like = mutableListOf(),
                            dislike = mutableListOf(),
                            comment = mutableListOf(),
                            uploaderId = userId,
                            subscriberCount = 0,
                            profilePic = user?.profilePic ?: "",
                            createdTime = Timestamp.now(),
                            category = category
                        )
                        Firebase.firestore.collection("videos")
                            .document(videoModel.videoId)
                            .set(videoModel)
                            .addOnSuccessListener {
                                setInProgress(false)
                                UiUtil.showToast(applicationContext, "Video uploaded")
                                finish()
                            }.addOnFailureListener {
                                setInProgress(false)
                                UiUtil.showToast(applicationContext, "Video failed to upload")
                            }
                    } else {
                        setInProgress(false)
                        UiUtil.showToast(applicationContext, "Error retrieving user details")
                    }
                }
                .addOnFailureListener {
                    setInProgress(false)
                    UiUtil.showToast(applicationContext, "Error: ${it.message}")
                }
        } else {
            setInProgress(false)
            UiUtil.showToast(applicationContext, "User not logged in")
        }
    }*/
private fun postToFirestore(videoUrl: String, imageUrl: String, duration: String) {
    val userId = FirebaseAuth.getInstance().currentUser?.uid
    if (userId != null) {
        Firebase.firestore.collection("users").document(userId).get()
            .addOnSuccessListener { document ->
                if (document != null) {
                    val user = document.toObject(UserModel::class.java)
                    val channelname = user?.channelname ?: ""
                    val uploaderName = user?.username ?: "Unknown User"
                    val category = binding.postCategorySpinner.selectedItem.toString()

                    val videoModel = VideoModel(
                        uploadername = uploaderName,
                        channelname = channelname,
                        videoId = "${userId}_${Timestamp.now().toDate().time}",
                        caption = binding.postCaptionInput.text.toString(),
                        url = videoUrl,
                        duration = duration,  // Store duration as String
                        videoimage = imageUrl,
                        like = mutableListOf(),
                        dislike = mutableListOf(),
                        comment = mutableListOf(),
                        uploaderId = userId,
                        subscriberCount = 0,
                        profilePic = user?.profilePic ?: "",
                        createdTime = Timestamp.now(),
                        category = category
                    )
                    Firebase.firestore.collection("videos")
                        .document(videoModel.videoId)
                        .set(videoModel)
                        .addOnSuccessListener {
                            // Increment videocount
                            Firebase.firestore.collection("users").document(userId)
                                .update("videocount", FieldValue.increment(1))
                                .addOnSuccessListener {
                                    setInProgress(false)
                                    UiUtil.showToast(applicationContext, "Video uploaded")
                                    finish()
                                }.addOnFailureListener {
                                    setInProgress(false)
                                    UiUtil.showToast(applicationContext, "Video uploaded but failed to update video count")
                                }
                        }.addOnFailureListener {
                            setInProgress(false)
                            UiUtil.showToast(applicationContext, "Video failed to upload")
                        }
                } else {
                    setInProgress(false)
                    UiUtil.showToast(applicationContext, "Error retrieving user details")
                }
            }
            .addOnFailureListener {
                setInProgress(false)
                UiUtil.showToast(applicationContext, "Error: ${it.message}")
            }
    } else {
        setInProgress(false)
        UiUtil.showToast(applicationContext, "User not logged in")
    }
}

    private fun setInProgress(inProgress: Boolean) {
        if (inProgress) {
            binding.progressBar.visibility = View.VISIBLE
            binding.submitPostBtn.visibility = View.GONE
        } else {
            binding.progressBar.visibility = View.GONE
            binding.submitPostBtn.visibility = View.VISIBLE
        }
    }

    private fun showPostView() {
        selectedVideoUri?.let {
            binding.postView.visibility = View.VISIBLE
            binding.uploadView.visibility = View.GONE
            Glide.with(binding.postVideo).load(it).into(binding.postVideo)
        }
    }

    private fun checkPermissionAndOpenVideoPicker() {
        val readExternalVideo: String = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            android.Manifest.permission.READ_MEDIA_VIDEO
        } else {
            android.Manifest.permission.READ_EXTERNAL_STORAGE
        }
        if (ContextCompat.checkSelfPermission(this, readExternalVideo) == PackageManager.PERMISSION_GRANTED) {
            openVideoPicker()
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(readExternalVideo),
                100
            )
        }
    }

    private fun openVideoPicker() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Video.Media.EXTERNAL_CONTENT_URI)
        intent.type = "video/*"
        videoLauncher.launch(intent)
    }

    private fun openImagePicker() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        intent.type = "image/*"
        imageLauncher.launch(intent)
    }
}
