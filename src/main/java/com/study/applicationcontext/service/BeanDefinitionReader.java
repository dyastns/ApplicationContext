package com.study.applicationcontext.service;

import com.study.applicationcontext.entity.BeanDefinition;

import java.util.List;

public interface BeanDefinitionReader {
    List<BeanDefinition> readBeanDefinitions();
}
