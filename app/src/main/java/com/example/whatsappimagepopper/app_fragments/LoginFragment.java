package com.example.whatsappimagepopper.app_fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.room.Room;

import android.os.Handler;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import com.google.android.material.bottomnavigation.BottomNavigationView;

import org.json.JSONObject;

import java.io.IOException;
import java.util.Locale;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class LoginFragment extends Fragment {
    public View this_fragment;


    public LoginFragment() { } // Required empty public constructor

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        this.this_fragment = inflater.inflate(R.layout.fragment_login, container, false);
        TextView shift_login_opt = this_fragment.findViewById(R.id.shift_signup);
        shift_login_opt.setOnClickListener((View v)->{
            getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.fragmentHolder, new SignupFragment()).commit();
        });

        EditText uname_entry = this_fragment.findViewById(R.id.uname_entry_login);
        EditText password_entry = this_fragment.findViewById(R.id.pass_entry_login);
        AppRoomDataBase app_info_db = AppRoomDataBase.getInstance(getActivity().getApplicationContext());


        Button login_ = this_fragment.findViewById(R.id.btn_login);
        login_.setOnClickListener((View v)->{
            String username = uname_entry.getText().toString();
            String password = password_entry.getText().toString();

            MainActivity.blockButtonAtUI(getActivity(), login_);

            new HttpRequestMaker(getString(R.string.api_url) + "/login/login_now") {
                @Override
                public void onResp(String data) {
                    try {
                        JSONObject login_data = new JSONObject(data);
                        if (login_data.getString("status").compareTo("OK") == 0){
                            AppInfoDao cursor_ = app_info_db.appInfoDao();
                            cursor_.clearTable();
                            AppInfoTable row1 = new AppInfoTable();
                            row1.user_name = login_data.getString("username");  // from MongoDB
                            row1.password = login_data.getString("password");  // from MongoDB
                            row1.endpoint_name = login_data.getString("endpoint");  // from MongoDB
                            cursor_.insertOne(row1);
                            getActivity().runOnUiThread(()->{
                                ((MainActivity) getActivity()).changeBottomNavMenu(R.menu.with_login_navbar);
                                MainActivity.setAppFragment(getActivity(), new HomePage());
                            });
                        }
                        else if (login_data.getString("status").compareTo("ACCOUNT_NOT_EXISTS") == 0){
                            getActivity().runOnUiThread(()->{
                                MainActivity.createMsgBox(this_fragment, "Failed to login - given username and password not exists.", "error");
                            });
                        }
                        else{
                            getActivity().runOnUiThread(()-> {
                                MainActivity.createMsgBox(this_fragment, "Login failed due to an unknown error!", "error");
                            });
                        }
                    }
                    catch (Exception e){
                        Log.d("SCIHACK", e.toString());
                    }
                }

                @Override
                public void onFail(IOException err) {
                    Toast.makeText(getContext(), "Error : "+err.toString(), Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onCompletion() {
                    MainActivity.unblockButtonAtUI(getActivity(), login_);
                }
            }.postText(String.format(Locale.ENGLISH, "{\"username\": \"%s\", \"password\": \"%s\"}", username, password));
        });
        return this_fragment;
    }
}