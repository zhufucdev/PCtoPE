package com.zhufuc.pctope.Adapters;

import com.zhufuc.pctope.Tools.PackVersionDecisions;

import java.io.File;

/**
 * Created by zhufu on 7/22/17.
 */

public class Textures {

    private File path;

    private String name,description;

    private Boolean isResourcePack = true;

    private PackVersionDecisions version;

    public Textures(File path){
        this.path = path;

        version = new PackVersionDecisions(path);

        name = version.getName();
        description = version.getDescription();
        isResourcePack = version.getPackVersion().charAt(0)!='E';
    }

    public Boolean IfIsResourcePack(){
        return isResourcePack;
    }

    public String getVersion(){
        return version.getPackVersion();
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
