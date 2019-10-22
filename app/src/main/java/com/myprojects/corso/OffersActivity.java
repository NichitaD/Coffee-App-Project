package com.myprojects.corso;


import android.os.Bundle;
import android.util.Log;
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
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import java.util.ArrayList;
import java.util.Map;

public class OffersActivity extends AppCompatActivity{

    private static final String TAG = "OffersActivity";
    private ArrayList<Map> mapArray = new ArrayList<>();
    private ArrayList<String> mNames = new ArrayList<>();
    private ArrayList<String> mOffers = new ArrayList<>();
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private OfferRecyclerAdapter public_adapter;
    private TextView title;
    private boolean event = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.review_list);
        title = findViewById(R.id.title_review);
        title.setText("LATEST OFFERS:");
        Log.d(TAG, "onCreate: started");
        setData();
    }

    private void setData() {
        DocumentReference doc_ref = db.collection("offers").document("offers");
        doc_ref.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        mapArray =(ArrayList<Map>) document.get("offers");
                        for(Map <String, Map<String,String>> map: mapArray){
                            for (Map.Entry<String, Map<String, String>> entry : map.entrySet()) {
                                for (Map.Entry<String,String> second_entry : entry.getValue().entrySet()) {
                                    mNames.add(second_entry.getKey());
                                    mOffers.add(second_entry.getValue());
                                }
                            }
                        }
                        if(event == true){
                            public_adapter.notifyDataSetChanged();
                            event = false;
                            return;
                        }
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
        RecyclerView recyclerView = findViewById(R.id.recycler_view);
        OfferRecyclerAdapter adapter = new OfferRecyclerAdapter(mNames, mOffers);
        public_adapter = adapter;
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        final DocumentReference docRef = db.collection("offers").document("offers");
        docRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                Log.d(TAG, "onEvent: called");
                if (e != null) {
                    Log.w(TAG, "Listen failed.", e);
                    return;
                } else {
                    mOffers.clear();
                    mNames.clear();
                    event = true;
                    setData();
                }
            }
        });
    }
}












