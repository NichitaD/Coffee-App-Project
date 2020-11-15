package com.myprojects.corso.views;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.myprojects.corso.R;
import com.myprojects.corso.adapters.RecyclerViewAdapter;
import com.myprojects.corso.model.CoffeeShop;
import com.myprojects.corso.viewModels.ReviewsActivityViewModel;

import java.util.ArrayList;

public class ReviewsActivity extends AppCompatActivity {

    private String marker_name;
    private TextView noReviews;
    private TextView title;
    private ReviewsActivityViewModel mReviewsActivityViewModel;
    private CoffeeShop mCoffeeShop;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.review_list);
        title = findViewById(R.id.title_review);
        title.setText("REVIEWS:");
        noReviews = findViewById(R.id.no_reviews);
        Intent intent = getIntent();
        marker_name = intent.getStringExtra("marker");
        mReviewsActivityViewModel = ViewModelProviders.of(this).get(ReviewsActivityViewModel.class);
        mReviewsActivityViewModel.init(marker_name.toLowerCase().replaceAll("[^A-Za-z0-9']", "_"));
        mReviewsActivityViewModel.getCoffeeShop().observe(this, new Observer<CoffeeShop>() {
            @Override
            public void onChanged(CoffeeShop coffeeShop) {
                if(coffeeShop != null) {
                    mCoffeeShop = coffeeShop;
                    initRecyclerView();
                }
            }
        });
    }

    private void initRecyclerView() {
        RecyclerView recyclerView = findViewById(R.id.recycler_view);
        RecyclerViewAdapter adapter = new RecyclerViewAdapter( mCoffeeShop.getReviews());
        recyclerView.setAdapter(adapter);
        if(mCoffeeShop.getReviews().isEmpty()){
            noReviews.setVisibility(View.VISIBLE);
        }else {noReviews.setVisibility(View.INVISIBLE);}
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
    }
}












