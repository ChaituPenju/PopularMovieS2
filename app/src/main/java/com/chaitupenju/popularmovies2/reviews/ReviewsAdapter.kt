package com.chaitupenju.popularmovies2.reviews

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import com.chaitupenju.popularmovies2.R

class ReviewsAdapter(
    private val context: Context,
    private val mReviews: ArrayList<Review>
): BaseAdapter() {

    override fun getCount() = mReviews.size

    override fun getItem(position: Int): Review? {
        return if (position >= 0 && position < mReviews.size) mReviews[position] else null
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        var reviewItem = convertView
        val review = getItem(position)

        if (reviewItem == null) {
            try {
                val li: LayoutInflater = LayoutInflater.from(context)
                reviewItem = li.inflate(R.layout.review_list_item, parent, false)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        if (reviewItem != null) {
            (reviewItem.findViewById<View>(R.id.tv_review_item_author) as TextView).text =
                review!!.author
            (reviewItem.findViewById<View>(R.id.tv_review_item_content) as TextView).text =
                review.content
        }

        return reviewItem!!
    }


    private fun clear() {
        mReviews.clear()
        notifyDataSetChanged()
    }

    fun setReviews(reviews: ArrayList<Review>) {
        clear()
        mReviews.addAll(reviews)
        notifyDataSetChanged()
    }
}