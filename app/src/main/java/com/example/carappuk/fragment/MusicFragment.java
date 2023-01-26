package com.example.carappuk.fragment;

import static com.example.carappuk.util.DateUtil.parseTime;

import android.Manifest;

import android.media.MediaPlayer;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.listener.OnItemClickListener;
import com.example.carappuk.ApplicationCarApp;
import com.example.carappuk.MainActivity;
import com.example.carappuk.R;
import com.example.carappuk.adapter.MusicListAdapter;
import com.example.carappuk.media.ListeningMusicActivity;
import com.example.carappuk.model.Song;
import com.example.carappuk.util.Constant;
import com.example.carappuk.util.MusicUtils;
import com.example.carappuk.util.ObjectUtils;
import com.example.carappuk.util.PlayMusic;
import com.example.carappuk.util.SPUtils;
import com.example.carappuk.util.StatusBarUtil;
import com.example.carappuk.util.ToastUtils;
import com.example.carappuk.util.VibrateHelp;
import com.tbruyelle.rxpermissions2.RxPermissions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;


public class MusicFragment extends Fragment implements MediaPlayer.OnCompletionListener, View.OnClickListener{


    RecyclerView rvMusic;
    Button btnScan;
    LinearLayout scanLay;
    TextView tvClearList;
    TextView tvTitle;
    Toolbar toolbar;
    TextView tvPlayTime;
    SeekBar timeSeekBar;
    TextView tvTotalTime;
    ImageView btnPrevious;
    ImageView btnPlayOrPause;
    ImageView btnNext;
    TextView tvPlaySongInfo;
    ImageView playStateImg;
    LinearLayout playStateLay;


    private MusicListAdapter mAdapter;//歌曲适配器
    private List<Song> mList;//歌曲列表
    private RxPermissions rxPermissions;//权限请求
    private MediaPlayer mediaPlayer;//音频播放器
    private String musicData = null;
    // 记录当前播放歌曲的位置
    public int mCurrentPosition;
    private static final int INTERNAL_TIME = 1000;// 音乐进度间隔时间

