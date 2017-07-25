package com.zhufuc.pctope;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ClipboardManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.database.CursorJoiner;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.support.annotation.BoolRes;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputEditText;
import android.support.v4.view.ScrollingView;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.util.Zip4jUtil;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLConnection;
import java.nio.BufferUnderflowException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.UUID;




public class ConversionActivity extends AppCompatActivity {

    final Intent finishIntent = new Intent();

    public String makeSpace(int i){
        String spaces="";
        for (int j=0;j<=i;j++) spaces+=" ";
        return spaces;
    }

    public void MakeErrorDialog(final String errorString){
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

    //==>define
    String path,packname,packdescription;
    boolean isPreFinished = false;

    public boolean doVersionDecisions(){
        File root = new File(path);
        if(root.exists()){
            File iconPE = new File(root+"/pack_icon.png");
            File iconPC = new File(root+"/pack.png");
            File texturePE = new File(root+"/textures");
            File texturePC = new File(root+"/assets/minecraft/textures");
            if(iconPE.exists()&&texturePE.exists()){
                Log.d("status","Icon for PE exists.");
                Log.d("status","Textures for PE exist.");
                onPEDecisions();
                return true;
            }
            else if(iconPC.exists()&&texturePC.exists()){
                Log.d("status","Icon for PC exists.");
                Log.d("status","Textures for PC exist.");
                onPcDecisions();
                return true;
            }
            else if (texturePC.exists()||texturePE.exists()) return true;
        }
        return false;
    }

    public boolean CopyFileOnSD(String fileFrom, String fileTo){
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
                String lastStr = new String(fileindir.substring(fileindir.lastIndexOf('.')));
                if(lastStr == ".mcmeta"){
                    f.delete();
                    Log.d("files","Deleted .mcmeta file:"+f);
                }
                else{
                    fileindir=fileindir.substring(0,fileindir.indexOf('.'));
                    Log.d("files","NO."+filelist.size()+":"+fileindir+' '+lastStr);
                    filelist.add(fileindir);
                }

            }
        }
        return filelist;
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
        DeleteFolder(path+"/assets");

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
                String line = terrainTxt.substring(j,i);
                terrainText.add(line);
                j=i;
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
                        int x=i,y=i+1,t=0;
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


