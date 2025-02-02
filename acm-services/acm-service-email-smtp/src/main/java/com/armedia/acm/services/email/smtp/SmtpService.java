package com.armedia.acm.services.email.smtp;

/*-
 * #%L
 * ACM Service: Email SMTP
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

import com.armedia.acm.auth.AuthenticationUtils;
import com.armedia.acm.convertfolder.ConversionException;
import com.armedia.acm.convertfolder.DefaultFolderAndFileConverter;
import com.armedia.acm.core.exceptions.AcmUserActionFailedException;
import com.armedia.acm.email.model.EmailSenderConfig;
import com.armedia.acm.muletools.mulecontextmanager.MuleContextManager;
import com.armedia.acm.plugins.ecm.model.EcmFile;
import com.armedia.acm.plugins.ecm.service.EcmFileService;
import com.armedia.acm.services.email.model.AttachmentsProcessableDTO;
import com.armedia.acm.services.email.model.EmailBodyBuilder;
import com.armedia.acm.services.email.model.EmailBuilder;
import com.armedia.acm.services.email.model.EmailWithAttachmentsAndLinksDTO;
import com.armedia.acm.services.email.model.EmailWithAttachmentsDTO;
import com.armedia.acm.services.email.model.EmailWithEmbeddedLinksDTO;
import com.armedia.acm.services.email.model.EmailWithEmbeddedLinksResultDTO;
import com.armedia.acm.services.email.sender.service.EmailSenderConfigurationServiceImpl;
import com.armedia.acm.services.email.service.AcmEmailContentGeneratorService;
import com.armedia.acm.services.email.service.AcmEmailSenderService;
import com.armedia.acm.services.email.service.TemplatingEngine;
import com.armedia.acm.services.users.model.AcmUser;

import org.mule.api.MuleException;
import org.mule.api.MuleMessage;
import org.mule.util.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.security.core.Authentication;

import javax.activation.DataHandler;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * @author Lazo Lazarev a.k.a. Lazarius Borg @ zerogravity Jun 21, 2017
 */
public class SmtpService implements AcmEmailSenderService, ApplicationEventPublisherAware
{

    private final Logger LOG = LoggerFactory.getLogger(getClass());

    private EcmFileService ecmFileService;

    private MuleContextManager muleContextManager;

    private ApplicationEventPublisher eventPublisher;

    private String flow;

    private AcmEmailContentGeneratorService acmEmailContentGeneratorService;

    private EmailSenderConfig emailSenderConfig;

    private TemplatingEngine templatingEngine;

    private EmailSenderConfigurationServiceImpl emailSenderConfigurationService;

    private DefaultFolderAndFileConverter defaultFolderAndFileConverter;

