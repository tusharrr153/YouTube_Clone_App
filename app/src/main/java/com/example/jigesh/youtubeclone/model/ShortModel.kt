package com.example.jigesh.youtubeclone.model
import com.google.firebase.Timestamp
import com.google.firebase.firestore.IgnoreExtraProperties

@IgnoreExtraProperties
data class ShortModel(
    var videoId: String = "",
    var uploaderId: String = "",
    var channelname: String = "",
    var view: Long = 0,
    var category: String = "",
    var url: String = "",
    var thumbnail: String = "",
    var likeCount: Long = 0,
    var dislikeCount: Long = 0,
    var comment: MutableList<String> = mutableListOf(),
    var like: List<String> = emptyList(),  // New field
    var dislike: List<String> = emptyList(),  // New field
    var profilePic : String = "",
    var createdTime : Timestamp = Timestamp.now()
)