package com.chaitupenju.popularmovies2.reviews;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.chaitupenju.popularmovies2.R;

import java.util.ArrayList;

public class ReviewsAdapter extends BaseAdapter {
    private Context context;
    private ArrayList<Review> mReviews;

    public ReviewsAdapter(Context context){
        this.context = context;
        mReviews = new ArrayList<>();
    }

    private void clear(){
        mReviews.clear();
        notifyDataSetChanged();
    }

    public void setReviews(ArrayList<Review> reviews){
        clear();
        mReviews.addAll(reviews);
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return mReviews.size();
    }

    @Override
    public Review getItem(int position) {
        if(position >= 0 && position < mReviews.size())
            return mReviews.get(position);
        return null;
    }

    @Override
    public long getItemId(int position) {
        if(getItem(position) == null)
            return -1L;
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View reviewItem = convertView;
        Review review = getItem(position);
        if(reviewItem == null){
            try{
                LayoutInflater li;
                li = LayoutInflater.from(context);
                reviewItem = li.inflate(R.layout.review_list_item, parent, false);
            }
            catch(Exception e){
                e.printStackTrace();
            }
        }
        if(reviewItem != null){
            ((TextView) reviewItem.findViewById(R.id.tv_review_item_author)).setText(review.author);
            ((TextView) reviewItem.findViewById(R.id.tv_review_item_content)).setText(review.content);
        }
        return reviewItem;
    }
}
