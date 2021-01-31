package com.mr.pigeonone.fragments

import android.os.Build
import android.view.View
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.mr.pigeonone.R
import com.mr.pigeonone.databinding.ListItemBinding
import com.mr.pigeonone.models.Inbox
import com.mr.pigeonone.utils.formatAsListItem
import com.squareup.picasso.Picasso

class ChatViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val binding: ListItemBinding= ListItemBinding.bind(itemView)
    private val countTv:TextView=binding.countTv
    private val titleTv:TextView=binding.titleTv
    private val subTitleTv:TextView=binding.subTitleTv
    private val timeTv:TextView=binding.timeTv
    private val userImgView:com.google.android.material.imageview.ShapeableImageView=binding.userImgView
    @RequiresApi(Build.VERSION_CODES.N)
    fun bind(item: Inbox, onClick: (name: String, photo: String, id: String) -> Unit) =
        with(itemView) {
            countTv.isVisible = item.count > 0
            countTv.text = item.count.toString()
            timeTv.text = item.time.formatAsListItem(context)

            titleTv.text = item.name
            subTitleTv.text = item.msg
            Picasso.get()
                .load(item.image)
                .placeholder(R.drawable.defaultavatar)
                .error(R.drawable.defaultavatar)
                .into(userImgView)
            setOnClickListener {
                onClick.invoke(item.name.toString(), item.image.toString(), item.from.toString()) //used higher order functn to handle click
            }
        }
}

