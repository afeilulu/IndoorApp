package com.chinaairdome.indoorapp.util;

/**
 * Created by chen on 14-10-29.
 */
public class Config {
    private static final String SERVICE_URL = "http://chinaairdome.com:9080/indoor";
    private static final String STADIUM_LIST_URL = SERVICE_URL + "/stadium.json";
    private static final String SPORT_LIST_URL = SERVICE_URL + "/sport.json";

    public static String getStadiumListUrl(String city){
        return STADIUM_LIST_URL + "?city=" + city;
    }

    public static String getStadiumAllUrl(){
        return STADIUM_LIST_URL;
    }

    public static String getSportListUrl(String stadiumid){
        return SPORT_LIST_URL + "?stadiumid=" + stadiumid;
    }
}
