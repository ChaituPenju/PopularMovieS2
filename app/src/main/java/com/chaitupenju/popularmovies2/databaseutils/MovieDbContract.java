package com.chaitupenju.popularmovies2.databaseutils;

import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;

public class MovieDbContract {

    public static final String CONTENT_AUTHORITY = "com.chaitupenju.popularmovies2";

    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    public static final String PATH_MOVIE = "moviedata";

    public static final class MovieEntry implements BaseColumns{

        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon()
                .appendPath(PATH_MOVIE)
                .build();

        public static final String TABLE_NAME = "FavouriteMovies";
        public static final String MOVIE_ID = "id";
        public static final String MOVIE_TITLE = "title";
        public static final String MOVIE_OVERVIEW = "overview";
        public static final String MOVIE_POSTER= "poster";
        public static final String MOVIE_POSTER_PATH = "poster_path";
        public static final String MOVIE_AVG = "voteAverage";
        public static final String MOVIE_RELEASE_DATE = "releaseDate";
        public static final String MOVIE_TRAILERS = "trailers";
        public static final String MOVIE_REVIEWS = "reviews";

        public static Uri buildMovieUriWithId(long id) {
            return ContentUris.withAppendedId(BASE_CONTENT_URI, id);
        }
    }

}
