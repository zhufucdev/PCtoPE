package com.zhufuc.pctope.Adapters;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import com.zhufuc.pctope.Utils.PackVersionDecisions;

import org.w3c.dom.Text;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Serializable;

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

        public Edit(String path){
            this.path = path;
            textures = new Textures(new File(path));
        }

        public void changeName(String nameIndex){
            readManifest();
            StringBuilder builder = new StringBuilder(intro);

            int now = intro.indexOf("\"name\"");
            if (now!=-1){
                int start = intro.indexOf('\"',now+7)+1
                        ,stop = intro.indexOf("\"",start);
                builder.replace(start,stop,nameIndex);
                intro = builder.toString();
            }
            writeResult(intro);
        }

        public void changeDescription(String descriptionIndex){
            readManifest();
            StringBuilder builder = new StringBuilder(intro);

            int posTemp = 0;
            while (true){
                int now = intro.indexOf("\"description\"",posTemp);
                if (now!=-1){
                    int start = intro.indexOf('\"',now+14)+1
                            ,stop = intro.indexOf("\"",start);
                    posTemp = stop;
                    builder.replace(start,stop,descriptionIndex);
                    intro = builder.toString();
                }
                else break;
            }

            writeResult(intro);
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
    }

}
