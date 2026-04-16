package com.prianshuparashar.newstime.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.prianshuparashar.newstime.R
import com.prianshuparashar.newstime.data.model.ApiArticle
import com.prianshuparashar.newstime.databinding.ItemNewsBinding
import com.prianshuparashar.newstime.ui.util.DiffCallback

class NewsAdapter(
    private val onItemClick: (ApiArticle) -> Unit
) : ListAdapter<ApiArticle, NewsAdapter.NewsViewHolder>(DIFF_CALLBACK) {

    companion object {
        private val DIFF_CALLBACK = DiffCallback<ApiArticle>(
            itemsTheSame = { old, new -> old.url == new.url },
            contentsTheSame = { old, new -> old == new }
        )
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NewsViewHolder =
        NewsViewHolder(ItemNewsBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: NewsViewHolder, position: Int) =
        holder.bind(getItem(position))

    inner class NewsViewHolder(private val binding: ItemNewsBinding) :
        RecyclerView.ViewHolder(binding.root) {
        init {
            binding.root.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) onItemClick(getItem(position))
            }
        }

        fun bind(article: ApiArticle) {
            with(binding) {
                textTitle.text = article.title.orEmpty()
                textDescription.text = article.description.orEmpty()
                textSource.text = article.source?.name.orEmpty()
            }
            setNewsImage(article)
        }

        private fun setNewsImage(article: ApiArticle) {
            val imageUrl = article.urlToImage?.takeIf { it.isNotBlank() }
            binding.imageNews.visibility = if (imageUrl != null) View.VISIBLE else View.GONE
            if (imageUrl != null) {
                Glide.with(binding.root.context)
                    .load(imageUrl)
                    .centerCrop()
                    .placeholder(R.drawable.ic_image_placeholder)
                    .error(R.drawable.ic_image_placeholder)
                    .into(binding.imageNews)
            } else {
                binding.imageNews.setImageDrawable(null)
            }
        }
    }
}
