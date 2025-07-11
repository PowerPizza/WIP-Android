package com.example.whatsappimagepopper;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import com.example.whatsappimagepopper.app_fragments.AddImageFragment;
import com.example.whatsappimagepopper.app_fragments.DashboardFragment;
import com.example.whatsappimagepopper.app_fragments.HomePage;
import com.example.whatsappimagepopper.app_fragments.ProfileFragment;
import com.example.whatsappimagepopper.app_fragments.ProfileLoggedInFragment;
import com.example.whatsappimagepopper.room_database.AppInfoDao;
import com.example.whatsappimagepopper.room_database.AppInfoTable;
import com.google.android.material.bottomnavigation.BottomNavigationView;

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

import com.example.whatsappimagepopper.room_database.AppRoomDataBase;

public class MainActivity extends AppCompatActivity {
    public int curNavigation = R.id.navigation_home;
    public AppRoomDataBase app_info_db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.app_info_db = AppRoomDataBase.getInstance(getApplicationContext());

        EdgeToEdge.enable(this);  // creates problem of bottom padding with bottom nav bar.

        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, 0, systemBars.right, 0);
            return insets;
        });

        BottomNavigationView bottom_nav_bar = findViewById(R.id.bottom_nav_bar);
        Thread login_check_td = this.checkLogin(bottom_nav_bar);
        login_check_td.start();
        try {
            login_check_td.join();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        Fragment home_fragment = new HomePage();
        this.setAppFragment(home_fragment);

        bottom_nav_bar.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.navigation_home){
                this.curNavigation = item.getItemId();
                this.setAppFragment(home_fragment);
            }
            else if (item.getItemId() == R.id.navigation_profile){
                this.curNavigation = item.getItemId();
                this.setAppFragment((Fragment) new ProfileFragment());
            }
            else if (item.getItemId() == R.id.navigation_logged_in_profile){
                this.curNavigation = item.getItemId();
                this.setAppFragment((Fragment) new ProfileLoggedInFragment());
            }
            else if (item.getItemId() == R.id.navigation_add_img){
                this.curNavigation = item.getItemId();
                this.setAppFragment((Fragment) new AddImageFragment());
            }

            else if (item.getItemId() == R.id.navigation_dashboard) {
                this.curNavigation = item.getItemId();
                this.setAppFragment((Fragment) new DashboardFragment());
            }

            return true;
        });
    }

    public Thread checkLogin(BottomNavigationView bottom_nav_bar){
        // Changes bottom nav bar's menu as per user logged in status (login or not)
        return (
            new Thread(()-> {
                this.changeBottomNavMenu(R.menu.without_login_navbar);

                AppInfoDao app_info_cursor = this.app_info_db.appInfoDao();
                List<AppInfoTable> user_info_ = app_info_cursor.selectFirstRow();
                if (user_info_.isEmpty()) return;

                AppInfoTable user_info = user_info_.get(0);
                OkHttpClient cli = new OkHttpClient();
                RequestBody resp = RequestBody.create(String.format(Locale.ENGLISH, "{\"username\": \"%s\", \"password\": \"%s\"}", user_info.user_name, user_info.password), MediaType.get("text/plain"));
                Request r = new Request.Builder().url(getString(R.string.api_url) + "/login/login_now").post(resp).build();
                cli.newCall(r).enqueue(new Callback() {
                    @Override
                    public void onFailure(@NonNull Call call, @NonNull IOException e) {
                        Log.d("SH_ERROR", e.toString());
                        call.cancel();
                    }

                    @Override
                    public void onResponse(@NonNull Call call, @NonNull Response response) {
                        ResponseBody resp = response.body();
                        if (resp == null) {
                            Log.d("SH_WARN", "While login check server responded null.");
                            return;
                        }
                        try {
                            JSONObject resp_ = new JSONObject(resp.string());
                            if (resp_.getString("status").compareTo("OK") == 0) {
                                bottom_nav_bar.getMenu().clear();
                                bottom_nav_bar.inflateMenu(R.menu.with_login_navbar);
                            } else {
                                app_info_cursor.clearTable();
                            }
                        } catch (Exception e) {
                            Log.d("SH_ERROR", e.toString());
                        }
                    }
                });
            })
        );
    }

    public void setAppFragment(Fragment frag){
        getSupportFragmentManager().beginTransaction().replace(R.id.fragmentHolder, frag).commit();
    }
    public static void setAppFragment(AppCompatActivity activity, Fragment frag){
        activity.getSupportFragmentManager().beginTransaction().replace(R.id.fragmentHolder, frag).commit();
    }
    public static void setAppFragment(FragmentActivity activity, Fragment frag){
        activity.getSupportFragmentManager().beginTransaction().replace(R.id.fragmentHolder, frag).commit();
    }

    public void changeBottomNavMenu(int menu_id) {
        BottomNavigationView bottom_nav_bar = findViewById(R.id.bottom_nav_bar);
        bottom_nav_bar.getMenu().clear();
        bottom_nav_bar.inflateMenu(menu_id);
    }

    public void logout(){
        Thread td = new Thread(() -> {
            AppInfoDao cursor_ = this.app_info_db.appInfoDao();
            cursor_.clearTable();
        });
        try {
            td.start();
            td.join();
        }
        catch (Exception ignored){ }
        this.changeBottomNavMenu(R.menu.without_login_navbar);
        this.setAppFragment(new HomePage());
    }

    public static void createMsgBox(View fragment_, String msg, String msg_type){
        // To use it in any fragment - add a horizontal linear layout with id msg_holder in that fragment.
        int bg_color =  R.color.msg_bg_green;
        int fg_color = R.color.msg_fg_green;

        switch (msg_type){
            case "error":
                bg_color = R.color.msg_bg_red;
                fg_color = R.color.msg_fg_red;
                break;
            case "info":
                bg_color = R.color.msg_bg_blue;
                fg_color = R.color.msg_fg_blue;
                break;
            case "warning":
                bg_color = R.color.msg_bg_orange;
                fg_color = R.color.msg_fg_orange;
            default:
                break;
        }

        LinearLayout fr_holder = fragment_.findViewById(R.id.msg_holder);
        fr_holder.removeAllViews();
        TextView msg_ele = new TextView(fragment_.getContext());
        msg_ele.setText(msg);
        msg_ele.setBackgroundColor(ContextCompat.getColor(fragment_.getContext(), bg_color));
        msg_ele.setTextColor(ContextCompat.getColor(fragment_.getContext(), fg_color));
        msg_ele.setTextSize(16);
        msg_ele.setPadding(4, 4, 4, 4);
        LinearLayout.LayoutParams lp1 = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        msg_ele.setLayoutParams(lp1);
        fr_holder.addView(msg_ele);

        Handler h1 = new Handler();
        h1.postDelayed(fr_holder::removeAllViews, 3000);
    }
}