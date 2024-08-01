package com.example.jigesh.youtubeclone.model

import com.google.firebase.Timestamp

data class VideoModel(
    val uploadername: String = "",
    val channelname: String = "",
    var subscribers: MutableList<String> = mutableListOf(),
    var view: Long = 0,
    var category: String = "",
    val videoId: String = "",
    val caption: String = "",
    val url: String = "",
    var duration: String = "",  // Ensure duration is Long
    var videoimage: String = "",
    var like: MutableList<String> = mutableListOf(),  // Added like property
    var dislike: MutableList<String> = mutableListOf(),  // Added dislike property
    val comment: MutableList<String> = mutableListOf(),
    val uploaderId: String = "",
    var profilePic: String = "",
    val createdTime: Timestamp = Timestamp.now(),
    var subscriberCount: Int? = 0  // Made it nullable with default value
)
