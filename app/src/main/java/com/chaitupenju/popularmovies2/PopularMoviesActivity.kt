package com.chaitupenju.popularmovies2

import android.annotation.SuppressLint
import android.content.Intent
import android.content.SharedPreferences
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import android.database.Cursor
import android.os.Bundle
import android.preference.PreferenceManager
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.loader.app.LoaderManager
import androidx.loader.content.AsyncTaskLoader
import androidx.loader.content.Loader
import androidx.recyclerview.widget.DefaultItemAnimator
import com.chaitupenju.MovieAdapter
import com.chaitupenju.MovieCriteriaActivity
import com.chaitupenju.popularmovies2.databaseutils.MovieDbContract
import com.chaitupenju.popularmovies2.databinding.ActivityPopularMoviesBinding
import com.chaitupenju.popularmovies2.datautils.MovieDetails
import com.chaitupenju.popularmovies2.datautils.NetworkUtils
import com.chaitupenju.popularmovies2.datautils.ParseMovieJsonData
import com.chaitupenju.popularmovies2.models.MovieDetail
import com.chaitupenju.popularmovies2.network.PopularMoviesNetwork
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONException
import java.io.IOException
import java.util.*

class PopularMoviesActivity: AppCompatActivity() {

    companion object {
        const val MOVIE_SER_KEY = "movie-serialize"
        const val MOVIE_BUNDLE_KEY = "movie-key"
        private const val MOVIE_LOADER_ID = 0
    }

    private lateinit var popularMoviesBinding: ActivityPopularMoviesBinding
    private lateinit var movieAdapter: MovieAdapter

    private val sharedPreferenceChangeListener get() = OnSharedPreferenceChangeListener { sharedPreferences, key ->
        if (key == getString(R.string.pref_movie_key)) {
            loadCriteriaFromPref(sharedPreferences!!)
        }
    }

    private val movieLoaderManager = object : LoaderManager.LoaderCallbacks<Array<MovieDetails>> {
        override fun onCreateLoader(id: Int, args: Bundle?): AsyncTaskLoader<Array<MovieDetails>?> {
            return object : AsyncTaskLoader<Array<MovieDetails>?>(this@PopularMoviesActivity) {

                var details: Array<MovieDetails>? = null

                override fun onStartLoading() {
                    popularMoviesBinding.progressBar.visibility = View.VISIBLE
                    popularMoviesBinding.tvErrorMsg.visibility = View.INVISIBLE
                    if (details != null) {
                        deliverResult(details)
                    } else {
                        forceLoad()
                    }
                }

                override fun loadInBackground(): Array<MovieDetails>? {
                    val sortCriteria =
                        if (args != null) args.getString(MOVIE_BUNDLE_KEY) else getString(R.string.pref_criteria_popular_value)
                    if (sortCriteria == getString(R.string.pref_criteria_toprated_value) || sortCriteria == getString(
                            R.string.pref_criteria_popular_value
                        )
                    ) {} else if (sortCriteria == getString(R.string.pref_criteria_favourite_value)) {
                        Log.d("FFFF", "favourite movies local query")
                        val cursor = contentResolver.query(
                            MovieDbContract.MovieEntry.CONTENT_URI,
                            null,
                            null,
                            null,
                            null
                        )
                        if (cursor != null) {
                            val res: ArrayList<MovieDetails> =
                                getMoviesFromCursor(cursor) ?: return null
                            val resDetails = res.toTypedArray()
                            cursor.close()
                            popularMoviesBinding.tvErrorMsg.visibility = View.INVISIBLE
                            popularMoviesBinding.movieDetails.visibility = View.VISIBLE
                            return resDetails
                        }
                    }
                    return null
                }
            }
        }

        override fun onLoadFinished(
            loader: Loader<Array<MovieDetails>>,
            data: Array<MovieDetails>?
        ) {
            if (data != null) {
                popularMoviesBinding.progressBar.visibility = View.INVISIBLE
                popularMoviesBinding.movieDetails.visibility = View.VISIBLE
//            movieAdapter.setMovieData(data)
                Log.d("LLL", Arrays.toString(data))
            } else {
                popularMoviesBinding.tvErrorMsg.visibility = View.VISIBLE
                popularMoviesBinding.movieDetails.visibility = View.INVISIBLE
                popularMoviesBinding.progressBar.visibility = View.INVISIBLE
            }
        }

        override fun onLoaderReset(loader: Loader<Array<MovieDetails>>) {
            // no impl here
        }

    }

