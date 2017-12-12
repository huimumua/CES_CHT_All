package com.askey.mobile.zwave.control.home.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.amazonaws.mobileconnectors.iot.AWSIotMqttClientStatusCallback;
import com.amazonaws.mobileconnectors.iot.AWSIotMqttQos;
import com.askey.mobile.zwave.control.R;
import com.askey.mobile.zwave.control.base.BaseActivity;
import com.askey.mobile.zwave.control.deviceContr.localMqtt.MQTTManagement;
import com.askey.mobile.zwave.control.deviceContr.net.TcpClient;
import com.askey.mobile.zwave.control.home.activity.addDevice.SelectBrandActivity;
import com.askey.mobile.zwave.control.home.adapter.HomeAdapter;
import com.askey.mobile.zwave.control.home.fragment.FavoritesFragment;
import com.askey.mobile.zwave.control.home.fragment.RoomsFragment;
import com.askey.mobile.zwave.control.home.fragment.ScenesFragment;
import com.askey.mobile.zwave.control.login.ui.LogInActivity;
import com.askey.mobile.zwave.control.util.Const;
import com.askey.mobile.zwave.control.util.ImageUtils;
import com.askey.mobile.zwave.control.util.Logg;
import com.askey.mobile.zwave.control.util.PreferencesUtils;
import com.askeycloud.sdk.device.response.AWSIoTCertResponse;
import com.askeycloud.sdk.device.response.IoTDeviceInfoResponse;
import com.askeycloud.webservice.sdk.iot.AskeyIoTUtils;
import com.askeycloud.webservice.sdk.iot.callback.MqttConnectionCallback;
import com.askeycloud.webservice.sdk.iot.callback.MqttServiceConnectedCallback;
import com.askeycloud.webservice.sdk.service.device.AskeyIoTDeviceService;
import com.askeycloud.webservice.sdk.service.iot.AskeyIoTService;
import com.askeycloud.webservice.sdk.service.web.AskeyApiAuthService;
import com.bumptech.glide.Glide;

import java.io.File;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutionException;

