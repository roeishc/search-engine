package com.handson.com.search_engine.crawler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.handson.com.search_engine.kafka.Producer;
import com.handson.com.search_engine.model.*;
import com.handson.com.search_engine.util.ElasticSearch;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class Crawler {

    protected final Log logger = LogFactory.getLog(getClass());

    public static final int MAX_CAPACITY = 100000;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private ObjectMapper om;

    @Autowired
    private Producer producer;

    @Autowired
    private ElasticSearch elasticSearch;


    public void crawl(String crawlId, CrawlerRequest crawlerRequest) throws InterruptedException, IOException {
        initCrawlInRedis(crawlId);
        producer.send(CrawlerRecord.of(crawlId, crawlerRequest));
    }

    public void crawlOneRecord(String crawlId, CrawlerRecord rec) throws JsonProcessingException, InterruptedException {

        logger.info("crawling url:" + rec.getUrl());
        StopReason stopReason = getStopReason(rec);

        if (stopReason == null) {
            Document webPageContent;
            try {
                webPageContent = Jsoup.connect(rec.getUrl()).get();
            } catch (IOException e) {
                logger.info("skipping url: " + rec.getUrl());
                webPageContent = null;
                incSkippedUrls(rec);
            }
            if (webPageContent != null) {
                indexElasticSearch(rec, webPageContent);
                List<String> innerUrls = extractWebPageUrls(rec.getBaseUrl(), webPageContent);
                addUrlsToQueue(rec, innerUrls, rec.getDistance() + 1);
            }
        }

        setCrawlStatus(crawlId, CrawlStatus.of(rec.getDistance(), rec.getStartTime(), stopReason, 0, 0));

    }

    private StopReason getStopReason(CrawlerRecord rec) {
        if (rec.getDistance() == rec.getMaxDistance() +1)
            return StopReason.maxDistance;
        if (getVisitedUrls(rec.getCrawlId()) >= rec.getMaxUrls())
            return StopReason.maxUrls;
        if (System.currentTimeMillis() >= rec.getMaxTime())
            return StopReason.timeout;
        return null;
    }

    private void addUrlsToQueue(CrawlerRecord rec, List<String> urls, int distance) throws JsonProcessingException {
        logger.info(">> adding urls to queue: distance->" + distance + " amount->" + urls.size());
        for (String url : urls) {
            if (!crawlHasVisited(rec, url))
                producer.send(CrawlerRecord.of(rec).withUrl(url).withIncDistance()); ;
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

    private void initCrawlInRedis(String crawlId) throws JsonProcessingException {
        setCrawlStatus(crawlId, CrawlStatus.of(0, System.currentTimeMillis(),null, 0, 0));
        redisTemplate.opsForValue().set(crawlId + ".urls.count", "0");
        redisTemplate.opsForValue().set(crawlId + ".urls.skippedUrls", "0");
    }

    private void setCrawlStatus(String crawlId, CrawlStatus crawlStatus) throws JsonProcessingException {
        redisTemplate.opsForValue().set(crawlId + ".status", om.writeValueAsString(crawlStatus));
    }

    private boolean crawlHasVisited(CrawlerRecord rec, String url) {
        if (redisTemplate.opsForValue().setIfAbsent(rec.getCrawlId() + ".urls." + url, "1")) { // false if already exists
            redisTemplate.opsForValue().increment(rec.getCrawlId() + ".urls.count", 1L);
            return false;
        }
        else
            return true;
    }

    private void incSkippedUrls(CrawlerRecord rec){
//        redisTemplate.opsForValue().setIfAbsent(rec.getCrawlId() + ".urls." + rec.getUrl(), "1");
        crawlHasVisited(rec, rec.getUrl());
        redisTemplate.opsForValue().increment(rec.getCrawlId() + ".urls.skippedUrls", 1L);
    }

    private long getVisitedUrls(String crawlId) {
        Object curCount = redisTemplate.opsForValue().get(crawlId + ".urls.count");
        if (curCount == null)
            return 0L;
        return Long.parseLong(curCount.toString());
    }

    private long getSkippedUrls(String crawlId){
        Object count = redisTemplate.opsForValue().get(crawlId + ".urls.skippedUrls");
        if (count == null)
            return 0L;
        return Long.parseLong(count.toString());
    }

    public CrawlStatusOut getCrawlInfo(String crawlId) throws JsonProcessingException {
        CrawlStatus cs = om.readValue(redisTemplate.opsForValue().get(crawlId + ".status").toString(), CrawlStatus.class);
        cs.setNumPages(getVisitedUrls(crawlId));
        cs.setSkippedUrls(getSkippedUrls(crawlId));
        return CrawlStatusOut.of(cs);
    }

    public void indexElasticSearch(CrawlerRecord rec, Document webPageContent) {
        logger.info(">> adding elastic-search entry for web page: " + rec.getUrl());
        String text = String.join(" ", webPageContent.select("a[href]").eachText());
        UrlSearchDoc searchDoc = UrlSearchDoc.of(rec.getCrawlId(), text, rec.getUrl(), rec.getBaseUrl(), rec.getDistance());
        elasticSearch.addData(searchDoc);
    }

}