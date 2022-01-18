package com.scc.datastorage.utils;

import android.Manifest;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.format.DateUtils;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;

/**
 * 功能描述：将图片文件保存至本地
 */
public class PictureStorageUtils {
    public static boolean isSaveImage(Context context, Bitmap bm, String name) {
        boolean isSave;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            //大于等于android 10
            isSave = saveImageQ(context, bm, name);
        } else {
            isSave = saveImage(context, bm, name);
        }
        return isSave;
    }

    private static boolean saveImage(Context context, Bitmap outB, String name) {
        String imgName = name.isEmpty()?String.valueOf(System.currentTimeMillis()):name;
        //File.separator就是文件路径
        String fileName = Environment.getExternalStorageDirectory() + File.separator + "DCIM"
                + File.separator + "demo" + File.separator;
        try {
            File file = new File(fileName);
            if (!file.exists()) {
                file.mkdirs();
            }
            Log.e("File","saveAndGetImage:" + file);
            File filePath = new File(file + "/" + imgName + ".png");
            Log.e("File","filePath:" + filePath);
            FileOutputStream out = new FileOutputStream(filePath); //保存到本地，格式为JPEG
            if (outB.compress(Bitmap.CompressFormat.PNG, 100, out)) {
                out.flush();
                out.close();
            }
            Log.e("File","saveAndGetImage:END");
            //刷新图库
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {//高于22版本要手动授权
                // 检查该权限是否已经获取
                int i = ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE);
                // 权限是否已经 授权 GRANTED---授权  DINIED---拒绝
                if (i != PackageManager.PERMISSION_GRANTED) {
                    // 提示用户应该去应用设置界面手动开启权限
                } else {
                    context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(filePath)));
                }
            } else {
                context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(filePath)));
            }
            return true;
        } catch (FileNotFoundException e) {
            Log.e("File","FileNotFoundException e.toString: " + e.toString());
            e.printStackTrace();
            return false;
        } catch (IOException e) {
            Log.e("File","IOException e.toString: " + e.toString());
            e.printStackTrace();
            return false;
        }
    }

    //功能描述：Android10及以上保存图片到相册
    @RequiresApi(api = Build.VERSION_CODES.Q)
    private static boolean saveImageQ(Context context, Bitmap image, String name) {
        long mImageTime = System.currentTimeMillis();
        String mImageFileName = name.isEmpty()?String.valueOf(mImageTime):name;
        final ContentValues values = new ContentValues();
        values.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DCIM
                + File.separator + "demo"); //图库(DCIM)中显示的文件夹名。
        values.put(MediaStore.MediaColumns.DISPLAY_NAME, mImageFileName);
        values.put(MediaStore.MediaColumns.MIME_TYPE, "image/png");
        values.put(MediaStore.MediaColumns.DATE_ADDED, mImageTime / 1000);
        values.put(MediaStore.MediaColumns.DATE_MODIFIED, mImageTime / 1000);
        values.put(MediaStore.MediaColumns.DATE_EXPIRES, (mImageTime + DateUtils.DAY_IN_MILLIS) / 1000);
        values.put(MediaStore.MediaColumns.IS_PENDING, 1);
        Log.e("File",values.get(MediaStore.MediaColumns.RELATIVE_PATH).toString());
        ContentResolver resolver = context.getContentResolver();
        final Uri uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
        try {
            //写下我们截图的实际数据
            try (OutputStream out = resolver.openOutputStream(uri)) {
                if (!image.compress(Bitmap.CompressFormat.PNG, 100, out)) {
                    throw new IOException("Failed to compress");
                }
            }
            //一切都很顺利
            values.clear();
            values.put(MediaStore.MediaColumns.IS_PENDING, 0);
            values.putNull(MediaStore.MediaColumns.DATE_EXPIRES);
            resolver.update(uri, values, null, null);
            return true;
        } catch (IOException e) {
            Log.e("File",e.getMessage());
            return false;
        }
    }
}