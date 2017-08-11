package com.zhufuc.pctope.Utils;

import android.os.Environment;
import android.view.View;

import com.zhufuc.pctope.R;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Objects;
import java.util.StringTokenizer;

/**
 * Created by zhufu on 7/30/17.
 */

public class PackVersionDecisions {
    private File path;
    private String name = null,description = null;

    public PackVersionDecisions(File rootPath){
        this.path = rootPath;
        if (new File(path+"/manifest.json").exists()) readManifest();
    }

    public String getPackVersion(){
        if (!path.exists()) return "E:file not found.";
        if (path.isDirectory()) {
            File manifest = new File(path+"/manifest.json");
            String v = "";
            if (manifest.exists()) {
                if (name!=null && description!=null) {
                    if (new File(path+"/pack_icon.png").exists())
                        v = "full";
                    else v = "broken";
                }
                else v = "broken";
            }
            else{
                if (new File(path+"/pack.png").exists()) v = "full";
                else v = "broken";
            }

            if (new File(path+"/textures/blocks").exists()){
                String[] PEblock = new File(path+"/textures/blocks").list();
                int i = 0;
                for (String n : PEblock){
                    if (n.substring(n.lastIndexOf('.'),n.length()).equals(".png"))
                        i++;
                    if (i>=10) return "Found:"+v+" PE pack.";
                }
            }
            else if (new File(path+"/assets/minecraft/textures/blocks").exists()){
                String[] PCblock = new File(path+"/assets/minecraft/textures/blocks").list();
                int i = 0;
                for (String n : PCblock){
                    if (n.substring(n.lastIndexOf('.'),n.length()).equals(".png"))
                        i++;
                    if (i>=10) return "Found:"+v+" PC pack.";
                }
            }
            else if (new File(path+"/assets/minecraft/textures/items").exists()){
                String[] PEitem = new File(path+"/textures/items").list();
                int i = 0;
                for (String n : PEitem){
                    if (n.substring(n.lastIndexOf('.'),n.length()).equals(".png"))
                        i++;
                    if (i>=10) return "Found:"+v+" PE pack.";
                }
            }
            else if (new File(path+"/assets/minecraft/textures/items").exists()) {
                String[] PCitem = new File(path + "/assets/minecraft/textures/items").list();
                int i = 0;
                for (String n : PCitem) {
                    if (n.substring(n.lastIndexOf('.'), n.length()).equals(".png"))
                        i++;
                    if (i >= 10) return "Found:" + v + " PC pack.";
                }
            }

            return "E:nothing found.";
        }
        else return "E:File isn't a directory.";
    }

    public boolean getIfIsResourcePack(String testVersion){
        if (path.isDirectory())
            if (Objects.equals(testVersion,"PE")||Objects.equals(testVersion,"ALL")){
                File[] test = new File(path+"/textures").listFiles();
                if (test != null)
                    for (File f:test)
                        if (f.isDirectory()) return true;
            }
            if (Objects.equals(testVersion,"PC")||Objects.equals(testVersion,"ALL")){
                File[] test = new File(path+"/assets/minecraft/textures").listFiles();
                if (test != null)
                    for (File f:test)
                        if (f.isDirectory()) return true;
            }

        return false;
    }

    public String getInMinecraftVer(View v){
        File metaFile = new File(path+"/pack.mcmeta");
        if (metaFile.exists()){
            FileInputStream metaText = null;
            BufferedReader reader = null;
            StringBuilder stringBuilder = new StringBuilder();

            try {
                metaText = new FileInputStream(metaFile);
                reader = new BufferedReader(new InputStreamReader(metaText));
                String line = "";
                while ((line = reader.readLine())!=null)
                    stringBuilder.append(line);
            } catch (IOException e) {
                e.printStackTrace();
            }

            String content = stringBuilder.toString();

            //Find version code
            int posStart = content.indexOf(':',content.indexOf("pack_format"));
            int posEnd = content.indexOf(',',posStart);
            String StartToEnd = content.substring(posStart,posEnd);
            for (int i=0;i<StartToEnd.length();i++){
                char now = StartToEnd.charAt(i);
                if (now>=48 && now<=57){
                    switch (now-48){
                        case 1:return v.getResources().getString(R.string.type_before_1_9);
                        case 2:return v.getResources().getString(R.string.type_1_9_1_10);
                        case 3:return v.getResources().getString(R.string.type_after_1_11);
                        default:return null;
                    }
                }
            }
        }
        return null;
    }

    private void readManifest(){
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
            } catch (IOException e) {
                e.printStackTrace();
            }
            String intro = stringBuilder.toString();

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
    }

    public String getName(){
        return name;
    }

    public String getDescription(){
        return description;
    }
}
