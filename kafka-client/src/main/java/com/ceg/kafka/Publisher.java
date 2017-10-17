package com.ceg.kafka;



import com.google.common.io.Resources;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;


/**
 * Created by Sean on 10/17/2017.
 * The main header for the java class.
 */
public class Publisher {

    public static void main(String[] args) throws IOException {
        System.out.println("Running Publisher...");
        // set up the producer
        String topic = "";
        String topicSummary = "";
        KafkaProducer<String, String> producer;
        try (InputStream props = Resources.getResource("publisher.properties").openStream()) {
            Properties properties = new Properties();
            properties.load(props);
            topic = properties.getProperty("publisher.topic");
            topicSummary = properties.getProperty("publisher.summary.topic");
            producer = new KafkaProducer<>(properties);
        }

        try {
            // send 1,000,000 million messages.

            for (int i = 0; i < 1000000; i++) {
                // send lots of messages
                producer.send(new ProducerRecord<String, String>(
                        topic,
                        String.format("{\"type\":\"test\", \"t\":%.3f, \"k\":%d}", System.nanoTime() * 1e-9, i)));

                // every so often send to a different topic
                if (i % 1000 == 0) {
                    producer.send(new ProducerRecord<String, String>(
                            topic,
                            String.format("{\"type\":\"marker\", \"t\":%.3f, \"k\":%d}", System.nanoTime() * 1e-9, i)));
                    producer.send(new ProducerRecord<String, String>(
                            topicSummary,
                            String.format("{\"type\":\"other\", \"t\":%.3f, \"k\":%d}", System.nanoTime() * 1e-9, i)));
                    producer.flush();
                    System.out.println("Sent msg number " + i);
                }
            }
        } catch (Throwable throwable) {
            System.out.printf("%s", throwable.getStackTrace());
        } finally {
            producer.close();
        }

    }
}
