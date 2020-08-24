package com.ztstech.gpsdemo;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.GnssStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.amap.api.location.AMapLocation;

import java.math.BigDecimal;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;


/**
 * 位置页面
 *
 * @author BugMonkey
 */
public class LocationFragment extends Fragment {
    private static final String TAG = "LocationFragment";
    private View mViewMargin;

    /** 坐标类型 */
    private TextView mTvType;
    /** 经度 */
    private TextView mTvLa;
    /** 纬度 */
    private TextView mTvLo;
    /** 精度 */
    private TextView mTvAccuracy;
    /** 高度 */
    private TextView mTvAltitude;
    /** 方位 */
    private TextView mTvBear;
    /** 速度 */
    private TextView mTvSpeed;
    /** 定位方式 */
    private TextView mTvLocationType;
    /** 地址 */
    private TextView mTvAddress;
    private TextView mTvSatelliteCount;
    /** 载噪比  api 30 时可以获得载噪比 */
    private TextView mTvBaseBandCn0;
    private SatellitesWidget mSatelliteWidget;

    //位置管理器
    private LocationManager manager;
    private LocationListener locationListener;
    private TableLayout mTableLayout;
    //当前正在使用的卫星
    private int curUsedSatellite = -1;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_location, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initView(view);
        initListener();
        initLocation();

    }

    @Override
    public void onResume() {
        super.onResume();
        if (getActivity() != null && getActivity().getWindow() != null) {
            getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        }

    }

    @Override
    public void onPause() {
        super.onPause();
        if (getActivity() != null && getActivity().getWindow() != null) {
            getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);

            // Clear the systemUiVisibility flag
            getActivity().getWindow().getDecorView().setSystemUiVisibility(0);
        }

    }

    @Override
    public void onDestroy() {
        super.onDestroy();

    }

    private void initListener() {
        locationListener = new GPSListener();
    }


    private void initView(View view) {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ConstraintLayout.LayoutParams.MATCH_PARENT, MapsActivity.getScreenTopMargin());
        mViewMargin = view.findViewById(R.id.margin_view);
        mTableLayout = (TableLayout) view.findViewById(R.id.table_layout);
        mTvSatelliteCount = (TextView) view.findViewById(R.id.tv_satellite_count);
        mTvBaseBandCn0 = (TextView) view.findViewById(R.id.tv_baseband_cn0);
        mTvType = (TextView) view.findViewById(R.id.tv_type);
        mTvLa = (TextView) view.findViewById(R.id.tv_la);
        mTvLo = (TextView) view.findViewById(R.id.tv_lo);
        mTvAccuracy = (TextView) view.findViewById(R.id.tv_accuracy);
        mTvAltitude = (TextView) view.findViewById(R.id.tv_altitude);
        mTvBear = (TextView) view.findViewById(R.id.tv_bear);
        mTvSpeed = (TextView) view.findViewById(R.id.tv_speed);
        mTvLocationType = (TextView) view.findViewById(R.id.tv_location_type);
        mTvAddress = (TextView) view.findViewById(R.id.tv_address);
        mSatelliteWidget = (SatellitesWidget) view.findViewById(R.id.satellite_widget);

//        mViewMargin.setLayoutParams(params);
    }

    /**
     * 初始化定位管理
     */
    private void initLocation() {
        manager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
        //判断GPS是否正常启动
        if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            Toast.makeText(getActivity(), "请开启GPS导航", Toast.LENGTH_SHORT).show();
            //返回开启GPS导航设置界面
            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivityForResult(intent, 0);
            return;
        }
        //添加卫星状态改变监听
        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        manager.registerGnssStatusCallback(new GnssStatus.Callback() {
            @Override
            public void onStarted() {
                super.onStarted();
            }

            @Override
            public void onStopped() {
                super.onStopped();
            }

            @Override
            public void onFirstFix(int ttffMillis) {
                super.onFirstFix(ttffMillis);
            }

            @Override
            public void onSatelliteStatusChanged(GnssStatus status) {
                super.onSatelliteStatusChanged(status);
                showSatelliteInfo(status);
            }
        }, new Handler());
        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_FINE);
