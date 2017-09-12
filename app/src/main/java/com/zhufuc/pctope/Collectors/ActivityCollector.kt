package com.zhufuc.pctope.Collectors

import android.app.Activity
import android.util.Log

import java.util.ArrayList

/**
 * Created by zhufu on 17-6-23.
 */

object ActivityCollector {
    private val activities = ArrayList<Activity>()
    fun addActivity(activity: Activity) {
        activities.add(activity)
        Log.d("Activity", "Added " + activity.javaClass.getSimpleName())
    }

    fun removeActivity(activity: Activity) {
        activities.remove(activity)
        Log.d("Activity", "Removed " + activity.javaClass.getSimpleName())
    }

    fun finishOther(activity: Activity) {
        for (act in activities) {
            if (act != activity) {
                if (!act.isFinishing) {
                    act.finish()
                }
            }
        }
    }

    fun finishAll() {
        for (activity in activities) {
            if (!activity.isFinishing) {
                activity.finish()
            }
        }
    }
}
