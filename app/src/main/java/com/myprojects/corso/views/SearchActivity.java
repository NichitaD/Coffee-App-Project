package com.myprojects.corso.views;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SearchView;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.myprojects.corso.R;
import com.myprojects.corso.adapters.ListViewAdapter;

import java.util.ArrayList;

public class SearchActivity extends AppCompatActivity implements SearchView.OnQueryTextListener {

    ListView list;
    ListViewAdapter adapter;
    SearchView editSearch;
    ArrayList<String> arrayList = new ArrayList<String>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        arrayList = intent.getStringArrayListExtra("list");
        setContentView(R.layout.search_view);
        list =  findViewById(R.id.listview);
        adapter = new ListViewAdapter(this, arrayList);
        list.setAdapter(adapter);
        editSearch = findViewById(R.id.search);
        editSearch.setOnQueryTextListener(this);
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                AlertDialog.Builder adb = new AlertDialog.Builder(SearchActivity.this);
                adb.setMessage("Open " + arrayList.get(position) + "'s page?");
                adb.setNegativeButton("No", null);
                adb.setPositiveButton("Yes",  new AlertDialog.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Intent intent = new Intent (SearchActivity.this, CoffeeShopActivity.class);
                        intent.putExtra("name", arrayList.get(position));
                        SearchActivity.this.startActivity(intent);
                    }
                });
                adb.show();
            }
        });
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }
    @Override
    public boolean onQueryTextChange(String newText) {
        String text = newText;
        adapter.filter(text);
        return false;
    }
}