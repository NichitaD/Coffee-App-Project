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
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class MenuActivity extends Activity implements View.OnClickListener {

    private Long monday, tuesday, wednesday, thursday, friday, sunday, saturday;
    private BarChart chart;
    private ArrayList<BarEntry> BARENTRY;
    private ArrayList<String> BarEntryLabels;
    private BarDataSet Bardataset;
    private BarData BARDATA;
    private FirebaseAuth mAuth;
    private Calendar old_date = Calendar.getInstance();
    private String TAG = "Menu_Activity";
    private TextView coffee_display;
    private Integer coffees_drank_today;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private Date last_access_date;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
        checkDate();
        setContentView(R.layout.activity_menu);
        coffee_display = findViewById(R.id.coffee_display);
        setNumber();
        findViewById(R.id.near_button).setOnClickListener(this);
        findViewById(R.id.all_button).setOnClickListener(this);
        findViewById(R.id.signOut).setOnClickListener(this);
        findViewById(R.id.plus).setOnClickListener(this);
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
        if (button == R.id.plus) {
            incrementDatabase();
            setNumber();
        }
        if (button == R.id.minus) {
            decrementDatabase();
            setNumber();
        }
        if (button == R.id.stats) {
            openStats();
        }
        if (button == R.id.see_offers){
            Intent intent = new Intent(MenuActivity.this, OffersActivity.class);
            MenuActivity.this.startActivity(intent);
        }
        if (button == R.id.search){
            ArrayList<String> arraylist = new ArrayList<>();
            CollectionReference ref = db.collection("coffee_shops");
            ref.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    if(task.isSuccessful()){
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            arraylist.add((String)document.get("name"));
                        }
                        Intent intent = new Intent (MenuActivity.this, SearchActivity.class);
                        intent.putExtra("list",arraylist);
                        MenuActivity.this.startActivity(intent);
                    } else {
                        Log.d("Database_info", "Error getting documents: ", task.getException());
                    }
                }
            });
        }
    }

    private void checkDate() {
        Log.d(TAG, "checkDate: called");
        last_access_date = LoginActivity.getDate();
        Log.d(TAG, "checkDate: last date is " + last_access_date);
        Calendar current_date = Calendar.getInstance();
        DocumentReference docRef = db.collection("users").document(mAuth.getCurrentUser().getEmail());
        docRef.get().addOnCompleteListener(task -> {
            Log.d(TAG, "onComplete: complete");
            if (task.isSuccessful()) {
                    old_date.setTime(last_access_date);
                    Integer this_day = current_date.get(Calendar.DAY_OF_MONTH);
                    Integer old_day = old_date.get(Calendar.DAY_OF_MONTH);
                    Integer this_week = current_date.get(Calendar.WEEK_OF_YEAR);
                    Integer old_week = old_date.get(Calendar.WEEK_OF_YEAR);
             if (old_day - this_day < 0 || this_day - old_day < 0) {
                        coffee_display.setText("0");
                        DocumentReference doc_date = db.collection("users").document(mAuth.getCurrentUser().getEmail());
                        doc_date.update("last_access_date", current_date.getTime());
                        DocumentReference doc_tracker = db.collection("users").document(mAuth.getCurrentUser().getEmail());
                        doc_tracker.update("today", 0);
                    }
             if(old_week - this_week < 0 ||this_week- old_week < 0) {
                       DocumentReference doc_tracker = db.collection("users").document(mAuth.getCurrentUser().getEmail());
                        doc_tracker.update("Monday", 0);
                        doc_tracker.update("Tuesday", 0);
                        doc_tracker.update("Wednesday", 0);
                        doc_tracker.update("Thursday", 0);
                        doc_tracker.update("Friday", 0);
                        doc_tracker.update("Saturday", 0);
                        doc_tracker.update("Sunday", 0);
             }
             updateDatabase();
                } else {
                    Log.d(TAG, "Error getting documents: ", task.getException());
                }
        });
    }

    private  void incrementDatabase(){
        DocumentReference docRef = db.collection("users").document(mAuth.getCurrentUser().getEmail());
        docRef.update("today", FieldValue.increment(1));
    }
    private void decrementDatabase(){
        DocumentReference docRef = db.collection("users").document(mAuth.getCurrentUser().getEmail());
        docRef.update("today", FieldValue.increment(-1));
    }

    private void updateDatabase(){
        Calendar current_date = Calendar.getInstance();
        Calendar last_acces =  Calendar.getInstance();
        String day_in_week = findDay(current_date.get(Calendar.DAY_OF_WEEK));
        Log.d("access_date", "got in updateDatabase");
        if(last_access_date == null) checkDate();
        last_acces.setTime(last_access_date);
        Integer this_day = current_date.get(Calendar.DAY_OF_YEAR);
        Integer old_day = last_acces.get(Calendar.DAY_OF_YEAR);
        Integer day_diffrence = this_day - old_day;
        DocumentReference docRef = db.collection("users").document(mAuth.getCurrentUser().getEmail());
        docRef.update(day_in_week,coffees_drank_today);
        Integer day_for_change;
        this_day = current_date.get(Calendar.DAY_OF_WEEK);
        Log.d("saturday", "day dif is : "+ day_diffrence);
        for(day_for_change = this_day-1; day_diffrence > 1; --day_diffrence){
            if(day_for_change == 0) day_for_change = 7;
           docRef.update(findDay(day_for_change),0);
            --day_for_change;
        }
    }

    private String findDay (Integer day){
        String day_name = new String();
        if(day == 2)day_name = "Monday";
        else if(day == 3)day_name = "Tuesday";
        else if(day == 4)day_name = "Wednesday";
        else if(day == 5)day_name = "Thursday";
        else if(day == 6)day_name = "Friday";
        else if(day == 7)day_name = "Saturday";
        else if(day == 1)day_name = "Sunday";
        Log.d("Find_day", day_name + " " + day );
        return day_name;
    }

    private void setNumber() {
        DocumentReference docRef = db.collection("users").document(mAuth.getCurrentUser().getEmail());
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        Long coffee_number = (Long)document.get("today");
                        coffees_drank_today = coffee_number.intValue();
                        coffee_display.setText(coffee_number.toString());
                        monday = (Long) document.get("Monday");
                        thursday = (Long) document.get("Thursday");
                        wednesday = (Long) document.get("Wednesday");
                        tuesday = (Long) document.get("Tuesday");
                        friday = (Long) document.get("Friday");
                        saturday = (Long) document.get("Saturday");
                        sunday = (Long) document.get("Sunday");
                        updateDatabase();
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
        chart = (BarChart) dialog3.findViewById(R.id.chart1);
        BARENTRY = new ArrayList<>();
        BarEntryLabels = new ArrayList<String>();
        AddValuesToBARENTRY();
        AddValuesToBarEntryLabels();
        Bardataset = new BarDataSet(BARENTRY, "Coffees / Day");
        BARDATA = new BarData(BarEntryLabels, Bardataset);
        Bardataset.setColor(R.color.redish);
        chart.setData(BARDATA);
        chart.getBarData().setGroupSpace(10);
        chart.animateY(3000);
        chart.setDrawGridBackground(false);
        chart.getAxisLeft().setDrawGridLines(false);
        chart.getXAxis().setDrawGridLines(false);
        chart.setDescription("");
        chart.setDragEnabled(true);
        chart.setVisibleXRange(1, 4);
        dialog3.show();
    }

    public void AddValuesToBARENTRY(){
        BARENTRY.add(new BarEntry(monday, 0));
        BARENTRY.add(new BarEntry(tuesday, 1));
        BARENTRY.add(new BarEntry(wednesday, 2));
        BARENTRY.add(new BarEntry(thursday, 3));
        BARENTRY.add(new BarEntry(friday, 4));
        BARENTRY.add(new BarEntry(saturday, 5));
        BARENTRY.add(new BarEntry(sunday, 6));
    }

    private void AddValuesToBarEntryLabels() {
        BarEntryLabels.add("Monday");
        BarEntryLabels.add("Tuesday");
        BarEntryLabels.add("Wednesday");
        BarEntryLabels.add("Thursday");
        BarEntryLabels.add("Friday");
        BarEntryLabels.add("Saturday");
        BarEntryLabels.add("Sunday");
    }
}