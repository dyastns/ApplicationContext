package com.study.applicationcontext.impl;

import com.study.applicationcontext.service.BeanPostProcessor;

import java.lang.reflect.Field;

public class InjectTrueBeanPostProcessor implements BeanPostProcessor {
    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) {
        Class clazz = bean.getClass();

        for (Field field : clazz.getDeclaredFields()) {
            Class fieldClass = field.getType();
            if (fieldClass == boolean.class || fieldClass == Boolean.class) {
                field.setAccessible(true);
                try {
                    field.set(bean, true);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
                field.setAccessible(false);
            }
        }

        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) {
        Class clazz = bean.getClass();

        for (Field field : clazz.getDeclaredFields()) {
            Class fieldClass = field.getType();
            if (fieldClass == int.class || fieldClass == Integer.class) {
                field.setAccessible(true);
                try {
                    field.set(bean, 1);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
                field.setAccessible(false);
            }
        }

        return bean;
    }
}
