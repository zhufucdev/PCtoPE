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
import android.net.Uri;
import android.os.AsyncTask;
import android.support.annotation.BoolRes;
import android.support.design.widget.BottomSheetDialog;
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
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.zhufuc.pctope.Adapters.Textures;
import com.zhufuc.pctope.R;
import com.zhufuc.pctope.Utils.CompressImage;
import com.zhufuc.pctope.Utils.FindFile;
import com.zhufuc.pctope.Utils.GetPathFromUri4kitkat;
import com.zhufuc.pctope.Utils.TextureCompat;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.channels.FileChannel;

import static android.view.View.GONE;
import static com.zhufuc.pctope.Utils.TextureCompat.brokenPC;
import static com.zhufuc.pctope.Utils.TextureCompat.fullPC;

public class DetailsActivity extends BaseActivity {

    private String name,description,version,icon,path;
    private Textures textures = null;
    private Textures.Edit textureEditor;
    private BigDecimal size;

    private NestedScrollView cards;
    private FloatingActionButton fab;

    private final String fullPE = "Found:full PE pack.";
    private int compressSize = 0,compressFinalSize = 0;

    private boolean isDataChanged = false;

    //Utils
    public long getFolderTotalSize(String path) {
        File[] files = new File(path).listFiles();
        long size = 0;
        for (File f:files)
            if (f.exists()){
                if (f.isFile()){
                    FileChannel fc = null;
                    FileInputStream inputStream = null;
                    try {
                        inputStream = new FileInputStream(f);
                        fc = inputStream.getChannel();
                        size += fc.size();
                        inputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                else size += getFolderTotalSize(f.getPath());
            }

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

            dialog.setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    String setName = editName.getText().toString(),setDescription = editDescription.getText().toString();
                    if (!setName.equals(name) || !setDescription.equals(description)) {
                        textureEditor.changeNameAndDescription(setName,setDescription);
                        isDataChanged = true;
                        loadDetailedInfo();
                    }
                }
            });

            dialog.setNeutralButton(R.string.icon_edit, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                    intent.setType("image/*");
                    startActivityForResult(intent,0);
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

        initToolbar();

        loadDetailedInfo();

    }

