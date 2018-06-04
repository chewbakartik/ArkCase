package com.armedia.acm.plugins.onlyoffice.service;

/*-
 * #%L
 * ACM Extra Plugin: OnlyOffice Integration
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

import com.armedia.acm.core.exceptions.AcmCreateObjectFailedException;
import com.armedia.acm.plugins.ecm.dao.EcmFileDao;
import com.armedia.acm.plugins.ecm.model.EcmFile;
import com.armedia.acm.plugins.ecm.service.EcmFileService;
import com.armedia.acm.plugins.onlyoffice.exceptions.OnlyOfficeException;
import com.armedia.acm.plugins.onlyoffice.model.CallbackResponse;
import com.armedia.acm.plugins.onlyoffice.model.CallbackResponseError;
import com.armedia.acm.plugins.onlyoffice.model.CallbackResponseSuccess;
import com.armedia.acm.plugins.onlyoffice.model.StatusType;
import com.armedia.acm.plugins.onlyoffice.model.callback.CallBackData;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Objects;

public class CallbackServiceImpl implements CallbackService
{
    private Logger logger = LoggerFactory.getLogger(getClass());
    private EcmFileDao ecmFileDao;
    private EcmFileService ecmFileService;

    @Override
    public CallbackResponse handleCallback(CallBackData callBackData, Authentication authentication)
    {
        Objects.nonNull(callBackData);
        logger.debug("handle callback data [{}]", callBackData);
        switch (StatusType.from(callBackData.getStatus()))
        {
        case NO_DOCUMENT_WITH_ID_FOUND:
            return handleNoDocumentWithIdFound(callBackData);
        case BEING_EDITED:
            return handleBeingEdited(callBackData);
        case READY_FOR_SAVING:
            return handleReadyForSaving(callBackData, authentication);
        case SAVING_ERROR_OCCURED:
            return handleSavingErrorOccured(callBackData);
        case CLOSED_NO_CHANGES:
            return handleClosedNoChanges(callBackData);
        case EDITED_BUT_ALREADY_SAVED:
            return handleEditedButAlreadySaved(callBackData);
        case ERROR_WHILE_SAVING:
            return handleErrorWhileSaving(callBackData);
        default:
            return handleUnknownStatusProvided(callBackData);
        }
    }

    private CallbackResponse handleUnknownStatusProvided(CallBackData callBackData)
    {
        logger.debug("handleUnknownStatusProvided.");
        return new CallbackResponseError("Unknown status provided");
    }

    private CallbackResponse handleErrorWhileSaving(CallBackData callBackData)
    {
        logger.debug("handleErrorWhileSaving.");
        return new CallbackResponseSuccess();
    }

    private CallbackResponse handleEditedButAlreadySaved(CallBackData callBackData)
    {
        logger.debug("handleEditedButAlreadySaved.");
        return new CallbackResponseSuccess();
    }

    private CallbackResponse handleClosedNoChanges(CallBackData callBackData)
    {
        // TODO release lock
        logger.debug("handleClosedNoChanges.");
        return new CallbackResponseSuccess();
    }

    private CallbackResponse handleSavingErrorOccured(CallBackData callBackData)
    {
        logger.debug("handleSavingErrorOccured.");
        return new CallbackResponseSuccess();
    }

    private CallbackResponse handleReadyForSaving(CallBackData callBackData, Authentication authentication)
    {
        logger.debug("handleReadyForSaving.");

        java.net.HttpURLConnection connection;
        try
        {
            URL url = new URL(callBackData.getUrl());
            connection = (java.net.HttpURLConnection) url.openConnection();
        }
        catch (IOException e)
        {
            throw new OnlyOfficeException("Provided url [" + callBackData.getUrl() + "] is not accessible.", e);
        }

        try (InputStream stream = connection.getInputStream())
        {
            if (connection.getResponseCode() == 200)
            {
                String key = callBackData.getKey();
                Long fileId = Long.parseLong(key.substring(0, key.indexOf("-")));
                EcmFile ecmFile = ecmFileDao.find(fileId);

                ecmFileService.update(ecmFile, stream, authentication);
                stream.close();
                logger.debug("Document with key [{}] successfully saved to Arkcase.", key);
                // TODO release lock
            }
            else
            {
                // TODO release lock?
                logger.error("File not saved. Got response status [{}]", connection.getResponseCode());
                return new CallbackResponseError("File not saved. Got response status [" + connection.getResponseCode() + "]");
            }
        }
        catch (IOException | AcmCreateObjectFailedException e)
        {
            logger.error("Error saving document. Reason: [{}]", e.getMessage());
            return new CallbackResponseError(e.getMessage());
        }
        return new CallbackResponseSuccess();
    }

    private CallbackResponse handleBeingEdited(CallBackData callBackData)
    {
        // this method is being called when user opens document for editing
        // can't think of any other handling except for logging.
        // TODO update lock information about users who are editing or update information about active documents and
        // users
        logger.debug("handleBeingEdited.");
        return new CallbackResponseSuccess();
    }

    private CallbackResponse handleNoDocumentWithIdFound(CallBackData callBackData)
    {
        logger.error("Document with wrong id[{}] provided, editing is not possible.", callBackData.getKey());
        return new CallbackResponseSuccess();
    }

    public void setEcmFileDao(EcmFileDao ecmFileDao)
    {
        this.ecmFileDao = ecmFileDao;
    }

    public void setEcmFileService(EcmFileService ecmFileService)
    {
        this.ecmFileService = ecmFileService;
    }
}
