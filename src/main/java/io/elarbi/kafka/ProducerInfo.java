package io.elarbi.kafka;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.extern.log4j.Log4j;

/**
 * @author elarbiaboussoror
 */
@Getter
@Setter
public class ProducerInfo {

    private int nbOfMessagesToPublish;
    private int nbOfMessagesPublished;
}
