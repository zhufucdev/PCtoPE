package com.zhufucdev.pctope.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

/**
 * Created by zhufu on 17-9-24.
 */

public class FileType {
    private static String bytesToHexString(byte[] src){
        StringBuilder stringBuilder = new StringBuilder();
        if (src == null || src.length <= 0) {
            return null;
        }
        for (byte aSrc : src) {
            int v = aSrc & 0xFF;
            String hv = Integer.toHexString(v);
            if (hv.length() < 2) {
                stringBuilder.append(0);
            }
            stringBuilder.append(hv);
        }
        return stringBuilder.toString();
    }

    public static String get(File file){
        byte[] b = new byte[4];
        try {
            FileInputStream fis = new FileInputStream(file);
            fis.read(b ,0 ,b.length);
            return bytesToHexString(b);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
