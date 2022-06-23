package com.chaitupenju.popularmovies2;

import static com.chaitupenju.popularmovies2.PopularMoviesActivity.MOVIE_SER_KEY;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.AsyncTaskLoader;
import androidx.loader.content.Loader;

import com.chaitupenju.popularmovies2.databaseutils.MovieDbContract;
import com.chaitupenju.popularmovies2.databinding.ActivityMovieDetailBinding;
import com.chaitupenju.popularmovies2.datautils.MovieDetails;
import com.chaitupenju.popularmovies2.datautils.NetworkUtils;
import com.chaitupenju.popularmovies2.datautils.ParseMovieJsonData;
import com.chaitupenju.popularmovies2.reviews.Review;
import com.chaitupenju.popularmovies2.reviews.ReviewsAdapter;
import com.chaitupenju.popularmovies2.trailers.Trailer;
import com.chaitupenju.popularmovies2.trailers.TrailersAdapter;

import org.json.JSONException;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;


public class MovieDetailActivity extends AppCompatActivity
            implements LoaderManager.LoaderCallbacks<Object> {


    ArrayList<Trailer> mTrailer;
    ArrayList<Review> mReview;
    Intent receiveMovieData;
    MovieDetails mDetails;
    TrailersAdapter trailersAdapter;
    ReviewsAdapter reviewsAdapter;
    final Bitmap[] posterBitmap = new Bitmap[1];
    private static final int LOADER_ID = 400;
    Bundle bFav;
    ActivityMovieDetailBinding mBinding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_movie_detail);
        bFav = new Bundle();

        //Click the button to add movie to favourites
        mBinding.movieFavourite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Context context = getApplicationContext();
                if(mDetails.isFauvorite(context)){
                    mDetails.removeFavourites(getApplicationContext());
                    mBinding.movieFavourite.setText("Mark as Favourite");
                    mBinding.movieFavourite.setTextColor(Color.parseColor("#0000FF"));
                }
                else{
                    mDetails.saveFavourites(context);
                    mBinding.movieFavourite.setText("Marked as Favourite");
                    mBinding.movieFavourite.setTextColor(Color.parseColor("#000000"));
                }
            }
        });

        trailersAdapter = new TrailersAdapter(this);
        reviewsAdapter = new ReviewsAdapter(this);
        mBinding.trailerList.setAdapter(trailersAdapter);
        mBinding.reviewList.setAdapter(reviewsAdapter);
        receiveMovieData = getIntent();
        mDetails = (MovieDetails) receiveMovieData.getSerializableExtra(MOVIE_SER_KEY);
        mBinding.tvMovieTitle.setText(mDetails.getTitle());
        String imgURL = MovieAdapter.BASE_IMAGE_URL1 + mDetails.getImage_url() + MovieAdapter.BASE_IMAGE_URL2;
        Log.d("CCC","image url is:"+ imgURL);
        //Picasso.with(this).load(imgURL).into(mImage);
        mBinding.trailerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Uri uri = trailersAdapter.getTrailerUri(position);
                if (uri != null) {
                    Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                    startActivity(intent);
                }
            }
        });
        mBinding.movieSynopsys.setText(mDetails.getSynopsys());
        mBinding.movieUserRating.setText(mDetails.getRating() + " / 10");
        mBinding.movieReleaseDate.setText(mDetails.getRelease_date().substring(0, 4));
        LoaderManager manager = getSupportLoaderManager();
        if(mDetails.isFauvorite(this)){
            mBinding.movieFavourite.setText("Marked as Favourite");
            mBinding.movieFavourite.setTextColor(Color.parseColor("#0000FF"));
            bFav.putBoolean("local", true);
        }
        else{
            mBinding.movieFavourite.setText("Mark as Favourite");
            //Picasso.with(this).load(imgURL).into(mImage);
//            Picasso.with(this).load(imgURL).into(new Target() {
//                @Override
//                public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
//                    posterBitmap[0] = bitmap;
//                    mDetails.setPoster(posterBitmap[0]);
//                    mBinding.movieThumbnail.setImageBitmap(posterBitmap[0]);
//                }
//
//                @Override
//                public void onBitmapFailed(Drawable errorDrawable) {
//
//                }
//
//                @Override
//                public void onPrepareLoad(Drawable placeHolderDrawable) {
//
//                }
//            });
            mBinding.movieFavourite.setTextColor(Color.parseColor("#000000"));
            bFav.putBoolean("local",false);
        }
        Log.d("CCC","oncreate");
        manager.restartLoader(LOADER_ID, bFav, this);

    }

    @Override
    public Loader<Object> onCreateLoader(final int id, final Bundle args) {

        return new AsyncTaskLoader<Object>(this) {
            @Override
            protected void onStartLoading() {
                forceLoad();
                Log.d("CCC","onstartloading");
            }

            @Override
            public Void loadInBackground() {
                Log.d("CCC", "load in background");
                boolean favOrNot = args.getBoolean("local");
                if(!favOrNot){
                    long id = Long.parseLong(mDetails.getId());
                    URL requestTrailersUrl = NetworkUtils.buildTrailersUrlFromId(id);
                    URL requestReviewsUrl = NetworkUtils.buildReviewsUrlFromId(id);
                    try {
                        String JSONResponseTrailers = NetworkUtils.getResponseHttpUrl(requestTrailersUrl);
                        mTrailer = ParseMovieJsonData.parseTrailerDetails(JSONResponseTrailers);
                        String JSONResponseReviews = NetworkUtils.getResponseHttpUrl(requestReviewsUrl);
                        mReview = ParseMovieJsonData.parseReviewDetails(JSONResponseReviews);

                    } catch (IOException | JSONException e) {
                        e.printStackTrace();
                    }
                }
                else{
                    Log.d("CCC", "Starting local query");
                    Cursor cursor = getContentResolver()
                            .query(MovieDbContract.MovieEntry.CONTENT_URI,
                                    new String[]{MovieDbContract.MovieEntry.MOVIE_TRAILERS, MovieDbContract.MovieEntry.MOVIE_REVIEWS, MovieDbContract.MovieEntry.MOVIE_POSTER},
                                    MovieDbContract.MovieEntry.MOVIE_ID + "=?",
                                    new String[]{mDetails.getId()}, null);
                    Log.d("CCC","ID is "+mDetails.getId());
                    if (cursor != null && cursor.moveToFirst()) {
                        Log.d("CCC","cursor is not null");
                        mTrailer = Trailer.stringToArray(cursor.getString(cursor.getColumnIndex(MovieDbContract.MovieEntry.MOVIE_TRAILERS)));
                        mReview = Review.stringToArray(cursor.getString(cursor.getColumnIndex(MovieDbContract.MovieEntry.MOVIE_REVIEWS)));
                        mDetails.setPosterFromCursor(cursor);
                        cursor.close();
                    }else{
                        Log.d("CCC","cursor is null :(");
                    }
                }
                return null;
            }
        };
    }

    @Override
    public void onLoadFinished(@NonNull Loader<Object> loader, Object data) {

    }

