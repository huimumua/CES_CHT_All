package com.askey.mobile.zwave.control.home.activity;

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
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.askey.mobile.zwave.control.R;
import com.askey.mobile.zwave.control.application.ZwaveClientApplication;
import com.askey.mobile.zwave.control.base.BaseActivity;
import com.askey.mobile.zwave.control.data.LocalMqttData;
import com.askey.mobile.zwave.control.deviceContr.localMqtt.MQTTManagement;
import com.askey.mobile.zwave.control.deviceContr.localMqtt.MqttMessageArrived;
import com.askey.mobile.zwave.control.deviceContr.net.SocketTransceiver;
import com.askey.mobile.zwave.control.deviceContr.net.TCPReceive;
import com.askey.mobile.zwave.control.deviceContr.net.TcpClient;
import com.askey.mobile.zwave.control.home.activity.addDevice.AddSmartStartActivity;
import com.askey.mobile.zwave.control.home.activity.addDevice.DeleteDeviceActivity;
import com.askey.mobile.zwave.control.home.activity.addDevice.SelectBrandActivity;
import com.askey.mobile.zwave.control.home.activity.addDevice.VersionActivity;
import com.askey.mobile.zwave.control.home.adapter.HomeAdapter;
import com.askey.mobile.zwave.control.home.fragment.FavoritesFragment;
import com.askey.mobile.zwave.control.home.fragment.RoomsFragment;
import com.askey.mobile.zwave.control.home.fragment.ScenesFragment;
import com.askey.mobile.zwave.control.login.ui.LogInActivity;
import com.askey.mobile.zwave.control.login.ui.LoginPageActivity;
import com.askey.mobile.zwave.control.util.Const;
import com.askey.mobile.zwave.control.util.ImageUtils;
import com.askey.mobile.zwave.control.util.Logg;
import com.askey.mobile.zwave.control.util.PreferencesUtils;
import com.askeycloud.webservice.sdk.model.ServicePreference;
import com.askeycloud.webservice.sdk.service.iot.AskeyIoTService;
import com.askeycloud.webservice.sdk.service.web.AskeyApiAuthService;

import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.JSONException;
import org.json.JSONObject;

import java.security.acl.Group;
import java.util.Timer;
import java.util.TimerTask;

public class HomeActivity extends BaseActivity implements View.OnClickListener, NavigationView.OnNavigationItemSelectedListener, TabLayout.OnTabSelectedListener, PreviewPhotoActivity.ChangeBackgroundCallback {
    public static String LOG_TAG = "HomeActivity";
    private FrameLayout container;
    private NavigationView sliding_menu,smartStartMenu;
    private DrawerLayout drawer_layout;
    private TabLayout bottom_tab;
    private Fragment[] fragments;
    private HomeAdapter myAdapter;
    private ImageView edit, voice, head_cion;
    private Menu menu_1, menu_2, menu_3, menu_4, menu_5, menu_6, menu_7, menu_8, menu_9;
    private MenuItem menu_add, menu_remove, menu_reset, menu_learn_mode, menu_network_check,
            version_msg, menu_add_dak, menu_get_all_dsk, menu_remove_all_dsk;

    private String[] titles = new String[]{"My Devices","Smart Start"};

    private int[] icon = new int[]{ R.drawable.tab_rooms_bg, R.drawable.tab_scenes_bg };
    private int currentIndex;
    public static String shadowTopic = "";
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

//        checkForUpdates();

