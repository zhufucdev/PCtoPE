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




public class ConversionActivity extends BaseActivity {

    final Intent finishIntent = new Intent();

    public String makeSpace(int i){
        String spaces="";
        for (int j=0;j<=i;j++) spaces+=" ";
        return spaces;
    }

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

    private boolean CopyFileOnSD(String fileFrom, String fileTo){
        File from = new File(fileFrom);
        File to = new File(fileTo);
        if (from.exists()){
            if(to.exists()) {
                to.delete();
                try {
                    to.createNewFile();
                } catch (IOException e) {
                    ErrorsCollector.putError(e.toString(),0);
                    e.printStackTrace();
                    return false;
                }
            }
            try {
                InputStream inputStream = new FileInputStream(fileFrom);
                OutputStream outputStream = new FileOutputStream(fileTo);
                byte[] buffer = new byte[1444];
                int bytesum = 0, byteread = 0;
                while ((byteread = inputStream.read(buffer))!=-1){
                    bytesum += byteread;
                    outputStream.write(buffer, 0, byteread);
                }
                inputStream.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                ErrorsCollector.putError(e.toString(),0);
                return false;
            } catch (IOException e) {
                e.printStackTrace();
                ErrorsCollector.putError(e.toString(),0);
                return false;
            }
            return true;
        }
        else {
            ErrorsCollector.putError("File not found.",0);
            return false;
        }

    }

    //==>define
    String path,packname,packdescription;
    boolean isPreFinished = false;
    private TextInputEditText name;
    private TextInputEditText description;

    private final String fullPC = "Found:full PC pack.";
    private final String brokenPE = "Found:broken PE pack.";
    private final String brokenPC = "Found:broken PC pack.";
    private final String fullPE = "Found:full PE pack.";

    PackVersionDecisions decisions;
    private String VerStr = null;
    private void doVersionDecisions(){
        if (VerStr.equals(brokenPE)||VerStr.equals(fullPE)){
            onPEDecisions();
        }
        else if (VerStr.equals(brokenPC)||VerStr.equals(fullPC)){
            onPcDecisions();
        }
    }


    //如题，遍历所有子png文件
    static ArrayList<String> filelist = new ArrayList<String>();
    public static ArrayList<String> ListFiles(File path) {
        File files[] = path.listFiles();
        if(files==null) return null;
        for (File f:files){
            if(f.isDirectory()){
                ListFiles(f);
            }
            else{
                String fileindir = "textures",strnow = f.toString();
                int j=0,i;
                for (i=strnow.length()-1;i>=0;i--){
                    if (strnow.charAt(i)=='/') j++;
                    if (j>=2) break;
                }
                fileindir+=strnow.substring(i);
                String lastStr = fileindir.substring(fileindir.lastIndexOf('.'));
                if(Objects.equals(lastStr, ".png")){
                    fileindir=fileindir.substring(0,fileindir.indexOf('.'));
                    Log.d("files","NO."+filelist.size()+":"+fileindir+' '+lastStr);
                    filelist.add(fileindir);
                }
            }
        }
        return filelist;
    }

