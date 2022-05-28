package com.zhufuc.pctope.interf

import com.google.android.material.snackbar.Snackbar
import com.zhufuc.pctope.utils.DeleteFolder
import com.zhufuc.pctope.utils.Textures
import com.zhufuc.pctope.utils.mLog

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
                    DeleteFolder.Delete(it.path)
                }
            }.start()
        }
    }

}
