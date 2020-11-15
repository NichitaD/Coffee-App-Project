package com.myprojects.corso.views;


import android.os.Bundle;
import android.util.Log;
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
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.myprojects.corso.R;
import com.myprojects.corso.adapters.OfferRecyclerAdapter;
import com.myprojects.corso.model.Offer;
import com.myprojects.corso.viewModels.OffersActivityViewModel;

import java.util.ArrayList;
import java.util.Map;

public class OffersActivity extends AppCompatActivity {

    private static final String TAG = "OffersActivity";
    private OfferRecyclerAdapter public_adapter;
    private TextView title;
    private OffersActivityViewModel mOfferActivityViewModel;
    private ArrayList<Offer> offers = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.review_list);
        title = findViewById(R.id.title_review);
        title.setText("LATEST OFFERS:");
        Log.d(TAG, "onCreate: started");
        mOfferActivityViewModel = ViewModelProviders.of(this).get(OffersActivityViewModel.class);
        mOfferActivityViewModel.init();
        mOfferActivityViewModel.getOffers().observe(this, new Observer<ArrayList<Offer>>() {
            @Override
            public void onChanged(ArrayList<Offer> data) {
                offers = data;
                // TODO: Should only notify adapter, not initialize it
                initRecyclerView();
            }
        });
    }

    private void initRecyclerView() {
        RecyclerView recyclerView = findViewById(R.id.recycler_view);
        public_adapter = new OfferRecyclerAdapter(offers);
        recyclerView.setAdapter(public_adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
//        final DocumentReference docRef = db.collection("offers").document("offers");
//        docRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
//            @Override
//            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
//                Log.d(TAG, "onEvent: called");
//                if (e != null) {
//                    Log.w(TAG, "Listen failed.", e);
//                    return;
//                } else {
//                    mOffers.clear();
//                    mNames.clear();
//                    event = true;
//                    setData();
//                }
//            }
//        });
    }
}












