package com.zhufuc.pctope.Interf

import android.support.design.widget.Snackbar

import com.zhufuc.pctope.Utils.DeleteFolder

import java.io.File

/**
 * Created by zhufu on 17-7-29.
 */

class DeletingCallback(whatWillBeDeleted: File) : Snackbar.Callback() {

    private var WhatWillBeDeleted: File = whatWillBeDeleted

    override fun onDismissed(snackbar: Snackbar?, event: Int) {
        super.onDismissed(snackbar, event)
        if (event != Snackbar.Callback.DISMISS_EVENT_ACTION) {
            Thread(Runnable { DeleteFolder.Delete(WhatWillBeDeleted.path) }).start()
        }
    }

}
