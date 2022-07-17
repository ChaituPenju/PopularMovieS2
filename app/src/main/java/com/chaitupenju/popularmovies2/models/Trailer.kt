package com.chaitupenju.popularmovies2.models

import android.util.Log

data class Trailer(
    val title: String,
    val url: String
) {

    companion object {
        private const val delimitStr = "-TS-"
        private const val delimitTrailerStr = "-TOBJ-"

        fun arrayToString(trailers: ArrayList<Trailer>): String {
            val res = StringBuilder()

            try {
                for (i in trailers.indices) {
                    res.append(trailers[i].title).append(delimitTrailerStr).append(
                        trailers[i].url
                    )
                    if (i < trailers.size - 1) {
                        res.append(delimitStr)
                    }
                }
            } catch (e: NullPointerException) {
                return ""
            }

            return res.toString()
        }

        fun stringToArray(string: String): ArrayList<Trailer> {
            val elements = string.split(delimitStr).toTypedArray()
            val res = ArrayList<Trailer>()

            for (element in elements) {
                val item = element.split(delimitTrailerStr).toTypedArray()
                try {
                    res.add(Trailer(item[0], item[1]))
                } catch (e: IndexOutOfBoundsException) {
                    e.printStackTrace()
                }
            }

            Log.i("STR2ARR", res.toString())

            return res
        }
    }
}