package com.chaitupenju.popularmovies2.adapters

import android.content.Context
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import com.chaitupenju.popularmovies2.R
import com.chaitupenju.popularmovies2.models.Trailer

class TrailersAdapter(
    private val context: Context,
    private val mTrailers: ArrayList<Trailer>
): BaseAdapter() {

    override fun getCount() = mTrailers.size

    override fun getItem(position: Int): Trailer? {
        return if (position >= 0 && position < mTrailers.size) {
            mTrailers[position]
        } else null
    }

    override fun getItemId(position: Int): Long {
        return if (getItem(position) == null) {
            -1L
        } else position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View? {
        var trailerItem = convertView
        val trailer = getItem(position)

        if (trailerItem == null) {
            try {
                val vi: LayoutInflater = LayoutInflater.from(context)
                trailerItem = vi.inflate(R.layout.trailer_list_item, parent, false)
            } catch (e: Exception) {
                Log.e(context.javaClass.simpleName, e.toString())
            }
        }

        if (trailerItem != null) {
            (trailerItem.findViewById<View>(R.id.tv_trailer_item_title) as TextView).text =
                trailer!!.title
        }

        return trailerItem
    }

    private fun clear() {
        mTrailers.clear()
        notifyDataSetChanged()
    }

    fun setTrailers(trailers: ArrayList<Trailer>) {
        clear()
        mTrailers.addAll(trailers)
        notifyDataSetChanged()
    }

    fun getTrailerUri(position: Int): Uri? {
        val trailer = getItem(position)

        return if (trailer != null) {
            Uri.parse(trailer.url)
        } else null
    }
}