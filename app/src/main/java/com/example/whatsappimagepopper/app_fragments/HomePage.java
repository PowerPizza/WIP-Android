package com.example.whatsappimagepopper.app_fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.whatsappimagepopper.CustomArrayAdapter;
import com.example.whatsappimagepopper.ImageCardEle;
import com.example.whatsappimagepopper.R;
import com.example.whatsappimagepopper.http_requests.HttpRequestMaker;
import com.google.android.flexbox.FlexboxLayout;

import org.json.JSONObject;

import java.io.IOException;
import java.util.Iterator;
import java.util.Locale;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class HomePage extends Fragment {
    public View this_fragment;

    public Spinner search_by_menu;
    public FlexboxLayout card_holder;
    public TextView page_status_txt;
    public int current_page = 1;
    public int total_page = 0;

    public HomePage() { }  // Required empty public constructor

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        this.this_fragment = inflater.inflate(R.layout.fragment_home_page, container, false);

        search_by_menu = this_fragment.findViewById(R.id.search_by_menu);
        CustomArrayAdapter a1 = new CustomArrayAdapter(getContext(), R.layout.dropdown_item_ele);
        a1.addAll(getResources().getStringArray(R.array.search_by_choices));
        search_by_menu.setAdapter(a1);

        this.card_holder = this_fragment.findViewById(R.id.image_cards_holder);
        page_status_txt = this_fragment.findViewById(R.id.pages_status);

        this.loadImagesByPage(this.current_page);

        ImageView next_btn = this_fragment.findViewById(R.id.page_right);
        next_btn.setOnClickListener((View v)->{
            if (this.current_page+1 <= this.total_page){
                this.current_page++;
                this.page_status_txt.setText(String.format(Locale.ENGLISH, "%d/%d", current_page, total_page));
                this.loadImagesByPage(this.current_page);
            }
        });

        ImageView prev_btn = this_fragment.findViewById(R.id.page_left);
        prev_btn.setOnClickListener((View v)->{
            if (this.current_page-1 > 0){
                this.current_page--;
                this.page_status_txt.setText(String.format(Locale.ENGLISH, "%d/%d", current_page, total_page));
                this.loadImagesByPage(this.current_page);
            }
        });

        return this_fragment;
    }

    public void setTotal_page(int total_page) {
        this.total_page = total_page;
    }

    void loadImagesByPage(int page) {
        card_holder.removeAllViews();
        this.toggleLoadingCircle(true);

        new HttpRequestMaker(getString(R.string.api_url) + "/get_img_links") {
            @Override
            public void onResp(String data) {
                try {
                    JSONObject data_ = new JSONObject(data);
                    JSONObject images_data = data_.getJSONArray("doc").getJSONObject(0);
                    setTotal_page(data_.getInt("total_docs")-1);
                    getActivity().runOnUiThread(()->{
                        toggleLoadingCircle(false);
                        card_holder.removeAllViews();
                    });

                    Iterator<String> it = images_data.keys();
                    while (it.hasNext()){
                        String image_name = it.next();
                        if (image_name.equals("endpoint")){
                            continue;
                        }
                        getActivity().runOnUiThread(()->{
                            try {
                                page_status_txt.setText(String.format(Locale.ENGLISH, "%d/%d", current_page, total_page));
                                ImageCardEle img_card = new ImageCardEle(getContext(), images_data.getString(image_name), image_name, images_data.getString("endpoint"));
                                card_holder.addView(img_card);
                            }
                            catch (Exception e){
                                Log.d("SCIHACK", "SUPER ERROR : "+e.toString());
                            }
                        });
                    }
                }
                catch (Exception e){
                    Log.d("SCIHACK", "failed to load json :: "+e);
                }
            }

            @Override
            public void onFail(IOException err) {
                Toast.makeText(getContext(), "Error : "+err.toString(), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCompletion() { }
        }.postText(String.format(Locale.ENGLISH, "{\"doc_no\": %d}", page));
    }

    public void toggleLoadingCircle(boolean show){
        View loading_cir = this.this_fragment.findViewById(R.id.loading_circle);
        loading_cir.setVisibility(show ? View.VISIBLE : View.GONE);
    }
}