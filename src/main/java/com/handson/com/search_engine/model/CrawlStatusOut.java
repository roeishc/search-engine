package com.handson.com.search_engine.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.handson.com.search_engine.util.Dates;
import org.joda.time.LocalDateTime;

import java.util.Date;

public class CrawlStatusOut {

    private int distance;

    private StopReason stopReason;

    private long numPages = 0;

    private int skippedUrls = 0;

    private long startTime;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonProperty("startTime")
    public LocalDateTime calcStartTime() {
        return Dates.atLocalTime(new Date(startTime));
    }

    private long lastModified;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonProperty("lastModified")
    public LocalDateTime calcLastModified() {
        return Dates.atLocalTime(new Date(lastModified));
    }

    public static CrawlStatusOut of(CrawlStatus in) {
        CrawlStatusOut res = new CrawlStatusOut();
        res.distance = in.getDistance();
        res.startTime =  in.getStartTime();
        res.lastModified = in.getLastModified();
        res.stopReason = in.getStopReason();
        res.numPages = in.getNumPages();
        res.skippedUrls = in.getSkippedUrls();
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

}