package com.tmzpanda.kafka.consumer;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.errors.WakeupException;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.Arrays;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;

public class MultiThreadedConsumer {

    public static void main(String[] args) {
        new MultiThreadedConsumer().run();
    }


    private void run() {
        Logger logger = LoggerFactory.getLogger(MultiThreadedConsumer.class.getName());

        String bootstrapServers = "127.0.0.1:9092";
        String topic = "first_topic";
        String groupId = "second-application";

        // Latch for dealing with multiple threads
        CountDownLatch latch = new CountDownLatch(1);

        // create the runnable consumer
        logger.info("Creating the consumer thread");
        ConfiguredConsumer consumer = new ConfiguredConsumer(bootstrapServers, groupId, topic, latch);

        // start the thread
        Thread thread = new Thread(consumer);
        thread.start();

        // add a shutdown hook
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            logger.info("Caught shutdown hook");
            consumer.shutdown();
            try {
                latch.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            logger.info("Application has exited");
        })
        );

        try {
            latch.await();
        } catch (InterruptedException e) {
            logger.error("Application got interrupted", e);
        } finally {
            logger.info("Application is closing");
        }

    }


    public static class ConfiguredConsumer implements Runnable {

        // fields
        private CountDownLatch latch;
        private KafkaConsumer<String, String> consumer;
        private Logger logger = LoggerFactory.getLogger(ConfiguredConsumer.class.getName());

        // Constructor - create consumer configs, create consumer, subscribe consumer to our topic(s)
        public ConfiguredConsumer(String bootstrapServers,
                                  String topic,
                                  String groupId,
                                  CountDownLatch latch) {
            this.latch = latch;

            // create consumer configs
            Properties properties = new Properties();
            properties.setProperty(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
            properties.setProperty(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
            properties.setProperty(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
            properties.setProperty(ConsumerConfig.GROUP_ID_CONFIG, groupId);
            properties.setProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

            // create consumer
            consumer = new KafkaConsumer<String, String>(properties);
            // subscribe consumer to our topic(s)
            consumer.subscribe(Arrays.asList(topic));
        }

        // run and shutdown methods
        @Override
        public void run() {
            // poll for new data
            try {
                while (true) {
                    ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(100));

                    for (ConsumerRecord<String, String> record : records) {
                        logger.info("Key: " + record.key() + ", Value: " + record.value());
                        logger.info("Partition: " + record.partition() + ", Offset:" + record.offset());
                    }
                }
            } catch (WakeupException e) {
                logger.info("Received shutdown signal!");
            } finally {
                consumer.close();
                // tell our main code we're done with the consumer
                latch.countDown();
            }
        }

        public void shutdown() {
            // the wakeup() method is a special method to interrupt consumer.poll()
            // it will throw the exception WakeUpException
            consumer.wakeup();

        }

    }
}

