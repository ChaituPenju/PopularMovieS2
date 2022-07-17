package com.chaitupenju.popularmovies2.network

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object PopularMoviesNetwork {
    private const val BASE_URL = "https://api.themoviedb.org/3/movie/"

    // todo move to gradle file
    private const val API_KEY = "api_key"
    private const val API_VALUE = "e62aae5e1c8389286f8dd8a887787ff7"

    private const val LANGUAGE_PARAM = "language"
    private const val LANGUAGE_NAME = "en-US"
    const val PAGE = "page"


    private val popularMoviesClient = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(
            OkHttpClient.Builder()
                .addInterceptor(
                    HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY)
                )
                .build()
        )
        .addConverterFactory(GsonConverterFactory.create())
        .build()


    val moviesQueryMap = hashMapOf<String, String>(
        API_KEY to API_VALUE,
        LANGUAGE_PARAM to LANGUAGE_NAME
    )

    val movieDbService = popularMoviesClient.create(TheMovieDbService::class.java)
}