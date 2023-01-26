package com.example.carappuk.media;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.MediaController;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.carappuk.MainActivity;
import com.example.carappuk.R;

import java.io.File;

public class VideoViewingActivity extends AppCompatActivity {

    private double max_size = 1024;
    private int PICK_IMAGE_REQUEST = 1;
    private VideoView video;
    private Bitmap selectbp;
    private Bitmap selectbptmp;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_viewing);
        // staticLoadCVLibraries();

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        video = (VideoView)findViewById(R.id.videoview);

        Button selectImageBtn = (Button)findViewById(R.id.bt_choose);
        selectImageBtn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v) {selectImage(); }
        });
        findViewById(R.id.bt_pause).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(video.isPlaying())
                    video.pause();  // no stop()
            }
        });

        findViewById(R.id.bt_play).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!video.isPlaying())
                    video.start();
            }
        });
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if (requestCode == 1)
        {
            if (resultCode == RESULT_OK)
            {
                Uri uri = data.getData();
                video.setVideoURI(uri);
            }
        }

        MediaController mc = new MediaController(VideoViewingActivity.this);
        video.setMediaController(mc);
        video.requestFocus();

        try{
            video.start();
        }catch(Exception e){
            e.printStackTrace();
        }

        video.setOnCompletionListener(new MediaPlayer.OnCompletionListener(){
            @Override
            public void onCompletion(MediaPlayer mp)
            {
                Toast.makeText(VideoViewingActivity.this, "End of video play", Toast.LENGTH_SHORT).show();
            }
        });

        super.onActivityResult(requestCode, resultCode, data);
    }



    private void selectImage()
    {
        Intent intent = new Intent();
        intent.setType("video/*;image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent,"choose img..."), PICK_IMAGE_REQUEST);
    }



}
