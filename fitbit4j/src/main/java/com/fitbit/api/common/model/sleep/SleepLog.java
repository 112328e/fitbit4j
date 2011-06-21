package com.fitbit.api.common.model.sleep;

import org.json.JSONException;
import org.json.JSONObject;

public class SleepLog {
    long logId;
    String startTime;
    boolean isMainSleep;
    long duration;
    int minutesToFallAsleep;
    int minutesAsleep;
    int minutesAwake;
    int awakeningsCount;
    int timeInBed;

    public SleepLog(long logId, String startTime, boolean mainSleep, long duration, int minutesToFallAsleep,
                    int minutesAsleep, int minutesAwake, int awakeningsCount, int timeInBed) {
        this.logId = logId;
        this.startTime = startTime;
        isMainSleep = mainSleep;
        this.duration = duration;
        this.minutesToFallAsleep = minutesToFallAsleep;
        this.minutesAsleep = minutesAsleep;
        this.minutesAwake = minutesAwake;
        this.awakeningsCount = awakeningsCount;
        this.timeInBed = timeInBed;
    }

    public SleepLog(JSONObject json) throws JSONException {
        logId = json.getLong("logId");
        startTime = json.getString("startTime");
        isMainSleep = json.getBoolean("isMainSleep");
        duration = json.getLong("duration");
        minutesToFallAsleep = json.getInt("minutesToFallAsleep");
        minutesAsleep = json.getInt("minutesAsleep");
        minutesAwake = json.getInt("minutesAwake");
        awakeningsCount = json.getInt("awakeningsCount");
        timeInBed = json.getInt("timeInBed");
    }

    public long getLogId() {
        return logId;
    }

    public String getStartTime() {
        return startTime;
    }

    public boolean isMainSleep() {
        return isMainSleep;
    }

    public long getDuration() {
        return duration;
    }

    public int getMinutesToFallAsleep() {
        return minutesToFallAsleep;
    }

    public int getMinutesAsleep() {
        return minutesAsleep;
    }

    public int getMinutesAwake() {
        return minutesAwake;
    }

    public int getAwakeningsCount() {
        return awakeningsCount;
    }

    public int getTimeInBed() {
        return timeInBed;
    }
}
