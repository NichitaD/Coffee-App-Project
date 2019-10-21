package com.myprojects.corso;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
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
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Transaction;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import java.util.ArrayList;

public class CoffeeShopActivity extends AppCompatActivity implements View.OnClickListener {

    private String TAG = "CoffeeShopActivity";
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private String recieved_marker_name;
    private EditText review_text;
    private RatingBar rating_bar;
    private Dialog dialog;
    private StorageReference mStorageRef;
    private ImageView imageView;
    private ImageView review_page_logo;
    private TextView noOffers;
    private String imageURL;
    private ArrayList<String> mOffers = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.coffee_shop_view);
        imageView = findViewById(R.id.coffee_name);
        noOffers = findViewById(R.id.no_offers);
        Intent intent = getIntent();
        recieved_marker_name = intent.getStringExtra("name");
        getOffers();
        String storage_name = recieved_marker_name.toLowerCase().replaceAll("[^A-Za-z0-9']", "_") + ".png";
        mStorageRef = FirebaseStorage.getInstance().getReference().child(storage_name);
        getLogo(imageView);
        ImageView rating_bar = findViewById(R.id.rating_bar);
        setRating( rating_bar);
    }

    public void onClick(View view) {
        int button = view.getId();
        if (button == R.id.directions_button) {
            Intent intent = new Intent (CoffeeShopActivity.this, MapsActivity.class);
            intent.putExtra("option",4);
            intent.putExtra("name", recieved_marker_name);
            CoffeeShopActivity.this.startActivity(intent);
        }
        if (button == R.id.rate_button) {
                LayoutInflater inflater = LayoutInflater.from(this);
                final Dialog dialog3 = new Dialog(this, R.style.mydialog);
                Window window = dialog3.getWindow();
                WindowManager.LayoutParams wlp = window.getAttributes();
                wlp.width = WindowManager.LayoutParams.MATCH_PARENT;
                wlp.height = WindowManager.LayoutParams.MATCH_PARENT;
                wlp.gravity = Gravity.CENTER;
                wlp.flags &= ~WindowManager.LayoutParams.FLAG_DIM_BEHIND;
                wlp.y = 60;
                window.setAttributes(wlp);
                View view3 = inflater.inflate(R.layout.rating_dialog, null);
                dialog3.requestWindowFeature(Window.FEATURE_NO_TITLE);
                dialog3.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                dialog3.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
                dialog3.setContentView(view3);
                review_page_logo = dialog3.findViewById(R.id.review_page_logo);
                getLogo(review_page_logo);
                dialog3.show();
                dialog = dialog3;
                review_text = dialog3.findViewById(R.id.review);
                rating_bar = dialog3.findViewById(R.id.ratingBar);
                rating_bar.setStepSize(1);
            }
        if (button == R.id.review_done) {
            Float rating = rating_bar.getRating();
            String review = review_text.getText().toString();
            String user = mAuth.getCurrentUser().getEmail();
            dialog.dismiss();
            addReview(rating.intValue(), review, user);
        }
        if (button == R.id.see_reviews) {
            Intent marker_intent = new Intent(CoffeeShopActivity.this, ReviewsActivity.class);
            marker_intent.putExtra("marker", recieved_marker_name);
            CoffeeShopActivity.this.startActivity(marker_intent);
        }
    }

    private void setRating (ImageView ratingBar) {
        String coffee_shop_id = recieved_marker_name.toLowerCase().replaceAll("[^A-Za-z0-9']", "_");
        Log.d("Test_name",coffee_shop_id);
        DocumentReference doc_ref = db.collection("coffee_shops").document(coffee_shop_id);
        doc_ref.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                Log.d(TAG, "Task is started ----------------");
                if (task.isSuccessful()) {
                    Log.d(TAG, "Task is succcesful ----------------");
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        Long rating = (Long) document.get("rating");
                        switch (rating.intValue()){
                            case 1 : ratingBar.setBackgroundResource(R.drawable.rating_stars_1); break;
                            case 2 : ratingBar.setBackgroundResource(R.drawable.rating_stars_2); break;
                            case 3 : ratingBar.setBackgroundResource(R.drawable.rating_stars_3); break;
                            case 4 : ratingBar.setBackgroundResource(R.drawable.rating_stars_4); break;
                            case 5 : ratingBar.setBackgroundResource(R.drawable.rating_stars_5); break;
                        }
                    } else {
                        Log.d(TAG, "No such document");
                        return;
                    }
                } else {
                    Log.d(TAG, "get failed with ", task.getException());
                    return;
                }
            }
        });
    }

    private void addReview(Integer rating, String review, String user) {
        String coffee_shop_id =recieved_marker_name.toLowerCase().replaceAll("[^A-Za-z0-9']", "_");
        DocumentReference doc_ref = db.collection("coffee_shops").document(coffee_shop_id);
        doc_ref.update("ratings_sum", FieldValue.increment(rating));
        doc_ref.update("number_of_ratings", FieldValue.increment(1));
        db.runTransaction(new Transaction.Function<Void>() {
            @Override
            public Void apply(Transaction transaction) throws FirebaseFirestoreException {
                DocumentSnapshot snapshot = transaction.get(doc_ref);
                ArrayList<String> reviewers = (ArrayList<String>) snapshot.get("reviewers");
                ArrayList<String> reviews = (ArrayList<String>) snapshot.get("reviews");
                ArrayList<Integer> reviewers_rating = (ArrayList<Integer>) snapshot.get("reviewers_rating");
                Long update_rating = (Long) snapshot.get("ratings_sum") / (Long) snapshot.get("number_of_ratings");
                Log.d("rating_update", "updated rating :" + update_rating);
                reviewers.add(user);
                reviews.add(review);
                reviewers_rating.add(rating);
                transaction.update(doc_ref, "reviewers", reviewers);
                transaction.update(doc_ref, "reviewers_rating", reviewers_rating);
                transaction.update(doc_ref, "reviews", reviews);
                transaction.update(doc_ref, "rating", update_rating);
                return null;
            }
        }).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Log.d(TAG, "Transaction success!");
            }
        })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Transaction failure.", e);
                    }
                });
    }

    public void getLogo(ImageView view) {
        mStorageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                imageURL = uri.toString();
                Glide.with(getApplicationContext()).load(imageURL).into(view);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle any errors
            }
        });
    }

    private void getOffers() {
        String coffee_shop_id = recieved_marker_name.toLowerCase().replaceAll("[^A-Za-z0-9']", "_");
        DocumentReference docRef = db.collection("coffee_shops").document(coffee_shop_id);
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        mOffers = (ArrayList<String>) document.get("offers");
                        Log.d(TAG, "DocumentSnapshot data: " + document.getData());
                        setData();
                    } else {
                        Log.d(TAG, "No such document");
                    }
                } else {
                    Log.d(TAG, "get failed with ", task.getException());
                }
            }
        });
    }

    private void setData() {
        ListView listView = findViewById(R.id.offer_list_client);
        OfferViewAdapter adapter = new OfferViewAdapter(this, R.layout.offer_item_client, mOffers, recieved_marker_name);
        listView.setAdapter(adapter);
        if (mOffers.isEmpty()) {
            noOffers.setVisibility(View.VISIBLE);
        } else {
            noOffers.setVisibility(View.INVISIBLE);
        }
    }
}

