package com.ztstech.gpsdemo;

import android.net.wifi.ScanResult;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

public class WifiFragment extends Fragment {

    private WifiViewModel mViewModel;
    WifiChannelSheet wifiChannelSheet;

    public static WifiFragment newInstance() {
        return new WifiFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.wifi_fragment, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        wifiChannelSheet=view.findViewById(R.id.wifi_channel_sheet);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = ViewModelProviders.of(this).get(WifiViewModel.class);
        mViewModel.getScanWifiInfo().observe(this, new Observer<List<ScanResult>>() {
            @Override
            public void onChanged(List<ScanResult> scanResults) {
                wifiChannelSheet.setWifiInfos(scanResults);
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        mViewModel.scanWifiList(getContext());
    }


}