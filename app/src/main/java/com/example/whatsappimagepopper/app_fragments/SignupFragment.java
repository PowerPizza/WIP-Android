package com.example.whatsappimagepopper.app_fragments;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.room.Room;

import android.os.Handler;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.whatsappimagepopper.MainActivity;
import com.example.whatsappimagepopper.R;
import com.example.whatsappimagepopper.http_requests.HttpRequestMaker;
import com.example.whatsappimagepopper.room_database.AppInfoDao;
import com.example.whatsappimagepopper.room_database.AppInfoTable;
import com.example.whatsappimagepopper.room_database.AppRoomDataBase;

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
import okio.BufferedSink;


public class SignupFragment extends Fragment {
    AppRoomDataBase app_info_db;
    View this_fragment;

    public SignupFragment() {}  // required empty constructor.

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        this.this_fragment = inflater.inflate(R.layout.fragment_signup, container, false);
        this.app_info_db = AppRoomDataBase.getInstance(getActivity().getApplicationContext());

        EditText entry_uname = this.this_fragment.findViewById(R.id.uname_entry_sup);
        EditText entry_pass = this.this_fragment.findViewById(R.id.pass_entry_sup);
        EditText entry_ep = this.this_fragment.findViewById(R.id.ep_entry_sup);

        TextView shift_signup_opt = this.this_fragment.findViewById(R.id.shift_login);
        shift_signup_opt.setOnClickListener((View v)->{
            getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.fragmentHolder, new LoginFragment()).commit();
        });

        Button signup_ = this.this_fragment.findViewById(R.id.btn_signup);
        signup_.setOnClickListener((View v)->{
            String uname = entry_uname.getText().toString();
            String password = entry_pass.getText().toString();
            String end_point = entry_ep.getText().toString().toLowerCase();

            if (uname.isEmpty() || password.isEmpty() || end_point.isEmpty()){
                MainActivity.createMsgBox(this_fragment, "All fields are required. Please fill in every entry.", "warning");
                return;
            }
            if (password.length() < 5){
                MainActivity.createMsgBox(this_fragment, "Your password is too short. It must be more than 5 characters long.", "warning");
                return;
            }
            String special_chars = "!*'();:@&=+$,/?#[].~ ";
            for (int i = 0; i < special_chars.length(); i++){
                if (end_point.indexOf(special_chars.charAt(i)) >= 0){
                    MainActivity.createMsgBox(this_fragment, "Only - OR _ are allowed special characters for end point, please remove all other special characters.", "warning");
                    return;
                }
            }

            MainActivity.blockButtonAtUI(getActivity(), signup_);

            new HttpRequestMaker(getString(R.string.api_url)+"/signup/register") {
                @Override
                public void onResp(String data) {
                    if (data.compareTo("OK") == 0){
                        Thread data_writer = new Thread(()->{
                            AppInfoDao app_info_cursor = app_info_db.appInfoDao();
                            app_info_cursor.clearTable();
                            AppInfoTable row1 = new AppInfoTable();
                            row1.user_name = uname;
                            row1.password = password;
                            row1.endpoint_name = end_point;
                            long db_resp = app_info_cursor.insertOne(row1);
                            if (db_resp == -1){
                                getActivity().runOnUiThread(()->{
                                    MainActivity.createMsgBox(this_fragment, "Account registered but failed to auto-login. Please try logging in manually.", "error");
                                });
                                return;
                            }
                            getActivity().runOnUiThread(()->{
                                ((MainActivity) getActivity()).changeBottomNavMenu(R.menu.with_login_navbar);
                                MainActivity.setAppFragment(getActivity(), new HomePage());
                            });
                        });
                        data_writer.start();
                    }
                    else if (data.compareTo("USERNAME_ALREADY_EXISTS") == 0){
                        getActivity().runOnUiThread(()->{
                            MainActivity.createMsgBox(this_fragment, "Username already exists. Please choose a different one.", "error");
                        });
                    }
                    else if (data.compareTo("ENDPOINT_ALREADY_EXISTS") == 0){
                        getActivity().runOnUiThread(()->{
                            MainActivity.createMsgBox(this_fragment, "This endpoint is already taken. Please choose another endpoint.", "error");
                        });
                    }
                    else{
                        getActivity().runOnUiThread(()->{
                            MainActivity.createMsgBox(this_fragment, "Unexpected error while signing up!", "error");
                        });
                    }
                }

                @Override
                public void onFail(IOException err) {
                    Toast.makeText(getContext(), "Error : "+err.toString(), Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onCompletion() {
                    MainActivity.unblockButtonAtUI(getActivity(), signup_);
                }
            }.postText(String.format(Locale.ENGLISH, "{\"username\": \"%s\", \"password\": \"%s\", \"endpoint\": \"%s\"}", uname, password, end_point));
        });
        return this.this_fragment;
    }
}