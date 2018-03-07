package com.askey.mobile.zwave.control.home.activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.askey.mobile.zwave.control.R;
import com.askey.mobile.zwave.control.util.Logg;
import com.askey.mobile.zwave.control.util.SystemUtil;
import com.askey.mobile.zwave.control.util.UriPathUtils;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class TakePictureActivity extends AppCompatActivity implements View.OnClickListener, SurfaceHolder.Callback {
    private static String TAG = "TakePictureActivity";
    private SurfaceView preview;
    private ImageView cancel, choose_photo, take_pic;
    private boolean isOpenCamera = false;
    private Camera mCamera;
    private SurfaceHolder surfaceHolder;
    private static Handler mHandler;
    private String rootDir;
    private DisplayMetrics metrics;
    private static final int PICK_IMAGE = 1;
    private boolean AlbumPermission = false;
    private boolean safeToTakePicture = false;
    private CameraSizeComparator sizeComparator = new CameraSizeComparator();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_take_picture);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, 1);
        }

        /*
            有存储权限，检查文件夹是否存在
         */
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            AlbumPermission = true;
            if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                rootDir = Environment.getExternalStorageDirectory().toString() + "/Zwave";
//                rootDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).toString() + "/Zwave";
                Log.d(TAG, rootDir);
                File dir = new File(rootDir);
                if (!dir.exists()) {
                    dir.mkdir();
                }
            }
        } else {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, 2);
            }
        }

        initView();
    }


    @Override
    protected void onResume() {
        super.onResume();
        Logg.i(TAG,"===getDeviceBrand=="+SystemUtil.getDeviceBrand());
    }

    private void initView() {
        Log.d(TAG, "initview");
        preview = (SurfaceView) findViewById(R.id.preview);
        surfaceHolder = preview.getHolder();
        surfaceHolder.addCallback(this);
        cancel = (ImageView) findViewById(R.id.cancel);
        cancel.setOnClickListener(this);
        choose_photo = (ImageView) findViewById(R.id.choose_photo);
        choose_photo.setOnClickListener(this);
        take_pic = (ImageView) findViewById(R.id.take_pic);
        take_pic.setOnClickListener(this);

        HandlerThread picHandlerThread = new HandlerThread("photoThread");
        picHandlerThread.start();
        mHandler = new Handler(picHandlerThread.getLooper());
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            if (requestCode == 1) {
                configCamera(mCamera);
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, 2);
                }
            }
            if (requestCode == 2) {
                AlbumPermission = true;
                if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                    rootDir = Environment.getExternalStorageDirectory().toString() + "/Zwave";
//                    rootDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).toString() + "/Zwave";
                    Log.d(TAG, rootDir);
                    File dir = new File(rootDir);
                    if (!dir.exists()) {
                        dir.mkdir();
                    }
                }
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
//        Log.d(TAG,data.getData()+"");
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case PICK_IMAGE:
                    String path = "";
                    Log.d(TAG, "====onActivityResult========"+data.getData().toString());
//                    if(SystemUtil.getDeviceBrand().equals("htc")){
                        path = UriPathUtils.getPath(this, data.getData());
                    Log.d(TAG, "==onActivityResult===path======="+path);
