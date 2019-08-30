package com.xzp.firebasechatdemo.utils;

import android.Manifest;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.widget.Toast;

import com.xzp.firebasechatdemo.MainActivity;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class   CompressUtils {

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public static String handleImageOnKitKat(Intent data, Context context,Activity activity) {

        String imagePath = null;

        Uri uri = data.getData();



        if (DocumentsContract.isDocumentUri(context, uri)) {

            String documentId = DocumentsContract.getDocumentId(uri);

            if ("com.android.providers.media.documents".equals(uri.getAuthority())) {

                String id = documentId.split(":")[1];

                String selection = MediaStore.Images.Media._ID + "=" + id;

                imagePath = getImagePath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, selection,context,activity);


            } else if ("com.android.providers.downloads.documents".equals(uri.getAuthority())) {

                Uri contentUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"), Long.valueOf(documentId));

                imagePath = getImagePath(contentUri, null,context,activity);
            }





        } else if ("content".equalsIgnoreCase(uri.getScheme())) {

            imagePath = getImagePath(uri, null,context,activity);





        } else if ("file".equalsIgnoreCase(uri.getScheme())) {

            imagePath = uri.getPath();





        }

        //displayPath(imagePath,context);
        return imagePath;

    }



    public static String handleImageBeforeKitkat(Intent data, Context context,Activity activity) {

        Uri uri = data.getData();

        String imagePath = getImagePath(uri, null,context,activity);
        //displayPath(imagePath,context);
        return imagePath;

    }



    public static File displayPath(String imagePath, Context context,Activity activity) {
        File file= null;

        if (imagePath != null) {

            Bitmap bitmap = BitmapFactory.decodeFile(imagePath);

            //压缩图片

            Bitmap bitmap1 = compressBySampleSize(bitmap, 2, true);



            //保存图片
            if ((ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
            ){
                ActivityCompat.requestPermissions(activity,new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,},1);

            }else {
                file = saveBmp2Gallery(bitmap1, System.currentTimeMillis() + "", context);
            }

        } else {

            Toast.makeText(context, "获得图片失败", Toast.LENGTH_SHORT).show();

        }
        return file;

    }


    /**
     * 获取图片路径
     * @param uri
     * @param selection
     * @param context
     * @return
     */

    public static String getImagePath(Uri uri, String selection, Context context, Activity activity) {

        String path = null;
        Cursor cursor = null;
        if ((ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
        ){
            ActivityCompat.requestPermissions(activity,new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,},1);

        }else {

             cursor = context.getContentResolver().query(uri, null, selection, null, null);
        }

        if (cursor != null) {

            if (cursor.moveToFirst()) {

                path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));

            }

            cursor.close();

        }



        return path;

    }





    /**
     * @param bmp     获取的bitmap数据
     * @param picName 自定义的图片名
     */

    public static File saveBmp2Gallery(Bitmap bmp, String picName, Context context) {
        String fileName = null;
        //系统相册目录
        String galleryPath = Environment.getExternalStorageDirectory()
                + File.separator + Environment.DIRECTORY_DCIM
                + File.separator + "Camera" + File.separator;
        // 声明文件对象
        File file = null;
        // 声明输出流
        FileOutputStream outStream = null;
        try {

            // 如果有目标文件，直接获得文件对象，否则创建一个以filename为名称的文件
            file = new File(galleryPath, picName + ".jpg");
            // 获得文件相对路径

            fileName = file.toString();

            // 获得输出流，如果文件中有内容，追加内容

            outStream = new FileOutputStream(fileName);

            if (null != outStream) {

                bmp.compress(Bitmap.CompressFormat.JPEG, 20, outStream);

            }
        } catch (Exception e) {

            e.getStackTrace();

        } finally {

            try {

                if (outStream != null) {

                    outStream.close();

                }

            } catch (IOException e) {

                e.printStackTrace();

            }

        }
        //通知相册更新
        MediaStore.Images.Media.insertImage(context.getContentResolver(),

                bmp, fileName, null);

        Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);

        Uri uri = Uri.fromFile(file);

        intent.setData(uri);

        context.sendBroadcast(intent);



        //Toast.makeText(context, "已保存"+fileName, Toast.LENGTH_LONG).show();
        return file;


    }



    //采样压缩

    public static Bitmap compressBySampleSize(final Bitmap src, final int sampleSize, final boolean recycle) {

        if (src == null) {

            return null;

        }

        BitmapFactory.Options options = new BitmapFactory.Options();

        options.inSampleSize = sampleSize;

        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        src.compress(Bitmap.CompressFormat.JPEG, 100, baos);

        byte[] bytes = baos.toByteArray();

        if (recycle && !src.isRecycled()) {

            src.recycle();

        }

        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length, options);

    }
    /**
     * 删除相册指定路径文件
     */
    public static void deleteImage(String imgPath, Context context) {
        ContentResolver resolver = context.getContentResolver();
        Cursor cursor = MediaStore.Images.Media.query(resolver, MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                new String[]{MediaStore.Images.Media._ID}, MediaStore.Images.Media.DATA + "=?",
                new String[]{imgPath}, null);
        boolean result = false;
        Uri uri = null;
        if (cursor.moveToFirst()) {
            long id = cursor.getLong(0);
            Uri contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
            uri = ContentUris.withAppendedId(contentUri, id);
            int count = context.getContentResolver().delete(uri, null, null);
            result = count == 1;
        } else {

            Cursor cursor2 = MediaStore.Images.Media.query(resolver, MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                    new String[]{MediaStore.Images.Media._ID}, MediaStore.Images.Media.DATA + "=?",
                    new String[]{imgPath}, null);
            if (cursor2.moveToFirst()) {
                long id = cursor2.getLong(0);
                Uri contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                uri = ContentUris.withAppendedId(contentUri, id);
                int count = context.getContentResolver().delete(uri, null, null);
                result = count == 1;
            }

        }
//更新到图库
        Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        intent.setData(Uri.fromFile(new File(imgPath)));
        context.sendBroadcast(intent);

    }

}
