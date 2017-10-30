package com.askey.firefly.zwave.control.ui;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.askey.firefly.zwave.control.R;

/**
 * Created by chiapin on 2017/10/27.
 */

public class SelectSensorFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        return inflater.inflate(R.layout.select_sensor_fragment, container, false );
    }

}
