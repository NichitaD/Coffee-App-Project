package com.myprojects.corso.views;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProviders;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.myprojects.corso.R;
import com.myprojects.corso.adapters.OfferViewAdapter;
import com.myprojects.corso.model.CoffeeShop;
import com.myprojects.corso.viewModels.ShopOwnerActivityViewModel;

public class ShopOwnerActivity extends AppCompatActivity implements View.OnClickListener{

    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private EditText offer_content;
    private String offer_to_post;
    private Dialog dialog;
    private OfferViewAdapter public_adapter;
    private TextView noOffersToShow;
    private ImageView logo;
    private ShopOwnerActivityViewModel mShopOwnerActivityViewModel;
    private CoffeeShop mCoffeeShop;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.owner_view);
        noOffersToShow = findViewById(R.id.no_offers_owner);
        logo = findViewById(R.id.shop_logo);
        mShopOwnerActivityViewModel = ViewModelProviders.of(this).get(ShopOwnerActivityViewModel.class);
        mShopOwnerActivityViewModel.init(mAuth.getCurrentUser().getEmail());
        mCoffeeShop = mShopOwnerActivityViewModel.getCoffeeShop().getValue();
        setData();
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
            mShopOwnerActivityViewModel.postOffer(offer_to_post);
            public_adapter.notifyDataSetChanged();
            noOffersToShow.setVisibility(View.INVISIBLE);
            dialog.dismiss();
        }

        if (button == R.id.see_reviews){
            Intent marker_intent = new Intent(ShopOwnerActivity.this, ReviewsActivity.class);
            marker_intent.putExtra("marker", mCoffeeShop.getName());
            ShopOwnerActivity.this.startActivity(marker_intent);
        }

        if (button == R.id.signOut){
            mAuth.signOut();
            Toast.makeText(ShopOwnerActivity.this,
                    "Signed out",
                    Toast.LENGTH_SHORT).show();
            Intent myIntent = new Intent(ShopOwnerActivity.this, LoginActivity.class);
            ShopOwnerActivity.this.startActivity(myIntent);
        }
    }




    private void setData() {
        Glide.with(getApplicationContext()).load(mCoffeeShop.getLogoUri()).into(logo);
        if(mCoffeeShop.getOffers().isEmpty()){
            noOffersToShow.setVisibility(View.VISIBLE);
        } else { noOffersToShow.setVisibility(View.INVISIBLE);}
        ListView listView = findViewById(R.id.offer_list);
        OfferViewAdapter adapter = new OfferViewAdapter(this, R.layout.offer_item_owner, mCoffeeShop.getOffers(), mCoffeeShop.getName());
        public_adapter = adapter;
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> a, View v, int position, long id) {
                AlertDialog.Builder adb = new AlertDialog.Builder(ShopOwnerActivity.this);
                adb.setTitle("Delete?");
                adb.setMessage("Are you sure you want to delete this ?");
                final int positionToRemove = position;
                adb.setNegativeButton("Cancel", null);
                adb.setPositiveButton("Ok", new AlertDialog.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        mShopOwnerActivityViewModel.deleteOffer(positionToRemove);
                        adapter.notifyDataSetChanged();
                    }
                });
                adb.show();
            }
        });
    }
}
