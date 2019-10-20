package com.myprojects.corso;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;


public class OfferRecyclerAdapter extends RecyclerView.Adapter<OfferRecyclerAdapter.ViewHolder> {

    private static final String TAG = "OfferRecycleViewAdapter";
    private ArrayList<String> mNames = new ArrayList<>();
    private ArrayList<String> mOffers = new ArrayList<>();

    public OfferRecyclerAdapter(ArrayList<String> names, ArrayList<String> offers) {
        Log.d(TAG, "constructor called");
        mNames = names;
        mOffers = offers;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Log.d(TAG, "view_holde_called");
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.offer_item_owner, parent, false);
        ViewHolder holder = new ViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.name.setText(mNames.get(position));
        holder.offer_content.setText(mOffers.get(position));
    }


    @Override
    public int getItemCount() {
        return mNames.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView name;
        TextView offer_content;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.offer_shop_name);
            offer_content = itemView.findViewById(R.id.offer_content);
        }
    }
}
