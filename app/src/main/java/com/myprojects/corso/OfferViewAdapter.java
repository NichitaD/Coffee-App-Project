package com.myprojects.corso;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

public class OfferViewAdapter  extends ArrayAdapter<String> {

    Context mContext;
    int mResource;
    String shop_name;

    public OfferViewAdapter(@NonNull Context context, int resource, @NonNull ArrayList<String> objects, String name) {
        super(context, resource, objects);
        mContext = context;
        shop_name = name;
        mResource = resource;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        String name = shop_name;
        String offer = getItem(position);
        LayoutInflater inflater = LayoutInflater.from(mContext);
        convertView = inflater.inflate(mResource,parent, false);
        TextView title = convertView.findViewById(R.id.offer_shop_name);
        TextView content = convertView.findViewById(R.id.offer_content);
        title.setText(name);
        content.setText(offer);
        return convertView;
    }
}
