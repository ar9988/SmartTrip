package com.neungi.moyeo.views.album.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.neungi.domain.model.PhotoAlbum
import com.neungi.moyeo.databinding.ListAlbumBinding
import com.neungi.moyeo.views.album.viewmodel.AlbumViewModel

class AlbumAdapter(
    private val viewModel: AlbumViewModel
) : ListAdapter<PhotoAlbum, AlbumAdapter.AlbumViewHolder>(diffUtil) {

    class AlbumViewHolder(private val binding: ListAlbumBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(photoAlbum: PhotoAlbum, viewModel: AlbumViewModel) {
            binding.photoAlbum = photoAlbum
            binding.vm = viewModel
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AlbumViewHolder =
        AlbumViewHolder(
            ListAlbumBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        )

    override fun onBindViewHolder(holder: AlbumViewHolder, position: Int) {
        holder.bind(currentList[position], viewModel)
    }

    companion object {

        val diffUtil = object : DiffUtil.ItemCallback<PhotoAlbum>() {

            override fun areContentsTheSame(oldItem: PhotoAlbum, newItem: PhotoAlbum): Boolean =
                (oldItem == newItem)

            override fun areItemsTheSame(oldItem: PhotoAlbum, newItem: PhotoAlbum): Boolean =
                (oldItem.id == newItem.id)
        }
    }
}