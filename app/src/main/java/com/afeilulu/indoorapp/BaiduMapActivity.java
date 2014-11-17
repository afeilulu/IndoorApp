package com.afeilulu.indoorapp;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.afeilulu.indoorapp.model.Stadium;
import com.afeilulu.indoorapp.util.Config;
import com.afeilulu.indoorapp.util.LogUtil;
import com.androidquery.AQuery;
import com.androidquery.callback.AjaxStatus;
import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.MyLocationConfiguration;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.geocode.GeoCodeResult;
import com.baidu.mapapi.search.geocode.GeoCoder;
import com.baidu.mapapi.search.geocode.OnGetGeoCoderResultListener;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeOption;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeResult;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;


public class BaiduMapActivity extends ActionBarActivity implements
        OnGetGeoCoderResultListener {

    private static final String TAG = LogUtil.makeLogTag(BaiduMapActivity.class);
    private Drawable mActionBarBackgroundDrawable;
    private AQuery aq;
    private ArrayList<Stadium> stadiums;
    private GeoCoder mSearch = null; // 搜索模块，也可去掉地图模块独立使用
    private boolean isStadiumInfoGot = false;
    private ArrayList<Integer> marks;
    private int markIndex;

    // 定位相关
    LocationClient mLocClient;
    public MyLocationListenner myListener = new MyLocationListenner();
    private MyLocationConfiguration.LocationMode mCurrentMode;
    BitmapDescriptor mCurrentMarker;

    MapView mMapView;
    BaiduMap mBaiduMap;




    // UI相关
    Button requestLocButton;
    boolean isFirstLoc = true;// 是否首次定位

    BaiduMap.OnMarkerClickListener markerClickListener = new BaiduMap.OnMarkerClickListener() {
        @Override
        public boolean onMarkerClick(Marker marker) {
            Intent intent = new Intent(BaiduMapActivity.this, StadiumActivity.class);
            intent.putExtra("stadium_json", new Gson().toJson(stadiums.get(marker.getExtraInfo().getInt("stadium_index", 0))));
            startActivity(intent);

            return false;
        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.baidu_map, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            startActivity(new Intent(this, BMapApiDemoMain.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_baidu_map);

        mActionBarBackgroundDrawable = getResources().getDrawable(R.drawable.ab_background);
//        mActionBarBackgroundDrawable.setAlpha(0);

        if (Build.VERSION.SDK_INT < 11)
            getSupportActionBar().setBackgroundDrawable(mActionBarBackgroundDrawable);
        else
            getActionBar().setBackgroundDrawable(mActionBarBackgroundDrawable);

        aq = new AQuery(this);
        stadiums = new ArrayList<Stadium>();
        initMarks();

        requestLocButton = (Button) findViewById(R.id.button1);
        mCurrentMode = MyLocationConfiguration.LocationMode.NORMAL;
        requestLocButton.setText("普通");
        View.OnClickListener btnClickListener = new View.OnClickListener() {
            public void onClick(View v) {
                switch (mCurrentMode) {
                    case NORMAL:
                        requestLocButton.setText("跟随");
                        mCurrentMode = MyLocationConfiguration.LocationMode.FOLLOWING;
                        mBaiduMap
                                .setMyLocationConfigeration(new MyLocationConfiguration(
                                        mCurrentMode, true, mCurrentMarker));
                        break;
                    case COMPASS:
                        requestLocButton.setText("普通");
                        mCurrentMode = MyLocationConfiguration.LocationMode.NORMAL;
                        mBaiduMap
                                .setMyLocationConfigeration(new MyLocationConfiguration(
                                        mCurrentMode, true, mCurrentMarker));
                        break;
                    case FOLLOWING:
                        requestLocButton.setText("罗盘");
                        mCurrentMode = MyLocationConfiguration.LocationMode.COMPASS;
                        mBaiduMap
                                .setMyLocationConfigeration(new MyLocationConfiguration(
                                        mCurrentMode, true, mCurrentMarker));
                        break;
                }
            }
        };
        requestLocButton.setOnClickListener(btnClickListener);


        // 修改为自定义marker
        /*mCurrentMarker = BitmapDescriptorFactory
                .fromResource(R.drawable.icon_geo);
        mBaiduMap
                .setMyLocationConfigeration(new MyLocationConfiguration(
                        mCurrentMode, true, mCurrentMarker));
        */

        // 地图初始化
        mMapView = (MapView) findViewById(R.id.bmapView);
        mBaiduMap = mMapView.getMap();
        mBaiduMap.setOnMarkerClickListener(markerClickListener);

        // 开启定位图层
        mBaiduMap.setMyLocationEnabled(true);
        // 定位初始化
        mLocClient = new LocationClient(this);
        mLocClient.registerLocationListener(myListener);
        LocationClientOption option = new LocationClientOption();
        option.setOpenGps(true);// 打开gps
        option.setCoorType("bd09ll"); // 设置坐标类型
        option.setScanSpan(1000);
        mLocClient.setLocOption(option);
        mLocClient.start();

        // 初始化搜索模块，注册事件监听
        mSearch = GeoCoder.newInstance();
        mSearch.setOnGetGeoCodeResultListener(this);

    }

    /**
     * 定位SDK监听函数
     */
    public class MyLocationListenner implements BDLocationListener {

        @Override
        public void onReceiveLocation(BDLocation location) {
            // map view 销毁后不在处理新接收的位置
            if (location == null || mMapView == null)
                return;
            MyLocationData locData = new MyLocationData.Builder()
                    .accuracy(location.getRadius())
                            // 此处设置开发者获取到的方向信息，顺时针0-360
                    .direction(100).latitude(location.getLatitude())
                    .longitude(location.getLongitude()).build();
            mBaiduMap.setMyLocationData(locData);
            if (isFirstLoc) {
                isFirstLoc = false;
                LatLng ll = new LatLng(location.getLatitude(),
                        location.getLongitude());
                MapStatusUpdate u = MapStatusUpdateFactory.newLatLng(ll);
                mBaiduMap.animateMapStatus(u);

                // 获取城市地址
                // 反Geo搜索
                mSearch.reverseGeoCode(new ReverseGeoCodeOption()
                        .location(ll));
            }

        }

        public void onReceivePoi(BDLocation poiLocation) {
        }
    }

    @Override
    protected void onPause() {
        mMapView.onPause();
        super.onPause();
    }

    @Override
    protected void onResume() {
        mMapView.onResume();
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        // 退出时销毁定位
        mLocClient.stop();
        // 关闭定位图层
        mBaiduMap.setMyLocationEnabled(false);
        mMapView.onDestroy();
        mMapView = null;
        mSearch.destroy(); // GeoCoder
        super.onDestroy();
    }

    @Override
    public void onGetGeoCodeResult(GeoCodeResult result) {
        if (result == null || result.error != SearchResult.ERRORNO.NO_ERROR) {
            Toast.makeText(this, "抱歉，未能找到结果", Toast.LENGTH_LONG)
                    .show();
            return;
        }

        mBaiduMap.clear();
        MarkerOptions markerOptions = new MarkerOptions().position(result.getLocation())
                .icon(BitmapDescriptorFactory
                        .fromResource(getTopMark()))
                .title(result.getAddress());
        mBaiduMap.addOverlay(markerOptions);
//        mBaiduMap.setMapStatus(MapStatusUpdateFactory.newLatLng(result
//                .getLocation()));
//        String strInfo = String.format("纬度：%f 经度：%f",
//                result.getLocation().latitude, result.getLocation().longitude);
//        Toast.makeText(this, strInfo, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onGetReverseGeoCodeResult(ReverseGeoCodeResult result) {
        if (result == null || result.error != SearchResult.ERRORNO.NO_ERROR) {
            Toast.makeText(this, "抱歉，未能找到结果", Toast.LENGTH_LONG)
                    .show();
            return;
        }

        // 获取城市,经度，纬度后
        // 从服务器获取该城市所有场馆信息
        // 按距离当前位置远近排序返回
        // TODO

        if (!isStadiumInfoGot) {
            mBaiduMap.clear();
            String url = Config.getStadiumListUrl(result.getAddressDetail().city);
//            String url = Config.getStadiumAllUrl();
            aq.ajax(url, JSONArray.class, BaiduMapActivity.this, "getStadiumListCallBack");
        } else {

            Bundle args = new Bundle();
            args.putInt("stadium_index", markIndex - 1);
            MarkerOptions markerOptions = new MarkerOptions().position(result.getLocation())
                    .icon(BitmapDescriptorFactory
                            .fromResource(getTopMark()))
                    .title(result.getAddress())
                    .extraInfo(args);
            mBaiduMap.addOverlay(markerOptions);

            // to mark get next address
            reverseGeoCode();
        }

    }

    public void getStadiumListCallBack(String url, JSONArray json, AjaxStatus status) {
        if (json != null) {
            //successful ajax call
//            String jsonString = "[{\"classa\":\"com.chinaairdome.Stadium\",\"id\":1,\"address\":\"陕西省西安市碑林区朱雀大街\",\"city\":\"西安市\",\"dateCreated\":\"2014-10-29T09:42:45Z\",\"lastUpdated\":\"2014-10-29T09:42:45Z\",\"lat\":34.209958,\"lng\":108.945478,\"name\":\"陕西省体育场\",\"phone\":\"13312345678\",\"picUrl\":\"http://www.epiaogo.com/data/show/b22e0b3a-f578-459a-bb0f-37f1bf01ddda.jpg\"},{\"classa\":\"com.chinaairdome.Stadium\",\"id\":2,\"address\":\"安定路甲3号(北京奥林匹克公园内,亚运村北)\",\"city\":\"北京市\",\"dateCreated\":\"2014-10-29T10:28:06Z\",\"lastUpdated\":\"2014-10-29T10:28:06Z\",\"lat\":39.950862,\"lng\":116.387064,\"name\":\"北京奥运主场鸟巢\",\"phone\":\"13312312312\",\"picUrl\":\"http://img2.imgtn.bdimg.com/it/u=852065470,3168935368&fm=23&gp=0.jpg\"}]";
//            Type collectionType = new TypeToken<Collection<Stadium>>(){}.getType();
//            Collection<Stadium> stadiums = new Gson().fromJson(jsonString,collectionType);

            for (int i = 0; i < json.length(); i++) {
                try {
                    JSONObject jsonObject = json.getJSONObject(i);
                    Stadium stadium = new Stadium(jsonObject.getInt("id"),
                            jsonObject.getString("name"),
                            jsonObject.getString("phone"),
                            jsonObject.getString("city"),
                            jsonObject.getString("address"),
                            jsonObject.getString("picUrl"),
                            jsonObject.getDouble("lng"),
                            jsonObject.getDouble("lat"));
                    stadiums.add(stadium);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            isStadiumInfoGot = true;

            // now we get list of address, we need mark them one by one
            // Geo搜索
//            mSearch.geocode(new GeoCodeOption().city("西安市").address("陕西省体育场"));

            markIndex = 0;
            if (stadiums.size() > 0) {
                reverseGeoCode();
            }

            // TODO save into local database to cache
        } else {
            Toast.makeText(this, R.string.stadium_not_found, Toast.LENGTH_SHORT).show();
        }
    }

    private void initMarks() {
        if (marks == null)
            marks = new ArrayList<Integer>();
        else
            marks.clear();

        marks.add(R.drawable.icon_marka);
        marks.add(R.drawable.icon_markb);
        marks.add(R.drawable.icon_markc);
        marks.add(R.drawable.icon_markd);
        marks.add(R.drawable.icon_marke);
        marks.add(R.drawable.icon_markf);
        marks.add(R.drawable.icon_markg);
        marks.add(R.drawable.icon_markh);
        marks.add(R.drawable.icon_marki);
        marks.add(R.drawable.icon_markj);
    }

    private int getTopMark() {
        if (marks != null && marks.size() > 0) {
            int result = marks.get(0);
            marks.remove(0);
            return result;
        }

        return R.drawable.icon_marka;
    }

    private void reverseGeoCode() {
        if (markIndex < stadiums.size()) {
            Stadium stadium = stadiums.get(markIndex);
            LatLng ll = new LatLng(stadium.getLat(), stadium.getLng());

            // 获取城市地址
            // 反Geo搜索
            mSearch.reverseGeoCode(new ReverseGeoCodeOption()
                    .location(ll));

            markIndex++;
        }
    }

}
