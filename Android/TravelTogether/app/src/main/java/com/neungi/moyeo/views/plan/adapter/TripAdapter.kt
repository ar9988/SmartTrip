package com.neungi.moyeo.views.plan.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import coil.transform.RoundedCornersTransformation
import com.neungi.domain.model.Trip
import com.neungi.moyeo.R
import com.neungi.moyeo.databinding.ItemTripBinding
import com.neungi.moyeo.util.CommonUtils
import com.neungi.moyeo.util.CommonUtils.formatZonedDateTimeWithZone
import com.neungi.moyeo.util.RegionMapper
import timber.log.Timber

class TripAdapter(
    private val onItemClick: (Trip) -> Unit,
    private val onDeleteClick: (Trip) -> Unit,
    private val regionMapper: RegionMapper
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
            val titles = trip.title.split(" ")
            binding.apply {
                this.trip = trip
                tvDateRange.text = "${formatZonedDateTimeWithZone(trip.startDate,"yy.MM.dd")} ~ ${formatZonedDateTimeWithZone(trip.endDate,"yy.MM.dd")}"
                tvTodayLabel.text = CommonUtils.getDdayText(trip.startDate,trip.endDate)
                val image = regionMapper.getRegionDrawable(titles.firstOrNull() ?: "")
                ivPlan.load(image){
                    transformations(RoundedCornersTransformation(radius = 16f))
                    error(R.drawable.image_noimg)
                }

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
