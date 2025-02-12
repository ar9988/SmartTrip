package com.neungi.moyeo.views.home.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.neungi.domain.model.Festival
import com.neungi.domain.model.Place
import com.neungi.moyeo.R
import com.neungi.moyeo.databinding.ItemFestivalHomeBinding
import com.neungi.moyeo.databinding.ItemPlaceHomeBinding
import com.neungi.moyeo.views.home.viewmodel.HomeViewModel

class HomePlaceAdapter() : ListAdapter<Place, HomePlaceAdapter.HomePlaceViewHolder>(PlaceDiffCallback()) {
    inner class HomePlaceViewHolder(val binding: ItemPlaceHomeBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: Place) {
            binding.data = item
            binding.ivPlaceImage.load(item.imageUrl) {
                error(R.drawable.image_noimg)
                placeholder(R.drawable.ic_placeholder)
            }
        }
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HomePlaceViewHolder {

        return HomePlaceViewHolder(ItemPlaceHomeBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        ))
    }

    override fun onBindViewHolder(holder: HomePlaceViewHolder, position: Int) {
        holder.bind(getItem(position))
    }


    private class PlaceDiffCallback : DiffUtil.ItemCallback<Place>() {
        override fun areItemsTheSame(oldItem: Place, newItem: Place): Boolean {
            return oldItem.placeName == newItem.placeName
        }

        override fun areContentsTheSame(oldItem: Place, newItem: Place): Boolean {
            return oldItem == newItem
        }
    }
}

