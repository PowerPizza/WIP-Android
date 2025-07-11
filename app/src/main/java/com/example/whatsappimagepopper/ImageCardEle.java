package com.example.whatsappimagepopper;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.ColorRes;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;

import com.bumptech.glide.Glide;

import java.util.Locale;
import java.util.logging.LogRecord;

import okhttp3.internal.http2.Http2Reader;

public class ImageCardEle extends LinearLayout {
    private ImageView card_image;
    private int card_width = 300;
    private Context context;

    public ImageCardEle(Context context, String image_url, String image_name, String endpoint_name){
        super(context);
        this.context = context;
        this.card_width = (context.getResources().getDisplayMetrics().widthPixels-50) / 2;
        this.setOrientation(VERTICAL);
        this.setPadding(10, 10, 10, 10);
        LayoutParams lp_main = new LayoutParams(this.card_width, ViewGroup.LayoutParams.MATCH_PARENT);
        lp_main.setMargins(5, 5, 5, 5);
        this.setLayoutParams(lp_main);
        this.setBackgroundResource(R.drawable.image_card_bg);

        this.card_image = new ImageView(context);
        this.card_image.setLayoutParams(new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, this.card_width));
        this.card_image.setScaleType(ImageView.ScaleType.CENTER_CROP);
        this.card_image.setBackgroundResource(R.drawable.border_dark_green);
        this.card_image.setPadding(3, 3, 3, 3);
        Glide.with(this)
                .load(image_url)
                .placeholder(R.drawable.loading_img_icon)
                .error(
                        Glide.with(this).load(R.drawable.no_image)
                )
                .into(this.card_image);
        this.addView(this.card_image);

        TextView image_name_text = new TextView(context);
        image_name_text.setTextSize(16);
        image_name_text.setText(String.format(Locale.ENGLISH, "Name : %s", image_name));
        image_name_text.setTextColor(ContextCompat.getColor(context, R.color.black));
        this.addView(image_name_text);

        TextView endpint_name_text = new TextView(context);
        endpint_name_text.setTextSize(16);
        endpint_name_text.setText(String.format(Locale.ENGLISH, "Endpoint : %s", endpoint_name));
        endpint_name_text.setTextColor(ContextCompat.getColor(context, R.color.black));
        this.addView(endpint_name_text);

        Button copy_link = new Button(context);
        copy_link.setBackgroundResource(R.drawable.link_btn_layout);
        LayoutParams lp = new LayoutParams(70, 70);
        lp.gravity = Gravity.END;
        lp.rightMargin = 5;
        lp.bottomMargin = 5;
        copy_link.setLayoutParams(lp);
        copy_link.setPadding(10, 10, 10, 10);
        copy_link.setOnClickListener((View v)-> {
            copy_link.setBackgroundResource(R.drawable.tick_btn_bg);
            Handler handler = new Handler();
            handler.postDelayed(()->{
                copy_link.setBackgroundResource(R.drawable.link_btn_layout);
            }, 2000);
            copyToClipBoard(image_name, String.format(Locale.ENGLISH, "https://wipr.onrender.com/use/%s/%s", endpoint_name, image_name));
        });
        this.addView(copy_link);
    }

    public void copyToClipBoard(String label, String text){
        ClipboardManager cp1 = (ClipboardManager) this.context.getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData to_set = ClipData.newPlainText(label, text);
        cp1.setPrimaryClip(to_set);
        Toast.makeText(this.context, "Image url copied to clipboard!", Toast.LENGTH_SHORT).show();
    }
}
