package com.neungi.moyeo.views.album.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.neungi.domain.model.PhotoPlace
import com.neungi.moyeo.databinding.ListPlaceAlbumDetailBinding
import com.neungi.moyeo.views.album.viewmodel.AlbumViewModel

class PhotoPlaceAdapter(
    private val viewModel: AlbumViewModel
) : ListAdapter<PhotoPlace, PhotoPlaceAdapter.PhotoPlaceViewHolder>(diffUtil) {

    class PhotoPlaceViewHolder(private val binding: ListPlaceAlbumDetailBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(photoPlace: PhotoPlace, viewModel: AlbumViewModel) {
            binding.photoPlace = photoPlace
            binding.vm = viewModel
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PhotoPlaceViewHolder =
        PhotoPlaceViewHolder(
            ListPlaceAlbumDetailBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        )

    override fun onBindViewHolder(holder: PhotoPlaceViewHolder, position: Int) {
        holder.bind(currentList[position], viewModel)
    }

    companion object {

        val diffUtil = object : DiffUtil.ItemCallback<PhotoPlace>() {

            override fun areContentsTheSame(oldItem: PhotoPlace, newItem: PhotoPlace): Boolean =
                (oldItem == newItem)

            override fun areItemsTheSame(oldItem: PhotoPlace, newItem: PhotoPlace): Boolean =
                (oldItem.id == newItem.id)
        }
    }
}