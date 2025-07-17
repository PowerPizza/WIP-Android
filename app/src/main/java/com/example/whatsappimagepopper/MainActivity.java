package com.example.whatsappimagepopper;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
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
import com.example.whatsappimagepopper.http_requests.HttpRequestMaker;
import com.example.whatsappimagepopper.room_database.AppInfoDao;
import com.example.whatsappimagepopper.room_database.AppInfoTable;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import org.json.JSONObject;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicInteger;

import com.example.whatsappimagepopper.room_database.AppRoomDataBase;

public class MainActivity extends AppCompatActivity {
    public int curNavigation = R.id.navigation_home;
    public AppRoomDataBase app_info_db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.app_info_db = AppRoomDataBase.getInstance(getApplicationContext());

        EdgeToEdge.enable(this);

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

        AtomicInteger last_item_id = new AtomicInteger(-1);
        bottom_nav_bar.setOnItemSelectedListener(item -> {
            if (last_item_id.get() == item.getItemId()){
                return true;
            }

            if (item.getItemId() == R.id.navigation_home){
                this.curNavigation = item.getItemId();
                this.setAppFragment(home_fragment);
            }
            else if (item.getItemId() == R.id.navigation_profile){
                this.curNavigation = item.getItemId();
                this.setAppFragment(new ProfileFragment());
            }
            else if (item.getItemId() == R.id.navigation_logged_in_profile){
                this.curNavigation = item.getItemId();
                this.setAppFragment(new ProfileLoggedInFragment());
            }
            else if (item.getItemId() == R.id.navigation_add_img){
                this.curNavigation = item.getItemId();
                this.setAppFragment(new AddImageFragment());
            }
            else if (item.getItemId() == R.id.navigation_dashboard) {
                this.curNavigation = item.getItemId();
                this.setAppFragment(new DashboardFragment());
            }
            last_item_id.set(item.getItemId());

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

                new HttpRequestMaker(getString(R.string.api_url) + "/login/login_now") {
                    @Override
                    public void onResp(String data) {
                        try {
                            JSONObject resp_ = new JSONObject(data);
                            if (resp_.getString("status").compareTo("OK") == 0) {
                                changeBottomNavMenu(R.menu.with_login_navbar);
                            } else {
                                app_info_cursor.clearTable();
                            }
                        } catch (Exception e) {
                            Log.d("SH_ERROR", e.toString());
                        }
                    }

                    @Override
                    public void onFail(IOException err) {
                        Log.d("SH_ERROR", err.toString());
                    }

                    @Override
                    public void onCompletion() { }
                }.postText(String.format(Locale.ENGLISH, "{\"username\": \"%s\", \"password\": \"%s\"}", user_info.user_name, user_info.password));
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
        runOnUiThread(()->{
            BottomNavigationView bottom_nav_bar = findViewById(R.id.bottom_nav_bar);
            bottom_nav_bar.getMenu().clear();
            bottom_nav_bar.inflateMenu(menu_id);
        });
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

    public static void blockButtonAtUI(Activity activity, Button b){
        activity.runOnUiThread(()->{
            b.setTag(b.getText());
            b.setText(activity.getString(R.string.pls_wait));
            b.setEnabled(false);
        });
    }
    public static void unblockButtonAtUI(Activity activity, Button b){
        activity.runOnUiThread(()->{
            b.setText(b.getTag().toString());
            b.setEnabled(true);
        });
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