package com.armedia.acm.services.notification.service;

/*-
 * #%L
 * ACM Service: Notification
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

import com.armedia.acm.email.model.EmailSenderConfig;
import com.armedia.acm.files.AbstractConfigurationFileEvent;
import com.armedia.acm.files.ConfigurationFileChangedEvent;
import com.armedia.acm.services.email.sender.service.EmailSenderConfigurationServiceImpl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationListener;

import java.util.Map;

public class NotificationSenderFactory implements ApplicationListener<AbstractConfigurationFileEvent>
{
    String flowType = "smtp";
    private Map<String, NotificationSender> notificationSenderMap;
    private EmailSenderConfigurationServiceImpl emailSenderConfigurationService;
    private Logger logger = LoggerFactory.getLogger(getClass());

    public NotificationSender getNotificationSender()
    {
        return notificationSenderMap.get(flowType);
    }

    @Override
    public void onApplicationEvent(AbstractConfigurationFileEvent event)
    {

        if (event instanceof ConfigurationFileChangedEvent && event.getConfigFile().getName().equals("acmEmailSender.properties"))
        {
            EmailSenderConfig senderConfigurationUpdated = emailSenderConfigurationService.readConfiguration();
            flowType = senderConfigurationUpdated.getType();
        }
    }

    /**
     * @param emailSenderConfigurationService
     *            the emailSenderConfigurationService to set
     */
    public void setEmailSenderConfigurationService(EmailSenderConfigurationServiceImpl emailSenderConfigurationService)
    {
        this.emailSenderConfigurationService = emailSenderConfigurationService;
    }

    public void setNotificationSenderMap(Map<String, NotificationSender> notificationSenderMap)
    {
        this.notificationSenderMap = notificationSenderMap;
    }
}
