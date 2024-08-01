package com.example.jigesh.youtubeclone.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.jigesh.youtubeclone.R
import com.example.jigesh.youtubeclone.databinding.ShortvideodesignBinding
import com.example.jigesh.youtubeclone.model.UserModel
import com.example.jigesh.youtubeclone.model.ShortModel
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.auth.ktx.auth
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.jigesh.youtubeclone.YoutuberFragment

class ShortAdapter(
    options: FirestoreRecyclerOptions<ShortModel>
) : FirestoreRecyclerAdapter<ShortModel, ShortAdapter.VideoViewHolder>(options) {

    inner class VideoViewHolder(private val binding: ShortvideodesignBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bindVideo(shortModel: ShortModel) {
            // Bind user data
            Firebase.firestore.collection("users")
                .document(shortModel.uploaderId)
                .get().addOnSuccessListener {
                    val userModel = it?.toObject(UserModel::class.java)
                    userModel?.apply {
                        binding.usernameView.text = channelname
                        // Bind profile picture
                        Glide.with(binding.profileIcon).load(profilePic)
                            .circleCrop()
                            .apply(
                                RequestOptions().placeholder(R.drawable.icon_profile)
                            )
                            .into(binding.profileIcon)
                    }
                }

            binding.captionView.text = shortModel.thumbnail
            binding.progressBar.visibility = View.VISIBLE

            // Bind video
            binding.videoView.apply {
                setVideoPath(shortModel.url)
                setOnPreparedListener {
                    binding.progressBar.visibility = View.GONE
                    it.start()
                    it.isLooping = true
                }
                // Play/pause
                setOnClickListener {
                    if (isPlaying) {
                        pause()
                        binding.pauseIcon.visibility = View.VISIBLE
                    } else {
                        start()
                        binding.pauseIcon.visibility = View.GONE
                    }
                }
            }

            // Set like count (dislike count is not shown)
            binding.likeCount.text = shortModel.likeCount.toString()

            // Update button images based on user actions
            updateButtonImages(shortModel)

            // Handle like button click
            binding.likeButton.setOnClickListener {
                handleLikeDislike(shortModel, true)
            }

            // Handle dislike button click
            binding.dislikeButton.setOnClickListener {
                handleLikeDislike(shortModel, false)
            }

            // Handle user detail layout click
            binding.userDetailLayout.setOnClickListener {
                openYoutuberFragment(shortModel.uploaderId)
            }
        }

        private fun updateButtonImages(shortModel: ShortModel) {
            val currentUserId = Firebase.auth.currentUser?.uid ?: return
            if (shortModel.like.contains(currentUserId)) {
                binding.likeButton.setImageResource(R.drawable.likeblue)
            } else {
                binding.likeButton.setImageResource(R.drawable.like)
            }
        }

        private fun handleLikeDislike(shortModel: ShortModel, isLike: Boolean) {
            val videoRef = Firebase.firestore.collection("shorts").document(shortModel.videoId)
            val currentUserId = Firebase.auth.currentUser?.uid ?: return

            Firebase.firestore.runTransaction { transaction ->
                val snapshot = transaction.get(videoRef)
                val likeCount = snapshot.getLong("likeCount") ?: 0
                val dislikeCount = snapshot.getLong("dislikeCount") ?: 0

                val likes = snapshot.get("like") as? List<String> ?: emptyList()
                val dislikes = snapshot.get("dislike") as? List<String> ?: emptyList()

                var newLikeCount = likeCount
                var newDislikeCount = dislikeCount

                if (isLike) {
                    if (likes.contains(currentUserId)) {
                        // User has already liked, so remove like
                        transaction.update(videoRef, "like", FieldValue.arrayRemove(currentUserId))
                        newLikeCount -= 1
                    } else {
                        // User has not liked, so add like
                        transaction.update(videoRef, "like", FieldValue.arrayUnion(currentUserId))
                        newLikeCount += 1
                        if (dislikes.contains(currentUserId)) {
                            // Remove dislike if user previously disliked
                            transaction.update(videoRef, "dislike", FieldValue.arrayRemove(currentUserId))
                            newDislikeCount -= 1
                        }
                    }
                } else {
                    if (dislikes.contains(currentUserId)) {
                        // User has already disliked, so remove dislike
                        transaction.update(videoRef, "dislike", FieldValue.arrayRemove(currentUserId))
                        newDislikeCount -= 1
                    } else {
                        // User has not disliked, so add dislike
                        transaction.update(videoRef, "dislike", FieldValue.arrayUnion(currentUserId))
                        newDislikeCount += 1
                        if (likes.contains(currentUserId)) {
                            // Remove like if user previously liked
                            transaction.update(videoRef, "like", FieldValue.arrayRemove(currentUserId))
                            newLikeCount -= 1
                        }
                    }
                }

                // Ensure counts don't go below zero
                transaction.update(videoRef, "likeCount", newLikeCount.coerceAtLeast(0))
                transaction.update(videoRef, "dislikeCount", newDislikeCount.coerceAtLeast(0))
            }.addOnSuccessListener {
                Log.d("VideoListAdapter", "Transaction success: $currentUserId ${if (isLike) "liked" else "disliked"} video ${shortModel.videoId}")
                // Update button images after successful transaction
                updateButtonImages(shortModel)
            }.addOnFailureListener { e ->
                Log.e("VideoListAdapter", "Transaction failure", e)
            }

            // Update UI immediately
            if (isLike) {
                if (shortModel.like.contains(currentUserId)) {
                    shortModel.likeCount -= 1
                    shortModel.like = shortModel.like - currentUserId
                } else {
                    shortModel.likeCount += 1
                    shortModel.like = shortModel.like + currentUserId
                    if (shortModel.dislike.contains(currentUserId)) {
                        shortModel.dislikeCount -= 1
                        shortModel.dislike = shortModel.dislike - currentUserId
                    }
                }
            } else {
                if (shortModel.dislike.contains(currentUserId)) {
                    shortModel.dislikeCount -= 1
                    shortModel.dislike = shortModel.dislike - currentUserId
                } else {
                    shortModel.dislikeCount += 1
                    shortModel.dislike = shortModel.dislike + currentUserId
                    if (shortModel.like.contains(currentUserId)) {
                        shortModel.likeCount -= 1
                        shortModel.like = shortModel.like - currentUserId
                    }
                }
            }
            binding.likeCount.text = shortModel.likeCount.toString()
        }

        private fun openYoutuberFragment(userId: String) {
            val activity = binding.root.context as AppCompatActivity
            val fragment = YoutuberFragment.newInstance(userId)
            activity.supportFragmentManager.beginTransaction()
                .replace(R.id.framelayout, fragment) // Ensure R.id.framelayout is your fragment container view ID
                .addToBackStack(null)
                .commit()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VideoViewHolder {
        val binding = ShortvideodesignBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VideoViewHolder(binding)
    }

    override fun onBindViewHolder(holder: VideoViewHolder, position: Int, model: ShortModel) {
        holder.bindVideo(model)
    }
}
