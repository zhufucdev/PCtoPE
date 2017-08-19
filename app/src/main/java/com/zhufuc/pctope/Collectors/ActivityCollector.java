package com.zhufuc.pctope.Collectors;

import android.app.Activity;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zhufu on 17-6-23.
 */

public class ActivityCollector {
    private static List<Activity> activities = new ArrayList<>();
    public static void addActivity(Activity activity){
        activities.add(activity);
        Log.d("Activity","Added "+activity.getClass().getSimpleName());
    }
    public static void removeActivity(Activity activity){
        activities.remove(activity);
        Log.d("Activity","Removed "+activity.getClass().getSimpleName());
    }

    public static void finishActivity(Activity activity){
        if (!activity.isFinishing()){
            activity.finish();
        }
    }

    public static void finishAll(){
        for (Activity activity : activities){
            if(!activity.isFinishing()){
                activity.finish();
            }
        }
    }
}