        //TcpClient.getInstance().rigister(tcpReceive);
        //MQTTManagement.getSingInstance().rigister(mMqttMessageArrived);
    }

    private void initView() {
        container = (FrameLayout) findViewById(R.id.container);
        sliding_menu = (NavigationView) findViewById(R.id.sliding_menu);

        menu_add = sliding_menu.getMenu().findItem(R.id.item_add);
        menu_remove = sliding_menu.getMenu().findItem(R.id.item_remouve);
        menu_reset = sliding_menu.getMenu().findItem(R.id.item_reset);
        menu_learn_mode = sliding_menu.getMenu().findItem(R.id.item_learn_mode);
        menu_network_check = sliding_menu.getMenu().findItem(R.id.item_network_check);
        version_msg = sliding_menu.getMenu().findItem(R.id.item_version_msg);

        menu_add_dak = sliding_menu.getMenu().findItem(R.id.item_add_dsk);
        menu_get_all_dsk = sliding_menu.getMenu().findItem(R.id.item_get_all_dsk);
        menu_remove_all_dsk = sliding_menu.getMenu().findItem(R.id.item_remove_all_dsk);
        menu_add_dak.setVisible(false);
        menu_get_all_dsk.setVisible(false);
        menu_remove_all_dsk.setVisible(false);

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
        fragments = new Fragment[]{roomsFragment, scenesFragment }; //fragment的集合

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
        ((RoomsFragment) fragments[0]).register();
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
        Intent intent;
        switch (item.getItemId()) {
            case R.id.item_add:
//                changeIndexFragment(item, 0);
                startActivity(new Intent(this, SelectBrandActivity.class));
                Const.currentRoomName = "My Home";
                return true;

            case R.id.item_remouve:
                intent = new Intent(this, DeleteDeviceActivity.class);
                intent.putExtra("deviceId", "1");
                intent.putExtra("roomName", " ");
                startActivity(intent);
                Log.i("~~~~~~~~~~~~", "onNavigationItemSelected: ");
                return true;

            case R.id.item_reset:
                Log.i("~~~~~~~~~~~~", "onNavigationItemSelected: 2 ");
                String nodeId = "0";
                //MQTTManagement.getSingInstance().publishMessage(Const.subscriptionTopic+"Zwave"+nodeId, LocalMqttData.getBrigtness(nodeId));
                //TcpClient.getInstance().getTransceiver().send("mobile_zwave:resetDevice:Zwave:" + nodeId);
                //MQTTManagement.getSingInstance().publishMessage(Const.subscriptionTopic+"Zwave"+nodeId,LocalMqttData.setDefault());
                intent = new Intent(this, ResetActivity.class);
                startActivity(intent);
                return true;
            case R.id.item_learn_mode:
                Log.i("~~~~~~~~~~~~", "onNavigationItemSelected: 3 ");
                //MQTTManagement.getSingInstance().publishMessage(Const.subscriptionTopic+"Zwave"+nodeId, LocalMqttData.getBrigtness(nodeId));
                //TcpClient.getInstance().getTransceiver().send("mobile_zwave:resetDevice:Zwave:" + nodeId);
                //MQTTManagement.getSingInstance().publishMessage(Const.subscriptionTopic+"Zwave"+nodeId,LocalMqttData.setDefault());
                intent = new Intent(this, LearnModeActivity.class);
                startActivity(intent);
                return true;
            case R.id.item_network_check:
                Log.i("~~~~~~~~~~~~", "onNavigationItemSelected: 3 ");
                //MQTTManagement.getSingInstance().publishMessage(Const.subscriptionTopic+"Zwave"+nodeId, LocalMqttData.getBrigtness(nodeId));
                //TcpClient.getInstance().getTransceiver().send("mobile_zwave:resetDevice:Zwave:" + nodeId);
                //MQTTManagement.getSingInstance().publishMessage(Const.subscriptionTopic+"Zwave"+nodeId,LocalMqttData.setDefault());
                intent = new Intent(this, NetworkHealthCheckActivity.class);
                startActivity(intent);
                return true;
            case R.id.item_version_msg:
                intent = new Intent(this, VersionActivity.class);
                startActivity(intent);
                return true;
            case R.id.item_add_dsk:
                ScenesFragment.newInstance().responseMenu(Const.ADD_DSK); //调用ScenesFragment里面的方法responseMenu（int）;
                Intent addDskIntent = new Intent(this, AddSmartStartActivity.class);
                startActivity(addDskIntent);
                return true;
            case R.id.item_remove_all_dsk:
                ScenesFragment.newInstance().responseMenu(Const.REMOVE_ALL_DSK);
                return true;
            case R.id.item_get_all_dsk:
                ScenesFragment.newInstance().responseMenu(Const.GET_ALL_DSK);
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
        switch (currentIndex) { //注销监听
            //case 0:
            //    ((FavoritesFragment) fragments[0]).unRegister();
            //    break;
            case 0:
                ((RoomsFragment) fragments[0]).unRegister();
                break;
            case 1:
                ((ScenesFragment) fragments[1]).unRegister();
                break;
        }
        transaction.hide(fragments[currentIndex]);
        if (!fragments[position].isAdded()) {
            transaction.add(R.id.container, fragments[position]);
//            transaction.replace(R.id.container,fragments[position]);
        }
        transaction.show(fragments[position]).commit();
        switch (position) { //启动监听
            //case 0:
            //    ((FavoritesFragment) fragments[0]).register();
            //   break;
            case 0:
                ((RoomsFragment) fragments[0]).register();
                break;
            case 1:
                ((ScenesFragment) fragments[1]).register();
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

    /**
     * fragement切换的监听
     * @param tab
     */
    @Override
    public void onTabSelected(TabLayout.Tab tab) {
        switch (tab.getPosition()) {
            case 0:
                switchFragment(0);

                menu_add.setVisible(true);
                menu_remove.setVisible(true);
                menu_reset.setVisible(true);
                menu_learn_mode.setVisible(true);
                menu_network_check.setVisible(true);
                version_msg.setVisible(true);

                menu_add_dak.setVisible(false);
                menu_get_all_dsk.setVisible(false);
                menu_remove_all_dsk.setVisible(false);
                break;
            case 1:
                switchFragment(1);

                menu_add.setVisible(false);
                menu_remove.setVisible(false);
                menu_reset.setVisible(false);
                menu_learn_mode.setVisible(false);
                menu_network_check.setVisible(false);
                version_msg.setVisible(false);

                menu_add_dak.setVisible(true);
                menu_get_all_dsk.setVisible(true);
                menu_remove_all_dsk.setVisible(true);
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

    @Override
    public void onResume() {
        super.onResume();
        // ... your own onResume implementation
//        checkForCrashes();
    }


    @Override
    public void onPause() {
        super.onPause();
//        unregisterManagers();
    }

    @Override
    protected void onStop() {
        super.onStop();
        Logg.i(LOG_TAG, "===onStop=====");
        unrigister();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        HomeActivity.shadowTopic = "";
//        unregisterManagers();
        Logg.i(LOG_TAG, "===onDestroy=====");
    }


    /**
     * 菜单、返回键响应
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            exitBy2Click(mContext); // 调用双击退出
        }
        return false;
    }

    /**
     * 双击退出应用程序
     */
    private Boolean isExit = false;

    public void exitBy2Click(Context context) {
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
            HomeActivity.shadowTopic = "";
            TcpClient.getInstance().disconnect();
            MQTTManagement.getSingInstance().closeMqtt();
            AskeyIoTService.getInstance(appContext).disconnectIoTMQTTManager();
            ZwaveClientApplication.appExit();
            finish();
        }
    }

    @Override
    public void changeBackground(Bitmap bitmap, String fileSrc) {
        BitmapDrawable drawable = new BitmapDrawable(getResources(), bitmap);
        drawer_layout.setBackground(drawable);
        PreferencesUtils.put(this, BACK_IMG_SRC, fileSrc);
    }

    MqttMessageArrived mMqttMessageArrived = new MqttMessageArrived() {
        @Override
        public void mqttMessageArrived(String topic, MqttMessage message) {
            final String result = new String(message.getPayload());
            Logg.i("~~~~HomeActivity", "=mqttMessageArrived=>=topic=" + topic);
            Logg.i("~~~~HomeActivity", "=mqttMessageArrived=>=message=" + result);

            if (result.contains("desired")) {
                return;
            }
        }
    };


    TCPReceive tcpReceive = new TCPReceive() {
        @Override
        public void onConnect(SocketTransceiver transceiver) {

        }

        @Override
        public void onConnectFailed() {

        }

        @Override
        public void receiveMessage(SocketTransceiver transceiver, String tcpMassage) {
            //处理结果
            Logg.i("~~~~HomeActivity", "=tcpMassage=" + tcpMassage);
            resetResult(tcpMassage);
        }

        @Override
        public void onDisconnect(SocketTransceiver transceiver) {

        }

    };

    private void resetResult(final String result) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    final JSONObject jsonObject = new JSONObject(result);
                    String messageType = jsonObject.optString("MessageType");
                    String status = jsonObject.optString("Status");
                    if ("Success".equals(status)) {
                        Log.i("HomeActivity", "Reset Device Success");
                    } else {
                        Toast.makeText(mContext, status, Toast.LENGTH_SHORT).show();
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                    Logg.i(LOG_TAG, "errorJson------>" + result);
                }
            }
        });
    }

    private void unrigister() {
        if(tcpReceive!=null){
            TcpClient.getInstance().unrigister(tcpReceive);
        }
//        if(mMqttMessageArrived!=null){
//            MQTTManagement.getSingInstance().unrigister(mMqttMessageArrived);
//        }
    }
}
