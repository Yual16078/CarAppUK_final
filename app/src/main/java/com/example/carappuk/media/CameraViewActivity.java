package com.example.carappuk.media;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import android.Manifest;
import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.hardware.Camera.AutoFocusCallback;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.Size;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;

import com.example.carappuk.R;

/**
 * 视频录制
 */
@SuppressWarnings("deprecation")
public class CameraViewActivity extends Activity implements OnClickListener,
        SensorEventListener, Callback {
    private SurfaceView surfaceView; // 用于绘制缓冲图像的
    private Button luXiang_bt; // 开始录制的按钮
    private Button tingZhi_bt; // 停止录制的按钮
    private Button auto_focus; // 进行对焦
    private Button screenshot; // 截图
    private TextView time_tv; // 显示时间的文本框
    private MediaRecorder mRecorder;
    private boolean recording; // 记录是否正在录像,fasle为未录像, true 为正在录像
    private File videoFolder; // 存放视频的文件夹
    private File videFile; // 视频文件
    private Handler handler;
    private int time; // 时间
    private Camera myCamera; // 相机声明
    private SurfaceHolder holder; // 用来访问surfaceview的接口
    private SensorManager sManager; // 传感器管理者
    private Sensor sensor; // 传感器对象
    private int mX, mY, mZ; // x y z 坐标
    private Calendar calendar; // 日历
    private long lasttimestamp = 0; // 上一次用时的标志
    /**
     * 录制过程中,时间变化
     */
    private Runnable timeRun = new Runnable() {
        @Override
        public void run() {
            time++;
            time_tv.setText(time + "秒");
            handler.postDelayed(timeRun, 1000);
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);// 强制横屏
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_camera_view);
        initView();
        initSensor();
        initCreateFile();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[] {Manifest.permission.CAMERA}, 1);
            }
        }


    }

    /**
     * 对传感器进行初始化
     */
    private void initSensor() {
        sManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        sensor = sManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        if (sManager == null) {
// throw new IllegalArgumentException("SensorManager is null");
        }
        sManager.registerListener(this, sensor,
                SensorManager.SENSOR_DELAY_NORMAL);
    }

    /**
     * 文件的创建
     */
    private void initCreateFile() {
// 判断sd卡是否存在
        boolean sdCardExist = Environment.getExternalStorageState().equals(
                android.os.Environment.MEDIA_MOUNTED);
        if (sdCardExist) {
// 设定存放视频的文件夹的路径
            String path = Environment.getExternalStorageDirectory()
                    .getAbsolutePath()
                    + File.separator
                    + "VideoFolder"
                    + File.separator;
// 声明存放视频的文件夹的File对象
            videoFolder = new File(path);
// 如果不存在此文件夹,则创建
            if (!videoFolder.exists()) {
                videoFolder.mkdirs();
            }
// 设置surfaceView不管理的缓冲区
            surfaceView.getHolder().setType(
                    SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
// 设置surfaceView分辨率
//surfaceView.getHolder().setFixedSize(1000, 500);
            luXiang_bt.setOnClickListener(this);
        } else
            Toast.makeText(this, "未找到sdCard!", Toast.LENGTH_LONG).show();
    }

    /**
     * 初始化工作
     */
    private void initView() {
// 获取控件
        surfaceView = (SurfaceView) findViewById(R.id.surfaceview);
        luXiang_bt = (Button) findViewById(R.id.luXiang_bt);
        tingZhi_bt = (Button) findViewById(R.id.tingZhi_bt);
        time_tv = (TextView) findViewById(R.id.time);
        auto_focus = (Button) findViewById(R.id.auto_focus);
        screenshot = (Button) findViewById(R.id.screenshot);
        handler = new Handler();
        holder = surfaceView.getHolder();
        tingZhi_bt.setOnClickListener(this);
        auto_focus.setOnClickListener(this);
        screenshot.setOnClickListener(this);
// 添加回调
        holder.addCallback(this);
    }

    /**
     * 将Camera和mediaRecoder释放
     */
    @Override
    protected void onDestroy() {
        handler.removeCallbacks(timeRun);
        if (mRecorder != null) {
            mRecorder.release();
        }
        if (myCamera != null) {
            myCamera.stopPreview();
            myCamera.release();
        }
        super.onDestroy();
    }

    /**
     * 控件点击事件的监听
     */
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.luXiang_bt: // 录像点击事件
                if (!recording) {
                    try {
// 获取当前时间,作为视频文件的文件名
                        String nowTime = java.text.MessageFormat.format(
                                "{0,date,yyyyMMdd_HHmmss}",
                                new Object[]{new java.sql.Date(System.currentTimeMillis())});
// 声明视频文件对象
                        videFile = new File(videoFolder.getAbsoluteFile()
                                + File.separator + "video" + nowTime + ".mp4");
// 关闭预览并释放资源
                        myCamera.unlock();
                        mRecorder = new MediaRecorder();
                        mRecorder.setCamera(myCamera);
// 创建此视频文件
                        videFile.createNewFile();
                        mRecorder.setPreviewDisplay(surfaceView.getHolder()
                                .getSurface()); // 预览
                        mRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA); // 视频源
                        mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC); // 录音源为麦克风
                        mRecorder
                                .setOutputFormat(MediaRecorder.OutputFormat.MPEG_4); // 输出格式为mp4
/**
 *引用android.util.DisplayMetrics 获取分辨率
 */
// DisplayMetrics dm = new DisplayMetrics();
// getWindowManager().getDefaultDisplay().getMetrics(dm);
                        mRecorder.setVideoSize(800, 480); // 视频尺寸
                        mRecorder.setVideoEncodingBitRate(2 * 1280 * 720); //设置视频编码帧率
                        mRecorder.setVideoFrameRate(30); // 视频帧频率
                        mRecorder
                                .setVideoEncoder(MediaRecorder.VideoEncoder.MPEG_4_SP); // 视频编码
                        mRecorder
                                .setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB); // 音频编码
                        mRecorder.setMaxDuration(1800000); // 设置最大录制时间
                        mRecorder.setOutputFile(videFile.getAbsolutePath()); // 设置录制文件源
                        mRecorder.prepare(); // 准备录像
                        mRecorder.start(); // 开始录像
                        time_tv.setVisibility(View.VISIBLE); // 设置文本框可见
                        handler.post(timeRun); // 调用Runable
                        recording = true; // 改变录制状态为正在录制
                        setAutofocus();
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    } catch (IllegalStateException e) {
                        e.printStackTrace();
                    }
                } else
                    Toast.makeText(CameraViewActivity.this, "视频正在录制中...",
                            Toast.LENGTH_LONG).show();
                break;
            case R.id.tingZhi_bt: // 停止点击事件
                if (recording) {
                    mRecorder.stop();
                    mRecorder.release();
                    handler.removeCallbacks(timeRun);
                    time_tv.setVisibility(View.GONE);
                    int videoTimeLength = time;
                    time = 0;
                    recording = false;
                    Toast.makeText(
                            CameraViewActivity.this,
                            videFile.getAbsolutePath() + " " + videoTimeLength
                                    + "秒", Toast.LENGTH_LONG).show();
                }
