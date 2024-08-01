package com.example.jigesh.youtubeclone.model

data class UserModel(
    var id : String = "",
    var email : String ="",
    var username : String ="",
    var profilePic : String = "",
    var channelname : String = "",
    var subscribers : MutableList<String> = mutableListOf(),
    var videocount : Long = 0L

)
