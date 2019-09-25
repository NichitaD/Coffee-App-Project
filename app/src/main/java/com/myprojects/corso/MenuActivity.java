package com.myprojects.corso;

import android.app.Activity;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MenuActivity extends Activity implements View.OnClickListener {
    private FirebaseAuth mAuth;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
        setContentView(R.layout.activity_menu);
        findViewById(R.id.near_button).setOnClickListener(this);
        findViewById(R.id.all_button).setOnClickListener(this);
        findViewById(R.id.signOut).setOnClickListener(this);
    }
    @Override
    public void onClick(View view) {
        int button = view.getId();
        if(button == R.id.near_button){
            Intent myIntent = new Intent(MenuActivity.this, MapsActivity.class);
            myIntent.putExtra("option", 1);
            MenuActivity.this.startActivity(myIntent);
        }
        if(button == R.id.all_button){
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
    }
}
