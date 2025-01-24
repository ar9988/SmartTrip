package com.neungi.moyeo.views.aiplanning.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.neungi.moyeo.databinding.ItemLocationBinding
import com.neungi.moyeo.views.aiplanning.viwmodel.AIPlanningViewModel
import kotlinx.coroutines.launch

class LocationItemAdapter(
    private val locations: List<String>,
    private val viewModel: AIPlanningViewModel,
    private val lifecycleOwner: LifecycleOwner
) : RecyclerView.Adapter<LocationItemAdapter.ViewHolder>() {

    class ViewHolder(val binding: ItemLocationBinding) : RecyclerView.ViewHolder(binding.root) {
//        fun bind(location: String, onItemClick: (String) -> Unit) {
//            binding.tvLocalDetail.text = location
//            binding.root.setOnClickListener {
//                onItemClick(location)
//            }
//        }
        fun bind(location: String){
            binding.tvLocalDetail.text = location


        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemLocationBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(locations[position])
        val location = locations[position]


        lifecycleOwner.lifecycleScope.launch {
            viewModel.selectedLocations.collect { selectedLocations ->
                holder.binding.tvLocalDetail.isSelected = selectedLocations.contains(location)
            }
        }

        holder.itemView.setOnClickListener {
            viewModel.toggleLocationSelection(location)
            notifyItemChanged(position)
        }
//            , onItemClick)
    }

    override fun getItemCount() = locations.size
}