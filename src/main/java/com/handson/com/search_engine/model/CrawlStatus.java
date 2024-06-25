package com.handson.com.search_engine.model;

public class CrawlStatus {

    private int distance;

    private long startTime;

    private StopReason stopReason;

    private long lastModified;

    private long numPages = 0;

    private int skippedUrls = 0;


    public static CrawlStatus of(int distance, long startTime, StopReason stopReason, int numPages, int skippedUrls) {
        CrawlStatus res = new CrawlStatus();
        res.distance = distance;
        res.startTime =  startTime;
        res.stopReason = stopReason;
        res.lastModified = System.currentTimeMillis();
        res.numPages = numPages;
        res.skippedUrls = skippedUrls;
        return res;
    }

    public int getDistance() {
        return distance;
    }

    public long getLastModified() {
        return lastModified;
    }

    public long getStartTime() {
        return startTime;
    }

    public StopReason getStopReason() {
        return stopReason;
    }

    public long getNumPages() {
        return numPages;
    }

    public void setNumPages(long numPages) {
        this.numPages = numPages;
    }

    public int getSkippedUrls() {
        return skippedUrls;
    }

    public void setSkippedUrls(int skippedUrls) {
        this.skippedUrls = skippedUrls;
    }

    @Override
    public String toString() {
        return "CrawlStatus{" +
                "distance=" + distance +
                ", startTime=" + startTime +
                ", stopReason=" + stopReason +
                ", lastModified=" + lastModified +
                ", numPages=" + numPages +
                ", skippedUrls=" + skippedUrls +
                '}';
    }
}