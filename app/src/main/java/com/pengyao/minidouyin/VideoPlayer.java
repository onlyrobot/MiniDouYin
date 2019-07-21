package com.pengyao.minidouyin;

import androidx.appcompat.app.AppCompatActivity;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.airbnb.lottie.LottieAnimationView;
import com.pengyao.minidouyin.tool.FullScreenVideoView;
import com.pengyao.minidouyin.tool.OnDoubleClickListener;

public class VideoPlayer extends AppCompatActivity {

    private boolean isPlay = false;
    private long time = 0;
    private FullScreenVideoView videoView;
    private TextView tvAuthor;
    private TextView tvInfo;
    private ImageView avatar;
    private LottieAnimationView like;
    private int duration = 0;
    Handler handler = new Handler();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_player);
        videoView = findViewById(R.id.video_view);
        tvAuthor = findViewById(R.id.player_tv_author);
        tvInfo = findViewById(R.id.player_tv_info);
        avatar = findViewById(R.id.player_iv_avatar);
        like = findViewById(R.id.like);
        videoView.setVideoPath(getIntent().getStringExtra("video_path"));
        tvAuthor.setText("@" + getIntent().getStringExtra("author"));
        tvInfo.setText(getIntent().getStringExtra("info"));
        avatar.setImageResource(getIntent().getIntExtra("avatar", 0));
        like.setVisibility(View.INVISIBLE);
        videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                videoView.start();
                isPlay = true;
            }
        });
        videoView.setOnTouchListener(new OnDoubleClickListener(new OnDoubleClickListener.DoubleClickCallback(){

            @Override
            public void onDoubleClick() {
                Toast.makeText(VideoPlayer.this, "双击", Toast.LENGTH_LONG).show();
                duration = duration + 1;
            }
        }));
        handler.post(new Runnable() {
            @Override
            public void run() {
                if(duration > 0){
                    duration = duration - 1;
                    like.setVisibility(View.VISIBLE);
                    like.requestLayout();
                }else{
                    like.setVisibility(View.INVISIBLE);
                    like.requestLayout();
                }
                handler.postDelayed(this, 1000);
            }
        });
        videoView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                Toast.makeText(VideoPlayer.this, "点击", Toast.LENGTH_LONG).show();
                if(time == 0){
                    time = System.currentTimeMillis();
                }else{
                    if(System.currentTimeMillis() - time < 500){
                        Toast.makeText(VideoPlayer.this, "双击", Toast.LENGTH_LONG).show();
                        time = 0;
                    }
                }
                if (isPlay) {
                    videoView.pause();
                    isPlay = false;
                } else {
                    videoView.start();
                    isPlay = true;
                }
                return true;
            }
        });
    }
    @Override
    protected void onDestroy(){
        super.onDestroy();
        handler.removeCallbacksAndMessages(this);
    }
}
