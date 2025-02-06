package com.neungi.moyeo.views.plan.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.neungi.domain.model.Place
import com.neungi.moyeo.databinding.ItemSearchPlaceBinding

class PlaceAdapter(
    private val onClick: (Place) -> Unit
) : ListAdapter<Place, PlaceAdapter.ViewHolder>(PlaceDiffCallback()) {

    // ViewHolder는 기존과 동일합니다.
    inner class ViewHolder(private val binding: ItemSearchPlaceBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(data: Place) {
            binding.tvPlaceName.text = data.title
            binding.tvPlaceCategory.text = data.category
//            binding.onItemClick = View.OnClickListener {
//                onClick(data)
//            }
        }
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemSearchPlaceBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val place = getItem(position) // ListAdapter는 getItem() 메서드를 제공
        holder.bind(place)
    }

    class PlaceDiffCallback : DiffUtil.ItemCallback<Place>() {
        override fun areItemsTheSame(oldItem: Place, newItem: Place): Boolean {
            return oldItem.title == newItem.title
        }

        override fun areContentsTheSame(oldItem: Place, newItem: Place): Boolean {
            // 아이템의 내용이 동일한지 비교하는 로직.
            return oldItem == newItem
        }
    }
}
