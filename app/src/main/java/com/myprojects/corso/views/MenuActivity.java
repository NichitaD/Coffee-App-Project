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
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.google.firebase.auth.FirebaseAuth;
import com.myprojects.corso.R;
import com.myprojects.corso.model.User;
import com.myprojects.corso.viewModels.MenuActivityViewModel;

import java.util.ArrayList;
import java.util.HashMap;

public class MenuActivity extends AppCompatActivity implements View.OnClickListener {

    private BarChart chart;
    private ArrayList<BarEntry> BARENTRY;
    private ArrayList<String> BarEntryLabels;
    private BarDataSet Bardataset;
    private BarData BARDATA;
    private FirebaseAuth mAuth;
    private TextView coffee_display;
    private MenuActivityViewModel mMenuActivityViewModel;
    private User mUser;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
        mMenuActivityViewModel = ViewModelProviders.of(this).get(MenuActivityViewModel.class);
        mMenuActivityViewModel.init();
        mMenuActivityViewModel.getUser().observe(this, new Observer<User>() {
            @Override
            public void onChanged(User user) {
                if (user.getEmail() != null){
                    coffee_display.setText(String.valueOf(user.getTodaysCoffees()));
                    updateUser(user.getUser());
                    mMenuActivityViewModel.checkDate();
                }
            }
        });

        setContentView(R.layout.activity_menu);
        coffee_display = findViewById(R.id.coffee_display);
        findViewById(R.id.near_button).setOnClickListener(this);
        findViewById(R.id.all_button).setOnClickListener(this);
        findViewById(R.id.signOut).setOnClickListener(this);
        findViewById(R.id.plus).setOnClickListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mMenuActivityViewModel.checkDate();
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
            mMenuActivityViewModel.incrementCount();
        }
        if (button == R.id.minus) {
            mMenuActivityViewModel.decrementCount();
        }

        if (button == R.id.stats) {
            openStats();
        }
        if (button == R.id.see_offers){
            Intent intent = new Intent(MenuActivity.this, OffersActivity.class);
            MenuActivity.this.startActivity(intent);
        }
        if (button == R.id.search){
            Intent intent = new Intent (MenuActivity.this, SearchActivity.class);
            intent.putExtra("list",mMenuActivityViewModel.getCoffeeShopNames());
            MenuActivity.this.startActivity(intent);
        }
    }

    private void updateUser(User user) {
        this.mUser = user;
    }

    // Stats section

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
        chart = dialog3.findViewById(R.id.chart1);
        BARENTRY = new ArrayList<>();
        BarEntryLabels = new ArrayList<>();
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
        HashMap<String, Integer> weekleyCoffees = mUser.getWeekleyCoffees();
        BARENTRY.add(new BarEntry(weekleyCoffees.get("Monday"), 0));
        BARENTRY.add(new BarEntry(weekleyCoffees.get("Tuesday"), 1));
        BARENTRY.add(new BarEntry(weekleyCoffees.get("Wednesday"), 2));
        BARENTRY.add(new BarEntry(weekleyCoffees.get("Thursday"), 3));
        BARENTRY.add(new BarEntry(weekleyCoffees.get("Friday"), 4));
        BARENTRY.add(new BarEntry(weekleyCoffees.get("Saturday"), 5));
        BARENTRY.add(new BarEntry(weekleyCoffees.get("Sunday"), 6));
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