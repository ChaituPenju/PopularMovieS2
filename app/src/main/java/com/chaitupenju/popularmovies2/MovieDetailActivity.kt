package com.chaitupenju.popularmovies2

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.loader.app.LoaderManager
import androidx.loader.content.AsyncTaskLoader
import androidx.loader.content.Loader
import com.chaitupenju.MovieAdapter
import com.chaitupenju.popularmovies2.databaseutils.MovieDbContract
import com.chaitupenju.popularmovies2.databinding.ActivityMovieDetailBinding
import com.chaitupenju.popularmovies2.datautils.MovieDetails
import com.chaitupenju.popularmovies2.datautils.NetworkUtils
import com.chaitupenju.popularmovies2.datautils.ParseMovieJsonData
import com.chaitupenju.popularmovies2.models.Review
import com.chaitupenju.popularmovies2.adapters.ReviewsAdapter
import com.chaitupenju.popularmovies2.models.Trailer
import com.chaitupenju.popularmovies2.adapters.TrailersAdapter
import com.squareup.picasso.Picasso
import org.json.JSONException
import java.io.IOException

class MovieDetailActivity: AppCompatActivity(),
    LoaderManager.LoaderCallbacks<Any> {

    companion object {
        private const val LOADER_ID = 400
    }

    var mTrailer: ArrayList<Trailer> = arrayListOf()
    var mReview: ArrayList<Review> = arrayListOf()

    private lateinit var receiveMovieData: Intent

    var mDetails: MovieDetails? = null

    lateinit var trailersAdapter: TrailersAdapter
    lateinit var reviewsAdapter: ReviewsAdapter

    val posterBitmap = arrayOfNulls<Bitmap>(1)

    var bFav: Bundle? = null
    private lateinit var mBinding: ActivityMovieDetailBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_movie_detail)
        bFav = Bundle()

        //Click the button to add movie to favourites
        mBinding.movieFavourite.setOnClickListener {
            val context = applicationContext
            if (mDetails!!.isFauvorite(context)) {
                mDetails!!.removeFavourites(applicationContext)
                mBinding.movieFavourite.text = "Mark as Favourite"
                mBinding.movieFavourite.setTextColor(Color.parseColor("#0000FF"))
            } else {
                mDetails!!.saveFavourites(context)
                mBinding.movieFavourite.text = "Marked as Favourite"
                mBinding.movieFavourite.setTextColor(Color.parseColor("#000000"))
            }
        }

        trailersAdapter = TrailersAdapter(this, arrayListOf())
            .also { mBinding.trailerList.adapter = it }
        reviewsAdapter = ReviewsAdapter(this, arrayListOf())
            .also { mBinding.reviewList.adapter = it }

        receiveMovieData = intent
        mDetails = receiveMovieData.getSerializableExtra(PopularMoviesActivity.MOVIE_SER_KEY) as MovieDetails?
        mBinding.tvMovieTitle.text = mDetails!!.title
        val imgURL =
            MovieAdapter.BASE_IMAGE_URL1 + mDetails!!.image_url + MovieAdapter.BASE_IMAGE_URL2
        Log.d("CCC", "image url is:$imgURL")

        Picasso.Builder(this).build()
            .load(imgURL).into(mBinding.movieThumbnail)

        mBinding.trailerList.setOnItemClickListener { _, _, position, _ ->
            val uri = trailersAdapter.getTrailerUri(position)
            if (uri != null) {
                val intent = Intent(Intent.ACTION_VIEW, uri)
                startActivity(intent)
            }
        }

        mBinding.movieSynopsys.text = mDetails!!.synopsys
        mBinding.movieUserRating.text = mDetails!!.rating + " / 10"
        mBinding.movieReleaseDate.text = mDetails!!.release_date.substring(0, 4)
        val manager = supportLoaderManager
        if (mDetails!!.isFauvorite(this)) {
            mBinding.movieFavourite.text = "Marked as Favourite"
            mBinding.movieFavourite.setTextColor(Color.parseColor("#0000FF"))
            bFav!!.putBoolean("local", true)
        } else {
            mBinding.movieFavourite.text = "Mark as Favourite"
            //Picasso.with(this).load(imgURL).into(mImage);
            //todo paste here
            mBinding.movieFavourite.setTextColor(Color.parseColor("#000000"))
            bFav!!.putBoolean("local", false)
        }
        Log.d("CCC", "oncreate")
        manager.restartLoader(LOADER_ID, bFav, this)
    }



    override fun onCreateLoader(id: Int, args: Bundle?): AsyncTaskLoader<Any?> {
        return object : AsyncTaskLoader<Any?>(this) {
            override fun onStartLoading() {
                forceLoad()
                Log.d("CCC", "onstartloading")
            }

            override fun loadInBackground(): Void? {
                Log.d("CCC", "load in background")
                val favOrNot = args!!.getBoolean("local")
                if (!favOrNot) {
                    val id = mDetails!!.id.toLong()
                    val requestTrailersUrl = NetworkUtils.buildTrailersUrlFromId(id)
                    val requestReviewsUrl = NetworkUtils.buildReviewsUrlFromId(id)
                    try {
                        val JSONResponseTrailers =
                            NetworkUtils.getResponseHttpUrl(requestTrailersUrl)
                        mTrailer = ParseMovieJsonData.parseTrailerDetails(JSONResponseTrailers)
                        val JSONResponseReviews = NetworkUtils.getResponseHttpUrl(requestReviewsUrl)
                        mReview = ParseMovieJsonData.parseReviewDetails(JSONResponseReviews)
                    } catch (e: IOException) {
                        e.printStackTrace()
                    } catch (e: JSONException) {
                        e.printStackTrace()
                    }
                } else {
                    Log.d("CCC", "Starting local query")
                    val cursor = contentResolver
                        .query(
                            MovieDbContract.MovieEntry.CONTENT_URI,
                            arrayOf(
                                MovieDbContract.MovieEntry.MOVIE_TRAILERS,
                                MovieDbContract.MovieEntry.MOVIE_REVIEWS,
                                MovieDbContract.MovieEntry.MOVIE_POSTER
                            ),
                            MovieDbContract.MovieEntry.MOVIE_ID + "=?",
                            arrayOf(mDetails!!.id),
                            null
                        )
                    Log.d("CCC", "ID is " + mDetails!!.id)
                    if (cursor != null && cursor.moveToFirst()) {
                        Log.d("CCC", "cursor is not null")
                        mTrailer = Trailer.stringToArray(
                            cursor.getString(
                                cursor.getColumnIndex(MovieDbContract.MovieEntry.MOVIE_TRAILERS)
                            )
                        )
                        mReview = Review.stringToArray(
                            cursor.getString(
                                cursor.getColumnIndex(MovieDbContract.MovieEntry.MOVIE_REVIEWS)
                            )
                        )
                        mDetails!!.setPosterFromCursor(cursor)
                        cursor.close()
                    } else {
                        Log.d("CCC", "cursor is null :(")
                    }
                }
                return null
            }
        }
    }

    override fun onLoadFinished(loader: Loader<Any>, data: Any?) {
        mDetails!!.trailers = mTrailer
        mDetails!!.reviews = mReview
        mBinding.movieThumbnail.setImageBitmap(mDetails!!.poster)
        if (mTrailer != null) {
            trailersAdapter!!.setTrailers(mTrailer!!)
            setListViewHeightBasedOnChildren(mBinding.trailerList, "trailerlist")
        }
        if (mReview != null) {
            reviewsAdapter!!.setReviews(mReview!!)
            setListViewHeightBasedOnChildren(mBinding.reviewList, "reviewlist")
        }
        mBinding.scrollMovieView.smoothScrollTo(0, 0)
    }

    override fun onLoaderReset(loader: Loader<Any>) {
        // no impl here
    }


    private fun setListViewHeightBasedOnChildren(listView: ListView, listType: String) {
        if (listType == "trailerlist") {
            val listAdapter = listView.adapter as TrailersAdapter
            setHeightOfTrailerAdapter(listAdapter, listView)
        } else if (listType == "reviewlist") {
            val listAdapter = listView.adapter as ReviewsAdapter
            setHeightOfReviewAdapter(listAdapter, listView)
        }
    }

    private fun setHeightOfTrailerAdapter(listAdapter: TrailersAdapter?, listView: ListView) {
        if (listAdapter == null) {
            return
        }
        val elements = listAdapter.count
        if (elements > 0) {
            val listItem = listAdapter.getView(0, null, listView)
            listItem?.measure(0, 0)
            val totalHeight = listItem?.measuredHeight?.times((elements + 1))
            val params = listView.layoutParams
            if (totalHeight != null) {
                params.height = (totalHeight
                        + listView.dividerHeight * (listAdapter.count - 1))
            }
            listView.layoutParams = params
        }
    }

    private fun setHeightOfReviewAdapter(listAdapter: ReviewsAdapter?, listView: ListView) {
        if (listAdapter == null) {
            return
        }
        val elements = listAdapter.count
        if (elements > 0) {
            val listItem = listAdapter.getView(0, null, listView)
            listItem.measure(0, 0)
            val totalHeight = listItem.measuredHeight * (elements + 3)
            val params = listView.layoutParams
            params.height = (totalHeight
                    + listView.dividerHeight * (listAdapter.count - 1))
            listView.layoutParams = params
        } else {
            mBinding.tvMovieReview.text = "No Reviews"
        }
    }
}