    public void doPCSpecialResourcesDecisions(){
        //需要特殊处理的贴图包括:
        //滞留药水 无法解决
        //怪物蛋手持贴图 无法解决
        //钟 无法解决
        //指南针 已解决
        //船手持贴图 无法解决
        //北极熊实体 已解决
        //豹猫 已解决
        //僵尸贴图错误 已解决
        //僵尸猪人实体 已解决
        //剥皮者贴图错误 已解决
        //卫道士实体
        //唤魔者实体
        //威克斯
        //箱子手持贴图及大箱子实体 无法解决<>已解决
        //活塞 无法解决

        File[] FROM = {
                new File(path+"/textures/entity/chest/normal_double.png"),
                new File(path+"/textures/items/dragon_breath.png"),
                new File(path+"/textures/entity/bear/polarbear.png"),
                new File(path+"/textures/entity/cat/black.png"),
                new File(path+"/textures/entity/zombie_pigman.png")
        },
        TO = {
                new File(path+"/textures/entity/chest/double_normal.png"),
                new File(path+"/textures/items/dragons_breath.png"),
                new File(path+"/textures/entity/polarbear.png"),
                new File(path+"/textures/entity/cat/blackcat.png"),
                new File(path+"/textures/entity/pig/pigzombie.png")
        };
        for (int i = 0;i<TO.length;i++){
            FROM[i].renameTo(TO[i]);
        }


        //For compasses
        if (new File(path+"/textures/items/compass_00.png").exists()){

            int imageHeight = 0;

            ArrayList<Bitmap> compasses = new ArrayList<>();
            for (int i = 0;i <= 31;i++){
                Format format = new DecimalFormat("00");
                File image = new File(path+"/textures/items/compass_"+format.format(i)+".png");
                if (image.exists()) {
                    Bitmap now = BitmapFactory.decodeFile(image.getPath());
                    compasses.add(now);
                    imageHeight += now.getHeight();
                    image.delete();
                }
            }

            Bitmap bitmap = Bitmap.createBitmap(compasses.get(0).getWidth(),imageHeight,compasses.get(0).getConfig());
            Canvas canvas = new Canvas(bitmap);
            canvas.drawBitmap(compasses.get(0),new Matrix(),null);

            for (int i=1;i<compasses.size();i++){
                canvas.drawBitmap(compasses.get(i),0,compasses.get(i-1).getHeight()*i,null);
            }

            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG,100,byteArrayOutputStream);
            try {
                FileOutputStream outputStream = new FileOutputStream(path+"/textures/items/compass_atlas.png");
                outputStream.write(byteArrayOutputStream.toByteArray());

                outputStream.flush();
                outputStream.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        //For zombies
        File[] images = {new File(path+"/textures/entity/zombie/zombie.png"),new File(path+"/textures/entity/zombie/husk.png"),new File(path+"/textures/entity/pig/pigzombie.png")};
        for (int i = 0;i < images.length;i++){
            if (images[i].exists()){
                Bitmap zombieImage = BitmapFactory.decodeFile(images[i].getPath());

                if (zombieImage.getHeight() == zombieImage.getWidth()){
                    Bitmap bitmap = Bitmap.createBitmap(zombieImage,0,0,zombieImage.getWidth(),zombieImage.getHeight()/2);

                    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                    bitmap.compress(Bitmap.CompressFormat.PNG,100,byteArrayOutputStream);
                    try {
                        FileOutputStream outputStream = new FileOutputStream(images[i]);
                        outputStream.write(byteArrayOutputStream.toByteArray());
                        outputStream.flush();
                        outputStream.close();
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

            }
        }
    }

    public void onPcDecisions() {
        File icon = new File(path + "/pack.png");
        File iconPE = new File(path + "/pack_icon.png");
        icon.renameTo(iconPE);//Rename icon to PE
        File texture = new File(path + "/assets/minecraft/textures");
        File texturePE = new File(path + "/textures");
        texture.renameTo(texturePE);//Move textures folder

        //Delete something that we don't need
        new File(path+"/pack.mcmeta").delete();
        DeleteFolder.Delete(path+"/assets");

        doPCSpecialResourcesDecisions();

        ArrayList<String> files = ListFiles(new File(path));
        int fileslength = files.size()-1;
        Log.d("files", "Now we have " + fileslength + " files...Writing to textures_list.json...");
        Log.d("files","The first(0) one is "+files.get(0));
        Log.d("files","The final("+fileslength+") one is "+files.get(fileslength));
        File textures_list = new File(path + "/textures/textures_list.json");
        if (fileslength != 0) {
            FileOutputStream out = null;
            try {
                textures_list.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                out = new FileOutputStream(textures_list.getPath());
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            try {
                out.write(("[" + System.getProperty("line.separator")).getBytes());
            } catch (IOException e) {
                e.printStackTrace();
            }
            //Write
            for (int i = 0; i <= fileslength; i++){
                try {
                    String fileNow = files.get(i);
                    out.write(("\"" + fileNow + "\"" + "," + System.getProperty("line.separator")).getBytes());
                    Log.d("files","Now i is "+i);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            try {
                out.write("]".getBytes());
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        
    }


    public void onPEDecisions(){
        File[] JSONs = new File(path+"/textures/").listFiles();
        for (File f:JSONs){
            String n = f.getPath();
            if (n.substring(n.lastIndexOf('.'),n.length())==".json")
                f.delete();
        }
    }

    public String doJsonFixing(InputStream data,int SearchFrom){
        ByteArrayOutputStream bais = new ByteArrayOutputStream();
        int temp;
        try {
            while ((temp=data.read())!=-1){
                bais.write(temp);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        String terrainTxt = bais.toString();
        ArrayList<String> terrainText = new ArrayList<String>();
        int j=0;

        for (int i=0;i<terrainTxt.length();i++){
            if (terrainTxt.charAt(i)=='\n'){
                String line = terrainTxt.substring(j,i+1);
                terrainText.add(line);
                j=i+1;
            }
        }

        for (int i=SearchFrom;i<terrainText.size();i++) {
            String str = terrainText.get(i);
            for (int a=0;a<str.length()-9;a++){
                if (Objects.equals(str.substring(a, a + 9), "textures/")){
                    String texturePath;int g;
                    for (g=a+1;g<str.length();g++){
                        if (Objects.equals(str.charAt(g),'\"'))
                            break;
                    }
                    texturePath = str.substring(a,g);
                    File testPath = new File(path+"/"+texturePath+".png");
                    if (!testPath.exists()){
                        int x=i,y=i,t=0;
                        Boolean isFound = false;
                        while (x>=0){
                            for (t=0;t<terrainText.get(x).length();t++)
                                if((terrainText.get(x).charAt(t)=='{')){
                                    isFound = true;
                                    break;
                                }
                            if (isFound) break;
                            else x--;
                        }
                        isFound = false;
                        while (y<terrainText.size()){
                            for (t=0;t<terrainText.get(y).length()-1;t++)
                                if(Objects.equals(terrainText.get(y).charAt(t),'}')){
                                    isFound = true;
                                    break;
                                }
                            if (isFound) break;
                            else y++;
                        }

                        for (int b=1;b<=y-x+1;b++){
                            terrainText.remove(x);
                        }
                    }
                    break;
                }

            }
        }
        String FinalText = "";
        for (int i=0;i<terrainText.size();i++) FinalText+=terrainText.get(i);
        return FinalText;
    }
    
    public void doJSONWriting() throws FileNotFoundException {
        //==>define
        //for basic information
        Resources raw = getResources();
        InputStream data[] = {
                 raw.openRawResource(R.raw.items_client)
                ,raw.openRawResource(R.raw.blocks)
                ,raw.openRawResource(R.raw.flipbook_textures)
                ,raw.openRawResource(R.raw.item_texture)
                ,raw.openRawResource(R.raw.terrain_texture)};
        byte[] bt = new byte[1444];
        FileOutputStream pathes[] = {
                 new FileOutputStream(path+"/items_client.json")
                ,new FileOutputStream(path+"/blocks.json") };

        for(int i=0;i<pathes.length;i++)
            try {
                int bytesum = 0, byteread = 0;
                while ((byteread=data[i].read(bt))!=-1){
                    bytesum += byteread;
                    pathes[i].write(bt, 0, byteread);
                }
                pathes[i].close();
            } catch (IOException e) {
                MakeErrorDialog(e.toString());
                e.printStackTrace();
            }

        //for terrain block textures
        String textBefore = "{"+System.getProperty("line.separator")+makeSpace(4)+"\"resource_pack_name\":"+"\""+packname+"\""+System.getProperty("line.separator");
        FileOutputStream terrainOut = new FileOutputStream(path+"/textures/terrain_texture.json");
        try {
            terrainOut.write((textBefore).getBytes());
            terrainOut.write(doJsonFixing(data[4],77).getBytes());
        } catch (IOException e) {
            MakeErrorDialog(e.toString());
            e.printStackTrace();
        }

        //for item texture file

        FileOutputStream itemOut = new FileOutputStream(path+"/textures/item_texture.json");
        Boolean isCreated = true;
        String[] temp = {path+"/textures/item_texture.json",path+"/textures/flipbook_textures.json",path+"/manifest.json"};
        for (int i=0;i<temp.length;i++){
            File t = new File(temp[i]);
            if (!t.exists())
                try {
                    if(!t.createNewFile())
                        isCreated = false;
                } catch (IOException e) {
                    MakeErrorDialog(e.toString());
                    e.printStackTrace();
                }
        }
        if (isCreated){
            try {
                itemOut.write(textBefore.getBytes());
                itemOut.write(doJsonFixing(data[3],5).getBytes());
            } catch (IOException e1) {
                MakeErrorDialog(e1.toString());
                e1.printStackTrace();
            }

            //for flip book texture
            FileOutputStream flipOut = new FileOutputStream(path+"/textures/flipbook_textures.json");
            try {
                flipOut.write(doJsonFixing(data[2],2).getBytes());
            } catch (IOException e) {
                MakeErrorDialog(e.toString());
                e.printStackTrace();
            }

            //for manifest file
            FileOutputStream manifest = new FileOutputStream(path+"/manifest.json");
            String intro;
            intro="{"+System.getProperty("line.separator");
            intro+=makeSpace(2)+"\"format_version\": 1,"+System.getProperty("line.separator");
            intro+=makeSpace(2)+"\"header\": {"+System.getProperty("line.separator");
            intro+=makeSpace(4)+"\"description\": \""+packdescription+"\","+System.getProperty("line.separator");
            intro+=makeSpace(4)+"\"name\": \""+packname+"\","+System.getProperty("line.separator");
            String uuid = new UUID(12,4).randomUUID().toString();
            intro+=makeSpace(4)+"\"uuid\": \""+uuid+"\","+System.getProperty("line.separator");
            intro+=makeSpace(4)+"\"version\": [0, 0, 1]"+System.getProperty("line.separator");
            intro+=makeSpace(2)+"},"+System.getProperty("line.separator");
            intro+=makeSpace(2)+"\"modules\": ["+System.getProperty("line.separator");
            intro+=makeSpace(4)+"{"+System.getProperty("line.separator");
            intro+=makeSpace(6)+"\"description\": \""+packdescription+"\","+System.getProperty("line.separator");
            intro+=makeSpace(6)+"\"type\": \"resources\","+System.getProperty("line.separator");
            intro+=makeSpace(6)+"\"uuid\": \""+new UUID(12,4).randomUUID().toString()+"\",";
            intro+=makeSpace(6)+"\"version\" :[0, 0, 1]"+System.getProperty("line.separator");
            intro+=makeSpace(4)+"}"+System.getProperty("line.separator");
            intro+=makeSpace(2)+"]"+System.getProperty("line.separator");;
            intro+="}";
            try {
                manifest.write(intro.getBytes());
            } catch (IOException e2) {
                e2.printStackTrace();
            }
        }
        else MakeErrorDialog("Could not create JSON files.");

    }

    public void doMainInCompressing(final File n,final ProgressDialog alertDialog){
        if (n.isFile()) {
            //Show progress
            Log.d("compression","Compressing "+n);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    alertDialog.setTitle(getResources().getString(R.string.progress_compressing_title));
                    alertDialog.setMessage(n.getPath());
                }
            });


            String str = n.getPath();
            if (!str.substring(str.lastIndexOf("."),str.length()).equals(".png"))
                return;

            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inSampleSize = 1;
            Bitmap image = BitmapFactory.decodeFile(n.getPath(),options);
            //get compressed bitmap
            int compressHeight = compressFinalSize,compressWidth = compressFinalSize;
            if (image.getWidth()-image.getHeight()<-5){
                compressHeight = compressHeight*(image.getWidth()/compressFinalSize);
            }
            else if (image.getHeight()-image.getWidth()>5){
                compressWidth = compressWidth*(image.getHeight()/compressFinalSize);
            }
            Bitmap compressed = CompressImage.getBitmap(image,compressHeight,compressWidth);

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            compressed.compress(Bitmap.CompressFormat.PNG, 100, baos);//png

            n.delete();
            try {
                FileOutputStream outputStream = new FileOutputStream(n);
                outputStream.write(baos.toByteArray());

                outputStream.flush();
                outputStream.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                MakeErrorDialog(e.toString());
                e.printStackTrace();
            }
        }
    }

    public void doImageCompressions(ProgressDialog progressDialog){
        if (compressFinalSize == 0)
            return;
        //get images
        File[] items = new File(path+"/textures/items").listFiles(),blocks = new File(path+"/textures/blocks").listFiles();
        if (items!=null){
            for (File n:items)
                doMainInCompressing(n,progressDialog);
        }
        if (blocks!=null){
            for (File n:blocks)
                doMainInCompressing(n,progressDialog);
        }
    }
    
    public static void unzip(File zipFile, String dest, String passwd) throws ZipException, net.lingala.zip4j.exception.ZipException {
        net.lingala.zip4j.core.ZipFile zFile = new net.lingala.zip4j.core.ZipFile(zipFile); // 首先创建ZipFile指向磁盘上的.zip文件
        if (!zFile.isValidZipFile()) {   // 验证.zip文件是否合法，包括文件是否存在、是否为zip文件、是否被损坏等
            throw new ZipException("压缩文件不合法,可能被损坏.");
        }
        File destDir = new File(dest);// 解压目录
        if (destDir.exists()){
            DeleteFolder.Delete(dest);
        }
        destDir.mkdirs();
        if (zFile.isEncrypted()) {
            zFile.setPassword(passwd.toCharArray());  // 设置密码
        }
        zFile.extractAll(dest);      // 将文件抽出到解压目录(解压)
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

    Boolean skipUnzip = false;int compressSize = 0,compressFinalSize = 0;
    protected void onCreate(Bundle savedInstanceState) {
        //Test Area
        //*Code Something for test

        //End of Test

        //Preload
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conversion);

        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        final CollapsingToolbarLayout collapsingbar = (CollapsingToolbarLayout) findViewById(R.id.collapsing_bar);
        setResult(RESULT_OK,finishIntent);//I don't need to use it

        name = (TextInputEditText) findViewById(R.id.pname);
        description = (TextInputEditText) findViewById(R.id.pdescription);
        path = this.getExternalCacheDir().toString();

        //set back button
        setSupportActionBar(toolbar);
        final ActionBar actionbar = getSupportActionBar();
        if(actionbar!=null){
            actionbar.setDisplayHomeAsUpEnabled(true);
        }

        //set project name on changed listener
        packname = getResources().getString(R.string.project_unnamed);
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

        Intent intent = getIntent();
        skipUnzip = intent.getBooleanExtra("willSkipUnzipping",false);
        File fileT = null;
        if (!skipUnzip) {
            final String file = intent.getStringExtra("filePath");
            String fileName = "";
            //==>get file name
            for (int i = file.length() - 1; i >= 0; i--) {
                if (file.charAt(i) == '/') {
                    Log.d("files", "/ is at " + i);
                    for (int j = i + 1; j < file.length(); j++) {
                        fileName += file.charAt(j);
                    }
                    fileName = fileName.substring(0, fileName.lastIndexOf('.'));
                    break;
                }
            }
            path += "/" + fileName;
            Log.d("unzip", "We will unzip " + file + " to " + path);
            fileT = new File(file);
        }
        else path = intent.getStringExtra("filePath");
        final File fileIn = fileT;
        Log.d("status","Path:"+path);
        //do main
        class UnzippingTask extends AsyncTask<Void, Integer ,Boolean>{
            //==>define
            final LinearLayout unzipping_tip = (LinearLayout) findViewById(R.id.unzipping_tip);
            final LinearLayout cards = (LinearLayout) findViewById(R.id.cards_grid);
            final LinearLayout error_layout = (LinearLayout)findViewById(R.id.error_layout);

            @Override
            protected void onPreExecute(){
                cards.setVisibility(View.GONE);
                unzipping_tip.setVisibility(View.VISIBLE);

                //Listeners
                button_finish.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        doFinishButtonDoes(v);
                    }
                });
                button_finish_bottom.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        doFinishButtonDoes(v);
                    }
                });
            }

            @Override
            protected Boolean doInBackground(Void... params){
                if (!skipUnzip)
                    try {
                        Log.d("unzip","Unzipping to "+path);
                        unzip(fileIn,path,"0");
                    } catch (Exception e) {
                        ErrorsCollector.putError(e.toString(),0);
                        return false;
                    }

                return isPathUseful();//Find the true root path
            }

            @Nullable
            private Boolean isPathUseful(){
                File pathDecisions = new File(path);
                if(pathDecisions.exists()&&pathDecisions.isDirectory()){
                    File[] FileListInPath = pathDecisions.listFiles();
                    ArrayList<File> Dirs = new ArrayList<>();
                    int FilesFound = 0 ,DirsFound = 0;
                    for (File test : FileListInPath){
                        if (test.isFile()) FilesFound++;
                        else if (test.isDirectory()) {
                            Dirs.add(test);
                            DirsFound++;
                        }
                    }
                    if (FilesFound>=1&&DirsFound>=1){
                        return true;
                    }
                    else {
                        Boolean isFoundNext = false;
                        for (int i = 0;i<Dirs.size();i++){
                            path=Dirs.get(i).getPath();
                            if (isPathUseful()){
                                isFoundNext = true;
                                return true;
                            }
                        }
                        if (!isFoundNext){
                            ErrorsCollector.putError("There's nothing useful in the unzipped directory.",0);
                            return false;
                        }
                    }
                }
                return false;
            }


            @Override
            protected void onPostExecute(Boolean result){
                if(result){
                    decisions = new PackVersionDecisions(new File(path));
                    VerStr = decisions.getPackVersion();
                    if(VerStr.charAt(0)!='E'){
                        if (VerStr.equals(fullPE) && name.getText().toString().equals("") && description.getText().toString().equals("")){
                            name.setText(decisions.getName());
                            description.setText(decisions.getDescription());
                        }
                        doOnSuccess();
                    }
                    else {
                        doOnFail();
                    }
                }
                else{
                    //ERRORS
                    //set visibilities
                    cards.setVisibility(View.GONE);
                    unzipping_tip.setVisibility(View.GONE);
                    error_layout.setVisibility(View.VISIBLE);
                    //Read errors
                    MakeErrorDialog(ErrorsCollector.getError(0));
                }
            }
        }

        //Overwrite dialog
        if (new File(path).exists()&&!skipUnzip){
            AlertDialog.Builder dialog = new AlertDialog.Builder(ConversionActivity.this);
            dialog.setTitle(R.string.overwrite_title);
            dialog.setMessage(R.string.overwrite_content);
            dialog.setCancelable(false);
            dialog.setNegativeButton(R.string.skip, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    skipUnzip = true;
                    new UnzippingTask().execute();
                }
            });
            dialog.setPositiveButton(R.string.overwrite, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    skipUnzip = false;
                    new UnzippingTask().execute();
                }
            });
            dialog.show();
        }
        else new UnzippingTask().execute();
    }

    private void doFinishButtonDoes(View v){
        if (isPreFinished){

            packdescription = description.getText().toString();

            final ProgressDialog loadingDialog = new ProgressDialog(ConversionActivity.this);

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    loadingDialog.setTitle(R.string.loading);
                    loadingDialog.setMessage(getResources().getString(R.string.do_final_step));
                    loadingDialog.setCancelable(false);
                    loadingDialog.show();
                }
            });

            name.setEnabled(false);
            description.setEnabled(false);

             Thread doing = new Thread(new Runnable() {
                 @Override
                 public void run() {

                     doVersionDecisions();

                     doImageCompressions(loadingDialog);

                     runOnUiThread(new Runnable() {
                         @Override
                         public void run() {
                             loadingDialog.setMessage(getResources().getString(R.string.do_final_step));
                             loadingDialog.setTitle(getResources().getString(R.string.progress_writing_json));
                         }
                     });
                     try {
                         doJSONWriting();
                     } catch (FileNotFoundException e) {
                         ErrorsCollector.putError(e.toString(),1);
                         e.printStackTrace();
                     }

                     //For if icon doesn't exist
                     File iconTest = new File(path+"/pack_icon.png");
                     if (!iconTest.exists()){
                         byte[] buffer = new byte[1444];int i;
                         InputStream inputStream = getResources().openRawResource(R.raw.bug_pack_icon);
                         try {
                             FileOutputStream outputStream = new FileOutputStream(path+"/pack_icon.png");
                             while ((i=inputStream.read(buffer))!=-1){
                                 outputStream.write(buffer,0,i);
                             }
                         } catch (IOException e) {
                             e.printStackTrace();
                         }
                     }

                     //Move to dest
                     File dest = new File (Environment.getExternalStorageDirectory()+"/games/com.mojang/resource_packs/"+packname);
                     if (dest.isDirectory()&&dest.exists()) dest.mkdirs();
                     new File(path).renameTo(dest);
                     finishIntent.putExtra("Status_return",true);
                     runOnUiThread(new Runnable() {
                         @Override
                         public void run() {
                             loadingDialog.setMessage(getResources().getString(R.string.completed));
                         }
                     });
                     try {
                         Thread.sleep(1000);
                     } catch (InterruptedException e) {
                         e.printStackTrace();
                     }
                     //Done
                     finish();
                 }
             });
            doing.start();

            if (ErrorsCollector.getError(1)!=null)
                MakeErrorDialog(ErrorsCollector.getError(1));

        }
        else
            Snackbar.make(v,R.string.unclickable_unzipping,Snackbar.LENGTH_LONG).show();
    }

    public void loadIcon(){
        ImageView icon = (ImageView) findViewById(R.id.img_card_icon);
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize=1;
        if (VerStr.equals(fullPE)){
            File iconTest = new File(path+"/pack_icon.png");
            if (iconTest.exists()){
                Bitmap bm = BitmapFactory.decodeFile(iconTest.getPath(),options);
                icon.setImageBitmap(bm);
            }
        }
        else if (VerStr.equals(fullPC)){
            File iconTest = new File(path+"/pack.png");
            if (iconTest.exists()){
                Bitmap bm = BitmapFactory.decodeFile(iconTest.getPath(),options);
                icon.setImageBitmap(bm);
            }
        }
        else{
            FloatingActionButton finishBottom = (FloatingActionButton)findViewById(R.id.finishBottom);
            Snackbar.make(finishBottom,R.string.pack_icon_not_found,5000)
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
        switch (VerStr){
            case fullPE:type += getResources().getString(R.string.type_fullPE);break;
            case fullPC:type += getResources().getString(R.string.type_fullPC);break;
            case brokenPE:type += getResources().getString(R.string.type_brokenPE);break;
            case brokenPC:type += getResources().getString(R.string.type_brokenPC);break;
            default:type = null;
        }
        PackType.setText(type);

        String ver = decisions.getInMinecraftVer(PackInMC);
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
        if (VerStr.equals(fullPC)||VerStr.equals(brokenPC)) baseFrom = path+"/assets/minecraft/textures";
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
                            Toast.makeText(ConversionActivity.this,R.string.compression_alert,Toast.LENGTH_LONG).show();
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
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        if(resultCode == Activity.RESULT_OK)
            if(requestCode == 0){
                Uri uri = data.getData();
                String fileLocation = GetPathFromUri4kitkat.getPath(ConversionActivity.this,uri);
                Log.d("files","Copying image to "+path+"/pack_icon.png");
                if(!CopyFileOnSD(fileLocation,path+"/pack_icon.png")) MakeErrorDialog(ErrorsCollector.getError(0));
                else{
                    LinearLayout viewC = (LinearLayout)findViewById(R.id.cards);
                    Snackbar.make(viewC,R.string.completed,Snackbar.LENGTH_SHORT).show();
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
        final File notpack = new File(path);
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
}
