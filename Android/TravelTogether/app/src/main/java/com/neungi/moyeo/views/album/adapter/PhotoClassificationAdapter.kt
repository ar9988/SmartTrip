package com.neungi.moyeo.views.album.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.neungi.moyeo.views.album.PhotoClassifiedFragment

class PhotoClassificationAdapter(fm: FragmentActivity, private val size: Int) :
    FragmentStateAdapter(fm) {

    override fun getItemCount(): Int = size

    override fun createFragment(position: Int): Fragment {
        val itemId = getItemId(position)

        return PhotoClassifiedFragment.newInstance(itemId)
    }

    override fun getItemId(position: Int): Long = (position - START_POSITION).toLong()

    override fun containsItem(itemId: Long): Boolean =
        ((itemId.toInt() >= START_POSITION) && (itemId.toInt() < size))

    companion object {

        const val START_POSITION = 0
    }
}