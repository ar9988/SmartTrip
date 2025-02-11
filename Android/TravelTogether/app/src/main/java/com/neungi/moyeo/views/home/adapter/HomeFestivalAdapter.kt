package com.neungi.moyeo.views.home.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.neungi.domain.model.Festival
import com.neungi.moyeo.R
import com.neungi.moyeo.databinding.ItemFestivalHomeBinding
import com.neungi.moyeo.views.home.viewmodel.HomeViewModel

class HomeFestivalAdapter(private val viewModel: HomeViewModel) : ListAdapter<Festival, HomeFestivalAdapter.HomeFestivalViewHolder>(FestivalDiffCallback()) {
    inner class HomeFestivalViewHolder(val binding: ItemFestivalHomeBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: Festival) {
            binding.data = item
            binding.ivFestivalImage.load(item.imageUrl) {
                error(R.drawable.image_noimg)
                placeholder(R.drawable.ic_placeholder)
            }
            binding.root.setOnClickListener {
                viewModel.selectFestival(item)
            }

        }
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HomeFestivalViewHolder {

        return HomeFestivalViewHolder(ItemFestivalHomeBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        ))
    }

    override fun onBindViewHolder(holder: HomeFestivalViewHolder, position: Int) {
        holder.bind(getItem(position))
    }


    private class FestivalDiffCallback : DiffUtil.ItemCallback<Festival>() {
        override fun areItemsTheSame(oldItem: Festival, newItem: Festival): Boolean {
            return oldItem.title == newItem.title
        }

        override fun areContentsTheSame(oldItem: Festival, newItem: Festival): Boolean {
            return oldItem == newItem
        }
    }
}

