package com.zhufuc.pctope.Interf;

import android.support.design.widget.Snackbar;

import com.zhufuc.pctope.Tools.DeleteFolder;

import java.io.File;

/**
 * Created by zhufu on 17-7-29.
 */

public class DeletingCallback extends Snackbar.Callback{
    private static File WhatWillBeDeleted;

    public DeletingCallback(File whatWillBeDeleted){
        this.WhatWillBeDeleted = whatWillBeDeleted;
    }

    @Override
    public void onDismissed(Snackbar snackbar, int event){
        super.onDismissed(snackbar,event);
        if (event !=DISMISS_EVENT_ACTION){
            new Thread(new Runnable() {
                @Override
                public void run() {
                    DeleteFolder.Delete(WhatWillBeDeleted.getPath());
                }
            }).start();

        }
    }

}
