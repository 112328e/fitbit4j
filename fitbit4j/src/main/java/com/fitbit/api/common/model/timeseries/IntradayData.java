package com.fitbit.api.common.model.timeseries;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class IntradayData {

    private String dateTime;
    private double value;

    public IntradayData(String dateTime, double value) {
        this.dateTime = dateTime;
        this.value = value;
    }

    public IntradayData(JSONObject json) throws JSONException {
        value = json.getDouble("value");
        dateTime = json.getString("dateTime");
    }

    public String getDateTime() {
        return dateTime;
    }

    public double getValue() {
        return value;
    }

    public static List<IntradayData> jsonArrayToDataList(JSONArray array) throws JSONException {
        List<IntradayData> intradayDataList = new ArrayList<IntradayData>(array.length());
        for (int i = 0; i < array.length(); i++) {
            JSONObject jsonData = array.getJSONObject(i);
            intradayDataList.add(new IntradayData(jsonData));
        }
        return intradayDataList;
    }
}
