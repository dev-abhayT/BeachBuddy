package com.example.beachbuddy.adapters

import android.content.res.ColorStateList
import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.beachbuddy.R
import com.example.beachbuddy.data.Beach
import com.example.beachbuddy.databinding.CardBeachBinding
import com.example.beachbuddy.databinding.NewCardBeachBinding

class BeachCardAdapter(private var listener: OnBeachClickListener? = null) : RecyclerView.Adapter<BeachCardAdapter.BeachViewHolder>(){
    private var beaches = listOf<Beach>()

    fun submitList(newBeaches: List<Beach>) {
        beaches = newBeaches
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BeachViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = NewCardBeachBinding.inflate(inflater, parent, false)
        return BeachViewHolder(binding)
    }

    override fun onBindViewHolder(holder: BeachViewHolder, position: Int) {
        holder.bind(beaches[position])
    }

    override fun getItemCount() = beaches.size

    inner class BeachViewHolder(private val binding: NewCardBeachBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(beach: Beach) {
            with(binding) {
                beachName.text = beach.name
//                beachDistance.text = "${beach.distance}s from your location"
                statusReason.text = beach.safetyReason
                statusText.text = beach.safetyStatus.displayName
                beachAddress.text = beach.address

                Glide.with(itemView.context)
                    .load(beach.photoUrl)
                    .placeholder(R.drawable.ic_google)
                    .error(R.drawable.activity_splash_background)
                    .into(ivBeachImage)

                // Set safety status color
                val statusColor = Color.parseColor(beach.safetyStatus.color)
                beachStatusCard.backgroundTintList = ColorStateList.valueOf(statusColor)
                statusReason.setTextColor(ColorStateList.valueOf(Color.parseColor(beach.safetyStatus.color)))

                val bgColor = Color.parseColor(beach.safetyStatus.bgColor)
                safetyReasonBg.backgroundTintList = ColorStateList.valueOf(bgColor)

//                statusReason.setTextColor(statusColor)


//                // Set favorite icon
//                if (beach.isFavorite) {
//                    favorites.setImageResource(R.drawable.ic_filled_heart)
//                }

                root.setOnClickListener {
                    listener?.onBeachClick(beach)
                }

    }
}
    }}

interface OnBeachClickListener{
    fun onBeachClick(beach:Beach)
}
