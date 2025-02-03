package com.neungi.moyeo.views.aiplanning.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.neungi.domain.model.Festival
import com.neungi.moyeo.databinding.ItemAiDestinationFestivalBinding
import com.neungi.moyeo.views.aiplanning.viewmodel.AIPlanningViewModel
import kotlinx.coroutines.launch
import timber.log.Timber

class AiRecommendFestivalAdapter(private val viewModel: AIPlanningViewModel,
                                 private val lifecycleOwner: LifecycleOwner) : RecyclerView.Adapter<AiRecommendFestivalAdapter.AiRecommendFestivalViewHolder>() {
    private var items: List<Festival> = emptyList()
    init {
        lifecycleOwner.lifecycleScope.launch {
            viewModel.recommendFestivals.collect { festivals ->
                items = festivals
            }
        }
    }
    inner class AiRecommendFestivalViewHolder(val binding: ItemAiDestinationFestivalBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: Festival) {
            binding.tvFestivalTitle.text = item.title
            binding.ivFestivalImage.load(item.imageUrl)
            lifecycleOwner.lifecycleScope.launch {
                viewModel.selectedPlaces.collect { selectedPlaces ->
                    Timber.d(selectedPlaces.toString())
                    val isSelected = selectedPlaces.contains(item.title)
                    updateSelectedUI(isSelected)
                }
            }
            binding.root.setOnClickListener {
                if(viewModel.selectedLocations.value.contains(item.title)){
                    viewModel.togglePlaceSelection(item.title)
                }else {
                    viewModel.selectFestival(item)
                }

            }

        }
        private fun updateSelectedUI(isSelected: Boolean) {
            binding.viewBorder.visibility = if (isSelected) View.VISIBLE else View.GONE
            binding.ivCheck.visibility = if (isSelected) View.VISIBLE else View.GONE
        }
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AiRecommendFestivalViewHolder {

        return AiRecommendFestivalViewHolder(ItemAiDestinationFestivalBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        ))
    }

    override fun onBindViewHolder(holder: AiRecommendFestivalViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount() = items.size
}

