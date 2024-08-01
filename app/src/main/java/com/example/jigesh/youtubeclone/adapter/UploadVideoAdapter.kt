package com.example.jigesh.youtubeclone.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.jigesh.youtubeclone.R
import com.example.jigesh.youtubeclone.model.VideoModel
import java.util.Date
import java.util.concurrent.TimeUnit

class UploadVideoAdapter(private val videoList: List<VideoModel>) :
    RecyclerView.Adapter<UploadVideoAdapter.UploadVideoViewHolder>() {

    class UploadVideoViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val videoThumbnail: ImageView = view.findViewById(R.id.imageView3)
        val videoCaption: TextView = view.findViewById(R.id.textView9)
        val videoViews: TextView = view.findViewById(R.id.textView12)
        val videoTimestamp: TextView = view.findViewById(R.id.textView13)
        val videoDuration: TextView = view.findViewById(R.id.videoDuration)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UploadVideoViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.videodesign, parent, false)
        return UploadVideoViewHolder(view)
    }

    override fun onBindViewHolder(holder: UploadVideoViewHolder, position: Int) {
        val video = videoList[position]
        Glide.with(holder.itemView.context)
            .load(video.videoimage)
            .into(holder.videoThumbnail)
        holder.videoCaption.text = video.caption
        holder.videoDuration.text = video.duration
        holder.videoViews.text = "${video.view} views"
        holder.videoTimestamp.text = getRelativeTime(video.createdTime.toDate()) // Format the timestamp
    }

    override fun getItemCount(): Int {
        return videoList.size
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
