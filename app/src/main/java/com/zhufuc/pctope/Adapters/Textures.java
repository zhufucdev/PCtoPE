package com.zhufuc.pctope.Adapters;

import android.content.Context;
import android.util.Log;
import android.widget.ImageView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Objects;

/**
 * Created by zhufu on 7/22/17.
 */

public class Textures {

    private File path;

    private String name,description;

    private Boolean isResourcePack = true;

    public Textures(File path){
        this.path = path;
        readManifest();
    }

    public Boolean IfIsResourcePack(){
        return isResourcePack;
    }

    private void readManifest(){
        if (path.exists()&&path.isDirectory()){
            File manifest = new File(path+"/manifest.json");
            if (manifest.exists()) {
                FileInputStream in = null;
                BufferedReader reader = null;
                StringBuilder stringBuilder = new StringBuilder();

                try {
                    in = new FileInputStream(manifest);
                    reader = new BufferedReader(new InputStreamReader(in));
                    String line = "";
                    while ((line = reader.readLine())!=null)
                        stringBuilder.append(line);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                String intro = stringBuilder.toString();
                //look for information

                for (int i=11;i<intro.length();i++){
                    String sub11 = intro.substring(i-11,i);
                    if (Objects.equals(sub11, "description")){
                        int Start = 0,Stop = 0,Count = 0;
                        for (int j=i+1;j<intro.length();j++){
                            if (intro.charAt(j)=='\"'){
                                Count++;
                                if (Count==1) Start = j;
                                else if (Count==2){
                                    Stop = j;
                                    break;
                                }
                            }
                        }
                        description = intro.substring(Start+1,Stop);
                        break;
                    }
                }
                for (int i=4;i<intro.length();i++){
                    String sub4 = intro.substring(i-4,i);
                    if (Objects.equals(sub4,"name")){
                        int Start = 0,Stop = 0,Count = 0;
                        for (int j=i+1;j<intro.length();j++){
                            if (intro.charAt(j)=='\"'){
                                Count++;
                                if (Count==1) Start = j;
                                else if (Count==2){
                                    Stop = j;
                                    break;
                                }
                            }
                        }
                        name = intro.substring(Start+1,Stop);
                        break;
                    }
                }
            }
            else isResourcePack = false;
        }
        else isResourcePack = false;
        return;
    }

    public String getName() { return name; }

    public String getDescription() {
        return description;
    }

    public File getIcon(){
        File icon =new File(path+"/pack_icon.png");
        if (icon.exists())
            return icon;
        else return null;
    }

    public File getPath() {
        return path;
    }
}
