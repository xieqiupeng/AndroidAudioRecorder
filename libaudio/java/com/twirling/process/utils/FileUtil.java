package com.twirling.process.utils;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Created by xieqi on 2016/9/29.
 */
public class FileUtil {
    public static final String PATH_DOWNLOAD = Environment.getExternalStorageDirectory() + "/" + Environment.DIRECTORY_DOWNLOADS;

    // 都是相对路径，一一对应
    public static String copyAssetFileToFiles(Context context, String filename) {
        copyAssetFileToFiles(context, filename, new File(PATH_DOWNLOAD + "/" + filename));
        return PATH_DOWNLOAD + "/" + filename;
    }

    private static boolean copyAssetFileToFiles(Context context, String filename, File of) {
        InputStream is = null;
        FileOutputStream os = null;
        try {
            is = context.getAssets().open(filename);
            createFile(of);
            os = new FileOutputStream(of);
            int readedBytes;
            byte[] buf = new byte[1024];
            while ((readedBytes = is.read(buf)) > 0) {
                os.write(buf, 0, readedBytes);
            }
            os.flush();
            is.close();
            os.close();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean mkdir(File file) {
        while (!file.getParentFile().exists()) {
            mkdir(file.getParentFile());
        }
        return file.mkdir();
    }

    public static boolean createFile(File file) {
        try {
            if (!file.getParentFile().exists()) {
                mkdir(file.getParentFile());
            }
            return file.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    // 读取assets文件
    public static String readFromAsset(Context context, String fileName) {
        InputStream is = null;
        BufferedReader br = null;
        try {
            is = context.getAssets().open(fileName);
            br = new BufferedReader(new InputStreamReader(is));
            String addonStr = "";
            String line = br.readLine();
            while (line != null) {
                addonStr = addonStr + line;
                line = br.readLine();
            }
            return addonStr;
        } catch (Exception e) {
            return null;
        } finally {
            try {
                br.close();
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static File writeFileFromShort(short[] changeData) {
        File file = new File(PATH_DOWNLOAD + File.separator + "short.wav");
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        FileOutputStream fos = null;
        DataOutputStream dos = null;
        try {
            fos = new FileOutputStream(file);
            dos = new DataOutputStream(fos);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        for (short j : changeData) {
            try {
                dos.writeShort(j);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try {
            dos.flush();
            fos.flush();
            dos.close();
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Log.w("fileLength", file.length() + "");
        return file;
    }
}
