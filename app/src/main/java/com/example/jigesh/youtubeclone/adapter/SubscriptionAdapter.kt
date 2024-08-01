package com.example.jigesh.youtubeclone.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.jigesh.youtubeclone.R
import com.example.jigesh.youtubeclone.model.Subscription
import com.example.jigesh.youtubeclone.model.VideoModel

class SubscriptionAdapter(
    private var subscriptions: List<Subscription>,
    private val listener: OnItemClickListener
) : RecyclerView.Adapter<SubscriptionAdapter.SubscriptionViewHolder>() {

    interface OnItemClickListener {
        fun onItemClick(subscription: Subscription)
    }

    inner class SubscriptionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener {
        val channelName: TextView = itemView.findViewById(R.id.youtuber_channelname)
        val profileImage: ImageView = itemView.findViewById(R.id.youtuber_profileimage)

        init {
            itemView.setOnClickListener(this)
        }

        override fun onClick(v: View?) {
            val position = adapterPosition
            if (position != RecyclerView.NO_POSITION) {
                listener.onItemClick(subscriptions[position])
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SubscriptionViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_subscription, parent, false)
        return SubscriptionViewHolder(view)
    }

    override fun onBindViewHolder(holder: SubscriptionViewHolder, position: Int) {
        val subscription = subscriptions[position]
        holder.channelName.text = subscription.channelName
        Glide.with(holder.profileImage.context)
            .load(subscription.profilePic)
            .placeholder(R.drawable.icon_profile)
            .error(R.drawable.icon_profile)
            .apply(RequestOptions.circleCropTransform())
            .into(holder.profileImage)
    }


    override fun getItemCount(): Int {
        return subscriptions.size
    }
}
