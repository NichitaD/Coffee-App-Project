package com.myprojects.corso.views;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.myprojects.corso.R;
import com.myprojects.corso.adapters.OfferViewAdapter;
import com.myprojects.corso.model.CoffeeShop;
import com.myprojects.corso.model.Review;
import com.myprojects.corso.viewModels.CoffeeShopActivityViewModel;

import java.util.ArrayList;

public class CoffeeShopActivity extends AppCompatActivity implements View.OnClickListener {

    private String TAG = "CoffeeShopActivity";
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private String recieved_marker_name;
    private EditText review_text;
    private RatingBar set_rating_bar;
    private ImageView display_rating_bar;
    private Dialog dialog;
    private ImageView nameView;
    private ImageView review_page_logo;
    private TextView noOffersView;
    private ArrayList<String> mOffers = new ArrayList<>();
    private CoffeeShopActivityViewModel mCoffeeShopActivityViewModel;
    private CoffeeShop coffeeShop;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.coffee_shop_view);
        Intent intent = getIntent();
        recieved_marker_name = intent.getStringExtra("name");
        display_rating_bar = findViewById(R.id.rating_bar);
        nameView = findViewById(R.id.coffee_name);
        noOffersView = findViewById(R.id.no_offers);
        mCoffeeShopActivityViewModel = ViewModelProviders.of(this).get(CoffeeShopActivityViewModel.class);
        mCoffeeShopActivityViewModel.init(recieved_marker_name);
        mCoffeeShopActivityViewModel.getCoffeeShop().observe(this, new Observer<CoffeeShop>() {
            @Override
            public void onChanged(CoffeeShop shop) {
                coffeeShop = shop;
                setData();
            }
        });

    }

    private void setData() {
        // Set logo
        Glide.with(getApplicationContext()).load(coffeeShop.getLogoUri()).into(nameView);

        // Set rating
        switch (coffeeShop.getRating()){
            case 1 : this.display_rating_bar.setBackgroundResource(R.drawable.rating_stars_1); break;
            case 2 : this.display_rating_bar.setBackgroundResource(R.drawable.rating_stars_2); break;
            case 3 : this.display_rating_bar.setBackgroundResource(R.drawable.rating_stars_3); break;
            case 4 : this.display_rating_bar.setBackgroundResource(R.drawable.rating_stars_4); break;
            case 5 : this.display_rating_bar.setBackgroundResource(R.drawable.rating_stars_5); break;
        }

        // Set offers
        ListView listView = findViewById(R.id.offer_list_client);
        mOffers = coffeeShop.getOffers();
        OfferViewAdapter adapter = new OfferViewAdapter(this, R.layout.offer_item_client, mOffers, recieved_marker_name);
        listView.setAdapter(adapter);
        if (coffeeShop.getOffers().isEmpty()) {
            noOffersView.setVisibility(View.VISIBLE);
        } else {
            noOffersView.setVisibility(View.INVISIBLE);
        }
    }

    public void onClick(View view) {
        int button = view.getId();

        // Directions button
        if (button == R.id.directions_button) {
            goToDirections();
        }

        // Leave a rating button
        if (button == R.id.rate_button) {
            openRatingDialog();
        }

        // Post rating button
        if (button == R.id.review_done) {
            Review review = new Review(mAuth.getCurrentUser().getEmail(), set_rating_bar.getRating(), review_text.getText().toString());
            mCoffeeShopActivityViewModel.addReview(review);
            dialog.dismiss();
        }

        // See the reviews button
        if (button == R.id.see_reviews) {
            goToReviews();
        }
    }

    // Opens the Map Activity, and displays the directions
    private void goToDirections() {
        Intent intent = new Intent (CoffeeShopActivity.this, MapsActivity.class);
        intent.putExtra("option",4);
        intent.putExtra("name", recieved_marker_name);
        CoffeeShopActivity.this.startActivity(intent);
    }

    // Opens the Reviews Activity for this coffee shop
    private void goToReviews() {
        Intent marker_intent = new Intent(CoffeeShopActivity.this, ReviewsActivity.class);
        marker_intent.putExtra("marker", recieved_marker_name);
        CoffeeShopActivity.this.startActivity(marker_intent);
    }

    //Displays the rating dialog
    private void openRatingDialog() {
        LayoutInflater inflater = LayoutInflater.from(this);
        final Dialog ratingDialog = new Dialog(this, R.style.mydialog);
        Window window = ratingDialog.getWindow();
        WindowManager.LayoutParams wlp = window.getAttributes();
        wlp.width = WindowManager.LayoutParams.MATCH_PARENT;
        wlp.height = WindowManager.LayoutParams.MATCH_PARENT;
        wlp.gravity = Gravity.CENTER;
        wlp.flags &= ~WindowManager.LayoutParams.FLAG_DIM_BEHIND;
        wlp.y = 60;
        window.setAttributes(wlp);
        View rating_view = inflater.inflate(R.layout.rating_dialog, null);
        ratingDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        ratingDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        ratingDialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
        ratingDialog.setContentView(rating_view);
        review_page_logo = ratingDialog.findViewById(R.id.review_page_logo);
        Glide.with(getApplicationContext()).load(coffeeShop.getLogoUri()).into(review_page_logo);
        ratingDialog.show();
        review_text = ratingDialog.findViewById(R.id.review);
        set_rating_bar = ratingDialog.findViewById(R.id.ratingBar);
        set_rating_bar.setStepSize(1);
        dialog = ratingDialog;
    }
}

