package com.cloud.starter.mq.annotation;

import com.cloud.starter.mq.selector.CloudMqConfigurationSelector;
import org.springframework.context.annotation.Import;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author candy_seven
 * @date 2023/3/1 16:49
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
@Import(CloudMqConfigurationSelector.class)
public @interface EnableCloudMq {

    /**
     * 启动生产者
     *
     * @return {@link boolean}
     */
    boolean producer() default true;

    /**
     * 启动消费者
     *
     * @return {@link boolean}
     */
    boolean consumer() default true;
}