//                    }else{
//                        path = getRealPathFromURI(data.getData());
//                    }
                    Intent intent = new Intent(TakePictureActivity.this, PreviewPhotoActivity.class);
                    intent.putExtra("path", path);
                    startActivity(intent);
                    finish();
                    break;
            }
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.cancel:
                System.gc();
                finish();
                break;
            case R.id.choose_photo:
                if (AlbumPermission) {
//                    Intent intent = new Intent(Intent.ACTION_PICK);
//                    intent.setData(MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
//                    startActivityForResult(intent, PICK_IMAGE);
                    Intent intent = new Intent();
                    intent.setType("image/*");
                    intent.setAction(Intent.ACTION_GET_CONTENT);
                    startActivityForResult(intent, PICK_IMAGE);
                } else {
                    Toast.makeText(this, "Please open permission", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.take_pic:
                if (null != mCamera && safeToTakePicture) {
                    mCamera.takePicture(null, null, new Camera.PictureCallback() {
                        @Override
                        public void onPictureTaken(final byte[] data, Camera camera) {
                            Log.d(TAG, "currentThread: "+Thread.currentThread().getName());
                            Runnable runnable = new Runnable() {
                                @Override
                                public void run() {
                                    Bitmap bitmap = null;
                                    BufferedOutputStream bos = null;
                                    if (rootDir == null || rootDir.equals("")) {
                                        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                                            rootDir = Environment.getExternalStorageDirectory().toString() + "/Zwave";
//                                            rootDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).toString() + "/Zwave";
                                            File dir = new File(rootDir);
                                            if (!dir.exists()) {
                                                dir.mkdir();
                                            }
                                        }
                                    }
                                    String photoPath = rootDir + File.separator + System.currentTimeMillis() + ".jpeg";
                                    try {
                                        bos = new BufferedOutputStream(new FileOutputStream(photoPath));
                                        Log.d(TAG, "==============1111, "+data.length);
                                        bitmap = adjustPhotoRotationToPortrait(data, metrics.widthPixels, metrics.heightPixels);//照片的方向不对，此处旋转90度
                                        Log.d(TAG, "==============2222");
                                        if (bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bos)) {
                                            Log.d(TAG, "==============3333");
                                            Intent intent = new Intent(TakePictureActivity.this, PreviewPhotoActivity.class);
                                            intent.putExtra("path", photoPath);
                                            startActivity(intent);
                                            finish();
                                        }
                                    } catch (FileNotFoundException e) {
                                        e.printStackTrace();
                                    } finally {
                                        if (null != mCamera) {
                                            mCamera.startPreview();
                                        }
                                        if (bos != null) {
                                            try {
                                                bos.close();
                                            } catch (IOException e) {
                                                e.printStackTrace();
                                            }
                                        }
                                    }
                                }
                            };
                            mHandler.post(runnable);
                        }
                    });
                    safeToTakePicture = false;
                }
                break;
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        Log.d(TAG, "===surfaceCreated");
        configCamera(mCamera);
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        Log.d(TAG, "===surfaceChanged");
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        Log.d(TAG, "===surfaceDestroyed");
        if (!isOpenCamera) {
            return;
        }
        if (mCamera != null) {
            freeCameraResource();
        }
    }

    private void configCamera(Camera camera) {
        if (camera != null) {
            freeCameraResource();
        }
        try {
            camera = android.hardware.Camera.open();
            mCamera = camera;
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (camera == null)
            return;

        try {
            camera.setPreviewDisplay(surfaceHolder);
            metrics = getResources().getDisplayMetrics();
            setCameraParams(camera, metrics.widthPixels, metrics.heightPixels);
            camera.startPreview();
            safeToTakePicture = true;
            isOpenCamera = true;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void freeCameraResource() {
        Log.d(TAG, "freecamera");
        if (mCamera != null) {
            mCamera.stopPreview();
            mCamera.setPreviewCallback(null);
            mCamera.release();
            mCamera = null;
        }
        isOpenCamera = false;
    }

    private void setCameraParams(Camera camera, int width, int height) {
        Log.i(TAG, "setCameraParams  width=" + width + "  height=" + height);
        Camera.Parameters parameters = camera.getParameters();
        // 获取摄像头支持的PictureSize列表
        List<Camera.Size> pictureSizeList = parameters.getSupportedPictureSizes();
        for (Camera.Size size : pictureSizeList) {
            Log.i(TAG, "pictureSizeList size.width=" + size.width + "  size.height=" + size.height);
        }
        /*从列表中选取合适的分辨率*/
        Camera.Size picSize = getProperSize(pictureSizeList, ((float) height / width));
        if (null != picSize) {
            Log.i(TAG, "null != picSize , width: "+picSize.width+", height :"+picSize.height);
            parameters.setPictureSize(picSize.width, picSize.height);
        } else {
            Log.i(TAG, "null == picSize");
            picSize = parameters.getPictureSize();//如果没有匹配到最佳的分辨率，使用默认分辨率生成的照片会很大，可能会内存溢出
            parameters.setPictureSize(picSize.width, picSize.height);
            Log.i(TAG, "picSize.width=" + picSize.width + "  picSize.height=" + picSize.height);
        }
        // 根据选出的PictureSize重新设置SurfaceView大小
        float w = picSize.width;
        float h = picSize.height;
        preview.setLayoutParams(new RelativeLayout.LayoutParams((int) (height * (h / w)), height));

        // 获取摄像头支持的PreviewSize列表
        List<Camera.Size> previewSizeList = parameters.getSupportedPreviewSizes();
        for (Camera.Size size : previewSizeList) {
            Log.i(TAG, "previewSizeList size.width=" + size.width + "  size.height=" + size.height);
        }
        Camera.Size preSize = getProperSize(previewSizeList, ((float) height) / width);
        if (null != preSize) {
            Log.i(TAG, "preSize.width=" + preSize.width + "  preSize.height=" + preSize.height);
            parameters.setPreviewSize(preSize.width, preSize.height);
        }
        parameters.setPictureFormat(ImageFormat.JPEG);
        parameters.setJpegQuality(100); // 设置照片质量
        if (parameters.getSupportedFocusModes().contains(android.hardware.Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
            parameters.setFocusMode(android.hardware.Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);// 连续对焦模式
        }
        parameters.setRotation(90);//设置拍照的方向，默认是水平的；但是这里设置此项并没有效果，只能通过矩阵变换的方式来实现
        parameters.set("orientation", "portrait");//portrait竖屏，landscape横屏
        camera.cancelAutoFocus();//自动对焦
        camera.setDisplayOrientation(90);// 设置PreviewDisplay的方向，效果就是将捕获的画面旋转多少度显示
        try {
            camera.setParameters(parameters);
        } catch (Exception e) {
            Log.i(TAG, "=====Crash=====");
            e.printStackTrace();
        }
    }

    private Camera.Size getProperSize(List<Camera.Size> pictureSizeList, float screenRatio) {
        Log.i(TAG, "screenRatio=" + screenRatio);
        Collections.sort(pictureSizeList, sizeComparator);//降序排列
        Camera.Size result = null;
        float currentRatio;
        for (Camera.Size size : pictureSizeList) {
            currentRatio = ((float) size.width) / size.height;
            if (size.width < 2000 && Math.abs(currentRatio - screenRatio) <= 0.2) {//选取宽度小于2000且比例误差小于0.2的分辨率
                result = size;
                break;
            }
        }

        if (null == result) {
            for (Camera.Size size : pictureSizeList) {
                float curRatio = ((float) size.width) / size.height;
                if (curRatio - 4f / 3 <= 0.2) {// 默认w:h = 4:3
                    result = size;
                    break;
                }
            }
        }

        return result;
    }

    private Bitmap adjustPhotoRotationToPortrait(byte[] data, int currentWidth, int currentHeight) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeByteArray(data, 0, data.length, options);
        int w = options.outWidth;
        int h = options.outHeight;
        Log.d(TAG, "adj, w = "+w+", h = "+h);
        int halfWidth;
        int halfHeight;
        int inSampleSize = 1;//基准放缩比例

        //计算合适的放缩比例
        if (w > currentWidth || h > currentHeight) {
            halfWidth = w/2;
            halfHeight = h/2;
            while ((halfWidth / inSampleSize) > currentWidth && (halfHeight / inSampleSize) > currentHeight) {
                inSampleSize *= 2;
            }
            Log.d(TAG,"halfWidth = "+halfWidth);
            Log.d(TAG,"halfHeight = "+ halfHeight);
        }

        options.inSampleSize = inSampleSize;
        options.inJustDecodeBounds = false;

        if (options.outHeight < options.outWidth) {
            Matrix mtx = new Matrix();
            mtx.postRotate(90);
            // Rotating Bitmap
            Bitmap bmp = BitmapFactory.decodeByteArray(data, 0, data.length, options);
            Bitmap rotatedBMP = Bitmap.createBitmap(bmp, 0, 0, w, h, mtx, true);
            return rotatedBMP;
        } else {
            return BitmapFactory.decodeByteArray(data, 0, data.length, options);
        }
    }

    public String getRealPathFromURI(Uri contentUri) {
        String[] proj = {MediaStore.Audio.Media.DATA};
        Cursor cursor = managedQuery(contentUri, proj, null, null, null);
        if (null != cursor) {
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        }
        return null;
    }

    private class CameraSizeComparator implements Comparator<Camera.Size> {
        public int compare(Camera.Size lhs, Camera.Size rhs) {
            if (lhs.width == rhs.width) {
                return 0;
            } else if (lhs.width < rhs.width) {
                return 1;
            } else {
                return -1;
            }
        }

    }
}