    private val movieDbService get() = PopularMoviesNetwork.movieDbService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        popularMoviesBinding = DataBindingUtil.setContentView(this@PopularMoviesActivity, R.layout.activity_popular_movies)

        movieAdapter = MovieAdapter(this) {
            val intentMovieDetailActivity = Intent(this@PopularMoviesActivity, MovieDetailActivity::class.java)
//            intentMovieDetailActivity.putExtra(MOVIE_SER_KEY, it)

            startActivity(intentMovieDetailActivity)
        }

        popularMoviesBinding.movieDetails.apply {
            itemAnimator = DefaultItemAnimator()
            adapter = movieAdapter
        }

        getMoviesFromNetwork()

        loadCriteriaFromPref(PreferenceManager.getDefaultSharedPreferences(this))
        PreferenceManager.getDefaultSharedPreferences(this)
            .registerOnSharedPreferenceChangeListener(sharedPreferenceChangeListener)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.sort_order_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val itemId = item.itemId

        if (itemId == R.id.movie_criteria) {
            val intent = Intent(this, MovieCriteriaActivity::class.java)
            startActivity(intent)
            return true
        }

        return super.onOptionsItemSelected(item)
    }

    override fun onDestroy() {
        super.onDestroy()

        PreferenceManager.getDefaultSharedPreferences(this)
            .unregisterOnSharedPreferenceChangeListener(sharedPreferenceChangeListener)
    }


    private fun loadCriteriaFromPref(sp: SharedPreferences) {
        val bundle = Bundle()

        val callbacks: LoaderManager.LoaderCallbacks<Array<MovieDetails>> = movieLoaderManager

        bundle.putString(
            MOVIE_BUNDLE_KEY,
            sp.getString(
                getString(R.string.pref_movie_key),
                getString(R.string.pref_criteria_popular_value)
            )
        )
        val manager = supportLoaderManager
        manager.restartLoader(MOVIE_LOADER_ID, bundle, callbacks)
            ?: manager.initLoader(MOVIE_LOADER_ID, bundle, callbacks)
    }

    private fun getMoviesFromNetwork() {
        val queryMap = PopularMoviesNetwork.moviesQueryMap
            .apply {
                put(PopularMoviesNetwork.PAGE, "1")
            }

        CoroutineScope(Dispatchers.IO).launch {
            movieDbService.moviesList(queryMap = queryMap).run moviesList@{
                if (isSuccessful) {
                    body()?.let {
                        withContext(Dispatchers.Main) {
                            movieAdapter.setMovieData(movieData = it.movieResults)
                        }
                    }
                } else Unit // todo impl error scenario later
            }
        }
    }

    @SuppressLint("Range")
    private fun getMoviesFromCursor(cursor: Cursor): ArrayList<MovieDetails>? {
        val mCursorDetails = ArrayList<MovieDetails>()
        if (cursor.count == 0) {
            Log.d("CUR", "cursor is null")
            return null
        }
        if (cursor.moveToFirst()) {
            do {
                val movie = MovieDetails(
                    cursor.getString(cursor.getColumnIndex(MovieDbContract.MovieEntry.MOVIE_ID)),
                    cursor.getString(cursor.getColumnIndex(MovieDbContract.MovieEntry.MOVIE_TITLE)),
                    cursor.getString(cursor.getColumnIndex(MovieDbContract.MovieEntry.MOVIE_POSTER_PATH)),
                    cursor.getString(cursor.getColumnIndex(MovieDbContract.MovieEntry.MOVIE_OVERVIEW)),
                    cursor.getString(cursor.getColumnIndex(MovieDbContract.MovieEntry.MOVIE_AVG)),
                    cursor.getString(cursor.getColumnIndex(MovieDbContract.MovieEntry.MOVIE_RELEASE_DATE))
                )
                movie.setPosterFromCursor(cursor)
                mCursorDetails.add(movie)
            } while (cursor.moveToNext())
        }
        return mCursorDetails
    }
}