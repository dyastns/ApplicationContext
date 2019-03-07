package com.study.applicationcontext.impl;

import com.study.applicationcontext.entity.BeanDefinition;
import com.study.applicationcontext.service.BeanFactoryPostProcessor;

import java.util.List;
import java.util.Map;

public class TestBeanFactoryPostProcessor implements BeanFactoryPostProcessor {
    @Override
    public void postProcessBeanFactory(List<BeanDefinition> beanDefinitions) {
        for (BeanDefinition beanDefinition : beanDefinitions) {
            String className = beanDefinition.getBeanClassName();

            if ("com.study.applicationcontext.impl.TestClass4".equals(className)) {
                Map<String, String> dependencies = beanDefinition.getDependencies();
                dependencies.put("field45", "field value AFTER BFPP");
            }
        }
    }
}
