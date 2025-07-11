package com.example.whatsappimagepopper.app_fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.example.whatsappimagepopper.MainActivity;
import com.example.whatsappimagepopper.R;
import com.example.whatsappimagepopper.room_database.AppInfoDao;
import com.example.whatsappimagepopper.room_database.AppInfoTable;
import com.example.whatsappimagepopper.room_database.AppRoomDataBase;
import com.google.android.material.textfield.TextInputLayout;

import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttp;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class AddImageFragment extends Fragment {
    public View this_fragment;

    public AddImageFragment() {}  // Required empty constructor

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        this.this_fragment = inflater.inflate(R.layout.fragment_add_image, container, false);

        EditText img_name = ((TextInputLayout) this.this_fragment.findViewById(R.id.img_name)).getEditText();
        EditText img_url = ((TextInputLayout) this.this_fragment.findViewById(R.id.img_url)).getEditText();
        if (img_name == null || img_url == null){
            Log.d("SCIHACK", "Input fields not found!");
            return this.this_fragment;
        }
        img_url.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) { }

            @Override
            public void afterTextChanged(Editable s) {
                ImageView img_view = this_fragment.findViewById(R.id.img_view);
                Glide.with(this_fragment).load(s.toString()).placeholder(R.drawable.loading_img_icon).error(
                        Glide.with(this_fragment).load(R.drawable.no_image)
                ).centerCrop().into(img_view).onLoadFailed(ContextCompat.getDrawable(getContext(), R.drawable.no_image));
            }
        });

        this_fragment.findViewById(R.id.add_img_btn).setOnClickListener(this::onAddImage);

        return this.this_fragment;
    }

    public void onAddImage(View v) {
        EditText img_name = ((TextInputLayout) this.this_fragment.findViewById(R.id.img_name)).getEditText();
        EditText img_url = ((TextInputLayout) this.this_fragment.findViewById(R.id.img_url)).getEditText();
        if (img_name == null || img_url == null) {
            return;
        }
        String invalids = "!*'();:@&=+$,/?#[].~ ";
        String img_name_str = img_name.getText().toString();
        for (int i = 0; i < invalids.length(); i++){
            if (img_name_str.indexOf(invalids.charAt(i)) >= 0){
                MainActivity.createMsgBox(this_fragment, "Invalid image name : Only - OR _ are allowed special characters for image name.", "error");
                return;
            }
        }

        JSONObject to_send = new JSONObject();
        Thread td = new Thread(()->{
            AppInfoDao cursor_ = AppRoomDataBase.getInstance(getContext()).appInfoDao();
            AppInfoTable info_ = cursor_.selectFirstRow().get(0);
            try {
                to_send.put("username", info_.user_name);
                to_send.put("password", info_.password);
            }
            catch (Exception ignored) {}
        });
        try {
            td.start();
            td.join();
            to_send.put("image_name", img_name.getText());
            to_send.put("image_url", img_url.getText());
        }
        catch (Exception e){
            Log.d("SCIHACK", e.toString());
        }

        this.blockButtonAtUI(this_fragment.findViewById(R.id.add_img_btn));
        OkHttpClient cli =  new OkHttpClient();
        RequestBody to_resp = RequestBody.create(to_send.toString(), MediaType.get("text/plain"));
        Request req = new Request.Builder().url(getString(R.string.api_url)+"/api/put_img_link").post(to_resp).build();
        cli.newCall(req).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                call.cancel();
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                ResponseBody rb = response.body();
                if (rb == null){
                    return;
                }
                getActivity().runOnUiThread(()->{
                    unblockButtonAtUI(this_fragment.findViewById(R.id.add_img_btn));
                });

                String data_ = rb.string();
                if (data_.compareTo("OK") == 0){
                    getActivity().runOnUiThread(()->{
                        MainActivity.createMsgBox(this_fragment, "Image added successfully!", "success");
                    });
                }
                else if (data_.compareTo("FAILED") == 0){
                    getActivity().runOnUiThread(()->{
                        MainActivity.createMsgBox(this_fragment, "Failed to add image - Given image name already exists!", "error");
                    });
                }
                else if (data_.compareTo("OUT_OF_LIMIT") == 0) {
                    getActivity().runOnUiThread(()->{
                        MainActivity.createMsgBox(this_fragment, "Failed to add image - You have reached max image uploads on this account.", "error");
                    });
                }
            }
        });
    }

    public void blockButtonAtUI(Button b){
        getActivity().runOnUiThread(()->{
            b.setTag(b.getText());
            b.setText(getString(R.string.pls_wait));
            b.setEnabled(false);
        });
    }
    public void unblockButtonAtUI(Button b){
        getActivity().runOnUiThread(()->{
            b.setText(b.getTag().toString());
            b.setEnabled(true);
        });
    }
}