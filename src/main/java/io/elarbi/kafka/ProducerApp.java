package io.elarbi.kafka;

/**
 * Produces lots of messages to a Kafka Cluster and prints some metrics
 *
 * @author elarbiaboussoror
 */
public class ProducerApp {
    private static ProducerInfo producerInfo = new ProducerInfo();

    public static void main(String[] args) {
        producerInfo.setNbOfMessagesToPublish(Integer.MAX_VALUE);
    }
}

