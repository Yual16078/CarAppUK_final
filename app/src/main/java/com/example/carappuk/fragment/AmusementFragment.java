package com.example.carappuk.fragment;


import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import com.example.carappuk.R;
import com.example.carappuk.game.TetrisActivityAW;
import com.example.carappuk.media.PictureViewingActivity;
import com.example.carappuk.media.VideoViewingActivity;
import com.example.carappuk.util.VibrateHelp;


public class AmusementFragment extends Fragment {

    private View mBaseFragment;
    private final String[] items = {"Picture", "Video"};



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mBaseFragment = inflater.inflate(R.layout.fragment_amusement, container, false);
        initView();
        return mBaseFragment;
    }

    private void initView() {
        mBaseFragment.findViewById(R.id.bt_tetris).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                VibrateHelp.vSimple(getContext());
                Intent intent = new Intent(getActivity(), TetrisActivityAW.class);
                startActivity(intent);
            }
        });

        mBaseFragment.findViewById(R.id.bt_media).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                VibrateHelp.vSimple(getContext());
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setTitle("Select");
                builder.setItems(items, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Intent intent;
                        if (i == 0) {
                            intent = new Intent(getActivity(), PictureViewingActivity.class);
                        } else {
                            intent = new Intent(getActivity(), VideoViewingActivity.class);
                        }
                        startActivity(intent);
                    }
                });
                AlertDialog alertDialog =builder.create();//这个方法可以返回一个alertDialog对象
                alertDialog.show();

            }
        });
    }
}