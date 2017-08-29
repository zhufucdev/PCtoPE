package com.zhufuc.pctope.Utils;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;

import com.zhufuc.pctope.Activities.ConversionActivity;
import com.zhufuc.pctope.Collectors.ErrorsCollector;
import com.zhufuc.pctope.R;

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

/**
 * Created by zhufu on 17-8-28.
 */

public class TextureConversionUtils {

    private String makeSpace(int i){
        String spaces="";
        for (int j=0;j<=i;j++) spaces+=" ";
        return spaces;
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


    //如题，遍历所有子png文件
    private static ArrayList<String> filelist = new ArrayList<String>();
    private static ArrayList<String> ListFiles(File path) {
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

    public PackVersionDecisions decisions;
    public String VerStr = null;

    private void doVersionDecisions(){
        Log.d("Pack Version",VerStr);
        if (VerStr.equals(TextureCompat.brokenPE)||VerStr.equals(TextureCompat.fullPE)){
            onPEDecisions();
        }
        else if (VerStr.equals(TextureCompat.brokenPC)||VerStr.equals(TextureCompat.fullPC)){
            onPcDecisions();
        }
    }

    private void doPCSpecialResourcesDecisions(){
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
        //方块破坏崩裂贴图 已解决

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
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        //For zombies
        File[] images = {new File(path+"/textures/entity/zombie/zombie.png"),new File(path+"/textures/entity/zombie/husk.png"),new File(path+"/textures/entity/pig/pigzombie.png")};
        for (File image : images) {
            if (image.exists()) {
                Bitmap zombieImage = BitmapFactory.decodeFile(image.getPath());

                if (zombieImage.getHeight() == zombieImage.getWidth()) {
                    Bitmap bitmap = Bitmap.createBitmap(zombieImage, 0, 0, zombieImage.getWidth(), zombieImage.getHeight() / 2);

                    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
                    try {
                        FileOutputStream outputStream = new FileOutputStream(image);
                        outputStream.write(byteArrayOutputStream.toByteArray());
                        outputStream.flush();
                        outputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

            }
        }

        //For destroy stages
        for (int i = 0;i <= 9;i ++){
            File stage = new File(path+"/textures/blocks/destroy_stage_"+i+".png");
            if (stage.exists())
                stage.renameTo(new File(path + "/textures/environment/" + stage.getName()));
        }
    }

    private void onPcDecisions() {
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


    private void onPEDecisions(){
        File[] JSONs = new File(path+"/textures/").listFiles();
        for (File f:JSONs){
            String n = f.getPath();
            if (n.substring(n.lastIndexOf('.'),n.length()).equals(".json"))
                f.delete();
        }
    }

    private String doJsonFixing(InputStream data,int SearchFrom){
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
        Resources raw = context.getResources();
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
                mOnCrashListener.onCrash(e.toString());
                e.printStackTrace();
            }

        //for terrain block textures
        String textBefore = "{"+System.getProperty("line.separator")+makeSpace(4)+"\"resource_pack_name\":"+"\""+packname+"\""+System.getProperty("line.separator");
        FileOutputStream terrainOut = new FileOutputStream(path+"/textures/terrain_texture.json");
        try {
            terrainOut.write((textBefore).getBytes());
            terrainOut.write(doJsonFixing(data[4],77).getBytes());
        } catch (IOException e) {
            mOnCrashListener.onCrash(e.toString());
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
                    mOnCrashListener.onCrash(e.toString());
                    e.printStackTrace();
                }
        }
        if (isCreated){
            try {
                itemOut.write(textBefore.getBytes());
                itemOut.write(doJsonFixing(data[3],5).getBytes());
            } catch (IOException e) {
                mOnCrashListener.onCrash(e.toString());
                e.printStackTrace();
            }

            //for flip book texture
            FileOutputStream flipOut = new FileOutputStream(path+"/textures/flipbook_textures.json");
            try {
                flipOut.write(doJsonFixing(data[2],2).getBytes());
            } catch (IOException e) {
                mOnCrashListener.onCrash(e.toString());
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
        else mOnCrashListener.onCrash("Could not create JSON files.");

    }

    public int compressFinalSize = 0;
    private void doMainInCompressing(final File n){
        if (n.isFile()) {
            //Show progress
            Log.d("compression","Compressing "+n);
            mConversionChangeListener.inDoingImageCompressions(n.getPath());


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
                mOnCrashListener.onCrash(e.toString());
                e.printStackTrace();
            }
        }
    }

    private void doImageCompressions(){
        if (compressFinalSize == 0)
            return;
        Log.i("Pack Conversion","Doing image compressions...");
        //get images
        File[] items = new File(path+"/textures/items").listFiles(),blocks = new File(path+"/textures/blocks").listFiles();
        if (items!=null){
            for (File n:items)
                doMainInCompressing(n);
        }
        if (blocks!=null){
            for (File n:blocks)
                doMainInCompressing(n);
        }
    }

    private static void unzip(File zipFile, String dest, String passwd) throws ZipException, net.lingala.zip4j.exception.ZipException {
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


    private String FilePath;
    public String path;
    private Context context;
    private Handler handler;
    private String packname,packdescription;
    public boolean isPreFinished = false;
    public boolean skipUnzip;
    public TextureConversionUtils(String FilePath,Context context){
        this.FilePath = FilePath;
        this.context = context;
        this.path = context.getExternalCacheDir().getPath();
        this.handler = handler;

        if (!skipUnzip) {
            String fileName = FilePath.substring(FilePath.lastIndexOf('/')+1,FilePath.lastIndexOf('.'));
            //==>get file name
            path += "/" + fileName;
            Log.d("unzip", "We will unzip " + FilePath + " to " + path);
        }
        else path = FilePath;
        Log.i("status","Path:"+path);
    }

    /*
        Some Listeners...
     */
    private OnUncompressListener mOnUncompressListener = null;
    public static interface OnUncompressListener{
        void onPreUncompress();
        void inUncompressing();
        void onPostUncompress(boolean result,String version);
    }
    private OnCrashListener mOnCrashListener;
    public static interface OnCrashListener{
        void onCrash(String errorContent);
    }
    
    public void setOnUncompressListener(OnUncompressListener listener){
        this.mOnUncompressListener = listener;
    }
    public void setOnCrashListener(OnCrashListener listener){
        this.mOnCrashListener = listener;
    }

    private ConversionChangeListener mConversionChangeListener = null;
    public static interface ConversionChangeListener{
        void inDoingVersionDecisions();
        void inDoingImageCompressions(String whatsBeingCompressing);
        void inDoingJSONWriting();
        void onDone();
    }

    public void setConversionChangeListener(ConversionChangeListener listener){
        this.mConversionChangeListener = listener;
    }
    
    public void UncompressPack(){
        class UnzippingTask extends AsyncTask<Void, Integer ,Boolean> {

            @Override
            protected void onPreExecute(){
                mOnUncompressListener.onPreUncompress();
            }

            @Override
            protected Boolean doInBackground(Void... params){
                if (!skipUnzip) {
                    mOnUncompressListener.inUncompressing();
                    try {
                        Log.d("unzip", "Unzipping to " + path);
                        unzip(new File(FilePath), path, "0");
                    } catch (Exception e) {
                        return false;
                    }
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
                         iconPath = (VerStr.equals(TextureCompat.fullPC))? path+"/pack.png" : (VerStr.equals(TextureCompat.fullPE))?path+"/pack_icon.png" : null;
                        mOnUncompressListener.onPostUncompress(true,VerStr);
                    }
                    else {
                        mOnUncompressListener.onPostUncompress(false,null);
                    }
                }
                else mOnUncompressListener.onPostUncompress(false,null);
            }
        }

        new UnzippingTask().execute();
    }

    public void doConversions(final String packname, String packdescription){
        this.packname = packname;this.packdescription = packdescription;

        Log.i("Pack Conversion","Doing version decisions...");
        mConversionChangeListener.inDoingVersionDecisions();
        doVersionDecisions();

        doImageCompressions();
        try {
            Log.i("Pack Conversion","Doing json Writing");
            mConversionChangeListener.inDoingJSONWriting();
            doJSONWriting();
        } catch (FileNotFoundException e) {
            ErrorsCollector.putError(e.toString(),1);
            e.printStackTrace();
        }

        //For if icon doesn't exist
        File iconTest = new File(path+"/pack_icon.png");
        if (!iconTest.exists()){
            Log.i("Pack Conversion","Writing icon for non-icon-pack...");
            byte[] buffer = new byte[1444];int i;
            InputStream inputStream = context.getResources().openRawResource(R.raw.bug_pack_icon);
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
        Log.i("Pack Conversion","Moving to dest...");
        File dest = new File (Environment.getExternalStorageDirectory()+"/games/com.mojang/resource_packs/"+packname);
        if (dest.isDirectory()&&dest.exists()) dest.mkdirs();
        new File(path).renameTo(dest);

        mConversionChangeListener.onDone();
        //Done
    }

    private String iconPath;
    public Bitmap getIcon(){
        if (iconPath==null || !new File(iconPath).exists()) return null;
        return BitmapFactory.decodeFile(iconPath);
    }

    public void setIcon(Bitmap icon){
        if (iconPath!=null){
            try {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                FileOutputStream output = new FileOutputStream(iconPath);
                output.write(baos.toByteArray());
                output.close();
            } catch (IOException e) {
                e.printStackTrace();
                mOnCrashListener.onCrash(e.toString());
            }

        }
    }
    
}
