package com.nextgen.keyboard.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.nextgen.keyboard.R
import com.nextgen.keyboard.data.model.Clip
import java.text.SimpleDateFormat
import java.util.*

class ClipboardAdapter(
    private val onItemClick: (Clip) -> Unit,
    private val onPinClick: (Clip) -> Unit
) : ListAdapter<Clip, ClipboardAdapter.ClipViewHolder>(ClipDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ClipViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_clipboard, parent, false)
        return ClipViewHolder(view)
    }

    override fun onBindViewHolder(holder: ClipViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ClipViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val contentText: TextView = itemView.findViewById(R.id.clip_content)
        private val timestampText: TextView = itemView.findViewById(R.id.clip_timestamp)
        private val pinButton: ImageButton = itemView.findViewById(R.id.pin_button)

        fun bind(clip: Clip) {
            contentText.text = clip.content
            timestampText.text = formatTimestamp(clip.timestamp)

            pinButton.setImageResource(
                if (clip.isPinned) R.drawable.ic_pin_filled
                else R.drawable.ic_pin_outline
            )

            itemView.setOnClickListener { onItemClick(clip) }
            pinButton.setOnClickListener { onPinClick(clip) }
        }

        private fun formatTimestamp(timestamp: Long): String {
            val now = System.currentTimeMillis()
            val diff = now - timestamp

            return when {
                diff < 60000 -> "Just now"
                diff < 3600000 -> "${diff / 60000}m ago"
                diff < 86400000 -> "${diff / 3600000}h ago"
                else -> SimpleDateFormat("MMM dd", Locale.getDefault()).format(Date(timestamp))
            }
        }
    }

    private class ClipDiffCallback : DiffUtil.ItemCallback<Clip>() {
        override fun areItemsTheSame(oldItem: Clip, newItem: Clip): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Clip, newItem: Clip): Boolean {
            return oldItem == newItem
        }
    }
}