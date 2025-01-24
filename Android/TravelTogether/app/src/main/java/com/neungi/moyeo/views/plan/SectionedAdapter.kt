package com.neungi.moyeo.views.plan

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class SectionedAdapter(
    private val sections: MutableList<Section>
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val VIEW_TYPE_SECTION_HEADER = 0
        private const val VIEW_TYPE_ITEM = 1
    }

    private val listItems = mutableListOf<ListItem>()

    init {
        buildListItems()
    }

    private fun buildListItems() {
        listItems.clear()
        sections.forEachIndexed { sectionIndex, section ->
            listItems.add(ListItem.SectionHeader(section.title))
            section.items.forEach { item ->
                listItems.add(ListItem.Item(item, sectionIndex))
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (listItems[position]) {
            is ListItem.SectionHeader -> VIEW_TYPE_SECTION_HEADER
            is ListItem.Item -> VIEW_TYPE_ITEM
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == VIEW_TYPE_SECTION_HEADER) {
            val view = LayoutInflater.from(parent.context)
                .inflate(android.R.layout.simple_list_item_1, parent, false)
            SectionHeaderViewHolder(view)
        } else {
            val view = LayoutInflater.from(parent.context)
                .inflate(android.R.layout.simple_list_item_1, parent, false)
            ItemViewHolder(view)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = listItems[position]) {
            is ListItem.SectionHeader -> (holder as SectionHeaderViewHolder).bind(item.title)
            is ListItem.Item -> (holder as ItemViewHolder).bind(item.name)
        }

    }

    override fun getItemCount(): Int = listItems.size

    fun moveItem(fromPosition: Int, toPosition: Int) {
        val item = listItems.removeAt(fromPosition) as ListItem.Item
        listItems.add(toPosition, item)
        notifyItemMoved(fromPosition, toPosition)
    }

    fun rebuildSections() {
        sections.clear()
        val currentSections = mutableMapOf<String, MutableList<String>>()

        listItems.forEach {
            if (it is ListItem.SectionHeader) {
                currentSections[it.title] = mutableListOf()
            } else if (it is ListItem.Item) {
                val currentSection = listItems[it.sectionIndex] as ListItem.SectionHeader
                currentSections[currentSection.title]?.add(it.name)
            }
        }

        currentSections.forEach { (title, items) ->
            sections.add(Section(title, items))
        }
        buildListItems()
        notifyDataSetChanged()
    }

    class SectionHeaderViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val textView: TextView = view.findViewById(android.R.id.text1)
        fun bind(title: String) {
            textView.text = title
        }
    }

    class ItemViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val textView: TextView = view.findViewById(android.R.id.text1)
        fun bind(name: String) {
            textView.text = name
        }
    }
}

data class Section(
    val title: String,
    val items: MutableList<String>
)

sealed class ListItem {
    data class SectionHeader(val title: String) : ListItem()
    data class Item(val name: String, val sectionIndex: Int) : ListItem()
}