public class HomeActivity extends BaseActivity implements View.OnClickListener, NavigationView.OnNavigationItemSelectedListener, TabLayout.OnTabSelectedListener, PreviewPhotoActivity.ChangeBackgroundCallback{
    public static String LOG_TAG = "HomeActivity";
    private FrameLayout container;
    private NavigationView sliding_menu;
    private DrawerLayout drawer_layout;
    private TabLayout bottom_tab;
    private Fragment[] fragments;
    private HomeAdapter myAdapter;
    private ImageView edit, voice, head_cion;
    private String[] titles = new String[]{"Favorites", "Rooms", "Scenes"};
    private int[] icon = new int[]{
            R.drawable.tab_favorite_bg, R.drawable.tab_rooms_bg, R.drawable.tab_scenes_bg
    };
    private int currentIndex;
    public static String shadowTopic;
    private String userId,cert,pk;
    private String back_img_src;
    private static final String BACK_IMG_SRC = "backgroundImg";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        if (Build.VERSION.SDK_INT >= 21) {
            View decorView = getWindow().getDecorView();
            int option = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE;
            decorView.setSystemUiVisibility(option);
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }
        initView();
        initFragment();
        if(Const.isRemote){
            showWaitingDialog();
            lookupIoTDevice(mContext);
        }

    }


    private void initView() {
        container = (FrameLayout) findViewById(R.id.container);
        sliding_menu = (NavigationView) findViewById(R.id.sliding_menu);
        View headerLayout = sliding_menu.inflateHeaderView(R.layout.navigation_head);
        head_cion = (ImageView) headerLayout.findViewById(R.id.head_icon);

        drawer_layout = (DrawerLayout) findViewById(R.id.drawer_layout);
        BitmapDrawable drawable = ImageUtils.getBackgroundImg();
        if (null != drawable) {
            drawer_layout.setBackground(drawable);
        }
        bottom_tab = (TabLayout) findViewById(R.id.bottom_tab);

        edit = (ImageView) findViewById(R.id.edit);
        voice = (ImageView) findViewById(R.id.voice);

        for (int i = 0; i < titles.length; i++) {
            bottom_tab.addTab(bottom_tab.newTab().setCustomView(getTabView(i)));
        }

        FavoritesFragment favoritesFragment = FavoritesFragment.newInstance();
        RoomsFragment roomsFragment = RoomsFragment.newInstance();
        ScenesFragment scenesFragment = ScenesFragment.newInstance();
        fragments = new Fragment[]{
                favoritesFragment, roomsFragment, scenesFragment
        };
        head_cion.setOnClickListener(this);
        bottom_tab.addOnTabSelectedListener(this);
        sliding_menu.setNavigationItemSelectedListener(this);
        PreviewPhotoActivity.setChangeBackgroundCallback(this);
    }

    private void initFragment() {
        getSupportFragmentManager()
                .beginTransaction()
                .add(R.id.container, fragments[0])
                .show(fragments[0])
                .commit();
        ((FavoritesFragment)fragments[0]).register();
        currentIndex = 0;
    }

    private View getTabView(int position) {
        //为子tab布置一个布局
        View v = LayoutInflater.from(this).inflate(R.layout.tab_view, null);
        TextView tv = (TextView) v.findViewById(R.id.tab_tv);
        tv.setText(titles[position]);
        ImageView iv = (ImageView) v.findViewById(R.id.tab_img);
        iv.setImageResource(icon[position]);
        return v;
    }

    public void toggleDrawerLayout() {
        drawer_layout.openDrawer(Gravity.START);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        drawer_layout.closeDrawer(Gravity.START);
        switch (item.getItemId()) {
            case R.id.item_add:
//                changeIndexFragment(item, 0);
                startActivity(new Intent(this, SelectBrandActivity.class));
                Const.currentRoomName = "My Home";
                return true;
            case R.id.item_account:
                changeIndexFragment(item, 1);
                return true;
            case R.id.item_about:
                changeIndexFragment(item, 4);
                return true;
            case R.id.item_terms:
                changeIndexFragment(item, 5);
                return true;
            case R.id.item_out:
//                changeIndexFragment(item, 6);
                AskeyApiAuthService.getInstance(mContext).revoke();
                Intent intent = new Intent(mContext, LogInActivity.class);
                startActivity(intent);
                return true;
        }
        return false;
    }

    private void changeIndexFragment(MenuItem item, int currentIndex) {
        item.setChecked(true);
//        switchFragment(currentIndex);//切换Fragment
    }

    private void switchFragment(int position) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        switch (currentIndex) {
            case 0:
                ((FavoritesFragment)fragments[0]).unRegister();
                break;
            case 1:
                ((RoomsFragment)fragments[1]).unRegister();
                break;
            case 2:
                ((ScenesFragment)fragments[2]).unRegister();
                break;
        }
        transaction.hide(fragments[currentIndex]);
        if (!fragments[position].isAdded()) {
            transaction.add(R.id.container,fragments[position]);
//            transaction.replace(R.id.container,fragments[position]);
        }
        transaction.show(fragments[position]).commit();
        switch (position) {
            case 0:
                ((FavoritesFragment)fragments[0]).register();
                break;
            case 1:
                ((RoomsFragment)fragments[1]).register();
                break;
            case 2:
                ((ScenesFragment)fragments[2]).register();
                break;
        }
        currentIndex = position;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.head_icon:
                drawer_layout.closeDrawer(Gravity.START);
                break;
        }
    }

    @Override
    public void onTabSelected(TabLayout.Tab tab) {
        switch (tab.getPosition()) {
            case 0:
                switchFragment(0);
                break;
            case 1:
                switchFragment(1);
                break;
            case 2:
                switchFragment(2);
                break;
        }
    }

    @Override
    public void onTabUnselected(TabLayout.Tab tab) {

    }

    @Override
    public void onTabReselected(TabLayout.Tab tab) {

    }

    /**
     * 检查设备是否存在
     * */
    public  void lookupIoTDevice(final Context mcontext) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                String deviceId = (String) PreferencesUtils.get(mContext, Const.TOPIC_TAG, "");
                Logg.i(LOG_TAG, "=====lookupIoTDevice===deviceId====="+deviceId);
                IoTDeviceInfoResponse ioTDeviceInfoResponse = AskeyIoTDeviceService.getInstance(mcontext).lookupIoTDevice(Const.DEVICE_MODEL, deviceId );
                if(ioTDeviceInfoResponse!=null && !"".equals(ioTDeviceInfoResponse)){
                    int code = ioTDeviceInfoResponse.getCode();
                    Logg.i(LOG_TAG, "===lookupIoTDevice===DEVICE_MODEL====" + Const.DEVICE_MODEL );
                    Logg.i(LOG_TAG, "===lookupIoTDevice===deviceId====" + Const.subscriptionTopic );
                    Logg.i(LOG_TAG, "===lookupIoTDevice===getCode====" + ioTDeviceInfoResponse.getCode() );
                    Logg.i(LOG_TAG, "===lookupIoTDevice===getMessage====" + ioTDeviceInfoResponse.getMessage() );
                    Logg.i(LOG_TAG, "===lookupIoTDevice===getAddtionMessage====" + ioTDeviceInfoResponse.getAddtionMessage() );

                    if(400005 == code || 403 == code){
                        //设备不存在  进行创建
                        try {
                            Thread.sleep(3000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        lookupIoTDevice(mcontext);
                    }else if(400006 == code ){

                    }else if(200 == code){
                        //存在成功
                        getCertificateKey(ioTDeviceInfoResponse);
                    }

                }

            }
        }).start();

    }



    public  void getCertificateKey(final IoTDeviceInfoResponse ioTDeviceInfoResponse) {
        AWSIoTCertResponse response = AskeyIoTDeviceService.getInstance(appContext).getIotCert();
        Logg.i(LOG_TAG, "==getCertificateKey====response=" + response);
        AskeyIoTService.getInstance(appContext).changeMQTTQos(AWSIotMqttQos.QOS1);
        final MqttServiceConnectedCallback mqttServiceConnectedCallback = new MqttServiceConnectedCallback() {
            @Override
            public void onMqttServiceConnectedSuccess() {
                Logg.i(LOG_TAG, "===MqttServiceConnectedCallback===onMqttServiceConnectedSuccess===");
                if(!"".equals(userId) && !"".equals(cert) && !"".equals(pk)){
                    AskeyIoTService.getInstance(appContext).connectToAWSIot(userId, cert, pk,
                            new MqttConnectionCallback() {
                                @Override
                                public void onConnected() {
                                    Logg.i(LOG_TAG, "====MqttConnectionCallback==onConnected===");
                                    if (ioTDeviceInfoResponse != null) {
                                        stopWaitDialog();
                                        HomeActivity.shadowTopic = ioTDeviceInfoResponse.getShadowTopic();
                                        AskeyIoTService.getInstance(getApplicationContext()).subscribeMqtt(ioTDeviceInfoResponse.getShadowTopic());
                                    }
                                }

                                @Override
                                public void unConnected(AWSIotMqttClientStatusCallback.AWSIotMqttClientStatus awsIotMqttClientStatus) {
                                    Logg.i(LOG_TAG, "===MqttConnectionCallback===unConnected ===");

                                }
                            });
                }

            }

            @Override
            public void onMqttServiceConnectedError() {
                Logg.i(LOG_TAG, "======onMqttServiceConnectedError===");
            }
        };


        if(response != null){
            response = AskeyIoTDeviceService.getInstance(appContext).getIotCert();
            cert = response.getCertificatePem();
            pk = response.getPrivateKey();
            userId = response.getUserid();
            Logg.i(LOG_TAG, "==getCertificateKey===getCertificatePem==" + cert);
            Logg.i(LOG_TAG, "==getCertificateKey====getPrivateKey==" + pk);
            Logg.i(LOG_TAG, "==getCertificateKey====getUserid==" + userId);
        }

        new Thread(new Runnable() {
            @Override
            public void run() {
                if (ioTDeviceInfoResponse != null) {
                    Logg.i(LOG_TAG, "=ioTDeviceInfoResponse.getRestEndpoint()==" + ioTDeviceInfoResponse.getRestEndpoint());
                    if (ioTDeviceInfoResponse.getRestEndpoint() != null) {
                        AskeyIoTService.getInstance(appContext).configAWSIot(
                                AskeyIoTUtils.translatMqttUseEndpoint(ioTDeviceInfoResponse.getRestEndpoint()),
                                mqttServiceConnectedCallback
                        );
                    }
                }
            }
        }).start();


    }

    @Override
    protected void onStop() {
        super.onStop();
        Logg.i(LOG_TAG,"===onStop=====");

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Logg.i(LOG_TAG,"===onDestroy=====");
    }


    /**
     * 菜单、返回键响应
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            exitBy2Click(mContext); // 调用双击退出函数
        }
        return false;
    }

    /**
     * 双击退出函数
     */
    private  Boolean isExit = false;
    public  void exitBy2Click(Context context) {
        if (context == null)
            return;
        Timer tExit = null;
        if (isExit == false) {
            isExit = true; // 准备退出
            Toast.makeText(context,
                    context.getResources().getString(R.string.two_click_exit),
                    Toast.LENGTH_SHORT).show();
            tExit = new Timer();
            tExit.schedule(new TimerTask() {
                @Override
                public void run() {
                    isExit = false; // 取消退出
                }
            }, 2000); // 如果2秒钟内没有按下返回键，则启动定时器取消掉刚才执行的任务
        } else {
            TcpClient.getInstance().disconnect();
            MQTTManagement.getSingInstance().closeMqtt();
            ((Activity) context).finish();
            System.exit(0);
        }
    }

    /*
        加载背景图
     */
    public void loadBackground(){
        back_img_src = (String) PreferencesUtils.get(this, BACK_IMG_SRC, "");
        if (!back_img_src.equals("")) {
            final File file = new File(back_img_src);
            if (file.exists()) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Bitmap myBitmap = null;
                        try {
                            myBitmap = Glide.with(HomeActivity.this)
                                    .load(file)
                                    .asBitmap() //必须
                                    .centerCrop()
                                    .into(1080, 1920)
                                    .get();
                            Log.d(LOG_TAG,"=====1");
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                            Log.d(LOG_TAG,"=====2");
                        } catch (ExecutionException e) {
                            e.printStackTrace();
                            Log.d(LOG_TAG,"=====3");
                        }
                        final Bitmap finalMyBitmap = myBitmap;
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Log.d(LOG_TAG,"=====4");
                                BitmapDrawable drawable = new BitmapDrawable(getResources(),finalMyBitmap);
                                drawer_layout.setBackground(drawable);
                            }
                        });
                    }
                }).start();
            }
        }
    }

    @Override
    public void changeBackground(Bitmap bitmap, String fileSrc) {
        BitmapDrawable drawable = new BitmapDrawable(getResources(),bitmap);
        drawer_layout.setBackground(drawable);
        PreferencesUtils.put(this,BACK_IMG_SRC,fileSrc);
    }
}
