package com.example.carappuk;

import static com.example.carappuk.util.DateUtil.parseTime;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.carappuk.fragment.AmusementFragment;
import com.example.carappuk.fragment.CameraFragment;
import com.example.carappuk.fragment.ControlFragment;
import com.example.carappuk.fragment.Mainfragment;
import com.example.carappuk.fragment.MusicFragment;
import com.example.carappuk.fragment.NavigationFragment;
import com.example.carappuk.googlemap.MapsActivity;
import com.example.carappuk.media.ListeningMusicActivity;
import com.example.carappuk.util.VibrateHelp;
import com.example.carappuk.util.VolumeUtil;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private final static String Tag = "MainActivity";
    private ImageButton ic_bottom1;
    private ImageButton ic_bottom2;
    private ImageButton ic_bottom3;
    private ImageButton ic_bottom4;
    private ImageButton ic_bottom5;
    private int[] sign = {0,0,0,0,0};
    private SeekBar mSeekBarLeft;
    private SeekBar mSeekBarRight;
    private TextView mTextTemperatureLeft;
    private TextView mTextTemperatureRight;
    private ImageButton bt_home;
    private ImageButton bt_navigation;
    private ImageButton bt_amusement;
    private ImageButton bt_music;
    private ImageButton bt_camera;
    private LinearLayout control_volume;
    private TextView tx_volume;
    private SeekBar seekBarVolume;
    private VolumeUtil volumeUtil;
    private MusicFragment musicFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);// 设置全屏
        setContentView(R.layout.activity_main);
        musicFragment = new MusicFragment();
        ic_bottom1 = findViewById(R.id.ic_bottom1);
        ic_bottom2 = findViewById(R.id.ic_bottom2);
        ic_bottom3 = findViewById(R.id.ic_bottom3);
        ic_bottom4 = findViewById(R.id.ic_bottom4);
        ic_bottom5 = findViewById(R.id.ic_bottom5);
        bt_home = findViewById(R.id.bt_home);
        bt_navigation = findViewById(R.id.bt_navigation);
        bt_amusement = findViewById(R.id.bt_amusement);
        bt_music = findViewById(R.id.bt_music);
        bt_camera = findViewById(R.id.bt_camera);
        bt_home.setOnClickListener(this);
        bt_navigation.setOnClickListener(this);
        bt_amusement.setOnClickListener(this);
        bt_music.setOnClickListener(this);
        bt_camera.setOnClickListener(this);


        seekBarVolume = findViewById(R.id.seek_bar_volume);
        seekBarVolume.setVisibility(View.INVISIBLE);
        volumeUtil = new VolumeUtil(MainActivity.this);

        ///
        ic_bottom1.setOnClickListener(this);
        ic_bottom2.setOnClickListener(this);
        ic_bottom3.setOnClickListener(this);
        ic_bottom4.setOnClickListener(this);
        ic_bottom5.setOnClickListener(this);
        mSeekBarLeft = findViewById(R.id.seek_bar_left);
        mSeekBarRight = findViewById(R.id.seek_bar_right);
        mTextTemperatureLeft = findViewById(R.id.tx_temperature_left);
        mTextTemperatureRight = findViewById(R.id.tx_temperature_right);
        replaceFragment(new Mainfragment());
        mSeekBarLeft.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                mTextTemperatureLeft.setText(i + "C°");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        mSeekBarRight.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                mTextTemperatureRight.setText(i + "C°");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    private float mFirstTouchX;
    private float mFirstTouchY;
    private float mSecondTouchX;
    private float mSecondTouchY;

    int count = 0;
    int ii = 0;
    @Override
    public boolean onTouchEvent(MotionEvent event) {

        switch (event.getActionMasked()){
            case MotionEvent.ACTION_DOWN:
                //获取第一个点（A点）的位置
                mFirstTouchX = event.getX();
                mFirstTouchY = event.getY();
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                if(event.getActionIndex() == 1){
                    //获取第二个点（B点）的位置
                    mSecondTouchX = event.getX(1);
                    mSecondTouchY = event.getY(1);
                    //根据两点的位置获取两个触摸点之间的距离（AB）
                    float firstLengthX = Math.abs(mFirstTouchX - mSecondTouchX);
                    float firstLengthY = Math.abs(mFirstTouchY - mSecondTouchY);
                    //firstPointerLength = Math.sqrt(Math.pow(firstLengthX, 2) + Math.pow(firstLengthY, 2));
                }
                break;
            case MotionEvent.ACTION_MOVE:
                if (event.getPointerCount() == 1) {
                    if (event.getX() - mFirstTouchX > 500) {
                        replaceFragment(new Mainfragment());
                    }
                    if (event.getX() - mFirstTouchX < -500)
                        replaceFragment(new ControlFragment());
                }
                if(event.getPointerCount() >= 2){
                    //获取第一个点（A‘）的位置
                    float firstX = event.getX(0);
                    float firstY = event.getY(0);

                    //获取第二个点（B‘）的位置
                    float secondX = event.getX(1);
                    float secondY = event.getY(1);

                    //计算两点之间的距离（A'B'）
                    float secondLengthX = Math.abs(firstX - secondX);
                    float secondLengthY = Math.abs(firstY - secondY);
                    double secondPointerLength = Math.sqrt(Math.pow(secondLengthX, 2) + Math.pow(secondLengthY, 2));
                    ii = ii + 1;
                    if (ii == 5) {
                        count = (int)secondPointerLength;
                        ii = 0;
                    }
                    if ((int)secondPointerLength - count > 10) {
                        volumeUtil.setMediaVolume(volumeUtil.getMediaVolume()+1);
                    }

                    if ((int)secondPointerLength - count < -10) {
                        volumeUtil.setMediaVolume(volumeUtil.getMediaVolume()-1);
                    }

                    System.out.println("--------------iiiii" + ii);
                    System.out.println("--------------count" + count);
                    System.out.println("sendasdasdasdasdasd" + (int)secondPointerLength);

                }
                break;
        }
        return true;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.ic_bottom1:
                VibrateHelp.vSimple(this);
                if (sign[0] == 0){
                    ic_bottom1.setBackgroundResource(R.mipmap.ic_bottom1_ture);
                    sign[0] = 1;
                }
                else {
                    ic_bottom1.setBackgroundResource(R.mipmap.ic_bottom1);
                    sign[0] = 0;
                }
                break;
            case R.id.ic_bottom2:
                VibrateHelp.vSimple(this);
                if (sign[1] == 0){
                    ic_bottom2.setBackgroundResource(R.mipmap.ic_bottom2_ture);
                    sign[1] = 1;
                }
                else {
                    ic_bottom2.setBackgroundResource(R.mipmap.ic_bottom2);
                    sign[1] = 0;
                }
                break;
            case R.id.ic_bottom3:
                VibrateHelp.vSimple(this);
                if (sign[2] == 0){
                    ic_bottom3.setBackgroundResource(R.mipmap.ic_bottom3_ture);
                    sign[2] = 1;
                }
                else {
                    ic_bottom3.setBackgroundResource(R.mipmap.ic_bottom3);
                    sign[2] = 0;
                }
                break;
            case R.id.ic_bottom4:
                VibrateHelp.vSimple(this);
                if (sign[3] == 0){
                    ic_bottom4.setBackgroundResource(R.mipmap.ic_bottom4_ture);
                    sign[3] = 1;
                }
                else {
                    ic_bottom4.setBackgroundResource(R.mipmap.ic_bottom4);
                    sign[3] = 0;
                }
                break;
            case R.id.ic_bottom5:
                VibrateHelp.vSimple(this);
                if (sign[4] == 0){
                    seekBarVolume.setMax(volumeUtil.getMediaMaxVolume());
                    seekBarVolume.setProgress(volumeUtil.getMediaVolume());
                    seekBarVolume.setVisibility(View.VISIBLE);
                    sign[4] = 1;
                    seekBarVolume.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                        @Override
                        public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                            volumeUtil.setMediaVolume(i);

                        }

                        @Override
                        public void onStartTrackingTouch(SeekBar seekBar) {

                        }

                        @Override
                        public void onStopTrackingTouch(SeekBar seekBar) {

                        }
                    });
                } else {
                    seekBarVolume.setVisibility(View.INVISIBLE);
                    sign[4] = 0;
                }
                break;
            case R.id.bt_navigation:
                VibrateHelp.vSimple(this);
                replaceFragment(new NavigationFragment());
                break;
            case R.id.bt_home:
                VibrateHelp.vSimple(this);
                replaceFragment(new Mainfragment());
                break;
            case R.id.bt_music:
                VibrateHelp.vSimple(this);
                // startActivity(new Intent(this, ListeningMusicActivity.class));
                replaceFragment(musicFragment);
                break;
            case R.id.bt_camera:
                VibrateHelp.vSimple(this);
                replaceFragment(new CameraFragment());
                break;
            case R.id.bt_amusement:
                VibrateHelp.vSimple(this);
                replaceFragment(new AmusementFragment());
                break;
        }
    }
    private void replaceFragment(Fragment fragment) {

        FragmentManager fragmentManager = getSupportFragmentManager();

        FragmentTransaction transaction = fragmentManager.beginTransaction();

        transaction.replace(R.id.framelayout_right,fragment);

        transaction.addToBackStack(null);

        transaction.commit();
    }

    // ListeningMusic
    
}