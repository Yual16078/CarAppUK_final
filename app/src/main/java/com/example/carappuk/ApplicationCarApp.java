package com.example.carappuk;

import android.app.Application;
import android.media.MediaPlayer;

import com.example.carappuk.model.Song;

import java.util.ArrayList;
import java.util.List;

public class ApplicationCarApp extends Application {
    public static List<Song> mList = new ArrayList<>();//歌曲列表
    public static int POSITION;
    public static MediaPlayer mediaPlayer = new MediaPlayer();
}
