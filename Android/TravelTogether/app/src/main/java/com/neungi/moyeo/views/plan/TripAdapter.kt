//package com.neungi.moyeo.views.plan
//
//import android.view.LayoutInflater
//import android.view.ViewGroup
//import androidx.databinding.DataBindingUtil
//import androidx.recyclerview.widget.DiffUtil
//import androidx.recyclerview.widget.ListAdapter
//import com.neungi.moyeo.R
//import com.neungi.moyeo.databinding.ItemTripBinding
//
//class TripAdapter(
//    private val onItemClick: (Trip) -> Unit,
//    private val onEditClick: (Trip) -> Unit,
//    private val onDeleteClick: (Trip) -> Unit
//) : ListAdapter<Trip, TripAdapter.TripViewHolder>(TripDiffCallback()) {
//
//    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TripViewHolder {
//        val binding = DataBindingUtil.inflate<ItemTripBinding>(
//            LayoutInflater.from(parent.context),
//            R.layout.item_trip,
//            parent,
//            false
//        )
//        return TripViewHolder(binding)
//    }
//
//    override fun onBindViewHolder(holder: TripViewHolder, position: Int) {
//        val trip = getItem(position)  // ListAdapter에서 제공하는 getItem 사용
//        holder.bind(trip)
//    }
//
//    inner class TripViewHolder(private val binding: ItemTripBinding) : androidx.recyclerview.widget.RecyclerView.ViewHolder(binding.root) {
//        fun bind(trip: Trip) {
//            binding.trip = trip
//            binding.root.setOnClickListener {
//                onItemClick(trip)
//            }
//            binding.buttonEdit.setOnClickListener {
//                onEditClick(trip)
//            }
//            binding.buttonDelete.setOnClickListener {
//                onDeleteClick(trip)
//            }
//        }
//    }
//}
//
//// DiffUtil.ItemCallback 구현
//class TripDiffCallback : DiffUtil.ItemCallback<Trip>() {
//    override fun areItemsTheSame(oldItem: Trip, newItem: Trip): Boolean {
//        return oldItem.id == newItem.id // 각 Trip 객체의 고유 ID 비교
//    }
//
//    override fun areContentsTheSame(oldItem: Trip, newItem: Trip): Boolean {
//        return oldItem == newItem // 전체 객체 내용 비교
//    }
//}
