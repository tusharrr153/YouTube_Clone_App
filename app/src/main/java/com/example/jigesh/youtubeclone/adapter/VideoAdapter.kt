package com.example.jigesh.youtubeclone.adapter

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.jigesh.youtubeclone.R
import com.example.jigesh.youtubeclone.VideoPlayActivity
import com.example.jigesh.youtubeclone.model.VideoModel
import java.util.Date
import java.util.concurrent.TimeUnit

class VideoAdapter(private var videoList: List<VideoModel>) :
    RecyclerView.Adapter<VideoAdapter.VideoViewHolder>() {

    class VideoViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val videoThumbnail: ImageView = view.findViewById(R.id.videoThumbnail)
        val videoCaption: TextView = view.findViewById(R.id.videoCaption)
        val profileIcon: ImageView = view.findViewById(R.id.profile_icon)
        val usernameView: TextView = view.findViewById(R.id.username_view)
        val totalView: TextView = view.findViewById(R.id.totalview)
        val uploadTime: TextView = view.findViewById(R.id.uploadtime)
        val videoDuration: TextView = view.findViewById(R.id.video_duration)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VideoViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_video, parent, false)
        return VideoViewHolder(view)
    }

    override fun onBindViewHolder(holder: VideoViewHolder, position: Int) {
        val video = videoList[position]

        // Load video thumbnail
        Glide.with(holder.videoThumbnail.context)
            .load(video.videoimage)
            .placeholder(R.drawable.grey)
            .error(R.drawable.video)
            .into(holder.videoThumbnail)

        // Load profile icon as a rounded image
        Glide.with(holder.profileIcon.context)
            .load(video.profilePic)
            .placeholder(R.drawable.youtube)
            .error(R.drawable.profile)
            .apply(RequestOptions.circleCropTransform())
            .into(holder.profileIcon)

        holder.videoCaption.text = video.caption
        holder.usernameView.text = video.channelname
        holder.videoDuration.text = video.duration

        // Set the total views
        holder.totalView.text = "${video.view} views"

        // Set the upload time
        holder.uploadTime.text = getRelativeTime(video.createdTime.toDate())

        holder.itemView.setOnClickListener {
            val intent = Intent(holder.itemView.context, VideoPlayActivity::class.java).apply {
                putExtra("VIDEO_URL", video.url)
                putExtra("VIDEO_CAPTION", video.caption)
                putExtra("CHANNEL_NAME", video.channelname)
                putExtra("PROFILE_PIC", video.profilePic)
                putExtra("UPLOAD_TIME", video.createdTime.toDate().time)
                putExtra("TOTAL_VIEWS", video.view)
                putExtra("SUBSCRIBER_COUNT", video.subscriberCount ?: 0) // Provide a default value if null
                putExtra("VIDEO_ID", video.videoId)
            }
            holder.itemView.context.startActivity(intent)
        }
    }

    override fun getItemCount() = videoList.size

    fun updateVideos(newVideoList: List<VideoModel>) {
        videoList = newVideoList
        notifyDataSetChanged()
    }

    private fun getRelativeTime(date: Date): String {
        val now = Date()
        val diff = now.time - date.time

        val minutes = TimeUnit.MILLISECONDS.toMinutes(diff)
        val hours = TimeUnit.MILLISECONDS.toHours(diff)
        val days = TimeUnit.MILLISECONDS.toDays(diff)

        return when {
            minutes < 60 -> "$minutes min ago"
            hours < 24 -> "$hours hour ago"
            else -> "$days day ago"
        }
    }
}
