package com.study.applicationcontext.impl;

import com.study.applicationcontext.ConfigurationTestClass;
import com.study.applicationcontext.entity.Bean;
import com.study.applicationcontext.entity.BeanDefinition;
import com.study.applicationcontext.exception.BeanInstantiationException;
import org.junit.Before;
import org.junit.Test;

import java.util.*;

import static org.junit.Assert.*;

public class ClassPathApplicationContextTest {
    private ClassPathApplicationContext classPathApplicationContext;
    private ClassPathApplicationContext classPathApplicationContextInternal;
    private Object beanValue1;
    private Object beanValue2;
    private Object beanValue3;
    private Object beanValue4;

    @Before
    public void before() {
        //for testing public methods:
        Map<String, Bean> beanMap = new HashMap<>();
        beanValue1 = "string_bean1";
        beanMap.put("id1_string", new Bean("id1_string", beanValue1));
        beanValue2 = new Date();
        beanMap.put("id2_date", new Bean("id2_date", beanValue2));
        beanValue3 = new Date(1542931200);
        beanMap.put("id3_date", new Bean("id3_date", beanValue3));
        beanValue4 = new ArrayList();
        beanMap.put("id4_list", new Bean("id4_list", beanValue4));

        classPathApplicationContext = new ClassPathApplicationContext("/test-empty-context.xml");
        classPathApplicationContext.setBeanMap(beanMap);

        //for testing private methods:
        classPathApplicationContextInternal = new ClassPathApplicationContext("/test-empty-context.xml");
        List<BeanDefinition> beanDefinitions = ConfigurationTestClass.configureTestBeanDefinitions();
        classPathApplicationContextInternal.setBeanDefinitions(beanDefinitions);
    }

    @Test
    public void testClassPathApplicationContext() {
        //prepare
        String[] path = {"/test-context1.xml", "/test-context2.xml"};

        Map<String, Bean> expectedBeanMap = new HashMap<>();
        expectedBeanMap.put("testClass1", new Bean("testClass1", new TestClass1()));
        expectedBeanMap.put("testClass21", new Bean("testClass21", new TestClass2()));
        expectedBeanMap.put("testClass22", new Bean("testClass22", new TestClass2()));
        expectedBeanMap.put("testClass3", new Bean("testClass3", new TestClass3()));

        //when
        ClassPathApplicationContext applicationContext = new ClassPathApplicationContext(path);

        //then
        Map<String, Bean> actualBeanMap = applicationContext.getBeanMap();
        assertEquals(expectedBeanMap.size(), actualBeanMap.size());
        for (String expectedId : expectedBeanMap.keySet()){
            assertTrue(actualBeanMap.containsKey(expectedId));
            Bean expectedBean = expectedBeanMap.get(expectedId);
            Bean actualBean = actualBeanMap.get(expectedId);
            assertEquals(expectedBean.getId(), actualBean.getId());
            assertEquals(expectedBean.getValue().getClass(), actualBean.getValue().getClass());
        }

        //check dependencies
        Bean bean1 = actualBeanMap.get("testClass1");
        TestClass1 value1 = (TestClass1) bean1.getValue();
        assertEquals("field11_testValue", value1.getField11());
        assertEquals(123, value1.getField12());
        assertEquals(456L, value1.getField13());
        assertEquals(7888.9, value1.getField14(), 0.001);
        assertEquals(true, value1.getField15());

        Bean bean21 = actualBeanMap.get("testClass21");
        TestClass2 value21 = (TestClass2) bean21.getValue();
        assertEquals("field_testValue", value21.getField21());
        assertEquals(123, value21.getField22());

        Bean bean22 = actualBeanMap.get("testClass22");
        TestClass2 value22 = (TestClass2) bean22.getValue();
        assertEquals("field_testValue_other", value22.getField21());
        assertEquals(55555, value22.getField22());

        //check refDependencies
        Bean bean3 = actualBeanMap.get("testClass3");
        TestClass3 value3 = (TestClass3) bean3.getValue();
        assertEquals(value1, value3.getRefField31());

        assertEquals(value1, value21.getRefField21());
        assertEquals(value3, value21.getRefField22());

        assertEquals(value1, value22.getRefField21());
        assertEquals(value3, value22.getRefField22());
    }

