package com.zhufuc.pctope;

import android.app.Application;
import android.util.Log;

import java.util.ArrayList;


/**
 * Created by zhufu on 7/11/17.
 */

public class ErrorsCollector extends Application{
    private static ArrayList<String> errors = new ArrayList<String>();
    public static void putError(String errorString,int errorID){
        errors.add(errorID,errorString);
        Log.d("ErrorCollector","Error added，id:"+errorID+";string:"+errorString);
    }
    public static String getError(int errorID){
        if (errorID<errors.size())
            return errors.get(errorID);
        else return null;
    }
}
