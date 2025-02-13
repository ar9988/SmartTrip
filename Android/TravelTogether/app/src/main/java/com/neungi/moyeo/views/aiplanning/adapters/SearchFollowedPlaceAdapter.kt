package com.neungi.moyeo.views.aiplanning.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.neungi.domain.model.Place
import com.neungi.moyeo.R
import com.neungi.moyeo.databinding.ItemPlaceHomeBinding
import com.neungi.moyeo.databinding.ItemPlaceSearchBinding
import com.neungi.moyeo.views.aiplanning.viewmodel.AIPlanningViewModel
import com.neungi.moyeo.views.home.viewmodel.HomeViewModel

class SearchFollowedPlaceAdapter(private val onCardClick: (String) -> Unit,
                                 private val onFollowClick: (String) -> Unit,
                                private val onPop:()->Unit) : ListAdapter<Place, SearchFollowedPlaceAdapter.HomePlaceViewHolder>(PlaceDiffCallback()) {
    inner class HomePlaceViewHolder(val binding: ItemPlaceSearchBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: Place) {
            binding.data = item
            binding.ivFollow.isSelected = item.isFollowed
            binding.ivPlaceImage.load(item.imageUrl) {
                error(R.drawable.image_noimg)
                placeholder(R.drawable.ic_placeholder)
            }
            binding.cvItemFollowedPlace.setOnClickListener{
                onCardClick(item.placeName)
                onPop()
            }
            binding.ivFollow.setOnClickListener{
                onFollowClick(item.contentId)
            }

        }
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HomePlaceViewHolder {

        return HomePlaceViewHolder(
            ItemPlaceSearchBinding.inflate(
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
