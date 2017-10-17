package com.ceg.kafka;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.io.Resources;
import org.HdrHistogram.Histogram;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Properties;
import java.util.Random;

/**
 * Created by Sean on 10/17/2017.
 * The main header for the java class.
 */
public class Consumer {
    public static void main(String[] args) throws IOException {
        // set up house-keeping
        ObjectMapper mapper = new ObjectMapper();
        Histogram stats = new Histogram(1, 10000000, 2);
        Histogram global = new Histogram(1, 10000000, 2);

        String topic = "";
        String topicSummary="";
        // and the consumer

        KafkaConsumer<String, String> consumer;
        try (InputStream props = Resources.getResource("consumer.properties").openStream()) {
            Properties properties = new Properties();
            properties.load(props);

            topic = properties.getProperty("consumer.topic");
            topicSummary = properties.getProperty("consumer.summary.topic");

            if (properties.getProperty("group.id") == null) {
                properties.setProperty("group.id", "group-" + new Random().nextInt(100000));
            }
            consumer = new KafkaConsumer<>(properties);
        }
        consumer.subscribe(Arrays.asList(topic, topicSummary));
        int timeouts = 0;
        //noinspection InfiniteLoopStatement
        while (true) {
            // read records with a short timeout. If we time out, we don't really care.
            ConsumerRecords<String, String> records = consumer.poll(200);
            if (records.count() == 0) {
                timeouts++;
            } else {
                System.out.printf("Got %d records after %d timeouts\n", records.count(), timeouts);
                timeouts = 0;
            }
            for (ConsumerRecord<String, String> record : records) {
                if (record.topic().equals(topic)){
                    // the send time is encoded inside the message
                    JsonNode msg = mapper.readTree(record.value());
                    switch (msg.get("type").asText()) {
                        case "test":
                            long latency = (long) ((System.nanoTime() * 1e-9 - msg.get("t").asDouble()) * 1000);
                            stats.recordValue(latency);
                            global.recordValue(latency);
                            break;
                        case "marker":
                            // whenever we get a marker message, we should dump out the stats
                            // note that the number of fast messages won't necessarily be quite constant
                            System.out.printf("%d messages received in period, latency(min, max, avg, 99%%) = %d, %d, %.1f, %d (ms)\n",
                                    stats.getTotalCount(),
                                    stats.getValueAtPercentile(0), stats.getValueAtPercentile(100),
                                    stats.getMean(), stats.getValueAtPercentile(99));
                            System.out.printf("%d messages received overall, latency(min, max, avg, 99%%) = %d, %d, %.1f, %d (ms)\n",
                                    global.getTotalCount(),
                                    global.getValueAtPercentile(0), global.getValueAtPercentile(100),
                                    global.getMean(), global.getValueAtPercentile(99));

                            stats.reset();
                            break;
                        default:
                            throw new IllegalArgumentException("Illegal message type: " + msg.get("type"));
                    }

                } else if (record.topic().equals(topicSummary)){
                    System.out.println("Handing summary topic message");
                } else {
                    throw new IllegalStateException("Shouldn't be possible to get message on topic " + record.topic());

                }
            }
        }
    }

}
