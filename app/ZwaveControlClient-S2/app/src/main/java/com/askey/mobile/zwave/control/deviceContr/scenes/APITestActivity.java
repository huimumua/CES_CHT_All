package com.askey.mobile.zwave.control.deviceContr.scenes;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.askey.mobile.zwave.control.R;
import com.askey.mobile.zwave.control.base.BaseActivity;
import com.askey.mobile.zwave.control.data.APIListData;
import com.askey.mobile.zwave.control.deviceContr.adapter.ApiArrayAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * 项目名称：ZwaveControlClient-S2
 * 类描述：
 * 创建人：skysoft  charles.bai
 * 创建时间：2018/3/21 13:37
 * 修改人：skysoft
 * 修改时间：2018/3/21 13:37
 * 修改备注：
 */
public class APITestActivity extends BaseActivity {
    private final String TAG = DeviceTestActivity.class.getSimpleName();
    private Spinner mySpinner;
    private String title;
    private LinearLayout parameterLayout;
    private ArrayList<String> data_list = new ArrayList<String>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_api_test);

        title = getIntent().getStringExtra("title");
        TextView titleView = (TextView) this.findViewById(R.id.api_test_title);
        parameterLayout =  (LinearLayout)this.findViewById(R.id.parameter_layout);
        mySpinner = (Spinner)this.findViewById(R.id.spinner2);
        titleView.setText(title);

        initData();
    }

    private void initData() {
        if(title.equals("Command Queue相关接口")){
            data_list = APIListData.getCommandQueueAPIList();
        }else if(title.equals("Network Health Check功能")){
            data_list = APIListData.getNetworkHealthCheckAPIList();
        }else if(title.equals("Smart Start相关API")){
            data_list = APIListData.getSmartStartAPIList();
        }else if(title.equals("Controller相关接口")){
            data_list = APIListData.getControllerAPIList();
        }else if(title.equals("Command Class Battery")){
            data_list = APIListData.getBatteryAPIList();
        }else if(title.equals("Command Class Basic ver 1~2")){
            data_list = APIListData.getBasicVerAPIList();
        }else if(title.equals("Command Class Switch Multi-Level")){
            data_list = APIListData.getSwitchMultiLevelAPIList();
        }else if(title.equals("Command Class Configuration")){
            data_list = APIListData.getConfigurationAPIList();
        }else if(title.equals("Command Class Power Level")){
            data_list = APIListData.getPowerLevelAPIList();
        }else if(title.equals("Command Class Switch All")){
            data_list = APIListData.getSwitchAllAPIList();
        }else if(title.equals("Command Class Switch Binary ver 1~2")){
            data_list = APIListData.getSwitchAllAPIList();
        }else if(title.equals("Command Class Sensor Binary v2")){
            data_list = APIListData.getSensorBinaryV2APIList();
        }else if(title.equals("Command Class Meter v3")){
            data_list = APIListData.getMeterV3APIList();
        }else if(title.equals("Command Class Wake Up")){
            data_list = APIListData.getWakeUpAPIList();
        }else if(title.equals("Command Class Door Lock")){
            data_list = APIListData.getDoorLockAPIList();
        }else if(title.equals("Command Class User Code")){
            data_list = APIListData.getUserCodeAPIList();
        }else if(title.equals("Command Class Protection v1-v3")){
            data_list = APIListData.getProtectionAPIList();
        }else if(title.equals("Command Class Indicator v1")){
            data_list = APIListData.getIndicatorAPIList();
        }else if(title.equals("Command Class Door Lock Looging")){
            data_list = APIListData.getDoorLockLoogingAPIList();
        }else if(title.equals("Command Class Language")){
            data_list = APIListData.getLanguageAPIList();
        }else if(title.equals("Command Class Switch Color")){
            data_list = APIListData.getSwitchColorAPIList();
        }else if(title.equals("Command Class Barrier Operator")){
            data_list = APIListData.getBarrierOperatorAPIList();
        }else if(title.equals("Command Class Basic Tariff Info")){
            data_list = APIListData.getBasicTariffInfoAPIList();
        }else if(title.equals("Command Class Association & Multi-Channel Association")){
            data_list = APIListData.getAssociationAPIList();
        }else if(title.equals("Command Class Notification version 4")){
            data_list = APIListData.getNotificationAPIList();
        }else if(title.equals("Command Class Central Scene version 2")){
            data_list = APIListData.getCentralSceneVersionAPIList();
        }else if(title.equals("Command Class Scene Actuator Conf ver 1")){
            data_list = APIListData.getSceneActuatorConfAPIList();
        }else if(title.equals("Command Class Firmware Update Md")){
            data_list = APIListData.getFirmwareUpdateMdAPIList();
        }else if(title.equals("Command Class Multi Cmd")){
            data_list = APIListData.getMultiCmdAPIList();
        }

        //适配器
        ApiArrayAdapter arrAdapter = new ApiArrayAdapter(this, data_list);
        //加载适配器
        mySpinner.setAdapter(arrAdapter);
        mySpinner.setSelection(data_list.size() - 1, true);
    }

}
