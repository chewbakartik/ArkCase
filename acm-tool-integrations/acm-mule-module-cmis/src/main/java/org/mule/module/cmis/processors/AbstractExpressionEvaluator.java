
package org.mule.module.cmis.processors;

/*-
 * #%L
 * ACM Mule CMIS Connector
 * %%
 * Copyright (C) 2014 - 2018 ArkCase LLC
 * %%
 * This file is part of the ArkCase software. 
 * 
 * If the software was purchased under a paid ArkCase license, the terms of 
 * the paid license agreement will prevail.  Otherwise, the software is 
 * provided under the following open source license terms:
 * 
 * ArkCase is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *  
 * ArkCase is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with ArkCase. If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

import org.mule.api.MuleContext;
import org.mule.api.MuleEvent;
import org.mule.api.MuleMessage;
import org.mule.api.expression.ExpressionManager;
import org.mule.api.transformer.DataType;
import org.mule.api.transformer.MessageTransformer;
import org.mule.api.transformer.Transformer;
import org.mule.api.transformer.TransformerException;
import org.mule.api.transformer.TransformerMessagingException;
import org.mule.transformer.types.DataTypeFactory;
import org.mule.util.TemplateParser;

import javax.annotation.Generated;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.WildcardType;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

@Generated(value = "Mule DevKit Version 3.4.0", date = "2014-05-13T04:20:32-03:00", comments = "Build 3.4.0.1555.8df15c1")
public abstract class AbstractExpressionEvaluator
{

    /**
     * Get all superclasses and interfaces recursively.
     * 
     * @param classes
     *            List of classes to which to add all found super classes and interfaces.
     * @param clazz
     *            The class to start the search with.
     */
    protected void computeClassHierarchy(Class clazz, List classes)
    {
        for (Class current = clazz; (current != null); current = current.getSuperclass())
        {
            if (classes.contains(current))
            {
                return;
            }
            classes.add(current);
            for (Class currentInterface : current.getInterfaces())
            {
                computeClassHierarchy(currentInterface, classes);
            }
        }
    }

    /**
     * Checks whether the specified class parameter is an instance of {@link List }
     * 
     * @param clazz
     *            <code>Class</code> to check.
     * @return
     */
    protected boolean isListClass(Class clazz)
    {
        List<Class> classes = new ArrayList<Class>();
        computeClassHierarchy(clazz, classes);
        return classes.contains(List.class);
    }

    /**
     * Checks whether the specified class parameter is an instance of {@link Map }
     * 
     * @param clazz
     *            <code>Class</code> to check.
     * @return
     */
    protected boolean isMapClass(Class clazz)
    {
        List<Class> classes = new ArrayList<Class>();
        computeClassHierarchy(clazz, classes);
        return classes.contains(Map.class);
    }

    protected boolean isList(Type type)
    {
        if ((type instanceof Class) && isListClass(((Class) type)))
        {
            return true;
        }
        if (type instanceof ParameterizedType)
        {
            return isList(((ParameterizedType) type).getRawType());
        }
        if (type instanceof WildcardType)
        {
            Type[] upperBounds = ((WildcardType) type).getUpperBounds();
            return ((upperBounds.length != 0) && isList(upperBounds[0]));
        }
        return false;
    }

    protected boolean isMap(Type type)
    {
        if ((type instanceof Class) && isMapClass(((Class) type)))
        {
            return true;
        }
        if (type instanceof ParameterizedType)
        {
            return isMap(((ParameterizedType) type).getRawType());
        }
        if (type instanceof WildcardType)
        {
            Type[] upperBounds = ((WildcardType) type).getUpperBounds();
            return ((upperBounds.length != 0) && isMap(upperBounds[0]));
        }
        return false;
    }

    protected boolean isAssignableFrom(Type expectedType, Class clazz)
    {
        if (expectedType instanceof Class)
        {
            if (((Class) expectedType).isPrimitive())
            {
                if (((Class) expectedType).getName().equals("boolean") && (clazz == Boolean.class))
                {
                    return true;
                }
                if (((Class) expectedType).getName().equals("byte") && (clazz == Byte.class))
                {
                    return true;
                }
                if (((Class) expectedType).getName().equals("short") && (clazz == Short.class))
                {
                    return true;
                }
                if (((Class) expectedType).getName().equals("char") && (clazz == Character.class))
                {
                    return true;
                }
                if (((Class) expectedType).getName().equals("int") && (clazz == Integer.class))
                {
                    return true;
                }
                if (((Class) expectedType).getName().equals("float") && (clazz == Float.class))
                {
                    return true;
                }
                if (((Class) expectedType).getName().equals("long") && (clazz == Long.class))
                {
                    return true;
                }
                if (((Class) expectedType).getName().equals("double") && (clazz == Double.class))
                {
                    return true;
                }
                return false;
            }
            else
            {
                return ((Class) expectedType).isAssignableFrom(clazz);
            }
        }
        if (expectedType instanceof ParameterizedType)
        {
            return isAssignableFrom(((ParameterizedType) expectedType).getRawType(), clazz);
        }
        if (expectedType instanceof WildcardType)
        {
            Type[] upperBounds = ((WildcardType) expectedType).getUpperBounds();
            if (upperBounds.length != 0)
            {
                return isAssignableFrom(upperBounds[0], clazz);
            }
        }
        return false;
    }

    protected Object evaluate(TemplateParser.PatternInfo patternInfo, ExpressionManager expressionManager, MuleMessage muleMessage,
            Object source)
    {
        if (source instanceof String)
        {
            String stringSource = ((String) source);
            if (stringSource.startsWith(patternInfo.getPrefix()) && stringSource.endsWith(patternInfo.getSuffix()))
            {
                return expressionManager.evaluate(stringSource, muleMessage);
            }
            else
            {
                return expressionManager.parse(stringSource, muleMessage);
            }
        }
        return source;
    }

    protected Object evaluateAndTransform(MuleContext muleContext, MuleEvent event, Type expectedType, String expectedMimeType,
            Object source)
            throws TransformerException, TransformerMessagingException
    {
        if (source == null)
        {
            return source;
        }
        Object target = null;
        if (isList(source.getClass()))
        {
            if (isList(expectedType))
            {
                List newList = new ArrayList();
                Type valueType = ((ParameterizedType) expectedType).getActualTypeArguments()[0];
                ListIterator iterator = ((List) source).listIterator();
                while (iterator.hasNext())
                {
                    Object subTarget = iterator.next();
                    newList.add(evaluateAndTransform(muleContext, event, valueType, expectedMimeType, subTarget));
                }
                target = newList;
            }
            else
            {
                target = source;
            }
        }
        else
        {
            if (isMap(source.getClass()))
            {
                if (isMap(expectedType))
                {
                    Type keyType = Object.class;
                    Type valueType = Object.class;
                    if (expectedType instanceof ParameterizedType)
                    {
                        keyType = ((ParameterizedType) expectedType).getActualTypeArguments()[0];
                        valueType = ((ParameterizedType) expectedType).getActualTypeArguments()[1];
                    }
                    Map map = ((Map) source);
                    Map newMap = new HashMap();
                    for (Object entryObj : map.entrySet())
                    {
                        {
                            Map.Entry entry = ((Map.Entry) entryObj);
                            Object newKey = evaluateAndTransform(muleContext, event, keyType, expectedMimeType, entry.getKey());
                            Object newValue = evaluateAndTransform(muleContext, event, valueType, expectedMimeType, entry.getValue());
                            newMap.put(newKey, newValue);
                        }
                    }
                    target = newMap;
                }
                else
                {
                    target = source;
                }
            }
            else
            {
                target = evaluate(TemplateParser.createMuleStyleParser().getStyle(), muleContext.getExpressionManager(), event.getMessage(),
                        source);
            }
        }
        return transform(muleContext, event, expectedType, expectedMimeType, target);
    }

    protected Object evaluateAndTransform(MuleContext muleContext, MuleMessage muleMessage, Type expectedType, String expectedMimeType,
            Object source)
            throws TransformerException, TransformerMessagingException
    {
        if (source == null)
        {
            return source;
        }
        Object target = null;
        if (isList(source.getClass()))
        {
            if (isList(expectedType))
            {
                List newList = new ArrayList();
                Type valueType = ((ParameterizedType) expectedType).getActualTypeArguments()[0];
                ListIterator iterator = ((List) source).listIterator();
                while (iterator.hasNext())
                {
                    Object subTarget = iterator.next();
                    newList.add(evaluateAndTransform(muleContext, muleMessage, valueType, expectedMimeType, subTarget));
                }
                target = newList;
            }
            else
            {
                target = source;
            }
        }
        else
        {
            if (isMap(source.getClass()))
            {
                if (isMap(expectedType))
                {
                    Type keyType = Object.class;
                    Type valueType = Object.class;
                    if (expectedType instanceof ParameterizedType)
                    {
                        keyType = ((ParameterizedType) expectedType).getActualTypeArguments()[0];
                        valueType = ((ParameterizedType) expectedType).getActualTypeArguments()[1];
                    }
                    Map map = ((Map) source);
                    Map newMap = new HashMap();
                    for (Object entryObj : map.entrySet())
                    {
                        {
                            Map.Entry entry = ((Map.Entry) entryObj);
                            Object newKey = evaluateAndTransform(muleContext, muleMessage, keyType, expectedMimeType, entry.getKey());
                            Object newValue = evaluateAndTransform(muleContext, muleMessage, valueType, expectedMimeType, entry.getValue());
                            newMap.put(newKey, newValue);
                        }
                    }
                    target = newMap;
                }
                else
                {
                    target = source;
                }
            }
            else
            {
                target = evaluate(TemplateParser.createMuleStyleParser().getStyle(), muleContext.getExpressionManager(), muleMessage,
                        source);
            }
        }
        return transform(muleContext, muleMessage, expectedType, expectedMimeType, target);
    }

    protected Object transform(MuleMessage muleMessage, Type expectedType, Object source)
            throws TransformerException
    {
        if (source == null)
        {
            return source;
        }
        Object target = null;
        if (isList(source.getClass()))
        {
            if (isList(expectedType))
            {
                List newList = new ArrayList();
                Type valueType = ((ParameterizedType) expectedType).getActualTypeArguments()[0];
                ListIterator iterator = ((List) source).listIterator();
                while (iterator.hasNext())
                {
                    Object subTarget = iterator.next();
                    newList.add(transform(muleMessage, valueType, subTarget));
                }
                target = newList;
            }
            else
            {
                target = source;
            }
        }
        else
        {
            if (isMap(source.getClass()))
            {
                if (isMap(expectedType))
                {
                    Type keyType = Object.class;
                    Type valueType = Object.class;
                    if (expectedType instanceof ParameterizedType)
                    {
                        keyType = ((ParameterizedType) expectedType).getActualTypeArguments()[0];
                        valueType = ((ParameterizedType) expectedType).getActualTypeArguments()[1];
                    }
                    Map map = ((Map) source);
                    Map newMap = new HashMap();
                    for (Object entryObj : map.entrySet())
                    {
                        {
                            Map.Entry entry = ((Map.Entry) entryObj);
                            Object newKey = transform(muleMessage, keyType, entry.getKey());
                            Object newValue = transform(muleMessage, valueType, entry.getValue());
                            newMap.put(newKey, newValue);
                        }
                    }
                    target = newMap;
                }
                else
                {
                    target = source;
                }
            }
            else
            {
                target = source;
            }
        }
        if ((target != null) && (!isAssignableFrom(expectedType, target.getClass())))
        {
            DataType sourceDataType = DataTypeFactory.create(target.getClass());
            if (expectedType instanceof ParameterizedType)
            {
                expectedType = ((ParameterizedType) expectedType).getRawType();
            }
            DataType targetDataType = DataTypeFactory.create(((Class) expectedType));
            Transformer t = muleMessage.getMuleContext().getRegistry().lookupTransformer(sourceDataType, targetDataType);
            return t.transform(target);
        }
        else
        {
            return target;
        }
    }

    protected Object transform(MuleContext muleContext, MuleEvent event, Type expectedType, String expectedMimeType, Object source)
            throws TransformerException, TransformerMessagingException
    {
        if ((source != null) && (!isAssignableFrom(expectedType, source.getClass())))
        {
            DataType sourceDataType = DataTypeFactory.create(source.getClass());
            DataType targetDataType = null;
            if (expectedType instanceof ParameterizedType)
            {
                expectedType = ((ParameterizedType) expectedType).getRawType();
            }
            if (expectedMimeType != null)
            {
                targetDataType = DataTypeFactory.create(((Class) expectedType), expectedMimeType);
            }
            else
            {
                targetDataType = DataTypeFactory.create(((Class) expectedType));
            }
            Transformer t = muleContext.getRegistry().lookupTransformer(sourceDataType, targetDataType);
            if (t instanceof MessageTransformer)
            {
                return ((MessageTransformer) t).transform(source, event);
            }
            else
            {
                return t.transform(source);
            }
        }
        else
        {
            return source;
        }
    }

    protected Object transform(MuleContext muleContext, MuleMessage message, Type expectedType, String expectedMimeType, Object source)
            throws TransformerException, TransformerMessagingException
    {
        if ((source != null) && (!isAssignableFrom(expectedType, source.getClass())))
        {
            DataType sourceDataType = DataTypeFactory.create(source.getClass());
            DataType targetDataType = null;
            if (expectedType instanceof ParameterizedType)
            {
                expectedType = ((ParameterizedType) expectedType).getRawType();
            }
            if (expectedMimeType != null)
            {
                targetDataType = DataTypeFactory.create(((Class) expectedType), expectedMimeType);
            }
            else
            {
                targetDataType = DataTypeFactory.create(((Class) expectedType));
            }
            Transformer t = muleContext.getRegistry().lookupTransformer(sourceDataType, targetDataType);
            return t.transform(source);
        }
        else
        {
            return source;
        }
    }

}
