package com.study.applicationcontext.service.impl;

import com.study.applicationcontext.ConfigurationTestClass;
import com.study.applicationcontext.entity.BeanDefinition;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

public class XMLBeanDefinitionReaderTest {
    @Test
    public void testReadBeanDefinitions() {
        //prepare
        String[] path = {"src/test/resources/test-context1.xml", "src/test/resources/test-context2.xml"};
        XMLBeanDefinitionReader xmlBeanDefinitionReader = new XMLBeanDefinitionReader(path);

        List<BeanDefinition> expected = ConfigurationTestClass.configureTestBeanDefinitions();

        //when
        List<BeanDefinition> actual = xmlBeanDefinitionReader.readBeanDefinitions();

        //then
        assertEquals(expected, actual);
    }

    @Test(expected = RuntimeException.class)
    public void testReadBeanDefinitionsWithIncorrectXml() {
        //prepare
        String[] path = {"src/test/resources/test-bad-context.xml"};
        XMLBeanDefinitionReader xmlBeanDefinitionReader = new XMLBeanDefinitionReader(path);

        //when
        List<BeanDefinition> actual = xmlBeanDefinitionReader.readBeanDefinitions();
    }
}