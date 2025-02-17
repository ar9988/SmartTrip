package com.neungi.moyeo.views.plan.adapter

import android.annotation.SuppressLint
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.neungi.data.entity.PathReceive
import com.neungi.data.entity.ServerReceive
import com.neungi.domain.model.Path
import com.neungi.domain.model.ScheduleData
import com.neungi.moyeo.R
import com.neungi.moyeo.databinding.ItemSectionHeaderBinding
import com.neungi.moyeo.databinding.ItemScheduleBinding
import com.neungi.moyeo.util.ListItem
import com.neungi.moyeo.util.ScheduleHeader
import com.neungi.moyeo.util.Section
import timber.log.Timber
import java.time.LocalTime

@SuppressLint("SetTextI18n")
class SectionedAdapter(
    private val itemTouchHelper: ItemTouchHelper,
    private val onEditClick: (ScheduleData) -> Unit,
    private val onAddClick: (Int) -> Unit,
    private val onDeletePath: (Int) -> Unit,
    private val handleMarker:(ScheduleData, Boolean) -> Unit,
    private val recyclerView: RecyclerView
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    var sections = mutableListOf<Section>()
    val pathItems = mutableMapOf<Int, Int>() //key = scheduelId , value = travel time
    val pathInfo = mutableMapOf<Int, Pair<Int,Int>>() // key = scheduleId , value = <day,>
    private var listItems = mutableListOf<ListItem>()

    inner class SectionHeaderViewHolder(private val binding: ItemSectionHeaderBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(data: ScheduleHeader) {
            binding.dayInfo = data
            binding.onClick = View.OnClickListener {
                onAddClick(data.dayId)
            }
        }
    }

    inner class ItemViewHolder(private val binding: ItemScheduleBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(data: ScheduleData, path: Int?) {
            binding.titleSchedule.text = data.placeName
            binding.typeSchedule.text = data.positionPath.toString()
            binding.tvFrom.text = data.fromTime.toString()
            binding.tvTo.text = data.toTime.toString()
            if(data.type==1){
                binding.cardSchedule.setBackgroundColor(binding.root.context.getColor(R.color.colorSecondary))
            }
            if(data.type==2){
                binding.cardSchedule.setBackgroundColor(binding.root.context.getColor(R.color.colorPrimary))
            }
            binding.cardSchedule.setOnClickListener {
                onEditClick(data)
            }

            binding.cardSchedule.setOnLongClickListener { view ->
                itemTouchHelper.startDrag(this)
                true
            }
            if (path == null) {
                binding.bottomSection.visibility = View.GONE
            } else {
                binding.bottomSection.visibility = View.VISIBLE
                val hour = path / 60
                val minute = path % 60
                var timeInfo = ""
                if (hour > 0) {
                    timeInfo += "${hour}시간 "
                }
                timeInfo += "${minute}분 이동"
                binding.tvTravelTime.text = timeInfo
            }
        }
    }

    private fun buildTimeInfo() { // 활동 시간, 이동 시간 계산해서 표시
        listItems.forEachIndexed { position, item ->
            if (item is ListItem.Item) {
                if (listItems[position - 1] is ListItem.SectionHeader) {
                    val from = LocalTime.of(9, 0)
                    item.data.fromTime = from
                    item.data.toTime = from.plusMinutes(item.data.duration.toLong())
                } else {
                    val from = (listItems[position - 1] as ListItem.Item).data.toTime
                    val adjustedFrom = from?.plusMinutes(
                        pathItems[(listItems[position - 1] as ListItem.Item).data.scheduleId]?.toLong()
                            ?: 0
                    )
                    item.data.fromTime = adjustedFrom
                    if (adjustedFrom != null) {
                        item.data.toTime = adjustedFrom.plusMinutes(item.data.duration.toLong())
                    }
                }
                if (position + 1 < listItems.size && listItems[position + 1] is ListItem.SectionHeader || position + 1 == listItems.size) {
                    pathItems.remove((listItems[position] as ListItem.Item).data.scheduleId)
                    notifyItemChanged(position)
                    onDeletePath((listItems[position] as ListItem.Item).data.scheduleId)
                }
            }
        }
    }

    fun buildListItems() {
        listItems.clear()
        sections.forEachIndexed { sectionIndex, section ->
            listItems.add(ListItem.SectionHeader(section.head))
            section.items.forEach { item: ScheduleData ->
                listItems.add(ListItem.Item(item, sectionIndex))
            }
        }
        Timber.d(listItems.toString())
        if (!recyclerView.isComputingLayout) {
            notifyDataSetChanged()
        } else {
            // 레이아웃 계산 중일 때는 다음에 처리하도록 Handler를 사용
            Handler(Looper.getMainLooper()).post {
                notifyDataSetChanged()
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
            val binding =
                ItemSectionHeaderBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return SectionHeaderViewHolder(binding)
        } else {
            val binding =
                ItemScheduleBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return ItemViewHolder(binding)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = listItems[position]) {
            is ListItem.SectionHeader -> (holder as SectionHeaderViewHolder).bind(item.data)
            is ListItem.Item -> {
                (holder as ItemViewHolder).bind(
                    item.data,
                    pathItems[item.data.scheduleId]
                )
            }
        }
    }

    override fun getItemCount(): Int = listItems.size

    fun moveItem(fromPosition: Int, toPosition: Int) {
        val item = listItems.removeAt(fromPosition) as ListItem.Item
        listItems.add(toPosition, item)
        notifyItemMoved(fromPosition, toPosition)
    }

    fun rebuildSections() {
        val newSections = mutableListOf<Section>()
        var currentSection: MutableList<ScheduleData>? = null
        val sortedItems = listItems.sortedWith(
            compareBy<ListItem> {
                when (it) {
                    is ListItem.SectionHeader -> it.data.positionPath
                    is ListItem.Item -> it.data.positionPath
                }
            }.thenByDescending { //path 값이 동일할 경우 시간순으로 정렬
                when (it) {
                    is ListItem.SectionHeader -> it.data.positionPath
                    is ListItem.Item -> it.data.timeStamp
                }
            }
        )

        // 섹션별로 아이템 재구성
        sortedItems.forEach { item ->
            when (item) {
                is ListItem.SectionHeader -> {
                    currentSection = mutableListOf()
                    newSections.add(Section(item.data, currentSection!!))
                }

                is ListItem.Item -> {
                    currentSection?.add(item.data)
                    val idx = currentSection?.size ?: 0
                    pathInfo[item.data.scheduleId] = Pair(item.data.positionPath/10000,idx)
                    handleMarker(item.data,true)
                }
            }
        }

        // 2. sections 업데이트
        sections.clear()
        sections.addAll(newSections)

        // 3. listItems 재구성
        val oldList = listItems.toList() // 이전 리스트 복사
        buildListItems() // 새로운 순서로 리스트 아이템 재구성

        // 4. DiffUtil을 사용한 효율적인 업데이트
        val diffResult = DiffUtil.calculateDiff(object : DiffUtil.Callback() {
            override fun getOldListSize() = oldList.size
            override fun getNewListSize() = listItems.size

            override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                val oldItem = oldList[oldItemPosition]
                val newItem = listItems[newItemPosition]
                return when {
                    oldItem is ListItem.SectionHeader && newItem is ListItem.SectionHeader ->
                        oldItem.data.title == newItem.data.title

                    oldItem is ListItem.Item && newItem is ListItem.Item ->
                        oldItem.data.scheduleId == newItem.data.scheduleId

                    else -> false
                }
            }

            override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                return oldList[oldItemPosition] == listItems[newItemPosition]
            }
        })
        // RecyclerView가 레이아웃을 계산 중인지 확인하고 안전하게 업데이트
        buildTimeInfo()
        if (!recyclerView.isComputingLayout) {
            diffResult.dispatchUpdatesTo(this)
        } else {
            // 레이아웃 계산 중일 때는 다음에 처리하도록 Handler를 사용
            Handler(Looper.getMainLooper()).post {
                diffResult.dispatchUpdatesTo(this)
            }
        }
    }

    fun getItem(position: Int): ListItem {
        if (position < 0 || position >= listItems.size) {
            throw IndexOutOfBoundsException("Invalid position")
        }
        return listItems[position]
    }

    fun updateItem(event: ServerReceive, isUserDragging: Boolean) {
        //서버에서 입력받은 수정
        if (event.operation.action == "DELETE") {
            for (position in listItems.indices.reversed()) {
                val item = listItems[position]
                if (item is ListItem.Item && item.data.scheduleId == event.operation.scheduleId) {
                    // 아이템을 삭제
                    listItems.removeAt(position)
                    handleMarker(item.data,false)
                    if(position-1 in listItems.indices && listItems[position-1] is ListItem.Item){
                        onDeletePath((listItems[position-1] as ListItem.Item).data.scheduleId)
                    }
                    onDeletePath(event.operation.scheduleId)
                    if (!isUserDragging) notifyItemRemoved(position)
                }
            }
        } else if (event.operation.action == "MOVE") {
            listItems.forEachIndexed { position, item ->
                if (item is ListItem.Item && item.data.scheduleId == event.operation.scheduleId && item.data.timeStamp < event.timestamp) {
                    item.data.positionPath = event.operation.positionPath
                }
            }
        }

        // 섹션을 재구성하여 순서를 반영
        if (!isUserDragging) rebuildSections()
    }


    fun updateValue(position: Int, newPositionPath: Int) {
        listItems[position] = ListItem.Item(
            (listItems[position] as ListItem.Item).data.copy(positionPath = newPositionPath),
            (listItems[position] as ListItem.Item).sectionIndex
        )
        //내가 수정할때 호출됨
    }

    fun uiUpdate(position: Int) {
        notifyItemChanged(position)
    }


    fun updatePathInfo(pathReceive: PathReceive, isUserDragging: Boolean) {
        pathReceive.paths.forEach {
            pathItems[it.sourceScheduleId] = it.totalTime!!
        }
        if (!isUserDragging) rebuildSections()

    }

    fun addSchedule(event: ScheduleData, isUserDragging: Boolean) {
        listItems.forEach {
            if (it is ListItem.Item && it.data.scheduleId == event.scheduleId) {
                return
            }
        }
        listItems.add(ListItem.Item(event,1))
        if (!isUserDragging) rebuildSections() //notifyItemInserted(index + 1)
        else {
            // 레이아웃 계산 중일 때는 다음에 처리하도록 Handler를 사용
            Handler(Looper.getMainLooper()).post {
                notifyDataSetChanged()
            }
        }

    }

    fun editItem(item: ScheduleData, isUserDragging: Boolean) {
        listItems.forEach {
            if (it is ListItem.Item && it.data.scheduleId == item.scheduleId) {
                it.data.duration = item.duration
                it.data.placeName = item.placeName
                if(!isUserDragging) rebuildSections() // 최적화 여지 있음
                return
            }
        }
    }

    fun delete(scheduleId: Int) {
        listItems.forEachIndexed { index, it ->
            if (it is ListItem.Item && it.data.scheduleId == scheduleId) {
                listItems.removeAt(index)
                handleMarker(it.data,false)
                if(index-1 in listItems.indices && listItems[index-1] is ListItem.Item){
                    onDeletePath((listItems[index-1] as ListItem.Item).data.scheduleId)
                }
                onDeletePath(scheduleId)
                rebuildSections()
                return
            }
        }
    }

    companion object {

        private const val VIEW_TYPE_SECTION_HEADER = 0
        private const val VIEW_TYPE_ITEM = 1
    }
}
