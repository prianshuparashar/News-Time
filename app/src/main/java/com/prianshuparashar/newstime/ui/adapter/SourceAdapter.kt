package com.prianshuparashar.newstime.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.prianshuparashar.newstime.common.constant.Const
import com.prianshuparashar.newstime.data.model.ApiSource
import com.prianshuparashar.newstime.databinding.ItemSourceBinding
import com.prianshuparashar.newstime.ui.util.DiffCallback

class SourceAdapter(
    private val onItemClick: (ApiSource) -> Unit
) : ListAdapter<ApiSource, SourceAdapter.SourceViewHolder>(
    DiffCallback(
    { old, new -> old.id == new.id },
    { old, new -> old == new }
    )
) {
    private var selectedPosition = Const.SELECTED_POSITION_NONE

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        SourceViewHolder(ItemSourceBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: SourceViewHolder, position: Int) = holder.bind(getItem(position), position == selectedPosition)

    inner class SourceViewHolder(
        private val binding: ItemSourceBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(source: ApiSource, isSelected: Boolean) = with(binding) {
            root.isSelected = isSelected
            textSource.text = source.name
            root.setOnClickListener {
                val previousPosition = selectedPosition
                selectedPosition = bindingAdapterPosition
                notifyItemChanged(previousPosition)
                notifyItemChanged(selectedPosition)
                onItemClick(source)
            }
        }
    }
}