
package org.mule.module.cmis.config;

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

import org.mule.module.cmis.processors.CheckInMessageProcessor;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

import javax.annotation.Generated;

@Generated(value = "Mule DevKit Version 3.4.0", date = "2014-05-13T04:20:32-03:00", comments = "Build 3.4.0.1555.8df15c1")
public class CheckInDefinitionParser
        extends AbstractDefinitionParser
{

    public BeanDefinition parse(Element element, ParserContext parserContext)
    {
        BeanDefinitionBuilder builder = BeanDefinitionBuilder.rootBeanDefinition(CheckInMessageProcessor.class.getName());
        builder.setScope(BeanDefinition.SCOPE_PROTOTYPE);
        parseConfigRef(element, builder);
        if (hasAttribute(element, "document-ref"))
        {
            if (element.getAttribute("document-ref").startsWith("#"))
            {
                builder.addPropertyValue("document", element.getAttribute("document-ref"));
            }
            else
            {
                builder.addPropertyValue("document", (("#[registry:" + element.getAttribute("document-ref")) + "]"));
            }
        }
        parseProperty(builder, element, "documentId", "documentId");
        if (hasAttribute(element, "content-ref"))
        {
            if (element.getAttribute("content-ref").startsWith("#"))
            {
                builder.addPropertyValue("content", element.getAttribute("content-ref"));
            }
            else
            {
                builder.addPropertyValue("content", (("#[registry:" + element.getAttribute("content-ref")) + "]"));
            }
        }
        parseProperty(builder, element, "filename", "filename");
        parseProperty(builder, element, "mimeType", "mimeType");
        parseProperty(builder, element, "major", "major");
        parseProperty(builder, element, "checkinComment", "checkinComment");
        parseMapAndSetProperty(element, builder, "properties", "properties", "property", new ParseDelegate<String>()
        {

            public String parse(Element element)
            {
                return element.getTextContent();
            }

        });
        parseProperty(builder, element, "username", "username");
        parseProperty(builder, element, "password", "password");
        parseProperty(builder, element, "baseUrl", "baseUrl");
        parseProperty(builder, element, "repositoryId", "repositoryId");
        parseProperty(builder, element, "endpoint", "endpoint");
        parseProperty(builder, element, "connectionTimeout", "connectionTimeout");
        parseProperty(builder, element, "useAlfrescoExtension", "useAlfrescoExtension");
        parseProperty(builder, element, "cxfPortProvider", "cxfPortProvider");
        BeanDefinition definition = builder.getBeanDefinition();
        setNoRecurseOnDefinition(definition);
        attachProcessorDefinition(parserContext, definition);
        return definition;
    }

}