    private Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message message) {
            // 展示给进度条和当前时间
            int progress = mediaPlayer.getCurrentPosition();
            timeSeekBar.setProgress(progress);
            tvPlayTime.setText(parseTime(progress));
            // 继续定时发送数据
            updateProgress();
            return true;
        }
    });
    private View mBaseView;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getActivity().findViewById(R.id.bottom_menu).setVisibility(View.GONE);

    }

    @Override
    public void onStop() {
        super.onStop();
        getActivity().findViewById(R.id.bottom_menu).setVisibility(View.VISIBLE);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        getActivity().findViewById(R.id.bottom_menu).setVisibility(View.GONE);
        mBaseView = inflater.inflate(R.layout.fragment_music, container, false);
        initView();
        ButterKnife.bind(getActivity());
        StatusBarUtil.StatusBarLightMode(getActivity());
        rxPermissions = new RxPermissions(getActivity());//使用前先实例化
        timeSeekBar.setOnSeekBarChangeListener(seekBarChangeListener);//滑动条监听
        musicData = SPUtils.getString(Constant.MUSIC_DATA_FIRST, "yes", getContext());


        if (musicData.equals("no")) {//说明是第一次打开APP，未进行扫描
            scanLay.setVisibility(View.GONE);
            initMusic();
        } else {
            scanLay.setVisibility(View.VISIBLE);
        }
//        if (mediaPlayer.isPlaying()) {
//            btnPlayOrPause.setBackground(getResources().getDrawable(R.mipmap.icon_play));
//            playStateImg.setBackground(getResources().getDrawable(R.mipmap.list_pause_state));
//        }

        return mBaseView;
    }

    private void initView() {
        rvMusic = mBaseView.findViewById(R.id.rv_music);
        btnScan = mBaseView.findViewById(R.id.btn_scan);
        scanLay = mBaseView.findViewById(R.id.scan_lay);
        tvClearList = mBaseView.findViewById(R.id.tv_clear_list);
        tvTitle = mBaseView.findViewById(R.id.tv_title);
        toolbar = mBaseView.findViewById(R.id.toolbar);
        tvPlayTime = mBaseView.findViewById(R.id.tv_play_time);
        timeSeekBar = mBaseView.findViewById(R.id.time_seekBar);
        tvTotalTime = mBaseView.findViewById(R.id.tv_total_time);
        btnPrevious = mBaseView.findViewById(R.id.btn_previous);
        btnPlayOrPause = mBaseView.findViewById(R.id.btn_play_or_pause);
        if (ApplicationCarApp.mediaPlayer.isPlaying()) {
            btnPlayOrPause.setBackgroundResource(R.mipmap.icon_play);
        }
        btnNext = mBaseView.findViewById(R.id.btn_next);
        tvPlaySongInfo = mBaseView.findViewById(R.id.tv_play_song_info);
        playStateImg = mBaseView.findViewById(R.id.play_state_img);
        playStateLay = mBaseView.findViewById(R.id.play_state_lay);
        tvClearList.setOnClickListener(this);
        btnScan.setOnClickListener(this);
        btnPrevious.setOnClickListener(this);
        btnPlayOrPause.setOnClickListener(this);
        btnNext.setOnClickListener(this);

    }


    private void permissionRequest() {//使用这个框架需要制定JDK版本，建议用1.8
        rxPermissions.request(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .subscribe(granted -> {
                    if (granted) {//请求成功之后开始扫描
                        initMusic();
                    } else {//失败时给一个提示
                        ToastUtils.showShortToast(getContext(), "未授权");
                    }
                });
    }

    //获取音乐列表
    private void initMusic() {
        mList = new ArrayList<>();//实例化

        //数据赋值
        mList = MusicUtils.getMusicData(getContext());//将扫描到的音乐赋值给音乐列表
        ApplicationCarApp.mList = mList;
        if (!ObjectUtils.isEmpty(mList) && mList != null) {
            scanLay.setVisibility(View.GONE);
            SPUtils.putString(Constant.MUSIC_DATA_FIRST, "no", getContext());
        }
        mAdapter = new MusicListAdapter(R.layout.item_music_rv_list, mList);//指定适配器的布局和数据源
        //线性布局管理器，可以设置横向还是纵向，RecyclerView默认是纵向的，所以不用处理,如果不需要设置方向，代码还可以更加的精简如下
        rvMusic.setLayoutManager(new LinearLayoutManager(getContext()));
        //如果需要设置方向显示，则将下面代码注释去掉即可
//        LinearLayoutManager manager = new LinearLayoutManager(this);
//        manager.setOrientation(RecyclerView.HORIZONTAL);
//        rvMusic.setLayoutManager(manager);

        //设置适配器
        rvMusic.setAdapter(mAdapter);

        //item的点击事件

        mAdapter.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(@NonNull BaseQuickAdapter<?, ?> adapter, @NonNull View view, int position) {
                if (view.getId() == R.id.item_music) {
                    mCurrentPosition = position;
                    changeMusic(mCurrentPosition);
                }
            }
        });
        //设置背景样式
        initStyle();

    }


    private void initStyle() {
        //toolbar背景变透明
        toolbar.setBackgroundColor(getResources().getColor(R.color.half_transparent));
        //文字变白色
        tvTitle.setTextColor(getResources().getColor(R.color.white));
        tvClearList.setTextColor(getResources().getColor(R.color.white));
        StatusBarUtil.transparencyBar(getActivity());
    }

