package com.pengyao.minidouyin;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private RelativeLayout relativelayout;
    /**
     * 首页
     */
    private TextView tv_home;
    private ImageView iv_home;
    private ImageView iv_shoot;
    /**
     * 消息
     */
    private TextView tv_msg;
    private ImageView iv_msg;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        //getSupportActionBar().hide();
        getSupportFragmentManager().beginTransaction().replace(R.id.relativelayout, new Home()).commit();
    }

    private void initView() {
        relativelayout = (RelativeLayout) findViewById(R.id.relativelayout);
        tv_home = (TextView) findViewById(R.id.tv_home);
        tv_home.setOnClickListener(this);
        iv_home = (ImageView) findViewById(R.id.iv_home);
        iv_shoot = (ImageView) findViewById(R.id.iv_shoot);
        iv_shoot.setOnClickListener(this);
        tv_msg = (TextView) findViewById(R.id.tv_msg);
        iv_msg = (ImageView) findViewById(R.id.iv_msg);
        tv_msg.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            default:
                break;
            case R.id.tv_home:
                getSupportFragmentManager().beginTransaction().replace(R.id.relativelayout, new Home()).commit();
                iv_msg.setBackgroundResource(R.drawable.linen);
                tv_msg.setTextColor(Color.parseColor("#ffffff"));
                iv_home.setBackgroundResource(R.drawable.linew);
                tv_home.setTextColor(Color.parseColor("#55c7bb"));
                break;
            case R.id.iv_shoot:
                startActivity(new Intent(MainActivity.this, Capsture.class));
                break;
            case R.id.tv_msg: getSupportFragmentManager().beginTransaction().replace(R.id.relativelayout, new News()).commit();
                iv_home.setBackgroundResource(R.drawable.linen);
                tv_home.setTextColor(Color.parseColor("#ffffff"));
                iv_msg.setBackgroundResource(R.drawable.linew);
                tv_msg.setTextColor(Color.parseColor("#55c7bb"));
                break;
        }
    }
}
