package com.zhufuc.pctope.Activities;

import android.app.ActivityOptions;
import android.content.Intent;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.transition.Transition;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.transition.TransitionInflater;
import android.view.MenuItem;
import android.view.Window;

import com.zhufuc.pctope.Collectors.ActivityCollector;
import com.zhufuc.pctope.R;

public class CompressionActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //translation animation

        setContentView(R.layout.activity_compression);

        InitToolbar();

        navigationView = (NavigationView)findViewById(R.id.nav_view);
        drawerLayout = (DrawerLayout)findViewById(R.id.drawer_main);

        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                drawerLayout.closeDrawer(GravityCompat.START);
                switch (item.getItemId()){
                    case R.id.nav_manager:finish();
                        //final Intent main = new Intent(CompressionActivity.this,MainActivity.class);
                        //startActivity(main, ActivityOptions.makeSceneTransitionAnimation(CompressionActivity.this).toBundle());
                        break;
                    case R.id.nav_settings:Intent settings = new Intent(CompressionActivity.this,SettingsActivity.class);
                        startActivity(settings);
                        break;
                    case R.id.nav_about:Intent about = new Intent(CompressionActivity.this,AboutActivity.class);
                        startActivity(about);
                        break;
                }
                return true;
            }
        });
    }

    public void InitToolbar(){
        Toolbar toolbar = (Toolbar)findViewById(R.id.compression_act_toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle(R.string.nav_textures_packer);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar!=null){
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(R.drawable.menu);
        }
    }

    public void initContentCard(){
        RecyclerView list = (RecyclerView)findViewById(R.id.compression_gamedir_list);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        int id = item.getItemId();
        if (id == android.R.id.home){
            drawerLayout.openDrawer(GravityCompat.START);
        }
        return true;
    }
}
