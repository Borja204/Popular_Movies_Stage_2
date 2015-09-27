package com.example.admin.popularmovies2;

import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

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


public class MainActivityFragment extends Fragment {

    private MoviesAdapter mMoviesAdapter;
    private ArrayList<Movie> mMovies = new ArrayList<>();

    @Bind(R.id.gridView_movies)
    GridView movies_gridview;

    public interface Callback {
        /**
         * DetailFragmentCallback for when an item has been selected.
         */
        public void onItemSelected(Movie movie);
    }

    public MainActivityFragment() {
    }

    public void updateMovies() {

        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());
        String sortValue = pref.getString(getString(R.string.pref_sort_key), getString(R.string.pref_sort_popular));

        if (sortValue.equals(getString(R.string.pref_sort_favorites))) {

            mMovies = loadFavorites();
            mMoviesAdapter.clear();
            if (mMovies != null) {
                for (Movie m: mMovies) {
                    mMoviesAdapter.add(m);
                }
            }
        }

        else {
            FetchMoviesTask movieTask = new FetchMoviesTask();
            movieTask.execute(sortValue);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        updateMovies();
    }


    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putParcelableArrayList("movies_list", mMovies);
        super.onSaveInstanceState(outState);

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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        ButterKnife.bind(this, rootView);

        if (savedInstanceState != null && savedInstanceState.containsKey("movies_list"))
        {
            mMovies = savedInstanceState.getParcelableArrayList("movies_list");
        }

        mMoviesAdapter = new MoviesAdapter(getActivity(), mMovies);
        movies_gridview.setAdapter(mMoviesAdapter);
        movies_gridview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                Movie movie = mMoviesAdapter.getItem(position);
                ((Callback) getActivity())
                        .onItemSelected(movie);
                /*Intent i = new Intent(getActivity(), DetailActivity.class);
                i.putExtra(Intent.EXTRA_TEXT, movie);
                startActivity(i);*/
            }
        });

        return rootView;

    }

    public class FetchMoviesTask extends AsyncTask<String, Void, ArrayList<Movie>> {

        private final String LOG_TAG = FetchMoviesTask.class.getSimpleName();

        /**
         * Take the String representing the complete forecast in JSON Format and
         * pull out the data we need to construct the Strings needed for the wireframes.
         *
         * Fortunately parsing is easy:  constructor takes the JSON string and converts it
         * into an Object hierarchy for us.
         */
        private ArrayList<Movie> getMoviesDataFromJson(String moviesJsonStr)
                throws JSONException {

            // These are the names of the JSON objects that need to be extracted.
            final String OWM_LIST = "results";
            final String OWM_ID = "id";
            final String OWM_ORIGINAL_TITLE = "original_title";
            final String OWM_SYNOPSIS = "overview";
            final String OWM_RELEASE_DATE = "release_date";
            final String OWM_RATING = "vote_average";
            final String OWM_POSTER = "poster_path";

            JSONObject moviesJson = new JSONObject(moviesJsonStr);
            JSONArray moviesArray = moviesJson.getJSONArray(OWM_LIST);


            ArrayList<Movie> result = new ArrayList<>();

            for(int i = 0; i < moviesArray.length(); i++) {

                int id = 0;
                String title = "";
                String synopsis = "";
                String releaseDate = "";
                String poster = "";
                Double rating = 0.0;

                // Get the JSON object representing the day
                JSONObject movieJSON = moviesArray.getJSONObject(i);

                id = movieJSON.getInt(OWM_ID);
                title = movieJSON.getString(OWM_ORIGINAL_TITLE);
                synopsis = movieJSON.getString(OWM_SYNOPSIS);
                releaseDate = movieJSON.getString(OWM_RELEASE_DATE);
                poster = movieJSON.getString(OWM_POSTER);
                rating = movieJSON.getDouble(OWM_RATING);

                Movie movie = new Movie(id, title, poster, synopsis, releaseDate, rating);

                result.add(movie);

            }

            for (Movie m : result) {
                Log.v(LOG_TAG, "Movies entry: " + m.getTitle() + ", " + m.getReleaseDate() + ", " + m.getSynopsis() + ", " + m.getUserRating() + ", " + m.getPoster() );
            }
            return result;

        }

        @Override
        protected ArrayList<Movie> doInBackground(String... params) {

            if (params.length == 0) {
                return null;
            }

            // These two need to be declared outside the try/catch
            // so that they can be closed in the finally block.
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            // Will contain the raw JSON response as a string.
            String moviesJsonStr = null;

            String sortMode = params[0];
            String apiKey= "YOUR_API_KEY";

            try {

                final String MOVIES_BASE_URL = "http://api.themoviedb.org/3/discover/movie?";
                final String SORT_PARAM = "sort_by";
                final String API_KEY_PARAM = "api_key";


                Uri builtUri = Uri.parse(MOVIES_BASE_URL).buildUpon()
                        .appendQueryParameter(SORT_PARAM, sortMode)
                        .appendQueryParameter(API_KEY_PARAM, apiKey)
                        .build();

                URL url = new URL(builtUri.toString());

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
                moviesJsonStr = buffer.toString();
                Log.v(LOG_TAG, "Movies JSON String: "+ moviesJsonStr);
                try {
                    return getMoviesDataFromJson(moviesJsonStr);

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
        protected void onPostExecute(ArrayList<Movie> movies) {
            super.onPostExecute(movies);

            if (movies != null) {

                mMovies = movies;

                mMoviesAdapter.clear();

                for (Movie m: movies) {

                    mMoviesAdapter.add(m);
                }

            }

        }

    }
}
