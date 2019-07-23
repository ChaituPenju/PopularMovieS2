package com.chaitupenju.popularmovies2;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.chaitupenju.popularmovies2.datautils.MovieDetails;
import com.squareup.picasso.Picasso;


public class MovieAdapter extends RecyclerView.Adapter<MovieAdapter.MovieAdapterViewHolder> {
    Context context;
    private MovieDetails[] mMovieData;
    public static final String BASE_IMAGE_URL1 = "https://image.tmdb.org/t/p/w500";
    public static final String BASE_IMAGE_URL2 = "?api_key=e62aae5e1c8389286f8dd8a887787ff7&page=1&language=en-US";

    private final MovieAdapterClickHandler mMovieClickHandler;

    public interface MovieAdapterClickHandler{
        void onClick(MovieDetails mIntent);
    }

    public MovieAdapter(Context context, MovieAdapterClickHandler mMovieClickHandler){
        this.context = context;
        this.mMovieClickHandler = mMovieClickHandler;
    }
    @NonNull
    @Override
    public MovieAdapterViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.movie_list_item, parent, false);
        return new MovieAdapterViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MovieAdapterViewHolder holder, int position) {
        MovieDetails movieDetail = mMovieData[position];
        Picasso.with(context)
                .load(BASE_IMAGE_URL1 + movieDetail.getImage_url() + BASE_IMAGE_URL2)
                .into(holder.mMovieImageView);
    }

    @Override
    public int getItemCount() {
        if(null == mMovieData) return 0;
        return mMovieData.length;
    }

    public class MovieAdapterViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        public final ImageView mMovieImageView;
        public MovieAdapterViewHolder(View itemView) {
            super(itemView);
            mMovieImageView = itemView.findViewById(R.id.movie_img);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            int adapterPos = getAdapterPosition();
            MovieDetails movieData = mMovieData[adapterPos];
            mMovieClickHandler.onClick(movieData);
        }
    }
    public void setMovieData(MovieDetails[] movieData) {
        mMovieData = movieData;
        notifyDataSetChanged();
    }
}
