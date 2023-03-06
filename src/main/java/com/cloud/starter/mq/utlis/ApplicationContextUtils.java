package com.cloud.starter.mq.utlis;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * @author candy_seven
 * @date 2023/3/2 17:23
 */
@Component
public class ApplicationContextUtils implements ApplicationContextAware {

    private static ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        ApplicationContextUtils.applicationContext = applicationContext;
    }

    @SuppressWarnings("unchecked")
    public static <T> T getBean(String className) {
        if(ApplicationContextUtils.applicationContext.containsBean(className)){
            return (T) ApplicationContextUtils.applicationContext.getBean(className);
        }

        try {
            ClassLoader classLoader = ApplicationContextUtils.applicationContext.getClassLoader();
            if (classLoader != null) {
                return (T) ApplicationContextUtils.applicationContext.getBean(classLoader.loadClass(className));
            }

            // 2. ClassLoader 不存在(系统类加载器不可见)，尝试用类名称小写加载
            String[] split = className.split("\\.");
            String beanName = split[split.length - 1];
            // 小写转大写
            char[] cs = beanName.toCharArray();
            cs[0] += 32;
            String beanName0 = String.valueOf(cs);
            return (T) ApplicationContextUtils.applicationContext.getBean(beanName0);
        } catch (Exception exception) {
            return null;
        }
    }

    public static <T> Map<String, T> getBeansOfType(Class<T> baseType){
        return applicationContext.getBeansOfType(baseType);
    }
}
