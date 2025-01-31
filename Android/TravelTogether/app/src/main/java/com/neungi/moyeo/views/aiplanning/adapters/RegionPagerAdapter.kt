package com.neungi.moyeo.views.aiplanning.adapters

import android.content.Context
import android.view.ViewGroup
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.neungi.moyeo.R
import com.neungi.moyeo.views.aiplanning.viewmodel.AIPlanningViewModel

class RegionPagerAdapter(
    private val context: Context,
    private val viewModel: AIPlanningViewModel,
    private val lifecycleOwner: LifecycleOwner
) : RecyclerView.Adapter<RegionPagerAdapter.ViewHolder>() {

    private val regions = mapOf(
        "특별시/광역시" to R.array.local_special,
        "경기" to R.array.local_gyeonggi,
        "강원" to R.array.local_gangwon,
        "경북" to R.array.local_gyeongbuk,
        "경남" to R.array.local_gyeongnam,
        "전북" to R.array.local_jeonbuk,
        "전남" to R.array.local_jeonnam,
        "충북" to R.array.local_chungbuk,
    )

    inner class ViewHolder(val recyclerView: RecyclerView) : RecyclerView.ViewHolder(recyclerView)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val recyclerView = RecyclerView(parent.context).apply {
            layoutManager = LinearLayoutManager(parent.context)
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        }
        return ViewHolder(recyclerView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val regionName = regions.keys.elementAt(position)
        val arrayId = regions[regionName] ?: return

        if (arrayId != 0) {
            val locations = context.resources.getStringArray(arrayId)
            holder.recyclerView.adapter = LocationItemAdapter(locations.toList(), viewModel,lifecycleOwner)
        }
    }

    override fun getItemCount() = regions.size
}