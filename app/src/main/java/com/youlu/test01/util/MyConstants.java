package com.youlu.test01.util;

import android.os.Environment;

public class MyConstants {
    public static final String TEST02_PATH = Environment
           .getExternalStorageDirectory().getPath()
            + "/youlu/test02/";

    public static final String CACHE_FILE_PATH = TEST02_PATH + "usercache";

    public static final int MSG_FROM_CLIENT = 0;
    public static final int MSG_FROM_SERVICE = 1;

}
