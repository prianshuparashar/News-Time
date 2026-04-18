package com.prianshuparashar.newstime.ui.adapter

import androidx.recyclerview.widget.ListAdapter
import com.prianshuparashar.newstime.common.constant.Const
import com.prianshuparashar.newstime.data.model.Country
import com.prianshuparashar.newstime.databinding.ItemCountryBinding
import com.prianshuparashar.newstime.ui.util.DiffCallback

class CountryAdapter(
    private val onItemClick: (Country) -> Unit
) : ListAdapter<Country, CountryAdapter.CountryViewHolder>(
    DiffCallback(
        { old, new -> old.code == new.code },
        { old, new -> old == new }
    )
) {
    private var selectedPosition = Const.SELECTED_POSITION_NONE

    override fun onCreateViewHolder(parent: android.view.ViewGroup, viewType: Int) =
        CountryViewHolder(
            ItemCountryBinding.inflate(
                android.view.LayoutInflater.from(parent.context),
                parent,
                false
            )
        )

    override fun onBindViewHolder(holder: CountryViewHolder, position: Int) =
        holder.bind(getItem(position), position == selectedPosition)

    inner class CountryViewHolder(
        private val binding: ItemCountryBinding
    ) : androidx.recyclerview.widget.RecyclerView.ViewHolder(binding.root) {

        fun bind(country: Country, isSelected: Boolean) = with(binding) {
            root.isSelected = isSelected
            textCountry.text = country.name
            root.setOnClickListener {
                val previousPosition = selectedPosition
                selectedPosition = bindingAdapterPosition
                notifyItemChanged(previousPosition)
                notifyItemChanged(selectedPosition)
                onItemClick(country)
            }
        }
    }

}