                        for (int b=0;b<=y-x;b++){
                            terrainText.remove(x);
                        }
                        i--;
                        break;
                    }
                    else    break;
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
    
    public static void unzip(File zipFile, String dest, String passwd) throws ZipException, net.lingala.zip4j.exception.ZipException {
        net.lingala.zip4j.core.ZipFile zFile = new net.lingala.zip4j.core.ZipFile(zipFile); // 首先创建ZipFile指向磁盘上的.zip文件
        zFile.setFileNameCharset("GBK");       // 设置文件名编码，在GBK系统中需要设置
        if (!zFile.isValidZipFile()) {   // 验证.zip文件是否合法，包括文件是否存在、是否为zip文件、是否被损坏等
            throw new ZipException("压缩文件不合法,可能被损坏.");
        }
        File destDir = new File(dest);     // 解压目录
        if (destDir.isDirectory() && !destDir.exists()) {
            destDir.mkdir();
        }
        if (zFile.isEncrypted()) {
            zFile.setPassword(passwd.toCharArray());  // 设置密码
        }
        if(destDir.exists()&&destDir.getTotalSpace() >= zipFile.getTotalSpace()){
            return;
        }
        zFile.extractAll(dest);      // 将文件抽出到解压目录(解压)
    }

    /**
     * 删除单个文件
     * @param   filePath    被删除文件的文件名
     * @return 文件删除成功返回true，否则返回false
     */
    public boolean deleteFile(String filePath) {
        File file = new File(filePath);
        if (file.isFile() && file.exists()) {
            return file.delete();
        }
        return false;
    }
    /**
     * 删除文件夹以及目录下的文件
     * @param   filePath 被删除目录的文件路径
     * @return  目录删除成功返回true，否则返回false
     */
    public boolean deleteDirectory(String filePath) {
        boolean flag = false;
        //如果filePath不以文件分隔符结尾，自动添加文件分隔符
        if (!filePath.endsWith(File.separator)) {
            filePath = filePath + File.separator;
        }
        File dirFile = new File(filePath);
        if (!dirFile.exists() || !dirFile.isDirectory()) {
            return false;
        }
        flag = true;
        File[] files = dirFile.listFiles();
        //遍历删除文件夹下的所有文件(包括子目录)
        for (int i = 0; i < files.length; i++) {
            if (files[i].isFile()) {
                //删除子文件
                flag = deleteFile(files[i].getAbsolutePath());
                if (!flag) break;
            } else {
                //删除子目录
                flag = deleteDirectory(files[i].getAbsolutePath());
                if (!flag) break;
            }
        }
        if (!flag) return false;
        //删除当前空目录
        return dirFile.delete();
    }

    /**
     *  根据路径删除指定的目录或文件，无论存在与否
     *@param filePath  要删除的目录或文件
     *@return 删除成功返回 true，否则返回 false。
     */
    public boolean DeleteFolder(String filePath) {
        File file = new File(filePath);
        if (!file.exists()) {
            return false;
        } else {
            if (file.isFile()) {
                // 为文件时调用删除文件方法
                return deleteFile(filePath);
            } else {
                // 为目录时调用删除目录方法
                return deleteDirectory(filePath);
            }
        }
    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //Test Area
        //*Code Something for test

        //End of Test

        //Preload
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conversion);
        final TextInputEditText name = (TextInputEditText) findViewById(R.id.pname);
        final TextInputEditText description = (TextInputEditText) findViewById(R.id.pdescription);
        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        final CollapsingToolbarLayout collapsingbar = (CollapsingToolbarLayout) findViewById(R.id.collapsing_bar);
        setResult(RESULT_OK,finishIntent);//I don't need to use it
        packname = getResources().getString(R.string.project_unnamed);

        //set back button
        setSupportActionBar(toolbar);
        final ActionBar actionbar = getSupportActionBar();
        if(actionbar!=null){
            actionbar.setDisplayHomeAsUpEnabled(true);
        }
        //set project name on changed listener
        name.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if(charSequence!=null){
                    packname = charSequence.toString();
                    collapsingbar.setTitle(charSequence.toString());
                }
                else{
                    packname = getResources().getString(R.string.project_unnamed);
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

        Intent intent=getIntent();
        final String file = intent.getStringExtra("filePath");

        //do main
        class UnzippingTask extends AsyncTask<Void, Integer ,Boolean>{
            //==>define
            final LinearLayout unzipping_tip = (LinearLayout) findViewById(R.id.unzipping_tip);
            final LinearLayout cards = (LinearLayout) findViewById(R.id.cards_layout);
            final LinearLayout error_layout = (LinearLayout)findViewById(R.id.error_layout);

            protected void doFinishButtonDoes(View v){
                if (isPreFinished){
                    packdescription = description.getText().toString();
                    final ProgressDialog loadingDialog = new ProgressDialog(ConversionActivity.this);
                    name.setEnabled(false);
                    description.setEnabled(false);
                    loadingDialog.hide();

                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    loadingDialog.setTitle(R.string.loading);
                                    loadingDialog.setMessage(getApplicationContext().getString(R.string.do_final_step));
                                    loadingDialog.setCancelable(false);
                                    loadingDialog.show();
                                }
                            });

                            try {
                                doJSONWriting();

                            } catch (FileNotFoundException e) {
                                ErrorsCollector.putError(e.toString(),1);
                                e.printStackTrace();
                            }
                            File dest = new File (Environment.getExternalStorageDirectory()+"/games/com.mojang/resource_packs/"+packname);
                            if (dest.isDirectory()&&dest.exists()) dest.mkdir();
                            new File(path).renameTo(dest);
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    loadingDialog.hide();
                                    if (ErrorsCollector.getError(1)==null) {
                                        Snackbar.make(cards, R.string.completed, Snackbar.LENGTH_LONG).show();
                                        finishIntent.putExtra("Status_return",true);
                                        try {
                                            Thread.sleep(1000);
                                        } catch (InterruptedException e) {
                                            e.printStackTrace();
                                        }
                                        finish();
                                    }
                                }
                            });
                        }
                    }).start();

                    if (ErrorsCollector.getError(1)!=null)
                        MakeErrorDialog(ErrorsCollector.getError(1));
                }
                else
                    Snackbar.make(v,R.string.unclickable_unzipping,Snackbar.LENGTH_LONG).show();
            }

            @Override
            protected void onPreExecute(){
                cards.setVisibility(View.GONE);
                unzipping_tip.setVisibility(View.VISIBLE);
                path = ConversionActivity.this.getExternalCacheDir().getPath();
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
                try {
                    String fileName="";
                    //==>get file name
                    for(int i=file.length()-1;i>=0;i--){
                        if(file.charAt(i)=='/'){
                            Log.d("files","/ is at "+i);
                            for (int j=i+1;j<file.length();j++){
                                fileName+=file.charAt(j);
                            }
                            fileName=fileName.substring(0,fileName.lastIndexOf('.'));
                            break;
                        }
                    }
                    path+="/"+fileName+"/";
                    Log.d("unzip","Unzipping "+file+" to "+path);
                    File fileIn = new File(file);
                    unzip(fileIn,path,"0");
                } catch (Exception e) {
                    ErrorsCollector.putError(e.toString(),0);
                    return false;
                }

                //Find the true root path
                return isPathUseful();
            }

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
                        if (!isFoundNext) return false;
                    }
                }
                else return false;
                return null;
            }

            @Override
            protected void onPostExecute(Boolean result){
                if(result){
                    if(doVersionDecisions()){
                        doOnSuccesses();
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
                    //define
                    FileInputStream in = null;
                    BufferedReader reader = null;
                    final StringBuilder content = new StringBuilder();
                    //Read errors
                    MakeErrorDialog(ErrorsCollector.getError(0));
                }
            }
        }
        new UnzippingTask().execute();
    }


    public void loadIcon(){
        ImageView icon = (ImageView) findViewById(R.id.img_card_icon);
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize=2;
        File iconTest = new File(path.toString()+"/pack_icon.png");
        if (iconTest.exists()){
            Bitmap bm = BitmapFactory.decodeFile(iconTest.getPath(),options);
            icon.setImageBitmap(bm);
        }
        else{
            FloatingActionButton finishBottom = (FloatingActionButton)findViewById(R.id.finishBottom);
            Snackbar.make(finishBottom,R.string.pack_icon_not_found,Snackbar.LENGTH_LONG)
                    .setAction(R.string.ok, new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent choose = new Intent(Intent.ACTION_GET_CONTENT);
                            choose.setType("image/*");
                            choose.addCategory(Intent.CATEGORY_OPENABLE);
                            startActivityForResult(choose, 0);
                        }
                    })
                    .setActionTextColor(getResources().getColor(R.color.colorPrimaryDark))
                    .show();
        }
    }


    public void doOnSuccesses(){
        //Set layouts
        //==>define
        final LinearLayout unzipping_tip = (LinearLayout) findViewById(R.id.unzipping_tip);
        final LinearLayout cards = (LinearLayout) findViewById(R.id.cards_layout);
        final LinearLayout error_layout = (LinearLayout)findViewById(R.id.error_layout);
        cards.setVisibility(View.VISIBLE);
        unzipping_tip.setVisibility(View.GONE);
        error_layout.setVisibility(View.GONE);
        LinearLayout cards_layout = (LinearLayout) findViewById(R.id.cards_layout);

        isPreFinished = true;

        Animation animation = AnimationUtils.loadAnimation(ConversionActivity.this, R.anim.cards_show);
        cards.startAnimation(animation);
        //Load icon
        loadIcon();
        //Set icon editor
        Button edit = (Button) findViewById(R.id.card_icon_edit);
        edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String[] items = new String[]{ConversionActivity.this.getString(R.string.cover),ConversionActivity.this.getString(R.string.edit_converactdialog)};
                AlertDialog.Builder builder = new AlertDialog.Builder(ConversionActivity.this);
                builder.setTitle(R.string.conversionact_edit_dialog_title);
                builder.setItems(items, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        switch (i){
                            case 0://overwrite
                                Intent choose = new Intent(Intent.ACTION_GET_CONTENT);
                                choose.setType("image/*");
                                choose.addCategory(Intent.CATEGORY_OPENABLE);
                                startActivityForResult(choose, 0);
                        }
                    }
                }).show();
            }
        });
        //Set icon open button
        Button open = (Button)findViewById(R.id.card_icon_open);
        open.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Snackbar.make(v,"Ummm....",Snackbar.LENGTH_SHORT).show();
            }
        });
    }

    //onResult
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        if(resultCode == Activity.RESULT_OK)
            if(requestCode == 0){
                Uri uri = data.getData();
                String fileLocation = GetPathFromUri4kitkat.getPath(ConversionActivity.this,uri);
                Log.d("files","Copying image to "+path+"pack_icon.png");
                if(!CopyFileOnSD(fileLocation,path+"pack_icon.png")) MakeErrorDialog(ErrorsCollector.getError(0));
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
        final LinearLayout cards = (LinearLayout) findViewById(R.id.cards_layout);
        final LinearLayout error_layout = (LinearLayout)findViewById(R.id.error_layout);
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
                    r = DeleteFolder(notpack.toString());
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
