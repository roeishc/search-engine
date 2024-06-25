package com.handson.com.search_engine.crawler;

import com.handson.com.search_engine.model.CrawlStatus;
import com.handson.com.search_engine.model.CrawlerRecord;
import com.handson.com.search_engine.model.CrawlerRequest;
import com.handson.com.search_engine.model.StopReason;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jsoup.Jsoup;
import org.jsoup.UnsupportedMimeTypeException;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.stream.Collectors;

@Service
public class Crawler {

    protected final Log logger = LogFactory.getLog(getClass());

    public static final int MAX_CAPACITY = 100000;

    private Set<String> visitedUrls = new HashSet<>();

    private BlockingQueue<CrawlerRecord> queue = new ArrayBlockingQueue<>(MAX_CAPACITY);

    private int curDistance = 0;

    private long startTime = 0;

    private StopReason stopReason;

    private int skippedUrls = 0;


    public CrawlStatus crawl(String crawlId, CrawlerRequest crawlerRequest) throws InterruptedException, IOException {

        visitedUrls.clear();
        queue.clear();
        curDistance = 0;
        startTime = System.currentTimeMillis();
        stopReason = null;
        skippedUrls = 0;

        queue.put(CrawlerRecord.of(crawlId, crawlerRequest));

        Document webPageContent = null;
        while (!queue.isEmpty() && getStopReason(queue.peek()) == null) {
            CrawlerRecord rec = queue.poll();
            logger.info("crawling url:" + rec.getUrl());
            try {
                webPageContent = Jsoup.connect(rec.getUrl()).get();
            }
            catch (IOException e){
                logger.info("skipping url: " + rec.getUrl());
                webPageContent = null;
                skippedUrls++;
            }
            if (webPageContent != null) {
                List<String> innerUrls = extractWebPageUrls(rec.getBaseUrl(), webPageContent);
                addUrlsToQueue(rec, innerUrls, rec.getDistance() + 1);
            }
        }

        stopReason = queue.isEmpty() ? null : getStopReason(queue.peek());
        return CrawlStatus.of(curDistance, startTime, stopReason, visitedUrls.size(), skippedUrls);

    }

    private StopReason getStopReason(CrawlerRecord rec) {
        if (rec.getDistance() == rec.getMaxDistance() +1)
            return StopReason.maxDistance;
        if (visitedUrls.size() >= rec.getMaxUrls())
            return StopReason.maxUrls;
        if (System.currentTimeMillis() >= rec.getMaxTime())
            return StopReason.timeout;
        return null;
    }

    private void addUrlsToQueue(CrawlerRecord rec, List<String> urls, int distance) throws InterruptedException {
        logger.info(">> adding urls to queue: distance->" + distance + " amount->" + urls.size());
        curDistance = distance;
        for (String url : urls) {
            if (!visitedUrls.contains(url)) {
                visitedUrls.add(url);
                queue.put(CrawlerRecord.of(rec).withUrl(url).withIncDistance()) ;
            }
        }
    }

    private List<String> extractWebPageUrls(String baseUrl, Document webPageContent) {
        List<String> links = webPageContent.select("a[href]")
                .eachAttr("abs:href")
                .stream()
                .filter(url -> url.startsWith(baseUrl))
                .collect(Collectors.toList());
        logger.info(">> extracted->" + links.size() + " links from " + baseUrl);
        return links;
    }

}