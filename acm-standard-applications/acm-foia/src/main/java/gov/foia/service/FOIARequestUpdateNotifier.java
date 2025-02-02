package gov.foia.service;

/*-
 * #%L
 * ACM Standard Application: Freedom of Information Act
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

import com.armedia.acm.plugins.casefile.model.CaseEvent;
import com.armedia.acm.plugins.casefile.model.CaseFile;
import com.armedia.acm.plugins.casefile.model.CaseFileConstants;
import com.armedia.acm.plugins.person.model.Person;
import com.armedia.acm.plugins.person.model.PersonAssociation;
import com.fasterxml.jackson.core.JsonProcessingException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationListener;

import javax.jms.JMSException;

import java.util.Date;

import gov.foia.broker.FOIARequestBrokerClient;
import gov.foia.model.PortalFOIARequestStatus;

/**
 * Foia Request update nofifier, Listens for case file update requests and sends notification messages to outbound queue
 *
 * @author dame.gjorgjievski
 */
public class FOIARequestUpdateNotifier implements ApplicationListener<CaseEvent>
{
    private transient Logger LOG = LoggerFactory.getLogger(getClass());

    private FOIARequestBrokerClient brokerClient;

    @Override
    public void onApplicationEvent(CaseEvent event)
    {
        if (CaseFileConstants.EVENT_TYPE_CREATED.equals(event.getEventType().toLowerCase())
                || CaseFileConstants.EVENT_TYPE_UPDATED.equals(event.getEventType().toLowerCase()))
        {
            // sendRequestStatusUpdate(event.getCaseFile());
        }
    }

    /**
     * Populate and send request status update
     *
     * @param caseFile
     */
    private void sendRequestStatusUpdate(CaseFile caseFile)
    {
        PortalFOIARequestStatus status = new PortalFOIARequestStatus();
        status.setRequestId(caseFile.getCaseNumber());
        status.setRequestStatus(caseFile.getStatus());
        status.setUpdateDate(new Date());
        Person person = caseFile.getPersonAssociations().stream().filter(pa -> "Requester".equals(pa.getPersonType())).findFirst()
                .map(PersonAssociation::getPerson).orElse(null);
        if (person != null)
        {
            status.setLastName(person.getFamilyName());

            // String phone = person.getContactMethods().stream().filter(cm -> "Phone".equals(cm.getType())).findFirst()
            // .map(ContactMethod::getValue).orElse(null);
            // status.setPhoneNumber(phone);

            /// String email = person.getContactMethods().stream().filter(cm ->
            /// "Email".equals(cm.getType())).findFirst()
            // .map(ContactMethod::getValue).orElse(null);
            // status.setEmail(email);
        }

        try
        {
            brokerClient.sendObject(status);
        }
        catch (JsonProcessingException | JMSException e)
        {
            LOG.error("Failed to send FOIA request status update", e);
        }
    }

    /**
     * Set broker client for sending request status updates
     *
     * @param brokerClient
     */
    public void setBrokerClient(FOIARequestBrokerClient brokerClient)
    {
        this.brokerClient = brokerClient;
    }

}
