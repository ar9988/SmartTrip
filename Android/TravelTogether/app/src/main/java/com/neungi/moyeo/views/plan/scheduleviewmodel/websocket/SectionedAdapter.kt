package com.neungi.moyeo.views.plan.scheduleviewmodel.websocket

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.neungi.moyeo.R
import com.neungi.moyeo.databinding.ItemSectionHeaderBinding
import com.neungi.moyeo.views.plan.scheduleviewmodel.ScheduleData

class SectionedAdapter(
    private val onItemClick: (Int) -> Unit,
    private val onDeleteClick: (Int) -> Unit,
    private val onEditClick: (Int) -> Unit,
    private val onAddClick: () -> Unit,
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
            val binding = ItemSectionHeaderBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return SectionHeaderViewHolder(binding)
        } else {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_schedule, parent, false)
            ItemViewHolder(view)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = listItems[position]) {
            is ListItem.SectionHeader -> (holder as SectionHeaderViewHolder).bind(item.title)
            is ListItem.Item -> (holder as ItemViewHolder).bind(item.data)
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
        val currentSections = mutableMapOf<String, MutableList<ScheduleData>>()

        listItems.forEach {
            if (it is ListItem.SectionHeader) {
                currentSections[it.title] = mutableListOf()
            } else if (it is ListItem.Item) {
                val currentSection = listItems[it.sectionIndex] as ListItem.SectionHeader
                currentSections[currentSection.title]?.add(it.data)
            }
        }

        currentSections.forEach { (title, items) ->
            sections.add(Section(title, items))
        }
        buildListItems()
        notifyDataSetChanged()
    }

    inner class SectionHeaderViewHolder(private val binding: ItemSectionHeaderBinding) : RecyclerView.ViewHolder(binding.root) {
//        private val textView: TextView = view.findViewById(R.id.tv_section_header)
        fun bind(title: String) {
            binding.tvSectionHeaderIconText.text = title
            binding.onClick = View.OnClickListener {
                onAddClick()
            }
        }
    }

    class ItemViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val titleTextView: TextView = view.findViewById(R.id.title_schedule)

        fun bind(data: ScheduleData) {
            titleTextView.text = data.scheduleTitle
        }
    }

    fun getItem(position: Int): ListItem {
        return listItems[position]
    }

    fun getSectionTitle(sectionIndex: Int): String {
        return sections[sectionIndex].title
    }
}
