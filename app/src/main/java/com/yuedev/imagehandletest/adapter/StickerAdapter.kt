package com.yuedev.imagehandletest.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.yuedev.imagehandletest.R
import com.yuedev.imagehandletest.bean.Sticker

/**
 * Created by Yue on 2020/8/21.
 *
 * 贴纸列表
 */
class StickerAdapter(private val itemClick: (index: Int) -> Unit) :
    ListAdapter<Sticker, StickerAdapter.StickerHolder>(StickerDiffCallback) {



    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StickerHolder {
        val itemView =
            LayoutInflater.from(parent.context).inflate(R.layout.item_sticker, parent, false)

        val holder = StickerHolder(itemView)

        itemView.setOnClickListener {
            itemClick(holder.adapterPosition)
        }

        return holder
    }

    override fun onBindViewHolder(holder: StickerHolder, position: Int) {
        holder.bind(getItem(position))
    }






    class StickerHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val stickerNameTextView = itemView.findViewById<TextView>(R.id.stickerNameTextView)
        private val stickerImageView = itemView.findViewById<ImageView>(R.id.stickerImageView)

        fun bind(sticker: Sticker) {

            stickerNameTextView.text = sticker.name
            stickerImageView.setImageResource(sticker.imgResId)
        }

    }


    object StickerDiffCallback : DiffUtil.ItemCallback<Sticker>() {
        override fun areItemsTheSame(oldItem: Sticker, newItem: Sticker) =
            oldItem.imgResId == newItem.imgResId

        override fun areContentsTheSame(oldItem: Sticker, newItem: Sticker) = oldItem == newItem
    }
}

