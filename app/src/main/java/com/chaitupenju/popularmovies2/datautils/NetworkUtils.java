package com.chaitupenju.popularmovies2.datautils;


import android.net.Uri;
import android.util.Log;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Scanner;

public class NetworkUtils {

    private static final String TAG = NetworkUtils.class.getSimpleName();
    private static final String BASE_URL = "https://api.themoviedb.org/3/movie/";
    private static final String API_KEY_PARAM = "api_key";
    private static final String API_VALUE_PARAM = "e62aae5e1c8389286f8dd8a887787ff7";
    private static final String LANGUAGE_PARAM = "language";
    private static final String LANG_VALUE_PARAM = "en-US";
    private static final String PAGE_PARAM = "page";
    private static final String API_TRAILERS_PATH = "videos";
    private static final String API_REVIEWS_PATH = "reviews";

    public static URL buildUrl(String sortOrder, int pageNumber){
        Uri builtUri = Uri.parse(BASE_URL + sortOrder).buildUpon()
                .appendQueryParameter(API_KEY_PARAM, API_VALUE_PARAM)
                .appendQueryParameter(PAGE_PARAM, String.valueOf(pageNumber))
                .appendQueryParameter(LANGUAGE_PARAM, LANG_VALUE_PARAM)
                .build();
        URL url = null;
        try{
            url = new URL(builtUri.toString());
        }
        catch(MalformedURLException mae){
            mae.printStackTrace();
        }
        Log.d(TAG, "Build URL is:"+url);
        return url;
    }

    public static URL buildTrailersUrlFromId(long id){
        Uri builtUri = Uri.parse(BASE_URL).buildUpon()
                .appendPath(Long.toString(id))
                .appendPath(API_TRAILERS_PATH)
                .appendQueryParameter(API_KEY_PARAM, API_VALUE_PARAM)
                .build();

        URL url = null;
        try {
            url = new URL(builtUri.toString());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        return url;
    }
    public static URL buildReviewsUrlFromId(long id){
        Uri builtUri = Uri.parse(BASE_URL).buildUpon()
                .appendPath(Long.toString(id))
                .appendPath(API_REVIEWS_PATH)
                .appendQueryParameter(API_KEY_PARAM, API_VALUE_PARAM)
                .build();
        URL url = null;
        try {
            url = new URL(builtUri.toString());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        return url;
    }


    public static String getResponseHttpUrl(URL url) throws IOException {
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        try{
            Scanner sc = new Scanner(conn.getInputStream());
            sc.useDelimiter("\\A");
            boolean hasInput = sc.hasNext();
            if(hasInput){
                return sc.next();
            }
            else{
                return null;
            }
        }
        finally{
            conn.disconnect();
        }
    }
}
