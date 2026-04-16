package com.prianshuparashar.newstime.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.prianshuparashar.newstime.common.dispatcher.DispatcherProvider
import com.prianshuparashar.newstime.databinding.ItemLoadStateBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class LoadStateAdapter(
    private val scope: CoroutineScope,
    private val dispatcherProvider: DispatcherProvider,
    private val retry: () -> Unit
) : RecyclerView.Adapter<LoadStateAdapter.LoadStateViewHolder>() {

    private var loadState: LoadState = LoadState.NotLoading

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LoadStateViewHolder =
        LoadStateViewHolder(ItemLoadStateBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: LoadStateViewHolder, position: Int) =
        holder.bind(loadState)

    override fun getItemCount(): Int = if (loadState == LoadState.NotLoading) 0 else 1

    inner class LoadStateViewHolder(private val binding: ItemLoadStateBinding) :
        RecyclerView.ViewHolder(binding.root) {

        init {
            binding.buttonRetry.setOnClickListener { retry() }
        }

        fun bind(state: LoadState) = with(binding) {
            when (state) {
                LoadState.Loading -> {          // Object - Equality Match
                    progressBar.isVisible = true
                    buttonRetry.isVisible = false
                    textViewError.isVisible = false
                }

                LoadState.NotLoading -> {       // Object - Equality Match
                    progressBar.isVisible = false
                    buttonRetry.isVisible = false
                    textViewError.isVisible = false
                }

                is LoadState.Error -> {         // Data Class - Property Match + 'is' for Type Check
                    progressBar.isVisible = false
                    buttonRetry.isVisible = true
                    textViewError.isVisible = true
                    textViewError.text = state.message
                }
            }
        }
    }

    fun setLoadState(newLoadState: LoadState) {
        val previousState = loadState
        loadState = newLoadState
        if (previousState != newLoadState) {
            scope.launch(dispatcherProvider.immediate) {
                when {
                    previousState == LoadState.NotLoading -> notifyItemInserted(0)
                    newLoadState == LoadState.NotLoading -> notifyItemRemoved(0)
                    else -> notifyItemChanged(0)
                }
            }
        }
    }

    sealed class LoadState {
        data object Loading : LoadState()
        data object NotLoading : LoadState()
        data class Error(val message: String) : LoadState()
    }
}