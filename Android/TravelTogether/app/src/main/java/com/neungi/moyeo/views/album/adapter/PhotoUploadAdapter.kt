package com.neungi.moyeo.views.album.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.neungi.moyeo.databinding.ListPhotoAddBinding
import com.neungi.moyeo.databinding.ListPhotoUploadedBinding
import com.neungi.moyeo.views.album.viewmodel.AlbumViewModel
import com.neungi.moyeo.views.album.viewmodel.PhotoUploadUiState
import com.neungi.moyeo.views.album.viewmodel.PhotoUploadUiState.Companion.PHOTO_VIEW_TYPE
import com.neungi.moyeo.views.album.viewmodel.PhotoUploadUiState.Companion.UPLOAD_VIEW_TYPE

class PhotoUploadAdapter(
    private val viewModel: AlbumViewModel
) : ListAdapter<PhotoUploadUiState, RecyclerView.ViewHolder>(diffUtil) {

    class PhotoUploadButtonViewHolder(private val binding: ListPhotoAddBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(uploadBtn: PhotoUploadUiState.PhotoUploadButton, viewModel: AlbumViewModel) {
            binding.uploadBtn = uploadBtn
            binding.vm = viewModel
        }
    }

    class UploadedPhotoViewHolder(private val binding: ListPhotoUploadedBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(photo: PhotoUploadUiState.UploadedPhoto, viewModel: AlbumViewModel) {
            binding.photo = photo
            binding.vm = viewModel
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder =
        when (viewType) {
            UPLOAD_VIEW_TYPE -> PhotoUploadButtonViewHolder(
                ListPhotoAddBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                )
            )

            else -> UploadedPhotoViewHolder(
                ListPhotoUploadedBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                )
            )
        }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder.itemViewType) {
            UPLOAD_VIEW_TYPE -> {
                (holder as PhotoUploadButtonViewHolder).bind(
                    currentList[position] as PhotoUploadUiState.PhotoUploadButton,
                    viewModel
                )
            }

            PHOTO_VIEW_TYPE -> {
                (holder as UploadedPhotoViewHolder).bind(
                    currentList[position] as PhotoUploadUiState.UploadedPhoto,
                    viewModel
                )
            }
        }
    }

    override fun getItemViewType(position: Int): Int = when {
        currentList[position] is PhotoUploadUiState.PhotoUploadButton -> UPLOAD_VIEW_TYPE

        else -> PHOTO_VIEW_TYPE
    }

    companion object {

        val diffUtil = object : DiffUtil.ItemCallback<PhotoUploadUiState>() {

            override fun areContentsTheSame(
                oldItem: PhotoUploadUiState,
                newItem: PhotoUploadUiState
            ): Boolean =
                (oldItem == newItem)

            override fun areItemsTheSame(
                oldItem: PhotoUploadUiState,
                newItem: PhotoUploadUiState
            ): Boolean =
                (oldItem.id == newItem.id)
        }
    }
}