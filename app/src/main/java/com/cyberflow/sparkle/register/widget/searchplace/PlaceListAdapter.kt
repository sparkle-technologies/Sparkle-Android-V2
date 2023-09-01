package com.cyberflow.sparkle.register.widget.searchplace

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.cyberflow.sparkle.R

class PlaceListAdapter @JvmOverloads constructor(
    private var placeList: List<PlaceResult>,
    private val placeCallback: PlaceResultCallback
) : RecyclerView.Adapter<PlaceListAdapter.PlaceViewHolder>() {

    private var selPosition = -1

    class PlaceViewHolder(view: View) : ViewHolder(view) {
        val placeNameText: TextView = view.findViewById(R.id.tv_place_result)
    }


    override fun getItemViewType(position: Int): Int {
        return placeList.size
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlaceViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.item_place_layout, parent, false)
        val holder = PlaceViewHolder(view)
        holder.itemView.setOnClickListener {
            selPosition = holder.layoutPosition
            val placeResult = holder.placeNameText.text.toString()
            val placeId = placeList[selPosition].placeId
            placeCallback.onPlaceClicked(placeResult, placeId)
        }
        return holder
    }

    override fun onBindViewHolder(holder: PlaceViewHolder, position: Int) {
        holder.placeNameText.text = placeList[position].placeName
    }

    override fun getItemCount(): Int {
        return placeList.size
    }

    fun setList(list: List<PlaceResult>) {
        placeList = list
        notifyDataSetChanged()
    }

}