/*
 * Copyright 2018 Broadband Forum
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.broadband_forum.obbaa.aggregator.jaxb.utils;

import org.broadband_forum.obbaa.netconf.api.util.DocumentUtils;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.helpers.XMLFilterImpl;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import java.io.StringReader;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * JAXB utils.
 */
public final class JaxbUtils {

    private JaxbUtils() {
        //Hide
    }

    /**
     * Build marshaller.
     *
     * @param object Schema class object
     * @param <T>    Schema class
     * @return Marshaller
     * @throws JAXBException Exception
     */
    public static <T> Marshaller buildMarshaller(T object) throws JAXBException {
        JAXBContext jaxbContext = JAXBContext.newInstance(object.getClass());

        Marshaller marshaller = jaxbContext.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

        return marshaller;
    }

    /**
     * Marshal to String.
     *
     * @param object Schema object
     * @param <T>    Schema object class
     * @return Message string
     * @throws JAXBException Exception
     */
    public static <T> String marshalToString(T object) throws JAXBException {
        Marshaller marshaller = buildMarshaller(object);
        StringWriter stringWriter = new StringWriter();
        marshaller.marshal(object, buildXMLFilterImpl(stringWriter));

        return stringWriter.toString();
    }

    /**
     * Build one XML writer.
     *
     * @param stringWriter String writer
     * @return XML writer
     */
    private static XMLWriter buildXMLWriter(StringWriter stringWriter) {
        OutputFormat outputFormat = new OutputFormat();
        outputFormat.setIndent(true);
        outputFormat.setNewlines(true);
        outputFormat.setNewLineAfterDeclaration(false);

        return new XMLWriter(stringWriter, outputFormat);
    }

    /**
     * Build XML Filter Implementation.
     *
     * @param stringWriter String writer
     * @return XML Filter object
     */
    private static XMLFilterImpl buildXMLFilterImpl(StringWriter stringWriter) {
        XMLFilterImpl xmlFilter = new NetconfXmlFilterImpl();
        xmlFilter.setContentHandler(buildXMLWriter(stringWriter));

        return xmlFilter;
    }

    /**
     * Marshal to String.
     *
     * @param object Schema object
     * @param <T>    Schema object class
     * @return Message document
     * @throws JAXBException Exception
     */
    public static <T> Document marshalToDocument(T object) throws JAXBException {
        Marshaller marshaller = buildMarshaller(object);

        Document document = DocumentUtils.createDocument();
        marshaller.marshal(object, document);

        return document;
    }

    /**
     * Build Unmarshaller.
     *
     * @param clazz Classes of schema
     * @return Unmarshaller
     * @throws JAXBException Exception
     */
    public static Unmarshaller buildUnmarshaller(Class... clazz) throws JAXBException {
        JAXBContext jaxbContext = JAXBContext.newInstance(clazz);

        return jaxbContext.createUnmarshaller();
    }

    /**
     * Unmarshal message.
     *
     * @param message message
     * @param clazz   Classes
     * @param <T>     Schema class
     * @return Object of schema class
     * @throws JAXBException Exception
     */
    public static <T> T unmarshal(String message, Class... clazz) throws JAXBException {
        Unmarshaller unmarshaller = buildUnmarshaller(clazz);

        return (T) unmarshaller.unmarshal(new StringReader(message));
    }

    /**
     * Unmarshal message.
     *
     * @param message message
     * @param clazz   Classes
     * @param <T>     Schema class
     * @return Object of schema class
     * @throws JAXBException Exception
     */
    public static <T> T unmarshal(Node message, Class... clazz) throws JAXBException {
        Unmarshaller unmarshaller = buildUnmarshaller(clazz);

        return (T) unmarshaller.unmarshal(message);
    }

    /**
     * Judge if the JAXB object contains no value valid.
     *
     * @param object JAXB Class
     * @return if contain null
     */
    public static boolean isObjectContainNull(Object object) {
        Method[] methods = object.getClass().getMethods();
        for (Method method : methods) {
            if (!isGetValueNull(object, method)) {
                return false;
            }
        }

        return true;
    }

    /**
     * Judge if the value from getting is null.
     *
     * @param object Object
     * @param method Method
     * @return If Value is null. If the method is not used for getting, it returns true.
     */
    public static boolean isGetValueNull(Object object, Method method) {
        if ((method.getDeclaringClass().equals(object.getClass())) && (method.getName().startsWith("get"))) {
            try {
                Object value = method.invoke(object, null);
                if (value != null) {
                    return false;
                }

            } catch (InvocationTargetException | IllegalAccessException | IllegalArgumentException ex) {
                //Ignore
            }
        }

        return true;
    }

    /**
     * Element with namespace to documents.
     *
     * @param objects Objects
     * @return Documents
     */
    public static List<Document> objectsToDocument(List<Object> objects) {
        List<Document> documents = new ArrayList<>();

        for (Object object : objects) {
            if (object instanceof Node) {
                Node node = (Node) object;
                Document document = node.getOwnerDocument();
                documents.add(document);
            }
        }

        return documents;
    }

    /**
     * Build one object of payload.
     *
     * @param object Object
     * @param <T>    Class
     * @return Node
     * @throws JAXBException Exception
     */
    public static <T> Node buildPayloadObject(T object) throws JAXBException {
        return JaxbUtils.marshalToDocument(object).getFirstChild();
    }
}