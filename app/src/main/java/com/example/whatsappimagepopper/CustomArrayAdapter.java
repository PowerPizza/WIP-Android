package com.example.whatsappimagepopper;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class CustomArrayAdapter extends ArrayAdapter<String> {
    public CustomArrayAdapter(Context context, int resource){
        super(context, resource);
    }

    @SuppressLint("ResourceAsColor")
    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        TextView v = (TextView) super.getView(position, convertView, parent);
        if (position == 0){
            v.setText("");
            v.setHint("Search By");
            v.setEnabled(false);
            v.setAlpha(0.8f);
        }
        v.setTextSize(15);
        v.setPadding(0, 0, 20, 0);
        Log.d("SCIHACK", String.valueOf(position));
        return v;
    }
}
