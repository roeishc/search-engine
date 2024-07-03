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
import static com.handson.com.search_engine.kafka.Producer.TEST_TOPIC;


@Component
public class Consumer {

    @Autowired
    private ObjectMapper om;

    @Autowired
    private Crawler crawler;


    @KafkaListener(topics = {APP_TOPIC})
    public void listen(ConsumerRecord<?, ?> record) throws IOException, InterruptedException {
        Optional<?> kafkaMessage = Optional.ofNullable(record.value());
        if (kafkaMessage.isPresent()) {
            Object message = kafkaMessage.get();
            CrawlerRecord rec = om.readValue(message.toString(), CrawlerRecord.class);
            crawler.crawlOneRecord(rec.getCrawlId(), rec);
        }
    }

    @KafkaListener(topics = {TEST_TOPIC})
    public void test(ConsumerRecord<?, ?> record){
        Optional<?> kafkaMessage = Optional.ofNullable(record.value());
        kafkaMessage.ifPresent(System.out::println);
    }

}