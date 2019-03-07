package com.study.applicationcontext.impl;

import com.study.applicationcontext.ApplicationContext;
import com.study.applicationcontext.entity.Bean;
import com.study.applicationcontext.entity.BeanDefinition;
import com.study.applicationcontext.exception.BeanInstantiationException;
import com.study.applicationcontext.service.BeanDefinitionReader;
import com.study.applicationcontext.service.BeanFactoryPostProcessor;
import com.study.applicationcontext.service.BeanPostProcessor;
import com.study.applicationcontext.service.impl.XMLBeanDefinitionReader;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ClassPathApplicationContext implements ApplicationContext {
    private BeanDefinitionReader beanDefinitionReader;
    private Map<String, Bean> beanMap = new HashMap<>();
    private List<Object> beanPostProcessors = new ArrayList<>();
    private List<BeanDefinition> beanDefinitions;

    public ClassPathApplicationContext(String[] path) {
        beanDefinitionReader = new XMLBeanDefinitionReader(path);
        beanDefinitions = beanDefinitionReader.readBeanDefinitions();

        invokeBeanFactoryPostProcess();

        createBeansFromBeanDefinitions();
        injectDependencies();
        injectRefDependencies();

        invokeBeanPostProcess("postProcessBeforeInitialization");
        invokeBeanPostProcess("postProcessAfterInitialization");
    }

    public ClassPathApplicationContext(String path) {
        this(new String[]{path});
    }

    public <T> T getBean(Class<T> clazz) {
        int count = 0;
        Object beanValue = null;

        for (Bean bean : beanMap.values()) {
            Object value = bean.getValue();
            Class<?> valueClass = value.getClass();

            if (valueClass == clazz || isImplements(valueClass, clazz)) {
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

    //check if valueClass implements interface clazz
    private boolean isImplements(Class<?> valueClass, Class<?> clazz) {
        if (!clazz.isInterface()) {
            return false;
        }

        for (Class<?> currentInterface : valueClass.getInterfaces()) {
            if (currentInterface == clazz) {
                return true;
            }
        }

        return false;
    }

    private void invokeBeanFactoryPostProcess() {
        try {
            for (BeanDefinition beanDefinition : beanDefinitions) {
                String className = beanDefinition.getBeanClassName();
                Class<?> clazz = Class.forName(className);

                if (isImplements(clazz, BeanFactoryPostProcessor.class)) {
                    Method postProcess = clazz.getMethod("postProcessBeanFactory", new Class[]{List.class});
                    postProcess.invoke(clazz.newInstance() , beanDefinitions);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    void createBeansFromBeanDefinitions() {
        String className = "";
        try {
            for (BeanDefinition beanDefinition : beanDefinitions) {
                className = beanDefinition.getBeanClassName();
                String beanId = beanDefinition.getId();

                if (beanMap.containsKey(beanId)) {
                    throw new BeanInstantiationException("Duplicated bean name: " + beanId);
                }

                Class<?> clazz = Class.forName(className);
                Object value = clazz.newInstance();
                Bean bean = new Bean(beanId, value);
                beanMap.put(beanId, bean);

                if (isImplements(clazz, BeanPostProcessor.class)) {
                    beanPostProcessors.add(value);
                }
            }
        } catch (Exception e) {
            throw new BeanInstantiationException("Exception while creating new instance of the class: " + className, e);
        }
    }


    void injectDependencies() {
        String beanId = "";
        try {
            for (BeanDefinition beanDefinition : beanDefinitions) {
                beanId = beanDefinition.getId();
                Bean bean = beanMap.get(beanId);
                if (bean != null) {
                    Object value = bean.getValue();
                    Class clazz = value.getClass();
                    Map<String, String> dependencies = beanDefinition.getDependencies();

                    if (dependencies != null) {
                        for (String fieldName : dependencies.keySet()) {
                            Field field = clazz.getDeclaredField(fieldName);
                            Method setter = clazz.getMethod(getSetterMethodName(fieldName), field.getType());
                            injectTypedValue(value, setter, dependencies.get(fieldName), field.getType());
                        }
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
                Bean bean = beanMap.get(beanId);
                if (bean != null) {
                    Object value = bean.getValue();
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
            }
        } catch (Exception e) {
            throw new BeanInstantiationException("Exception while setting reference fields for bean: " + beanId, e);
        }
    }

    private void invokeBeanPostProcess(String methodName) {
        for (Object postProcessor : beanPostProcessors) {
            Class clazz = postProcessor.getClass();
            Class<?>[] parameterTypes = {Object.class, String.class};
            try {
                Method postProcess = clazz.getMethod(methodName, parameterTypes);
                for (String beanName : beanMap.keySet()) {
                    Object[] arguments = {beanMap.get(beanName).getValue(), beanName};
                    Object newBeanValue = postProcess.invoke(postProcessor, arguments);
                    beanMap.put(beanName, new Bean(beanName, newBeanValue));
                }
            } catch (Exception e) {
                throw new RuntimeException("Exception while trying to invoke BeanPostProcessor " + postProcessor + " method " + methodName, e);
            }
        }
    }

    private String getSetterMethodName(String fieldName) {
        StringBuilder name = new StringBuilder("set");
        name.append(fieldName.substring(0, 1).toUpperCase());
        name.append(fieldName.substring(1));

        return name.toString();
    }

    private void injectTypedValue(Object object, Method setter, String value, Class clazz) {
        try {
            //Primitives and String
            if (clazz == boolean.class || clazz == Boolean.class) {
                setter.invoke(object, Boolean.valueOf(value));
            } else if (clazz == char.class || clazz == Character.class) {
                setter.invoke(object, Character.valueOf(value.charAt(0)));
            } else if (clazz == byte.class || clazz == Byte.class) {
                setter.invoke(object, Byte.valueOf(value));
            } else if (clazz == short.class || clazz == Short.class) {
                setter.invoke(object, Short.valueOf(value));
            } else if (clazz == int.class || clazz == Integer.class) {
                setter.invoke(object, Integer.valueOf(value));
            } else if (clazz == long.class || clazz == Long.class) {
                setter.invoke(object, Long.valueOf(value));
            } else if (clazz == float.class || clazz == Float.class) {
                setter.invoke(object, Float.valueOf(value));
            } else if (clazz == double.class || clazz == Double.class) {
                setter.invoke(object, Double.valueOf(value));
            } else if (clazz == String.class) {
                setter.invoke(object, value);
            } else {
                throw new BeanInstantiationException("Can't convert String value: '" + value + "' to class: " + clazz);
            }
        } catch (Exception e) {
            throw new BeanInstantiationException("Can't convert String value: '" + value + "' to class: " + clazz, e);
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
}
