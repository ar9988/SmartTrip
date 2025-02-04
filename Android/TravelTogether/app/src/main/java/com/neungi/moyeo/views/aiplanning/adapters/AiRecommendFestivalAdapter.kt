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
import com.neungi.moyeo.views.aiplanning.viewmodel.FestivalSelectUiState
import kotlinx.coroutines.launch
import timber.log.Timber

class AiRecommendFestivalAdapter(private val viewModel: AIPlanningViewModel,
                                 private val lifecycleOwner: LifecycleOwner) : RecyclerView.Adapter<AiRecommendFestivalAdapter.AiRecommendFestivalViewHolder>() {
    private var items: List<FestivalSelectUiState> = emptyList()

    inner class AiRecommendFestivalViewHolder(val binding: ItemAiDestinationFestivalBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: FestivalSelectUiState) {
            binding.tvFestivalTitle.text = item.festival.title
            binding.ivFestivalImage.load(item.festival.imageUrl)
            updateSelectedUI(item.isSelected)
            binding.root.setOnClickListener {
                if(viewModel.selectedLocations.value.contains(item.festival.title)){
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

    fun submitList(newItems:List<FestivalSelectUiState>){
        items = newItems.toList()
        notifyDataSetChanged()
    }
}

