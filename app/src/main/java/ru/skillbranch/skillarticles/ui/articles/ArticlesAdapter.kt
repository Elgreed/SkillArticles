package ru.skillbranch.skillarticles.ui.articles

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import ru.skillbranch.skillarticles.ui.custom.ArticleItemView
import ru.skillbranch.skillarticles.viewmodels.articles.ArticleItem

class ArticlesAdapter(
    private val onClick: (ArticleItem) -> Unit,
    private val onToggleBookmark: (ArticleItem, Boolean) -> Unit
) : ListAdapter<ArticleItem, ArticleVH>(ArticleDiffCallback()) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ArticleVH {
        return ArticleVH(ArticleItemView(parent.context))
    }

    override fun onBindViewHolder(holder: ArticleVH, position: Int) {
        holder.bind(getItem(position), onClick, onToggleBookmark)
    }
}

class ArticleDiffCallback : DiffUtil.ItemCallback<ArticleItem>() {
    override fun areItemsTheSame(oldItem: ArticleItem, newItem: ArticleItem): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: ArticleItem, newItem: ArticleItem): Boolean {
        return oldItem == newItem
    }

}

class ArticleVH(private val containerView: View) : RecyclerView.ViewHolder(containerView) {
    fun bind(
        item: ArticleItem,
        onClick: (ArticleItem) -> Unit,
        onToggleBookmark: (ArticleItem, Boolean) -> Unit
    ) {
        (containerView as ArticleItemView).bind(item, onClick, onToggleBookmark)
    }
}