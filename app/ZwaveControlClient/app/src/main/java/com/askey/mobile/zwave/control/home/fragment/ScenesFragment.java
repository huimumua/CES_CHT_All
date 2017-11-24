package com.askey.mobile.zwave.control.home.fragment;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import com.askey.mobile.zwave.control.R;
import com.askey.mobile.zwave.control.deviceContr.model.ScenesInfo;
import com.askey.mobile.zwave.control.deviceContr.scenes.NewScenceActivity;
import com.askey.mobile.zwave.control.home.activity.HomeActivity;
import com.askey.mobile.zwave.control.home.adapter.ScenesAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class ScenesFragment extends Fragment implements View.OnClickListener, ScenesAdapter.OnItemClickListener{
    private ImageView menu, voice, edit;
    private List<ScenesInfo> dataList;
    private RecyclerView scene_recycler;
    private ScenesAdapter adapter;

    public static ScenesFragment newInstance() {
        return new ScenesFragment();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_scenes, container, false);
        initView(view);
        initData();
        return view;
    }

    private void initView(View view) {
        menu = (ImageView) view.findViewById(R.id.menu_btn);
        menu.setOnClickListener(this);

        scene_recycler = (RecyclerView) view.findViewById(R.id.scene_recycler);
        scene_recycler.setLayoutManager(new GridLayoutManager(getActivity(),3));

    }

    private void initData() {
        dataList = new ArrayList<>();
        for (int i = 0; i < 11; i++) {
            dataList.add(new ScenesInfo(i+"","scene"+i));
        }

        adapter = new ScenesAdapter(dataList);
        adapter.setOnItemClickListener(this);
        scene_recycler.setAdapter(adapter);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.menu_btn:
                Activity activity = getActivity();
                if (activity instanceof HomeActivity) {
                    ((HomeActivity) activity).toggleDrawerLayout();
                }
                break;
        }
    }

    @Override
    public void onItemClick(View view, ScenesInfo scenesInfo) {
        Toast.makeText(getActivity(), "item", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void addItemClick() {
        Toast.makeText(getActivity(), "add", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(getActivity(), NewScenceActivity.class);
        startActivity(intent);
    }

    public void register(){

    }
    public void unRegister(){

    }
}
