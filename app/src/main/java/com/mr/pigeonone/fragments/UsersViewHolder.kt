package com.mr.pigeonone.fragments

import android.view.View
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.mr.pigeonone.R
import com.mr.pigeonone.databinding.ListItemBinding
import com.mr.pigeonone.models.User
import com.squareup.picasso.Picasso


class UsersViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val binding:ListItemBinding= ListItemBinding.bind(itemView)
    fun bind(user: User, onClick: (name: String, photo: String, id: String) -> Unit) =
        with(itemView) {
            binding.countTv.isVisible = false
            binding.timeTv.isVisible = false

            binding.titleTv.text = user.name
            binding.subTitleTv.text = user.status
            Picasso.get()
                .load(user.thumbImage)
                .placeholder(R.drawable.defaultavatar)
                .error(R.drawable.defaultavatar)
                .into(binding.userImgView)
            setOnClickListener {
                onClick.invoke(user.name, user.thumbImage, user.uid)
            }
        }
}

class EmptyViewHolder(view: View) : RecyclerView.ViewHolder(view)