//    @OnClick({R.id.tv_clear_list, R.id.btn_scan, R.id.btn_previous, R.id.btn_play_or_pause, R.id.btn_next})
//    public void onViewClicked(View view) {
//        switch (view.getId()) {
//            case R.id.tv_clear_list://清空数据
//                mList.clear();
//                mAdapter.notifyDataSetChanged();
//                SPUtils.putString(Constant.MUSIC_DATA_FIRST, "yes", getContext());
//                scanLay.setVisibility(View.VISIBLE);
//                toolbar.setBackgroundColor(getResources().getColor(R.color.white));
//                StatusBarUtil.StatusBarLightMode(getActivity());
//                tvTitle.setTextColor(getResources().getColor(R.color.black));
//                tvClearList.setTextColor(getResources().getColor(R.color.black));
//                if (mediaPlayer == null) {
//                    mediaPlayer = new MediaPlayer();
//                    mediaPlayer.setOnCompletionListener(this);//监听音乐播放完毕事件，自动下一曲
//                }
//                if (mediaPlayer.isPlaying()) {
//                    mediaPlayer.pause();
//                    mediaPlayer.reset();
//                }
//                break;
//            case R.id.btn_scan://扫描本地歌曲
//                // permissionRequest();
//                initMusic();
//                break;
//            case R.id.btn_previous://上一曲
//                changeMusic(--mCurrentPosition);//当前歌曲位置减1
//                break;
//            case R.id.btn_play_or_pause://播放或者暂停
//                // 首次点击播放按钮，默认播放第0首，下标从0开始
//                if (mediaPlayer == null) {
//                    changeMusic(0);
//                } else {
//                    if (mediaPlayer.isPlaying()) {
//                        mediaPlayer.pause();
//                        btnPlayOrPause.setBackground(getResources().getDrawable(R.mipmap.icon_pause));
//                        playStateImg.setBackground(getResources().getDrawable(R.mipmap.list_play_state));
//                        //如果你是用TextView的leftDrawable设置的图片，在代码里面就可以通过下面代码来动态更换
////                        Drawable leftImg = getResources().getDrawable(R.mipmap.list_play_state);
////                        leftImg.setBounds(0, 0, leftImg.getMinimumWidth(), leftImg.getMinimumHeight());
////                        tvPlaySongInfo.setCompoundDrawables(leftImg, null, null, null);
//                    } else {
//                        mediaPlayer.start();
//                        btnPlayOrPause.setBackground(getResources().getDrawable(R.mipmap.icon_play));
//                        playStateImg.setBackground(getResources().getDrawable(R.mipmap.list_pause_state));
//                    }
//                }
//                break;
//            case R.id.btn_next://下一曲
//                changeMusic(++mCurrentPosition);//当前歌曲位置加1
//                break;
//        }
//    }

    //切歌
    private void changeMusic(int position) {
        ApplicationCarApp.POSITION = position;
        Log.e("ListeningMusicActivity", "position:" + position);
        if (position < 0) {
            ApplicationCarApp.POSITION = mCurrentPosition = position = mList.size() - 1;
            Log.e("ListeningMusicActivity", "mList.size:" + mList.size());
        } else if (position > mList.size() - 1) {
            ApplicationCarApp.POSITION = mCurrentPosition = position = 0;
        }
        Log.e("ListeningMusicActivity", "position:" + position);
        if (mediaPlayer == null) {
            mediaPlayer = ApplicationCarApp.mediaPlayer;
            mediaPlayer.setOnCompletionListener(this);//监听音乐播放完毕事件，自动下一曲
        }
        try {
            // 切歌之前先重置，释放掉之前的资源
            mediaPlayer.reset();
            // 设置播放源
            Log.d("Music", mList.get(position).path);
            mediaPlayer.setDataSource(mList.get(position).path);
            tvPlaySongInfo.setText("歌名： " + mList.get(position).song +
                    "  歌手： " + mList.get(position).singer);

//            Glide.with(this).load(mList.get(position).album_art).into(songImage);
            tvPlaySongInfo.setSelected(true);//跑马灯效果
            playStateLay.setVisibility(View.VISIBLE);

            // 开始播放前的准备工作，加载多媒体资源，获取相关信息
            mediaPlayer.prepare();
            // 开始播放
            mediaPlayer.start();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // 切歌时重置进度条并展示歌曲时长
        timeSeekBar.setProgress(0);
        timeSeekBar.setMax(mediaPlayer.getDuration());
        tvTotalTime.setText(parseTime(mediaPlayer.getDuration()));

        updateProgress();
        if (mediaPlayer.isPlaying()) {
            btnPlayOrPause.setBackground(getResources().getDrawable(R.mipmap.icon_play));
            playStateImg.setBackground(getResources().getDrawable(R.mipmap.list_pause_state));
        } else {
            btnPlayOrPause.setBackground(getResources().getDrawable(R.mipmap.icon_pause));
            playStateImg.setBackground(getResources().getDrawable(R.mipmap.list_play_state));
        }
    }

    private void updateProgress() {
        // 使用Handler每间隔1s发送一次空消息，通知进度条更新
        Message msg = Message.obtain();// 获取一个现成的消息
        // 使用MediaPlayer获取当前播放时间除以总时间的进度
        int progress = mediaPlayer.getCurrentPosition();
        msg.arg1 = progress;
        mHandler.sendMessageDelayed(msg, INTERNAL_TIME);
    }

    //滑动条监听
    SeekBar.OnSeekBarChangeListener seekBarChangeListener = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        // 当手停止拖拽进度条时执行该方法
        // 获取拖拽进度
        // 将进度对应设置给MediaPlayer
        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            int progress = seekBar.getProgress();
            mediaPlayer.seekTo(progress);
        }
    };

    //播放完成之后自动下一曲
    @Override
    public void onCompletion(MediaPlayer mp) {
        changeMusic(++mCurrentPosition);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.tv_clear_list://清空数据
                VibrateHelp.vSimple(getContext());
                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.pause();
                }
                mList.clear();
                mAdapter.notifyDataSetChanged();
                SPUtils.putString(Constant.MUSIC_DATA_FIRST, "yes", getContext());
                scanLay.setVisibility(View.VISIBLE);
                toolbar.setBackgroundColor(getResources().getColor(R.color.white));
                StatusBarUtil.StatusBarLightMode(getActivity());
                tvTitle.setTextColor(getResources().getColor(R.color.black));
                tvClearList.setTextColor(getResources().getColor(R.color.black));
                if (mediaPlayer == null) {
                    mediaPlayer = ApplicationCarApp.mediaPlayer;
                    mediaPlayer.setOnCompletionListener(this);//监听音乐播放完毕事件，自动下一曲
                }
                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.pause();
                    mediaPlayer.reset();
                }
                break;
            case R.id.btn_scan://扫描本地歌曲
                VibrateHelp.vSimple(getContext());
                permissionRequest();
                break;
            case R.id.btn_previous://上一曲
                VibrateHelp.vSimple(getContext());
                changeMusic(--mCurrentPosition);//当前歌曲位置减1
                break;
            case R.id.btn_play_or_pause://播放或者暂停
                VibrateHelp.vSimple(getContext());
                // 首次点击播放按钮，默认播放第0首，下标从0开始
                if (mediaPlayer == null) {
                    changeMusic(0);
                } else {
                    if (mediaPlayer.isPlaying()) {
                        mediaPlayer.pause();
                        btnPlayOrPause.setBackground(getResources().getDrawable(R.mipmap.icon_pause));
                        playStateImg.setBackground(getResources().getDrawable(R.mipmap.list_play_state));
                        //如果你是用TextView的leftDrawable设置的图片，在代码里面就可以通过下面代码来动态更换
//                        Drawable leftImg = getResources().getDrawable(R.mipmap.list_play_state);
//                        leftImg.setBounds(0, 0, leftImg.getMinimumWidth(), leftImg.getMinimumHeight());
//                        tvPlaySongInfo.setCompoundDrawables(leftImg, null, null, null);
                    } else {
                        mediaPlayer.start();
                        btnPlayOrPause.setBackground(getResources().getDrawable(R.mipmap.icon_play));
                        playStateImg.setBackground(getResources().getDrawable(R.mipmap.list_pause_state));
                    }
                }
                break;
            case R.id.btn_next://下一曲
                VibrateHelp.vSimple(getContext());
                changeMusic(++mCurrentPosition);//当前歌曲位置加1
                break;
        }
    }
}