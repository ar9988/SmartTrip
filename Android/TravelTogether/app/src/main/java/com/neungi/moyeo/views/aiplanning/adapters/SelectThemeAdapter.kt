package com.neungi.moyeo.views.aiplanning.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.neungi.domain.model.Festival
import com.neungi.domain.model.ThemeItem
import com.neungi.moyeo.R
import com.neungi.moyeo.databinding.ItemThemeBinding
import com.neungi.moyeo.views.aiplanning.adapters.SelectedPlaceAdapter.PlaceUiModel
import com.neungi.moyeo.views.aiplanning.viewmodel.AIPlanningViewModel
import com.neungi.moyeo.views.aiplanning.viewmodel.ThemeSelectUiState
import kotlinx.coroutines.launch

class SelectThemeAdapter(
    private val onThemeClick: (String) -> Unit
) : ListAdapter<ThemeSelectUiState, SelectThemeAdapter.ViewHolder>(ThemeDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemThemeBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(private val binding: ItemThemeBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: ThemeSelectUiState) {
            with(binding) {
                // 이미지 로딩 최적화
                ivThemeItem.load(item.themeItem.imgId)
                tvThemeItem.text = item.themeItem.name
                root.setOnClickListener {
                    onThemeClick(item.themeItem.name)
                }
                viewBorder.isVisible = item.isSelected
                ivCheck.isVisible = item.isSelected
            }
        }
    }
    class ThemeDiffCallback : DiffUtil.ItemCallback<ThemeSelectUiState>() {
        override fun areItemsTheSame(oldItem: ThemeSelectUiState, newItem: ThemeSelectUiState): Boolean {
            return oldItem.themeItem.name == newItem.themeItem.name
        }

        override fun areContentsTheSame(oldItem: ThemeSelectUiState, newItem: ThemeSelectUiState): Boolean {
            return oldItem == newItem
        }
    }
}