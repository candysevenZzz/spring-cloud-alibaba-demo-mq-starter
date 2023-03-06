package com.cloud.starter.mq.producer;

import com.alibaba.fastjson.JSON;
import com.cloud.starter.mq.properties.CloudProducerProperties;
import com.cloud.starter.mq.properties.MqProperties;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.common.message.Message;
import org.slf4j.MDC;
import org.springframework.util.Assert;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

/**
 * @author candy_seven
 * @date 2023/3/1 17:24
 */
@Slf4j
public class CloudProducer {

    private DefaultMQProducer producer;

    public CloudProducer(MqProperties properties) throws MQClientException {
        Assert.notNull(properties.getNamesrv(), "namesrv不能为空");

        producer = new DefaultMQProducer(properties.getProducer().getGroupId());

        producer.setNamesrvAddr(properties.getNamesrv());

        // 如果需要同一个jvm中不同的producer往不同的mq集群发送消息，需要设置不同的instanceName
        producer.setInstanceName(Thread.currentThread().getName() + UUID.randomUUID());

        producer.setMaxMessageSize(properties.getProducer().getProducerMaxMessageSize());

        producer.setSendMsgTimeout(properties.getProducer().getProducerSendMsgTimeout());

        // 如果发送消息失败，设置重试次数，默认为2次
        producer.setRetryTimesWhenSendFailed(properties.getProducer().getProducerRetryTimesWhenSendFailed());

        try {
            producer.start();
            log.info("Producer is start! groupName:[{}], namesrv:[{}]!", properties.getProducer().getGroupId(), properties.getNamesrv());
        } catch (MQClientException e) {
            log.error("Producer start error! Error Message:[{}]!", e.getMessage());
            e.printStackTrace();
            throw e;
        }

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            this.destroy(producer);
        }));
    }

    public SendResult send(String topic, String tag, Object obj, int level) throws Exception {
        String key = getUniqueKey();
        return this.send(topic, tag, "", key, obj, level);
    }

    public SendResult send(String topic, String tag, Object obj) throws Exception {
        String key = getUniqueKey();
        return this.send(topic, tag, "", key, obj, 0);
    }

    public SendResult send(String topic, String tag, String group, String key, Object obj) throws Exception {
        return this.send(topic, tag, group, key, obj, 0);
    }

    public SendResult send(CloudProducerProperties properties, Object obj) throws Exception {
        String key = getUniqueKey();
        return this.send(properties.getTopic(), properties.getTag(), properties.getGroupId(), key, obj, 0);
    }

    /**
     * @param topic
     * @param tag
     * @param key
     * @param obj
     * @param level {@link int} messageDelayLevel=1s 5s 10s 30s 1m 2m 3m 4m 5m 6m 7m 8m 9m 10m 20m 30m 1h 2h
     * @return {@link SendResult}
     */
    public SendResult send(String topic, String tag, String group, String key, Object obj, int level) throws Exception {
        String json = JSON.parse(JSON.toJSONString(obj)).toString();
        try {
            String traceId = getUniqueKey();

            Message message = new Message(topic, tag, key, json.getBytes(StandardCharsets.UTF_8));

            message.putUserProperty("x1-trace-id", traceId);
            if (StringUtils.isNotBlank(group)) {
                message.putUserProperty("group-id", group);
            }

            if (level > 0) {
                message.setDelayTimeLevel(level);
            }

            SendResult res = this.producer.send(message);
            log.info("Producer: 发送消息成功! topic:[{}] tag:[{}] obj:[{}]", topic, tag, json);
            return res;
        } catch (Exception exception) {
            log.error("Producer: 发送消息异常! topic:[{}] tag:[{}] obj:[{}] e:[{}]", topic, tag, json, ExceptionUtils.getStackTrace(exception));
            throw exception;
        }
    }

    private String getUniqueKey() {
        String traceId = MDC.get("x1-trace-id");
        traceId = StringUtils.isNotBlank(traceId) ? traceId : UUID.randomUUID().toString();
        return traceId;
    }

    private void destroy(DefaultMQProducer producer) {
        log.info("Producer: producer is shutdown!");
        producer.shutdown();
    }
}
