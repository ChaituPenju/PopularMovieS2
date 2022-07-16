package com.chaitupenju.popularmovies2

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import android.database.Cursor
import android.os.Bundle
import android.os.Parcelable
import android.preference.PreferenceManager
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.loader.app.LoaderManager
import androidx.loader.content.AsyncTaskLoader
import androidx.loader.content.Loader
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.chaitupenju.MovieAdapter
import com.chaitupenju.MovieCriteriaActivity
import com.chaitupenju.popularmovies2.databaseutils.MovieDbContract
import com.chaitupenju.popularmovies2.datautils.MovieDetails
import com.chaitupenju.popularmovies2.datautils.NetworkUtils
import com.chaitupenju.popularmovies2.datautils.ParseMovieJsonData
import org.json.JSONException
import java.io.IOException
import java.util.*

class PopularMoviesActivity: AppCompatActivity(),
    OnSharedPreferenceChangeListener, LoaderManager.LoaderCallbacks<Array<MovieDetails>> {

    companion object {
        const val MOVIE_SER_KEY = "movie-serialize"
        const val MOVIE_BUNDLE_KEY = "movie-key"
        private const val MOVIE_LOADER_ID = 0
        private const val SCROLL_POS_KEY = "scroll-position"
    }

    var mListState: Parcelable? = null

    var pb_load: ProgressBar? = null
    lateinit var movieDetails: RecyclerView
    var movieAdapter: MovieAdapter? = null
    var error_msg: TextView? = null
    var mLayoutManager: RecyclerView.LayoutManager? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_popular_movies)

        pb_load = findViewById(R.id.progress_bar)
        error_msg = findViewById(R.id.tv_error_msg)
        movieDetails = findViewById(R.id.movie_details)

        movieAdapter = MovieAdapter(this) {
            val context: Context = this
            val destinationClass: Class<*> = MovieDetailActivity::class.java
            val intentMovieDetailActivity = Intent(context, destinationClass)
            intentMovieDetailActivity.putExtra(MOVIE_SER_KEY, it)
            startActivity(intentMovieDetailActivity)
        }

        movieDetails.apply {
            itemAnimator = DefaultItemAnimator()
            adapter = movieAdapter
        }

        loadCriteriaFromPref(PreferenceManager.getDefaultSharedPreferences(this))
        PreferenceManager.getDefaultSharedPreferences(this)
            .registerOnSharedPreferenceChangeListener(this)
    }

    override fun onSaveInstanceState(state: Bundle) {
        super.onSaveInstanceState(state)

        mListState = mLayoutManager!!.onSaveInstanceState()
        state.putParcelable(SCROLL_POS_KEY, mListState)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)

        mListState = savedInstanceState.getParcelable(SCROLL_POS_KEY)
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

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String) {
        if (key == getString(R.string.pref_movie_key)) {
            loadCriteriaFromPref(sharedPreferences!!)
        }
    }

    override fun onResume() {
        super.onResume()
        if (mListState != null) {
            mLayoutManager!!.onRestoreInstanceState(mListState)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        PreferenceManager.getDefaultSharedPreferences(this)
            .unregisterOnSharedPreferenceChangeListener(this)
    }

    override fun onCreateLoader(id: Int, args: Bundle?): AsyncTaskLoader<Array<MovieDetails>?> {
        return object : AsyncTaskLoader<Array<MovieDetails>?>(this) {

            var details: Array<MovieDetails>? = null

            override fun onStartLoading() {
                pb_load!!.visibility = View.VISIBLE
                error_msg!!.visibility = View.INVISIBLE
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
                ) {
                    return try {
                        val url = NetworkUtils.buildUrl(sortCriteria, 1)
                        val jsonMovieResponse = NetworkUtils.getResponseHttpUrl(url)
                        ParseMovieJsonData.parseJson(jsonMovieResponse)
                    } catch (e: JSONException) {
                        e.printStackTrace()
                        null
                    } catch (e: IOException) {
                        e.printStackTrace()
                        null
                    }
                } else if (sortCriteria == getString(R.string.pref_criteria_favourite_value)) {
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
                        error_msg!!.visibility = View.INVISIBLE
                        movieDetails.visibility = View.VISIBLE
                        return resDetails
                    }
                }
                return null
            }
        }
    }

    override fun onLoadFinished(loader: Loader<Array<MovieDetails>>, data: Array<MovieDetails>?) {
        if (data != null) {
            pb_load!!.visibility = View.INVISIBLE
            movieDetails.visibility = View.VISIBLE
            movieAdapter!!.setMovieData(data)
            Log.d("LLL", Arrays.toString(data))
        } else {
            error_msg!!.visibility = View.VISIBLE
            movieDetails.visibility = View.INVISIBLE
            pb_load!!.visibility = View.INVISIBLE
        }
        if (mListState != null) {
            mLayoutManager!!.onRestoreInstanceState(mListState)
        }
    }

    override fun onLoaderReset(loader: Loader<Array<MovieDetails>>) {
        // nothing to impl here
    }


    private fun loadCriteriaFromPref(sp: SharedPreferences) {
        val bundle = Bundle()

        val callbacks: LoaderManager.LoaderCallbacks<Array<MovieDetails>> =
            this@PopularMoviesActivity
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