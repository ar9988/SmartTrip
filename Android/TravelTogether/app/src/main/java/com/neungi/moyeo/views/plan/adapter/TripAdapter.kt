package com.neungi.moyeo.views.plan.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.neungi.domain.model.Trip
import com.neungi.moyeo.databinding.ItemTripBinding
import timber.log.Timber

class TripAdapter(
    private val onItemClick: (Trip) -> Unit,
    private val onDeleteClick: (Trip) -> Unit
) : ListAdapter<Trip, TripAdapter.TripViewHolder>(TripDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TripViewHolder {
        val binding = ItemTripBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TripViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TripViewHolder, position: Int) {
        val trip = getItem(position)
        holder.bind(trip)
    }

    inner class TripViewHolder(private val binding: ItemTripBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(trip: Trip) {
            binding.apply {
                this.trip = trip
                Timber.d(trip.title)
                onDeleteClick = View.OnClickListener {
                    onDeleteClick(trip)
                }
                onItemClick = View.OnClickListener{
                    onItemClick(trip)
                }
                executePendingBindings()
            }
        }
    }


    // DiffUtil을 사용하여 리스트 변경을 최적화
    class TripDiffCallback : DiffUtil.ItemCallback<Trip>() {
        override fun areItemsTheSame(oldItem: Trip, newItem: Trip): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Trip, newItem: Trip): Boolean {
            return oldItem == newItem
        }
    }
}
