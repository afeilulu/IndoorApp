package com.afeilulu.indoorapp.model;

/**
 * Created by chen on 14-10-30.
 */
public class Stadium {
    int id;
    String name;
    String phone;
    String city;
    String address;
    String picUrl;
    double lng; // 经度
    double lat; // 纬度

    public Stadium(int id,String name,String phone,String city,String address,String picUrl,double lng,double lat){
        this.id = id;
        this.name = name;
        this.phone =phone;
        this.city = city;
        this.address = address;
        this.picUrl = picUrl;
        this.lng =lng;
        this.lat = lat;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getPhone() {
        return phone;
    }

    public String getCity() {
        return city;
    }

    public String getAddress() {
        return address;
    }

    public String getPicUrl() {
        return picUrl;
    }

    public double getLng() {
        return lng;
    }

    public double getLat() {
        return lat;
    }
}
