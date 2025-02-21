package com.neungi.moyeo.views.album.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.net.toUri
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.neungi.moyeo.R
import com.neungi.moyeo.databinding.ListPhotoClassifiedBinding
import com.neungi.moyeo.util.MarkerData
import com.neungi.moyeo.views.album.viewmodel.AlbumViewModel
import timber.log.Timber

class PhotoClassifiedAdapter(
    private val place: Int,
    private val viewModel: AlbumViewModel
) : ListAdapter<MarkerData, PhotoClassifiedAdapter.PhotoClassifiedViewHolder>(diffUtil) {

    class PhotoClassifiedViewHolder(private val binding: ListPhotoClassifiedBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(marker: MarkerData, viewModel: AlbumViewModel, place: Int) {
            binding.ivListPhotoClassified.load(marker.photo.filePath.toUri()) {
                placeholder(R.drawable.ic_theme_white)
                error(R.drawable.ic_theme_white)
            }
            binding.vm = viewModel
            binding.mcvPhotoClassified.setOnClickListener {
                viewModel.onClickUpdatePhotoClassification(place, layoutPosition)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PhotoClassifiedViewHolder =
        PhotoClassifiedViewHolder(
            ListPhotoClassifiedBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        )

    override fun onBindViewHolder(holder: PhotoClassifiedViewHolder, position: Int) {
        holder.bind(currentList[position], viewModel, place)
    }

    companion object {

        val diffUtil = object : DiffUtil.ItemCallback<MarkerData>() {

            override fun areContentsTheSame(oldItem: MarkerData, newItem: MarkerData): Boolean =
                (oldItem == newItem)

            override fun areItemsTheSame(oldItem: MarkerData, newItem: MarkerData): Boolean =
                (oldItem.id == newItem.id)
        }
    }
}