    @Test
    public void testGetBeanByClass(){
        //prepare
        Class<String> stringClass = String.class;

        //when
        Object actual = classPathApplicationContext.getBean(stringClass);

        //then
        assertEquals(stringClass, actual.getClass());
        assertSame(beanValue1, actual);
    }

    @Test
    public void testGetBeanByInterfaceClass() {
        //prepare
        Class<List> interfaceClass = List.class;
        Class<ArrayList> expectedClass = ArrayList.class;

        //when
        Object actual = classPathApplicationContext.getBean(interfaceClass);

        //then
        assertEquals(expectedClass, actual.getClass());
        assertSame(beanValue4, actual);
    }

    @Test(expected = RuntimeException.class)
    public void testGetBeanByDuplicatedClass(){
        //prepare
        Class<Date> dateClass = Date.class;

        //when
        Object actual = classPathApplicationContext.getBean(dateClass);
    }

    @Test(expected = RuntimeException.class)
    public void testGetBeanByDuplicatedInterfaceClass(){
        //prepare
        Class<Cloneable> cloneableClass = Cloneable.class;

        //when
        Object actual = classPathApplicationContext.getBean(cloneableClass);
    }

    @Test
    public void testGetBeanByNameAndClass() {
        //prepare
        Class<Date> dateClass = Date.class;

        //when
        Object actual = classPathApplicationContext.getBean("id3_date", dateClass);

        //then
        assertEquals(dateClass, actual.getClass());
        assertSame(beanValue3, actual);
    }

    @Test
    public void testGetBeanByName() {
        //when
        Object actual = classPathApplicationContext.getBean("id2_date");

        //then
        assertSame(beanValue2, actual);
    }

    @Test
    public void testGetBeanNames() {
        //prepare
        List<String> expected = new ArrayList<String>();
        expected.add("id1_string");
        expected.add("id2_date");
        expected.add("id3_date");
        expected.add("id4_list");

        //when
        List<String> actual = classPathApplicationContext.getBeanNames();

        //then
        assertEquals(expected.size(), actual.size());

        Set<String> expectedSet = new HashSet<>();
        expectedSet.addAll(expected);
        Set<String> actualSet = new HashSet<>();
        actualSet.addAll(actual);
        assertEquals(expectedSet, actualSet);
    }

    @Test
    public void testInjectDependencies() {
        //prepare
        Map<String, Bean> beanMap = new HashMap<>();
        beanMap.put("testClass1", new Bean("testClass1", new TestClass1()));
        beanMap.put("testClass21", new Bean("testClass21", new TestClass2()));
        beanMap.put("testClass22", new Bean("testClass22", new TestClass2()));
        beanMap.put("testClass3", new Bean("testClass3", new TestClass3()));

        classPathApplicationContextInternal.setBeanMap(beanMap);

        //when
        classPathApplicationContextInternal.injectDependencies();

        //then
        Map<String, Bean> actualBeanMap = classPathApplicationContextInternal.getBeanMap();

        Bean bean1 = actualBeanMap.get("testClass1");
        TestClass1 value1 = (TestClass1) bean1.getValue();
        assertEquals("field11_testValue", value1.getField11());
        assertEquals(123, value1.getField12());
        assertEquals(456L, value1.getField13());
        assertEquals(7888.9, value1.getField14(), 0.001);
        assertEquals(true, value1.getField15());

        Bean bean21 = actualBeanMap.get("testClass21");
        TestClass2 value21 = (TestClass2) bean21.getValue();
        assertEquals("field_testValue", value21.getField21());
        assertEquals(123, value21.getField22());

        Bean bean22 = actualBeanMap.get("testClass22");
        TestClass2 value22 = (TestClass2) bean22.getValue();
        assertEquals("field_testValue_other", value22.getField21());
        assertEquals(55555, value22.getField22());
    }

