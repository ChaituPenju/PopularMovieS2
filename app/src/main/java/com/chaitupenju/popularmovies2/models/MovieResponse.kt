package com.chaitupenju.popularmovies2.models

import com.google.gson.annotations.SerializedName

data class MovieResponse(
    @SerializedName("page")
    val page: Int,

    @SerializedName("results")
    val movieResults: List<MovieDetail>
)