// 开启相机
                if (myCamera == null) {
                    myCamera = Camera.open();
                    try {
                        myCamera.setPreviewDisplay(holder);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                myCamera.startPreview(); // 开启预览
                break;
            case R.id.auto_focus:
                setAutofocus();
                break;
            case R.id.screenshot:
                myCamera.autoFocus(new AutoFocusCallback() {
                    @Override
                    public void onAutoFocus(boolean success, Camera camera) {
                        if (success) {
                            camera.takePicture(null, null, jpegCallBack);
                        }
                    }
                });
                break;
        }
    }

    /**
     * 设置自动对焦
     */
    private void setAutofocus() {
        if (myCamera != null) {
            myCamera.autoFocus(new AutoFocusCallback() {
                @Override
                public void onAutoFocus(boolean success, Camera camera) {
                    if (success) {
                    }
                }
            });
        }
    }

    /**
     * 传感器改变调用的方法
     */
    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor == null) {
            return;
        }
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            int x = (int) event.values[0];
            int y = (int) event.values[1];
            int z = (int) event.values[2];
            calendar = Calendar.getInstance();
            long stamp = calendar.getTimeInMillis();
            int px = Math.abs(mX - x);
            int py = Math.abs(mY - y);
            int pz = Math.abs(mZ - z);
            int maxValue = getMaxValue(px, py, pz);
            if (maxValue == 2 && (stamp - lasttimestamp) == 30) {
                lasttimestamp = stamp;
                setAutofocus();
            }
            mX = x;
            mY = y;
            mZ = z;
        }
    }

    /**
     * 获取最大改变的值
     */
    private int getMaxValue(int px, int py, int pz) {
        int max = 0;
        if (px == py && px == pz) {
            max = px;
        } else if (py > px && py > pz) {
            max = py;
        } else if (pz > px && pz > py) {
            max = pz;
        }
        return max;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    /**
     * suraceView 创建执行的操作
     */
    @Override
    public void surfaceCreated(SurfaceHolder holder) {
// 开启相机
        if (myCamera == null) {
            myCamera = Camera.open();
            try {
                myCamera.setPreviewDisplay(holder);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * suraceView 状态改变执行的操作
     */
    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width,
                               int height) {
// 开始预览
        myCamera.startPreview();
        Parameters parameters = myCamera.getParameters();// 获取mCamera的参数对象
        Size largestSize = getBestSupportedSize(parameters
                .getSupportedPreviewSizes());
        parameters.setPreviewSize(largestSize.width, largestSize.height);// 设置预览图片尺寸
        largestSize = getBestSupportedSize(parameters
                .getSupportedPictureSizes());// 设置捕捉图片尺寸
        parameters.setPictureSize(largestSize.width, largestSize.height);
        myCamera.setParameters(parameters);
    }

    private Size getBestSupportedSize(List<Size> sizes) {
// 取能适用的最大的SIZE
        Size largestSize = sizes.get(0);
        int largestArea = sizes.get(0).height * sizes.get(0).width;
        for (Size s : sizes) {
            int area = s.width * s.height;
            if (area != largestArea) {
                largestArea = area;
                largestSize = s;
            }
        }
        return largestSize;
    }

    /**
     * suraceView 销毁执行的操作
     */
    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
// 关闭预览并释放资源
        if (myCamera != null) {
            myCamera.stopPreview();
            myCamera.release();
            myCamera = null;
        }
    }

    /**
     * 创建jpeg图片回调数据对象
     */
    private String filepath = "";
    private PictureCallback jpegCallBack = new PictureCallback() {
        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            Bitmap oldBitmap = BitmapFactory.decodeByteArray(data, 0,
                    data.length);
            Matrix matrix = new Matrix();
            matrix.setRotate(90);
            Bitmap newBitmap = Bitmap.createBitmap(oldBitmap, 0, 0,
                    oldBitmap.getWidth(), oldBitmap.getHeight(),
                    matrix, true);
            filepath = Environment.getExternalStorageDirectory()
                    + File.separator
                    + new SimpleDateFormat("yyyyMMddHHmmss").format(new Date())
                    + ".jpg";
            File file = new File(filepath);
            try {
                BufferedOutputStream bos = new BufferedOutputStream(
                        new FileOutputStream(file));
                newBitmap.compress(Bitmap.CompressFormat.JPEG, 85, bos);
                bos.flush();
                bos.close();
                camera.stopPreview();
                camera.startPreview();
                newBitmap.recycle();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    };
}