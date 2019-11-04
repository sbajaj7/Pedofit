package com.example.pedofit.Model;

public class Steps {
    private long timestampMilliseconds;
    private int stepCount;
    private String stepTimeStamp;


    public Steps() {
    }

    public Steps(String timeStamp, int count, long milliseconds) {
        this.stepTimeStamp = timeStamp;
        this.stepCount = count;
        this.timestampMilliseconds = milliseconds;
    }

    public int getSteps() {
        return stepCount;
    }

    public void setSteps(int steps) {
        this.stepCount = steps;
    }

    public String getTimeStamp() {
        return stepTimeStamp;
    }

    public void setTimeStamp(String timeStamp) {
        this.stepTimeStamp = timeStamp;
    }

    public long getTimestampMilliseconds() {
        return timestampMilliseconds;
    }

    public void setTimestampMilliseconds(long timestampMilliseconds) {
        this.timestampMilliseconds = timestampMilliseconds;
    }

    public boolean equals(Object o) {
        if (!(o instanceof Steps)) return false;
        Steps other = (Steps) o;
        return (Long.valueOf(this.timestampMilliseconds).equals(other.timestampMilliseconds));
    }
}
