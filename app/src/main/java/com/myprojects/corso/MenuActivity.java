package com.myprojects.corso;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;

public class MenuActivity extends Activity implements View.OnClickListener {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        findViewById(R.id.near_button).setOnClickListener(this);
        findViewById(R.id.all_button).setOnClickListener(this);
    }


    @Override
    public void onClick(View view) {

        int button = view.getId();

        if(button == R.id.near_button){
            Toast.makeText(MenuActivity.this,
                    "This feature is not available yet!",
                    Toast.LENGTH_SHORT).show();
        }
        if(button == R.id.all_button){
            Intent myIntent = new Intent(MenuActivity.this, MapsActivity.class);
           MenuActivity.this.startActivity(myIntent);
        }

    }
}
