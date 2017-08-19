package com.zhufuc.pctope.Activities;


import android.annotation.TargetApi;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.SwitchPreference;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.RingtonePreference;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.MenuItem;
import android.support.v4.app.NavUtils;
import android.view.View;
import android.widget.Toast;

import com.zhufuc.pctope.Collectors.ActivityCollector;
import com.zhufuc.pctope.R;
import com.zhufuc.pctope.Utils.GetPathFromUri4kitkat;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import static java.lang.String.*;

public class SettingsActivity extends AppCompatPreferenceActivity {

    private Preference.OnPreferenceChangeListener changeListener = new Preference.OnPreferenceChangeListener() {
        @Override
        public boolean onPreferenceChange(Preference preference, Object newValue) {
            String key = preference.getKey();
            if (key.equals("pref_language")){
                Toast.makeText(SettingsActivity.this,R.string.rebooting,Toast.LENGTH_LONG).show();
                ActivityCollector.finishAll();
                startActivity(new Intent().setClass(SettingsActivity.this,FirstActivity.class));
            }
            return true;
        }
    };

    private ListPreference listPreference;
    private Preference customDrawer ,clearDrawer;
    @Override
    protected void onCreate(Bundle bundle){
        super.onCreate(bundle);

        ActivityCollector.addActivity(this);

        addPreferencesFromResource(R.xml.perf_main);
        ActionBar bar = getSupportActionBar();
        bar.setDisplayHomeAsUpEnabled(true);
        listPreference = (ListPreference)findPreference("pref_language");
        listPreference.setOnPreferenceChangeListener(changeListener);

        customDrawer = findPreference("pref_drawer_button");
        customDrawer.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                startActivityForResult(intent,1);
                return false;
            }
        });

        clearDrawer = findPreference("pref_drawer_clear");
        refreshClearButton();
        clearDrawer.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                deleteFile("header_image.png");
                refreshClearButton();
                return false;
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data){
        if (resultCode == RESULT_OK){
            if (requestCode == 1){
                Uri uri = data.getData();
                String path = GetPathFromUri4kitkat.getPath(this,uri);
                if (path==null) {
                    Toast.makeText(this,"File not found!",Toast.LENGTH_LONG).show();
                    return;
                }
                File imagePath = new File(path);
                if (!imagePath.exists()) {
                    Toast.makeText(this,"File not found!",Toast.LENGTH_LONG).show();
                    return;
                }

                FileOutputStream outputStream = null;
                Bitmap bitmap = BitmapFactory.decodeFile(path);
                if (bitmap == null){
                    Toast.makeText(this,"File isn't an image!",Toast.LENGTH_LONG).show();
                    return;
                }

                try {
                    outputStream = openFileOutput("header_image.png",MODE_PRIVATE);
                    bitmap.compress(Bitmap.CompressFormat.PNG,100,outputStream);

                } catch (FileNotFoundException e) {
                    Toast.makeText(this,"File not found!",Toast.LENGTH_LONG).show();
                    e.printStackTrace();
                }
                refreshClearButton();
            }

        }

    }

    public void refreshClearButton(){
        final File image = new File(this.getFilesDir() +"/header_image.png");
        clearDrawer.setEnabled(image.exists());
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        ActivityCollector.removeActivity(this);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        if (item.getItemId() == android.R.id.home){
            finish();
        }
        return true;
    }
}
