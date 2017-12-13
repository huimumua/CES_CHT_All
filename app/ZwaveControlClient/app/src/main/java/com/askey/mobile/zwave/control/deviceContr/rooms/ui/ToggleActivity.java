package com.askey.mobile.zwave.control.deviceContr.rooms.ui;

import android.content.Intent;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.askey.mobile.zwave.control.R;
import com.askey.mobile.zwave.control.deviceContr.adapter.ColorThirdAdapter;
import com.askey.mobile.zwave.control.deviceContr.scenes.SceneActionInfo;

import java.util.ArrayList;
import java.util.List;

public class ToggleActivity extends AppCompatActivity implements View.OnClickListener{
    private ImageView right;
    private List<View> mList;
    private int colors[] = {Color.GREEN,Color.YELLOW,Color.WHITE,Color.RED,Color.CYAN,Color.MAGENTA};
    private ColorPickViewOne pickOne;
    private ColorPickViewTwo pickTwo;
    private ColorPickViewThird pickThird;
    private CheckBox offDeviceone,offDeviceTwo;
    private RelativeLayout colorOne,colorTwo,colorThird;
    private Button btnRightColor,btnLeftColor;
    private int currentColor = 1;
    private Intent fromIntent;
    private String fromActivity;
    private SceneActionInfo sceneActionInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_toggle);

        initView();

    }
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        sceneActionInfo = getIntent().getParcelableExtra("sceneActionInfo");
    }

    private void initView() {
        fromIntent = getIntent();
        sceneActionInfo = getIntent().getParcelableExtra("sceneActionInfo");

        right = (ImageView) findViewById(R.id.iv_right);
        right.setOnClickListener(this);

        btnRightColor = (Button) findViewById(R.id.btn_right_color);
        btnLeftColor = (Button) findViewById(R.id.btn_left_color);
        btnRightColor.setOnClickListener(this);
        btnLeftColor.setOnClickListener(this);

        //中心button
        offDeviceone = (CheckBox) findViewById(R.id.iv_off_one);
        offDeviceTwo = (CheckBox) findViewById(R.id.iv_off_two);
        offDeviceone.setOnClickListener(this);
        offDeviceTwo.setOnClickListener(this);

        //调色板
        colorOne = (RelativeLayout) findViewById(R.id.ll_color_one);
        colorTwo = (RelativeLayout) findViewById(R.id.ll_color_two);
        colorThird = (RelativeLayout) findViewById(R.id.ll_color_third);

        pickOne = (ColorPickViewOne) findViewById(R.id.color_picker_one);
        pickTwo = (ColorPickViewTwo) findViewById(R.id.color_picker_two);
        pickThird = (ColorPickViewThird) findViewById(R.id.color_picker_third);
        pickOne.setOnColorChangedListener(new ColorPickViewOne.OnColorChangedListener() {

            @Override
            public void onColorChange(int color) {
                Log.i("BulbActivity",color + "" );
            }

        });

        pickTwo.setOnColorChangedListener(new ColorPickViewTwo.OnColorChangedListener() {

            @Override
            public void onColorChange(int color) {
                Log.i("BulbActivity",color + "onColorChange" );
            }

        });

        initMenuItem();
        pickThird.setAdapter(new ColorThirdAdapter(this,mList));

        pickThird.setOnItemClickListener(new ColorPickViewThird.OnItemClickListener() {
            @Override
            public void onItemClickListener(View v, int position) {
                if (position == mList.size() - 1) {
                    Log.i("BulbActivity","off" );
                } else {
                    Log.i("BulbActivity",colors[position] + "onItemClickListener" );
                }

            }
        });
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.iv_right:
                Intent intent = null;

                fromActivity = fromIntent.getStringExtra("from");

                if (fromActivity != null && (DoActionActivity.class.getSimpleName()).equals(fromActivity)) {

                    intent = new Intent(ToggleActivity.this, TimerActivity.class);

                    intent.putExtra("from", ToggleActivity.class.getSimpleName());
////                    intent.putExtra("arr", fromIntent.getStringExtra("arr"));
//                    intent.putExtra("nodeId", fromIntent.getStringExtra("nodeId"));
////                    intent.putExtra("interfaceId", fromIntent.getStringExtra("interfaceId"));
////                    intent.putExtra("groupId", fromIntent.getStringExtra("groupId"));
//                    intent.putExtra("type", fromIntent.getStringExtra("type"));
//                    intent.putExtra("action",fromIntent.getStringExtra("action"));
//                    intent.putExtra("name",fromIntent.getStringExtra("action"));
//                    startActivity(intent);

                } else if (fromActivity != null && (ActionSummaryActivity.class.getSimpleName()).equals(fromActivity)) {
                    intent = new Intent(ToggleActivity.this, ActionSummaryActivity.class);

                    intent.putExtra("from", ToggleActivity.class.getSimpleName());
//                    intent.putExtra("nodeId", fromIntent.getStringExtra("nodeId"));
////                    intent.putExtra("interfaceId", fromIntent.getStringExtra("interfaceId"));
////                    intent.putExtra("groupId", fromIntent.getStringExtra("groupId"));
////                    intent.putExtra("arr",fromIntent.getStringExtra("arr"));//需要从nodeid获取
//                    intent.putExtra("type", fromIntent.getStringExtra("type"));
//                    intent.putExtra("action", fromIntent.getStringExtra("action"));
//                    intent.putExtra("timer", fromIntent.getStringExtra("timer"));
//                    intent.putExtra("name", fromIntent.getStringExtra("name"));

                }

                intent.putExtra("sceneActionInfo", sceneActionInfo);
                startActivity(intent);

                break;
            case R.id.btn_left_color:
                changeColor();
                break;
            case R.id.btn_right_color:
                changeColor();
                break;
        }
    }

    private void changeColor() {
        if (currentColor == 1) {
            colorOne.setVisibility(View.GONE);
            colorTwo.setVisibility(View.VISIBLE);
            colorThird.setVisibility(View.GONE);
            currentColor++;
        } else if (currentColor == 2) {
            colorOne.setVisibility(View.VISIBLE);
            colorTwo.setVisibility(View.GONE);
            colorThird.setVisibility(View.GONE);
            currentColor++;
        } else if (currentColor == 3) {
            colorOne.setVisibility(View.GONE);
            colorTwo.setVisibility(View.GONE);
            colorThird.setVisibility(View.VISIBLE);
            currentColor = 1;
        }
    }

    // 初始化菜单项
    private void initMenuItem() {
        mList = new ArrayList<>();
        ImageView color;
//        {Color.CYAN,Color.YELLOW,Color.WHITE,Color.RED,Color.BLUE,Color.MAGENTA};
        color = new ImageView(this);
        color.setImageResource(R.drawable.circlegreen);
        color.setColorFilter(colors[0]);
        mList.add(color);
        color = new ImageView(this);
        color.setImageResource(R.drawable.circlegreen);
        color.setColorFilter(colors[1]);
        mList.add(color);
        color = new ImageView(this);
        color.setImageResource(R.drawable.circlestrokes);
//        color.setColorFilter(colors[2]);
        mList.add(color);
        color = new ImageView(this);
        color.setImageResource(R.drawable.circlegreen);
        color.setColorFilter(colors[3]);
        mList.add(color);
        color = new ImageView(this);
        color.setImageResource(R.drawable.circlegreen);
        color.setColorFilter(colors[4]);
        mList.add(color);
        color = new ImageView(this);
        color.setImageResource(R.drawable.circlegreen);
        color.setColorFilter(colors[5]);
        mList.add(color);
        color = new ImageView(this);
        color.setImageResource(R.mipmap.ic_launcher);
        mList.add(color);
    }
}
