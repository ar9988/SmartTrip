package com.neungi.moyeo.views.aiplanning.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.neungi.moyeo.R
import com.neungi.moyeo.databinding.ItemSelectedDestinationBinding
import com.neungi.moyeo.views.aiplanning.adapters.SelectedLocationAdapter.LocationUiModel
import com.neungi.moyeo.views.aiplanning.viewmodel.AIPlanningViewModel
import kotlinx.coroutines.launch
class SelectedPlaceAdapter(
    private val viewModel: AIPlanningViewModel,
    private val lifecycleOwner: LifecycleOwner
) : ListAdapter<SelectedPlaceAdapter.PlaceUiModel, SelectedPlaceAdapter.ViewHolder>(PlaceDiffCallback()) {

    sealed class PlaceUiModel {
        data class PlaceName(val name: String) : PlaceUiModel()
        data class AddButton(val isEmpty: Boolean) : PlaceUiModel()
    }

    init {
        lifecycleOwner.lifecycleScope.launch {
            viewModel.selectedPlaces.collect { places ->
                val uiModels = when {
                    places.isEmpty() -> {
                        listOf(PlaceUiModel.AddButton(true))
                    }

                    places.size >= 3 -> {
                        places.map { PlaceUiModel.PlaceName(it) }
                    }

                    else -> {
                        places.map { PlaceUiModel.PlaceName(it) } + PlaceUiModel.AddButton(false)  // 그 외에는 Location + AddButton
                    }
                }
                submitList(uiModels)
            }
        }
    }

    class ViewHolder(val binding: ItemSelectedDestinationBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(
            model: PlaceUiModel,
            onDeleteClick: () -> Unit,
            onAddClick: () -> Unit
        ) {
            when (model) {
                is PlaceUiModel.PlaceName -> {
                    binding.text = model.name
                    binding.isAddButton = false
                    binding.chipSelectedLocal.apply {
                        setOnClickListener { onDeleteClick() }
                    }
                }
                is PlaceUiModel.AddButton -> {
                    binding.text = if (model.isEmpty) "추가하기" else ""
                    binding.isAddButton = !model.isEmpty
                    if (model.isEmpty) {
                        binding.chipSelectedLocal.apply {
                            setCloseIcon(ContextCompat.getDrawable(context, R.drawable.baseline_add_circle_24))
                            setOnClickListener { onAddClick() }
                        }
                    } else {
                        binding.btnAddLocal.setOnClickListener { onAddClick() }
                    }
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemSelectedDestinationBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)
        when (item) {
            is PlaceUiModel.PlaceName -> {
                holder.bind(item, { viewModel.togglePlaceSelection(item.name) }, {viewModel.onClickGoToSearchPlace()})
            }
            is PlaceUiModel.AddButton -> {
                holder.bind(item, {}, {viewModel.onClickGoToSearchPlace()})
            }
        }
    }

    class PlaceDiffCallback : DiffUtil.ItemCallback<PlaceUiModel>() {
        override fun areItemsTheSame(oldItem: PlaceUiModel, newItem: PlaceUiModel): Boolean {
            return when {
                oldItem is PlaceUiModel.PlaceName && newItem is PlaceUiModel.PlaceName ->
                    oldItem.name == newItem.name
                oldItem is PlaceUiModel.AddButton && newItem is PlaceUiModel.AddButton ->
                    oldItem.isEmpty == newItem.isEmpty
                else -> false
            }
        }

        override fun areContentsTheSame(oldItem: PlaceUiModel, newItem: PlaceUiModel): Boolean {
            return areItemsTheSame(oldItem, newItem)
        }
    }
}