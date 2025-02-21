package com.neungi.moyeo.views.plan.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import com.neungi.domain.model.Place
import com.neungi.moyeo.databinding.ItemSearchPlaceBinding
import com.neungi.moyeo.views.aiplanning.adapters.SearchPlaceAdapter
import com.neungi.moyeo.views.aiplanning.viewmodel.AIPlanningViewModel

class AddPlaceAdapter(
    viewModel: AIPlanningViewModel,
    private val onPlaceSelected: (Place) -> Unit // 콜백 등록
) : SearchPlaceAdapter(viewModel) {

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        super.onBindViewHolder(holder, position)
        val place = getItem(position)
        holder.binding.root.setOnClickListener {
            onPlaceSelected(place) // 콜백 호출
        }
    }
}
