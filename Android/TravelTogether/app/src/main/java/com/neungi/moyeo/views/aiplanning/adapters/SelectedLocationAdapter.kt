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
import com.neungi.moyeo.views.aiplanning.viewmodel.AIPlanningViewModel
import kotlinx.coroutines.launch

class SelectedLocationAdapter(
    private val viewModel: AIPlanningViewModel,
    private val lifecycleOwner: LifecycleOwner
) : ListAdapter<SelectedLocationAdapter.LocationUiModel, SelectedLocationAdapter.ViewHolder>(LocationDiffCallback()) {

    sealed class LocationUiModel {
        data class Location(val name: String) : LocationUiModel()
        data class AddButton(val isEmpty: Boolean) : LocationUiModel()
    }

    init {
        lifecycleOwner.lifecycleScope.launch {
            viewModel.selectedLocations.collect { locations ->
                val uiModels = when {
                    locations.isEmpty() -> {
                        listOf(LocationUiModel.AddButton(true))
                    }
                    locations.size >= 3 -> {
                        locations.map { LocationUiModel.Location(it) }
                    }
                    else -> {
                        locations.map { LocationUiModel.Location(it) } + LocationUiModel.AddButton(false)  // 그 외에는 Location + AddButton
                    }
                }
                submitList(uiModels)
            }
        }
    }

    inner class ViewHolder(val binding: ItemSelectedDestinationBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(
            model: LocationUiModel,
            onDeleteClick: () -> Unit,
            onAddClick: () -> Unit
        ) {
            when (model) {
                is LocationUiModel.Location -> {
                    val parseString = model.name.split(" ")
                    binding.text =parseString.last()
                    binding.isAddButton = false
                    binding.chipSelectedLocal.setOnClickListener { onDeleteClick() }
                }
                is LocationUiModel.AddButton -> {
                    binding.text = if (model.isEmpty) "추가하기" else ""  // 빈 리스트일 때만 텍스트 표시
                    binding.isAddButton = !model.isEmpty  // 빈 리스트가 아닐 때만 + 버튼 스타일
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
            is LocationUiModel.Location -> {
                holder.bind(item, { viewModel.toggleLocationSelection(item.name) }, {viewModel.onClickGoToSelectLocal()})
            }
            is LocationUiModel.AddButton -> {
                holder.bind(item, {}, {viewModel.onClickGoToSelectLocal()})
            }
        }
    }

    class LocationDiffCallback : DiffUtil.ItemCallback<LocationUiModel>() {
        override fun areItemsTheSame(oldItem: LocationUiModel, newItem: LocationUiModel): Boolean {
            return when {
                oldItem is LocationUiModel.Location && newItem is LocationUiModel.Location ->
                    oldItem.name == newItem.name
                oldItem is LocationUiModel.AddButton && newItem is LocationUiModel.AddButton ->
                    oldItem.isEmpty == newItem.isEmpty
                else -> false
            }
        }

        override fun areContentsTheSame(oldItem: LocationUiModel, newItem: LocationUiModel): Boolean {
            return areItemsTheSame(oldItem, newItem)
        }
    }


}