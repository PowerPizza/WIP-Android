package com.example.whatsappimagepopper.app_fragments;

import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.fragment.app.Fragment;

import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.whatsappimagepopper.R;
import com.example.whatsappimagepopper.http_requests.HttpRequestMaker;
import com.example.whatsappimagepopper.room_database.AppInfoDao;
import com.example.whatsappimagepopper.room_database.AppInfoTable;
import com.example.whatsappimagepopper.room_database.AppRoomDataBase;

import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.IOException;
import java.util.Iterator;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

class UserPostField extends LinearLayout {
    private Context context;
    private String img_name;
    private Runnable delete_callback;
    static int no_of_posts = 0;

    public UserPostField(Context context, String imageName, String imageUrl){
        super(context);
        this.context = context;
        this.img_name = imageName;
        this.setOrientation(HORIZONTAL);
        LayoutInflater.from(context).inflate(R.layout.user_post_ele, this, true);

        ImageView iv = findViewById(R.id.userele_img_view);
        Glide.with(context).load(imageUrl).placeholder(R.drawable.loading_img_icon).error(
                Glide.with(context).load(R.drawable.no_image)
        ).into(iv);

        ((TextView) findViewById(R.id.userele_img_name)).setText(context.getString(R.string.image_name2, imageName));

        ((TextView) findViewById(R.id.userele_img_url)).setText(context.getString(R.string.image_url2, imageUrl));

        ImageButton delete_btn = findViewById(R.id.userele_delete_btn);
        delete_btn.setOnClickListener(this::onClickDelete);
    }
    public UserPostField(Context context, AttributeSet attr){
        super(context);
    }

    public void executeAfterDelete(Runnable callback){
        this.delete_callback = callback;
    }

    private void onClickDelete(View v){
        // callback method will be executed as entry deletes from database.
        JSONObject to_send = new JSONObject();
        Thread td = new Thread(()->{
            try {
                AppInfoDao cursor_ = AppRoomDataBase.getInstance(this.context).appInfoDao();
                AppInfoTable user_data = cursor_.selectFirstRow().get(0);
                to_send.put("username", user_data.user_name);
                to_send.put("password", user_data.password);
                to_send.put("img_name", this.img_name);
            }
            catch (Exception ignored) {}
        });
        try {
            td.start();
            td.join();
        }
        catch (Exception e){
            Log.d("SCIHACK", e.toString());
        }

        new HttpRequestMaker(this.context.getString(R.string.api_url) + "/api/delete_post") {
            @Override
            public void onResp(String data) {
                if (data.compareTo("OK") == 0){
                    no_of_posts -= 1;
                }
                delete_callback.run();
            }

            @Override
            public void onFail(IOException err) {
                Toast.makeText(context, "Error : "+err.toString(), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCompletion() { }
        }.postText(to_send.toString());
    }
}

public class DashboardFragment extends Fragment {
    public View this_fragment;

    public DashboardFragment() { }  // Required empty public constructor

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public void load_user_posts(){
        JSONObject to_send = new JSONObject();
        Thread td = new Thread(()->{
            AppInfoDao cursor_ = AppRoomDataBase.getInstance(getContext()).appInfoDao();
            AppInfoTable user_data = cursor_.selectFirstRow().get(0);
            try {
                to_send.put("username", user_data.user_name);
                to_send.put("password", user_data.password);
            }
            catch (Exception ignored) {}
        });

        try {
            td.start();
            td.join();
        }
        catch (Exception e){
            Log.d("SCIHACK", e.toString());
        }

        this.toggleLoadingCircle(true);
        new HttpRequestMaker(getString(R.string.api_url)+"/api/img_links_by_creds") {
            @Override
            public void onResp(String data) {
                try {
                    UserPostField.no_of_posts = 0;
                    getActivity().runOnUiThread(()->{
                        clearUserPostHolder();
                    });
                    JSONObject data_ = new JSONObject(data);
                    Iterator<String> image_names = data_.keys();
                    if (!image_names.hasNext()){
                        toggleNoPostText(true);
                        return;
                    }
                    while (image_names.hasNext()){
                        UserPostField.no_of_posts += 1;
                        String key_ = image_names.next();
                        String val_ = data_.getString(key_);
                        getActivity().runOnUiThread(()->{
                            addUserPostEle(key_, val_);
                        });
                    }
                    toggleNoPostText(false);
                    toggleLoadingCircle(false);
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
            public void onCompletion() { }
        }.postText(to_send.toString());
    }

    public void addUserPostEle(String img_name, String img_url){
        LinearLayout post_ele_holder = this.this_fragment.findViewById(R.id.user_post_ele_holder);
        UserPostField pf1 = new UserPostField(getContext(),img_name, img_url);
        pf1.executeAfterDelete(()->{
            if (UserPostField.no_of_posts == 0){
                this.toggleNoPostText(true);
            }
            getActivity().runOnUiThread(()->{
                this.clearUserPostHolder();
                this.load_user_posts();
            });
        });
        post_ele_holder.addView(pf1);
    }

    public void clearUserPostHolder(){
        LinearLayout post_ele_holder = this.this_fragment.findViewById(R.id.user_post_ele_holder);
        post_ele_holder.removeAllViews();
    }

    public void toggleNoPostText(boolean show){
        getActivity().runOnUiThread(()->{
            TextView tv = this.this_fragment.findViewById(R.id.no_posts_text);
            tv.setVisibility(show ? View.VISIBLE : View.GONE);
        });
    }

    public void toggleLoadingCircle(boolean show){
        getActivity().runOnUiThread(()->{
            View loading_cir = this.this_fragment.findViewById(R.id.loading_circle);
            loading_cir.setVisibility(show ? View.VISIBLE : View.GONE);
        });
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        this.this_fragment = inflater.inflate(R.layout.fragment_dashboard, container, false);
        this.toggleNoPostText(false);
        this.toggleLoadingCircle(true);

        this.load_user_posts();

        return this.this_fragment;
    }
}