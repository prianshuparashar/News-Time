package com.prianshuparashar.newstime.ui.util

import androidx.recyclerview.widget.DiffUtil

class DiffCallback<T : Any>(
    private val itemsTheSame: (oldItem: T, newItem: T) -> Boolean,
    private val contentsTheSame: (oldItem: T, newItem: T) -> Boolean
) : DiffUtil.ItemCallback<T>() {
    override fun areItemsTheSame(oldItem: T, newItem: T): Boolean = itemsTheSame(oldItem, newItem)
    override fun areContentsTheSame(oldItem: T, newItem: T): Boolean = contentsTheSame(oldItem, newItem)
}