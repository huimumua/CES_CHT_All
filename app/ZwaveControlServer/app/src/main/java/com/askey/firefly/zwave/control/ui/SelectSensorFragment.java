package com.askey.firefly.zwave.control.ui;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import com.askey.firefly.zwave.control.R;
import com.askey.firefly.zwave.control.dao.ZwaveDevice;
import com.askey.firefly.zwave.control.dao.ZwaveDeviceManager;
import com.askey.firefly.zwave.control.dao.ZwaveDeviceScene;
import com.askey.firefly.zwave.control.dao.ZwaveDeviceSceneManager;
import com.askey.firefly.zwave.control.page.zwNodeMember;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by chiapin on 2017/10/27.
 */

public class SelectSensorFragment extends Fragment {

    private static String LOG_TAG = SelectSensorFragment.class.getSimpleName();
    private View view;
    private String roomName;
    private Spinner spinner;
    private Button okButton;

    private String sensorNodeInfo;
    private ZwaveDeviceManager zwaveDeviceManager;
    private ZwaveDeviceSceneManager zwSceneManager;

    private FragmentManager manager;
    private FragmentTransaction transaction;

    private static List<zwNodeMember> sensorList = new ArrayList<>();
    private static ArrayList<String> spContacts = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        zwaveDeviceManager = ZwaveDeviceManager.getInstance(getActivity());
        zwSceneManager = ZwaveDeviceSceneManager.getInstance(getActivity());

        view = inflater.inflate(R.layout.select_sensor_fragment, container, false);

        manager = getFragmentManager();

        TextView text = (TextView)view.findViewById(R.id.txSensor);
        spinner = (Spinner) view.findViewById(R.id.spSensor);
        okButton = (Button) view.findViewById(R.id.okButton);

        roomName = (String)getArguments().get("SceneName");

        getSensorList();

        if (spContacts.size() != 0) {
            text.setText("Select your condition :");

            ArrayAdapter<String> spAdapter = new ArrayAdapter<>(getActivity(),
                    android.R.layout.simple_spinner_item, spContacts);
            spAdapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);

            spinner.setAdapter(spAdapter);

            okButton.setOnClickListener(new Button.OnClickListener(){

                @Override

                public void onClick(View v) {

                    // TODO Auto-generated method stub
                    String tmpCondition =  spinner.getSelectedItem().toString();

                    int sensorNodeId=0;

                    for (int idx = 0; idx < sensorList.size(); idx++) {
                        if (sensorList.get(idx).getName().equals(tmpCondition)) {
                            sensorNodeId = sensorList.get(idx).getNodeId();
                            sensorNodeInfo = sensorList.get(idx).getNodeInfo();
                            break;
                        }
                    }
                    // update scence in db
                    ZwaveDeviceScene tmpScene = zwSceneManager.getScene(roomName);
                    tmpScene.setCondition(tmpCondition);
                    tmpScene.setSensorNodeId(sensorNodeId);
                    tmpScene.update();

                    // change fragment to sensor fragment

                    transaction = manager.beginTransaction();

                    SensorFragment mSensorFragment = new SensorFragment();

                    Bundle bundle = new Bundle();
                    bundle.putString("SceneName",roomName);
                    mSensorFragment.setArguments(bundle);

                    transaction.replace(R.id.sensorLin, mSensorFragment, "SensorFragment");
                    //transaction.addToBackStack(null);
                    transaction.commit();

                }
            });
        }
        else {
            text.setText("no SENSOR in this room");
            spinner.setVisibility(View.GONE);
        }

        return view;
    }

    private List<zwNodeMember> getSensorList(){

        sensorList.clear();
        spContacts.clear();

        List<ZwaveDevice> tmpList = zwaveDeviceManager.getSceneDevicesList(roomName);

        for (int idx = 0; idx < tmpList.size(); idx++) {

            if (tmpList.get(idx).getDevType().equals("SENSOR")){

                if (tmpList.get(idx).getNodeInfo().contains("COMMAND_CLASS_NOTIFICATION")) {
                    try {
                        JSONObject jsonObject = new JSONObject(tmpList.get(idx).getNodeInfo());
                        if (jsonObject.getString("Product id").equals("001F")) {
                            //Water
                            sensorList.add(new zwNodeMember(tmpList.get(idx).getNodeId(),tmpList.get(idx).getHomeId(),
                                    tmpList.get(idx).getDevType(), "WATER","",false,""));
                            spContacts.add("WATER");
                        } else if (jsonObject.getString("Product id").equals("000C")) {
                            //Motion
                            /*
                            sensorList.add(new zwNodeMember(tmpList.get(idx).getNodeId(),tmpList.get(idx).getHomeId(),
                                    tmpList.get(idx).getDevType(), "MOTION",""));
                            spContacts.add("MOTION");
                            */
                            //Door/Window
                            sensorList.add(new zwNodeMember(tmpList.get(idx).getNodeId(),tmpList.get(idx).getHomeId(),
                                    tmpList.get(idx).getDevType(), "DOOR","",false,""));
                            spContacts.add("DOOR");
                            //Luminance
                            sensorList.add(new zwNodeMember(tmpList.get(idx).getNodeId(),tmpList.get(idx).getHomeId(),
                                    tmpList.get(idx).getDevType(), "LUMINANCE","",false,""));
                            spContacts.add("LUMINANCE");
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

            }
        }
        return sensorList;
    }
}