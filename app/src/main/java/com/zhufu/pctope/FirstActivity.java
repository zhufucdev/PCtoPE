package com.zhufu.pctope;

import android.Manifest;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.accessibility.AccessibilityManagerCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import static android.R.attr.permission;



public class FirstActivity extends AppCompatActivity {
    private String[] permissions = {Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityCollector.addActivity(this);
        setContentView(R.layout.activity_first);
        SharedPreferences.Editor editor = getSharedPreferences("data", MODE_PRIVATE).edit();
        SharedPreferences preferences = getSharedPreferences("data", MODE_PRIVATE);
        boolean isbooted = preferences.getBoolean("isbooted", false);
        //is first boot
        if (isbooted == false) {
            editor.putBoolean("isbooted", true);
            editor.apply();
        }
        //request permissions
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            final int length = permissions.length-1;
            ActivityCompat.requestPermissions(FirstActivity.this, permissions, 2);
            class Waiting extends AsyncTask<Void ,Integer ,Boolean>{
                @Override
                protected Boolean doInBackground(Void... voids) {
                    int j=0;
                    while(ContextCompat.checkSelfPermission(FirstActivity.this,permissions[0]) == PackageManager.PERMISSION_DENIED){
                        Log.d("status","Now j is "+j);
                        j++;
                        try {
                            Thread.sleep(100);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        if(j>250){
                            return false;
                        }
                    }
                    Log.d("status","Now break.");
                    return true;
                }
                @Override
                protected void onPostExecute(Boolean result){
                    Intent domain = new Intent(FirstActivity.this, MainActivity.class);
                    domain.putExtra("isgranted",result);
                    startActivity(domain);
                    finish();
                }
            }
            new Waiting().execute();
        }
    }
}