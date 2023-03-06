package com.cloud.starter.mq.consumer.handler;

import com.cloud.starter.mq.dao.entity.DemoEntity;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.springframework.stereotype.Component;

/**
 * @author candy_seven
 * @date 2023/3/2 14:25
 */
@Slf4j
@Component
public class Demo2Handler extends BaseConsumerHandler<DemoEntity>{
    @Override
    protected ConsumeConcurrentlyStatus consume(DemoEntity demoEntity, ConsumeConcurrentlyContext consumeContext) {
        log.info("Demo2Handler 消费成功!");

        return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
    }
}
