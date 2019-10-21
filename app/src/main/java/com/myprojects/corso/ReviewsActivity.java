package com.myprojects.corso;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

public class ReviewsActivity extends AppCompatActivity {

    private static final String TAG = "ReviewsActivity";
    private ArrayList<String> mEmails = new ArrayList<>();
    private ArrayList<String> mReviews = new ArrayList<>();
    private ArrayList<Long> mRatings = new ArrayList<>();
    private String marker_name;
    private TextView noReviews;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private TextView title;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.review_list);
        title = findViewById(R.id.title_review);
        title.setText("REVIEWS:");
        noReviews = findViewById(R.id.no_reviews);
        Log.d(TAG, "onCreate: started");
        Intent intent = getIntent();
        Log.d( "SpecialTag",intent.getStringExtra("marker"));
        marker_name = intent.getStringExtra("marker");
        setData();
    }

    private void setData() {
        String coffee_shop_id = marker_name.toLowerCase().replaceAll("[^A-Za-z0-9']", "_");
        Log.d(TAG, coffee_shop_id);
        DocumentReference doc_ref = db.collection("coffee_shops").document(coffee_shop_id);
        doc_ref.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        mEmails = (ArrayList<String>) document.get("reviewers");
                        mRatings = (ArrayList<Long>) document.get("reviewers_rating");
                        mReviews = (ArrayList<String>) document.get("reviews");
                        initRecyclerView();
                    } else {
                        Log.d(TAG, "No such document");
                    }
                } else {
                    Log.d(TAG, "get failed with ", task.getException());
                }
            }
        });
    }

    private void initRecyclerView() {
        Log.d(TAG, "initiating recyclerview");
        RecyclerView recyclerView = findViewById(R.id.recycler_view);
        Log.d(TAG, "size:" + mEmails.size());
        RecyclerViewAdapter adapter = new RecyclerViewAdapter( mEmails, mReviews, mRatings);
        recyclerView.setAdapter(adapter);
        if(mEmails.isEmpty()){
            noReviews.setVisibility(View.VISIBLE);
        }else {noReviews.setVisibility(View.INVISIBLE);}
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
    }
}












