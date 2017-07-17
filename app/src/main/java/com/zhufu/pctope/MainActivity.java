package com.zhufu.pctope;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import static android.widget.Toast.makeText;


public class MainActivity extends AppCompatActivity {

    private void Choose(){
        makeText(MainActivity.this, R.string.choosing_alert, Toast.LENGTH_SHORT).show();
        Intent choose = new Intent(Intent.ACTION_GET_CONTENT);
        choose.setType("*/*");
        choose.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(choose, 1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == 1) {
                Uri uri = data.getData();
                String realPath = GetPathFromUri4kitkat.getPath(MainActivity.this,uri);
                Intent intent = new Intent(MainActivity.this,ConversionActivity.class);
                intent.putExtra("filePath",realPath);
                startActivity(intent);
            }
        }
        else{
            FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
            Snackbar.make(fab,R.string.choosing_none,Snackbar.LENGTH_LONG).show();
        }
    }

    private String[] permissions = {Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.MOUNT_UNMOUNT_FILESYSTEMS, Manifest.permission.READ_EXTERNAL_STORAGE};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //defining
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        final Intent intent = getIntent();
        boolean isgranted = intent.getBooleanExtra("isgranted", true);
        ActivityCollector.addActivity(MainActivity.this);
        Log.d("status", isgranted + "");
        //file choosing
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            Choose();
            }
        });
        fab.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                Intent intent = new Intent(MainActivity.this,ConversionActivity.class);
                startActivity(intent);
                return false;
            }
        });
        //
        if (isgranted == false) {
            Snackbar.make(fab, R.string.permissions_request, Snackbar.LENGTH_LONG)
                    .setAction(R.string.ok, new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            ActivityCompat.requestPermissions(MainActivity.this, permissions, 1);
                        }
                    }).show();
        }

    }


}
