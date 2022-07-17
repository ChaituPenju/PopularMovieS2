package com.chaitupenju.popularmovies2.network

import com.chaitupenju.popularmovies2.models.MovieResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.QueryMap


interface TheMovieDbService {

    @GET("popular")
    suspend fun moviesList(@QueryMap queryMap: Map<String, String>): Response<MovieResponse>
}