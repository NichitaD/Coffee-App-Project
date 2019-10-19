package com.myprojects.corso;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class CoffeeShopActivity extends AppCompatActivity implements View.OnClickListener{

    private static final String TAG = "CoffeeShopActivity";
    private ArrayList<String> mOffers = new ArrayList<>();
    private String coffee_name;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private EditText offer_content;
    private String offer_to_post;
    private  Dialog dialog;
    private OfferViewAdapter public_adapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.coffee_view);
        getName();
    }

    @Override
    public void onClick(View view) {
        int button = view.getId();
        if(button == R.id.add_offer){
            LayoutInflater inflater = LayoutInflater.from(this);
            final Dialog dialog3 = new Dialog(this);
            Window window = dialog3.getWindow();
            WindowManager.LayoutParams wlp = window.getAttributes();
            wlp.gravity = Gravity.CENTER;
            wlp.flags &= ~WindowManager.LayoutParams.FLAG_DIM_BEHIND;
            wlp.y = 50;
            window.setAttributes(wlp);
            View view3 = inflater.inflate(R.layout.create_offer_dialog, null);
            dialog3.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog3.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialog3.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
            dialog3.setContentView(view3);
            dialog3.show();
            dialog = dialog3;
            offer_content = dialog3.findViewById(R.id.content);
        }
        if (button == R.id.post_offer){
            offer_to_post = offer_content.getText().toString();
            addOffer(offer_to_post);
            dialog.dismiss();
        }
        if (button == R.id.see_reviews){
            Intent marker_intent = new Intent(CoffeeShopActivity.this, ReviewsActivity.class);
            marker_intent.putExtra("marker", coffee_name);
            CoffeeShopActivity.this.startActivity(marker_intent);
        }
        if (button == R.id.signOut){
            mAuth.signOut();
            Toast.makeText(CoffeeShopActivity.this,
                    "Signed out",
                    Toast.LENGTH_SHORT).show();
            Intent myIntent = new Intent(CoffeeShopActivity.this, LoginActivity.class);
            CoffeeShopActivity.this.startActivity(myIntent);
        }
    }

    private void getName() {
        DocumentReference docRef = db.collection("users").document(mAuth.getCurrentUser().getEmail());
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        coffee_name =(String) document.get("name");
                        mOffers = (ArrayList<String>) document.get("offers");
                        getInfo();
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

    private void getInfo() {
        String coffee_shop_id = coffee_name.toLowerCase().replaceAll("[^A-Za-z0-9']", "_");
        DocumentReference docRef = db.collection("coffee_shops").document(coffee_shop_id);
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        mOffers = (ArrayList<String>) document.get("offers");
                        setData();
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

    private void setData() {
        ListView listView = findViewById(R.id.offer_list);
        OfferViewAdapter adapter = new OfferViewAdapter(this, R.layout.offer_list_item, mOffers, coffee_name);
        public_adapter = adapter;
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> a, View v, int position, long id) {
                AlertDialog.Builder adb=new AlertDialog.Builder(CoffeeShopActivity.this);
                adb.setTitle("Delete?");
                adb.setMessage("Are you sure you want to delete this ?");
                final int positionToRemove = position;
                adb.setNegativeButton("Cancel", null);
                adb.setPositiveButton("Ok", new AlertDialog.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        final Map<String, Object> removeOfferFromArrayMap = new HashMap<>();
                        removeOfferFromArrayMap.put("offers", FieldValue.arrayRemove(mOffers.get(positionToRemove)));
                        db.collection("coffee_shops").document(coffee_name.toLowerCase().
                                replaceAll("[^A-Za-z0-9']", "_")).update(removeOfferFromArrayMap);
                        findMapToRemove(mOffers.get(positionToRemove));
                        mOffers.remove(positionToRemove);
                        adapter.notifyDataSetChanged();
                    }});
                adb.show();
            }
        });
    }

    private void addOffer (String offer){
        final Map<String, Object> addOfferToArrayMap = new HashMap<>();
        addOfferToArrayMap.put("offers", FieldValue.arrayUnion(offer));
        db.collection("coffee_shops").document(coffee_name.toLowerCase().
                replaceAll("[^A-Za-z0-9']", "_")).update(addOfferToArrayMap);
        DocumentReference doc_ref = db.collection("offers").document("offers");
        doc_ref.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        Long iterator =(Long) document.get("iterator");
                        Map <String, String> map1 = new HashMap<>();
                        map1.put(coffee_name,offer);
                        Map <String, Map> map2 = new HashMap<>();
                        map2.put("offer"+iterator, map1);
                        Map <String, Object> addOffer = new HashMap<>();
                        addOffer.put("offers",FieldValue.arrayUnion(map2));
                        addOffer.put("iterator", FieldValue.increment(1));
                        db.collection("offers").document("offers").update(addOffer);
                        mOffers.add(offer);
                        public_adapter.notifyDataSetChanged();
                    } else {
                        Log.d(TAG, "No such document");
                    }
                } else {
                    Log.d(TAG, "get failed with ", task.getException());
                }
            }
        });
    }

    private void findMapToRemove(String offer_to_remove){
        Log.d("Remover", "findMapToRemove: called");
        DocumentReference doc_ref = db.collection("offers").document("offers");
        doc_ref.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()){
                        ArrayList<Map> offer_array = (ArrayList<Map>) document.get("offers");
                        for(Map <String, Map<String,String>> map: offer_array){
                            Log.d("Remover", "onComplete: started going through array" + map);
                            for (Map.Entry<String, Map<String, String>> entry : map.entrySet()){
                                Log.d("Remover", "onComplete: opened map" + entry);
                                for (Map.Entry<String, String> second_entry : entry.getValue().entrySet()) {
                                    Log.d("Remover", "onComplete: final open : " + second_entry + " compare to : " + coffee_name + " = " + offer_to_remove);
                                    if (coffee_name.equals(second_entry.getKey()) && second_entry.getValue().equals(offer_to_remove)){
                                        Log.d("Remover", "onComplete: found offer to delete");
                                        Map<String, Object> removeMe = new HashMap<>();
                                        removeMe.put("offers", FieldValue.arrayRemove(map));
                                        db.collection("offers").document("offers").update(removeMe);
                                        return;
                                    }
                                }
                            }
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
}
