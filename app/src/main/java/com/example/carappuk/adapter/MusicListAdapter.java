package com.example.carappuk.adapter;

import androidx.annotation.Nullable;


import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.example.carappuk.R;
import com.example.carappuk.model.Song;
import com.example.carappuk.util.MusicUtils;

import java.util.List;

public class MusicListAdapter extends BaseQuickAdapter<Song, BaseViewHolder> {

    public MusicListAdapter(int layoutResId, @Nullable List<Song> data) {
        super(layoutResId, data);

    }


    @Override
    protected void convert(BaseViewHolder helper, Song item) {
        //Assign a value to the control
        int duration = item.duration;
        String time = MusicUtils.formatTime(duration);

        helper.setText(R.id.tv_song_name,item.getSong().trim())//Song name
                .setText(R.id.tv_singer,item.getSinger()+" - "+item.getAlbum())//Singer - album
                .setText(R.id.tv_duration_time,time)//Song time
                //The song number, because getAdapterPosition gets the position is starting from 0, so add 1,
                //Because the position and 1 are both integer types, directly assigning values to TextView will report an error, so it is spliced ""
                .setText(R.id.tv_position,helper.getAdapterPosition()+1+"");
        // helper.addOnClickListener(R.id.item_music);//Add a click event to the item, pass data to the playback page or play music on this page, but this has been deprecated in the new version.


    }
}