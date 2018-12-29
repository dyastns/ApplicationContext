package com.study.applicationcontext;

import com.study.applicationcontext.entity.BeanDefinition;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ConfigurationTestClass {
    public static List<BeanDefinition> configureTestBeanDefinitions() {
        Map<String, String> dependencies;
        Map<String, String> refDependencies;

        BeanDefinition defClass1 = new BeanDefinition();
        defClass1.setId("testClass1");
        defClass1.setBeanClassName("com.study.applicationcontext.impl.TestClass1");
        dependencies = new HashMap<String, String>();
        dependencies.put("field11","field11_testValue");
        dependencies.put("field12","123");
        dependencies.put("field13","456");
        dependencies.put("field14","7888.9");
        dependencies.put("field15","true");
        defClass1.setDependencies(dependencies);
        refDependencies = new HashMap<String, String>();
        defClass1.setRefDependencies(refDependencies);

        BeanDefinition defClass21 = new BeanDefinition();
        defClass21.setId("testClass21");
        defClass21.setBeanClassName("com.study.applicationcontext.impl.TestClass2");
        dependencies = new HashMap<String, String>();
        dependencies.put("field21","field_testValue");
        dependencies.put("field22","123");
        defClass21.setDependencies(dependencies);
        refDependencies = new HashMap<String, String>();
        refDependencies.put("refField21", "testClass1");
        refDependencies.put("refField22", "testClass3");
        defClass21.setRefDependencies(refDependencies);

        BeanDefinition defClass22 = new BeanDefinition();
        defClass22.setId("testClass22");
        defClass22.setBeanClassName("com.study.applicationcontext.impl.TestClass2");
        dependencies = new HashMap<String, String>();
        dependencies.put("field21","field_testValue_other");
        dependencies.put("field22","55555");
        defClass22.setDependencies(dependencies);
        refDependencies = new HashMap<String, String>();
        refDependencies.put("refField21", "testClass1");
        refDependencies.put("refField22", "testClass3");
        defClass22.setRefDependencies(refDependencies);

        BeanDefinition defClass3 = new BeanDefinition();
        defClass3.setId("testClass3");
        defClass3.setBeanClassName("com.study.applicationcontext.impl.TestClass3");
        dependencies = new HashMap<String, String>();
        defClass3.setDependencies(dependencies);
        refDependencies = new HashMap<String, String>();
        refDependencies.put("refField31", "testClass1");
        defClass3.setRefDependencies(refDependencies);


        List<BeanDefinition> beanDefinitions = new ArrayList<BeanDefinition>();
        beanDefinitions.add(defClass1);
        beanDefinitions.add(defClass21);
        beanDefinitions.add(defClass22);
        beanDefinitions.add(defClass3);

        return beanDefinitions;
    }
}
