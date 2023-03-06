package com.cloud.starter.mq.properties;

import lombok.Data;
import org.apache.rocketmq.common.protocol.heartbeat.MessageModel;

/**
 * @author candy_seven
 * @date 2023/3/1 17:48
 */
@Data
public class CloudConsumerProperties {

    private String groupId;

    private String topic;

    private String tag;

    private String handlerClass;

    private MessageModel messageModel;

    private int consumerConsumeThreadMin = 5;

    private int consumerConsumeThreadMax = 30;

    private int consumerConsumeMessageBatchMaxSize = 1;
}
