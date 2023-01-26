package com.example.carappuk.fragment;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.Preview;
import androidx.camera.core.VideoCapture;
import androidx.camera.core.impl.utils.executor.CameraXExecutors;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.os.SystemClock;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Chronometer;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.carappuk.R;
import com.example.carappuk.util.Constants;
import com.example.carappuk.util.VibrateHelp;
import com.google.common.util.concurrent.ListenableFuture;

import java.io.File;
import java.util.concurrent.ExecutionException;


public class CameraFragment extends Fragment {

    private static final String TAG = "MainActivity";
    private PreviewView mPreviewView;
    private ImageButton mIvCamera;
    private ImageView mIvReverse;
    private ListenableFuture<ProcessCameraProvider> mProcessCameraProviderListenableFuture;
    private ImageCapture mImageCapture;
    private boolean isFront;//是否开启前置摄像头，默认false
    private Camera mCamera;
    private VideoCapture mVideoCapture;
    private ProcessCameraProvider mProcessCameraProvider;
    private CameraSelector mCameraSelector;
    private Preview mPreview;
    //    private ImageAnalysis mImageAnalysis;
    private int mPermissionGranted;
    private boolean mIsLessOneMin;//这次拍摄的视频是否不足一分钟

    private View mBaseFragment;
    private ImageButton mIvStop;
    private Chronometer chronometer;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestPermission();
        mPermissionGranted = PackageManager.PERMISSION_GRANTED;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        getActivity().findViewById(R.id.bottom_menu).setVisibility(View.GONE);
        mBaseFragment = inflater.inflate(R.layout.fragment_camera, container, false);
        initView();
        initCamera();
        return mBaseFragment;
    }


    @Override
    public void onStop() {
        super.onStop();
        getActivity().findViewById(R.id.bottom_menu).setVisibility(View.VISIBLE);
    }


    private void initView() {

        mPreviewView = mBaseFragment.findViewById(R.id.preview);
        mIvCamera = mBaseFragment.findViewById(R.id.iv_camera);
        mIvReverse = mBaseFragment.findViewById(R.id.iv_reverse);
        mIvStop = mBaseFragment.findViewById(R.id.iv_stop_camera);
        chronometer = mBaseFragment.findViewById(R.id.meter);
        mIvCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                VibrateHelp.vSimple(getContext());
                if (isFront) {
                    takePhoto();
                } else {
                    startVideo();
                    chronometer.start();
                    mIvStop.setVisibility(View.VISIBLE);
                    mIvCamera.setVisibility(View.INVISIBLE);
                }
            }
        });

        mIvStop.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("RestrictedApi")
            @Override
            public void onClick(View view) {
                VibrateHelp.vSimple(getContext());
                chronometer.stop();
                chronometer.setBase(SystemClock.elapsedRealtime());
                mVideoCapture.stopRecording();
                mIvCamera.setVisibility(View.VISIBLE);
                mIvStop.setVisibility(View.INVISIBLE);
            }
        });
        mIvReverse.setOnClickListener(v -> {
            VibrateHelp.vSimple(getContext());
            isFront = !isFront;
            initCamera();
        });
    }

    private void requestPermission() {
        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.CAMERA) != mPermissionGranted
                || ContextCompat.checkSelfPermission(getContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != mPermissionGranted
                || ContextCompat.checkSelfPermission(getContext(), Manifest.permission.RECORD_AUDIO) != mPermissionGranted) {
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.CAMERA,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.RECORD_AUDIO}, 1);
        } else {
        }
    }

    private void initCamera() {
        if (isFront) {
            chronometer.setVisibility(View.INVISIBLE);
        } else {
            chronometer.setVisibility(View.VISIBLE);
        }
        mProcessCameraProviderListenableFuture = ProcessCameraProvider.getInstance(getContext());
        mProcessCameraProviderListenableFuture.addListener(() -> {
            try {
                mProcessCameraProvider = mProcessCameraProviderListenableFuture.get();
                bindPreview(mProcessCameraProvider);
            } catch (ExecutionException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }, ContextCompat.getMainExecutor(getContext()));
    }


    @SuppressLint("RestrictedApi")
    private void takePhoto() {
        String path = Constants.getFilePath() + File.separator + System.currentTimeMillis() + ".jpg";
        ImageCapture.OutputFileOptions outputFileOptions = new ImageCapture.OutputFileOptions
                .Builder(new File(path)).build();
        mImageCapture.takePicture(outputFileOptions, CameraXExecutors.mainThreadExecutor(), new ImageCapture.OnImageSavedCallback() {
            @Override
            public void onImageSaved(@NonNull ImageCapture.OutputFileResults outputFileResults) {
                Toast.makeText(getActivity(), "图片以保存" + path, Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                Uri uri = Uri.fromFile(new File(path));
                intent.setData(uri);
                getActivity().sendBroadcast(intent);

            }

            @Override
            public void onError(@NonNull ImageCaptureException exception) {
                Toast.makeText(getActivity(), exception.toString(), Toast.LENGTH_SHORT).show();
            }
        });
    }


    @SuppressLint("RestrictedApi")
    private void startVideo() {
        Log.e(TAG, "startVideo: ");
        String path = Constants.getFilePath() + File.separator + System.currentTimeMillis() + ".mp4";
        @SuppressLint("RestrictedApi") VideoCapture.OutputFileOptions build = new VideoCapture.OutputFileOptions.Builder(new File(path)).build();
        mVideoCapture.startRecording(build, CameraXExecutors.mainThreadExecutor(), new VideoCapture.OnVideoSavedCallback() {
            @Override
            public void onVideoSaved(@NonNull VideoCapture.OutputFileResults outputFileResults) {
                if (mIsLessOneMin) {
                    new File(path).delete();
                } else {
                    Toast.makeText(getActivity(), "视频已保存" + outputFileResults.getSavedUri().getPath(), Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                    Uri uri = Uri.fromFile(new File(outputFileResults.getSavedUri().getPath()));
                    intent.setData(uri);
                    getActivity().sendBroadcast(intent);
                }

            }

            @Override
            public void onError(int videoCaptureError, @NonNull String message, @Nullable Throwable cause) {
                Log.e(TAG, "onError: " + message);
                new File(path).delete();//视频不足一秒会走到这里来，但是视频依然生成了，所以得删掉
            }
        });
    }


    @SuppressLint("RestrictedApi")
    private void bindPreview(ProcessCameraProvider processCameraProvider) {
        //创建preview
        mPreview = new Preview.Builder().build();
        //指定所需的相机选项,设置摄像头镜头切换
        mCameraSelector = new CameraSelector.Builder().requireLensFacing(isFront ? CameraSelector.LENS_FACING_FRONT :
                CameraSelector.LENS_FACING_BACK).build();
        //将 Preview 连接到 PreviewView。
        mPreview.setSurfaceProvider(mPreviewView.getSurfaceProvider());
        //将所选相机和任意用例绑定到生命周期。

        mImageCapture = new ImageCapture.Builder()
                .setTargetRotation(mPreviewView.getDisplay().getRotation())
                .build();

        mVideoCapture = new VideoCapture.Builder()
                .setTargetRotation(mPreviewView.getDisplay().getRotation())
                .setVideoFrameRate(25)//每秒的帧数
                .setBitRate(3 * 1024 * 1024)//设置每秒的比特率
                .build();

        processCameraProvider.unbindAll();
        mCamera = processCameraProvider.bindToLifecycle(this, mCameraSelector,
                mImageCapture, mVideoCapture, mPreview);
    }

}