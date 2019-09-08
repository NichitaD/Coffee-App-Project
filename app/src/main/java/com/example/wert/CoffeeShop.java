package com.example.wert;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;

public class CoffeeShop {
    private String name;
    private LatLng latLng;
    private Integer rating;
    private int numberOfRatings = 0, ratingsSum = 0;
    public  ArrayList<String>  reviews = new ArrayList<>();
    //Logo to be added

    public  CoffeeShop(String name, LatLng latLng){
        this.name = name;
        this.latLng = latLng;
    }

    public String getName() {
        return this.name;
    }

    public void updateRating (int rating) {
        ++this.numberOfRatings;
        this.ratingsSum += rating;
        this.rating = ratingsSum / numberOfRatings;
    }
    public Integer getRating () {
        return this.rating;
    }

    public LatLng getPosition(){
        return this.latLng;
    }
}
