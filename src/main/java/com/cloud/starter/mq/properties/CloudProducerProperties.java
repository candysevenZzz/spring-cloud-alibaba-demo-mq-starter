package com.cloud.starter.mq.properties;

import lombok.Data;

/**
 * @author candy_seven
 * @date 2023/3/1 17:48
 */
@Data
public class CloudProducerProperties {

    private String groupId;

    private String topic;

    private String tag;

    private int producerMaxMessageSize = 1024;

    private int producerSendMsgTimeout = 2000;

    private int producerRetryTimesWhenSendFailed = 2;
}
