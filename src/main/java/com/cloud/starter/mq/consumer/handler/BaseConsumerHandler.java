package com.cloud.starter.mq.consumer.handler;

import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.common.message.MessageExt;
import org.slf4j.MDC;

import java.lang.reflect.ParameterizedType;

/**
 * @author candy_seven
 * @date 2023/3/1 20:12
 */
@Slf4j
public abstract class BaseConsumerHandler<T> {

    private Class<T> clazz;

    public BaseConsumerHandler() {
        this.clazz = (Class) ((ParameterizedType) this.getClass().getGenericSuperclass()).getActualTypeArguments()[0];
    }

    /**
     * 信息消费
     *
     * @param message
     * @param consumeContext
     */
    public ConsumeConcurrentlyStatus consumeAction(MessageExt message, ConsumeConcurrentlyContext consumeContext) {
        String json = JSON.parse(message.getBody()).toString();
        log.info("Consumer: 获取消息数据 Group:[{}] Topic:[{}] Tags:[{}] MsgId:[{}] !",
                message.getUserProperty("group-id"), message.getTopic(), message.getTags(), message.getMsgId());

        String traceId = message.getUserProperty("x1-trace-id");
        if (StringUtils.isNotBlank(traceId)) {
            MDC.put("x1-trace-id", traceId);
        }

        T t = JSON.parseObject(json.getBytes(), this.clazz);
        ConsumeConcurrentlyStatus status = this.consume(t, consumeContext);

        if (status.equals(ConsumeConcurrentlyStatus.CONSUME_SUCCESS)) {
            log.info("Consumer: Message [{}] 消费成功!", json);
        } else {
            log.info("Consumer: Message [{}] 消费失败!", json);
        }


        return status;
    }

    protected abstract ConsumeConcurrentlyStatus consume(T t, ConsumeConcurrentlyContext consumeContext);
}
