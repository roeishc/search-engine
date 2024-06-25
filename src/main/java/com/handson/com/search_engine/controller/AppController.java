package com.handson.com.search_engine.controller;


import com.handson.com.search_engine.crawler.Crawler;
import com.handson.com.search_engine.model.CrawlStatus;
import com.handson.com.search_engine.model.CrawlStatusOut;
import com.handson.com.search_engine.model.CrawlerRequest;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Random;

@RestController
@RequestMapping("/api")
public class AppController {

    protected final Log logger = LogFactory.getLog(getClass());

    private static final int ID_LENGTH = 8;

    private final Random random = new Random();

    @Autowired
    Crawler crawler;

    @RequestMapping(value = "/crawl", method = RequestMethod.POST)
    public CrawlStatusOut crawl(@RequestBody CrawlerRequest request) throws IOException, InterruptedException {
        String crawlId = generateCrawlId();
        if (!request.getUrl().startsWith("http"))
            request.setUrl("https://" + request.getUrl());
        CrawlStatus res = crawler.crawl(crawlId, request);
        logger.info(res);
        return CrawlStatusOut.of(res);
    }

    private String generateCrawlId() {
        String charPool = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder res = new StringBuilder();
        for (int i = 0; i < ID_LENGTH; i++)
            res.append(charPool.charAt(random.nextInt(charPool.length())));
        return res.toString();
    }

}
