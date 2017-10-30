package com.askey.firefly.zwave.control.ui;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.askey.firefly.zwave.control.R;


/**
 * Created by chiapin on 2017/10/27.
 */

public class RoomMemberFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        //
        View view = inflater.inflate(R.layout.gridview, container, false);

        return view;
    }


}
