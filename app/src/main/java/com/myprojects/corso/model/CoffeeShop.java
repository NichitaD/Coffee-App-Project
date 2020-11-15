package com.myprojects.corso.model;

import android.net.Uri;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.HashMap;


public class CoffeeShop {

    private String name;
    private GeoPoint location;
    private String logoUri;
    private ArrayList<Review> reviews;
    private ArrayList<String> offers;
    private Long rating;

    public void setName(String name) {
        this.name = name;
    }

    public void setLocation(GeoPoint location) {
        this.location = location;
    }

    public void setReviews(ArrayList<HashMap<String, String>> reviews) {
        ArrayList<Review> reviewsArray = new ArrayList<>();
        for(HashMap<String, String> review : reviews){
            reviewsArray.add(new Review(review.get("author"), Float.valueOf(review.get("rating")), review.get("text")));
        }
        this.reviews = reviewsArray;
    }

    public void setOffers(ArrayList<String> offers) {
        this.offers = offers;
    }

    public void setRating(Long rating) {
        this.rating = rating;
    }

    public void setLogoUri(String uri) {
        this.logoUri = uri;
    }

    public String getName() {
        return name;
    }

    public GeoPoint getLocation() {
        return location;
    }

    public ArrayList<Review> getReviews() {
        return reviews;
    }

    public ArrayList<String> getOffers() {
        return offers;
    }

    public int getRating() {
        return rating.intValue();
    }

    public String getLogoUri() {
        return logoUri;
    }
}
