package com.example.carappuk.fragment;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import com.example.carappuk.R;

public class NavigationFragment extends Fragment {

    private View mNavigationView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mNavigationView = inflater.inflate(R.layout.fragment_navigation, container, false);
        EditText address = mNavigationView.findViewById(R.id.ed_address);
        mNavigationView.findViewById(R.id.bt_start_navigation).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (address.getText().toString().length() < 1) {
                    Toast.makeText(getContext(), "Please enter the address", Toast.LENGTH_SHORT).show();
                }
                Intent i1 = new Intent();
                i1.setData(Uri.parse("baidumap://map/navi?query="+ address.getText().toString()+"&src=andr.example.carappuk"));
                startActivity(i1);
            }
        });
        return mNavigationView;
    }
}