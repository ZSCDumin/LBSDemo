package com.edu.zscdm.lbsdemo;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.model.LatLng;

import java.util.ArrayList;
import java.util.List;

/**
 * 百度地图
 */
public class Main2Activity extends AppCompatActivity {

    public LocationClient mLocationClient;
    List<String> permissionList = new ArrayList<>();//权限列表，记录未允许的权限
    private BaiduMap baiduMap;
    private boolean isFirstLocate = true;
    private MapView mapView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mLocationClient = new LocationClient(getApplicationContext());
        mLocationClient.registerLocationListener(new MyLocationListener());//注册定位监听器，当获取到位置信息时，会回调这个监听器
        SDKInitializer.initialize(getApplicationContext());//一定要先初始化，再加载布局
        setContentView(R.layout.activity_main2);
        mapView = (MapView) findViewById(R.id.bmapView);
        baiduMap = mapView.getMap();
        baiduMap.setMyLocationEnabled(true);
        checkPermission();
    }

    public void checkPermission() {
        /**
         * 判断单个权限是否已经允许
         */
        if (ContextCompat.checkSelfPermission(Main2Activity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            permissionList.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }
        if (ContextCompat.checkSelfPermission(Main2Activity.this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            permissionList.add(Manifest.permission.READ_PHONE_STATE);
        }
        if (ContextCompat.checkSelfPermission(Main2Activity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            permissionList.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }

        /**
         * 判断所有权限是否已经申请完成
         */
        if (!permissionList.isEmpty()) {
            String[] permissions = permissionList.toArray(new String[permissionList.size()]);
            ActivityCompat.requestPermissions(Main2Activity.this, permissions, 1);
        } else {
            requestLocation();//请求位置信息
        }
    }

    private void requestLocation() {
        mLocationClient.start();//开始定位
        initLocation();
    }

    private void initLocation() {
        LocationClientOption option = new LocationClientOption();
        option.setScanSpan(5000);//设置更新间隔，5秒钟
        option.setLocationMode(LocationClientOption.LocationMode.Device_Sensors);//传感器模式，只进行GPS定位
        //LocationClientOption.LocationMode.Hight_Accuracy：高精度模式，在GPS信号正常的时候优先选择GPS,否则选择网络。
        //LocationClientOption.LocationMode.Battery_Saving：省电模式，只使用网络进行定位

        option.setIsNeedAddress(true);//获取当地详细的位置信息
        mLocationClient.setLocOption(option);
    }


    private void navigateTo(BDLocation location) {
        if (isFirstLocate) {
            Toast.makeText(this, "定位到：" + location.getAddress(), Toast.LENGTH_SHORT).show();
            LatLng ll = new LatLng(location.getLatitude(), location.getLongitude());//封装位置信息
            MapStatusUpdate update = MapStatusUpdateFactory.newLatLng(ll);
            baiduMap.animateMapStatus(update);
            update = MapStatusUpdateFactory.zoomTo(16f);//设置缩放级别，3-19，值越大越精确
            baiduMap.animateMapStatus(update);//将地图移动到指定的经纬度
            isFirstLocate = false;
        }
        MyLocationData.Builder locationBuilder = new MyLocationData.Builder();
        locationBuilder.latitude(location.getLatitude());
        locationBuilder.longitude(location.getLongitude());
        MyLocationData locationData = locationBuilder.build();
        baiduMap.setMyLocationData(locationData);//显示我的位置
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
        baiduMap.setMyLocationEnabled(false);
    }

    public class MyLocationListener implements BDLocationListener {

        @Override
        public void onReceiveLocation(BDLocation location) {
            if (location.getLocType() == BDLocation.TypeGpsLocation
                    || location.getLocType() == BDLocation.TypeNetWorkLocation) {
                navigateTo(location);
            }
        }
    }
}
