package com.cloud.starter.mq.consumer;

import com.cloud.starter.mq.consumer.handler.BaseConsumerHandler;
import com.cloud.starter.mq.properties.CloudConsumerProperties;
import com.cloud.starter.mq.properties.MqProperties;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author candy_seven
 * @date 2023/3/1 17:26
 */
@Slf4j
@Component
public class ConsumerListenerFactory {

    private ArrayList<DefaultMQPushConsumer> listeners;

    public ConsumerListenerFactory(MqProperties properties,  List<BaseConsumerHandler<?>> handlers) {
        Assert.notNull(properties.getNamesrv(), "namesrv不能为空!");

        init(properties, handlers);

        Runtime.getRuntime().addShutdownHook(new Thread(this::destroy));
    }

    void init(MqProperties properties,  List< BaseConsumerHandler<?>> handlers) {
        if (!properties.isEnable()) {
            log.info("Consumer: 未开启开机自动消费!");
        }

        List<CloudConsumerProperties> cloudConsumerPropertiesList = properties.getConsumer();
        Assert.notEmpty(cloudConsumerPropertiesList, "Consumer: consumer不能为空!");

        this.listeners = new ArrayList<>(cloudConsumerPropertiesList.size());

        Map<String, BaseConsumerHandler<?>> handlerMap = Maps.uniqueIndex(handlers, (x) -> x.getClass().getName());

        for (CloudConsumerProperties cloudConsumerProperties : cloudConsumerPropertiesList) {
            Assert.notNull(cloudConsumerProperties, "Consumer: 消费者配置信息缺失!");
            Assert.notNull(cloudConsumerProperties.getGroupId(), "Consumer: 消费组缺失!");

            CloudConsumer cloudConsumer = new CloudConsumer(properties, cloudConsumerProperties, handlerMap);
            this.listeners.add(cloudConsumer.getConsumer());
        }
    }

    void destroy() {
        log.info("Consumer: consumer prepare to shutdown!");

        for (DefaultMQPushConsumer consumer : this.listeners) {
            consumer.shutdown();
            log.info("Consumer: consumer [{}] is shutdown!", consumer.getInstanceName());
        }
    }
}
