package com.neungi.moyeo.views.aiplanning.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.neungi.domain.model.Festival
import com.neungi.moyeo.R
import com.neungi.moyeo.databinding.ItemFestivalAiDestinationBinding
import com.neungi.moyeo.views.aiplanning.viewmodel.AIPlanningViewModel
import com.neungi.moyeo.views.aiplanning.viewmodel.FestivalSelectUiState
import kotlinx.coroutines.launch
import timber.log.Timber

class AiRecommendFestivalAdapter(private val viewModel: AIPlanningViewModel) : ListAdapter<FestivalSelectUiState, AiRecommendFestivalAdapter.AiRecommendFestivalViewHolder>(FestivalDiffCallback()) {
    inner class AiRecommendFestivalViewHolder(val binding: ItemFestivalAiDestinationBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: FestivalSelectUiState) {
            binding.uiState = item
            binding.ivFestivalImage.load(item.festival.imageUrl) {
                error(R.drawable.image_noimg)
                placeholder(R.drawable.ic_placeholder)
            }
            updateSelectedUI(item.isSelected)
            binding.root.setOnClickListener {
                if(viewModel.selectedPlaces.value.contains(item.festival.title)){
                    viewModel.togglePlaceSelection(item.festival.title)
                }else {
                    viewModel.selectFestival(item.festival)
                }
            }
        }

        private fun updateSelectedUI(isSelected: Boolean) {
            binding.viewBorder.visibility = if (isSelected) View.VISIBLE else View.GONE
            binding.ivCheck.visibility = if (isSelected) View.VISIBLE else View.GONE
        }
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AiRecommendFestivalViewHolder {

        return AiRecommendFestivalViewHolder(ItemFestivalAiDestinationBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        ))
    }

    override fun onBindViewHolder(holder: AiRecommendFestivalViewHolder, position: Int) {
        holder.bind(getItem(position))
    }


    private class FestivalDiffCallback : DiffUtil.ItemCallback<FestivalSelectUiState>() {
        override fun areItemsTheSame(oldItem: FestivalSelectUiState, newItem: FestivalSelectUiState): Boolean {
            return oldItem.festival.title == newItem.festival.title
        }

        override fun areContentsTheSame(oldItem: FestivalSelectUiState, newItem: FestivalSelectUiState): Boolean {
            return oldItem == newItem
        }
    }
}