    @Override
    public void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher)
    {
        eventPublisher = applicationEventPublisher;
    }

    /*
     * (non-Javadoc)
     * @see com.armedia.acm.services.email.service.AcmEmailSenderService#sendPlainEmail(java.util.stream.Stream,
     * com.armedia.acm.services.email.model.EmailBuilder, com.armedia.acm.services.email.model.EmailBodyBuilder)
     */
    @Override
    public <T> void sendPlainEmail(Stream<T> emailsDataStream, EmailBuilder<T> emailBuilder, EmailBodyBuilder<T> emailBodyBuilder)
            throws Exception
    {
        Map<String, Object> messageProps = loadSmtpAndOriginatingProperties();

        emailsDataStream.forEach(emailData -> {
            emailBuilder.buildEmail(emailData, messageProps);

            try
            {
                MuleMessage received = muleContextManager.send(flow, emailBodyBuilder.buildEmailBody(emailData), messageProps);
                MuleException exception = received.getInboundProperty("sendEmailException");
                if (exception != null)
                {
                    LOG.error("Email message not sent ...", exception);
                }
            }
            catch (Exception e)
            {
                LOG.error("Email message not sent ...", e);
            }

        });
    }

    /*
     * (non-Javadoc)
     * @see
     * com.armedia.acm.services.email.service.AcmEmailSenderService#sendEmailWithAttachments(com.armedia.acm.services.
     * email.model.EmailWithAttachmentsDTO, org.springframework.security.core.Authentication,
     * com.armedia.acm.services.users.model.AcmUser)
     */
    @Override
    public void sendEmailWithAttachments(EmailWithAttachmentsDTO in, Authentication authentication, AcmUser user) throws Exception
    {
        in.setTemplatingEngine(getTemplatingEngine());

        Exception exception = null;
        Map<String, Object> messageProps = loadSmtpAndOriginatingProperties();
        messageProps.put("subject", in.getSubject());

        List<SmtpEventSentEvent> sentEvents = new ArrayList<>();
        Map<String, DataHandler> attachments = processAttachments(in, user, sentEvents);
        for (String emailAddress : in.getEmailAddresses())
        {
            try
            {
                messageProps.put("to", emailAddress);
                MuleMessage received = muleContextManager.send(flow, in.getMessageBody(), attachments, messageProps);
                exception = received.getInboundProperty("sendEmailException");

            }
            catch (MuleException e)
            {
                LOG.error("Email message not sent ...", exception);
                exception = e;
            }

        }
        for (SmtpEventSentEvent event : sentEvents)
        {
            boolean success = (exception == null);
            event.setSucceeded(success);
            eventPublisher.publishEvent(event);
        }

    }

    /*
     * (non-Javadoc)
     * @see com.armedia.acm.services.email.service.AcmEmailSenderService#sendEmail(com.armedia.acm.services.email.model.
     * EmailWithAttachmentsDTO, org.springframework.security.core.Authentication,
     * com.armedia.acm.services.users.model.AcmUser)
     */
    @Override
    public void sendEmail(EmailWithAttachmentsDTO in, Authentication authentication, AcmUser user) throws Exception
    {
        sendEmailWithAttachments(in, authentication, user);
    }

    /*
     * (non-Javadoc)
     * @see
     * com.armedia.acm.services.email.service.AcmEmailSenderService#sendEmailWithAttachmentsAndLinks(com.armedia.acm.
     * services.email.model.EmailWithAttachmentsAndLinksDTO, org.springframework.security.core.Authentication,
     * com.armedia.acm.services.users.model.AcmUser)
     */
    @Override
    public void sendEmailWithAttachmentsAndLinks(EmailWithAttachmentsAndLinksDTO in, Authentication authentication, AcmUser user)
            throws Exception
    {
        Exception exception = null;

        setFilenames(in);

        Map<String, Object> messageProps = loadSmtpAndOriginatingProperties();
        messageProps.put("subject", in.getSubject());
        List<SmtpEventSentEvent> sentEvents = new ArrayList<>();
        Map<String, DataHandler> attachments = processAttachments(in, user, sentEvents);
        for (String emailAddress : in.getEmailAddresses())
        {
            try
            {
                messageProps.put("to", emailAddress);
                MuleMessage received = muleContextManager.send(flow, makeNote(emailAddress, in, authentication), attachments, messageProps);
                exception = received.getInboundProperty("sendEmailException");

            }
            catch (MuleException e)
            {
                LOG.error("Email message not sent ...", exception);
                exception = e;
            }

        }

        for (SmtpEventSentEvent event : sentEvents)
        {
            boolean success = (exception == null);
            event.setSucceeded(success);
            eventPublisher.publishEvent(event);
        }
    }

    private Long setFilenames(EmailWithEmbeddedLinksDTO in)
    {
        EcmFile ecmFile = null;
        List<String> fileNames = new ArrayList<>();

        if (Objects.nonNull(in.getFileIds()) && in.getFileIds().size() > 0)
        {
            for (int i = 0; i < in.getFileIds().size(); i++)
            {
                ecmFile = ecmFileService.findById(in.getFileIds().get(i));
                fileNames.add(ecmFile.getFileName() + ecmFile.getFileActiveVersionNameExtension());
            }
            in.setFileNames(fileNames);
            return ecmFile.getParentObjectId();
        }

        return null;
    }

    /**
     * @param in
     * @param user
     *            *
     * @param sentEvents
     * @return
     * @throws MuleException
     * @throws AcmUserActionFailedException
     * @throws FileNotFoundException
     */
    private Map<String, DataHandler> processAttachments(AttachmentsProcessableDTO in, AcmUser user, List<SmtpEventSentEvent> sentEvents)
            throws MuleException, AcmUserActionFailedException, FileNotFoundException
    {
        Map<String, DataHandler> attachments = new HashMap<>();
        if (in.getAttachmentIds() != null && !in.getAttachmentIds().isEmpty())
        {
            // add index to make sure the fileKey is unique for AFDP- 5713
            int idx = 1;
            for (Long attachmentId : in.getAttachmentIds())
            {
                EcmFile ecmFile = ecmFileService.findById(attachmentId);
                InputStream contents = ecmFileService.downloadAsInputStream(attachmentId, ecmFile.getActiveVersionTag());
                String fileName = ecmFile.getFileName();
                File pdfConvertedFile = null;

                // add fileKey for AFDP- 5713
                String fileKey = fileName;
                if (attachments.containsKey(fileKey))
                {
                    fileKey = fileKey + "(" + idx + ")";
                    idx++;
                }

                if (ecmFile.getFileActiveVersionNameExtension() != null)
                {

                    fileName = fileName + ecmFile.getFileActiveVersionNameExtension();
                }

                //Convert the Attachment to PDF if set in Admin section
                if (getEmailSenderConfigurationService().readConfiguration().getConvertDocumentsToPdf() &&
                        Objects.nonNull(ecmFile) && !".pdf".equals(ecmFile.getFileActiveVersionNameExtension()))
                {
                    try
                    {
                        pdfConvertedFile = getDefaultFolderAndFileConverter().convertAndReturnConvertedFile(ecmFile);
                    }
                    catch (ConversionException e)
                    {
                        LOG.error(String.format("Could not convert file [%s] to PDF", fileName), e);
                    }

                    if (pdfConvertedFile != null)
                    {

                        try (InputStream pdfConvertedFileInputStream = new FileInputStream(pdfConvertedFile))
                        {
                            contents = pdfConvertedFileInputStream;
                            fileName = fileKey.concat(".pdf");
                            attachments.put(fileKey, new DataHandler(new InputStreamDataSource(contents, fileName)));
                        }
                        catch (IOException e)
                        {
                            LOG.error(String.format("Could not open input stream of file [%s]", fileName), e);
                        }
                        finally
                        {
                            FileUtils.deleteQuietly(pdfConvertedFile);
                        }
                    }
                }
                else
                {
                    attachments.put(fileKey, new DataHandler(new InputStreamDataSource(contents, fileName)));
                }

                String ipAddress = AuthenticationUtils.getUserIpAddress();

                sentEvents
                        .add(new SmtpEventSentEvent(ecmFile, user.getUserId(), ecmFile.getParentObjectId(), ecmFile.getParentObjectType(),
                                ipAddress));
                sentEvents.add(new SmtpEventSentEvent(ecmFile, user.getUserId(), ecmFile.getId(), ecmFile.getObjectType(), ipAddress));
            }
        }

        // Adding non ecmFile(s) as attachments
        if (in.getFilePaths() != null && !in.getFilePaths().isEmpty())
        {
            for (String filePath : in.getFilePaths())
            {
                File file = new File(filePath);
                FileInputStream contents = new FileInputStream(file);
                attachments.put(file.getName(), new DataHandler(new InputStreamDataSource(contents, file.getName())));
            }
        }
        return attachments;
    }

    /*
     * (non-Javadoc)
     * @see com.armedia.acm.services.email.service.AcmEmailSenderService#sendEmail(com.armedia.acm.services.email.model.
     * EmailWithAttachmentsAndLinksDTO, org.springframework.security.core.Authentication,
     * com.armedia.acm.services.users.model.AcmUser)
     */
    @Override
    public void sendEmail(EmailWithAttachmentsAndLinksDTO in, Authentication authentication, AcmUser user) throws Exception
    {
        sendEmailWithAttachmentsAndLinks(in, authentication, user);
    }

    /*
     * (non-Javadoc)
     * @see
     * com.armedia.acm.services.email.service.AcmEmailSenderService#sendEmailWithEmbeddedLinks(com.armedia.acm.services.
     * email.model.EmailWithEmbeddedLinksDTO, org.springframework.security.core.Authentication,
     * com.armedia.acm.services.users.model.AcmUser)
     */
    @Override
    public List<EmailWithEmbeddedLinksResultDTO> sendEmailWithEmbeddedLinks(EmailWithEmbeddedLinksDTO in, Authentication authentication,
            AcmUser user)
    {
        List<EmailWithEmbeddedLinksResultDTO> emailResultList = new ArrayList<>();
        Exception exception = null;

        Long parentId = setFilenames(in);

        Map<String, Object> messageProps = loadSmtpAndOriginatingProperties();
        messageProps.put("subject", in.getSubject());
        for (String emailAddress : in.getEmailAddresses())
        {
            try
            {
                messageProps.put("to", emailAddress);
                MuleMessage received = muleContextManager.send(flow, makeNote(emailAddress, in, authentication), messageProps);
                exception = received.getInboundProperty("sendEmailException");
            }
            catch (MuleException e)
            {
                exception = e;
            }

            if (exception != null)
            {
                emailResultList.add(new EmailWithEmbeddedLinksResultDTO(emailAddress, false));
                LOG.error("Email message not sent ...", exception);
            }
            else
            {
                emailResultList.add(new EmailWithEmbeddedLinksResultDTO(emailAddress, true));
            }

            SmtpSentEventHyperlink event = new SmtpSentEventHyperlink(in, user.getUserId(), parentId, in.getParentType());

            boolean success = (exception == null);
            event.setSucceeded(success);
            eventPublisher.publishEvent(event);
        }
        return emailResultList;

    }

    protected Map<String, Object> loadSmtpAndOriginatingProperties()
    {
        Map<String, Object> messageProps = new HashMap<>();
        messageProps.put("host", emailSenderConfig.getHost());
        messageProps.put("port", emailSenderConfig.getPort());
        messageProps.put("user", emailSenderConfig.getUsername());
        messageProps.put("password", emailSenderConfig.getPassword());
        messageProps.put("from", emailSenderConfig.getUserFrom());
        messageProps.put("encryption", emailSenderConfig.getEncryption());

        return messageProps;
    }

    private String makeNote(String emailAddress, EmailWithEmbeddedLinksDTO emailWithEmbeddedLinksDTO, Authentication authentication)
    {
        return getAcmEmailContentGeneratorService().generateEmailBody(emailWithEmbeddedLinksDTO, emailAddress, authentication);
    }

    /**
     * @param ecmFileService
     *            the ecmFileService to set
     */
    public void setEcmFileService(EcmFileService ecmFileService)
    {
        this.ecmFileService = ecmFileService;
    }

    /**
     * @param muleContextManager
     *            the muleContextManager to set
     */
    public void setMuleContextManager(MuleContextManager muleContextManager)
    {
        this.muleContextManager = muleContextManager;
    }

    public AcmEmailContentGeneratorService getAcmEmailContentGeneratorService()
    {
        return acmEmailContentGeneratorService;
    }

    public void setAcmEmailContentGeneratorService(AcmEmailContentGeneratorService acmEmailContentGeneratorService)
    {
        this.acmEmailContentGeneratorService = acmEmailContentGeneratorService;
    }

    /**
     * @param flow
     *            the flow to set
     */
    public void setFlow(String flow)
    {
        this.flow = flow;
    }

    public EmailSenderConfig getEmailSenderConfig()
    {
        return emailSenderConfig;
    }

    public void setEmailSenderConfig(EmailSenderConfig emailSenderConfig)
    {
        this.emailSenderConfig = emailSenderConfig;
    }

    public TemplatingEngine getTemplatingEngine()
    {
        return templatingEngine;
    }

    public void setTemplatingEngine(TemplatingEngine templatingEngine)
    {
        this.templatingEngine = templatingEngine;
    }

    public EmailSenderConfigurationServiceImpl getEmailSenderConfigurationService()
    {
        return emailSenderConfigurationService;
    }

    public void setEmailSenderConfigurationService(EmailSenderConfigurationServiceImpl emailSenderConfigurationService)
    {
        this.emailSenderConfigurationService = emailSenderConfigurationService;
    }

    public DefaultFolderAndFileConverter getDefaultFolderAndFileConverter()
    {
        return defaultFolderAndFileConverter;
    }

    public void setDefaultFolderAndFileConverter(DefaultFolderAndFileConverter defaultFolderAndFileConverter)
    {
        this.defaultFolderAndFileConverter = defaultFolderAndFileConverter;
    }
}
