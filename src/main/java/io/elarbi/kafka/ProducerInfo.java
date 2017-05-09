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
@Log4j
public class ProducerInfo {

    private int nbOfMessagesToPublish;
    private int nbOfMessagesPublished;
}
