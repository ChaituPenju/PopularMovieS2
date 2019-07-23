package com.chaitupenju.popularmovies2;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.chaitupenju.popularmovies2.databaseutils.MovieDbContract;
import com.chaitupenju.popularmovies2.datautils.MovieDetails;
import com.chaitupenju.popularmovies2.datautils.NetworkUtils;
import com.chaitupenju.popularmovies2.datautils.ParseMovieJsonData;

import org.json.JSONException;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;

public class PopularMoviesActivity extends AppCompatActivity implements MovieAdapter.MovieAdapterClickHandler,
        SharedPreferences.OnSharedPreferenceChangeListener, LoaderManager.LoaderCallbacks<MovieDetails[]>{


    public static final String MOVIE_SER_KEY = "movie-serialize";
    public static final String MOVIE_BUNDLE_KEY = "movie-key";
    private static final int MOVIE_LOADER_ID = 0;
    private static final String SCROLL_POS_KEY = "scroll-position";
    Parcelable mListState;

    ProgressBar pb_load;
    RecyclerView movieDetails;
    MovieAdapter movieAdapter;
    TextView error_msg;
    RecyclerView.LayoutManager mLayoutManager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_popular_movies);
        pb_load = findViewById(R.id.progress_bar);
        error_msg = findViewById(R.id.tv_error_msg);
        movieDetails = findViewById(R.id.movie_details);
        movieAdapter = new MovieAdapter(this, this);
        mLayoutManager = new GridLayoutManager(this,2);
        movieDetails.setHasFixedSize(true);
        movieDetails.setLayoutManager(mLayoutManager);
        movieDetails.setItemAnimator(new DefaultItemAnimator());
        movieDetails.setAdapter(movieAdapter);
        loadCriteriaFromPref(PreferenceManager.getDefaultSharedPreferences(this));
        PreferenceManager.getDefaultSharedPreferences(this).registerOnSharedPreferenceChangeListener(this);

    }

    protected void onSaveInstanceState(Bundle state) {
        super.onSaveInstanceState(state);
        mListState = mLayoutManager.onSaveInstanceState();
        state.putParcelable(SCROLL_POS_KEY, mListState);

    }

    protected void onRestoreInstanceState(Bundle state) {
        super.onRestoreInstanceState(state);
        if(state != null)
            mListState = state.getParcelable(SCROLL_POS_KEY);
    }

    private void loadCriteriaFromPref(SharedPreferences sp){
        Bundle bundle = new Bundle();
        LoaderManager.LoaderCallbacks<MovieDetails[]> callbacks = PopularMoviesActivity.this;
        bundle.putString(MOVIE_BUNDLE_KEY, sp.getString(getString(R.string.pref_movie_key),getString(R.string.pref_criteria_popular_value)));
        LoaderManager manager = getSupportLoaderManager();
        if(manager == null){
            manager.initLoader(MOVIE_LOADER_ID, bundle, callbacks);
        }
        else{
            manager.restartLoader(MOVIE_LOADER_ID, bundle, callbacks);
        }

    }

    private ArrayList<MovieDetails> getMoviesFromCursor(Cursor cursor) {
        ArrayList<MovieDetails> mCursorDetails = new ArrayList<>();
        if (cursor.getCount() == 0) {
            Log.d("CUR","cursor is null");
            return null;
        }
        if (cursor.moveToFirst()) {
            do {
                MovieDetails movie = new MovieDetails(
                        cursor.getString(cursor.getColumnIndex(MovieDbContract.MovieEntry.MOVIE_ID)),
                        cursor.getString(cursor.getColumnIndex(MovieDbContract.MovieEntry.MOVIE_TITLE)),
                        cursor.getString(cursor.getColumnIndex(MovieDbContract.MovieEntry.MOVIE_POSTER_PATH)),
                        cursor.getString(cursor.getColumnIndex(MovieDbContract.MovieEntry.MOVIE_OVERVIEW)),
                        cursor.getString(cursor.getColumnIndex(MovieDbContract.MovieEntry.MOVIE_AVG)),
                        cursor.getString(cursor.getColumnIndex(MovieDbContract.MovieEntry.MOVIE_RELEASE_DATE))
                );
                movie.setPosterFromCursor(cursor);
                mCursorDetails.add(movie);
            } while (cursor.moveToNext());
        }
        return mCursorDetails;
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.sort_order_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        if(itemId == R.id.movie_criteria){
            Intent in  = new Intent(this, MovieCriteriaActivity.class);
            startActivity(in);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onClick(MovieDetails mIntent) {
        Context context = this;
        Class destinationClass = MovieDetailActivity.class;
        Intent intentMovieDetailActivity = new Intent(context, destinationClass);
        intentMovieDetailActivity.putExtra(MOVIE_SER_KEY, mIntent);
        startActivity(intentMovieDetailActivity);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if(key.equals(getString(R.string.pref_movie_key))){
            loadCriteriaFromPref(sharedPreferences);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mListState != null) {
            mLayoutManager.onRestoreInstanceState(mListState);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        PreferenceManager.getDefaultSharedPreferences(this).unregisterOnSharedPreferenceChangeListener(this);
    }

    @NonNull
    @Override
    public Loader<MovieDetails[]> onCreateLoader(int id, @Nullable final Bundle args) {
        return new AsyncTaskLoader<MovieDetails[]>(this){
            MovieDetails[] details;

            @Override
            protected void onStartLoading() {
                pb_load.setVisibility(View.VISIBLE);
                error_msg.setVisibility(View.INVISIBLE);
                if(details != null){
                    deliverResult(details);
                }
                else{
                    forceLoad();
                }
            }

            @Nullable
            @Override
            public MovieDetails[] loadInBackground() {
                String sortCriteria = (args != null)?(args.getString(MOVIE_BUNDLE_KEY)):getString(R.string.pref_criteria_popular_value);
                if(sortCriteria.equals(getString(R.string.pref_criteria_toprated_value)) || sortCriteria.equals(getString(R.string.pref_criteria_popular_value))){
                    try {
                        URL url = NetworkUtils.buildUrl(sortCriteria, 1);
                        String jsonMovieResponse = NetworkUtils.getResponseHttpUrl(url);
                        return ParseMovieJsonData.parseJson(jsonMovieResponse);
                    } catch (JSONException e) {
                        e.printStackTrace();
                        return null;
                    }
                    catch (IOException e) {
                        e.printStackTrace();
                        return null;
                    }
                }
                else if(sortCriteria.equals(getString(R.string.pref_criteria_favourite_value))){
                    Log.d("FFFF","favourite movies local query");
                    Cursor cursor = getContentResolver().query(MovieDbContract.MovieEntry.CONTENT_URI,
                            null,
                            null,
                            null,
                            null);
                    if (cursor!=null){
                        ArrayList<MovieDetails> res = getMoviesFromCursor(cursor);
                        if(res == null){
                            return null;
                        }
                        MovieDetails[] resDetails = res.toArray(new MovieDetails[res.size()]);
                        cursor.close();
                        error_msg.setVisibility(View.INVISIBLE);
                        movieDetails.setVisibility(View.VISIBLE);
                        return resDetails;
                    }
                }
                return null;
            }

            /*@Override
            public void deliverResult(MovieDetails[] data) {
                details = data;
                super.deliverResult(data);
            }*/
        };
    }

    @Override
    public void onLoadFinished(@NonNull Loader<MovieDetails[]> loader, MovieDetails[] data) {
        if (data != null){
            pb_load.setVisibility(View.INVISIBLE);
            movieDetails.setVisibility(View.VISIBLE);
            movieAdapter.setMovieData(data);
            Log.d("LLL", Arrays.toString(data));
        }
        else{
            error_msg.setVisibility(View.VISIBLE);
            movieDetails.setVisibility(View.INVISIBLE);
            pb_load.setVisibility(View.INVISIBLE);
        }
        if (mListState != null) {
            mLayoutManager.onRestoreInstanceState(mListState);
        }
    }

    @Override
    public void onLoaderReset(@NonNull Loader<MovieDetails[]> loader) {

    }
}
