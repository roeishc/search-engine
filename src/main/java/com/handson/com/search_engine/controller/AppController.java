package com.handson.com.search_engine.controller;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.handson.com.search_engine.crawler.Crawler;
import com.handson.com.search_engine.kafka.Producer;
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
    private Crawler crawler;

    @Autowired
    private Producer producer;


    @RequestMapping(value = "/crawl", method = RequestMethod.POST)
    public String crawl(@RequestBody CrawlerRequest request) throws IOException, InterruptedException {
        String crawlId = generateCrawlId();
        if (!request.getUrl().startsWith("http")) {
            request.setUrl("https://" + request.getUrl());
        }
        new Thread(()-> {
            try {
                crawler.crawl(crawlId, request);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();

        return crawlId;
    }

    @RequestMapping(value = "/crawl/{crawlId}", method = RequestMethod.GET)
    public CrawlStatusOut getCrawl(@PathVariable String crawlId) throws IOException {
        return crawler.getCrawlInfo(crawlId);
    }

    @PostMapping(value = "/sendKafka")
    public String sendKafka(@RequestBody CrawlerRequest request) throws JsonProcessingException {
        producer.send(request);
        return "OK";
    }

    private String generateCrawlId() {
        String charPool = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder res = new StringBuilder();
        for (int i = 0; i < ID_LENGTH; i++)
            res.append(charPool.charAt(random.nextInt(charPool.length())));
        return res.toString();
    }

}
