package com.askey.mobile.zwave.control.qrcode.zxing.encode;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.os.Environment;
import android.view.WindowManager;
import android.widget.LinearLayout;

import com.askey.mobile.zwave.control.util.Logg;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.ChecksumException;
import com.google.zxing.DecodeHintType;
import com.google.zxing.EncodeHintType;
import com.google.zxing.FormatException;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.NotFoundException;
import com.google.zxing.Result;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.QRCodeReader;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Hashtable;

/***
 * Company: Chengdu Skysoft Info&Tech Co., Ltd.
 * Copyright ©2014-2018 成都天软信息技术有限公司
 * Created by lisa on 2016/4/28.
 *
 * @since:JDK1.7
 * @version:1.0
 * @see
 ***/
public class EncodeingView extends LinearLayout {

    //    private static final int IMAGE_HALFWIDTH = 35;//宽度值，影响中间图片大小
    private int QR_WIDTH = 400, QR_HEIGHT = 400;

    public EncodeingView(Context context) {
        super(context);
    }

    private static final String TAG = "EncodeingView";

    /**
     * 生成二维码
     *
     * @param string      二维码中包含的文本信息
     * @param logo_bitmap logo图片
     * @param format      编码格式
     * @return Bitmap 位图
     * @throws WriterException
     */
    public Bitmap createCode(Activity context, String string, Bitmap logo_bitmap, BarcodeFormat format)
            throws WriterException {
        WindowManager wm = context.getWindowManager();

        int screenWidth = wm.getDefaultDisplay().getWidth();
        int screenHeight = wm.getDefaultDisplay().getHeight();
//影响中间图片的大小
        int IMAGE_HALFWIDTH = screenWidth / 12;
        Matrix m = new Matrix();
        float sx = (float) 2 * IMAGE_HALFWIDTH / logo_bitmap.getWidth();
        float sy = (float) 2 * IMAGE_HALFWIDTH
                / logo_bitmap.getHeight();
        m.setScale(sx, sy);//设置缩放信息
        //将logo图片按martix设置的信息缩放
        logo_bitmap = Bitmap.createBitmap(logo_bitmap, 0, 0,
                logo_bitmap.getWidth(), logo_bitmap.getHeight(), m, false);
        MultiFormatWriter writer = new MultiFormatWriter();
        //将二维码的容错率提高为H，即30%，中间可以显示的logo最大为screenWidth / 12
        Hashtable hints = new Hashtable();
        hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);
//        Hashtable<EncodeHintType, String> hst = new Hashtable<EncodeHintType, String>();
//        hst.put(EncodeHintType.CHARACTER_SET, "UTF-8");//设置字符编码
        int qrWidth = screenWidth / 2;
        Logg.i(TAG, "============imageWidth==================" + qrWidth);
        int qrHeight = qrWidth;
        BitMatrix matrix = writer.encode(string, format, qrWidth, qrHeight, hints);//生成二维码矩阵信息
        int width = matrix.getWidth();//矩阵高度
        int height = matrix.getHeight();//矩阵宽度
        int halfW = width / 2;
        int halfH = height / 2;
        int[] pixels = new int[width * height];//定义数组长度为矩阵高度*矩阵宽度，用于记录矩阵中像素信息
        for (int y = 0; y < height; y++) {//从行开始迭代矩阵
            for (int x = 0; x < width; x++) {//迭代列
                if (x > halfW - IMAGE_HALFWIDTH && x < halfW + IMAGE_HALFWIDTH
                        && y > halfH - IMAGE_HALFWIDTH
                        && y < halfH + IMAGE_HALFWIDTH) {//该位置用于存放图片信息
//记录图片每个像素信息
                    pixels[y * width + x] = logo_bitmap.getPixel(x - halfW
                            + IMAGE_HALFWIDTH, y - halfH + IMAGE_HALFWIDTH);
                } else {
                    if (matrix.get(x, y)) {//如果有黑块点，记录信息
                        pixels[y * width + x] = 0xff000000;//记录黑块信息
                    }
                }

            }
        }
        Bitmap bitmap = Bitmap.createBitmap(width, height,
                Bitmap.Config.ARGB_8888);
        // 通过像素数组生成bitmap
        bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
        return bitmap;
    }


    /**
     * 生成只有文字的二维码
     */
    public Bitmap createTextImage(String text) {
        Bitmap bitmap = null;
        try {
            // 需要引入zxing.jar包
            QRCodeWriter writer = new QRCodeWriter();
            if (text == null || "".equals(text) || text.length() < 1) {
                return null;
            }

            // 把文本信息转化为二维码
            BitMatrix martix = writer.encode(text, BarcodeFormat.QR_CODE,
                    QR_WIDTH, QR_HEIGHT);

            System.out.println("w:" + martix.getWidth() + "h:"
                    + martix.getHeight());

            Hashtable<EncodeHintType, String> hints = new Hashtable<EncodeHintType, String>();
            hints.put(EncodeHintType.CHARACTER_SET, "utf-8");
            BitMatrix bitMatrix = new QRCodeWriter().encode(text,
                    BarcodeFormat.QR_CODE, QR_WIDTH, QR_HEIGHT, hints);
            int[] pixels = new int[QR_WIDTH * QR_HEIGHT];
            for (int y = 0; y < QR_HEIGHT; y++) {
                for (int x = 0; x < QR_WIDTH; x++) {
                    if (bitMatrix.get(x, y)) {
                        pixels[y * QR_WIDTH + x] = 0xff000000;
                    } else {
                        pixels[y * QR_WIDTH + x] = 0xffffffff;
                    }

                }
            }

            bitmap = Bitmap.createBitmap(QR_WIDTH, QR_HEIGHT,
                    Bitmap.Config.ARGB_8888);

            bitmap.setPixels(pixels, 0, QR_WIDTH, 0, 0, QR_WIDTH, QR_HEIGHT);

            System.out.println(Environment.getExternalStorageDirectory());

            try {
                saveMyBitmap(bitmap, "code");
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        } catch (WriterException e) {
            e.printStackTrace();
        }
        return bitmap;
    }


    /**
     * 解析QR图片  返回字符串
     **/
    private String scanningImage(Bitmap bitmap) {
        String qr_result = "";
        Hashtable<DecodeHintType, String> hints = new Hashtable<DecodeHintType, String>();
        hints.put(DecodeHintType.CHARACTER_SET, "utf-8");
        // 获得待解析的图片
//        Bitmap bitmap = ((BitmapDrawable) qr_image.getDrawable()).getBitmap();
        RGBLuminanceSource source = new RGBLuminanceSource(bitmap);
        BinaryBitmap bitmap1 = new BinaryBitmap(new HybridBinarizer(source));
        QRCodeReader reader = new QRCodeReader();
        Result result;
        try {
            result = reader.decode(bitmap1);
            // 得到解析后的文字
            result = reader.decode(bitmap1, hints);
        } catch (NotFoundException e) {
            e.printStackTrace();
        } catch (ChecksumException e) {
            e.printStackTrace();
        } catch (FormatException e) {
            e.printStackTrace();
        }
        return qr_result;
    }


    public void saveMyBitmap(Bitmap bm, String bitName) throws IOException {
        try {
            File f = new File("/mnt/sdcard/" + bitName + ".png");
            f.createNewFile();
            FileOutputStream fOut = null;
            fOut = new FileOutputStream(f);
            bm.compress(Bitmap.CompressFormat.PNG, 100, fOut);
            fOut.flush();
            fOut.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

    }


}
