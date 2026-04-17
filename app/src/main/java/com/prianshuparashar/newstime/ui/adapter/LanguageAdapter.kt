package com.prianshuparashar.newstime.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.prianshuparashar.newstime.common.constant.Const
import com.prianshuparashar.newstime.data.model.Language
import com.prianshuparashar.newstime.databinding.ItemLanguageBinding
import com.prianshuparashar.newstime.ui.util.DiffCallback

class LanguageAdapter(
    private val onItemClick: (Language) -> Unit
) : ListAdapter<Language, LanguageAdapter.LanguageViewHolder>(
    DiffCallback(
        { old, new -> old.code == new.code },
        { old, new -> old == new }
    )
) {
    private var selectedPosition = Const.SELECTED_POSITION_NONE

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        LanguageViewHolder(ItemLanguageBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: LanguageViewHolder, position: Int) =
        holder.bind(getItem(position), position == selectedPosition)

    inner class LanguageViewHolder(
        private val binding: ItemLanguageBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(language: Language, isSelected: Boolean) = with(binding) {
            root.isSelected = isSelected
            textLanguage.text = language.name
            root.setOnClickListener {
                val previousPosition = selectedPosition
                selectedPosition = bindingAdapterPosition
                notifyItemChanged(previousPosition)
                notifyItemChanged(selectedPosition)
                onItemClick(language)
            }
        }
    }
}