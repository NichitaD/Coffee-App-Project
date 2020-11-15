package com.myprojects.corso.model;

public class Review {
    private String author;
    private String id;
    private Float rating;
    private String text;


    public Review (String author, Float rating, String text) {
        this.author = author;
        this.rating = rating;
        this.text = text;
    }

    public String getAuthor(){
        return author;
    }

    public String getId(){
        return id;
    }

    public Float getRating(){
        return rating;
    }

    public String getText(){
        return text;
    }
}
