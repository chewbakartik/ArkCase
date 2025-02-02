package com.armedia.acm.services.subscription.model;

/*-
 * #%L
 * ACM Service: Subscription
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

import java.util.Date;

/**
 * Created by marjan.stefanoski on 11.02.2015.
 */
public class SubscriptionDeletedEvent extends SubscriptionPersistenceEvent
{

    private static final String EVENT_TYPE = "com.armedia.acm.subscription.deleted";

    public SubscriptionDeletedEvent(AcmSubscription source)
    {
        super(source);
    }

    public SubscriptionDeletedEvent(String userId, Long objectId, String objectType)
    {
        super(new AcmSubscription());
        setObjectType(objectType);
        setObjectId(objectId);
        setEventDate(new Date());
        setUserId(userId);
    }

    @Override
    public String getEventType()
    {
        return EVENT_TYPE;
    }
}
