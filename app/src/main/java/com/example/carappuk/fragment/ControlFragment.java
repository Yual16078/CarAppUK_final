package com.example.carappuk.fragment;

import android.content.ContentResolver;
import android.content.Context;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;

import com.example.carappuk.R;
import com.example.carappuk.util.VibrateHelp;
import com.example.carappuk.util.VolumeUtil;


public class ControlFragment extends Fragment implements View.OnClickListener {


    private boolean[] sign = {false,false,false,false,false,false,false,false};
    private int driveModelSign = 0;
    private View mControlFragment;
    private FrameLayout tpms;
    private FrameLayout interior_lights;
    private FrameLayout child_safety_lock;
    private FrameLayout ventilation;
    private FrameLayout trunk;
    private FrameLayout fuel_cap;
    private FrameLayout lock_unlock;
    private FrameLayout lock_airbag;
    private LinearLayout ly_tpms;
    private LinearLayout ly_interior_lights;
    private LinearLayout ly_child_safety_lock;
    private LinearLayout ly_ventilation;
    private LinearLayout ly_trunk;
    private LinearLayout ly_fuel_cap;
    private LinearLayout ly_lock_unlock;
    private LinearLayout ly_lock_airbag;
    private SeekBar seek_bar_volume;
    private SeekBar seek_bar_brightness;
    private SeekBar seek_bar_led_light;
    private ImageButton btLockUnlock;
    private TextView tx_lock_unlock;
    private ImageButton btDriveModel;
    private TextView txDriveModel;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mControlFragment = inflater.inflate(R.layout.fragment_control, container, false);
        initView();
        return mControlFragment;
    }

    private void initView() {
        VolumeUtil volumeUtil = new VolumeUtil(getContext());

        tpms = mControlFragment.findViewById(R.id.tpms);
        interior_lights = mControlFragment.findViewById(R.id.interior_lights);
        child_safety_lock = mControlFragment.findViewById(R.id.child_safety_lock);
        ventilation = mControlFragment.findViewById(R.id.ventilation);
        lock_airbag = mControlFragment.findViewById(R.id.lock_airbag);
        trunk = mControlFragment.findViewById(R.id.trunk);
        fuel_cap = mControlFragment.findViewById(R.id.fuel_cap);
        lock_unlock = mControlFragment.findViewById(R.id.unlock_lock);


        ly_tpms = mControlFragment.findViewById(R.id.ly_tpms);
        ly_tpms.setOnClickListener(this);
        ly_interior_lights = mControlFragment.findViewById(R.id.ly_interior_lights);
        ly_interior_lights.setOnClickListener(this);
        ly_child_safety_lock = mControlFragment.findViewById(R.id.ly_child_safety_lock);
        ly_child_safety_lock.setOnClickListener(this);
        ly_ventilation = mControlFragment.findViewById(R.id.ly_ventilation);
        ly_ventilation.setOnClickListener(this);
        ly_lock_airbag = mControlFragment.findViewById(R.id.ly_lock_airbag);
        ly_lock_airbag.setOnClickListener(this);
        ly_trunk = mControlFragment.findViewById(R.id.ly_trunk);
        ly_trunk.setOnClickListener(this);
        ly_fuel_cap = mControlFragment.findViewById(R.id.ly_fuel_cap);
        ly_fuel_cap.setOnClickListener(this);
        ly_lock_unlock = mControlFragment.findViewById(R.id.ly_unlock_lock);
        ly_lock_unlock.setOnClickListener(this);
        seek_bar_volume = mControlFragment.findViewById(R.id.seek_bar_volume);
        seek_bar_brightness = mControlFragment.findViewById(R.id.seek_bar_brightness);
        seek_bar_led_light = mControlFragment.findViewById(R.id.seek_bar_led_light);
        btLockUnlock = mControlFragment.findViewById(R.id.bt_lock_unlock);
        tx_lock_unlock = mControlFragment.findViewById(R.id.tx_lock_unlock);

        seek_bar_volume.setMax(volumeUtil.getMediaMaxVolume());
        seek_bar_volume.setProgress(volumeUtil.getMediaVolume());
        seek_bar_volume.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                volumeUtil.setMediaVolume(i);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                VibrateHelp.vSimple(getContext());
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        seek_bar_led_light.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                VibrateHelp.vSimple(getContext());
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        seek_bar_brightness.setProgress(getScreenBrightness(getContext()));
        seek_bar_brightness.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                setAppScreenBrightness(i);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        txDriveModel = mControlFragment.findViewById(R.id.tx_drive_model);
        btDriveModel = mControlFragment.findViewById(R.id.bt_drive_model);
        btDriveModel.setOnClickListener(this);


    }

    /**
     * 2.设置 APP界面屏幕亮度值方法
     * **/
    private void setAppScreenBrightness(int birghtessValue) {
        Window window = getActivity().getWindow();
        WindowManager.LayoutParams lp = window.getAttributes();
        lp.screenBrightness = birghtessValue / 255.0f;
        window.setAttributes(lp);
    }

    /**
     * 1.获取系统默认屏幕亮度值 屏幕亮度值范围（0-255）
     * **/
    private int getScreenBrightness(Context context) {
        ContentResolver contentResolver = context.getContentResolver();
        int defVal = 125;
        return Settings.System.getInt(contentResolver,
                Settings.System.SCREEN_BRIGHTNESS, defVal);
    }



    @Override
    public void onClick(View view) {

        switch (view.getId()) {
            case R.id.ly_tpms:
                VibrateHelp.vSimple(getContext());
                if (!sign[0]) {
                    tpms.setBackgroundResource(R.drawable.control_fragment_true);
                    sign[0] = true;
                } else {
                    tpms.setBackgroundResource(R.drawable.control_fragment_card);
                    sign[0] = false;
                }
                break;
            case R.id.ly_ventilation:
                VibrateHelp.vSimple(getContext());
                if (!sign[1]) {
                    ventilation.setBackgroundResource(R.drawable.control_fragment_true);
                    sign[1] = true;
                } else {
                    ventilation.setBackgroundResource(R.drawable.control_fragment_card);
                    sign[1] = false;
                }
                break;
            case R.id.ly_fuel_cap:
                VibrateHelp.vSimple(getContext());
                if (!sign[2]) {
                    fuel_cap.setBackgroundResource(R.drawable.control_fragment_true);
                    sign[2] = true;
                } else {
                    fuel_cap.setBackgroundResource(R.drawable.control_fragment_card);
                    sign[2] = false;
                }
                break;
            case R.id.ly_child_safety_lock:
                VibrateHelp.vSimple(getContext());
                if (!sign[3]) {
                    child_safety_lock.setBackgroundResource(R.drawable.control_fragment_true);
                    sign[3] = true;
                } else {
                    child_safety_lock.setBackgroundResource(R.drawable.control_fragment_card);
                    sign[3] = false;
                }
                break;
            case R.id.ly_trunk:
                VibrateHelp.vSimple(getContext());
                if (!sign[4]) {
                    trunk.setBackgroundResource(R.drawable.control_fragment_true);
                    sign[4] = true;
                } else {
                    trunk.setBackgroundResource(R.drawable.control_fragment_card);
                    sign[4] = false;
                }
                break;
            case R.id.ly_unlock_lock:
                VibrateHelp.vSimple(getContext());
                if (!sign[5]) {
                    lock_unlock.setBackgroundResource(R.drawable.control_fragment_true);
                    btLockUnlock.setBackgroundResource(R.mipmap.control_lock);
                    tx_lock_unlock.setText("Lock");
                    sign[5] = true;
                } else {
                    lock_unlock.setBackgroundResource(R.drawable.control_fragment_card);
                    btLockUnlock.setBackgroundResource(R.mipmap.control_unlock);
                    tx_lock_unlock.setText("Unlock");
                    sign[5] = false;
                }
                break;
            case R.id.ly_lock_airbag:
                VibrateHelp.vSimple(getContext());
                if (!sign[6]) {
                    lock_airbag.setBackgroundResource(R.drawable.control_fragment_true);
                    sign[6] = true;
                } else {
                    lock_airbag.setBackgroundResource(R.drawable.control_fragment_card);
                    sign[6] = false;
                }
                break;
            case R.id.ly_interior_lights:
                VibrateHelp.vSimple(getContext());
                if (!sign[7]) {
                    interior_lights.setBackgroundResource(R.drawable.control_fragment_true);
                    sign[7] = true;
                } else {
                    interior_lights.setBackgroundResource(R.drawable.control_fragment_card);
                    sign[7] = false;
                }
                break;
            case R.id.bt_drive_model:
                VibrateHelp.vSimple(getContext());
                driveModelSign += 1;
                if (driveModelSign == 3)
                    driveModelSign = 0;
                switch (driveModelSign) {
                    case 0:
                        btDriveModel.setBackgroundResource(R.mipmap.model_comfort);
                        txDriveModel.setText("Comfort");
                        break;
                    case 2:
                        btDriveModel.setBackgroundResource(R.mipmap.model_saving);
                        txDriveModel.setText("Saving");
                        break;
                    case 1:
                        btDriveModel.setBackgroundResource(R.mipmap.model_sport);
                        txDriveModel.setText("Sport");
                        break;
                }
        }
    }
}