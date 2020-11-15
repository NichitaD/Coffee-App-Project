package com.myprojects.corso.adapters;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.myprojects.corso.R;
import com.myprojects.corso.model.Offer;

import java.lang.reflect.Array;
import java.util.ArrayList;

public class OfferRecyclerAdapter extends RecyclerView.Adapter<OfferRecyclerAdapter.ViewHolder> {

    private ArrayList<Offer> mOffers;

    public OfferRecyclerAdapter(ArrayList<Offer> offers) {
        mOffers = offers;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.offer_item_owner, parent, false);
        ViewHolder holder = new ViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.name.setText(mOffers.get(position).getCoffeeShop());
        holder.offer_content.setText(mOffers.get(position).getText());
    }

    @Override
    public int getItemCount() {
        return mOffers.size();
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
