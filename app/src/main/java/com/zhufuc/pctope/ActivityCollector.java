package com.zhufuc.pctope;

import android.app.Activity;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zhufu on 17-6-23.
 */

public class ActivityCollector {
    public static List<Activity> activities = new ArrayList<>();
    public static void addActivity(Activity activity){
        activities.add(activity);
    }
    public static void removeActivity(Activity activity){
        activities.remove(activity);
    }
    public static void finishActivity(Activity activity){
        activity.finish();
    }
    public static void finishAll(){
        for (Activity activity : activities){
            if(!activity.isFinishing()){
                activity.finish();
            }
        }
    }
}
