package com.chaitupenju.popularmovies2.datautils;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.widget.Toast;

import com.chaitupenju.popularmovies2.databaseutils.MovieDbContract;
import com.chaitupenju.popularmovies2.models.Review;
import com.chaitupenju.popularmovies2.models.Trailer;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Serializable;
import java.util.ArrayList;

public class MovieDetails implements Serializable{

    private String title;
    private String id;
    private String image_url;
    private String synopsys;
    private String rating;
    private String release_date;
    private ArrayList<Trailer> trailers;
    private ArrayList<Review> reviews;
    private transient Bitmap poster;
    

    public MovieDetails(String id, String title, String image_url, String synopsys, String rating, String release_date) {
        this.id = id;
        this.title = title;
        this.image_url = image_url;
        this.synopsys = synopsys;
        this.rating = rating;
        this.release_date = release_date;
        this.trailers = new ArrayList<>();
        this.reviews = new ArrayList<>();
    }

    public boolean saveFavourites(Context context){
        ContentValues contentValues = new ContentValues();
        contentValues.put(MovieDbContract.MovieEntry.MOVIE_ID, this.id);
        contentValues.put(MovieDbContract.MovieEntry.MOVIE_TITLE, this.title);
        contentValues.put(MovieDbContract.MovieEntry.MOVIE_OVERVIEW, this.synopsys);
        contentValues.put(MovieDbContract.MovieEntry.MOVIE_POSTER_PATH, this.image_url);
        contentValues.put(MovieDbContract.MovieEntry.MOVIE_AVG, this.rating);
        contentValues.put(MovieDbContract.MovieEntry.MOVIE_RELEASE_DATE, this.release_date);
        contentValues.put(MovieDbContract.MovieEntry.MOVIE_TRAILERS,Trailer.Companion.arrayToString(trailers));
        contentValues.put(MovieDbContract.MovieEntry.MOVIE_REVIEWS,Review.Companion.arrayToString(reviews));

       /*ContentValues contentValues = new ContentValues();
        contentValues.put(MovieDbContract.MovieEntry.MOVIE_ID, this.id);
        contentValues.put(MovieDbContract.MovieEntry.MOVIE_TITLE, this.title);
        contentValues.put(MovieDbContract.MovieEntry.MOVIE_OVERVIEW, this.synopsys);
        contentValues.put(MovieDbContract.MovieEntry.MOVIE_POSTER_PATH, this.image_url);
        contentValues.put(MovieDbContract.MovieEntry.MOVIE_AVG, this.rating);
        contentValues.put(MovieDbContract.MovieEntry.MOVIE_RELEASE_DATE, this.release_date);
        contentValues.put(MovieDbContract.MovieEntry.MOVIE_TRAILERS,Trailer.arrayToString(trailers));
        contentValues.put(MovieDbContract.MovieEntry.MOVIE_REVIEWS,Review.arrayToString(reviews));
*/
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        this.poster.compress(Bitmap.CompressFormat.JPEG,100,bos);
        byte[] bytes = bos.toByteArray();


        contentValues.put(MovieDbContract.MovieEntry.MOVIE_POSTER,bytes);

        if (context.getContentResolver().insert(MovieDbContract.MovieEntry.CONTENT_URI,contentValues)!=null){
            Toast.makeText(context, "added to favourites", Toast.LENGTH_SHORT).show();
            return true;
        }else{
            Toast.makeText(context, "error inserting values", Toast.LENGTH_SHORT).show();
            return false;
        }
    }

    public boolean removeFavourites(Context context){
        long deletedRows = context.getContentResolver().delete(MovieDbContract.MovieEntry.CONTENT_URI,
                MovieDbContract.MovieEntry.MOVIE_ID + "=?",
                new String[]{this.id});
        if (deletedRows>0){
            Toast.makeText(context, "Removed from Favourites", Toast.LENGTH_SHORT).show();
            return true;
        }else {
            Toast.makeText(context, "error", Toast.LENGTH_SHORT).show();
            return false;
        }
    }

    public boolean isFauvorite(Context context){
        Cursor cursor = context.getContentResolver()
                .query(MovieDbContract.MovieEntry.CONTENT_URI,
                        new String[]{MovieDbContract.MovieEntry.MOVIE_ID},
                        MovieDbContract.MovieEntry.MOVIE_ID + "=?",
                        new String[]{this.id},
                        null);
        if (cursor!=null) {
            boolean bookmarked = cursor.getCount() > 0;
            cursor.close();
            return bookmarked;
        }
        return false;
    }

    public String getTitle() {
        return title;
    }


    public String getImage_url() {
        return image_url;
    }


    public String getSynopsys() {
        return synopsys;
    }


    public String getRating() {
        return rating;
    }


    public String getRelease_date() {
        return release_date;
    }

    public String getId(){return id;}

    public ArrayList<Trailer> getTrailers() {
        return trailers;
    }

    public void setTrailers(ArrayList<Trailer> trailers) {
        this.trailers = trailers;
    }

    public ArrayList<Review> getReviews() {
        return reviews;
    }

    public void setReviews(ArrayList<Review> reviews) {
        this.reviews = reviews;
    }

    public Bitmap getPoster() {
        return poster;
    }

    public void setPosterFromCursor(Cursor cursor){
        byte[] bytes = cursor.getBlob(cursor.getColumnIndex(MovieDbContract.MovieEntry.MOVIE_POSTER));
        ByteArrayInputStream posterStream = new ByteArrayInputStream(bytes);
        this.poster = BitmapFactory.decodeStream(posterStream);
    }

    public void setPoster(Bitmap poster) {
        this.poster = poster;
    }
}
