package com.zhufuc.pctope.Activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.transition.Transition;
import android.transition.TransitionInflater;
import android.util.DisplayMetrics;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;

import com.zhufuc.pctope.Collectors.ActivityCollector;
import com.zhufuc.pctope.R;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Locale;

/**
 * Created by zhufu on 8/17/17.
 */

public class BaseActivity extends AppCompatActivity {

    boolean isGranted;

    NavigationView navigationView;
    DrawerLayout drawerLayout;

    @Override
    protected void onCreate(Bundle bundle){
        super.onCreate(bundle);
        //Translate animation
        //Window window = getWindow();
        //window.requestFeature(Window.FEATURE_CONTENT_TRANSITIONS);

        //Transition fade = TransitionInflater.from(this).inflateTransition(R.transition.fade);
        //window.setEnterTransition(fade);
        //window.setReenterTransition(fade);
        //window.setExitTransition(fade);

        //Collector
        ActivityCollector.addActivity(this);
        //Language
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
        setLanguage(settings.getString("pref_language","auto"));

    }

    @Override
    protected void onStart(){
        super.onStart();

        Class thisClass = getClass();
        if (thisClass.equals(MainActivity.class)) {
            //if now the context is main or compression
            if (thisClass.equals(MainActivity.class)){
                navigationView.setCheckedItem(R.id.nav_manager);
            }

            //for header image
            boolean ifImageExists = true;

            Bitmap content = null;
            try {
                FileInputStream inputStream = openFileInput("header_image.png");
                content = BitmapFactory.decodeStream(inputStream);
            } catch (FileNotFoundException e) {
                //e.printStackTrace();
                ifImageExists = false;
            }
            ImageView imageView = null;
            View layout = navigationView.getHeaderView(0);
            imageView = (ImageView) layout.findViewById(R.id.drawer_header_image);

            if (ifImageExists) {
                imageView.setImageBitmap(content);
                //imageView.setImageResource(0);
            } else {
                imageView.setImageBitmap(null);
                imageView.setImageResource(R.drawable.material_design_4);
            }
        }

    }

    protected void setLanguage(String language){
        Resources resources = getApplicationContext().getResources();
        Configuration configuration = resources.getConfiguration();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        switch (language){
            case "en":configuration.setLocale(Locale.ENGLISH);break;
            case "ch":configuration.setLocale(Locale.CHINESE);break;
            default:configuration.setLocale(SystemLanguage());
        }
        resources.updateConfiguration(configuration,metrics);
    }

    public Locale SystemLanguage(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
            return getResources().getConfiguration().getLocales().get(0);
        else return getResources().getConfiguration().locale;
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();

        ActivityCollector.removeActivity(this);
    }

}
