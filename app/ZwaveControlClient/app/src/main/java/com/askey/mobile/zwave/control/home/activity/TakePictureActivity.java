package com.askey.mobile.zwave.control.home.activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
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
    private static final int PICK_IMAGE = 1;

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
            if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                rootDir = Environment.getExternalStorageDirectory().toString() + "/Zwave";
                File dir = new File(rootDir);
                if (!dir.exists()) {
                    dir.mkdir();
                }
            }
        }

        initView();
    }

    @Override
    protected void onResume() {
        super.onResume();
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
                if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                    rootDir = Environment.getExternalStorageDirectory().toString() + "/Zwave";
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
                    String path = getRealPathFromURI(data.getData());
                    Log.d(TAG,path);
                    Intent intent = new Intent(TakePictureActivity.this, PreviewPhotoActivity.class);
                    intent.putExtra("path",path);
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
                finish();
                break;
            case R.id.choose_photo:
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setData(MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent,PICK_IMAGE);
                break;
            case R.id.take_pic:
                Toast.makeText(TakePictureActivity.this, "takePic", Toast.LENGTH_SHORT).show();
                if (null != mCamera) {
                    mCamera.takePicture(null, null, new Camera.PictureCallback() {
                        @Override
                        public void onPictureTaken(final byte[] data, Camera camera) {
                            Runnable runnable = new Runnable() {
                                @Override
                                public void run() {
                                    Bitmap bitmap = null;
                                    BufferedOutputStream bos = null;
                                    String photoPath = rootDir + File.separator + System.currentTimeMillis() + ".jpeg";
                                    try {
                                        bos = new BufferedOutputStream(new FileOutputStream(photoPath));
                                        bitmap = adjustPhotoRotationToPortrait(data);//照片的方向不对，此处旋转90度
                                        if (bitmap.compress(Bitmap.CompressFormat.JPEG, 80, bos)) {
                                            Intent intent = new Intent(TakePictureActivity.this, PreviewPhotoActivity.class);
                                            intent.putExtra("path",photoPath);
                                            startActivity(intent);
                                            finish();
                                        }
                                    } catch (FileNotFoundException e) {
                                        e.printStackTrace();
                                    } finally {
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
            DisplayMetrics metrics = getResources().getDisplayMetrics();
            setCameraParams(camera, metrics.widthPixels, metrics.heightPixels);
            camera.startPreview();
            isOpenCamera = true;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void freeCameraResource() {
        Log.d(TAG, "freecamera");
        if (mCamera != null) {
            mCamera.setPreviewCallback(null);
            mCamera.stopPreview();
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
//        for (Camera.Size size : pictureSizeList) {
//            Log.i(TAG, "pictureSizeList size.width=" + size.width + "  size.height=" + size.height);
//        }
        /**从列表中选取合适的分辨率*/
        Camera.Size picSize = getProperSize(pictureSizeList, ((float) height / width));
        if (null == picSize) {
            Log.i(TAG, "null == picSize");
            picSize = parameters.getPictureSize();
        }
        Log.i(TAG, "picSize.width=" + picSize.width + "  picSize.height=" + picSize.height);
        // 根据选出的PictureSize重新设置SurfaceView大小
        float w = picSize.width;
        float h = picSize.height;
        preview.setLayoutParams(new RelativeLayout.LayoutParams((int) (height * (h / w)), height));

        // 获取摄像头支持的PreviewSize列表
        List<Camera.Size> previewSizeList = parameters.getSupportedPreviewSizes();
        Camera.Size preSize = getProperSize(previewSizeList, ((float) height) / width);
        if (null != preSize) {
            Log.i(TAG, "preSize.width=" + preSize.width + "  preSize.height=" + preSize.height);
            parameters.setPreviewSize(preSize.width, preSize.height);
            parameters.setPictureSize(preSize.width, preSize.height);
        }

        parameters.setJpegQuality(100); // 设置照片质量
        if (parameters.getSupportedFocusModes().contains(android.hardware.Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
            parameters.setFocusMode(android.hardware.Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);// 连续对焦模式
        }
        parameters.setRotation(90);//设置拍照的方向，默认是水平的；但是这里设置此项并没有效果，只能通过矩阵变换的方式来实现
        parameters.set("orientation", "portrait");
        camera.cancelAutoFocus();//自动对焦
        camera.setDisplayOrientation(90);// 设置PreviewDisplay的方向，效果就是将捕获的画面旋转多少度显示
        camera.setParameters(parameters);
    }

    private Camera.Size getProperSize(List<Camera.Size> pictureSizeList, float screenRatio) {
        Log.i(TAG, "screenRatio=" + screenRatio);
        Camera.Size result = null;
        for (Camera.Size size : pictureSizeList) {
            float currentRatio = ((float) size.width) / size.height;
            if (currentRatio - screenRatio == 0) {
                result = size;
                break;
            }
        }

        if (null == result) {
            for (Camera.Size size : pictureSizeList) {
                float curRatio = ((float) size.width) / size.height;
                if (curRatio == 4f / 3) {// 默认w:h = 4:3
                    result = size;
                    break;
                }
            }
        }

        return result;
    }

    private Bitmap adjustPhotoRotationToPortrait(byte[] data) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeByteArray(data, 0, data.length, options);
        if (options.outHeight < options.outWidth) {
            int w = options.outWidth;
            int h = options.outHeight;
            Matrix mtx = new Matrix();
            mtx.postRotate(90);
            // Rotating Bitmap
            Bitmap bmp = BitmapFactory.decodeByteArray(data, 0, data.length);
            Bitmap rotatedBMP = Bitmap.createBitmap(bmp, 0, 0, w, h, mtx, true);
            return rotatedBMP;
        } else {
            return null;
        }
    }

    public String getRealPathFromURI(Uri contentUri) {
        String[] proj = {MediaStore.Audio.Media.DATA};
        Cursor cursor = managedQuery(contentUri, proj, null, null, null);
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA);
        cursor.moveToFirst();
        return cursor.getString(column_index);
    }
}
