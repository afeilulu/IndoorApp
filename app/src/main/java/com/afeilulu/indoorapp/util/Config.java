package com.afeilulu.indoorapp.util;

/**
 * Created by chen on 14-10-29.
 */
public class Config {
    private static final String SERVICE_URL = "http://192.168.4.20:8080/indoor";
    private static final String STADIUM_LIST_URL = SERVICE_URL + "/stadium.json";

    public static String getStadiumListUrl(String city){
        return STADIUM_LIST_URL + "?city=" + city;
    }

    public static String getStadiumAllUrl(){
        return STADIUM_LIST_URL;
    }
}
