package com.zhufuc.pctope.Interf

import android.support.design.widget.Snackbar

import com.zhufuc.pctope.Utils.DeleteFolder
import com.zhufuc.pctope.Utils.Textures
import com.zhufuc.pctope.Utils.mLog

import java.io.File

/**
 * Created by zhufu on 17-7-29.
 */

class DeletingCallback(whatWillBeDeleted: List<Textures>) : Snackbar.Callback() {

    private var WhatWillBeDeleted = whatWillBeDeleted

    override fun onDismissed(snackbar: Snackbar?, event: Int) {
        super.onDismissed(snackbar, event)
        if (event != Snackbar.Callback.DISMISS_EVENT_ACTION) {


            Thread(Runnable { WhatWillBeDeleted.forEach({
                mLog.i("Manager","Deleting ${it.path}")
                DeleteFolder.Delete(it.path)
            }) }).start()
        }
    }

}
