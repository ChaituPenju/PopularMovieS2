package com.chaitupenju.popularmovies2.models

import android.util.Log


data class Review(
    val author: String,
    val content: String
) {

    companion object {
        private const val delimitStr = "-TS-"
        private const val delimitReviewStr = "-ROBJ-"

        fun stringToArray(reviewStr: String): ArrayList<Review> {
            val retReviewArr = ArrayList<Review>()
            val revParts: Array<String> = reviewStr.split(delimitStr).toTypedArray()
            for (review in revParts) {
                val revs: Array<String> = review.split(delimitReviewStr).toTypedArray()
                try {
                    retReviewArr.add(Review(revs[0], revs[1]))
                } catch (e: IndexOutOfBoundsException) {
                    e.printStackTrace()
                }
            }
            Log.i("STR2ARR", retReviewArr.toString())
            return retReviewArr
        }

        fun arrayToString(reviewArr: java.util.ArrayList<Review>): String {
            val retStr = StringBuilder()
            try {
                for (i in reviewArr.indices) {
                    retStr.append(reviewArr[i].author).append(delimitReviewStr).append(
                        reviewArr[i].content
                    )
                    if (i < reviewArr.size - 1) {
                        retStr.append(delimitStr)
                    }
                }
            } catch (npe: NullPointerException) {
                return ""
            }
            return retStr.toString()
        }
    }
}