package com.scc.datastorage;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;

import com.scc.datastorage.databinding.ActivityFileStorageBinding;
import com.scc.datastorage.utils.KeybordUtil;
import com.scc.datastorage.utils.PictureStorageUtils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

public class FileStorageActivity extends AppCompatActivity {
    ActivityFileStorageBinding fileStorageBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        fileStorageBinding = ActivityFileStorageBinding.inflate(getLayoutInflater());
        setContentView(fileStorageBinding.getRoot());

        //From internal storage, getFilesDir() or getCacheDir()
        File filesDir = getFilesDir();//持久文件目录
        //File: FilesDir：/data/user/0/com.scc.datastorage/files
        Log.e("File", "FilesDir：" + filesDir.getAbsolutePath());
        File cacheDir = getCacheDir();//缓存文件目录
        //File: CacheDir：/data/user/0/com.scc.datastorage/cache
        Log.e("File", "CacheDir：" + cacheDir.getAbsolutePath());

        //From external storage, getExternalFilesDir() or getExternalCacheDir()
        //因为没有存储卡等外置设备就不就外置存储做测试了。
        fileStorageBinding.btnWrite.setOnClickListener(v -> write());
        fileStorageBinding.btnRead.setOnClickListener(v -> read());
        fileStorageBinding.btnFilelist.setOnClickListener(v -> filelist());
        fileStorageBinding.btnDelete.setOnClickListener(v -> delete());

        fileStorageBinding.btnCreateCache.setOnClickListener(v -> createCache());
        fileStorageBinding.btnDeleteCache.setOnClickListener(v -> deleteCache());
        fileStorageBinding.btnBitmap.setOnClickListener(v -> saveBitmap());
    }

    //删除缓存文件
    private void deleteCache() {
        //scc20221181329566644563700891.tmp和scc2022118都存在
        //但是cache目录下没有，咱们调用删除试试
        File cacheFile = new File(getCacheDir(), "scc20221181329566644563700891.tmp");
        //判断文件是否存在
        if (cacheFile.exists()) {
            //使用File对象的delete()方法
            Log.e("File", "delete17:" + cacheFile.delete());
        }

        File cacheFile2 = new File(getCacheDir(), "scc2022118");
        if (cacheFile2.exists()) {
            //应用Context的deleteFile()方法
            Log.e("File", "deleteFile19:" + deleteFile("scc2022118"));
        }
    }

    //创建缓存文件
    private void createCache() {
        try {
            //方法一
            File timpfile = File.createTempFile("scc202225", ".txt", this.getCacheDir());
            Log.e("File", "是否存在：" + timpfile.exists());
            Log.e("File", "timpfile：" + timpfile.getName());
        } catch (IOException e) {
            e.printStackTrace();
        }
        //方法二：创建文件失败
        File file = new File(this.getCacheDir(), "file2022");
        Log.e("File", "是否存在：" + file.exists());
        Log.e("File", "file：" + file.getAbsolutePath());
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        KeybordUtil.closeKeybord(this);
        return super.onTouchEvent(event);
    }

    //删除文件
    public void delete() {
        String filename = fileStorageBinding.etInput.getText().toString();
        Log.e("File", "Delete：" + deleteFile(filename));
        filelist();
    }

    //查看文件列表
    public void filelist() {
        String[] files = fileList();
        for (String file : files) {
            Log.e("File", "FileList：" + file);
        }
    }

    //写入文件
    public void write() {
        //文件名
        String filename = fileStorageBinding.etInput.getText().toString();
        //写入内容(shuaici)
        String fileContents = fileStorageBinding.etInput.getText().toString();
        //内容不能为空
        if (fileContents.isEmpty()) {
            Log.e("File", "FileContents.isEmpty()");
            return;
        }
        BufferedWriter writer = null;
        try {
            //获取FileOutputStream对象
            FileOutputStream fos = openFileOutput(filename, Context.MODE_PRIVATE);
            //通过OutputStreamWriter构建出一个BufferedWriter对象
            writer = new BufferedWriter(new OutputStreamWriter(fos));
            //通过BufferedWriter对象将文本内容写入到文件中
            writer.write(fileContents);
        } catch (IOException e) {
            Log.e("File", e.getMessage());
        } finally {
            try {
                //不管是否抛异常，都手动关闭输入流
                if (writer != null) writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    //读取文件
    public void read() {
        Log.e("File", "read.start");
        String filename = "sccFile";
        BufferedReader reader = null;
        try {
            //获取FileInputStream对象
            FileInputStream fis = openFileInput(filename);
            //通过InputStreamReader构建出一个BufferedReader对象
            reader = new BufferedReader(new InputStreamReader(fis));
            StringBuilder sb = new StringBuilder();
            String line;
            //一行一行的读取，当数据为空时结束循环
            while ((line = reader.readLine()) != null) {
                sb.append(line);//将数据添加到StringBuilder
            }
            Log.e("File", sb.toString());
            fileStorageBinding.etInput.setText(sb.toString());
        } catch (IOException e) {
            Log.e("File", e.getMessage());
        } finally {
            Log.e("File", "read.finally");
            try {
                if (reader != null) reader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void saveBitmap() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // 检查该权限是否已经获取
            int i = ContextCompat.checkSelfPermission(FileStorageActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
            // 权限是否已经 授权 GRANTED---授权  DINIED---拒绝
            if (i != PackageManager.PERMISSION_GRANTED) {
                // 如果没有授予该权限，就去提示用户请求
                startRequestPermission();
            } else {
                resourceBitmap();
            }
        } else {
            resourceBitmap();
        }
    }

    private void resourceBitmap() {
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.ceshi);
        boolean isSave = PictureStorageUtils.isSaveImage(this, bitmap, "sccgx");
        Log.e("File","isSave:"+isSave);
    }

    //请求写入权限
    private void startRequestPermission() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 321);
    }

    /**
     * 用户权限 申请 的回调方法
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 321) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                // 判断用户是否 点击了不再提醒。(检测该权限是否还可以申请)
                int i = ContextCompat.checkSelfPermission(FileStorageActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
                // 权限是否已经 授权 GRANTED---授权  DINIED---拒绝
                if (i == PackageManager.PERMISSION_GRANTED) {
                    //已授权
                    saveBitmap();
                }
            }
        }
    }
}