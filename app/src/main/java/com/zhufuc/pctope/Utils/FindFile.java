package com.zhufuc.pctope.Utils;

import java.io.File;

/**
 * Created by zhufu on 17-8-10.
 */

public class FindFile {
    public static File withKeywordOnce(String keyword,String baseForm){
        File[] listBase = new File(baseForm).listFiles();
        for (File n : listBase){
            if (n.isFile()){
                String str = n.getPath();
                int keyLength = keyword.length(),start = str.lastIndexOf('/')+1;
                if(str.indexOf(keyword,start)!=-1)
                    return n;
            }
            else{
                File mayReturn = FindFile.withKeywordOnce(keyword,n.getPath());
                if (mayReturn != null) return mayReturn;
            }
        }
        return null;
    }
}
