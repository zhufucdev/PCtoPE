package com.zhufuc.pctope.Adapters;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import com.zhufuc.pctope.Utils.CompressImage;
import com.zhufuc.pctope.Utils.JsonFormatTool;
import com.zhufuc.pctope.Utils.PackVersionDecisions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.util.UUID;

/**
 * Created by zhufu on 7/22/17.
 */

public class Textures {

    private String path;

    private String name,description;

    private PackVersionDecisions version;

    public Textures(File path){
        this.path = path.getPath();
        version = new PackVersionDecisions(path);
        name = version.getName();
        description = version.getDescription();
    }

    public Boolean IfIsResourcePack(String testVersion){
        return version.getIfIsResourcePack(testVersion);
    }

    public String getVersion(){
        return version.getPackVersion();
    }

    public String getName() { return name; }

    public String getDescription() {
        return description;
    }

    public String getIcon(){
        File icon =new File(path+"/pack_icon.png");
        if (icon.exists()){
            return icon.getPath();
        }
        else return null;
    }

    public String getPath() {
        return path;
    }

    //EDITING
    public static class Edit{

        private String path;

        private String intro = null;

        private Textures textures;

        private String makeSpace(int i){
            String spaces="";
            for (int j=0;j<=i;j++) spaces+=" ";
            return spaces;
        }

        public interface CompressionProgressChangeListener{
            void OnProgressChangeListener(String whatsBeingCompressed,boolean isDone);
        }
        private CompressionProgressChangeListener compressionProgressChangeListener = null;
        public void setOnCompressionProgressChangeListener(CompressionProgressChangeListener listener){
            this.compressionProgressChangeListener = listener;
        }

        public interface onCrashListener{
            void onCrash(String e);
        }
        private onCrashListener onCrashListener = null;
        public void setOnCrashListener(onCrashListener listener){
            this.onCrashListener = listener;
        }

        public Edit(String path){
            this.path = path;
            textures = new Textures(new File(path));
        }

        public void changeNameAndDescription(String nameIndex,String descriptionIndex){
            readManifest();

            Log.i("Change Existed Pack","Name Set="+nameIndex+", Description Set="+descriptionIndex+", Manifest Read=\n"+intro);

            try {
                JSONObject object = new JSONObject(intro);
                if (object.has("header")){
                    JSONObject header = object.getJSONObject("header");
                    header.put("name",nameIndex);
                    object.put("header",header);
                    intro = object.toString();

                    header.put("description",descriptionIndex);
                    object.put("header",header);
                    intro = object.toString();
                }
                else intro = overwriteManifest(nameIndex,descriptionIndex);
                if (object.has("modules")){
                    JSONArray array = object.getJSONArray("modules");
                    JSONObject descriptionObj = array.getJSONObject(0);
                    descriptionObj.put("description",descriptionIndex);
                    object.put("modules",array);
                    intro = object.toString();
                }
                else intro = overwriteManifest(nameIndex,descriptionIndex);
            } catch (JSONException e) {
                intro = overwriteManifest(nameIndex,descriptionIndex);
            }

            writeResult(new JsonFormatTool().formatJson(intro));
        }

        public void iconEdit(String icon) throws IOException {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            Bitmap bitmap = BitmapFactory.decodeFile(icon);

            bitmap.compress(Bitmap.CompressFormat.PNG,100,baos);

            FileOutputStream output = null;
            output = new FileOutputStream(path+"/pack_icon.png");
            output.write(baos.toByteArray());
            output.close();
        }

        public void iconEdit(Bitmap bitmap) throws IOException {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG,100,baos);

            FileOutputStream output = null;
            output = new FileOutputStream(path+"/pack_icon.png");
            output.write(baos.toByteArray());
            output.close();
        }

        public void compressImages(int compressFinalSize){
            if (compressFinalSize == 0)
                return;
            Log.i("Pack Conversion","Doing image compressions...");
            //get images
            File[] items = new File(path+"/textures/items").listFiles(),blocks = new File(path+"/textures/blocks").listFiles();
            if (items!=null){
                for (File n:items)
                    doMainInCompressing(n,compressFinalSize);
            }
            if (blocks!=null){
                for (File n:blocks)
                    doMainInCompressing(n,compressFinalSize);
            }
            compressionProgressChangeListener.OnProgressChangeListener(null,true);
        }

        private void doMainInCompressing(final File n,int compressFinalSize){
            if (n.isFile()) {
                //Show progress
                compressionProgressChangeListener.OnProgressChangeListener(n.getPath(),false);
                Log.d("compression","Compressing "+n);

                String str = n.getPath();
                if (!str.substring(str.lastIndexOf("."),str.length()).equals(".png"))
                    return;

                Bitmap image = BitmapFactory.decodeFile(n.getPath());
                //get compressed bitmap
                int compressHeight = compressFinalSize,compressWidth = compressFinalSize;
                if (image.getWidth()-image.getHeight()<-5){
                    compressHeight = compressHeight*(image.getWidth()/compressFinalSize);
                }
                else if (image.getHeight()-image.getWidth()>5){
                    compressWidth = compressWidth*(image.getHeight()/compressFinalSize);
                }
                Bitmap compressed = CompressImage.getBitmap(image,compressHeight,compressWidth);

                if (compressed == null){
                    onCrashListener.onCrash("Compressing Resources: could not compress "+n.getPath());
                }

                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                compressed.compress(Bitmap.CompressFormat.PNG, 100, baos);//png

                try {
                    FileOutputStream outputStream = new FileOutputStream(n);
                    outputStream.write(baos.toByteArray());

                    outputStream.flush();
                    outputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        private void writeResult(String result){
            try {
                FileOutputStream outputStream = new FileOutputStream(path+"/manifest.json");
                outputStream.write(result.getBytes());

                outputStream.flush();
                outputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        private void readManifest(){
            File manifest = new File(path+"/manifest.json");
            if (!(intro==null))
                return;

            if (manifest.exists()){
                FileInputStream in = null;
                BufferedReader reader = null;
                StringBuilder stringBuilder = new StringBuilder();
                try {
                    in = new FileInputStream(manifest);
                    reader = new BufferedReader(new InputStreamReader(in));
                    String line = "";
                    while ((line = reader.readLine())!=null){
                        line+="\n";
                        stringBuilder.append(line);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                intro = stringBuilder.toString();
            }
        }

        public String overwriteManifest(String name,String description){

            String intro;
            JSONObject out = new JSONObject();
            JSONArray versionArray = new JSONArray();
            String uuid = UUID.randomUUID().toString();
            try {
                versionArray.put(0);
                versionArray.put(0);
                versionArray.put(1);

                out.put("format_version",1);
                JSONObject header = new JSONObject();
                header.put("description",description);
                header.put("name",name);
                header.put("uuid",uuid);
                header.put("version",versionArray);
                out.put("header",header);

                JSONArray modules = new JSONArray();
                JSONObject modulesObjs = new JSONObject();
                modulesObjs.put("description",description);
                modulesObjs.put("type","resources");
                modulesObjs.put("uuid",uuid);
                modulesObjs.put("version",versionArray);
                modules.put(modulesObjs);
                out.put("modules",modules);

            } catch (JSONException e) {
                e.printStackTrace();
            }

            return (new JsonFormatTool().formatJson(out.toString()));

        }
    }

}
