package com.chaitupenju.popularmovies2.databaseutils;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

public class MovieProvider extends ContentProvider {

    public static final int CODE_MOVIE = 400;

    private static final UriMatcher sUriMatcher = buildUriMatcher();

    private static UriMatcher buildUriMatcher() {
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);

        final String authority = MovieDbContract.CONTENT_AUTHORITY;
        matcher.addURI(authority, MovieDbContract.PATH_MOVIE, CODE_MOVIE);
        return matcher;
    }

    private MovieDbHelper mOpenHelper;

    @Override
    public boolean onCreate() {
        mOpenHelper = new MovieDbHelper(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        Cursor cursor;
        switch(sUriMatcher.match(uri)){
            case CODE_MOVIE: {
                SQLiteDatabase sdb = mOpenHelper.getReadableDatabase();
                cursor = sdb.query(MovieDbContract.MovieEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
            }
                break;
            default:
                throw new UnsupportedOperationException("Unknown URI:"+uri);
        }
        return cursor;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        return null;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        Uri retUri;
        switch(sUriMatcher.match(uri)){
            case CODE_MOVIE: {
                SQLiteDatabase sdb = mOpenHelper.getWritableDatabase();
                long id = sdb.insert(MovieDbContract.MovieEntry.TABLE_NAME, null, values);
                retUri = MovieDbContract.MovieEntry.buildMovieUriWithId(id);
            }
                break;
            default:
                throw new UnsupportedOperationException("unknown uri"+uri);
        }
        return retUri;
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        int delCount;
        switch (sUriMatcher.match(uri)) {
            case CODE_MOVIE: {
                SQLiteDatabase sdb = mOpenHelper.getWritableDatabase();
                delCount = sdb.delete(MovieDbContract.MovieEntry.TABLE_NAME, selection, selectionArgs);
            }
            break;
            default: {
                throw new UnsupportedOperationException("Unknown uri: " + uri);
            }
        }
        return delCount;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
        return 0;
    }
}