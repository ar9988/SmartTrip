package com.neungi.moyeo.views.album.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.neungi.domain.model.Photo
import com.neungi.moyeo.databinding.ListPhotoAlbumDetailBinding
import com.neungi.moyeo.views.album.viewmodel.AlbumViewModel

class PhotoAdapter(
    private val viewModel: AlbumViewModel
) : ListAdapter<Photo, PhotoAdapter.PhotoViewHolder>(diffUtil) {

    class PhotoViewHolder(private val binding: ListPhotoAlbumDetailBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(photo: Photo, viewModel: AlbumViewModel) {
            binding.photo = photo
            binding.vm = viewModel
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PhotoViewHolder =
        PhotoViewHolder(
            ListPhotoAlbumDetailBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        )

    override fun onBindViewHolder(holder: PhotoViewHolder, position: Int) {
        holder.bind(currentList[position], viewModel)
    }

    companion object {

        val diffUtil = object : DiffUtil.ItemCallback<Photo>() {

            override fun areContentsTheSame(oldItem: Photo, newItem: Photo): Boolean =
                (oldItem == newItem)

            override fun areItemsTheSame(oldItem: Photo, newItem: Photo): Boolean =
                (oldItem.id == newItem.id)
        }
    }
}