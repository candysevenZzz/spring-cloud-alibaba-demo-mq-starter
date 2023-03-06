package com.cloud.starter.mq.selector;

import com.cloud.starter.mq.annotation.EnableCloudMq;
import com.cloud.starter.mq.consumer.ConsumerListenerFactory;
import com.cloud.starter.mq.producer.CloudProducer;
import com.cloud.starter.mq.properties.MqProperties;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.ImportSelector;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.type.AnnotationMetadata;

import java.util.List;
import java.util.Objects;

/**
 * @author candy_seven
 * @date 2023/3/1 16:50
 */
@Slf4j
public class CloudMqConfigurationSelector implements ImportSelector {

    private static final String ENABLE_PRODUCER = "producer";

    private static final String ENABLE_CONSUMER = "consumer";

    @Override
    public String[] selectImports(AnnotationMetadata annotationMetadata) {
        AnnotationAttributes attributes = AnnotationAttributes.fromMap(
                annotationMetadata.getAnnotationAttributes(EnableCloudMq.class.getName()));
        if (Objects.isNull(attributes)) {
            return new String[]{};
        }

        List<String> classes = Lists.newArrayList();

        classes.add(MqProperties.class.getName());

        if (attributes.getBoolean(ENABLE_CONSUMER)) {
            classes.add(ConsumerListenerFactory.class.getName());
        }

        if (attributes.getBoolean(ENABLE_PRODUCER)) {
            classes.add(CloudProducer.class.getName());
        }

        return classes.toArray(new String[]{});
    }
}
