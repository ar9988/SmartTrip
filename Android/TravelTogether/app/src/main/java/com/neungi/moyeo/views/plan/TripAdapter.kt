import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.neungi.moyeo.databinding.ItemTripBinding
import com.neungi.moyeo.views.plan.tripviewmodel.TripData

class TripAdapter(
    private val onItemClick: (Int) -> Unit,
    private val onDeleteClick: (Int) -> Unit,
    private val onEditClick: (Int) -> Unit
) : ListAdapter<TripData, TripAdapter.TripViewHolder>(TripDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TripViewHolder {
        val binding = ItemTripBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TripViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TripViewHolder, position: Int) {
        val trip = getItem(position)
        holder.bind(trip)
    }

    inner class TripViewHolder(private val binding: ItemTripBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(trip: TripData) {
            binding.apply {

                this.trip = trip
                onDeleteClick = View.OnClickListener {
                    onDeleteClick(trip.tripId)
                }
                onEditClick = View.OnClickListener {
                    onEditClick(trip.tripId)
                }
                onItemClick = View.OnClickListener{
                    onItemClick(trip.tripId)
                }
                executePendingBindings()
            }
        }
    }


    // DiffUtil을 사용하여 리스트 변경을 최적화
    class TripDiffCallback : DiffUtil.ItemCallback<TripData>() {
        override fun areItemsTheSame(oldItem: TripData, newItem: TripData): Boolean {
            return oldItem.tripId == newItem.tripId
        }

        override fun areContentsTheSame(oldItem: TripData, newItem: TripData): Boolean {
            return oldItem == newItem
        }
    }
}
