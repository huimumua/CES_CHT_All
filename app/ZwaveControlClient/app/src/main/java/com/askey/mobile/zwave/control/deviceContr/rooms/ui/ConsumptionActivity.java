package com.askey.mobile.zwave.control.deviceContr.rooms.ui;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.askey.mobile.zwave.control.R;
import com.askey.mobile.zwave.control.application.ZwaveClientApplication;
import com.askey.mobile.zwave.control.data.CloudIotData;
import com.askey.mobile.zwave.control.data.LocalMqttData;
import com.askey.mobile.zwave.control.deviceContr.localMqtt.IotMqttManagement;
import com.askey.mobile.zwave.control.deviceContr.localMqtt.IotMqttMessageCallback;
import com.askey.mobile.zwave.control.deviceContr.localMqtt.MQTTManagement;
import com.askey.mobile.zwave.control.deviceContr.localMqtt.MqttMessageArrived;
import com.askey.mobile.zwave.control.home.activity.HomeActivity;
import com.askey.mobile.zwave.control.util.Const;
import com.askey.mobile.zwave.control.util.Logg;
import com.askeycloud.webservice.sdk.iot.MqttService;
import com.askeycloud.webservice.sdk.iot.callback.ShadowReceiveListener;
import com.askeycloud.webservice.sdk.iot.message.builder.MqttDesiredJStrBuilder;
import com.askeycloud.webservice.sdk.service.iot.AskeyIoTService;

import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import lecho.lib.hellocharts.gesture.ContainerScrollType;
import lecho.lib.hellocharts.model.Axis;
import lecho.lib.hellocharts.model.AxisValue;
import lecho.lib.hellocharts.model.ChartData;
import lecho.lib.hellocharts.model.Line;
import lecho.lib.hellocharts.model.LineChartData;
import lecho.lib.hellocharts.model.PointValue;
import lecho.lib.hellocharts.model.ValueShape;
import lecho.lib.hellocharts.model.Viewport;
import lecho.lib.hellocharts.view.LineChartView;

public class ConsumptionActivity extends AppCompatActivity implements View.OnClickListener{

    private static final String LOG_TAG = ConsumptionActivity.class.getSimpleName();
    private LineChartView mChartView;
    private ToggleButton mAuto,mStandbyKiller;
    private TextView mCurrentConsumption,mTodayPay,mTodayConsumption,mMonthPay,mMonthConsumption,mAlwaysPay,mAlwaysConsumption;
    private LineChartData lineChartData;
    private LineChartView lineChartView;
    private List<Line> linesList;
    private List<PointValue> pointValueList;
    private Axis axisY, axisX;
    private ArrayList<AxisValue> axisValuesX,axisValuesY;
    private ArrayList<Map<String,String>> temp;
    private String nodeId;
    private Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_consumption);
        mChartView = (LineChartView) findViewById(R.id.chart);

        initDatas();
        initView();
        initLine();
        mContext = this;

        nodeId = getIntent().getStringExtra("nodeId");

