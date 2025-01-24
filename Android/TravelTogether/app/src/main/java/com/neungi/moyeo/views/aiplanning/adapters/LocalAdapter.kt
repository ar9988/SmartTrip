package com.neungi.moyeo.views.aiplanning.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.chip.Chip
import com.neungi.moyeo.R

class LocalAdapter(private val items: List<String>) : RecyclerView.Adapter<LocalAdapter.LocalViewHolder>() {
    inner class LocalViewHolder(private val chip: Chip) : RecyclerView.ViewHolder(chip) {
        fun bind(text: String) {
            chip.text = text
        }
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LocalViewHolder {
        val chip = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_local_big_chip, parent, false) as Chip

        return LocalViewHolder(chip)
    }

    override fun onBindViewHolder(holder: LocalViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount() = items.size
}

