package com.chaitupenju

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.chaitupenju.popularmovies2.R
import com.chaitupenju.popularmovies2.datautils.MovieDetails

class MovieAdapter(
    private val context: Context,
    val mMovieClickHandler: (MovieDetails) -> Unit
) : RecyclerView.Adapter<MovieAdapter.MovieAdapterViewHolder>() {

    var mMovieData: Array<MovieDetails> = arrayOf()

    companion object {
        const val BASE_IMAGE_URL1 = "https://image.tmdb.org/t/p/w500"
        const val BASE_IMAGE_URL2 =
            "?api_key=e62aae5e1c8389286f8dd8a887787ff7&page=1&language=en-US"
    }

    fun setMovieData(movieData: Array<MovieDetails>) {
        mMovieData = movieData
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): MovieAdapter.MovieAdapterViewHolder {
        val view: View =
            LayoutInflater.from(parent.context).inflate(R.layout.movie_list_item, parent, false)
        return MovieAdapterViewHolder(view)
    }

    override fun onBindViewHolder(holder: MovieAdapter.MovieAdapterViewHolder, position: Int) {
        val movieDetail = mMovieData[position]
        holder.onBind(data = movieDetail)
//        Picasso.with(context)
//                .load(BASE_IMAGE_URL1 + movieDetail.getImage_url() + BASE_IMAGE_URL2)
//                .into(holder.mMovieImageView);
    }

    override fun getItemCount(): Int = mMovieData.size

    inner class MovieAdapterViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val mMovieImageView: ImageView

        init {
            mMovieImageView = itemView.findViewById(R.id.movie_img)
        }

        fun onBind(data: MovieDetails) {
            mMovieClickHandler.invoke(data)
        }
    }
}