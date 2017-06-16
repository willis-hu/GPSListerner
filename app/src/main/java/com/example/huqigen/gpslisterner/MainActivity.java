package com.example.huqigen.gpslisterner;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.GpsSatellite;
import android.location.GpsStatus;
import android.location.LocationProvider;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.Location;
import android.util.Log;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.location.Criteria;
import android.os.Build;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Iterator;

public class MainActivity extends AppCompatActivity {
    private TextView tv_location;
    private EditText editText;
    private LocationManager lm;
    private static final String TAG = "GpsActivity";

    @Override
    protected void onDestroy() {
        super.onDestroy();
        lm.removeUpdates(locationListener);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tv_location = (TextView) findViewById(R.id.tv_location);
        editText = (EditText) findViewById(R.id.editText);
        lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

//      判断gps是否已经启用
        if (!lm.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            Toast.makeText(this, "please open gps", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivityForResult(intent, 0);
            return;
        }
//        获取地理位置时的查询条件
        String bestProvider = lm.getBestProvider(getCriteria(), true);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        Location location = lm.getLastKnownLocation(bestProvider);
        updateView(location);

        lm.addGpsStatusListener(listener);

        lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 1, locationListener);

    }

    private LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            updateView(location);
            Log.i(TAG, "时间:" + location.getTime());
            Log.i(TAG, "经度:" + location.getLongitude());
            Log.i(TAG, "纬度:" + location.getLatitude());
            Log.i(TAG, "海拔:" + location.getAltitude());
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            switch (status) {
                case LocationProvider.AVAILABLE:
                    tv_location.setText("当前GPS状态为可见状态\n");
                    Log.i(TAG, "当前GPS状态为可见状态");
                    break;
                case LocationProvider.OUT_OF_SERVICE:
                    tv_location.setText("当前GPS状态为服务区外状态\n");
                    Log.i(TAG, "当前GPS状态为服务区外状态");
                    break;
                case LocationProvider.TEMPORARILY_UNAVAILABLE:
                    tv_location.setText("当前GPS状态为暂停服务状态\n");
                    Log.i(TAG, "当前GPS状态为暂停服务状态");
                    break;
            }
        }

        @Override
        public void onProviderEnabled(String provider) {
            if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            Location location = lm.getLastKnownLocation(provider);
            updateView(location);
        }

        @Override
        public void onProviderDisabled(String provider) {
            updateView(null);
        }
    };

    GpsStatus.Listener listener = new GpsStatus.Listener() {
        @Override
        public void onGpsStatusChanged(int event) {
            switch (event) {
                case GpsStatus.GPS_EVENT_FIRST_FIX:
                    tv_location.setText("第一次定位\n");
                    Log.i(TAG, "第一次定位");
                    break;
                case GpsStatus.GPS_EVENT_SATELLITE_STATUS:
                    tv_location.setText("卫星状态改变\n");
                    Log.i(TAG, "卫星状态改变");
                    if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        // TODO: Consider calling
                        //    ActivityCompat#requestPermissions
                        // here to request the missing permissions, and then overriding
                        //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                        //                                          int[] grantResults)
                        // to handle the case where the user grants the permission. See the documentation
                        // for ActivityCompat#requestPermissions for more details.
                        return;
                    }
                    GpsStatus gpsStatus = lm.getGpsStatus(null);
                    int maxSatellites = gpsStatus.getMaxSatellites();
                    Iterator<GpsSatellite> iters = gpsStatus.getSatellites().iterator();
                    int count = 0;
                    while (iters.hasNext()&&count <= maxSatellites){
                        GpsSatellite s = iters.next();
                        count++;
                    }
                    tv_location.setText("搜索到:" + count + "颗卫星\n");
                    Log.i(TAG,"搜索到:" + count + "颗卫星");
                    break;
                case GpsStatus.GPS_EVENT_STARTED:
                    tv_location.setText("定位启动\n");
                    Log.i(TAG, "定位启动");
                    break;
                case GpsStatus.GPS_EVENT_STOPPED:
                    tv_location.setText("定位结束\n");
                    Log.i(TAG, "定位结束");
                    break;

            }
        }
    };

    private void updateView(Location location){
        if(location != null){
            editText.setText("设备位置信息\n\n经度:");
            editText.append(String.valueOf(location.getLongitude()));
            editText.append("\n纬度:");
            editText.append(String.valueOf(location.getLatitude()));
        }
        else {
            editText.getEditableText().clear();
        }
    }

    private Criteria getCriteria()
    {
        Criteria criteria = new Criteria();
        //设置定位精确度
        criteria.setAccuracy(Criteria.ACCURACY_FINE);

        //设置是否要求速度
        criteria.setSpeedRequired(false);
        //设置是否允许运营商收费
        criteria.setCostAllowed(false);
        //设置是否需要方位信息
        criteria.setBearingAccuracy(0);
        //设置是否需要海拔信息
        criteria.setAltitudeRequired(false);
        //设置对电源的需求
        criteria.setPowerRequirement(Criteria.POWER_LOW);
        return criteria;
    }

    //获取权限
    private void getPersimmions()
    {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
        {
            ArrayList<String> permissions = new ArrayList<String>();
            //定位权限为必须权限，用户如果禁止，则每次进入都会申请
            //定位精确位置
            if(checkSelfPermission("Manifest.permission.ACCESS_FINE_LOCATION") != PackageManager.PERMISSION_GRANTED)
            {
                permissions.add("Manifest.permission.ACCESS_FINE_LOCATION");
            }
            if(checkSelfPermission("Manifest.permission.ACCESS_COARSE_LOCATION") != PackageManager.PERMISSION_GRANTED)
            {
                permissions.add("Manifest.permission.ACCESS_COARSE_LOCATION");
            }
            //读写权限和电话状态权限为非必要权限，只会申请一次
            //读写权限
            if(addPermission(permissions, "Manifest.permission.WRITE_EXTERNAL_STORAGE"))
            {
                //permissions +=
            }
        }

    }


    private boolean addPermission(ArrayList<String> permissionsList, String permission)
    {
        Boolean permission_add = true;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
        {
            if(checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED)
            {
                Log.i("TAG:", "正在请求权限");
                if(shouldShowRequestPermissionRationale(permission))
                {
                    permission_add = true;
                }
                else
                {
                    permissionsList.add(permission);
                    permission_add = false;
//                    return false;
                }
            }
        }
        else
        {
            permission_add = false;
//            return false;
        }
        return permission_add;
    }

}
