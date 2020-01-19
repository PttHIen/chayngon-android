package com.vantinviet.foodies.android.RActivitiesAndFragments.RiderModels;

import java.util.Date;

/**
 * Created by Nabeel on 1/17/2018.
 */

public class RParentModel {

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    String date;

    public Date getDateFormate() {
        return dateFormate;
    }

    public void setDateFormate(Date dateFormate) {
        this.dateFormate = dateFormate;
    }

    public Date dateFormate;
}
