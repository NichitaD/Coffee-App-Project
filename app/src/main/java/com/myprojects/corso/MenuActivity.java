package com.myprojects.corso;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import java.util.Calendar;

public class MenuActivity extends Activity implements View.OnClickListener {
    private FirebaseAuth mAuth;
    private Calendar old_date = Calendar.getInstance();
    private String TAG = "Menu_Activity";
    private TextView coffee_display;
    private TextView day_info;
    private TextView week_info;
    private TextView month_info;
    private TextView year_info;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
        setContentView(R.layout.activity_menu);
        coffee_display = findViewById(R.id.number_coffee);
        setNumber();
        findViewById(R.id.near_button).setOnClickListener(this);
        findViewById(R.id.all_button).setOnClickListener(this);
        findViewById(R.id.signOut).setOnClickListener(this);
        findViewById(R.id.add_coffee).setOnClickListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkDate();
        setNumber();
    }

    @Override
    public void onClick(View view) {
        int button = view.getId();
        if (button == R.id.near_button) {
            Intent myIntent = new Intent(MenuActivity.this, MapsActivity.class);
            myIntent.putExtra("option", 1);
            MenuActivity.this.startActivity(myIntent);
        }
        if (button == R.id.all_button) {
            Intent myIntent = new Intent(MenuActivity.this, MapsActivity.class);
            myIntent.putExtra("option", 2);
            MenuActivity.this.startActivity(myIntent);
        }
        if (button == R.id.signOut) {
            LoginActivity sign_out = new LoginActivity();
            Toast.makeText(MenuActivity.this,
                    "Signed out",
                    Toast.LENGTH_SHORT).show();
            sign_out.signOut_public(mAuth);
            Intent myIntent = new Intent(MenuActivity.this, LoginActivity.class);
            MenuActivity.this.startActivity(myIntent);
        }
        if (button == R.id.add_coffee) {
            setNumber();
            updateDatabase();
        }
        if (button == R.id.more){
            openStats();
        }
    }

    private void checkDate() {
        Log.d(TAG, "checkDate: called");
        Calendar current_date = Calendar.getInstance();
        CollectionReference ref = db.collection("date_check");
        Log.d(TAG, "checkDate: " + ref);
        ref.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                Log.d(TAG, "onComplete: complete");
                if (task.isSuccessful()) {
                    if (task.getResult().getDocuments().size() > 0) {
                        DocumentSnapshot document = task.getResult().getDocuments().get(0);
                        Timestamp date = (Timestamp) document.get("old_date");
                        old_date.setTime(date.toDate());
                        Integer this_day = current_date.get(Calendar.DAY_OF_MONTH);
                        Integer this_week = current_date.get(Calendar.WEEK_OF_YEAR);
                        Integer this_month = current_date.get(Calendar.MONTH);
                        Integer this_year = current_date.get(Calendar.YEAR);
                        Integer old_day = old_date.get(Calendar.DAY_OF_MONTH);
                        Integer old_week = old_date.get(Calendar.WEEK_OF_YEAR);
                        Integer old_month = old_date.get(Calendar.MONTH);
                        Integer old_year = old_date.get(Calendar.YEAR);
                        if (old_day - this_day < 0 || this_day - old_day < 0) {
                            coffee_display.setText("0");
                            DocumentReference doc_date = db.collection("date_check").document("old_date");
                            doc_date.update("old_date", current_date.getTime());
                            DocumentReference doc_tracker = db.collection("users").document(mAuth.getCurrentUser().getEmail());
                            doc_tracker.update("day", 0);
                        }
                        if (old_week - this_week < 0 || this_week - old_week < 0) {
                            DocumentReference doc_tracker = db.collection("users").document(mAuth.getCurrentUser().getEmail());
                            doc_tracker.update("week", 0);
                        }
                        if (old_month - this_month < 0 || this_month - old_month < 0) {
                            DocumentReference doc_tracker = db.collection("users").document(mAuth.getCurrentUser().getEmail());
                            doc_tracker.update("month", 0);
                        }
                        if (old_year - this_year < 0 || this_year - old_year < 0) {
                            DocumentReference doc_tracker = db.collection("users").document(mAuth.getCurrentUser().getEmail());
                            doc_tracker.update("year", 0);
                        }
                    } else {
                        Log.d(TAG, "Error getting documents: ", task.getException());
                    }
                }
            }
        });
    }

    private void updateDatabase () {
        DocumentReference docRef = db.collection("users").document(mAuth.getCurrentUser().getEmail());
        docRef.update("day", FieldValue.increment(1));
        docRef.update("week", FieldValue.increment(1));
        docRef.update("month", FieldValue.increment(1));
        docRef.update("year", FieldValue.increment(1));
    }

    private void setNumber() {
        DocumentReference docRef = db.collection("users").document(mAuth.getCurrentUser().getEmail());
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        Long coffee_number = (Long) document.get("day");
                        coffee_display.setText(coffee_number.toString());
                        Log.d(TAG, "DocumentSnapshot data: " + document.getData());
                    } else {
                        Log.d(TAG, "No such document");
                    }
                } else {
                    Log.d(TAG, "get failed with ", task.getException());
                }
            }
        });
    }

    private void openStats() {
        LayoutInflater inflater = LayoutInflater.from(this);
        final Dialog dialog3 = new Dialog(this);
        Window window = dialog3.getWindow();
        WindowManager.LayoutParams wlp = window.getAttributes();
        wlp.gravity = Gravity.CENTER;
        wlp.flags &= ~WindowManager.LayoutParams.FLAG_DIM_BEHIND;
        wlp.y = 50;
        window.setAttributes(wlp);
        View view3 = inflater.inflate(R.layout.stats_dialog, null);
        dialog3.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog3.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog3.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
        dialog3.setContentView(view3);
        dialog3.show();
        DocumentReference docRef = db.collection("users").document(mAuth.getCurrentUser().getEmail());
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        day_info = view3.findViewById(R.id.day);
                        week_info = view3.findViewById(R.id.week);
                        month_info = view3.findViewById(R.id.month);
                        year_info = view3.findViewById(R.id.year);
                        day_info.setText("Today you've drank " + document.get("day") + " coffes");
                        week_info.setText("This week you've drank " + document.get("week") + " coffees");
                        month_info.setText("This month you've drank " + document.get("month") + " coffees");
                        year_info.setText("This year you've drank " + document.get("year") + " coffees");
                        Log.d(TAG, "DocumentSnapshot data: " + document.getData());
                    } else {
                        Log.d(TAG, "No such document");
                    }
                } else {
                    Log.d(TAG, "get failed with ", task.getException());
                }
            }
        });
    }

    
}
