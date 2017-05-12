package io.elarbi.kafka;

import lombok.Getter;
import lombok.Setter;
import org.springframework.kafka.listener.MessageListener;

import java.util.UUID;

/**
 * Created by elarbiaboussoror on 18/04/2017.
 */
@Getter
@Setter
public class Message {
    String id;
    String payload;

    public static Message createBigMessage() {
        Message message = new Message();
        message.setId(UUID.randomUUID().toString());
        String uuid = UUID.randomUUID().toString();
        StringBuilder stringBuilder = new StringBuilder("Payload_");
        for (int i = 0; i < 20; i++)
            stringBuilder.append(uuid);
        message.setPayload(stringBuilder.toString());
        return message;
    }

    public static void main(String[] args) {
        String s = UUID.randomUUID().toString();
        int length = s.length();
    }
}
