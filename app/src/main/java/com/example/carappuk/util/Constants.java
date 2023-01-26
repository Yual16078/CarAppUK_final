package com.example.carappuk.util;

import android.os.Environment;

import java.io.File;

/**
 * 常量
 */
public class Constants {

    public static String FILE_PATH = Environment.getExternalStorageDirectory() + File.separator + "camerax_media";

    public static String getFilePath() {
        if (!new File(FILE_PATH).exists()) {
            new File(FILE_PATH).mkdirs();
        }
        return FILE_PATH;
    }
}