    @Test(expected = BeanInstantiationException.class)
    public void testInjectDependenciesException() {
        //prepare
        BeanDefinition defBadSetterClass = new BeanDefinition();
        defBadSetterClass.setId("testBadSetterClass");
        defBadSetterClass.setBeanClassName("com.study.applicationcontext.impl.TestBadSetterClass");
        Map<String, String> dependencies = new HashMap<String, String>();
        dependencies.put("field1","field_testValue");
        defBadSetterClass.setDependencies(dependencies);

        List<BeanDefinition> beanDefinitions = new ArrayList<BeanDefinition>();
        beanDefinitions.add(defBadSetterClass);
        classPathApplicationContextInternal.setBeanDefinitions(beanDefinitions);

        Map<String, Bean> beanMap = new HashMap<>();
        beanMap.put("testBadSetterClass", new Bean("testBadSetterClass", new TestBadSetterClass()));
        classPathApplicationContextInternal.setBeanMap(beanMap);

        //when
        classPathApplicationContextInternal.injectDependencies();
    }

    @Test
    public void testInjectRefDependencies() {
        //prepare
        Map<String, Bean> beanMap = new HashMap<>();
        beanMap.put("testClass1", new Bean("testClass1", new TestClass1()));
        beanMap.put("testClass21", new Bean("testClass21", new TestClass2()));
        beanMap.put("testClass22", new Bean("testClass22", new TestClass2()));
        beanMap.put("testClass3", new Bean("testClass3", new TestClass3()));

        classPathApplicationContextInternal.setBeanMap(beanMap);

        //when
        classPathApplicationContextInternal.injectRefDependencies();

        //then
        Map<String, Bean> actualBeanMap = classPathApplicationContextInternal.getBeanMap();

        Bean bean1 = actualBeanMap.get("testClass1");
        TestClass1 value1 = (TestClass1) bean1.getValue();

        Bean bean3 = actualBeanMap.get("testClass3");
        TestClass3 value3 = (TestClass3) bean3.getValue();
        assertEquals(value1, value3.getRefField31());

        Bean bean21 = actualBeanMap.get("testClass21");
        TestClass2 value21 = (TestClass2) bean21.getValue();
        assertEquals(value1, value21.getRefField21());
        assertEquals(value3, value21.getRefField22());

        Bean bean22 = actualBeanMap.get("testClass22");
        TestClass2 value22 = (TestClass2) bean22.getValue();
        assertEquals(value1, value22.getRefField21());
        assertEquals(value3, value22.getRefField22());
    }

    @Test(expected = BeanInstantiationException.class)
    public void testInjectRefDependenciesException() {
        //prepare
        BeanDefinition defBadSetterClass = new BeanDefinition();
        defBadSetterClass.setId("testBadSetterClass");
        defBadSetterClass.setBeanClassName("com.study.applicationcontext.impl.TestBadSetterClass");
        Map<String, String> refDependencies = new HashMap<String, String>();
        refDependencies.put("refField1","testClass1");
        defBadSetterClass.setRefDependencies(refDependencies);

        List<BeanDefinition> beanDefinitions = new ArrayList<BeanDefinition>();
        beanDefinitions.add(defBadSetterClass);
        classPathApplicationContextInternal.setBeanDefinitions(beanDefinitions);

        Map<String, Bean> beanMap = new HashMap<>();
        beanMap.put("testBadSetterClass", new Bean("testBadSetterClass", new TestBadSetterClass()));
        classPathApplicationContextInternal.setBeanMap(beanMap);

        //when
        classPathApplicationContextInternal.injectRefDependencies();
    }

    @Test
    public void testCreateBeansFromBeanDefinitions(){
        //prepare
        Map<String, Bean> expectedBeanMap = new HashMap<>();
        expectedBeanMap.put("testClass1", new Bean("testClass1", new TestClass1()));
        expectedBeanMap.put("testClass21", new Bean("testClass21", new TestClass2()));
        expectedBeanMap.put("testClass22", new Bean("testClass22", new TestClass2()));
        expectedBeanMap.put("testClass3", new Bean("testClass3", new TestClass3()));
        //when
        classPathApplicationContextInternal.createBeansFromBeanDefinitions();

        //then
        Map<String, Bean> actualBeanMap = classPathApplicationContextInternal.getBeanMap();
        assertEquals(expectedBeanMap.size(), actualBeanMap.size());
        for (String expectedId : expectedBeanMap.keySet()){
            assertTrue(actualBeanMap.containsKey(expectedId));
            Bean expectedBean = expectedBeanMap.get(expectedId);
            Bean actualBean = actualBeanMap.get(expectedId);
            assertEquals(expectedBean.getId(), actualBean.getId());
            assertEquals(expectedBean.getValue(), actualBean.getValue());
        }
    }

