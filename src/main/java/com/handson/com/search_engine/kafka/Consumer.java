package com.handson.com.search_engine.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.handson.com.search_engine.crawler.Crawler;
import com.handson.com.search_engine.model.CrawlerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Optional;

import static com.handson.com.search_engine.kafka.Producer.APP_TOPIC;

@Component
public class Consumer {

    @Autowired
    Crawler crawler;

    @Autowired
    ObjectMapper om;

    @KafkaListener(topics = {APP_TOPIC})
    public void listen(ConsumerRecord<?, ?> record) throws IOException, InterruptedException {
        Optional<?> kafkaMessage = Optional.ofNullable(record.value());
        if (kafkaMessage.isPresent()) {
            Object message = kafkaMessage.get();
            System.out.println("record  ---->" + record);
            System.out.println("message ---->" + message);
//            CrawlerRecord rec = om.readValue(message.toString(), CrawlerRecord.class);
//            crawler.crawlOneUrl(rec);
        }
    }
}