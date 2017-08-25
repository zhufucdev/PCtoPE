package com.zhufuc.pctope.Activities;

import android.animation.Animator;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.annotation.BoolRes;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputEditText;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;

import com.zhufuc.pctope.Adapters.Textures;
import com.zhufuc.pctope.R;

import java.io.File;
import java.math.BigDecimal;

import static android.view.View.GONE;

public class DetailsActivity extends BaseActivity {

    private String name,description,version,icon,path;
    private Textures textures = null;
    private BigDecimal size;

    private NestedScrollView cards;
    private FloatingActionButton fab;

    private final String fullPE = "Found:full PE pack.";

    private boolean isDataChanged = false;

    public long getFolderTotalSize(String path){
        File[] files = new File(path).listFiles();
        long size = 0;
        for (File f:files)
            if (f.isFile()){
                size += f.length();
            }
            else
                size += getFolderTotalSize(f.getPath());
        return size;
    }

    View.OnClickListener FabListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            final View dialogView = LayoutInflater.from(DetailsActivity.this).inflate(R.layout.details_texture_basic_info_editor,null);
            AlertDialog.Builder dialog = new AlertDialog.Builder(DetailsActivity.this);

            dialog.setTitle(R.string.project_icon_edit);
            dialog.setView(dialogView);

            final EditText editName = (EditText) dialogView.findViewById(R.id.details_edit_name)
                    ,editDescription = (EditText) dialogView.findViewById(R.id.details_edit_description);

            editName.setText(name);
            editDescription.setText(description);

            dialog.setNegativeButton(R.string.confirm, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    Textures.Edit edit = new Textures.Edit(path);
                    String setName = editName.getText().toString(),setDescription = editDescription.getText().toString();
                    if (!setName.equals(name)) {
                        edit.changeName(setName);
                        isDataChanged = true;
                    }
                    if (!setDescription.equals(description)) {
                        edit.changeDescription(setDescription);
                        isDataChanged = true;
                    }

                    loadDetailedInfo();
                }
            });

            dialog.show();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);

        progress = (ProgressBar)findViewById(R.id.details_loading_progress);
        fab = (FloatingActionButton)findViewById(R.id.details_fab);
        fab.setOnClickListener(FabListener);

        Intent intent = getIntent();
        icon = intent.getStringExtra("texture_icon");
        version = intent.getStringExtra("texture_version");
        name = intent.getStringExtra("texture_name");
        description = intent.getStringExtra("texture_description");
        path = intent.getStringExtra("texture_path");
        initBasicTitles();

        loadDetailedInfo();

        initToolbar();

    }

    public void updateInformation(){
        if (textures!=null)
            textures = null;
        textures = new Textures(new File(path));

        BigDecimal totalSize = new BigDecimal(getFolderTotalSize(path));
        BigDecimal BtoMB = new BigDecimal(1024*1024);
        size = totalSize.divide((BtoMB),5,BigDecimal.ROUND_HALF_UP);
    }

    public void loadDetailedInfo(){

        cards = (NestedScrollView) findViewById(R.id.details_info_layout);


        class LoadingTask extends AsyncTask<Void, Integer ,Boolean>{
            @Override
            public void onPreExecute(){
                showLoading();
                fab.setEnabled(false);
                cards.setVisibility(View.INVISIBLE);
            }

            @Override
            public Boolean doInBackground(Void... params){
                updateInformation();

                version = null;
                name = null;
                description = null;

                version = textures.getVersion();
                name = textures.getName();
                description = textures.getDescription();

                try {
                    Thread.sleep(600);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                return true;

            }

            @Override
            public void onPostExecute(Boolean result){
                loadViews();
                initBasicTitles();
                hideLoading();
            }
        }
        new LoadingTask().execute();
    }

    public void loadViews(){
        //-----FloatingActionButton-----
        fab.setEnabled(true);

        //-----CARD------
        TextView size = (TextView)findViewById(R.id.details_texture_size);
        size.setText(String.valueOf(getString(R.string.details_card_basic_info_size)+": "+this.size+"MB"));

        TextView location = (TextView)findViewById(R.id.details_texture_location);
        location.setText(String.valueOf(getString(R.string.details_card_basic_info_location)+": "+path));
        location.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ClipboardManager clipboardManager = (ClipboardManager)getSystemService(CLIPBOARD_SERVICE);
                ClipData data = ClipData.newPlainText("Path",path);
                clipboardManager.setPrimaryClip(data);
                Snackbar.make(view,R.string.copied,Snackbar.LENGTH_SHORT).show();
            }
        });

        Animator anim = ViewAnimationUtils.createCircularReveal(cards,cards.getWidth()/2,0,0,(float)Math.hypot(cards.getWidth(),cards.getHeight()));
        cards.setVisibility(View.VISIBLE);
        anim.setDuration(500);
        anim.setInterpolator(new AccelerateDecelerateInterpolator());
        anim.start();
    }

    public void initBasicTitles(){

        ImageView iconView = (ImageView)findViewById(R.id.details_icon);
        if (icon!=null)
            iconView.setImageBitmap(BitmapFactory.decodeFile(icon));
        else iconView.setImageResource(R.drawable.bug_pack_icon);

        TextView packdescription = (TextView)findViewById(R.id.details_description);
        CollapsingToolbarLayout toolbarLayout = (CollapsingToolbarLayout)findViewById(R.id.details_toolbar_layout);


        toolbarLayout.setExpandedTitleMarginStart(235);
        toolbarLayout.setTitle(name);
        if (!description.equals("")) {
            packdescription.setText(description);
            toolbarLayout.setExpandedTitleMarginBottom(140);
        }
        else {
            packdescription.setVisibility(GONE);
            toolbarLayout.setExpandedTitleMarginBottom(100);
        }
    }

    public void initToolbar(){
        Toolbar toolbar = (Toolbar)findViewById(R.id.details_toolbar);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar!=null){
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    private ProgressBar progress;
    public void showLoading(){
        progress.setVisibility(View.VISIBLE);
    }
    public void hideLoading(){
        progress.setVisibility(View.GONE);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        int id = item.getItemId();
        switch (id){
            case android.R.id.home:
                onBackPressed();
                break;
            default:break;
        }
        return true;
    }

    @Override
    public void onBackPressed() {
        fab.setVisibility(View.INVISIBLE);

        Intent intent = new Intent();
        intent.putExtra("isDataChanged",isDataChanged);
        setResult(RESULT_OK,intent);

        super.onBackPressed();
    }
}
