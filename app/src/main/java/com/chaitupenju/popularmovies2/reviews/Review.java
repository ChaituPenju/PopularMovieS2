package com.chaitupenju.popularmovies2.reviews;

import android.util.Log;

import java.util.ArrayList;

public class Review {
     String author;
     String content;

    private static final String delimitStr = "-TS-";
    private static final String delimitReviewStr = "-ROBJ-";
    public Review(String author, String content) {
        this.author = author;
        this.content = content;
    }

    /*
    my string will be like this
    author0,content0-RS-author1,content1-RS-author2,content2
    */
    public static ArrayList<Review> stringToArray(String reviewStr){
        ArrayList<Review> retReviewArr = new ArrayList<>();
        String revParts[] = reviewStr.split(delimitStr);
        for(String review: revParts){
            String revs[] = review.split(delimitReviewStr);
            try{
                retReviewArr.add(new Review(revs[0], revs[1]));
            }catch (IndexOutOfBoundsException e){
                e.printStackTrace();
            }
        }
        Log.i("STR2ARR",retReviewArr.toString());
        return retReviewArr;
    }

    public static String arrayToString(ArrayList<Review> reviewArr){
        StringBuilder retStr = new StringBuilder();
        try{
            for (int i = 0; i < reviewArr.size(); i++) {
                retStr.append(reviewArr.get(i).author).append(delimitReviewStr).append(reviewArr.get(i).content);
                if (i < reviewArr.size() - 1) {
                    retStr.append(delimitStr);
                }
            }
        }
        catch(NullPointerException npe){
            return "";
        }
        return retStr.toString();
    }
}
