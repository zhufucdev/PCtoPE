package com.zhufucdev.pctope.collectors

import android.app.Activity
import com.zhufucdev.pctope.utils.mLog

import java.util.ArrayList

/**
 * Created by zhufu on 17-6-23.
 */

object ActivityCollector {
    private val activities = ArrayList<Activity>()
    fun addActivity(activity: Activity) {
        activities.add(activity)
        mLog.d("Activity", "Added " + activity.javaClass.simpleName)
    }

    fun removeActivity(activity: Activity) {
        activities.remove(activity)
        mLog.d("Activity", "Removed " + activity.javaClass.simpleName)
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
