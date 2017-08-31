package com.zhufuc.pctope.Activities;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.BottomSheetDialog;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.zhufuc.pctope.Collectors.ErrorsCollector;
import com.zhufuc.pctope.R;
import com.zhufuc.pctope.Utils.CompressImage;
import com.zhufuc.pctope.Utils.DeleteFolder;
import com.zhufuc.pctope.Utils.FindFile;
import com.zhufuc.pctope.Utils.GetPathFromUri4kitkat;
import com.zhufuc.pctope.Utils.PackVersionDecisions;
import com.zhufuc.pctope.Utils.TextureCompat;
import com.zhufuc.pctope.Utils.TextureConversionUtils;

import net.lingala.zip4j.exception.ZipException;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.DecimalFormat;
import java.text.Format;
import java.util.ArrayList;
import java.util.Objects;
import java.util.UUID;
import java.util.logging.Handler;
import java.util.logging.LogRecord;

import static com.zhufuc.pctope.Utils.TextureCompat.brokenPC;


public class ConversionActivity extends BaseActivity {

    final Intent finishIntent = new Intent();

    private void MakeErrorDialog(final String errorString){
        //make up a error dialog
        AlertDialog.Builder error_dialog = new AlertDialog.Builder(ConversionActivity.this);
        error_dialog.setTitle(R.string.error);
        error_dialog.setMessage(ConversionActivity.this.getString(R.string.error_dialog)+errorString);
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

    Boolean skipUnzip = false,isPreFinished = false;
    private TextInputEditText name ,description;
    private String packname,packdescription,file;
    private LinearLayout unzipping_tip = null
            ,cards = null
            ,error_layout = null;

    private TextureConversionUtils conversion;

    protected void onCreate(Bundle savedInstanceState) {
        //Preload
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conversion);
        setResult(RESULT_OK,finishIntent);//I don't need to use it
        initViews();
        getIntentInfo();

        //Main
        conversion = new TextureConversionUtils(file,this);
        conversion.skipUnzip = skipUnzip;
        conversion.setOnUncompressListener(new TextureConversionUtils.OnUncompressListener() {
            @Override
            public void onPreUncompress() {
                unzipping_tip.setVisibility(View.VISIBLE);
                cards.setVisibility(View.GONE);
                error_layout.setVisibility(View.GONE);
            }

            @Override
            public void inUncompressing() {

            }

            @Override
            public void onPostUncompress(boolean result, String version) {
                unzipping_tip.setVisibility(View.GONE);
                cards.setVisibility(View.VISIBLE);
                error_layout.setVisibility(View.GONE);
                loadIcon();
                isPreFinished = true;
                if (result) doOnSuccess();
                else doOnFail();
            }
        });
        conversion.setOnCrashListener(new TextureConversionUtils.OnCrashListener() {
            @Override
            public void onCrash(final String errorContent) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        unzipping_tip.setVisibility(View.GONE);
                        cards.setVisibility(View.GONE);
                        error_layout.setVisibility(View.VISIBLE);
                        MakeErrorDialog(errorContent);
                    }
                });

            }
        });
        conversion.setConversionChangeListener(new TextureConversionUtils.ConversionChangeListener() {
            ProgressDialog alertDialog = null;

            @Override
            public void inDoingVersionDecisions() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        alertDialog = new ProgressDialog(ConversionActivity.this);
                        alertDialog.setTitle(getString(R.string.loading));
                        alertDialog.setMessage(getString(R.string.please_wait));
                        alertDialog.show();
                    }
                });
            }

            @Override
            public void inDoingImageCompressions(final String whatsBeingCompressed) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        alertDialog.setTitle(getString(R.string.progress_compressing_title));
                        alertDialog.setMessage(whatsBeingCompressed);
                    }
                });

            }

            @Override
            public void inDoingImageColorTurning() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        alertDialog.setTitle(R.string.turing_image_color_black_into_transparent);
                        alertDialog.setMessage(getString(R.string.please_wait));
                    }
                });
            }

            @Override
            public void inDoingJSONWriting() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        alertDialog.setMessage(getString(R.string.do_final_step));
                        alertDialog.setTitle(getString(R.string.progress_writing_json));
                        Log.i("Information On UI","Showing json writing dialog...");
                    }
                });

            }

            @Override
            public void onDone() {
                finishIntent.putExtra("Status_return",true);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        alertDialog.setMessage(getResources().getString(R.string.completed));
                    }
                });
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                finish();
            }
        });

        //Overwrite dialog
        if (new File(conversion.path).exists()&&!skipUnzip){
            AlertDialog.Builder dialog = new AlertDialog.Builder(ConversionActivity.this);
            dialog.setTitle(R.string.overwrite_title);
            dialog.setMessage(R.string.overwrite_content);
            dialog.setCancelable(false);
            dialog.setNegativeButton(R.string.skip, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    skipUnzip = true;
                    conversion.skipUnzip = skipUnzip;
                    conversion.UncompressPack();
                }
            });
            dialog.setPositiveButton(R.string.overwrite, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    skipUnzip = false;
                    conversion.skipUnzip = skipUnzip;
                    conversion.UncompressPack();
                }
            });
            dialog.show();
        }
        else conversion.UncompressPack();

    }

    private void getIntentInfo(){
        Intent intent = getIntent();
        skipUnzip = intent.getBooleanExtra("willSkipUnzipping",false);
        file = intent.getStringExtra("filePath");
    }

    private void initViews(){
        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        final CollapsingToolbarLayout collapsingbar = (CollapsingToolbarLayout) findViewById(R.id.collapsing_bar);
        unzipping_tip  = (LinearLayout) findViewById(R.id.unzipping_tip);
        cards = (LinearLayout) findViewById(R.id.cards_grid);
        error_layout = (LinearLayout)findViewById(R.id.error_layout);
        name = (TextInputEditText) findViewById(R.id.pname);
        description = (TextInputEditText) findViewById(R.id.pdescription);


        //set back icon
        setSupportActionBar(toolbar);
        final ActionBar actionbar = getSupportActionBar();
        if(actionbar!=null){
            actionbar.setDisplayHomeAsUpEnabled(true);
        }

        //set project name on changed listener
        packname = getString(R.string.project_unnamed);
        packname = doDestFixing(packname);
        collapsingbar.setTitle(packname);

        name.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if(!charSequence.toString().equals("")){
                    packname = charSequence.toString();
                    packname = doDestFixing(packname);
                    collapsingbar.setTitle(packname);
                }
                else{
                    packname = getResources().getString(R.string.project_unnamed);
                    packname = doDestFixing(packname);
                    collapsingbar.setTitle(packname);
                }

            }
            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        //for FAB
        final FloatingActionButton button_finish = (FloatingActionButton)findViewById(R.id.finish);
        final FloatingActionButton button_finish_bottom = (FloatingActionButton)findViewById(R.id.finishBottom);
        button_finish_bottom.hide();
        AppBarLayout appBarLayout = (AppBarLayout)findViewById(R.id.appBar);
        appBarLayout.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                if (button_finish.isShown()) button_finish_bottom.hide();
                else button_finish_bottom.show();
                if (Math.abs(verticalOffset)>=appBarLayout.getTotalScrollRange()-toolbar.getScrollBarSize()) button_finish_bottom.show();
            }
        });
        View.OnClickListener FABListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                doFinishButtonDoes(view);
            }
        };
        button_finish.setOnClickListener(FABListener);
        button_finish_bottom.setOnClickListener(FABListener);
    }

    private void doFinishButtonDoes(View v){
        if (isPreFinished) {

            packdescription = description.getText().toString();

            name.setEnabled(false);
            description.setEnabled(false);

            conversion.compressFinalSize = compressFinalSize;
            new Thread(new Runnable() {
                @Override
                public void run() {
                    conversion.doConverting(packname, packdescription);
                }
            }).start();

        }
        else
            Snackbar.make(v,R.string.unclickable_unzipping,Snackbar.LENGTH_LONG).show();
    }

    public void loadIcon(){
        ImageView icon = (ImageView) findViewById(R.id.img_card_icon);
        Bitmap bitmap = conversion.getIcon();
        if (bitmap!=null){
            icon.setImageBitmap(bitmap);
        }
        else {
            FloatingActionButton finishBottom = (FloatingActionButton)findViewById(R.id.finishBottom);
            Snackbar.make(finishBottom,R.string.pack_icon_not_found,Snackbar.LENGTH_INDEFINITE)
                    .setAction(R.string.ok, new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent choose = new Intent(Intent.ACTION_GET_CONTENT);
                            choose.setType("image/*");
                            choose.addCategory(Intent.CATEGORY_OPENABLE);
                            startActivityForResult(choose, 0);
                        }
                    })
                    .show();
        }

    }

    private int compressSize,compressFinalSize;
    private void doOnSuccess(){
        //Set layouts
        //==>define
        final LinearLayout unzipping_tip = (LinearLayout) findViewById(R.id.unzipping_tip);
        final LinearLayout cards = (LinearLayout) findViewById(R.id.cards_grid);
        final LinearLayout error_layout = (LinearLayout)findViewById(R.id.error_layout);
        final CardView PDI_card = (CardView)findViewById(R.id.pdi_card);
        //settings
        cards.setVisibility(View.VISIBLE);
        unzipping_tip.setVisibility(View.GONE);
        error_layout.setVisibility(View.GONE);

        isPreFinished = true;

        Animation animation = AnimationUtils.loadAnimation(ConversionActivity.this, R.anim.cards_show);
        cards.startAnimation(animation);
        //Load icon
        loadIcon();
        //Set icon editor
        final ImageView edit = (ImageView) findViewById(R.id.card_icon_edit);
        edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent choose = new Intent(Intent.ACTION_GET_CONTENT);
                choose.setType("image/*");
                choose.addCategory(Intent.CATEGORY_OPENABLE);
                startActivityForResult(choose, 0);
            }
        });
        //Set PDI CardView layout
        TextView PackType = (TextView)findViewById(R.id.info_pack_type);
        TextView PackInMC = (TextView)findViewById(R.id.info_pack_in_mc_ver);
        String type = getResources().getString(R.string.info_pack_type);
        switch (conversion.VerStr){
            case TextureCompat.fullPE:type += getResources().getString(R.string.type_fullPE);break;
            case TextureCompat.fullPC:type += getResources().getString(R.string.type_fullPC);break;
            case TextureCompat.brokenPE:type += getResources().getString(R.string.type_brokenPE);break;
            case brokenPC:type += getResources().getString(R.string.type_brokenPC);break;
            default:type = null;
        }
        PackType.setText(type);

        String ver = conversion.decisions.getInMinecraftVer(PackInMC);
        if (ver == null)
            ver = getResources().getString(R.string.info_file_not_exists);

        PackInMC.setText(getResources().getString(R.string.info_pack_in_mc_ver) + ver);

        ImageView supportOrNot = (ImageView)findViewById(R.id.support_or_not_icon);
        if (ver.equals(getResources().getString(R.string.type_before_1_9))){
            supportOrNot.setImageResource(R.drawable.close_circle);
        }

        //Set Compression
        File image = null;
        String baseFrom = null;
        if (conversion.VerStr.equals(TextureCompat.fullPC)||conversion.VerStr.equals(brokenPC)) baseFrom = conversion.path+"/assets/minecraft/textures";
        else baseFrom = conversion.path+"/textures";
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
                final BottomSheetDialog dialog = new BottomSheetDialog(ConversionActivity.this);


                final View dialogView = getLayoutInflater().inflate(R.layout.compression_dialog,null);

                dialog.setContentView(dialogView);


                BitmapFactory.Options optionsBitmap = new BitmapFactory.Options();
                optionsBitmap.inSampleSize = 1;

                final Bitmap bitmap = BitmapFactory.decodeFile(imageLocation,optionsBitmap);
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
                            Toast.makeText(ConversionActivity.this,R.string.compression_alert,Toast.LENGTH_SHORT).show();
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

    //on Result
    Bitmap iconMap = null;
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        if(resultCode == Activity.RESULT_OK)
            if(requestCode == 0){
                Uri uri = data.getData();
                final String fileLocation = GetPathFromUri4kitkat.getPath(ConversionActivity.this,uri);

                iconMap = BitmapFactory.decodeFile(fileLocation);
                if (CompressImage.testBitmap(512,512,iconMap)){
                    AlertDialog.Builder builder = new AlertDialog.Builder(ConversionActivity.this);
                    builder.setTitle(R.string.icon_edit_high_res_title);
                    builder.setMessage(R.string.icon_edit_high_res_subtitle);
                    builder.setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            float scale = 1,scaleHeight = 512f/iconMap.getHeight(),scaleWidth = 512f/iconMap.getWidth();
                            if (scaleHeight<=scaleWidth) scale = scaleHeight;
                            else scale = scaleWidth;

                            iconMap = CompressImage.getBitmap(iconMap,scale);

                            conversion.setIcon(iconMap);
                            loadIcon();


                        }
                    });
                    builder.setNegativeButton(R.string.thanks, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            conversion.setIcon(iconMap);
                            loadIcon();
                        }
                    });
                    builder.show();
                }
                else{
                    conversion.setIcon(iconMap);
                    loadIcon();
                }
            }
    }


    public void doOnFail(){
        //Delete not pack
        //==>define
        final LinearLayout unzipping_tip = (LinearLayout) findViewById(R.id.unzipping_tip);
        final LinearLayout cards = (LinearLayout) findViewById(R.id.cards_grid);
        final LinearLayout error_layout = (LinearLayout)findViewById(R.id.error_layout);
        //settings
        cards.setVisibility(View.GONE);
        unzipping_tip.setVisibility(View.GONE);
        error_layout.setVisibility(View.VISIBLE);

        final TextView text = (TextView)findViewById(R.id.error_layout_text);
        text.setText(text.getText()+ConversionActivity.this.getString(R.string.not_pack));
        final File notpack = new File(conversion.path);
        Log.d("status","Deleting "+notpack.toString());
        class deleteTask extends AsyncTask<Void , Integer , Boolean>{
            @Override
            protected Boolean doInBackground(Void... voids) {
                Snackbar.make(text,R.string.deleting,Snackbar.LENGTH_LONG).show();
                Boolean r = false;
                if (notpack.exists())
                    r = DeleteFolder.Delete(notpack.toString());
                else
                    r = true;


                if(r)
                    Snackbar.make(text,R.string.deleted_completed,Snackbar.LENGTH_LONG).show();
                else
                    Snackbar.make(text,R.string.deteted_failed,Snackbar.LENGTH_LONG).show();
                try {
                    Thread.sleep(1400);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                finishIntent.putExtra("Status_return",false);
                finish();
                return r;
            }
        }
        new deleteTask().execute();
    }

    //Set BACK Icon
    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        switch (item.getItemId()){
            case android.R.id.home:
                finishIntent.putExtra("Status_return",false);
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private String doDestFixing(final String old){
        File dest = new File(Environment.getExternalStorageDirectory()+"/games/com.mojang/resource_packs/" + old);
        int plus = 0;
        String str = old;
        while (dest.exists()){
            plus++;
            str = old+plus;
            dest = new File(Environment.getExternalStorageDirectory() + "/games/com.mojang/resource_packs/" + str);
        }
        return str;
    }
}
