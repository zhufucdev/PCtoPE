package com.zhufuc.pctope.Activities;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.zhufuc.pctope.Collectors.ActivityCollector;
import com.zhufuc.pctope.R;

import java.util.ArrayList;

import za.co.riggaroo.materialhelptutorial.TutorialItem;
import za.co.riggaroo.materialhelptutorial.tutorial.MaterialTutorialActivity;


public class FirstActivity extends BaseActivity {
    private String[] permissions = {Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE};
    Boolean isGranted = true;


    private ArrayList<TutorialItem> getTutorialItems(Context context){
        TutorialItem item1 = new TutorialItem(context.getString(R.string.tutorial_welcome),context.getString(R.string.tutorial_wlcome_subtitle)
                ,R.color.amber_primary,0,R.drawable.ic_launcher_foreground);
        TutorialItem item2 = new TutorialItem(context.getString(R.string.tutorial_step1),context.getString(R.string.tutorial_step1_subtitle)
                ,R.color.app_blue_dark,R.drawable.for_step2_fab,R.drawable.for_step2_background);
        TutorialItem item3 = new TutorialItem(context.getString(R.string.tutorial_step2),context.getString(R.string.tutorial_step2_subtitle)
                ,R.color.app_green,R.drawable.for_step3);
        TutorialItem item4 = new TutorialItem(context.getString(R.string.tutorial_step3),context.getString(R.string.tutorial_step3_subtitle)
                ,R.color.app_red,R.drawable.for_step4);
        TutorialItem item5 = new TutorialItem(context.getString(R.string.tutorial_step4),context.getString(R.string.tutorial_step4_subtitle)
                ,R.color.deep_purple_primary,R.drawable.for_step5);
        TutorialItem item6 = new TutorialItem(context.getString(R.string.tutorial_step5),context.getString(R.string.tutorial_step5_subtitle)
                ,R.color.orange_primary,R.drawable.for_step6);
        TutorialItem item7 = new TutorialItem(context.getString(R.string.tutorial_hope),context.getString(R.string.tutorial_hope_subtitle)
                ,R.color.yellow_primary_dark,R.drawable.happy_face);
        ArrayList<TutorialItem> tutorialItems = new ArrayList<>();
        tutorialItems.add(item1);
        tutorialItems.add(item2);
        tutorialItems.add(item3);
        tutorialItems.add(item4);
        tutorialItems.add(item5);
        tutorialItems.add(item6);
        tutorialItems.add(item7);
        return tutorialItems;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        if (requestCode == 0){
            Intent domain = new Intent(FirstActivity.this, MainActivity.class);
            domain.putExtra("isgranted",isGranted);
            startActivity(domain);
            finish();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //request permissions
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
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
                    isGranted = result;
                    InitTutorial();
                }
            }
            new Waiting().execute();
        }
        else InitTutorial();
    }

    private void InitTutorial(){
        SharedPreferences.Editor editor = getSharedPreferences("data", MODE_PRIVATE).edit();
        SharedPreferences preferences = getSharedPreferences("data", MODE_PRIVATE);
        boolean isbooted = preferences.getBoolean("isbooted", false);
        //is first boot
        if (isbooted == false) {
            //for Tutorial Activity
            Intent tutorialActivity = new Intent(FirstActivity.this, MaterialTutorialActivity.class);
            tutorialActivity.putParcelableArrayListExtra(MaterialTutorialActivity.MATERIAL_TUTORIAL_ARG_TUTORIAL_ITEMS,getTutorialItems(this));
            startActivityForResult(tutorialActivity,0);

            editor.putBoolean("isbooted", true);
            editor.apply();
        }
        else onActivityResult(0,0,null);
    }
}