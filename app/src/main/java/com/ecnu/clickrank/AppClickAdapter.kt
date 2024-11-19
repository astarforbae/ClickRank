package com.ecnu.clickrank

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class AppClickAdapter(private val dataList: List<AppClickData>) :
    RecyclerView.Adapter<AppClickAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val appNameTextView: TextView = view.findViewById(R.id.appNameTextView)
        val clickCountTextView: TextView = view.findViewById(R.id.clickCountTextView)
        val appIconImageView: ImageView = view.findViewById(R.id.appIconImageView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_app_click, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val data = dataList[position]
        holder.appNameTextView.text = data.appName
        holder.clickCountTextView.text = "Clicks: ${data.clickCount}"
        holder.appIconImageView.setImageDrawable(data.appIcon)
    }

    override fun getItemCount(): Int = dataList.size
}

