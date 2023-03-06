package com.cloud.starter.mq.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author candy_seven
 * @date 2023/3/1 17:03
 */
@Data
@Component
@ConfigurationProperties(prefix = "ons")
public class MqProperties {
    /**
     * 是否开机自启动消费
     */
    private boolean enable;

    private String namesrv;

    private String ak;

    private String sk;

    private CloudProducerProperties producer;

    private List<CloudConsumerProperties> consumer;

}