    @Test(expected = BeanInstantiationException.class)
    public void testCreateBeansFromBeanDefinitionsException(){
        //prepare
        BeanDefinition defBadConstructorClass = new BeanDefinition();
        defBadConstructorClass.setId("testBadConstructorClass");
        defBadConstructorClass.setBeanClassName("com.study.applicationcontext.impl.TestBadConstructorClass");
        Map<String, String> dependencies = new HashMap<String, String>();
        dependencies.put("field1","field_testValue");
        dependencies.put("field2","123");
        defBadConstructorClass.setDependencies(dependencies);

        List<BeanDefinition> beanDefinitions = new ArrayList<BeanDefinition>();
        beanDefinitions.add(defBadConstructorClass);
        classPathApplicationContextInternal.setBeanDefinitions(beanDefinitions);

        //when
        classPathApplicationContextInternal.createBeansFromBeanDefinitions();
    }

    @Test(expected = BeanInstantiationException.class)
    public void testCreateBeansFromBeanDefinitionsWithDuplicate() {
        //prepare
        BeanDefinition beanDefinition = new BeanDefinition();
        beanDefinition.setId("testClass1");
        beanDefinition.setBeanClassName("com.study.applicationcontext.impl.TestClass1");

        BeanDefinition beanDefinitionDuplicate = new BeanDefinition();
        beanDefinitionDuplicate.setId("testClass1");
        beanDefinitionDuplicate.setBeanClassName("com.study.applicationcontext.impl.TestClass2");

        List<BeanDefinition> beanDefinitions = new ArrayList<BeanDefinition>();
        beanDefinitions.add(beanDefinition);
        beanDefinitions.add(beanDefinitionDuplicate);

        classPathApplicationContextInternal.setBeanDefinitions(beanDefinitions);

        //when
        classPathApplicationContextInternal.createBeansFromBeanDefinitions();
    }

    @Test
    public void testBeanPostProcessorPostProcesses() {
        //prepare
        String[] path = {"/test-bpp-context.xml"};

        Map<String, Bean> expectedBeanMap = new HashMap<>();
        expectedBeanMap.put("testClass4", new Bean("testClass4", new TestClass4()));
        expectedBeanMap.put("injectTrueBeanPostProcessor", new Bean("injectTrueBeanPostProcessor", new InjectTrueBeanPostProcessor()));

        //when
        ClassPathApplicationContext applicationContext = new ClassPathApplicationContext(path);

        //then
        Map<String, Bean> actualBeanMap = applicationContext.getBeanMap();
        assertEquals(expectedBeanMap.size(), actualBeanMap.size());
        Bean expectedBean = expectedBeanMap.get("testClass4");
        Bean actualBean = actualBeanMap.get("testClass4");
        assertEquals(expectedBean.getId(), actualBean.getId());
        assertEquals(expectedBean.getValue().getClass(), actualBean.getValue().getClass());

        TestClass4 value4 = (TestClass4) actualBean.getValue();
        assertTrue(value4.getField41());
        assertTrue(value4.getField42());
        assertEquals(1, value4.getField43());
        assertEquals(1, value4.getField44());
    }

    @Test
    public void testBeanFactoryPostProcessorPostProcess() {
        //prepare
        String[] path = {"/test-bfpp-context.xml"};

        Map<String, Bean> expectedBeanMap = new HashMap<>();
        expectedBeanMap.put("testClass4", new Bean("testClass4", new TestClass4()));
        expectedBeanMap.put("testBeanFactoryPostProcessor", new Bean("testBeanFactoryPostProcessor", new TestBeanFactoryPostProcessor()));

        //when
        ClassPathApplicationContext applicationContext = new ClassPathApplicationContext(path);

        //then
        Map<String, Bean> actualBeanMap = applicationContext.getBeanMap();
        assertEquals(expectedBeanMap.size(), actualBeanMap.size());
        Bean expectedBean = expectedBeanMap.get("testClass4");
        Bean actualBean = actualBeanMap.get("testClass4");
        assertEquals(expectedBean.getId(), actualBean.getId());
        assertEquals(expectedBean.getValue().getClass(), actualBean.getValue().getClass());

        TestClass4 value4 = (TestClass4) actualBean.getValue();
        assertEquals("field value AFTER BFPP", value4.getField45());
    }
}
