package com.example.jigesh.youtubeclone.adapter

import android.content.Intent
import android.os.Bundle
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.example.jigesh.youtubeclone.R
import com.example.jigesh.youtubeclone.ShortActivity
import com.example.jigesh.youtubeclone.model.ShortModel
import java.util.Date
import java.util.concurrent.TimeUnit

class ShortListAdapter(private var shortList: List<ShortModel>) :
    RecyclerView.Adapter<ShortListAdapter.VideoViewHolder>() {

    class VideoViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val videoThumbnail: ImageView = view.findViewById(R.id.imageView3)
        val videoCaption: TextView = view.findViewById(R.id.textView9)
        val totalView: TextView = view.findViewById(R.id.textView12)
        val uploadTime: TextView = view.findViewById(R.id.textView13)
        val videoDuration: TextView = view.findViewById(R.id.videoDuration)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VideoViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.videodesign, parent, false)
        return VideoViewHolder(view)
    }

    override fun onBindViewHolder(holder: VideoViewHolder, position: Int) {
        val video = shortList[position]

        Glide.with(holder.videoThumbnail.context)
            .load(video.url)
            .placeholder(R.drawable.grey)
            .error(R.drawable.video)
            .into(holder.videoThumbnail)

        holder.videoCaption.text = video.thumbnail
        holder.videoDuration.text = "Shorts"
        holder.uploadTime.text = getRelativeTime(video.createdTime.toDate())
/*
        holder.itemView.setOnClickListener {
            val intent = Intent(holder.itemView.context, ShortActivity::class.java).apply {
                putExtra("CHANNEL_NAME", video.channelname)
                putExtra("VIDEO_ID", video.videoId)
                putExtra("VIDEO_URL", video.url) // Pass the video URL
            }
            holder.itemView.context.startActivity(intent)
        }*/
    }

    override fun getItemCount() = shortList.size

    fun updateVideos(newVideoList: List<ShortModel>) {
        shortList = newVideoList
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
