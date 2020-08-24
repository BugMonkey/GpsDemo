package com.ztstech.gpsdemo;


import android.Manifest;
import android.content.res.Resources;
import android.graphics.Rect;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.DisplayCutout;
import android.view.View;
import android.view.WindowInsets;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.maps2d.AMap;
import com.amap.api.maps2d.CameraUpdate;
import com.amap.api.maps2d.CameraUpdateFactory;
import com.amap.api.maps2d.LocationSource;
import com.amap.api.maps2d.MapView;
import com.amap.api.maps2d.model.CameraPosition;
import com.amap.api.maps2d.model.LatLng;
import com.amap.api.maps2d.model.MyLocationStyle;
import com.yanzhenjie.permission.Action;
import com.yanzhenjie.permission.AndPermission;

import java.util.List;

import androidx.appcompat.app.AppCompatActivity;

public class MapsActivity extends AppCompatActivity implements View.OnClickListener, LocationSource, AMapLocationListener {


    private static final String TAG = "MapsActivity";
    private static int screenTopMargin;
    private static int screenBottomMargin;

    private MapView mMapView;

    private String[] permissions = new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};
    LocationFragment locationFragment;
    private AMap aMap;
    private OnLocationChangedListener mListener;
    private AMapLocationClient mlocationClient;
    private AMapLocationClientOption mLocationOption;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= 28) {
            getWindow().getDecorView().setOnApplyWindowInsetsListener(new View.OnApplyWindowInsetsListener() {
                @Override
                public WindowInsets onApplyWindowInsets(View view, WindowInsets windowInsets) {
                    Log.e("notch", "onApplyWindowInsets");
                    if (windowInsets == null) {
                        return windowInsets;
                    }
                    DisplayCutout cutout = windowInsets.getDisplayCutout();
                    if (cutout == null) {
                        Log.e("notch", "cutout==null, is not notch screen");//通过cutout是否为null判断是否刘海屏手机

                    } else {
                        List<Rect> rects = cutout.getBoundingRects();
                        if (rects == null || rects.size() == 0) {
                            Log.e("notch", "rects==null || rects.size()==0, is not notch screen");
                        } else {
                            Log.e("notch", "rect size:" + rects.size());//注意：刘海的数量可以是多个
                            for (Rect rect : rects) {
                                Log.e("notch", "cutout.getSafeInsetTop():" + cutout.getSafeInsetTop()
                                        + ", cutout.getSafeInsetBottom():" + cutout.getSafeInsetBottom()
                                        + ", cutout.getSafeInsetLeft():" + cutout.getSafeInsetLeft()
                                        + ", cutout.getSafeInsetRight():" + cutout.getSafeInsetRight()
                                        + ", cutout.rects:" + rect
                                );
                            }

                            screenTopMargin = cutout.getSafeInsetTop();
                            screenBottomMargin = cutout.getSafeInsetBottom();
                        }
                    }
                    return windowInsets;
                }
            });
        } else {
            screenTopMargin = this.getStatusBarHeight(this.getResources());
            screenBottomMargin = 0;
        }
        setContentView(R.layout.activity_maps);
        mMapView = (MapView) findViewById(R.id.map);
        mMapView.onCreate(savedInstanceState);
        initMapView();
        locationFragment = (LocationFragment) getSupportFragmentManager()
                .findFragmentById(R.id.location_fragment);

        if (AndPermission.hasPermissions(this, permissions)) {
            initLocation();
        } else {
            requestPermission();
        }


    }
    MyLocationStyle myLocationStyle;

    private void initMapView() {
        if (aMap == null) {
            aMap = mMapView.getMap();
        }
        //初始化定位蓝点样式类myLocationStyle.myLocationType(MyLocationStyle.LOCATION_TYPE_LOCATION_ROTATE);//连续定位、且将视角移动到地图中心点，定位点依照设备方向旋转，并且会跟随设备移动。（1秒1次定位）如果不设置myLocationType，默认也会执行此种模式。
        myLocationStyle = new MyLocationStyle();
        //设置连续定位模式下的定位间隔，只在连续定位模式下生效，单次定位模式下不会生效。单位为毫秒。
        myLocationStyle.interval(2000);
        myLocationStyle.myLocationType(MyLocationStyle.LOCATION_TYPE_SHOW);//连续定位、蓝点会跟随设备移动。
        aMap.setMyLocationStyle(myLocationStyle);//设置定位蓝点的Style
//aMap.getUiSettings().setMyLocationButtonEnabled(true);设置默认定位按钮是否显示，非必需设置。
        aMap.setLocationSource(this);
        aMap.setMyLocationEnabled(true);// 设置为true表示启动显示定位蓝点，false表示隐藏定位蓝点并不进行定位，默认是false。
        aMap.setOnMyLocationChangeListener(new AMap.OnMyLocationChangeListener() {
            @Override
            public void onMyLocationChange(Location location) {
                locationFragment.showLocationInfo(location);
            }
        });
        //设置希望展示的地图缩放级别
        CameraUpdate mCameraUpdate = CameraUpdateFactory.zoomTo(17);
        aMap.moveCamera(mCameraUpdate);
    }

    /**
     * 获得状态栏高度
     * @param resources
     * @return
     */
    private int getStatusBarHeight(Resources resources) {
        int resourceId = resources.getIdentifier("status_bar_height", "dimen", "android");
        int height = resources.getDimensionPixelSize(resourceId);
        return height;
    }

    /**
     * 获取屏幕显示区域顶部距离
     * @return
     */
    public static int getScreenTopMargin() {
        return screenTopMargin;
    }

    /**
     * 获得屏幕显示区域底部距离
     * @return
     */
    public static int getScreenBottomMargin() {
        return screenBottomMargin;
    }

    private void initLocation() {

    }

    private void requestPermission() {
        AndPermission.with(this)
                .runtime()
                .permission(permissions)
                .onGranted(new Action<List<String>>() {
                    @Override
                    public void onAction(List<String> data) {
                        initLocation();
                    }
                })
                .onDenied(new Action<List<String>>() {
                    @Override
                    public void onAction(List<String> data) {

                    }
                }).start();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        //在activity执行onDestroy时执行mMapView.onDestroy()，销毁地图
        mMapView.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        //在activity执行onResume时执行mMapView.onResume ()，重新绘制加载地图
        mMapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        //在activity执行onPause时执行mMapView.onPause ()，暂停地图的绘制
        mMapView.onPause();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        //在activity执行onSaveInstanceState时执行mMapView.onSaveInstanceState (outState)，保存地图当前的状态
        mMapView.onSaveInstanceState(outState);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            default:
                break;
            case R.id.tv_satellite_count:
                break;
            case R.id.table_layout:
                break;
        }
    }


    @Override
    public void activate(OnLocationChangedListener listener) {
        mListener = listener;
        if (mlocationClient == null) {
            //初始化定位
            mlocationClient = new AMapLocationClient(this);
            //初始化定位参数
            mLocationOption = new AMapLocationClientOption();
            //设置定位回调监听
            mlocationClient.setLocationListener(this);
            //设置为高精度定位模式
            mLocationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
            //设置定位参数
            mlocationClient.setLocationOption(mLocationOption);
            // 此方法为每隔固定时间会发起一次定位请求，为了减少电量消耗或网络流量消耗，
            // 注意设置合适的定位时间的间隔（最小间隔支持为2000ms），并且在合适时间调用stopLocation()方法来取消定位请求
            // 在定位结束后，在合适的生命周期调用onDestroy()方法
            // 在单次定位情况下，定位无论成功与否，都无需调用stopLocation()方法移除请求，定位sdk内部会移除
            mlocationClient.startLocation();//启动定位
        }
    }

    @Override
    public void deactivate() {
        mListener = null;
        if (mlocationClient != null) {
            mlocationClient.stopLocation();
            mlocationClient.onDestroy();
        }
        mlocationClient = null;
    }

    /**
     * 位置信息变化
     * @param aMapLocation
     */
    @Override
    public void onLocationChanged(AMapLocation aMapLocation) {
        if (aMapLocation != null
                &&aMapLocation.getErrorCode() == 0) {
//            mListener.onLocationChanged(aMapLocation);// 显示系统小蓝点
            //移到中心
            CameraUpdate mCameraUpdate = CameraUpdateFactory.newCameraPosition(new CameraPosition(new LatLng(aMapLocation.getLatitude(),aMapLocation.getLongitude()),18,30,0));
            aMap.moveCamera(mCameraUpdate);
            locationFragment.showLocationInfo(aMapLocation);

        }
    }
}