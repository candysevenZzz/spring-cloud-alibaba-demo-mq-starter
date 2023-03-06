package com.cloud.starter.mq.consumer;

import com.alibaba.fastjson.JSON;
import com.cloud.starter.mq.consumer.handler.BaseConsumerHandler;
import com.cloud.starter.mq.properties.CloudConsumerProperties;
import com.cloud.starter.mq.properties.MqProperties;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.common.consumer.ConsumeFromWhere;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.common.protocol.heartbeat.MessageModel;
import org.springframework.beans.BeansException;

import java.util.Map;
import java.util.UUID;

/**
 * @author candy_seven
 * @date 2023/3/1 23:04
 */
@Slf4j
@Getter
public class CloudConsumer {

    private final DefaultMQPushConsumer consumer;

    public CloudConsumer(MqProperties properties, CloudConsumerProperties cloudConsumerProperties, Map<String, BaseConsumerHandler<?>> consumerHandlers) {

        consumer = new DefaultMQPushConsumer(cloudConsumerProperties.getGroupId());

        consumer.setNamesrvAddr(properties.getNamesrv());

        consumer.setConsumeThreadMin(cloudConsumerProperties.getConsumerConsumeThreadMin());

        consumer.setConsumeThreadMax(cloudConsumerProperties.getConsumerConsumeThreadMax());

        /**
         * 设置Consumer第一次启动是从队列头部开始消费还是队列尾部开始消费 如果非第一次启动，那么按照上次消费的位置继续消费
         */
        consumer.setConsumeFromWhere(ConsumeFromWhere.CONSUME_FROM_LAST_OFFSET);
        /**
         * 设置消费模型，集群还是广播，默认为集群
         */
        consumer.setMessageModel(null != cloudConsumerProperties.getMessageModel() ? cloudConsumerProperties.getMessageModel() : MessageModel.CLUSTERING);

        /**
         * 设置一次消费消息的条数，默认为1条
         */
        consumer.setConsumeMessageBatchMaxSize(cloudConsumerProperties.getConsumerConsumeMessageBatchMaxSize());

        /**
         * 配置了多个consumer，但没有指定instanceName。确保instanceName唯一
         */
        consumer.setInstanceName(Thread.currentThread().getName() + UUID.randomUUID());

        /**
         * 注册消息监听器
         */
        consumer.registerMessageListener((MessageListenerConcurrently) (msgs, context) -> {
            try {
                MessageExt msg = msgs.get(0);
                BaseConsumerHandler<?> handler = consumerHandlers.get(cloudConsumerProperties.getHandlerClass());

                return handler.consumeAction(msg, context);
            } catch (BeansException beansException) {
                log.error("Consumer: Bean获取异常 [{}] !", beansException.getMessage());
                beansException.printStackTrace();
            } catch (Exception exception) {
                log.error("Consumer: 消息消费获取异常 [{}] !", exception.getMessage());
                exception.printStackTrace();
            }

            return ConsumeConcurrentlyStatus.RECONSUME_LATER;
        });

        try {
            consumer.subscribe(cloudConsumerProperties.getTopic(), cloudConsumerProperties.getTag());
            consumer.start();
            log.info("Consumer: Topics [{}] 启动成功!", JSON.toJSONString(cloudConsumerProperties.getTopic()));
        } catch (MQClientException e) {
            log.error("Consumer: Topics [{}] 启动失败! Exception: [{}] !", JSON.toJSONString(cloudConsumerProperties.getTopic()), e.getMessage());
        }
    }
}
