package com.zhufuc.pctope.Activities;

import android.Manifest;
import android.app.Activity;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.CardView;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Toast;

import com.zhufuc.pctope.Collectors.ActivityCollector;
import com.zhufuc.pctope.Adapters.TextureItems;
import com.zhufuc.pctope.Adapters.Textures;
import com.zhufuc.pctope.Interf.DeletingCallback;
import com.zhufuc.pctope.Utils.GetPathFromUri4kitkat;
import com.zhufuc.pctope.Utils.*;
import com.zhufuc.pctope.R;

import java.io.File;

import static android.widget.Toast.makeText;


public class MainActivity extends AppCompatActivity {


    public void MakeErrorDialog(final String errorString){
        //make up a error dialog
        AlertDialog.Builder error_dialog = new AlertDialog.Builder(MainActivity.this);
        error_dialog.setTitle(R.string.error);
        error_dialog.setMessage(MainActivity.this.getString(R.string.error_dialog)+errorString);
        error_dialog.setIcon(R.drawable.alert_octagram);
        error_dialog.setCancelable(false);
        error_dialog.setPositiveButton(R.string.close, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                finish();
            }
        });
        error_dialog.setNegativeButton(R.string.copy, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                ClipboardManager copy = (ClipboardManager)getSystemService(Context.CLIPBOARD_SERVICE);
                copy.setText(errorString);
                finish();
            }
        }).show();
    }

    private void Choose(){
        makeText(MainActivity.this, R.string.choosing_alert, Toast.LENGTH_SHORT).show();
        Intent choose = new Intent(Intent.ACTION_GET_CONTENT);
        choose.setType("*/*");
        choose.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(choose, 1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 0){
            Boolean result = data.getBooleanExtra("Status_return",false);
            if (result)
                loadList();
        }
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == 1) {
                Uri uri = data.getData();
                String realPath = GetPathFromUri4kitkat.getPath(MainActivity.this,uri);
                Intent intent = new Intent(MainActivity.this,ConversionActivity.class);
                intent.putExtra("filePath",realPath);
                startActivityForResult(intent,0);
            }
        }
        else{
            FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
            Snackbar.make(fab,R.string.choosing_none,Snackbar.LENGTH_LONG).show();
        }
    }

    private String[] permissions = {Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.MOUNT_UNMOUNT_FILESYSTEMS, Manifest.permission.READ_EXTERNAL_STORAGE};
    Intent intent = getIntent();
    boolean isgranted;

    FloatingActionButton fab = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //defining
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActivityCollector.addActivity(MainActivity.this);
        Intent intent = getIntent();
        isgranted = intent.getBooleanExtra("isgranted", true);
        fab = (FloatingActionButton) findViewById(R.id.fab);
        final RecyclerView recyclerView = (RecyclerView)findViewById(R.id.recycle_view);
        Log.d("status", isgranted + "");

        //file choosing
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            Choose();
            }
        });

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (newState>0){
                    fab.hide();
                }
                else {
                    fab.show();
                }
            }
        });


        if (isgranted) {
            initActivity();
        }
        else{
            Snackbar.make(fab, R.string.permissions_request, Snackbar.LENGTH_INDEFINITE)
                    .setAction(R.string.ok, new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            ActivityCompat.requestPermissions(MainActivity.this, permissions, 1);
                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    while (ContextCompat.checkSelfPermission(MainActivity.this,permissions[0]) == PackageManager.PERMISSION_DENIED){}
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            initActivity();
                                        }
                                    });
                                }
                            }).start();
                        }
                    }).show();
        }
    }

    private RecyclerView recyclerView;private TextureItems items = new TextureItems();
    private void initActivity(){
        final CardView android_nothing_card = (CardView)findViewById(R.id.android_nothing);
        //init for textures list
        recyclerView = (RecyclerView)findViewById(R.id.recycle_view);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);

        RecyclerView.ItemAnimator swipe = new DefaultItemAnimator();
        recyclerView.setItemAnimator(swipe);
        recyclerView.setAdapter(items);
        recyclerView.setHasFixedSize(true);

        loadList();

        ItemTouchHelper.Callback mCallback = new ItemTouchHelper.SimpleCallback(0,ItemTouchHelper.START|ItemTouchHelper.END) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                final int position = viewHolder.getAdapterPosition();
                final Textures deleting = items.getItem(position);
                final File test = new File(deleting.getPath().toString());
                final TextureItems oldTemp = items;
                items.remove(position);

                final CardView android_nothing_card = (CardView)findViewById(R.id.android_nothing);
                if (items.getItemCount() == 0){
                    recyclerView.setVisibility(View.GONE);
                    android_nothing_card.setVisibility(View.VISIBLE);
                    Animation show = AnimationUtils.loadAnimation(MainActivity.this,R.anim.cards_show);
                    android_nothing_card.startAnimation(show);
                }

                DiffUtil.Callback callback = new DiffUtilCallback(oldTemp,items);
                final DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(callback);
                diffResult.dispatchUpdatesTo(items);
                items.notifyItemRemoved(position);

                Snackbar.make(fab,R.string.deleted_completed,Snackbar.LENGTH_LONG)
                        .setCallback(new DeletingCallback(test))
                        .setAction(R.string.undo, new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                if (test.exists()){
                                    TextureItems oldTemp1 = items;
                                    items.addItem(position,deleting);
                                    DiffUtil.Callback callback1 = new DiffUtilCallback(oldTemp1,items);
                                    DiffUtil.DiffResult diffResult1 = DiffUtil.calculateDiff(callback1);
                                    recyclerView.setVisibility(View.VISIBLE);
                                    android_nothing_card.setVisibility(View.GONE);
                                    diffResult.dispatchUpdatesTo(items);
                                    items.notifyItemInserted(position);
                                }
                                else {
                                    Snackbar.make(fab,R.string.failed,Snackbar.LENGTH_SHORT).show();
                                }
                            }
                        }).show();
            }
        };
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(mCallback);
        itemTouchHelper.attachToRecyclerView(recyclerView);

        //for swipe refresh layout
        final SwipeRefreshLayout swipeRefreshLayout = (SwipeRefreshLayout)findViewById(R.id.swipe_refresh);
        swipeRefreshLayout.setColorSchemeColors(getResources().getColor(R.color.colorAccent),getResources().getColor(R.color.google_blue)
                ,getResources().getColor(R.color.google_red),getResources().getColor(R.color.google_green));
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
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
                                loadList();
                                swipeRefreshLayout.setRefreshing(false);
                            }
                        });
                    }
                }).start();
            }
        });

        android_nothing_card.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Animation show = AnimationUtils.loadAnimation(MainActivity.this,R.anim.cards_show);
                android_nothing_card.startAnimation(show);
                show.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {

                    }
                    @Override
                    public void onAnimationEnd(Animation animation) {
                        loadList();
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {

                    }
                });
            }
        });
    }

    @Override
    protected void onResume(){
        super.onResume();
        loadList();
    }

    private void loadList(){
        TextureItems oldTemp = items;
        items.clear();

        File packsListDir = new File(Environment.getExternalStorageDirectory()+"/games/com.mojang/resource_packs/")
                ,packsList[] = null;
        Boolean make = true;

        RecyclerView recyclerView = (RecyclerView)findViewById(R.id.recycle_view);
        CardView android_nothing_card = (CardView)findViewById(R.id.android_nothing);
        Boolean isLoaded = false;

        if (!packsListDir.exists()) make = packsListDir.mkdirs();

        if (make){
            packsList = packsListDir.listFiles();

            for (int i=0;i<packsList.length;i++){
                if (packsList[i].exists())
                    if (packsList[i].isDirectory()){
                        Textures texture = new Textures(packsList[i]);
                        if (!isLoaded){
                            recyclerView.setVisibility(View.VISIBLE);
                            android_nothing_card.setVisibility(View.GONE);
                            isLoaded = true;
                        }
                        if (texture.IfIsResourcePack("ALL")) {
                            items.addItem(texture);
                        }
                    }
            }

            if (items.getItemCount() == 0){
                recyclerView.setVisibility(View.GONE);
                android_nothing_card.setVisibility(View.VISIBLE);
            }
            DiffUtil.Callback callback = new DiffUtilCallback(oldTemp,items);
            DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(callback);
            diffResult.dispatchUpdatesTo(items);
            items.notifyDataSetChanged();
        }
        else MakeErrorDialog("Failed to make textures root directory.");
    }
}