//        int ZwController_GetMeter(int deviceId, int meter_unit); meter_unit单位
        if(Const.isRemote){
            initIotMqttMessage();
            if(HomeActivity.shadowTopic!=null && !HomeActivity.shadowTopic.equals("")){
//                MqttService mqttService = MqttService.getInstance();
//                mqttService.publishMqttMessage(HomeActivity.shadowTopic, CloudIotData.getPower(nodeId));
                MqttDesiredJStrBuilder builder = new MqttDesiredJStrBuilder(Const.subscriptionTopic+"Zwave"+nodeId);
                builder.setJsonString(CloudIotData.getPower(nodeId));
                AskeyIoTService.getInstance(ZwaveClientApplication.getInstance()).publishDesiredMessage(HomeActivity.shadowTopic, builder);
            }
        }else{
            MQTTManagement.getSingInstance().rigister(mMqttMessageArrived);
            MQTTManagement.getSingInstance().publishMessage(Const.subscriptionTopic+"Zwave"+nodeId, LocalMqttData.getPower(nodeId));
        }
    }

    private void initIotMqttMessage() {

/*        IotMqttManagement.getInstance().setIotMqttMessageCallback(new IotMqttMessageCallback() {
            @Override
            public void receiveMqttMessage(String s, String s1, String s2) {
                //处理结果
                Logg.i(LOG_TAG, "==IotMqttMessageCallback====setIotMqttMessageCallback==s=" + s);
                Logg.i(LOG_TAG, "==IotMqttMessageCallback====setIotMqttMessageCallback==s1=" + s1);
                Logg.i(LOG_TAG, "==IotMqttMessageCallback====setIotMqttMessageCallback==s2=" + s2);
                ///\\\\\\
                if(s2.contains("desired")){
                    return;
                }
                mqttMessageResult(s2);//要验s2格式

            }

        });*/

       //以下这句为注册监听
        AskeyIoTService.getInstance(this).setShadowReceiverListener(new ShadowReceiveListener() {
            @Override
            public void receiveShadowDocument(String s, String s1, String s2) {
                Logg.i(LOG_TAG, "==IotMqttMessageCallback====setShadowReceiverListener==s=" + s);
                Logg.i(LOG_TAG, "==IotMqttMessageCallback====setShadowReceiverListener==s1=" + s1);
                Logg.i(LOG_TAG, "==IotMqttMessageCallback====setShadowReceiverListener==s2=" + s2);
                IotMqttManagement.getInstance().receiveMqttMessage(s,s1,s2);
                if(s2.contains("desired")){
                    return;
                }
                mqttMessageResult(s2);//要验s2格式
            }
        });
    }

    MqttMessageArrived mMqttMessageArrived = new MqttMessageArrived() {
        @Override
        public void mqttMessageArrived(String topic, MqttMessage message) {
            final String result = new String(message.getPayload());
            Logg.i(LOG_TAG,"=mqttMessageArrived=>=topic="+topic);
            Logg.i(LOG_TAG,"=mqttMessageArrived=>=message="+result);

            if(result.contains("desired")){
                return;
            }

        }
    };

    //mqtt调用返回结果
    private void mqttMessageResult(String result) {
        try {
            final JSONObject jsonObject = new JSONObject(result);

            ((Activity) mContext).runOnUiThread(new Runnable() {
                @Override
                public void run() {

                    }
            });
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    private void initDatas() {
        //test
        temp = new ArrayList<>();
        Map<String,String> one = new HashMap();

        one.put("time", "1508997209622");
        one.put("consum", "50");
        temp.add(one);
        one = new HashMap();
        one.put("time", "1508997210000");
        one.put("consum", "55");
        temp.add(one);
        one = new HashMap();
        one.put("time", "1508997211200");
        one.put("consum", "58");
        temp.add(one);
        one = new HashMap();
        one.put("time", "2408997212000");
        one.put("consum", "49");
        temp.add(one);
        one = new HashMap();
        one.put("time", "2508997220000");
        one.put("consum", "40");
        temp.add(one);
    }

    private void initLine() {

        for (int i = 0;i < temp.size();i++) {
            pointValueList.add(new PointValue(Float.parseFloat(temp.get(i).get("time")), Float.parseFloat(temp.get(i).get("consum"))));
        }


        //根据新的点的集合画出新的线
        Line line = new Line(pointValueList);
        line.setColor(Color.RED);
        line.setShape(ValueShape.CIRCLE);
        line.setCubic(false);//曲线是否平滑，即是曲线还是折线
        line.setHasLabelsOnlyForSelected(true);// 隐藏数据，触摸可以显示
        linesList.add(line);

        lineChartData = new LineChartData(linesList);
        lineChartData.setAxisYLeft(axisY);
        lineChartData.setAxisXTop(axisX);
        lineChartView.setLineChartData(lineChartData);
    }


    private void initView() {
        mCurrentConsumption = (TextView) findViewById(R.id.tv_current_consumption);
        mTodayPay = (TextView) findViewById(R.id.tv_today_pay);
        mTodayConsumption = (TextView) findViewById(R.id.tv_today_consumption);
        mMonthPay = (TextView) findViewById(R.id.tv_month_pay);
        mMonthConsumption = (TextView) findViewById(R.id.tv_month_consumption);
        mAlwaysPay = (TextView) findViewById(R.id.tv_always_pay);
        mAlwaysConsumption = (TextView) findViewById(R.id.tv_always_consumption);
        mAuto = (ToggleButton) findViewById(R.id.togBtn_auto);
        mStandbyKiller = (ToggleButton) findViewById(R.id.togBtn_kill);
        mAuto.setOnClickListener(this);
        mStandbyKiller.setOnClickListener(this);

        lineChartView = (LineChartView) findViewById(R.id.chart);
        pointValueList = new ArrayList<>();
        linesList = new ArrayList<>();

        //初始化坐标轴
        axisY = new Axis();
        axisY.setLineColor(Color.parseColor("#aab2bd"));
        axisY.setTextColor(Color.parseColor("#aab2bd"));
        axisY.setMaxLabelChars(5);  //最多几个X轴坐标


        axisX = new Axis();
        axisX.setLineColor(Color.parseColor("#aab2bd"));
        axisX.setTypeface(Typeface.DEFAULT_BOLD);// 设置文字样式，此处为默认

//        axisX.setMaxLabelChars(4); //最多几个X轴坐标，意思就是你的缩放让X轴上数据的个数7<=x<=mAxisXValues.length
        lineChartView.setInteractive(true);
        lineChartView.setZoomEnabled(false);
        lineChartView.setScrollEnabled(false);
        lineChartView.setValueSelectionEnabled(true);

        initAxis();
    }

    private void initAxis() {
        axisValuesY = new ArrayList<>();
        axisValuesX = new ArrayList<>();//定义X轴刻度值的数据集合 value需要获得
        for(int i = 0; i < temp.size(); i++){
            axisValuesY.add(new AxisValue(Float.parseFloat(temp.get(i).get("consum"))).setLabel(temp.get(i).get("consum") +"w"));
            axisValuesX.add(new AxisValue(Float.parseFloat(temp.get(i).get("time"))).setLabel(ms2HMS(Long.parseLong(temp.get(i).get("time")))));
        }
        axisY.setValues(axisValuesY);
        axisX.setValues(axisValuesX);
    }


    /**
     * 当前显示区域
     *
     * @param left
     * @param right
     * @return
     */
    private Viewport initViewPort(float left, float right) {
        Viewport port = new Viewport();
        port.top = 150;
        port.bottom = 0;
        port.left = left;
        port.right = right;
        return port;
    }

    /**
     * 最大显示区域
     *
     * @param right
     * @return
     */
    private Viewport initMaxViewPort(float right) {
        Viewport port = new Viewport();
        port.top = 150;
        port.bottom = 0;
        port.left = 0;
        port.right = right + 50;
        return port;
    }

//    将毫秒转换为小时：分钟：秒格式
    public static String ms2HMS(long _ms){
        SimpleDateFormat sdformat = new SimpleDateFormat("HH:mm");//24小时制
       String time = sdformat.format(_ms);

        return time;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.togBtn_auto:
                if (mAuto.isChecked()) {
                } else {

                }
                break;
            case R.id.tv_killer:
                if (mStandbyKiller.isChecked()) {
                } else {

                }
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unrigister();
    }

    private void unrigister() {
        if(mMqttMessageArrived!=null){
            MQTTManagement.getSingInstance().unrigister(mMqttMessageArrived);
        }
    }
}