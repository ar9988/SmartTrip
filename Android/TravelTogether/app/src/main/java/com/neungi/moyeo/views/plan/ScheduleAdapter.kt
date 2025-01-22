//package com.neungi.moyeo.views.plan
//
//import android.view.LayoutInflater
//import android.view.ViewGroup
//import androidx.databinding.DataBindingUtil
//import androidx.recyclerview.widget.DiffUtil
//import androidx.recyclerview.widget.ListAdapter
//import com.neungi.moyeo.R
//
//class ScheduleAdapter(
////    private val onItemClick: (Schedule) -> Unit,
////    private val onEditClick: (Schedule) -> Unit,
////    private val onDeleteClick: (Schedule) -> Unit
//) : ListAdapter<Schedule, ScheduleAdapter.ScheduleViewHolder>(ScheduleDiffCallback()) {
//
//    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ScheduleViewHolder {
//        val binding = DataBindingUtil.inflate<ItemScheduleBinding>(
//            LayoutInflater.from(parent.context),
//            R.layout.item_schedule,
//            parent,
//            false
//        )
//        return ScheduleViewHolder(binding)
//    }
//
//    override fun onBindViewHolder(holder: ScheduleViewHolder, position: Int) {
//        val schedule = getItem(position)
//        holder.bind(schedule)
//    }
//
//    inner class ScheduleViewHolder(private val binding: ItemScheduleBinding) : RecyclerView.ViewHolder(binding.root) {
//        fun bind(schedule: Schedule) {
//            binding.schedule = schedule
//            binding.root.setOnClickListener {
//                onItemClick(schedule)
//            }
//            binding.buttonEdit.setOnClickListener {
//                onEditClick(schedule)
//            }
//            binding.buttonDelete.setOnClickListener {
//                onDeleteClick(schedule)
//            }
//        }
//    }
//}
//
//// DiffUtil.ItemCallback 구현
//class ScheduleDiffCallback : DiffUtil.ItemCallback<Schedule>() {
//    override fun areItemsTheSame(oldItem: Schedule, newItem: Schedule): Boolean {
//        return oldItem.id == newItem.id // 각 Schedule 객체의 고유 ID 비교
//    }
//
//    override fun areContentsTheSame(oldItem: Schedule, newItem: Schedule): Boolean {
//        return oldItem == newItem // 전체 객체 내용 비교
//    }
//}
