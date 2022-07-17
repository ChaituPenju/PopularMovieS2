package com.chaitupenju

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.chaitupenju.popularmovies2.databinding.MovieListItemBinding
import com.chaitupenju.popularmovies2.datautils.MovieDetails
import com.chaitupenju.popularmovies2.models.MovieDetail
import com.squareup.picasso.Picasso

class MovieAdapter(
    private val context: Context,
    private val mMovieClickHandler: (MovieDetail) -> Unit
) : RecyclerView.Adapter<MovieAdapter.MovieAdapterViewHolder>() {

    var mMovieData: List<MovieDetail> = listOf()

    companion object {
        const val BASE_IMAGE_URL1 = "https://image.tmdb.org/t/p/w500"
        const val BASE_IMAGE_URL2 =
            "?api_key=e62aae5e1c8389286f8dd8a887787ff7&page=1&language=en-US"
    }

    fun setMovieData(movieData: List<MovieDetail>) {
        mMovieData = movieData
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): MovieAdapter.MovieAdapterViewHolder {
        return MovieAdapterViewHolder(
            MovieListItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }

    override fun onBindViewHolder(holder: MovieAdapter.MovieAdapterViewHolder, position: Int) {
        val movieDetail = mMovieData[position]
        holder.onBind(movieDetail = movieDetail)

        holder.itemView.setOnClickListener { mMovieClickHandler.invoke(movieDetail) }
    }

    override fun getItemCount(): Int = mMovieData.size

    inner class MovieAdapterViewHolder(private val binding: MovieListItemBinding) : RecyclerView.ViewHolder(binding.root) {

        fun onBind(movieDetail: MovieDetail) {
            Picasso.Builder(context)
                .build()
                .load(BASE_IMAGE_URL1 + movieDetail.posterPath + BASE_IMAGE_URL2)
                .into(binding.movieImg)
        }
    }
}