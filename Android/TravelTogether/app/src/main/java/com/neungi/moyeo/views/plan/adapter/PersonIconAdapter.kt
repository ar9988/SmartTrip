package com.neungi.moyeo.views.plan.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.neungi.moyeo.R
import com.neungi.moyeo.databinding.ItemPersonIconBinding

class PersonIconAdapter(private val urls: List<String>) : RecyclerView.Adapter<PersonIconAdapter.PersonIconViewHolder>() {

    inner class PersonIconViewHolder(private val binding: ItemPersonIconBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(url: String) {
            // Glide로 URL을 원형 아이콘으로 로드
            Glide.with(itemView.context)
                .load(url)
                .circleCrop() // 원형으로 크롭
                .into(binding.ivPersonIcon)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PersonIconViewHolder {
        val binding = ItemPersonIconBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PersonIconViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PersonIconViewHolder, position: Int) {
        holder.bind(urls[position])
    }

    override fun getItemCount(): Int = urls.size
}
