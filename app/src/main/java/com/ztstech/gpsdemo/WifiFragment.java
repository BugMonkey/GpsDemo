package com.ztstech.gpsdemo;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

public class WifiFragment extends Fragment {

    private WifiViewModel mViewModel;

    public static WifiFragment newInstance() {
        return new WifiFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.wifi_fragment, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = ViewModelProviders.of(this).get(WifiViewModel.class);

        // TODO: Use the ViewModel
    }
    public class NetworkReceiver extends BroadcastReceiver {
        public static final int STATE1 = 1;//密码错误
        public static final int STATE2 = 2;//连接成功
        public static final int STATE3 = 3;//连接失败
        public static final int STATE4 = 4;//正在获取ip地址
        public static final int STATE5 = 5;//正在连接
        @Override
        public void onReceive(Context context, Intent intent) {
               if (WifiManager.WIFI_STATE_CHANGED_ACTION.equals(intent.getAction())) {
                // 监听wifi的打开与关闭，与wifi的连接无关
                int wifiState = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, 0);
                switch (wifiState) {
                    case WifiManager.WIFI_STATE_DISABLING://正在停止0
                        Toast.makeText(getActivity(), "wifi已关闭", Toast.LENGTH_SHORT).show();
                        break;
                    case WifiManager.WIFI_STATE_DISABLED://已停止1
                        break;
                    case WifiManager.WIFI_STATE_UNKNOWN://未知4
                        break;
                    case WifiManager.WIFI_STATE_ENABLING://正在打开2
                        break;
                    case WifiManager.WIFI_STATE_ENABLED://已开启3
                        Toast.makeText(getActivity(), "wifi已开启", Toast.LENGTH_SHORT).show();
                        break;
                    default:
                        break;
                }
            } else if (WifiManager.NETWORK_STATE_CHANGED_ACTION.equals(intent.getAction())) {
                // 监听wifi的连接状态即是否连上了一个有效无线路由
                Parcelable parcelableExtra = intent
                        .getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
                if (null != parcelableExtra) {
                    // 获取联网状态的NetWorkInfo对象
                    NetworkInfo networkInfo = (NetworkInfo) parcelableExtra;
                    //获取的State对象则代表着连接成功与否等状态
                    NetworkInfo.State state = networkInfo.getState();
                    //判断网络是否已经连接
                    boolean isConnected = state == NetworkInfo.State.CONNECTED;
                    if (isConnected) {
                        mViewModel.getLinkedWifiInfo();
                    }else {
                        mViewModel.connectWifiInfo.setValue(null);
                    }
                }
            }else if(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION.equals(intent.getAction())){
                   //wifi扫描结果
               }
        }
    }
}