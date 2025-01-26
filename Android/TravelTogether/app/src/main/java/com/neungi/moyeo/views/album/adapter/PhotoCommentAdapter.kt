package com.neungi.moyeo.views.album.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.neungi.domain.model.Comment
import com.neungi.moyeo.databinding.ListPhotoCommentBinding
import com.neungi.moyeo.views.album.viewmodel.AlbumViewModel

class PhotoCommentAdapter(
    private val viewModel: AlbumViewModel
) : ListAdapter<Comment, PhotoCommentAdapter.PhotoCommentViewHolder>(diffUtil) {

    class PhotoCommentViewHolder(private val binding: ListPhotoCommentBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(comment: Comment, viewModel: AlbumViewModel) {
            binding.comment = comment
            binding.vm = viewModel
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PhotoCommentViewHolder =
        PhotoCommentViewHolder(
            ListPhotoCommentBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        )

    override fun onBindViewHolder(holder: PhotoCommentViewHolder, position: Int) {
        holder.bind(currentList[position], viewModel)
    }

    companion object {

        val diffUtil = object : DiffUtil.ItemCallback<Comment>() {

            override fun areContentsTheSame(oldItem: Comment, newItem: Comment): Boolean =
                (oldItem == newItem)

            override fun areItemsTheSame(oldItem: Comment, newItem: Comment): Boolean =
                (oldItem.id == newItem.id)
        }
    }
}