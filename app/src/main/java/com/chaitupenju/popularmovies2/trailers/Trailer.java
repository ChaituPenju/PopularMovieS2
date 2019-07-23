package com.chaitupenju.popularmovies2.trailers;

import android.util.Log;

import java.util.ArrayList;

public class Trailer{
    String title;
    String url;
    private static final String delimitStr = "-TS-";
    private static final String delimitTrailerStr = "-TOBJ-";

    public Trailer(String title, String url) {
        this.title = title;
        this.url = url;
    }

    public static String arrayToString(ArrayList<Trailer> trailers){
        StringBuilder res = new StringBuilder();
        try {
            for (int i = 0; i < trailers.size(); i++) {
                res.append(trailers.get(i).title).append(delimitTrailerStr).append(trailers.get(i).url);
                if (i < trailers.size() - 1) {
                    res.append(delimitStr);
                }
            }
        }
        catch(NullPointerException e){
            return "";
        }
        return res.toString();
    }

    public static ArrayList<Trailer> stringToArray(String string){
        String[] elements = string.split(delimitStr);
        ArrayList<Trailer> res = new ArrayList<>();
        for (String element : elements) {
                String[] item = element.split(delimitTrailerStr);
            try{
                res.add(new Trailer(item[0], item[1]));
            }catch (IndexOutOfBoundsException e) {
                e.printStackTrace();
            }
        }
        Log.i("STR2ARR",res.toString());
        return res;
    }

}