//    @Override
//    public void onLoadFinished(@NonNull Loader loader, Object data) {
//        mDetails.setTrailers(mTrailer);
//        mDetails.setReviews(mReview);
//        mBinding.movieThumbnail.setImageBitmap(mDetails.getPoster());
//        if (mTrailer != null) {
//            trailersAdapter.setTrailers(mTrailer);
//            setListViewHeightBasedOnChildren(mBinding.trailerList, "trailerlist");
//        }
//        if (mReview != null) {
//            reviewsAdapter.setReviews(mReview);
//            setListViewHeightBasedOnChildren(mBinding.reviewList, "reviewlist");
//        }
//        mBinding.scrollMovieView.smoothScrollTo(0,0);
//
//    }

    @Override
    public void onLoaderReset(@NonNull Loader loader) {

    }

    public void setListViewHeightBasedOnChildren(ListView listView, String listType) {
        if (listType.equals("trailerlist")) {
            TrailersAdapter listAdapter = (TrailersAdapter) listView.getAdapter();
            setHeightOfTrailerAdapter(listAdapter, listView);
        } else if (listType.equals("reviewlist")) {
            ReviewsAdapter listAdapter = (ReviewsAdapter) listView.getAdapter();
            setHeightOfReviewAdapter(listAdapter, listView);
        }

    }

    public void setHeightOfTrailerAdapter(TrailersAdapter listAdapter, ListView listView) {
        if (listAdapter == null) {
            return;
        }

        int elements = listAdapter.getCount();

        if (elements > 0) {
            View listItem = listAdapter.getView(0, null, listView);
            listItem.measure(0, 0);
            int totalHeight = listItem.getMeasuredHeight() * (elements + 1);

            ViewGroup.LayoutParams params = listView.getLayoutParams();

            params.height = totalHeight
                    + (listView.getDividerHeight() * (listAdapter.getCount() - 1));

            listView.setLayoutParams(params);
        }
    }

    public void setHeightOfReviewAdapter(ReviewsAdapter listAdapter, ListView listView) {
        if (listAdapter == null) {
            return;
        }

        int elements = listAdapter.getCount();

        if (elements > 0) {
            View listItem = listAdapter.getView(0, null, listView);
            listItem.measure(0, 0);
            int totalHeight = listItem.getMeasuredHeight() * (elements + 3);

            ViewGroup.LayoutParams params = listView.getLayoutParams();

            params.height = totalHeight
                    + (listView.getDividerHeight() * (listAdapter.getCount() - 1));

            listView.setLayoutParams(params);
        }
        else{
            mBinding.tvMovieReview.setText("No Reviews");
        }
    }
}