package com.example.whatsappimagepopper.app_fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.provider.MediaStore;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.example.whatsappimagepopper.MainActivity;
import com.example.whatsappimagepopper.R;
import com.example.whatsappimagepopper.room_database.AppInfoDao;
import com.example.whatsappimagepopper.room_database.AppInfoTable;
import com.example.whatsappimagepopper.room_database.AppRoomDataBase;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import org.json.JSONObject;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;


public class ProfileLoggedInFragment extends Fragment {
    AppRoomDataBase app_info_db;
    AppInfoDao app_info_cursor_;

    public ProfileLoggedInFragment() {}  // its empty but its required.

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.app_info_db = AppRoomDataBase.getInstance(getContext());
        this.app_info_cursor_ = app_info_db.appInfoDao();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View this_fragment = inflater.inflate(R.layout.fragment_profile_logged_in, container, false);

        TextInputLayout plif_uname_layout = this_fragment.findViewById(R.id.plif_uname_layout);
        TextInputLayout plif_pass_layout = this_fragment.findViewById(R.id.plif_pass_layout);
        TextInputLayout plif_ep_layout = this_fragment.findViewById(R.id.plif_ep_layout);

        EditText plif_uname = this_fragment.findViewById(R.id.plif_username);
        EditText plif_pass = this_fragment.findViewById(R.id.plif_password);
        EditText plif_ep = this_fragment.findViewById(R.id.plif_endpoint);

        Thread td1 = new Thread(()->{
            AppInfoTable login_data = this.app_info_cursor_.selectFirstRow().get(0);
            getActivity().runOnUiThread(()->{
                plif_uname.setText(login_data.user_name);
                plif_pass.setText(login_data.password);
                plif_ep.setText(login_data.endpoint_name);
            });
        });
        td1.start();

        Button logout_ = this_fragment.findViewById(R.id.plif_logout_btn);
        logout_.setOnClickListener((View v)->{
            ((MainActivity) getActivity()).logout();
        });

        Button edit_ = this_fragment.findViewById(R.id.plif_edit_btn);
        Button save_ = this_fragment.findViewById(R.id.plif_save_btn);

        edit_.setOnClickListener((View v)->{
            plif_uname_layout.setEnabled(true);
            plif_pass_layout.setEnabled(true);
            plif_ep_layout.setEnabled(true);
            edit_.setVisibility(View.GONE);
            save_.setVisibility(View.VISIBLE);
        });

        save_.setOnClickListener((View v)->{
            JSONObject info_to_send = new JSONObject();
            JSONObject updated_info = new JSONObject();

            Thread td_info_maker = new Thread(()->{
                try {
                    updated_info.put("username", plif_uname.getText());
                    updated_info.put("password", plif_pass.getText());
                    updated_info.put("endpoint", plif_ep.getText());
                    info_to_send.put("updated_info", updated_info);

                    AppInfoTable cur_login_creds = this.app_info_cursor_.selectFirstRow().get(0);
                    info_to_send.put("username", cur_login_creds.user_name);
                    info_to_send.put("password", cur_login_creds.password);
                }
                catch (Exception ignored){}
            });

            try {
                td_info_maker.start();
                td_info_maker.join();
            }
            catch (Exception e){
                Log.d("SCIHACK", e.toString());
            }

            blockButtonAtUI(save_);
            OkHttpClient cli = new OkHttpClient();
            RequestBody to_send = RequestBody.create(info_to_send.toString(), MediaType.get("text/plain"));
            Request req = new Request.Builder().url(getString(R.string.api_url) +  "/update_acc").post(to_send).build();
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
                    String resp = rb.string();
                    if (resp.compareTo("OK") == 0){
                        app_info_cursor_.clearTable();
                        AppInfoTable new_data = new AppInfoTable();
                        try {
                            new_data.user_name = updated_info.getString("username");
                            new_data.password = updated_info.getString("password");
                            new_data.endpoint_name = updated_info.getString("endpoint");
                        }
                        catch (Exception ignored) {}
                        app_info_cursor_.insertOne(new_data);
                        getActivity().runOnUiThread(()->{
                            save_.setVisibility(View.GONE);
                            edit_.setVisibility(View.VISIBLE);
                            plif_uname_layout.setEnabled(false);
                            plif_pass_layout.setEnabled(false);
                            plif_ep_layout.setEnabled(false);
                        });
                    }
                    unblockButtonAtUI(save_);
                }
            });
        });

        Button delete_ = this_fragment.findViewById(R.id.plif_delete_btn);
        delete_.setOnClickListener((View v)->{
            Thread td = new Thread(()->{
                blockButtonAtUI(delete_);
                AppInfoTable data_ = app_info_cursor_.selectFirstRow().get(0);
                OkHttpClient cli = new OkHttpClient();
                RequestBody to_send = RequestBody.create(String.format(Locale.ENGLISH, "{\"username\": \"%s\", \"password\": \"%s\"}", data_.user_name, data_.password), MediaType.get("text/plain"));
                Request req = new Request.Builder().url(getString(R.string.api_url)+"/delete_acc").post(to_send).build();
                cli.newCall(req).enqueue(new Callback() {
                    @Override
                    public void onFailure(@NonNull Call call, @NonNull IOException e) {
                        Log.d("SCIHACK", "FAILED!");
                        call.cancel();
                    }

                    @Override
                    public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                        unblockButtonAtUI(delete_);
                        ResponseBody resp_body = response.body();
                        if (resp_body == null){
                            Log.d("SCIHACK", "NO RESPONSE");
                            return;
                        }
                        String resp_ = resp_body.string();
                        if (resp_.compareTo("OK") == 0){
                            getActivity().runOnUiThread(()->{
                                ((MainActivity) getActivity()).logout();
                            });
                        }
                    }
                });
            });
            td.start();
        });

        return this_fragment;
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