package com.askey.mobile.zwave.control.util;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.provider.OpenableColumns;
import android.util.Log;

import java.io.BufferedReader;
import java.io.FileDescriptor;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * 项目名称：ZwaveControlClient
 * 类描述：
 * 创建人：skysoft  charles.bai
 * 创建时间：2017/12/26 22:39
 * 修改人：skysoft
 * 修改时间：2017/12/26 22:39
 * 修改备注：
 */
public class UriUtils {
    private static final int WRITE_REQUEST_CODE = 43;
    /**根据uri获取文件参数*/
    public static void dumpImageMetaData(Context context, Uri uri) {
        Cursor cursor = context.getContentResolver().query(uri, null, null, null, null, null);
        try {
            if (cursor != null && cursor.moveToFirst()) {
                String displayName = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                Log.e("HeHe", "Display Name: " + displayName);
                int sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE);
                String size = null;
                if (!cursor.isNull(sizeIndex)) {
                    size = cursor.getString(sizeIndex);
                } else {
                    size = "Unknown";
                }
                Log.e("HeHe", "Size: " + size);
            }
        } finally {
            cursor.close();
        }
    }
    /**根据Uri获得Bitmap*/
    public static Bitmap getBitmapFromUri(Context context, Uri uri) throws IOException {
        ParcelFileDescriptor parcelFileDescriptor = context.getContentResolver().openFileDescriptor(uri, "r");
        FileDescriptor fileDescriptor = parcelFileDescriptor.getFileDescriptor();
        Bitmap image = BitmapFactory.decodeFileDescriptor(fileDescriptor);
        parcelFileDescriptor.close();
        return image;
    }
    /** 根据Uri获取输入流 */
    public static String readTextFromUri(Context context, Uri uri) throws IOException {
        InputStream inputStream = context.getContentResolver().openInputStream(uri);
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        StringBuilder stringBuilder = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            stringBuilder.append(line);
        }
        inputStream.close();
        reader.close();
        return stringBuilder.toString();
    }
    /** 创建文件，可在onActivityResult()中获取被创建文件的uri */
    public static void createFile(Activity context, String mimeType, String fileName) {
        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType(mimeType);
        intent.putExtra(Intent.EXTRA_TITLE, fileName);
        context.startActivityForResult(intent, WRITE_REQUEST_CODE);
    }
}