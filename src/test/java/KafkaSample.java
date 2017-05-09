import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.ClassRule;
import org.junit.Test;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.listener.KafkaMessageListenerContainer;
import org.springframework.kafka.listener.MessageListener;
import org.springframework.kafka.listener.config.ContainerProperties;
import org.springframework.kafka.support.SendResult;
import org.springframework.kafka.test.rule.KafkaEmbedded;
import org.springframework.kafka.test.utils.ContainerTestUtils;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.util.concurrent.ListenableFutureCallback;

import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.springframework.kafka.test.hamcrest.KafkaMatchers.hasKey;
import static org.springframework.kafka.test.hamcrest.KafkaMatchers.hasPartition;
import static org.springframework.kafka.test.hamcrest.KafkaMatchers.hasValue;


/** Testing Spring KafkaTemplate with and {@link KafkaEmbedded}
 * Created by elarbiaboussoror on 30/01/2017.
 */
public class KafkaSample {

    private static final String TEMPLATE_TOPIC = "templateTopic";

    @ClassRule
    public static KafkaEmbedded embeddedKafka = new KafkaEmbedded(1, true, TEMPLATE_TOPIC);


    @Test
    public void testTemplate() throws Exception {

        Map<String, Object> consumerProps = KafkaTestUtils.consumerProps("testT", "false", embeddedKafka);

        DefaultKafkaConsumerFactory<Integer, String> cf =
                new DefaultKafkaConsumerFactory<Integer, String>(consumerProps);

        ContainerProperties containerProperties = new ContainerProperties(TEMPLATE_TOPIC);

        KafkaMessageListenerContainer<Integer, String> messageListenerContainer = new KafkaMessageListenerContainer<>(cf, containerProperties);

        final BlockingQueue<ConsumerRecord<Integer, String>> records = new LinkedBlockingQueue<>();

        messageListenerContainer.setupMessageListener((MessageListener<Integer, String>) record -> {
            System.out.println(record);
            records.add(record);
        });
//        messageListenerContainer.setupMessageListener((MessageListener<Integer, String>) record -> {
//            System.out.println("message has been received" + record);
//        });
        messageListenerContainer.setBeanName("templateTests");
        messageListenerContainer.start();

        ContainerTestUtils.waitForAssignment(messageListenerContainer, embeddedKafka.getPartitionsPerTopic());

        Map<String, Object> producerProps = KafkaTestUtils.senderProps(embeddedKafka.getBrokersAsString());

        ProducerFactory<Integer, String> pf =
                new DefaultKafkaProducerFactory<Integer, String>(producerProps);

        KafkaTemplate<Integer, String> template = new KafkaTemplate<>(pf);

        template.setDefaultTopic(TEMPLATE_TOPIC);

        template.sendDefault("foo");

        assertThat(records.poll(10, TimeUnit.SECONDS), hasValue("foo"));

        //send to the default topic
        template.sendDefault(0, 2, "bar").addCallback(new ListenableFutureCallback<SendResult<Integer, String>>() {
            @Override
            public void onFailure(Throwable ex) {
                System.err.println("Failure !");
            }

            @Override
            public void onSuccess(SendResult<Integer, String> result) {
                System.err.println("Success !");

            }
        });
        ConsumerRecord<Integer, String> received = records.poll(10, TimeUnit.SECONDS);
        assertThat(received, hasKey(2));
        assertThat(received, hasPartition(0));
        assertThat(received, hasValue("bar"));

        template.send(
                TEMPLATE_TOPIC,
                0,
                2,
                "baz");

        received = records.poll(10, TimeUnit.SECONDS);
        assertThat(received, hasKey(2));
        assertThat(received, hasPartition(0));
        assertThat(received, hasValue("baz"));
    }


}


