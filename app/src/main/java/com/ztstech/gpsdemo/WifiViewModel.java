package com.ztstech.gpsdemo;

import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class WifiViewModel extends ViewModel {
    public final MutableLiveData<WifiInfo> connectWifiInfo = new MutableLiveData<>();
    WifiManager wifiMgr;

    public WifiViewModel() {
        wifiMgr = (WifiManager) MyApplication.getContext().getSystemService(Context.WIFI_SERVICE);
        getLinkedWifiInfo();
    }

    /**
     * 获取当前连接wifi
     */
    public void getLinkedWifiInfo(){
        connectWifiInfo.setValue(wifiMgr.getConnectionInfo());
    }

    public void scanWifiList(){
//        wifiMgr.registerScanResultsCallback(getMain, new WifiManager.ScanResultsCallback() {
//            @Override
//            public void onScanResultsAvailable() {
//
//            }
//        });
    }
}