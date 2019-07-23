package com.chaitupenju.popularmovies2.datautils;




import com.chaitupenju.popularmovies2.reviews.Review;
import com.chaitupenju.popularmovies2.trailers.Trailer;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;


public class ParseMovieJsonData {

    private static final String TITLE_PARAM = "title";
    private static final String IMGURL_PARAM = "poster_path";
    private static final String SYNOPSYS_PARAM = "overview";
    private static final String RATING_PARAM = "vote_average";
    private static final String RELEASE_DATE_PARAM = "release_date";
    private static final String ID_PARAM = "id";
    private static final String RESULTS_PARAM = "results";
    private static final String SITE_PARAM = "site";
    private static final String KEY_PARAM = "key";
    private static final String NAME_PARAM = "name";
    private static final String AUTHOR_PARAM = "author";
    private static final String CONTENT_PARAM = "content";


    private static final String RESULT = "results";

    public static MovieDetails[] parseJson(String jsonStr) throws JSONException {
        MovieDetails[] details;
        JSONObject movieObj = new JSONObject(jsonStr);
        JSONArray movieArr = movieObj.getJSONArray(RESULT);
        details = new MovieDetails[movieArr.length()];
        for(int i = 0; i < movieArr.length(); i++){
            JSONObject indvObjs = movieArr.getJSONObject(i);
            MovieDetails intoArray = new MovieDetails(indvObjs.getString(ID_PARAM),indvObjs.getString(TITLE_PARAM), indvObjs.getString(IMGURL_PARAM), indvObjs.getString(SYNOPSYS_PARAM), String.valueOf(indvObjs.getDouble(RATING_PARAM)), indvObjs.getString(RELEASE_DATE_PARAM));
            details[i] = intoArray;
        }
        return details;
    }

    public static ArrayList<Trailer> parseTrailerDetails(String trailerStr) throws JSONException{
        JSONObject json = new JSONObject(trailerStr);
        JSONArray trailers = json.getJSONArray(RESULTS_PARAM);
        ArrayList<Trailer> traileResult = new ArrayList<>();

        for (int i = 0; i< trailers.length(); i++){
            JSONObject trailerObject = trailers.getJSONObject(i);
            String site = trailerObject.getString(SITE_PARAM);
            if (site.equals("YouTube")){
                String url = "https://www.youtube.com/watch?v="+trailerObject.getString(KEY_PARAM);
                traileResult.add(new Trailer(trailerObject.getString(NAME_PARAM),url));
            }
        }
        return traileResult;
    }

    public static ArrayList<Review> parseReviewDetails(String reviewStr) throws JSONException{
        JSONObject jsonObj = new JSONObject(reviewStr);
        JSONArray reviews = jsonObj.getJSONArray(RESULTS_PARAM);
        ArrayList<Review> reviewResult = new ArrayList<>();

        for (int i = 0; i< reviews.length(); i++){
            JSONObject trailerObject = reviews.getJSONObject(i);
            reviewResult.add(new Review(trailerObject.getString(AUTHOR_PARAM),trailerObject.getString(CONTENT_PARAM)));
        }
        return reviewResult;
    }
}
