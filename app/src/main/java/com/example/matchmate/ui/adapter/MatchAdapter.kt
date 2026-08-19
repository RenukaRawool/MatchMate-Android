package com.example.matchmate.ui.adapter

import android.transition.AutoTransition
import android.transition.Transition
import android.transition.TransitionManager
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.matchmate.R
import com.example.matchmate.data.local.UserEntity
import com.example.matchmate.databinding.ItemMatchCardBinding

class MatchAdapter(
    private val onAccept: (String) -> Unit,
    private val onDecline: (String) -> Unit
) : ListAdapter<UserEntity, MatchAdapter.MatchViewHolder>(UserDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MatchViewHolder {
        val binding = ItemMatchCardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MatchViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MatchViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class MatchViewHolder(
        private val binding: ItemMatchCardBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(user: UserEntity) = with(binding) {
            this.user = user
            executePendingBindings()
            Glide.with(root)
                .load(user.pictureUrl)
                .centerCrop()
                .placeholder(R.drawable.placeholder_avatar)
                .into(ivProfile)

            btnAccept.setOnClickListener { onAccept(user.email) }
            btnDecline.setOnClickListener { onDecline(user.email) }
        }
    }
}

class UserDiffCallback : DiffUtil.ItemCallback<UserEntity>() {
    override fun areItemsTheSame(oldItem: UserEntity, newItem: UserEntity) = oldItem.email == newItem.email
    override fun areContentsTheSame(oldItem: UserEntity, newItem: UserEntity) = oldItem == newItem
}
