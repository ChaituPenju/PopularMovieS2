package com.chaitupenju.popularmovies2.databaseutils;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class MovieDbHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "favouritemovies.db";

    private static final int DATABASE_VERSION = 2;


    public MovieDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
    @Override
    public void onCreate(SQLiteDatabase db) {
        final String SQL_CREATE_MOVIES_TABLE = "CREATE TABLE " + MovieDbContract.MovieEntry.TABLE_NAME + "(" +
                MovieDbContract.MovieEntry.MOVIE_ID + " TEXT PRIMARY KEY, " +
                MovieDbContract.MovieEntry.MOVIE_TITLE + " TEXT NOT NULL, " +
                MovieDbContract.MovieEntry.MOVIE_OVERVIEW + " TEXT NOT NULL, " +
                MovieDbContract.MovieEntry.MOVIE_POSTER + " BLOB NOT NULL, " +
                MovieDbContract.MovieEntry.MOVIE_POSTER_PATH + " TEXT NOT NULL, " +
                MovieDbContract.MovieEntry.MOVIE_AVG + " TEXT NOT NULL, " +
                MovieDbContract.MovieEntry.MOVIE_RELEASE_DATE + " TEXT NOT NULL, " +
                MovieDbContract.MovieEntry.MOVIE_TRAILERS + " TEXT NOT NULL, " +
                MovieDbContract.MovieEntry.MOVIE_REVIEWS + " TEXT NOT NULL" +
                ")";
        db.execSQL(SQL_CREATE_MOVIES_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + MovieDbContract.MovieEntry.TABLE_NAME);
        onCreate(db);
    }
}
