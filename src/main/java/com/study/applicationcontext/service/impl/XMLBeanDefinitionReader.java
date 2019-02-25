package com.study.applicationcontext.service.impl;

import com.study.applicationcontext.entity.BeanDefinition;
import com.study.applicationcontext.service.BeanDefinitionReader;
import org.w3c.dom.*;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class XMLBeanDefinitionReader implements BeanDefinitionReader {
    private static final SchemaFactory SCHEMA_FACTORY = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
    private static final String PATH_TO_XSD = "/context.xsd";
    private String[] path;
    private Validator validator;

    public XMLBeanDefinitionReader(String[] path) {
        this.path = path;

        Schema schema;
        try (InputStream stream = this.getClass().getResourceAsStream(PATH_TO_XSD);) {
            schema = SCHEMA_FACTORY.newSchema(new StreamSource(stream));
        } catch (SAXException e) {
            throw new RuntimeException("XSD file for context validation: " + PATH_TO_XSD + " is not valid", e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        validator = schema.newValidator();
    }

    public List<BeanDefinition> readBeanDefinitions() {
        List<BeanDefinition> beanDefinitions = new ArrayList<>();

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        try {
            DocumentBuilder builder = factory.newDocumentBuilder();

            for (int i = 0; i < path.length; i++) {
                String fileName = path[i].startsWith("/") ? path[i] : "/" + path[i];
                File file = new File(this.getClass().getResource(fileName).getFile());
                validateXMLByXSD(file);
                Document document = builder.parse(file);
                beanDefinitions.addAll(collectDocumentBeanDefinitions(document));
            }
        } catch (Exception e) {
            throw new RuntimeException("Exception during bean definition read", e);
        }

        return beanDefinitions;
    }

    private void validateXMLByXSD(File xmlFile) {
        try {
            validator.validate(new StreamSource(xmlFile));
        } catch (SAXException e) {
            throw new RuntimeException("XML file: " + xmlFile + " is not valid", e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private List<BeanDefinition> collectDocumentBeanDefinitions(Document document) {
        List<BeanDefinition> beanDefinitions = new ArrayList<>();

        NodeList beanElements = document.getDocumentElement().getElementsByTagName("bean");

        for (int i = 0; i < beanElements.getLength(); i++) {
            Element bean = (Element) beanElements.item(i);

            NamedNodeMap attributes = bean.getAttributes();

            BeanDefinition beanDefinition = new BeanDefinition();
            beanDefinition.setId(attributes.getNamedItem("id").getNodeValue());
            beanDefinition.setBeanClassName(attributes.getNamedItem("class").getNodeValue());

            NodeList propertyElements = bean.getElementsByTagName("property");
            Map<String, String> dependencies = new HashMap<>();
            Map<String, String> refDependencies = new HashMap<>();

            for (int j = 0; j < propertyElements.getLength(); j++) {
                Node property = propertyElements.item(j);
                NamedNodeMap propertyAttr = property.getAttributes();
                String name = propertyAttr.getNamedItem("name").getNodeValue();
                Node valueNode = propertyAttr.getNamedItem("value");
                Node refNode = propertyAttr.getNamedItem("ref");

                if (valueNode != null) {
                    dependencies.put(name, valueNode.getNodeValue());
                } else if (refNode != null) {
                    refDependencies.put(name, refNode.getNodeValue());
                }
            }

            beanDefinition.setDependencies(dependencies);
            beanDefinition.setRefDependencies(refDependencies);

            beanDefinitions.add(beanDefinition);
        }

        return beanDefinitions;
    }

    public String[] getPath() {
        return path;
    }
}
