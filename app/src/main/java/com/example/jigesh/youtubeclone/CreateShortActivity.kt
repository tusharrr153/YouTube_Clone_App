package com.example.jigesh.youtubeclone

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.example.jigesh.youtubeclone.databinding.ActivityCreateShortBinding
import com.example.jigesh.youtubeclone.model.ShortModel
import com.example.jigesh.youtubeclone.model.UserModel
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage

class CreateShortActivity : AppCompatActivity() {
    lateinit var binding: ActivityCreateShortBinding
    private var selectedVideoUri: Uri? = null
    lateinit var videoLauncher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreateShortBinding.inflate(layoutInflater)
        setContentView(binding.root)

        videoLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                selectedVideoUri = result.data?.data
                showPostView()
            }
        }

        binding.uploadView.setOnClickListener {
            checkPermissionAndOpenVideoPicker()
        }

        binding.submitPostBtn.setOnClickListener {
            postVideo()
        }

        binding.cancelPostBtn.setOnClickListener {
            finish()
        }
    }

    private fun postVideo() {
        if (binding.postCaptionInput.text.toString().isEmpty()) {
            binding.postCaptionInput.setError("Write something")
            return
        }
        setInProgress(true)
        selectedVideoUri?.apply {
            // Store in Firebase Cloud Storage
            val videoRef = FirebaseStorage.getInstance()
                .reference
                .child("videos/" + this.lastPathSegment)
            videoRef.putFile(this)
                .addOnSuccessListener {
                    videoRef.downloadUrl.addOnSuccessListener { downloadUrl ->
                        // Video model store in Firebase Firestore
                        postToFirestore(downloadUrl.toString())
                    }
                }
        }
    }

  /*  private fun postToFirestore(url: String) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId != null) {
            val shortModel = ShortModel(
                videoId = "${userId}_${Timestamp.now().toDate().time}",
                thumbnail = binding.postCaptionInput.text.toString(),
                url = url,
                likeCount = 0,
                dislikeCount = 0,
                comment = mutableListOf(),
                uploaderId = userId,
                createdTime = Timestamp.now()
            )
            Firebase.firestore.collection("shorts")
                .document(shortModel.videoId)
                .set(shortModel)
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
            UiUtil.showToast(applicationContext, "User not logged in")
        }
    }*/

    private fun postToFirestore(url: String) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId != null) {
            Firebase.firestore.collection("users").document(userId).get()
                .addOnSuccessListener { document ->
                    if (document != null) {
                        val user = document.toObject(UserModel::class.java)
                        val channelname = user?.channelname ?: ""

                        val shortModel = ShortModel(
                            videoId = "${userId}_${Timestamp.now().toDate().time}",
                            thumbnail = binding.postCaptionInput.text.toString(),
                            url = url,
                            likeCount = 0,
                            dislikeCount = 0,
                            comment = mutableListOf(),
                            uploaderId = userId,
                            createdTime = Timestamp.now(),
                            channelname = channelname
                        )
                        Firebase.firestore.collection("shorts")
                            .document(shortModel.videoId)
                            .set(shortModel)
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
            Glide.with(binding.postThumbnailView).load(it).into(binding.postThumbnailView)
        }
    }

    private fun checkPermissionAndOpenVideoPicker() {
        val readExternalVideo: String = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            android.Manifest.permission.READ_MEDIA_VIDEO
        } else {
            android.Manifest.permission.READ_EXTERNAL_STORAGE
        }
        if (ContextCompat.checkSelfPermission(this, readExternalVideo) == PackageManager.PERMISSION_GRANTED) {
            // We have permission
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
}
