package com.neungi.moyeo.views.album.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.net.toUri
import androidx.lifecycle.viewModelScope
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import coil.transform.CircleCropTransformation
import com.neungi.domain.model.Comment
import com.neungi.moyeo.R
import com.neungi.moyeo.databinding.ListPhotoCommentBinding
import com.neungi.moyeo.views.album.viewmodel.AlbumViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class PhotoCommentAdapter(
    private val viewModel: AlbumViewModel
) : ListAdapter<Comment, PhotoCommentAdapter.PhotoCommentViewHolder>(diffUtil) {

    class PhotoCommentViewHolder(private val binding: ListPhotoCommentBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(comment: Comment, viewModel: AlbumViewModel) {
            binding.comment = comment
            binding.vm = viewModel
            binding.ivProfilePhotoComment.load(comment.profileImage.toUri()) {
                error(R.drawable.ic_profile_empty)
                transformations(CircleCropTransformation())
            }

            viewModel.viewModelScope.launch {
                viewModel.fetchUserId().collectLatest { id ->
                    if (comment.userId != id) {
                        with(binding) {
                            ivModifyPhotoComment.visibility = View.GONE
                            ivDeletePhotoComment.visibility = View.GONE
                            ivUpdatePhotoComment.visibility = View.GONE
                            ivCancelPhotoComment.visibility = View.GONE
                        }
                    }
                }
            }

            binding.tvNicknamePhotoComment.isSelected = true

            // 수정
            binding.ivModifyPhotoComment.setOnClickListener {
                with(binding) {
                    tvContentPhotoComment.visibility = View.GONE
                    ivModifyPhotoComment.visibility = View.GONE
                    ivDeletePhotoComment.visibility = View.GONE
                    etPhotoComment.visibility = View.VISIBLE
                    ivUpdatePhotoComment.visibility = View.VISIBLE
                    ivCancelPhotoComment.visibility = View.VISIBLE
                    etPhotoComment.setText(comment.content)
                }
            }

            // 수정 취소
            binding.ivCancelPhotoComment.setOnClickListener {
                with(binding) {
                    tvContentPhotoComment.visibility = View.VISIBLE
                    ivModifyPhotoComment.visibility = View.VISIBLE
                    ivDeletePhotoComment.visibility = View.VISIBLE
                    etPhotoComment.visibility = View.GONE
                    ivUpdatePhotoComment.visibility = View.GONE
                    ivCancelPhotoComment.visibility = View.GONE
                }
            }

            // 수정 완료
            binding.ivUpdatePhotoComment.setOnClickListener {
                with(binding) {
                    tvContentPhotoComment.visibility = View.VISIBLE
                    ivModifyPhotoComment.visibility = View.VISIBLE
                    ivDeletePhotoComment.visibility = View.VISIBLE
                    etPhotoComment.visibility = View.GONE
                    ivUpdatePhotoComment.visibility = View.GONE
                    ivCancelPhotoComment.visibility = View.GONE
                }
                viewModel.onClickCommentUpdate(comment)
            }

            // 삭제
            binding.ivDeletePhotoComment.setOnClickListener {
                viewModel.onClickCommentDelete(comment)
            }
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