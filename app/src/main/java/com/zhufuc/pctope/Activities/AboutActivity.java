package com.zhufuc.pctope.Activities;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import com.zhufuc.pctope.R;

public class AboutActivity extends BaseActivity implements View.OnClickListener{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        TextView version = (TextView)findViewById(R.id.about_version_text);
        PackageManager packageManager = getPackageManager();
        PackageInfo info = null;
        try {
            info = packageManager.getPackageInfo(getPackageName(),0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        String versionName = info.versionName;
        int versionCode = info.versionCode;
        version.setText("PCtoPE "+versionName+"("+versionCode+")");

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null){
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        ImageView visitGithub = (ImageView)findViewById(R.id.visit_github);
        visitGithub.setOnClickListener(this);

        ImageView visitCoolapk = (ImageView)findViewById(R.id.visit_coolapk);
        visitCoolapk.setOnClickListener(this);

        final Animation show = AnimationUtils.loadAnimation(this,R.anim.cards_show);
        final ImageView icon = (ImageView)findViewById(R.id.about_icon);
        icon.setVisibility(View.INVISIBLE);
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        icon.startAnimation(show);
                    }
                });
            }
        }).start();
        show.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                icon.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animation animation) {

            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        if (item.getItemId() == android.R.id.home){
            finish();
        }
        return true;
    }

    @Override
    public void onClick(View view) {
        Uri uri = null;
        Intent site = new Intent(Intent.ACTION_VIEW);
        switch (view.getId()){
            case R.id.visit_github:
                uri = Uri.parse("https://github.com/zhufucdev/PCtoPE");
                break;
            case R.id.visit_coolapk:
                uri = Uri.parse("https://www.coolapk.com/apk/com.zhufuc.pctope");
                break;
            default:
        }
        site.setData(uri);
        startActivity(site);
    }
}