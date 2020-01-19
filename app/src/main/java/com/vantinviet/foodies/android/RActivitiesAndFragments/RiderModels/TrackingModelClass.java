package com.vantinviet.foodies.android.RActivitiesAndFragments.RiderModels;

import java.security.SecureRandom;

public class TrackingModelClass {

    public String getRider_lat() {
        return rider_lat;
    }

    public void setRider_lat(String rider_lat) {
        this.rider_lat = rider_lat;
    }

    public String getRider_long() {
        return rider_long;
    }

    public void setRider_long(String rider_long) {
        this.rider_long = rider_long;
    }

    public String getRider_previous_lat() {
        return rider_previous_lat;
    }

    public void setRider_previous_lat(String rider_previous_lat) {
        this.rider_previous_lat = rider_previous_lat;
    }
    public void setRider_name(String rider_name) {
        this.rider_name = rider_name;
    }

    public String getRider_previous_long() {
        return rider_previous_long;
    }
    public String getRider_name() {
        return rider_name;
    }

    public void setRider_previous_long(String rider_previous_long) {
        this.rider_previous_long = rider_previous_long;
    }
    public String getRider_id() {
        return rider_id;
    }
    public void setRider_id(String rider_id) {
        this.rider_id = rider_id;
    }

    public TrackingModelClass(String rider_lat,String rider_long,String rider_previous_lat,String rider_previous_long,String rider_name,String rider_id){
        this.rider_lat = rider_lat;
        this.rider_long = rider_long;
        this.rider_previous_lat = rider_previous_lat;
        this.rider_previous_long = rider_previous_long;
        this.rider_name = rider_name;
        this.rider_id = rider_id;
    }

    private String rider_lat,rider_long,rider_previous_lat,rider_previous_long,rider_name,rider_id;

}
