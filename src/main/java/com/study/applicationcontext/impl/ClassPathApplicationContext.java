package com.study.applicationcontext.impl;

import com.study.applicationcontext.ApplicationContext;
import com.study.applicationcontext.entity.Bean;
import com.study.applicationcontext.entity.BeanDefinition;
import com.study.applicationcontext.exception.BeanInstantiationException;
import com.study.applicationcontext.service.BeanDefinitionReader;
import com.study.applicationcontext.service.impl.XMLBeanDefinitionReader;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ClassPathApplicationContext implements ApplicationContext {
    private String[] path;
    private BeanDefinitionReader beanDefinitionReader;
    private Map<String, Bean> beanMap;
    private List<BeanDefinition> beanDefinitions;

    public ClassPathApplicationContext(String[] path) {
        this.path = path;

        beanDefinitionReader = new XMLBeanDefinitionReader(path);
        beanDefinitions = beanDefinitionReader.readBeanDefinitions();
        createBeansFromBeanDefinitions();
        injectDependencies();
        injectRefDependencies();
    }

    public ClassPathApplicationContext(String path) {
        this(new String[]{path});
    }

    public <T> T getBean(Class<T> clazz) {
        int count = 0;
        Object beanValue = null;

        for (Bean bean : beanMap.values()) {
            Object value = bean.getValue();
            if (value.getClass() == clazz) {
                count++;
                if (count > 1) {
                    throw new RuntimeException("More than one bean exists for the class: " + clazz);
                }
                beanValue = value;
            }
        }

        if (count == 1) {
            return (T) beanValue;
        } else {
            throw new RuntimeException("There's no bean for the class: " + clazz);
        }
    }

    public <T> T getBean(String name, Class<T> clazz) {
        return (T) getBean(name);
    }

    public Object getBean(String name) {
        if (beanMap.containsKey(name)) {
            Bean bean = beanMap.get(name);
            return bean.getValue();
        } else {
            throw new RuntimeException("There's no bean for the name: " + name);
        }
    }

    public List<String> getBeanNames() {
        return new ArrayList<>(beanMap.keySet());
    }

    public void setBeanDefinitionReader(BeanDefinitionReader beanDefinitionReader) {
        this.beanDefinitionReader = beanDefinitionReader;
    }

    void createBeansFromBeanDefinitions() {
        String className = "";
        try {
            Map<String, Bean> beanMap = new HashMap<>();

            for (BeanDefinition beanDefinition : beanDefinitions) {
                className = beanDefinition.getBeanClassName();
                String beanId = beanDefinition.getId();

                if (beanMap.containsKey(beanId)) {
                    throw new BeanInstantiationException("Duplicated bean name: " + beanId);
                }

                Object value = Class.forName(className).newInstance();
                Bean bean = new Bean(beanId, value);
                beanMap.put(beanId, bean);
            }

            setBeanMap(beanMap);
        } catch (Exception e) {
            throw new BeanInstantiationException("Exception while creating new instance of the class: " + className, e);
        }
    }

    void injectDependencies() {
        String beanId = "";
        try {
            for (BeanDefinition beanDefinition : beanDefinitions) {
                beanId = beanDefinition.getId();
                Object value = beanMap.get(beanId).getValue();
                Class clazz = value.getClass();
                Map<String, String> dependencies = beanDefinition.getDependencies();

                if (dependencies != null) {
                    for (String fieldName : dependencies.keySet()) {
                        Field field = clazz.getDeclaredField(fieldName);
                        Method setter = clazz.getMethod(getSetterMethodName(fieldName), field.getType());
                        Object argument = getTypedValueFromString(dependencies.get(fieldName), field.getType());
                        setter.invoke(value, argument);
                    }
                }
            }
        } catch (Exception e) {
            throw new BeanInstantiationException("Exception while setting field values for bean: " + beanId, e);
        }
    }

    void injectRefDependencies() {
        String beanId = "";
        try {
            for (BeanDefinition beanDefinition : beanDefinitions) {
                beanId = beanDefinition.getId();
                Object value = beanMap.get(beanId).getValue();
                Class clazz = value.getClass();
                Map<String, String> refDependencies = beanDefinition.getRefDependencies();

                if (refDependencies != null) {
                    for (String fieldName : refDependencies.keySet()) {
                        Field field = clazz.getDeclaredField(fieldName);
                        Method setter = clazz.getMethod(getSetterMethodName(fieldName), field.getType());
                        Object argument = beanMap.get(refDependencies.get(fieldName)).getValue();
                        setter.invoke(value, argument);
                    }
                }
            }
        } catch (Exception e) {
            throw new BeanInstantiationException("Exception while setting reference fields for bean: " + beanId, e);
        }
    }

    void setBeanMap(Map<String, Bean> beanMap) {
        this.beanMap = beanMap;
    }

    Map<String, Bean> getBeanMap() {
        return beanMap;
    }

    void setBeanDefinitions(List<BeanDefinition> beanDefinitions) {
        this.beanDefinitions = beanDefinitions;
    }

    private String getSetterMethodName(String fieldName) {
        StringBuilder name = new StringBuilder("set");
        name.append(fieldName.substring(0,1).toUpperCase());
        name.append(fieldName.substring(1));

        return name.toString();
    }

    private <T> T getTypedValueFromString(String value, Class<T> clazz) {
        try {
            //Primitives and String
            if (clazz == boolean.class || clazz == Boolean.class) {
                return (T) Boolean.valueOf(value);
            } else if (clazz == char.class || clazz == Character.class) {
                return (T) Character.valueOf(value.charAt(0));
            } else if (clazz == byte.class || clazz == Byte.class) {
                return (T) Byte.valueOf(value);
            } else if (clazz == short.class || clazz == Short.class) {
                return (T) Short.valueOf(value);
            } else if (clazz == int.class || clazz == Integer.class) {
                return (T) Integer.valueOf(value);
            } else if (clazz == long.class || clazz == Long.class) {
                return (T) Long.valueOf(value);
            } else if (clazz == float.class || clazz == Float.class) {
                return (T) Float.valueOf(value);
            } else if (clazz == double.class || clazz == Double.class) {
                return (T) Double.valueOf(value);
            } else if (clazz == String.class) {
                return (T) value;
            } else {
                throw new BeanInstantiationException("Can't convert String value: '" + value + "' to class: " + clazz);
            }
        } catch (Exception e) {
            throw new BeanInstantiationException("Can't convert String value: '" + value + "' to class: " + clazz, e);
        }
    }
}
