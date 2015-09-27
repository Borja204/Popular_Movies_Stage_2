package com.example.admin.popularmovies2;


import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

public class Movie implements Parcelable{

    private int id;
    private String title;
    private String poster;
    private String synopsis;
    private String releaseDate;
    private Double userRating;
    private ArrayList<String> trailers;
    private ArrayList<String> reviews;

    public Movie(int id, String title, String poster, String synopsis, String releaseDate, Double userRating) {
        this.id = id;
        this.title = title;
        this.poster = poster;
        this.synopsis = synopsis;
        this.releaseDate = releaseDate;
        this.userRating = userRating;
    }

    public Movie(int id, String title, String poster, String synopsis, String releaseDate, Double userRating, ArrayList<String> trailers, ArrayList<String> reviews) {
        this.id = id;
        this.title = title;
        this.poster = poster;
        this.synopsis = synopsis;
        this.releaseDate = releaseDate;
        this.userRating = userRating;
        this.trailers = trailers;
        this.reviews = reviews;
    }

    public Movie() {
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getPoster() {
        return poster;
    }

    public void setPoster(String poster) {
        this.poster = poster;
    }

    public String getSynopsis() {
        return synopsis;
    }

    public void setSynopsis(String synopsis) {
        this.synopsis = synopsis;
    }

    public String getReleaseDate() {
        return releaseDate;
    }

    public void setReleaseDate(String releaseDate) {
        this.releaseDate = releaseDate;
    }

    public Double getUserRating() {
        return userRating;
    }

    public void setUserRating(Double userRating) {
        this.userRating = userRating;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public ArrayList<String> getTrailers() {
        return trailers;
    }

    public void setTrailers(ArrayList<String> trailers) {
        this.trailers = trailers;
    }

    public ArrayList<String> getReviews() {
        return reviews;
    }

    public void setReviews(ArrayList<String> reviews) {
        this.reviews = reviews;
    }

    //from  http://developer.android.com/reference/android/os/Parcelable.html

    private Movie(Parcel in) {
        id = in.readInt();
        title = in.readString();
        poster = in.readString();
        synopsis = in.readString();
        releaseDate = in.readString();
        userRating = in.readDouble();
        trailers = in.readArrayList(null);
        reviews = in.readArrayList(null);
    }

    public static final Creator<Movie> CREATOR
            = new Creator<Movie>() {
        public Movie createFromParcel(Parcel in) {
            return new Movie(in);
        }

        public Movie[] newArray(int size) {
            return new Movie[size];
        }

    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(title);
        dest.writeString(poster);
        dest.writeString(synopsis);
        dest.writeString(releaseDate);
        dest.writeDouble(userRating);
        dest.writeList(trailers);
        dest.writeList(reviews);
    }
}