    public void updateInformation(){
        if (textures!=null)
            textures = null;
        if (textureEditor != null)
            textureEditor = null;
        textures = new Textures(new File(path));
        textureEditor = new Textures.Edit(path);

        version = textures.getVersion();
        name = textures.getName();
        description = textures.getDescription();

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
                if (description==null) {
                    description = "";
                }

                try {
                    Thread.sleep(600);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                return true;

            }

            @Override
            public void onPostExecute(Boolean result){
                initBasicTitles();
                loadViews();
                hideLoading();
                initOperationalCards();
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
        if (name!=null)
            toolbarLayout.setTitle(name);
        else
            toolbarLayout.setTitle(getString(R.string.unable_to_get_name));

        description = description==null? "":description;
        if (!description.equals("")) {
            packdescription.setVisibility(View.VISIBLE);
            packdescription.setText(description);
            toolbarLayout.setExpandedTitleMarginBottom(140);
        }
        else {
            packdescription.setVisibility(View.INVISIBLE);
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

    public void initOperationalCards(){
        String VerStr = textures.getVersion();

        //Set Compression
        File image = null;
        String baseFrom = null;
        if (VerStr.equals(TextureCompat.fullPC)||VerStr.equals(TextureCompat.brokenPC)) baseFrom = path+"/assets/minecraft/textures";
        else baseFrom = path+"/textures";
        //grass >> sword >> never mind
        if ((image = FindFile.withKeywordOnce("grass_side.png",baseFrom)) == null) {
            if ((image = FindFile.withKeywordOnce("iron_sword.png", baseFrom)) == null)
                image = FindFile.withKeywordOnce(".png", baseFrom);
        }
        final String imageLocation = image.getPath();


        //set listener
        final CardView compress = (CardView)findViewById(R.id.compression_card);


        compress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final BottomSheetDialog dialog = new BottomSheetDialog(DetailsActivity.this);


                final View dialogView = getLayoutInflater().inflate(R.layout.compression_dialog,null);

                dialog.setContentView(dialogView);

                final Bitmap bitmap = BitmapFactory.decodeFile(imageLocation);
                final Button confirm = (Button)dialogView.findViewById(R.id.compression_button_confirm);

                loadDialogLayout(dialogView,bitmap);

                Spinner spinner = (Spinner)dialogView.findViewById(R.id.compression_spinner);
                spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

                    @Override
                    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                        String[] options = getResources().getStringArray(R.array.compression_options);

                        switch (options[i]){
                            case "8x":compressSize = 8;break;
                            case "16x":compressSize = 16;break;
                            case "32x":compressSize = 32;break;
                            case "64x":compressSize = 64;break;
                            case "128x":compressSize = 128;break;
                            case "256x":compressSize = 256;break;
                            case "512x":compressSize = 512;break;
                            default:compressSize = 0;break;
                        }
                        if (compressSize != 0){
                            loadDialogLayout(dialogView, CompressImage.getBitmap(bitmap,compressSize,compressSize));
                        }
                        else loadDialogLayout(dialogView, bitmap);

                        if (compressSize > bitmap.getWidth() || compressSize > bitmap.getHeight()){
                            confirm.setEnabled(false);
                            confirm.setTextColor(getResources().getColor(R.color.grey_primary));
                            Toast.makeText(DetailsActivity.this,R.string.compression_alert,Toast.LENGTH_SHORT).show();
                        }
                        else {
                            confirm.setEnabled(true);
                            confirm.setTextColor(getResources().getColor(R.color.colorAccent));
                        }
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> adapterView) {
                        compressSize = 0;
                    }
                });

                dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialogInterface) {
                        compressSize = compressFinalSize;
                    }
                });

                dialog.show();


                confirm.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        compressFinalSize = compressSize;
                        dialog.dismiss();
                        if (compressFinalSize==0) {
                            return;
                        }

                        final ProgressDialog progressDialog = new ProgressDialog(DetailsActivity.this);
                        progressDialog.setTitle(R.string.progress_compressing_title);
                        progressDialog.setMessage(getString(R.string.please_wait));
                        progressDialog.show();
                        textureEditor.setOnCompressionProgressChangeListener(new Textures.Edit.CompressionProgressChangeListener() {
                            @Override
                            public void OnProgressChangeListener(final String whatsBeingCompressed, boolean isDone) {
                                if (!isDone){
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            progressDialog.setTitle(R.string.progress_compressing_title);
                                            progressDialog.setMessage(whatsBeingCompressed);
                                        }
                                    });
                                }
                                else {
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            progressDialog.dismiss();
                                            loadDetailedInfo();
                                        }
                                    });

                                }
                            }
                        });
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                textureEditor.compressImages(compressFinalSize);
                            }
                        }).start();


                    }
                });
            }
        });
    }

    private void loadDialogLayout(final View dialogView,final Bitmap bitmap){

        Spinner spinner = (Spinner)dialogView.findViewById(R.id.compression_spinner);
        if (compressSize!=0){
            switch (compressSize){
                case 8:spinner.setSelection(1);break;
                case 16:spinner.setSelection(2);break;
                case 32:spinner.setSelection(3);break;
                case 64:spinner.setSelection(4);break;
                case 128:spinner.setSelection(5);break;
                case 256:spinner.setSelection(6);break;
                case 512:spinner.setSelection(7);break;
                default:spinner.setSelection(0);break;
            }
        }
        else spinner.setSelection(0,true);


        //set view
        ImageView preview = (ImageView)dialogView.findViewById(R.id.compression_image);

        preview.setImageBitmap(bitmap);


        //set text
        TextView width_text = (TextView)dialogView.findViewById(R.id.compression_width_text);
        TextView height_text = (TextView)dialogView.findViewById(R.id.compression_height_text);
        width_text.setText(String.valueOf(bitmap.getWidth()));
        height_text.setText(String.valueOf(bitmap.getHeight()));
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

    //Activity Result
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data){
        if (resultCode == RESULT_OK)
            if (requestCode == 0){
                Uri uri = data.getData();
                final Bitmap iconMap = BitmapFactory.decodeFile(GetPathFromUri4kitkat.getPath(DetailsActivity.this,uri));
                if (CompressImage.testBitmap(512,512,iconMap)){
                    AlertDialog.Builder builder = new AlertDialog.Builder(DetailsActivity.this);
                    builder.setTitle(R.string.icon_edit_high_res_title);
                    builder.setMessage(R.string.icon_edit_high_res_subtitle);
                    builder.setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            try {
                                float scale = 1,scaleHeight = 512f/iconMap.getHeight(),scaleWidth = 512f/iconMap.getWidth();
                                if (scaleHeight<=scaleWidth) scale = scaleHeight;
                                else scale = scaleWidth;
                                textureEditor.iconEdit(CompressImage.getBitmap(iconMap,scale));
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            loadDetailedInfo();
                        }
                    });
                    builder.setNegativeButton(R.string.thanks, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            try {
                                textureEditor.iconEdit(iconMap);
                            }catch (IOException e){
                                e.printStackTrace();
                            }
                            loadDetailedInfo();
                        }
                    });
                    builder.show();
                }
                else {
                    try {
                        textureEditor.iconEdit(iconMap);
                    }catch (IOException e){
                        e.printStackTrace();
                    }
                    loadDetailedInfo();
                }

                isDataChanged = true;
            }
    }
}