//        criteria.setAltitudeRequired(false);
//        criteria.setBearingRequired(false);
//        criteria.setCostAllowed(true);
        criteria.setPowerRequirement(Criteria.POWER_LOW);
        String bestProvider = manager.getBestProvider(criteria, true);
        //1000位最小的时间间隔，1为最小位移变化；也就是说每隔1000ms会回调一次位置信息
        manager.requestLocationUpdates(bestProvider, 1000, 0, locationListener);
        showLocationInfo(manager.getLastKnownLocation(bestProvider));
    }

    /**
     * 表格内容
     */
    class TableRowViewHolder {
        View view;
        TextView mTvAzimuthDegrees;
        TextView mTvElevationDegrees;
        TextView mSatelliteConstellation;
        TextView mTvSvid;
        TextView mTvCarrierFrequencyHz;
        TextView mTvCn0Hz;
        TextView mEphemeris;
        TextView mAlmanac;
        TextView mTvBaseBandCn0;
        TextView mTvHasAlmanac;

        TableRowViewHolder(View view) {
            this.view = view;
            this.mTvAzimuthDegrees = (TextView) view.findViewById(R.id.tv_azimuth_degrees);
            this.mTvElevationDegrees = (TextView) view.findViewById(R.id.tv_elevation_degrees);
            this.mSatelliteConstellation = (TextView) view.findViewById(R.id.satellite_constellation);
            this.mTvSvid = (TextView) view.findViewById(R.id.tv_svid);
            this.mTvCarrierFrequencyHz = (TextView) view.findViewById(R.id.tv_carrier_frequency_hz);
            this.mTvCn0Hz = (TextView) view.findViewById(R.id.tv_Cn0_hz);
            this.mEphemeris = (TextView) view.findViewById(R.id.ephemeris);
            this.mAlmanac = (TextView) view.findViewById(R.id.almanac);
            this.mTvHasAlmanac = (TextView) view.findViewById(R.id.tv_has_almanac);
            this.mTvBaseBandCn0 = (TextView) view.findViewById(R.id.tv_baseband_cn0);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                //载噪比
                mTvBaseBandCn0.setVisibility(View.VISIBLE);
            } else {
                mTvBaseBandCn0.setVisibility(View.GONE);
            }

        }
    }


    /**
     * 显示卫星信息
     *
     * @param status
     */
    private void showSatelliteInfo(GnssStatus status) {
        if (getActivity() == null || getActivity().isFinishing()) {
            return;
        }
        int satelliteCount = status.getSatelliteCount();
        mTvSatelliteCount.setText("卫星总数：" + satelliteCount);
        mSatelliteWidget.updateSatellites(status);
        mTableLayout.removeViews(1, mTableLayout.getChildCount() - 1);
        for (int i = 0; i < satelliteCount; i++) {
            addTableRow(status, i);
        }
    }

    /**
     * 表格添加
     *
     * @param status
     * @param i
     */
    private void addTableRow(GnssStatus status, int i) {

        TableRowViewHolder tableRowViewHolder = new TableRowViewHolder(LayoutInflater.from(getActivity()).inflate(R.layout.satellite_info_row, null));
        //方向角
        tableRowViewHolder.mTvAzimuthDegrees.setText("" + status.getAzimuthDegrees(i) + "°");
        //高度角
        tableRowViewHolder.mTvElevationDegrees.setText("" + status.getElevationDegrees(i) + "°");
        //载频
        if (status.hasCarrierFrequencyHz(i)) {
            BigDecimal bigCf = new BigDecimal(status.getCarrierFrequencyHz(i) / 1024f / 1024f);
            float cf = bigCf.setScale(2, BigDecimal.ROUND_HALF_UP).floatValue();
            tableRowViewHolder.mTvCarrierFrequencyHz.setText("" + cf);
        } else {
            tableRowViewHolder.mTvCarrierFrequencyHz.setText("-");
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            //载噪比
            mTvBaseBandCn0.setText("" + status.getBasebandCn0DbHz(i));
        }
        //载波噪声密度
        tableRowViewHolder.mTvCn0Hz.setText("" + status.getCn0DbHz(i));
        //卫星星座类型
        tableRowViewHolder.mSatelliteConstellation.setText(getSatelliteName(status.getConstellationType(i)));

        //标识号SVID
        tableRowViewHolder.mTvSvid.setText("" + status.getSvid(i));
        //是否有星历数据
        tableRowViewHolder.mEphemeris.setText(status.hasEphemerisData(i) ? "√" : "-");
        //是否有历书数据
        tableRowViewHolder.mAlmanac.setText(status.hasAlmanacData(i) ? "√" : "-");
        tableRowViewHolder.mTvHasAlmanac.setText(i == curUsedSatellite ? "√" : "-");
        TableRow tableRow = (TableRow) tableRowViewHolder.view;
        mTableLayout.addView(tableRow);
    }

    private String getSatelliteName(int constellationType) {
        switch (constellationType) {
            case GnssStatus.CONSTELLATION_GPS:
                return "GPS";
            case GnssStatus.CONSTELLATION_SBAS:
                return "SBAS";
            case GnssStatus.CONSTELLATION_GLONASS:
                return "GLONASS";
            case GnssStatus.CONSTELLATION_QZSS:
                return "QZSS";
            case GnssStatus.CONSTELLATION_BEIDOU:
                return "北斗";
            case GnssStatus.CONSTELLATION_GALILEO:
                return "GALILEO";
            case GnssStatus.CONSTELLATION_IRNSS:
                return "IRNSS";
            default:
                return "未知";
        }
    }

    /**
     * 显示位置信息
     *
     * @param location
     */
    public void showLocationInfo(Location location) {
        if (location == null) {
            return;
        }
        mTvLa.setText(String.valueOf(location.getLongitude()));
        mTvLo.setText(String.valueOf(location.getLatitude()));
        mTvAccuracy.setText(String.valueOf(location.getAccuracy()));
        mTvAltitude.setText(String.valueOf(location.getAltitude()));
        mTvBear.setText(String.valueOf(location.getBearing()+"°"));
        mTvSpeed.setText(String.format("%s/秒", location.getSpeedAccuracyMetersPerSecond()));
        if (location instanceof AMapLocation) {
            AMapLocation aMapLocation = (AMapLocation) location;
            //坐标系
            mTvType.setText(String.valueOf(aMapLocation.getCoordType()));
            //详细地址
            mTvAddress.setText(String.valueOf(aMapLocation.getAddress()));
            //更新当前正在使用的卫星下标
            curUsedSatellite = aMapLocation.getSatellites();
            //定位方式 来源
            if(aMapLocation.getLocationType()!=AMapLocation.LOCATION_TYPE_SAME_REQ){
                mTvLocationType.setText(getLocationTypeName(aMapLocation.getLocationType()));

            }
        }

    }

    private String getLocationTypeName(int locationType) {
        switch (locationType){
            case AMapLocation.LOCATION_TYPE_GPS:
                return  "卫星";
            case AMapLocation.LOCATION_TYPE_WIFI:
                return "Wifi";
            case AMapLocation.LOCATION_TYPE_CELL:
                return "基站";
                case AMapLocation.LOCATION_TYPE_FIX_CACHE:
                return "网络定位缓存";
                case AMapLocation.LOCATION_TYPE_LAST_LOCATION_CACHE:
                return "最后位置缓存";
            case AMapLocation.LOCATION_TYPE_OFFLINE:
                return "离线定位结果";
            case AMapLocation. 	LOCATION_TYPE_SAME_REQ:
                return "传感器感知";
            default:
                return "-";
        }
    }

    /**
     * 位置信息回调
     */
    private class GPSListener implements LocationListener {
        @Override
        public void onLocationChanged(@NonNull Location location) {
            showLocationInfo(location);
        }

        @Override
        public void onProviderEnabled(@NonNull String provider) {
            Log.e(TAG, "onProviderEnabled: " + provider);
        }

        @Override
        public void onProviderDisabled(@NonNull String provider) {
            Log.e(TAG, "onProviderDisabled: " + provider);

        }
    }
}