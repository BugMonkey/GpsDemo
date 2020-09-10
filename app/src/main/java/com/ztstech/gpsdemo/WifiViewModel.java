package com.ztstech.gpsdemo;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;

import java.util.List;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class WifiViewModel extends ViewModel {
    private final MutableLiveData<WifiInfo> connectWifiInfo = new MutableLiveData<>();
    private final MutableLiveData<List<ScanResult>> scanWifiInfo = new MutableLiveData<>();
    WifiManager wifiMgr;

    public WifiViewModel() {
        wifiMgr = (WifiManager) MyApplication.getContext().getSystemService(Context.WIFI_SERVICE);
        getLinkedWifiInfo();
    }

    public MutableLiveData<WifiInfo> getConnectWifiInfo() {
        return connectWifiInfo;
    }

    public MutableLiveData<List<ScanResult>> getScanWifiInfo() {
        return scanWifiInfo;
    }

    /**
     * 获取当前连接wifi
     */
    public void getLinkedWifiInfo(){
        connectWifiInfo.setValue(wifiMgr.getConnectionInfo());
    }

    /**
     * 扫描wifi
     * @param context
     */
    public void scanWifiList(Context context){
        IntentFilter i = new IntentFilter();
        i.addAction (WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        scanWifiInfo.setValue(wifiMgr.getScanResults());
        context.registerReceiver(new BroadcastReceiver(){
            @Override
            public void onReceive(Context c, Intent i){
                scanWifiInfo.setValue(wifiMgr.getScanResults());
            }
        }, i );
    }
}