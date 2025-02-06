package com.neungi.moyeo.views.aiplanning.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.neungi.domain.model.Place
import com.neungi.moyeo.databinding.ItemSearchPlaceBinding

import com.neungi.moyeo.views.aiplanning.viewmodel.AIPlanningViewModel
import kotlinx.coroutines.launch

class SearchPlaceAdapter(private val viewModel: AIPlanningViewModel,
) : ListAdapter<Place, SearchPlaceAdapter.ViewHolder>(SearchPlaceDiffCallback()) {
     
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(ItemSearchPlaceBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))

    }

    inner class ViewHolder(
        private val binding: ItemSearchPlaceBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: Place) {
            binding.tvPlaceName.text = item.placeName
            binding.tvPlaceCategory.text = item.address
            binding.root.setOnClickListener {
                viewModel.togglePlaceSelection(item.placeName)
                viewModel.onClickPopBackToDestiination()

            }

        }
    }
    class SearchPlaceDiffCallback : DiffUtil.ItemCallback<Place>() {
        override fun areItemsTheSame(oldItem: Place, newItem: Place): Boolean {
            return oldItem.placeName == newItem.placeName
        }

        override fun areContentsTheSame(oldItem: Place, newItem: Place): Boolean {
            return oldItem == newItem
        }
    }



}

