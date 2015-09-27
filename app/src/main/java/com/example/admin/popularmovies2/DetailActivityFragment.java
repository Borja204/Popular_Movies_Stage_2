package com.example.admin.popularmovies2;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;


public class DetailActivityFragment extends Fragment {

    private Movie mMovie;
    @Bind(R.id.textview_originalTitle)
    TextView txtTitle;
    @Bind(R.id.textView_releaseDate)
    TextView txtreleaseDate;
    @Bind(R.id.textView_rating)
    TextView txtRating;
    @Bind(R.id.textView_synopsis)
    TextView txtSynopsis;
    @Bind(R.id.imageView_posterThumbnail)
    ImageView imgPosterThumbnail;
    @Bind(R.id.reviews_layout)
    LinearLayout reviewsLayout;
    @Bind(R.id.trailers_layout)
    LinearLayout trailersLayout;
    @Bind(R.id.favoriteSwitch)
    Switch favoriteSwitch;


    public DetailActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        Bundle arguments = getArguments();
                if (arguments != null) {
                    mMovie = arguments.getParcelable(Intent.EXTRA_TEXT);
                    FetchMovieDataTask movieTask = new FetchMovieDataTask();
                    movieTask.execute(mMovie.getId());
                    }

        View rootView = inflater.inflate(R.layout.fragment_detail, container, false);
        ButterKnife.bind(this, rootView);

        if (savedInstanceState != null && savedInstanceState.containsKey("movie"))
        {
            mMovie = savedInstanceState.getParcelable("movie");
            updateLayoutFromMovie();
        }
        else {

            Bundle extras = getActivity().getIntent().getExtras();
            if (extras != null) {

                mMovie = extras.getParcelable(Intent.EXTRA_TEXT);
                FetchMovieDataTask movieTask = new FetchMovieDataTask();
                movieTask.execute(mMovie.getId());
        }
        }

        favoriteSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeFavorite();
            }
        });

        return rootView;

    }



    public void changeFavorite() {

        if (!favoriteSwitch.isChecked()) {
            removeFavorite(mMovie);
        }
        else {
            addFavorite(mMovie);
        }
    }

    public void updateLayoutFromMovie() {

        if (mMovie != null) {

        if (isFavorite(mMovie.getId())) {

            favoriteSwitch.setChecked(true);

        }

        txtTitle.setText(mMovie.getTitle());
            txtreleaseDate.setText(mMovie.getReleaseDate());
            txtRating.setText(mMovie.getUserRating().toString()+"/10.0");
            txtSynopsis.setText(mMovie.getSynopsis());

            Picasso.with(getActivity())
                    .load("http://image.tmdb.org/t/p/w185/"+mMovie.getPoster())
                    .placeholder(R.drawable.user_placeholder)
                    .error(R.drawable.user_placeholder_error)
                    .into(imgPosterThumbnail);

            ArrayList<String> reviews = mMovie.getReviews();

        if (reviews.size() < 1) {
            TextView textView1 = new TextView(getActivity());
            textView1.setText("No reviews for this movie.");
            reviewsLayout.addView(textView1);
        }
        else {
            for (String s: reviews) {

                TextView textView_Review = new TextView(getActivity());
                textView_Review.setText(s);
                textView_Review.setTextSize(18);
                textView_Review.setTextColor(Color.BLACK);
                reviewsLayout.addView(textView_Review);

                TextView textView1 = new TextView(getActivity());
                textView1.setText(" ");
                reviewsLayout.addView(textView1);

            }

        }

            ArrayList<String> trailers = mMovie.getTrailers();

        if (trailers.size() < 1) {
            TextView textView = new TextView(getActivity());
            textView.setText("No trailers for this movie.");
            trailersLayout.addView(textView);
        }
        else {
            for (String s : trailers) {

                final String source = s;

                ImageView imageView_play = new ImageView(getActivity());
                imageView_play.setImageResource(R.drawable.ic_action);
                imageView_play.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.youtube.com/watch?v=" + source)));
                    }
                });
                trailersLayout.addView(imageView_play);

                TextView textView = new TextView(getActivity());
                textView.setText(" ");
                trailersLayout.addView(textView);

            }
        }

        }

    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putParcelable("movie", mMovie);
        super.onSaveInstanceState(outState);

    }

    public class FetchMovieDataTask extends AsyncTask<Integer, Void, Movie> {

        private final String LOG_TAG = FetchMovieDataTask.class.getSimpleName();

        /**
         * Take the String representing the complete forecast in JSON Format and
         * pull out the data we need to construct the Strings needed for the wireframes.
         *
         * Fortunately parsing is easy:  constructor takes the JSON string and converts it
         * into an Object hierarchy for us.
         */
        private Movie getMovieDataFromJson(String movieJsonStr)
                throws JSONException {

            // These are the names of the JSON objects that need to be extracted.

            final String OWM_ID = "id";
            final String OWM_ORIGINAL_TITLE = "original_title";
            final String OWM_SYNOPSIS = "overview";
            final String OWM_RELEASE_DATE = "release_date";
            final String OWM_RATING = "vote_average";
            final String OWM_POSTER = "poster_path";
            final String OWM_TRAILER = "trailers";
            final String OWM_REVIEW = "reviews";
            final String OWM_TRAILER_LIST = "youtube";
            final String OWM_REVIEW_LIST = "results";
            final String OWM_TRAILER_LIST_SOURCE = "source";
            final String OWM_REVIEW_LIST_AUTHOR = "author";
            final String OWM_REVIEW_LIST_CONTENT = "content";

            JSONObject movieJSON = new JSONObject(movieJsonStr);

                int id = 0;
                String title = "";
                String synopsis = "";
                String releaseDate = "";
                String poster = "";
                Double rating = 0.0;
                ArrayList<String> trailers = new ArrayList<>();
                ArrayList<String> reviews = new ArrayList<>();

                id = movieJSON.getInt(OWM_ID);
                title = movieJSON.getString(OWM_ORIGINAL_TITLE);
                synopsis = movieJSON.getString(OWM_SYNOPSIS);
                releaseDate = movieJSON.getString(OWM_RELEASE_DATE);
                poster = movieJSON.getString(OWM_POSTER);
                rating = movieJSON.getDouble(OWM_RATING);

                JSONArray movieTrailersArray = movieJSON.getJSONObject(OWM_TRAILER).getJSONArray(OWM_TRAILER_LIST);

                    for(int i = 0; i < movieTrailersArray.length(); i++) {

                       trailers.add(movieTrailersArray.getJSONObject(i).getString(OWM_TRAILER_LIST_SOURCE));

                    }

                JSONArray movieReviewsArray = movieJSON.getJSONObject(OWM_REVIEW).getJSONArray(OWM_REVIEW_LIST);

                    for(int i = 0; i < movieReviewsArray.length(); i++) {

                        reviews.add(movieReviewsArray.getJSONObject(i).getString(OWM_REVIEW_LIST_AUTHOR)+": "+movieReviewsArray.getJSONObject(i).getString(OWM_REVIEW_LIST_CONTENT));

                    }

                Movie movie = new Movie(id, title, poster, synopsis, releaseDate, rating, trailers, reviews);

            Log.v(LOG_TAG, "Movie entry: " + movie.getTitle() + ", " + movie.getReleaseDate() + ", " + movie.getSynopsis() + ", " + movie.getUserRating() + ", " + movie.getPoster()+ ", " + movie.getTrailers()+ ", " + movie.getReviews());

            return movie;

        }

        @Override
        protected Movie doInBackground(Integer... params) {

            if (params.length == 0) {
                return null;
            }

            // These two need to be declared outside the try/catch
            // so that they can be closed in the finally block.
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            // Will contain the raw JSON response as a string.
            String movieJsonStr = null;

            int movieId = params[0];
            String apiKey= "YOUR_API_KEY";

            try {

              //  http://api.themoviedb.org/3/movie/76341?api_key=313d4ee7b718897472a728de10aaa790&append_to_response=trailers,reviews

                final String MOVIES_BASE_URL = "http://api.themoviedb.org/3/movie/"+movieId+"?";
                final String API_KEY_PARAM = "api_key";


                Uri builtUri = Uri.parse(MOVIES_BASE_URL).buildUpon()
                        .appendQueryParameter(API_KEY_PARAM, apiKey)
                        .build();

                URL url = new URL(builtUri.toString()+"&append_to_response=trailers,reviews");

                Log.v(LOG_TAG, "Built uri: "+  builtUri.toString());

                // Create the request to OpenWeatherMap, and open the connection
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                // Read the input stream into a String
                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) {
                    // Nothing to do.
                    return null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                    // But it does make debugging a *lot* easier if you print out the completed
                    // buffer for debugging.
                    buffer.append(line + "\n");
                }

                if (buffer.length() == 0) {
                    // Stream was empty.  No point in parsing.
                    return null;
                }
                movieJsonStr = buffer.toString();
                Log.v(LOG_TAG, "Movies JSON String: "+ movieJsonStr);
                try {
                    return getMovieDataFromJson(movieJsonStr);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } catch (IOException e) {
                Log.e("PlaceholderFragment", "Error ", e);
                // If the code didn't successfully get the weather data, there's no point in attemping
                // to parse it.
                return null;
            } finally{
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.e("PlaceholderFragment", "Error closing stream", e);
                    }
                }
            }

            return null;
        }

        @Override
        protected void onPostExecute(Movie movie) {
            super.onPostExecute(movie);

            if (movie != null) {
                mMovie = movie;
                updateLayoutFromMovie();
            }
        }
    }

    public void storeFavorites(List<Movie> favorites) {

        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());
        Gson gson = new Gson();
        String jsonFavorites = gson.toJson(favorites);
        pref.edit().putString("favorites", jsonFavorites).commit();

    }

    public ArrayList<Movie> loadFavorites() {

        ArrayList<Movie> favoriteMovies;
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());
        if (pref.contains("favorites")) {
            String jsonFavorites = pref.getString("favorites", null);
            Gson gson = new Gson();
            favoriteMovies = gson.fromJson(jsonFavorites, new TypeToken<List<Movie>>(){}.getType());

        } else
            return null;
        return favoriteMovies;
    }
    public void addFavorite( Movie movie) {
        ArrayList<Movie> favorites = loadFavorites();
        if (favorites == null) {
            favorites = new ArrayList<>();
        }
        favorites.add(movie);
        storeFavorites(favorites);
    }
    public void removeFavorite(Movie movie) {
        ArrayList<Movie> favorites = loadFavorites();
        Movie movieToDelete = new Movie();

        //remove with the movie was not working
        if (favorites != null) {
            for (Movie m : favorites) {
                if (m.getId() == movie.getId()) {
                    movieToDelete=m;
                }
            }
            favorites.remove(movieToDelete);
            storeFavorites(favorites);
        }
    }

    public boolean isFavorite(int id) {

        Boolean isFavorite = false;
        ArrayList<Movie> favorites = loadFavorites();

        if (favorites != null) {
            for (Movie m : favorites) {
                if (m.getId() == id) {
                    isFavorite = true;
                }
            }
        }
        return isFavorite;

    }

}
