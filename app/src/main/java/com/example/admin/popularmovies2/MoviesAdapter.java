package com.example.admin.popularmovies2;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;


public class MoviesAdapter extends ArrayAdapter<Movie> {

    private static final String LOG_TAG = MoviesAdapter.class.getSimpleName();
    private Context mContext;

    @Bind(R.id.list_item_movies_grid_imageview)
    ImageView posterView;

    public MoviesAdapter(Activity context, List<Movie> movies) {
        super(context, 0,  movies);
        mContext = context;

    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Movie movie = getItem(position);

        View rootView = LayoutInflater.from(getContext()).inflate(R.layout.list_item_movies_grid, parent, false);
        ButterKnife.bind(this, rootView);

        Picasso.with(mContext).load("http://image.tmdb.org/t/p/w185/"+movie.getPoster()).into(posterView);

        return rootView;
    }
}
