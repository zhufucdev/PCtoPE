package com.zhufucdev.pctope.interf

import com.google.android.material.snackbar.Snackbar
import com.zhufucdev.pctope.utils.DeleteFolder
import com.zhufucdev.pctope.utils.Textures
import com.zhufucdev.pctope.utils.mLog

/**
 * Created by zhufu on 17-7-29.
 */

class DeletingCallback(whatWillBeDeleted: List<Textures>) : Snackbar.Callback() {

    private var WhatWillBeDeleted = whatWillBeDeleted

    override fun onDismissed(snackbar: Snackbar?, event: Int) {
        super.onDismissed(snackbar, event)
        if (event != DISMISS_EVENT_ACTION) {


            Thread {
                WhatWillBeDeleted.forEach {
                    mLog.i("Manager", "Deleting ${it.path}")
                    DeleteFolder.delete(it.path)
                }
            }.start()
        }
    }

}
